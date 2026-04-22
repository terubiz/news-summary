package com.example.news_summary.domain.summary.model

/** 新規作成用 */
data class NewSummaryIndexImpact(
    val summaryId: Long,
    val indexSymbol: String,
    val impactDirection: ImpactDirection
)

/** 永続化済み（ID確定） */
data class SummaryIndexImpact(
    val id: SummaryIndexImpactId,
    val summaryId: Long,
    val indexSymbol: String,
    val impactDirection: ImpactDirection
)
