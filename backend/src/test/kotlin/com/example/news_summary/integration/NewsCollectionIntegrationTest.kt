package com.example.news_summary.integration

import com.example.news_summary.domain.news.model.CollectionLog
import com.example.news_summary.domain.news.model.CollectionLogId
import com.example.news_summary.domain.news.model.NewsArticle
import com.example.news_summary.domain.news.model.NewsArticleId
import com.example.news_summary.domain.news.repository.CollectionLogRepository
import com.example.news_summary.domain.news.repository.NewsArticleRepository
import com.example.news_summary.domain.news.service.NewsApiClient
import com.example.news_summary.domain.news.service.RawNewsArticle
import com.example.news_summary.domain.shared.event.NewsCollectedEvent
import com.example.news_summary.domain.user.model.UserId
import com.example.news_summary.news.application.usecase.CollectNewsUseCase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant

/**
 * ニュース収集エンドツーエンド統合テスト。
 *
 * CollectNewsUseCase.execute() を呼び出し、
 * 記事取得 → 重複排除 → DB保存 → 収集ログ記録の一連フローを検証する。
 * @SpringBootTest は使わず、モックベースで軽量に検証する。
 */
@DisplayName("ニュース収集エンドツーエンド統合テスト")
class NewsCollectionIntegrationTest {

    private lateinit var newsApiClient: NewsApiClient
    private lateinit var articleRepository: NewsArticleRepository
    private lateinit var collectionLogRepository: CollectionLogRepository
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var useCase: CollectNewsUseCase

    private val testUserId = UserId(1L)

    @BeforeEach
    fun setUp() {
        newsApiClient = mock()
        articleRepository = mock()
        collectionLogRepository = mock()
        eventPublisher = mock()

        useCase = CollectNewsUseCase(
            newsApiClient = newsApiClient,
            articleRepository = articleRepository,
            collectionLogRepository = collectionLogRepository,
            eventPublisher = eventPublisher
        )

        // デフォルト: 重複なし
        whenever(articleRepository.existsBySourceUrl(any())).thenReturn(false)
        whenever(articleRepository.existsByTitle(any())).thenReturn(false)

        // save は引数をそのまま返す（IDを付与）
        var articleIdCounter = 100L
        whenever(articleRepository.save(any())).thenAnswer { invocation ->
            val article = invocation.getArgument<NewsArticle>(0)
            article.copy(id = NewsArticleId(articleIdCounter++), collectedAt = Instant.now())
        }

        whenever(collectionLogRepository.save(any())).thenAnswer { invocation ->
            val log = invocation.getArgument<CollectionLog>(0)
            log.copy(id = CollectionLogId(1L), executedAt = Instant.now())
        }
    }

    // -------------------------------------------------------
    // 正常系: 記事取得 → 保存 → ログ記録 → イベント発行
    // -------------------------------------------------------

    @Test
    @DisplayName("正常レスポンス: 記事取得→重複排除→DB保存→収集ログ記録→イベント発行の一連フロー")
    fun `should collect articles and save to DB with collection log and event`() {
        // Arrange: NewsAPIが3件の記事を返す
        val rawArticles = listOf(
            RawNewsArticle("経済ニュース1", "内容1", "https://example.com/1", "Source1", "2024-01-01T00:00:00Z"),
            RawNewsArticle("経済ニュース2", "内容2", "https://example.com/2", "Source2", "2024-01-02T00:00:00Z"),
            RawNewsArticle("経済ニュース3", "内容3", "https://example.com/3", "Source3", "2024-01-03T00:00:00Z")
        )
        whenever(newsApiClient.fetchLatestNews(any())).thenReturn(rawArticles)

        // Act
        val result = useCase.execute(testUserId)

        // Assert: 3件すべて保存
        assertEquals(3, result.savedCount)
        assertEquals(0, result.skippedCount)
        assertEquals(0, result.errorCount)

        // DB保存が3回呼ばれた
        verify(articleRepository, times(3)).save(any())

        // 収集ログが記録された
        verify(collectionLogRepository).save(argThat<CollectionLog> {
            this.articleCount == 3 && this.status == "SUCCESS" && this.userId == testUserId
        })

        // NewsCollectedEvent が発行された
        verify(eventPublisher).publishEvent(argThat<NewsCollectedEvent> {
            this.userId == testUserId && this.articleIds.size == 3
        })
    }

    // -------------------------------------------------------
    // 重複排除: 2回目の実行で重複記事がスキップされる
    // -------------------------------------------------------

