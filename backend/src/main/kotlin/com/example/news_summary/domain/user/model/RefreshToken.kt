package com.example.news_summary.domain.user.model

import java.time.Instant

/**
 * リフレッシュトークンドメインモデル
 * id は永続化後に確定する。新規作成時は null を許容する。
 */
data class RefreshToken(
    val id: Long? = null,
    val userId: UserId,
    val tokenHash: String,
    val expiresAt: Instant,
    val createdAt: Instant? = null
)
