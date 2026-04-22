package com.example.news_summary.domain.news.model

import java.time.Instant

/**
 * ニュース記事ドメインモデル（集約ルート）
 * 新規作成時は id = null, collectedAt = null を許容する（save後に確定する）。
 */
data class NewsArticle(
    val id: NewsArticleId? = null,
    val title: String,
    val content: String,
    val sourceUrl: String,
    val sourceName: String,
    val publishedAt: Instant,
    val collectedAt: Instant? = null
)