    @Test
    @DisplayName("2回目の実行で重複記事がスキップされる")
    fun `should skip duplicate articles on second execution`() {
        // Arrange: 同じ記事を2回返す
        val rawArticles = listOf(
            RawNewsArticle("経済ニュース1", "内容1", "https://example.com/1", "Source1", "2024-01-01T00:00:00Z"),
            RawNewsArticle("経済ニュース2", "内容2", "https://example.com/2", "Source2", "2024-01-02T00:00:00Z")
        )
        whenever(newsApiClient.fetchLatestNews(any())).thenReturn(rawArticles)

        // 1回目: 重複なし
        val result1 = useCase.execute(testUserId)
        assertEquals(2, result1.savedCount)
        assertEquals(0, result1.skippedCount)

        // 2回目: URL重複を検出するようモックを更新
        whenever(articleRepository.existsBySourceUrl("https://example.com/1")).thenReturn(true)
        whenever(articleRepository.existsBySourceUrl("https://example.com/2")).thenReturn(true)

        val result2 = useCase.execute(testUserId)

        // Assert: 2回目は全件スキップ
        assertEquals(0, result2.savedCount)
        assertEquals(2, result2.skippedCount)
        assertEquals(0, result2.errorCount)
    }

    @Test
    @DisplayName("タイトル重複でもスキップされる")
    fun `should skip articles with duplicate title`() {
        val rawArticles = listOf(
            RawNewsArticle("同じタイトル", "内容A", "https://example.com/a", "SourceA", "2024-01-01T00:00:00Z"),
            RawNewsArticle("同じタイトル", "内容B", "https://example.com/b", "SourceB", "2024-01-02T00:00:00Z")
        )
        whenever(newsApiClient.fetchLatestNews(any())).thenReturn(rawArticles)

        // 1件目は保存成功、2件目はタイトル重複
        whenever(articleRepository.existsByTitle("同じタイトル"))
            .thenReturn(false)  // 1件目
            .thenReturn(true)   // 2件目

        val result = useCase.execute(testUserId)

        assertEquals(1, result.savedCount)
        assertEquals(1, result.skippedCount)
    }

    // -------------------------------------------------------
    // エラー系: NewsAPIがエラーレスポンスを返す（空リスト）
    // -------------------------------------------------------

    @Test
    @DisplayName("NewsAPIがエラー時は空リストを返し、収集ログにSUCCESSが記録される")
    fun `should handle empty response from NewsAPI gracefully`() {
        // Arrange: NewsAPIが空リストを返す（接続失敗時の仕様）
        whenever(newsApiClient.fetchLatestNews(any())).thenReturn(emptyList())

        // Act
        val result = useCase.execute(testUserId)

        // Assert
        assertEquals(0, result.savedCount)
        assertEquals(0, result.skippedCount)
        assertEquals(0, result.errorCount)

        // 収集ログは記録される（0件でもログは残す）
        verify(collectionLogRepository).save(argThat<CollectionLog> {
            this.articleCount == 0 && this.status == "SUCCESS"
        })

        // イベントは発行されない（保存記事なし）
        verify(eventPublisher, never()).publishEvent(any<NewsCollectedEvent>())
    }

    @Test
    @DisplayName("記事保存中に例外が発生した場合、エラーが記録されPARTIALステータスになる")
    fun `should record errors and set PARTIAL status when save fails`() {
        val rawArticles = listOf(
            RawNewsArticle("正常記事", "内容1", "https://example.com/ok", "Source1", "2024-01-01T00:00:00Z"),
            RawNewsArticle("エラー記事", "内容2", "https://example.com/err", "Source2", "2024-01-02T00:00:00Z")
        )
        whenever(newsApiClient.fetchLatestNews(any())).thenReturn(rawArticles)

        // 2件目の保存で例外
        var callCount = 0
        whenever(articleRepository.save(any())).thenAnswer { invocation ->
            callCount++
            if (callCount == 2) throw RuntimeException("DB接続エラー")
            val article = invocation.getArgument<NewsArticle>(0)
            article.copy(id = NewsArticleId(100L), collectedAt = Instant.now())
        }

        val result = useCase.execute(testUserId)

        assertEquals(1, result.savedCount)
        assertEquals(0, result.skippedCount)
        assertEquals(1, result.errorCount)
        assertTrue(result.errors.any { it.contains("DB接続エラー") })

        // 収集ログのステータスはPARTIAL
        verify(collectionLogRepository).save(argThat<CollectionLog> {
            this.status == "PARTIAL" && this.articleCount == 1
        })
    }

    @Test
    @DisplayName("URL重複とタイトル重複が混在する場合、正しくカウントされる")
    fun `should correctly count mixed duplicates`() {
        val rawArticles = listOf(
            RawNewsArticle("新規記事", "内容1", "https://example.com/new", "Source1", "2024-01-01T00:00:00Z"),
            RawNewsArticle("URL重複", "内容2", "https://example.com/dup-url", "Source2", "2024-01-02T00:00:00Z"),
            RawNewsArticle("タイトル重複", "内容3", "https://example.com/unique", "Source3", "2024-01-03T00:00:00Z")
        )
        whenever(newsApiClient.fetchLatestNews(any())).thenReturn(rawArticles)

        // URL重複
        whenever(articleRepository.existsBySourceUrl("https://example.com/dup-url")).thenReturn(true)
        // タイトル重複
        whenever(articleRepository.existsByTitle("タイトル重複")).thenReturn(true)

        val result = useCase.execute(testUserId)

        assertEquals(1, result.savedCount)
        assertEquals(2, result.skippedCount)
    }
}
