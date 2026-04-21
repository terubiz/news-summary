package com.example.news_summary.domain.notification.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

/** 通知送信ログ */
@Entity
@Table(
    name = "delivery_logs",
    indexes = [
        Index(name = "idx_delivery_logs_channel_id", columnList = "channel_id"),
        Index(name = "idx_delivery_logs_summary_id", columnList = "summary_id"),
        Index(name = "idx_delivery_logs_status", columnList = "status"),
        Index(name = "idx_delivery_logs_channel_summary", columnList = "channel_id, summary_id")
    ]
)
data class DeliveryLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "channel_id", nullable = false)
    val channelId: Long,

    @Column(name = "summary_id", nullable = false)
    val summaryId: Long,

    /** SUCCESS / FAILED */
    @Column(nullable = false, length = 50)
    val status: String,

    @Column(name = "retry_count", nullable = false)
    val retryCount: Int = 0,

    @Column(name = "error_message", columnDefinition = "TEXT")
    val errorMessage: String? = null,

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    val sentAt: Instant? = null
)
