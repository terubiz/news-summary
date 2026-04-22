package com.example.news_summary.domain.summary.model

/** 株価指数への影響方向（summaryドメインに属する） */
enum class ImpactDirection(val displayName: String) {
    BULLISH("上昇要因"),
    BEARISH("下落要因"),
    NEUTRAL("中立")
}
