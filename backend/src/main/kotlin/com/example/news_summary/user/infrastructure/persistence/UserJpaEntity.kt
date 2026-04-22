package com.example.news_summary.user.infrastructure.persistence

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

/** JPA用ユーザーエンティティ。ドメインモデルとは分離されている。 */
@Entity
@Table(
    name = "users",
    indexes = [Index(name = "idx_users_email", columnList = "email")]
)
class UserJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 255)
    val email: String = "",

    @Column(name = "password_hash", nullable = false, length = 255)
    val passwordHash: String = "",

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant? = null
)
