package com.example.news_summary.domain.summary.model

/** 要約生成ステータス（summaryドメインに属する） */
enum class SummaryStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
