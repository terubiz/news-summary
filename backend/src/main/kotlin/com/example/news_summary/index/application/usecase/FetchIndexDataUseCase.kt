package com.example.news_summary.index.application.usecase

import com.example.news_summary.domain.index.model.IndexData
import com.example.news_summary.domain.index.repository.IndexDataRepository
import com.example.news_summary.domain.index.service.StockApiClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 株価指数データ取得ユースケース。
 *
 * 処理フロー:
 * 1. 各シンボルに対して StockApiClient.fetchQuote() を呼び出す
 * 2. 取得成功 → DB保存して返す
 * 3. 取得失敗 → キャッシュ（DB最新データ）を isStale=true で返す（要件2.3）
 */
@Service
class FetchIndexDataUseCase(
    private val stockApiClient: StockApiClient,
    private val indexDataRepository: IndexDataRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun execute(symbols: List<String>): List<IndexData> {
        return symbols.mapNotNull { symbol ->
            try {
                val quote = stockApiClient.fetchQuote(symbol)
                if (quote != null) {
                    // API取得成功 → DB保存
                    val indexData = IndexData(
                        symbol = quote.symbol,
                        currentValue = quote.currentValue,
                        changeAmount = quote.changeAmount,
                        changeRate = quote.changeRate,
                        isStale = false
                    )
                    indexDataRepository.save(indexData)
                } else {
                    // API取得失敗 → キャッシュフォールバック（要件2.3）
                    fallbackToCache(symbol)
                }
            } catch (e: Exception) {
                logger.error("株価指数データ取得失敗 (symbol=$symbol): ${e.message}")
                fallbackToCache(symbol)
            }
        }
    }

    /** キャッシュ（DB最新データ）を isStale=true で返す */
    private fun fallbackToCache(symbol: String): IndexData? {
        val cached = indexDataRepository.findLatestBySymbol(symbol)
        return cached?.copy(isStale = true).also {
            if (it != null) {
                logger.warn("株価指数データをキャッシュから返却 (symbol=$symbol, isStale=true)")
            } else {
                logger.warn("株価指数データのキャッシュが存在しません (symbol=$symbol)")
            }
        }
    }
}
