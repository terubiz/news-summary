package com.example.news_summary.domain.news.model

import java.time.Instant

/**
 * ニュース収集実行ログ ドメインモデル（newsドメインに属する）
 * JPAアノテーションを持たない純粋なドメインオブジェクト。
 */
data class CollectionLog(
    val id: CollectionLogId,
    val userId: Long,
    val articleCount: Int = 0,
    val status: String,
    val errorMessage: String? = null,
    val executedAt: Instant
)
