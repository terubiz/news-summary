package com.example.news_summary.user.infrastructure.persistence

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

/** JPA用リフレッシュトークンエンティティ */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = [
        Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
        Index(name = "idx_refresh_tokens_token_hash", columnList = "token_hash"),
        Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
    ]
)
class RefreshTokenJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    val tokenHash: String = "",

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant = Instant.now(),

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null
)
