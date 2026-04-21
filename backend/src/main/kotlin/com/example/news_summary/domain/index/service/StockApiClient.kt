package com.example.news_summary.domain.index.service

import java.math.BigDecimal

/**
 * 株価API クライアント（ドメイン層ポート）
 * 外部株価APIへの依存をドメインから分離する。
 */
interface StockApiClient {
    /**
     * 指定シンボルの最新株価データを取得する
     * @param symbol 株価指数シンボル（例: "N225", "SPX"）
     * @return 取得結果。接続失敗時は null を返す（例外を投げない）
     */
    fun fetchQuote(symbol: String): RawStockQuote?
}

/** 外部APIから取得した生の株価データ */
data class RawStockQuote(
    val symbol: String,
    val currentValue: BigDecimal,
    val changeAmount: BigDecimal,
    val changeRate: BigDecimal
)
