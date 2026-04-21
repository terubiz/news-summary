package com.example.news_summary.domain.index.model

import java.math.BigDecimal
import java.time.Instant

/**
 * 株価指数データ ドメインモデル（集約ルート）
 * JPAアノテーションを持たない純粋なドメインオブジェクト。
 * id: IndexDataId により「永続化済み = IDが確定している」ことを型で保証する。
 */
data class IndexData(
    val id: IndexDataId,
    val symbol: String,
    val currentValue: BigDecimal,
    val changeAmount: BigDecimal,
    val changeRate: BigDecimal,
    val isStale: Boolean = false,
    val fetchedAt: Instant
)
