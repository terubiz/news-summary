package com.example.news_summary.domain.settings.model

import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

/** ニュース収集スケジュール設定エンティティ */
@Entity
@Table(
    name = "collection_schedules",
    indexes = [
        Index(name = "idx_collection_schedules_user_id", columnList = "user_id"),
        Index(name = "idx_collection_schedules_enabled", columnList = "enabled")
    ]
)
data class CollectionSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    /** Quartz Cron式（例: "0 0 8,12,18 * * ?"） */
    @Column(name = "cron_expression", nullable = false, length = 100)
    val cronExpression: String,

    @Column(nullable = false)
    val enabled: Boolean = true,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant? = null
)
