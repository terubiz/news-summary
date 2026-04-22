package com.example.news_summary.summary.infrastructure.ai

import com.example.news_summary.domain.news.model.NewsArticle
import com.example.news_summary.domain.settings.model.SummarySettings
import com.example.news_summary.domain.shared.service.SsePublisher
import com.example.news_summary.domain.summary.model.*
import com.example.news_summary.domain.summary.repository.SummaryIndexImpactRepository
import com.example.news_summary.domain.summary.repository.SummaryRepository
import com.example.news_summary.domain.summary.service.AISummarizerService
import com.example.news_summary.summary.application.service.SummaryPromptBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.GoogleSearch
import com.google.genai.types.Tool
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Gemini ネイティブSDK を使った AI要約サービス実装。
 *
 * Google Search Grounding を有効にし、Gemini が株価指数の最新値を
 * リアルタイムで検索して要約に含める。
 *
 * 将来的に別AIプロバイダーに切り替える場合は、AISummarizerService の
 * 新しい具象クラスを作成し、@Primary や @Profile で切り替える。
 */
@Service
class GeminiSummarizerService(
    @Value("\${app.gemini.api-key}") private val apiKey: String,
    @Value("\${app.gemini.model:gemini-2.0-flash}") private val modelName: String,
    private val promptBuilder: SummaryPromptBuilder,
    private val summaryRepository: SummaryRepository,
    private val summaryIndexImpactRepository: SummaryIndexImpactRepository,
    private val objectMapper: ObjectMapper,
    private val ssePublisher: SsePublisher
) : AISummarizerService {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val client: Client by lazy {
        Client.builder().apiKey(apiKey).build()
    }

    @Transactional
    override fun summarize(
        articles: List<NewsArticle>,
        indexNames: List<String>,
        settings: SummarySettings,
        userId: Long
    ): Summary {
        val prompt = promptBuilder.build(articles, indexNames, settings)
        val articleIds = articles.mapNotNull { it.id }.toSet()

        return try {
            val responseText = callGeminiWithSearchGrounding(prompt)
            val parsed = parseResponse(responseText)

            val summary = summaryRepository.save(
                NewSummary(
                    userId = userId,
                    summaryText = parsed.summaryText,
                    supplementLevel = settings.supplementLevel,
                    summaryMode = settings.summaryMode,
                    status = SummaryStatus.COMPLETED,
                    retryCount = 0,
                    sourceArticleIds = articleIds
                )
            )

            if (parsed.indexImpacts.isNotEmpty()) {
                val impacts = parsed.indexImpacts.map { impact ->
                    NewSummaryIndexImpact(
                        summaryId = summary.id.value,
                        indexSymbol = impact.indexSymbol,
                        impactDirection = parseImpactDirection(impact.impactDirection)
                    )
                }
                summaryIndexImpactRepository.saveAll(impacts)
            }

            logger.info("要約生成完了: summaryId=${summary.id.value}, userId=$userId, articles=${articles.size}件")
            ssePublisher.publishSummaryCreated(summary)
            summary
        } catch (e: Exception) {
            logger.error("要約生成失敗: userId=$userId, error=${e.message}", e)
            summaryRepository.save(
                NewSummary(
                    userId = userId,
                    summaryText = "",
                    supplementLevel = settings.supplementLevel,
                    summaryMode = settings.summaryMode,
                    status = SummaryStatus.FAILED,
                    retryCount = 1,
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
            val backoffMs = (1000L * Math.pow(2.0, summary.retryCount.toDouble())).toLong()
            try { Thread.sleep(backoffMs) } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return
            }

            summaryRepository.update(
                Summary(
                    id = summary.id,
                    userId = summary.userId,
                    summaryText = summary.summaryText,
                    supplementLevel = summary.supplementLevel,
                    summaryMode = summary.summaryMode,
                    status = SummaryStatus.FAILED,
                    retryCount = summary.retryCount + 1,
                    generatedAt = summary.generatedAt,
                    sourceArticleIds = summary.sourceArticleIds
                )
            )
        }
    }

    /**
     * Gemini ネイティブAPI を Google Search Grounding 付きで呼び出す。
     * Gemini が自動的にGoogle検索を実行し、最新の株価指数データを取得して回答に含める。
     */
    private fun callGeminiWithSearchGrounding(prompt: String): String {
        val googleSearchTool = Tool.builder()
            .googleSearch(GoogleSearch.builder().build())
            .build()

        val config = GenerateContentConfig.builder()
            .tools(listOf(googleSearchTool))
            .temperature(0.7f)
            .build()

        val response = client.models.generateContent(
            modelName,
            prompt,
            config
        )

        return response.text() ?: throw RuntimeException("Gemini APIからの応答が空です")
    }

    private fun parseResponse(responseText: String): LlmSummaryResponse {
        return try {
            val jsonStr = extractJson(responseText)
            objectMapper.readValue(jsonStr, LlmSummaryResponse::class.java)
        } catch (e: Exception) {
            logger.warn("JSONパース失敗、プレーンテキストとして処理: ${e.message}")
            LlmSummaryResponse(summaryText = responseText.take(600), indexImpacts = emptyList())
        }
    }

    private fun extractJson(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start == -1 || end == -1 || start >= end) {
            throw IllegalArgumentException("JSONが見つかりません")
        }
        return text.substring(start, end + 1)
    }

    private fun parseImpactDirection(direction: String): ImpactDirection = try {
        ImpactDirection.valueOf(direction.uppercase())
    } catch (_: IllegalArgumentException) {
        ImpactDirection.NEUTRAL
    }

    data class LlmSummaryResponse(
        val summaryText: String = "",
        val indexImpacts: List<LlmIndexImpact> = emptyList()
    )

    data class LlmIndexImpact(
        val indexSymbol: String = "",
        val impactDirection: String = "NEUTRAL"
    )
}
