package com.example.news_summary.notification.infrastructure.persistence

import com.example.news_summary.domain.notification.model.ChannelType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

/** JPA用通知チャンネルエンティティ。ドメインモデルとは分離されている。 */
@Entity
@Table(
    name = "delivery_channels",
    indexes = [
        Index(name = "idx_delivery_channels_user_id", columnList = "user_id"),
        Index(name = "idx_delivery_channels_enabled", columnList = "enabled"),
        Index(name = "idx_delivery_channels_user_enabled", columnList = "user_id, enabled")
    ]
)
class DeliveryChannelJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 50)
    val channelType: ChannelType = ChannelType.EMAIL,

    /** AES-256-GCM で暗号化された接続設定（JSON） */
    @Column(name = "encrypted_config", nullable = false, columnDefinition = "TEXT")
    val encryptedConfig: String = "",

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
)
