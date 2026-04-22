package com.example.news_summary.domain.user.model

import java.time.Instant

/**
 * リフレッシュトークン新規作成用モデル。IDやタイムスタンプを持たない。
 * リポジトリの save(NewRefreshToken) で永続化し、RefreshToken（ID確定済み）が返る。
 */
data class NewRefreshToken(
    val userId: UserId,
    val tokenHash: String,
    val expiresAt: Instant
)

/**
 * 永続化済みリフレッシュトークンドメインモデル。
 * id は常に non-null。「このオブジェクトが存在する = DBに保存済み」を型で保証する。
 */
data class RefreshToken(
    val id: Long,
    val userId: UserId,
    val tokenHash: String,
    val expiresAt: Instant,
    val createdAt: Instant
)
