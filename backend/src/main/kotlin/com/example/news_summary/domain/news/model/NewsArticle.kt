package com.example.news_summary.domain.news.model

import java.time.Instant

/**
 * ニュース記事ドメインモデル（集約ルート）
 * JPAアノテーションを持たない純粋なドメインオブジェクト。
 * id: NewsArticleId により「永続化済み = IDが確定している」ことを型で保証する。
 */
data class NewsArticle(
    val id: NewsArticleId,
    val title: String,
    val content: String,
    val sourceUrl: String,
    val sourceName: String,
    val publishedAt: Instant,
    val collectedAt: Instant
)
