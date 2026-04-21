package com.example.economicnews.domain.settings.model

/** 要約に絡める分析観点（settingsドメインに属する） */
enum class AnalysisPerspective(val displayName: String) {
    INTEREST_RATE("金利・中央銀行政策"),
    GEOPOLITICAL_RISK("地政学リスク・戦争・紛争"),
    INFLUENTIAL_SPEECH("有力者・政治家の発言"),
    CORPORATE_EARNINGS("企業決算・業績"),
    FOREX_POLICY("為替・通貨政策"),
    ENERGY_RESOURCES("エネルギー・資源価格"),
    INFLATION_CPI("インフレ・物価指標"),
    EMPLOYMENT_STATS("雇用統計・経済指標"),
    CUSTOM("カスタム")
}
