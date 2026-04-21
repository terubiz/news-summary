package com.example.news_summary.domain.index.model

/**
 * 対応する株価指数シンボルの定義。
 * Alpha Vantage API のシンボル表記に準拠する。
 *
 * 要件2.1: 日経225、S&P500、NASDAQ Composite、DAX
 */
enum class StockSymbol(
    val apiSymbol: String,
    val displayName: String
) {
    NIKKEI_225("N225", "日経225"),
    SP500("SPX", "S&P500"),
    NASDAQ("IXIC", "NASDAQ Composite"),
    DAX("GDAXI", "DAX");

    companion object {
        /** デフォルトの対象指数（要件2.1） */
        val DEFAULT_SYMBOLS: List<String> = entries.map { it.apiSymbol }

        /** APIシンボルから表示名を取得する */
        fun displayNameOf(apiSymbol: String): String =
            entries.find { it.apiSymbol == apiSymbol }?.displayName ?: apiSymbol
    }
}
