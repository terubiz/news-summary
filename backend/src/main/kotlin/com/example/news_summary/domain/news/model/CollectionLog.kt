package com.example.news_summary.domain.news.model

import com.example.news_summary.domain.user.model.UserId
import java.time.Instant

/**
 * 収集ログの新規作成用モデル。IDやタイムスタンプを持たない。
 * リポジトリの save(NewCollectionLog) で永続化し、CollectionLog（ID確定済み）が返る。
 */
data class NewCollectionLog(
    val userId: UserId,
    val articleCount: Int = 0,
    val status: String,
    val errorMessage: String? = null
)

/**
 * 永続化済み収集ログドメインモデル。
 * id は常に non-null。「このオブジェクトが存在する = DBに保存済み」を型で保証する。
 */
data class CollectionLog(
    val id: CollectionLogId,
    val userId: UserId,
    val articleCount: Int,
    val status: String,
    val errorMessage: String? = null,
    val executedAt: Instant
)
