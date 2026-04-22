package com.example.news_summary.news.application.usecase

import com.example.news_summary.domain.news.model.NewCollectionLog
import com.example.news_summary.domain.news.model.NewNewsArticle
import com.example.news_summary.domain.news.repository.CollectionLogRepository
import com.example.news_summary.domain.news.repository.NewsArticleRepository
import com.example.news_summary.domain.news.service.CollectionResult
import com.example.news_summary.domain.news.service.NewsApiClient
import com.example.news_summary.domain.shared.event.NewsCollectedEvent
import com.example.news_summary.domain.user.model.UserId
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * ニュース収集ユースケース。
 *
 * 処理フロー:
 * 1. NewsApiClient.fetchLatestNews() で外部APIから記事を取得
 * 2. 各記事に対して重複チェック（sourceUrl or title）
 * 3. 重複なしの記事をDB保存
 * 4. 収集ログを記録
 * 5. NewsCollectedEvent を発行 → AI要約処理をトリガー
 */
@Service
class CollectNewsUseCase(
    private val newsApiClient: NewsApiClient,
    private val articleRepository: NewsArticleRepository,
    private val collectionLogRepository: CollectionLogRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun execute(userId: UserId): CollectionResult {
        val rawArticles = newsApiClient.fetchLatestNews("economy stock market finance")

        var savedCount = 0
        var skippedCount = 0
        val savedArticleIds = mutableListOf<com.example.news_summary.domain.news.model.NewsArticleId>()
        val errors = mutableListOf<String>()

        for (raw in rawArticles) {
            try {
                // 重複チェック（要件1.5: 同一URL or 同一タイトル）
                if (articleRepository.existsBySourceUrl(raw.sourceUrl) ||
                    articleRepository.existsByTitle(raw.title)) {
                    skippedCount++
                    continue
                }

                val article = NewNewsArticle(
                    title = raw.title,
                    content = raw.content,
                    sourceUrl = raw.sourceUrl,
                    sourceName = raw.sourceName,
                    publishedAt = parseInstant(raw.publishedAt)
                )
                val saved = articleRepository.save(article)
                savedArticleIds.add(saved.id)
                savedCount++
            } catch (e: Exception) {
                logger.error("記事保存失敗: ${raw.title} - ${e.message}")
                errors.add("${raw.title}: ${e.message}")
            }
        }

        // 収集ログ記録（要件1.6）
        val status = if (errors.isEmpty()) "SUCCESS" else "PARTIAL"
        collectionLogRepository.save(
            NewCollectionLog(
                userId = userId,
                articleCount = savedCount,
                status = status,
                errorMessage = errors.takeIf { it.isNotEmpty() }?.joinToString("; ")
            )
        )

        // ドメインイベント発行（要件1.7: 収集完了後にAI要約をトリガー）
        if (savedArticleIds.isNotEmpty()) {
            eventPublisher.publishEvent(NewsCollectedEvent(userId, savedArticleIds))
            logger.info("ニュース収集完了: ${savedCount}件保存, ${skippedCount}件スキップ, ${errors.size}件エラー")
        }

        return CollectionResult(
            savedCount = savedCount,
            skippedCount = skippedCount,
            errorCount = errors.size,
            errors = errors
        )
    }

    private fun parseInstant(dateStr: String): Instant = try {
        Instant.parse(dateStr)
    } catch (e: Exception) {
        Instant.now()
    }
}
