package com.example.news_summary.domain.summary.model

import com.example.news_summary.domain.news.model.NewsArticleId
import java.time.Instant

/**
 * 要約ドメインモデル（集約ルート）
 * JPAアノテーションを持たない純粋なドメインオブジェクト。
 * id: SummaryId により「永続化済み = IDが確定している」ことを型で保証する。
 * sourceArticleIds: 要約元記事のIDリスト（ManyToMany関係はJpaEntity側で管理）
 */
data class Summary(
    val id: SummaryId,
    val userId: Long,
    val summaryText: String,
    val supplementLevel: SupplementLevel,
    val summaryMode: SummaryMode,
    val status: SummaryStatus = SummaryStatus.PENDING,
    val retryCount: Int = 0,
    val generatedAt: Instant,
    val sourceArticleIds: Set<NewsArticleId> = emptySet()
)
