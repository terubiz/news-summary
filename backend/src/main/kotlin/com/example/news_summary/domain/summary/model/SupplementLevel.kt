package com.example.news_summary.domain.summary.model

/** 要約の補足レベル（summaryドメインに属する） */
enum class SupplementLevel(val displayName: String) {
    BEGINNER("初心者向け"),
    INTERMEDIATE("中級者向け"),
    ADVANCED("上級者向け")
}
