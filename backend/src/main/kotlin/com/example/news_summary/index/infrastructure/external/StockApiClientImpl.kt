package com.example.news_summary.index.infrastructure.external

import com.example.news_summary.domain.index.service.RawStockQuote
import com.example.news_summary.domain.index.service.StockApiClient
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

/**
 * Twelve Data API クライアント実装。
 * /quote エンドポイントで株価指数データを取得する。
 * 無料プラン: 800クレジット/日、8リクエスト/分。株価指数を直接取得可能。
 * 接続失敗時は null を返す（要件2.3: キャッシュフォールバックはサービス層で処理）。
 */
@Component
class StockApiClientImpl(
    @Value("\${app.stock-api.key}") private val apiKey: String,
    @Value("\${app.stock-api.base-url}") private val baseUrl: String
) : StockApiClient {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val restTemplate = RestTemplate()

    override fun fetchQuote(symbol: String): RawStockQuote? {
        return try {
            val url = "$baseUrl/quote?symbol=$symbol&apikey=$apiKey"
            val response = restTemplate.getForObject(url, TwelveDataQuoteResponse::class.java)

            if (response?.symbol == null || response.close == null) {
                logger.warn("Twelve Data APIから有効なデータが返りませんでした (symbol=$symbol)")
                return null
            }

            val close = response.close.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val change = response.change?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val percentChange = response.percentChange
                ?.toBigDecimalOrNull()
                ?.divide(BigDecimal(100))
                ?: BigDecimal.ZERO

            RawStockQuote(
                symbol = response.symbol,
                currentValue = close,
                changeAmount = change,
                changeRate = percentChange
            )
        } catch (e: Exception) {
            logger.error("株価API接続失敗 (symbol=$symbol): ${e.message}", e)
            null
        }
    }
}

// ---- Twelve Data レスポンスマッピング ----

@JsonIgnoreProperties(ignoreUnknown = true)
data class TwelveDataQuoteResponse(
    val symbol: String? = null,
    val name: String? = null,
    val close: String? = null,
    val change: String? = null,
    val percent_change: String? = null
) {
    // Jackson が percent_change をマッピングできるようにプロパティを追加
    val percentChange: String? get() = percent_change
}
