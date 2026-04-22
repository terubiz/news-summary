package com.example.news_summary.summary.application.listener

import com.example.news_summary.domain.news.repository.NewsArticleRepository
import com.example.news_summary.domain.settings.repository.SummarySettingsRepository
import com.example.news_summary.domain.shared.event.NewsCollectedEvent
import com.example.news_summary.domain.summary.service.AISummarizerService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * ニュース収集完了イベントリスナー。
 * NewsCollectedEvent を受け取り、GeminiSummarizerService で要約を生成する。
 *
 * @Async で非同期実行し、収集APIのレスポンスをブロックしない。
 */
@Component
class NewsCollectedEventListener(
    private val aiSummarizerService: AISummarizerService,
    private val newsArticleRepository: NewsArticleRepository,
    private val summarySettingsRepository: SummarySettingsRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @EventListener
    fun handle(event: NewsCollectedEvent) {
        logger.info("NewsCollectedEvent受信: userId=${event.userId.value}, articles=${event.articleIds.size}件")

        try {
            val articles = newsArticleRepository.findByIds(event.articleIds)
            if (articles.isEmpty()) {
                logger.warn("要約対象の記事が見つかりません")
                return
            }

            // ユーザーの要約設定を取得（なければデフォルト）
            val settings = summarySettingsRepository.findByUserId(event.userId)
                ?: com.example.news_summary.domain.settings.model.NewSummarySettings(
                    userId = event.userId,
                    selectedIndices = listOf("日経225", "S&P500", "NASDAQ", "DAX"),
                    analysisPerspectives = emptyList()
                ).let {
                    summarySettingsRepository.save(it)
                }

            // 指数名リスト（設定から取得、空ならデフォルト）
            val indexNames = settings.selectedIndices.ifEmpty {
                listOf("日経225", "S&P500", "NASDAQ", "DAX")
            }

            val summary = aiSummarizerService.summarize(
                articles = articles,
                indexNames = indexNames,
                settings = settings,
                userId = event.userId.value
            )

            logger.info("要約生成完了: summaryId=${summary.id.value}, userId=${event.userId.value}")
        } catch (e: Exception) {
            logger.error("要約生成失敗: userId=${event.userId.value}, error=${e.message}", e)
        }
    }
}
