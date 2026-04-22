package com.example.news_summary.domain.summary.model

import com.example.news_summary.domain.news.model.NewsArticleId
import java.time.Instant

/**
 * 要約の新規作成用モデル。IDやタイムスタンプを持たない。
 * リポジトリの save(NewSummary) で永続化し、Summary（ID確定済み）が返る。
 */
data class NewSummary(
    val userId: Long,
    val summaryText: String,
    val supplementLevel: SupplementLevel,
    val summaryMode: SummaryMode,
    val status: SummaryStatus = SummaryStatus.PENDING,
    val retryCount: Int = 0,
    val sourceArticleIds: Set<NewsArticleId> = emptySet()
)

/**
 * 永続化済み要約ドメインモデル（集約ルート）。
 * id は常に non-null。「このオブジェクトが存在する = DBに保存済み」を型で保証する。
 */
data class Summary(
    val id: SummaryId,
    val userId: Long,
    val summaryText: String,
    val supplementLevel: SupplementLevel,
    val summaryMode: SummaryMode,
    val status: SummaryStatus,
    val retryCount: Int,
    val generatedAt: Instant,
    val sourceArticleIds: Set<NewsArticleId> = emptySet()
)
