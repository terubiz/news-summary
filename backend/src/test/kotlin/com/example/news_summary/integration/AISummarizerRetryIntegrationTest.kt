package com.example.news_summary.integration

import com.example.news_summary.domain.index.model.IndexData
import com.example.news_summary.domain.news.model.NewsArticle
import com.example.news_summary.domain.news.model.NewsArticleId
import com.example.news_summary.domain.settings.model.SummarySettings
import com.example.news_summary.domain.shared.service.SsePublisher
import com.example.news_summary.domain.summary.model.*
import com.example.news_summary.domain.summary.repository.SummaryIndexImpactRepository
import com.example.news_summary.domain.summary.repository.SummaryRepository
import com.example.news_summary.domain.user.model.UserId
import com.example.news_summary.summary.application.service.SummaryPromptBuilder
import com.example.news_summary.summary.infrastructure.ai.AISummarizerServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.ai.chat.client.ChatClient
import java.math.BigDecimal
import java.time.Instant

/**
 * LLM APIリトライ統合テスト。
 *
 * ChatClient をモック（Mockito）して LLM API 失敗→リトライのシナリオを検証する。
 * @SpringBootTest は使わず、モックベースで軽量に検証する。
 */
@DisplayName("LLM APIリトライ統合テスト")
class AISummarizerRetryIntegrationTest {

    private lateinit var chatClientBuilder: ChatClient.Builder
    private lateinit var chatClient: ChatClient
    private lateinit var promptBuilder: SummaryPromptBuilder
    private lateinit var summaryRepository: SummaryRepository
    private lateinit var summaryIndexImpactRepository: SummaryIndexImpactRepository
    private lateinit var ssePublisher: SsePublisher
    private lateinit var service: AISummarizerServiceImpl

    private val objectMapper = ObjectMapper()
    private val testUserId = 1L

    private val testArticles = listOf(
        NewsArticle(
            id = NewsArticleId(1L),
            title = "テスト経済ニュース",
            content = "テスト内容",
            sourceUrl = "https://example.com/1",
            sourceName = "TestSource",
            publishedAt = Instant.parse("2024-01-01T00:00:00Z")
        )
    )

    private val testIndices = listOf(
        IndexData(
            symbol = "N225",
            currentValue = BigDecimal("38000.00"),
            changeAmount = BigDecimal("200.00"),
            changeRate = BigDecimal("0.53")
        )
    )

    private val testSettings = SummarySettings(
        userId = UserId(testUserId),
        selectedIndices = listOf("N225"),
        supplementLevel = SupplementLevel.INTERMEDIATE,
        summaryMode = SummaryMode.STANDARD
    )

    @BeforeEach
    fun setUp() {
        chatClient = mock()
        chatClientBuilder = mock()
        whenever(chatClientBuilder.build()).thenReturn(chatClient)

        promptBuilder = SummaryPromptBuilder()
        summaryRepository = mock()
        summaryIndexImpactRepository = mock()
        ssePublisher = mock()

        service = AISummarizerServiceImpl(
            chatClientBuilder = chatClientBuilder,
            promptBuilder = promptBuilder,
            summaryRepository = summaryRepository,
            summaryIndexImpactRepository = summaryIndexImpactRepository,
            objectMapper = objectMapper,
            ssePublisher = ssePublisher
        )

        // summaryRepository.save はIDを付与して返す
        var summaryIdCounter = 1L
        whenever(summaryRepository.save(any())).thenAnswer { invocation ->
            val summary = invocation.getArgument<Summary>(0)
            summary.copy(id = SummaryId(summaryIdCounter++))
        }

        whenever(summaryIndexImpactRepository.saveAll(any())).thenAnswer { invocation ->
            invocation.getArgument<List<SummaryIndexImpact>>(0)
        }
    }

    // -------------------------------------------------------
    // LLM API失敗 → status=FAILED, retryCount=1 で保存
    // -------------------------------------------------------

