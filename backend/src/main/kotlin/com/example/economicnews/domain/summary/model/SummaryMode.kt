package com.example.economicnews.domain.summary.model

/** 要約文字数モード（summaryドメインに属する） */
enum class SummaryMode(val charLimit: Int, val displayName: String) {
    SHORT(150, "短め（150文字以内）"),
    STANDARD(300, "標準（300文字以内）"),
    DETAILED(600, "詳細（600文字以内）")
}
