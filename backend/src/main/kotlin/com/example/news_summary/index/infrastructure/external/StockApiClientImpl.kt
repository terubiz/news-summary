package com.example.news_summary.index.infrastructure.external

import com.example.news_summary.domain.index.service.RawStockQuote
import com.example.news_summary.domain.index.service.StockApiClient
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

/**
 * Alpha Vantage API クライアント実装。
 * GLOBAL_QUOTE エンドポイントで株価指数データを取得する。
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
            val url = "$baseUrl/query?function=GLOBAL_QUOTE&symbol=$symbol&apikey=$apiKey"
            val response = restTemplate.getForObject(url, AlphaVantageResponse::class.java)
            val quote = response?.globalQuote ?: return null

            RawStockQuote(
                symbol = quote.symbol ?: symbol,
                currentValue = quote.price?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                changeAmount = quote.change?.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                changeRate = quote.changePercent
                    ?.removeSuffix("%")
                    ?.toBigDecimalOrNull()
                    ?.divide(BigDecimal(100))
                    ?: BigDecimal.ZERO
            )
        } catch (e: Exception) {
            logger.error("株価API接続失敗 (symbol=$symbol): ${e.message}", e)
            null
        }
    }
}

// ---- Alpha Vantage レスポンスマッピング ----

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlphaVantageResponse(
    @JsonProperty("Global Quote") val globalQuote: GlobalQuote?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GlobalQuote(
    @JsonProperty("01. symbol") val symbol: String?,
    @JsonProperty("05. price") val price: String?,
    @JsonProperty("09. change") val change: String?,
    @JsonProperty("10. change percent") val changePercent: String?
)