    @Test
    @DisplayName("LLM API失敗時にstatus=FAILED, retryCount=1で保存される")
    fun `should save with FAILED status and retryCount 1 when LLM API fails`() {
        // Arrange: ChatClient が例外を投げる
        val promptSpec = mock<ChatClient.ChatClientRequestSpec>()
        val callSpec = mock<ChatClient.CallResponseSpec>()
        whenever(chatClient.prompt()).thenReturn(promptSpec)
        whenever(promptSpec.user(any<String>())).thenReturn(promptSpec)
        whenever(promptSpec.call()).thenReturn(callSpec)
        whenever(callSpec.content()).thenThrow(RuntimeException("LLM API接続エラー"))

        // Act
        val result = service.summarize(testArticles, testIndices, testSettings, testUserId)

        // Assert: FAILED ステータスで保存
        assertEquals(SummaryStatus.FAILED, result.status)
        assertEquals(1, result.retryCount)
        assertEquals("", result.summaryText)

        // save が1回呼ばれた（FAILED保存）
        verify(summaryRepository, times(1)).save(argThat<Summary> {
            this.status == SummaryStatus.FAILED && this.retryCount == 1
        })

        // SSEイベントは発行されない
        verify(ssePublisher, never()).publishSummaryCreated(any())
    }

    // -------------------------------------------------------
    // retryFailedSummaries() で再試行される
    // -------------------------------------------------------

    @Test
    @DisplayName("retryFailedSummaries()でFAILED要約が再試行される")
    fun `should retry failed summaries with incremented retryCount`() {
        // Arrange: リトライ対象の要約
        val failedSummary = Summary(
            id = SummaryId(10L),
            userId = testUserId,
            summaryText = "",
            supplementLevel = SupplementLevel.INTERMEDIATE,
            summaryMode = SummaryMode.STANDARD,
            status = SummaryStatus.FAILED,
            retryCount = 1,
            generatedAt = Instant.now(),
            sourceArticleIds = setOf(NewsArticleId(1L))
        )
        whenever(summaryRepository.findRetryTargets()).thenReturn(listOf(failedSummary))

        // Act
        service.retryFailedSummaries()

        // Assert: retryCount が +1 されて保存される
        verify(summaryRepository).save(argThat<Summary> {
            this.id == SummaryId(10L) && this.retryCount == 2
        })
    }

    @Test
    @DisplayName("retryCount=2の要約がリトライされるとretryCount=3になる")
    fun `should increment retryCount to 3 on second retry`() {
        val failedSummary = Summary(
            id = SummaryId(20L),
            userId = testUserId,
            summaryText = "",
            supplementLevel = SupplementLevel.INTERMEDIATE,
            summaryMode = SummaryMode.STANDARD,
            status = SummaryStatus.FAILED,
            retryCount = 2,
            generatedAt = Instant.now(),
            sourceArticleIds = setOf(NewsArticleId(1L))
        )
        whenever(summaryRepository.findRetryTargets()).thenReturn(listOf(failedSummary))

        service.retryFailedSummaries()

        verify(summaryRepository).save(argThat<Summary> {
            this.id == SummaryId(20L) && this.retryCount == 3
        })
    }

    // -------------------------------------------------------
    // 正常系: LLM API成功時の検証
    // -------------------------------------------------------

    @Test
    @DisplayName("LLM API成功時にstatus=COMPLETED, retryCount=0で保存される")
    fun `should save with COMPLETED status when LLM API succeeds`() {
        // Arrange: ChatClient が正常レスポンスを返す
        val jsonResponse = """
            {
                "summaryText": "テスト要約テキスト",
                "indexImpacts": [
                    {"indexSymbol": "N225", "impactDirection": "BULLISH"}
                ]
            }
        """.trimIndent()

        val promptSpec = mock<ChatClient.ChatClientRequestSpec>()
        val callSpec = mock<ChatClient.CallResponseSpec>()
        whenever(chatClient.prompt()).thenReturn(promptSpec)
        whenever(promptSpec.user(any<String>())).thenReturn(promptSpec)
        whenever(promptSpec.call()).thenReturn(callSpec)
        whenever(callSpec.content()).thenReturn(jsonResponse)

        // Act
        val result = service.summarize(testArticles, testIndices, testSettings, testUserId)

        // Assert: COMPLETED ステータスで保存
        assertEquals(SummaryStatus.COMPLETED, result.status)
        assertEquals(0, result.retryCount)
        assertEquals("テスト要約テキスト", result.summaryText)

        // SummaryIndexImpact も保存される
        verify(summaryIndexImpactRepository).saveAll(argThat<List<SummaryIndexImpact>> {
            this.size == 1 && this[0].indexSymbol == "N225" && this[0].impactDirection == ImpactDirection.BULLISH
        })

        // SSEイベントが発行される
        verify(ssePublisher).publishSummaryCreated(any())
    }

    @Test
    @DisplayName("リトライ対象がない場合は何もしない")
    fun `should do nothing when no retry targets exist`() {
        whenever(summaryRepository.findRetryTargets()).thenReturn(emptyList())

        service.retryFailedSummaries()

        verify(summaryRepository, never()).save(any())
    }
}
