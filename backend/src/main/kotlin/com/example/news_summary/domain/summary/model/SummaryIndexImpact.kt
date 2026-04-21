package com.example.news_summary.domain.summary.model

/**
 * 要約指数影響ドメインモデル
 * JPAアノテーションを持たない純粋なドメインオブジェクト。
 */
data class SummaryIndexImpact(
    val id: SummaryIndexImpactId,
    val summaryId: Long,
    val indexSymbol: String,
    val impactDirection: ImpactDirection
)
