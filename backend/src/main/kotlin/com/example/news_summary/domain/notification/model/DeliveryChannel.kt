package com.example.news_summary.domain.notification.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

/** 通知送信チャンネル（集約ルート） */
@Entity
@Table(
    name = "delivery_channels",
    indexes = [
        Index(name = "idx_delivery_channels_user_id", columnList = "user_id"),
        Index(name = "idx_delivery_channels_enabled", columnList = "enabled"),
        Index(name = "idx_delivery_channels_user_enabled", columnList = "user_id, enabled")
    ]
)
data class DeliveryChannel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 50)
    val channelType: ChannelType,

    /** AES-256-GCM で暗号化された接続設定（JSON） */
    @Column(name = "encrypted_config", nullable = false, columnDefinition = "TEXT")
    val encryptedConfig: String,

    /** 送信スケジュール: IMMEDIATE / HOURLY / DAILY_HH:MM */
    @Column(name = "delivery_schedule", nullable = false, length = 100)
    val deliverySchedule: String = "IMMEDIATE",

    @Column(name = "filter_indices", nullable = false, columnDefinition = "varchar(50)[]")
    val filterIndices: Array<String> = emptyArray(),

    @Column(nullable = false)
    val enabled: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeliveryChannel) return false
        return id == other.id &&
            userId == other.userId &&
            channelType == other.channelType &&
            encryptedConfig == other.encryptedConfig &&
            deliverySchedule == other.deliverySchedule &&
            filterIndices.contentEquals(other.filterIndices) &&
            enabled == other.enabled
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + userId.hashCode()
        result = 31 * result + channelType.hashCode()
        result = 31 * result + encryptedConfig.hashCode()
        result = 31 * result + deliverySchedule.hashCode()
        result = 31 * result + filterIndices.contentHashCode()
        result = 31 * result + enabled.hashCode()
        return result
    }
}
