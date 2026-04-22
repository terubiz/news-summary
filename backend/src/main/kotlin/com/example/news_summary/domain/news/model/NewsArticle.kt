package com.example.news_summary.domain.news.model

import java.time.Instant

/**
 * ニュース記事の新規作成用モデル。IDやタイムスタンプを持たない。
 * リポジトリの save(NewNewsArticle) で永続化し、NewsArticle（ID確定済み）が返る。
 */
data class NewNewsArticle(
    val title: String,
    val content: String,
    val sourceUrl: String,
    val sourceName: String,
    val publishedAt: Instant
)

/**
 * 永続化済みニュース記事ドメインモデル（集約ルート）。
 * id は常に non-null。「このオブジェクトが存在する = DBに保存済み」を型で保証する。
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
