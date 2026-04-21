package com.example.economicnews.domain.news.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

/** ニュース収集実行ログ（newsドメインに属する） */
@Entity
@Table(
    name = "collection_logs",
    indexes = [
        Index(name = "idx_collection_logs_user_id", columnList = "user_id"),
        Index(name = "idx_collection_logs_executed_at", columnList = "executed_at"),
        Index(name = "idx_collection_logs_user_executed", columnList = "user_id, executed_at")
    ]
)
data class CollectionLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "article_count", nullable = false)
    val articleCount: Int = 0,

    @Column(nullable = false, length = 50)
    val status: String,

    @Column(name = "error_message", columnDefinition = "TEXT")
    val errorMessage: String? = null,

    @CreationTimestamp
    @Column(name = "executed_at", nullable = false, updatable = false)
    val executedAt: Instant? = null
)
