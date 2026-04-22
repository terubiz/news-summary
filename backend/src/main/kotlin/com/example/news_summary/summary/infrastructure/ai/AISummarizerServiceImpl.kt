package com.example.news_summary.summary.infrastructure.ai

import com.example.news_summary.domain.index.model.IndexData
import com.example.news_summary.domain.news.model.NewsArticle
import com.example.news_summary.domain.news.model.NewsArticleId
import com.example.news_summary.domain.settings.model.SummarySettings
import com.example.news_summary.domain.summary.model.*
import com.example.news_summary.domain.summary.repository.SummaryIndexImpactRepository
import com.example.news_summary.domain.summary.repository.SummaryRepository
import com.example.news_summary.domain.summary.service.AISummarizerService
import com.example.news_summary.summary.application.service.SummaryPromptBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * AI要約サービスのインフラ層実装。
 * Spring AI の ChatClient を使って Claude Sonnet を呼び出し、
 * 要約結果をパースして Summary + SummaryIndexImpact を DB 保存する。
 */
@Service
class AISummarizerServiceImpl(
    private val chatClientBuilder: ChatClient.Builder,
    private val promptBuilder: SummaryPromptBuilder,
    private val summaryRepository: SummaryRepository,
    private val summaryIndexImpactRepository: SummaryIndexImpactRepository,
    private val objectMapper: ObjectMapper
) : AISummarizerService {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val chatClient: ChatClient by lazy { chatClientBuilder.build() }

    @Transactional
    override fun summarize(
        articles: List<NewsArticle>,
        indices: List<IndexData>,
        settings: SummarySettings,
        userId: Long
    ): Summary {
        val prompt = promptBuilder.build(articles, indices, settings)
        val articleIds = articles.mapNotNull { it.id }.toSet()

        return try {
            val responseText = callLlm(prompt)
            val parsed = parseResponse(responseText)

            // Summary を保存
            val summary = summaryRepository.save(
                Summary(
                    id = SummaryId(0),
                    userId = userId,
                    summaryText = parsed.summaryText,
                    supplementLevel = settings.supplementLevel,
                    summaryMode = settings.summaryMode,
                    status = SummaryStatus.COMPLETED,
                    retryCount = 0,
                    generatedAt = Instant.now(),
                    sourceArticleIds = articleIds
                )
            )

            // SummaryIndexImpact を保存
            if (parsed.indexImpacts.isNotEmpty()) {
                val impacts = parsed.indexImpacts.map { impact ->
                    SummaryIndexImpact(
                        id = SummaryIndexImpactId(0),
                        summaryId = summary.id.value,
                        indexSymbol = impact.indexSymbol,
                        impactDirection = parseImpactDirection(impact.impactDirection)
                    )
                }
                summaryIndexImpactRepository.saveAll(impacts)
            }

            logger.info("要約生成完了: summaryId=${summary.id.value}, userId=$userId, articles=${articles.size}件")
            summary
        } catch (e: Exception) {
            logger.error("要約生成失敗: userId=$userId, error=${e.message}", e)
            // FAILED ステータスで保存し、retryCount++ で記録
            summaryRepository.save(
                Summary(
                    id = SummaryId(0),
                    userId = userId,
                    summaryText = "",
                    supplementLevel = settings.supplementLevel,
                    summaryMode = settings.summaryMode,
                    status = SummaryStatus.FAILED,
                    retryCount = 1,
                    generatedAt = Instant.now(),
                    sourceArticleIds = articleIds
                )
            )
        }
    }

    @Transactional
    override fun retryFailedSummaries() {
        val targets = summaryRepository.findRetryTargets()
        logger.info("リトライ対象の要約: ${targets.size}件")

        targets.forEach { summary ->
            val backoffMs = calculateBackoff(summary.retryCount)
            try {
                Thread.sleep(backoffMs)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return
            }

            try {
                // 既存の要約テキストが空の場合、再生成は元記事が必要だが
                // ここではステータスのみ更新する簡易リトライ
                val updatedSummary = summary.copy(
                    retryCount = summary.retryCount + 1,
                    status = SummaryStatus.FAILED
                )
                summaryRepository.save(updatedSummary)
                logger.warn("要約リトライ失敗（記事データ不足）: summaryId=${summary.id.value}, retryCount=${updatedSummary.retryCount}")
            } catch (e: Exception) {
                logger.error("要約リトライ中にエラー: summaryId=${summary.id.value}, error=${e.message}", e)
                summaryRepository.save(
                    summary.copy(
                        retryCount = summary.retryCount + 1,
                        status = SummaryStatus.FAILED
                    )
                )
            }
        }
    }

    /** LLM API を呼び出す */
    private fun callLlm(prompt: String): String {
        val response = chatClient.prompt()
            .user(prompt)
            .call()
            .content() ?: throw RuntimeException("LLM APIからの応答が空です")
        return response
    }

    /** LLM応答をパースする */
    private fun parseResponse(responseText: String): LlmSummaryResponse {
        return try {
            // JSON部分を抽出（LLMが余分なテキストを含む場合に対応）
            val jsonStr = extractJson(responseText)
            objectMapper.readValue(jsonStr, LlmSummaryResponse::class.java)
        } catch (e: Exception) {
            logger.warn("JSON パース失敗、プレーンテキストとして処理: ${e.message}")
            LlmSummaryResponse(
                summaryText = responseText.take(600),
                indexImpacts = emptyList()
            )
        }
    }

    /** レスポンスからJSON部分を抽出する */
    private fun extractJson(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start == -1 || end == -1 || start >= end) {
            throw IllegalArgumentException("JSONが見つかりません: $text")
        }
        return text.substring(start, end + 1)
    }

    /** 影響方向文字列をenumに変換する */
    private fun parseImpactDirection(direction: String): ImpactDirection = try {
        ImpactDirection.valueOf(direction.uppercase())
    } catch (_: IllegalArgumentException) {
        ImpactDirection.NEUTRAL
    }

    /** 指数バックオフ計算（1回目: 2秒、2回目: 4秒） */
    private fun calculateBackoff(retryCount: Int): Long =
        (1000L * Math.pow(2.0, retryCount.toDouble())).toLong()

    /** LLM応答のパース用データクラス */
    data class LlmSummaryResponse(
        val summaryText: String = "",
        val indexImpacts: List<LlmIndexImpact> = emptyList()
    )

    data class LlmIndexImpact(
        val indexSymbol: String = "",
        val impactDirection: String = "NEUTRAL"
    )
}
