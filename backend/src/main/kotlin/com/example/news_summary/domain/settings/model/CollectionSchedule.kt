package com.example.news_summary.domain.settings.model

import java.time.Instant

/**
 * ニュース収集スケジュール設定 ドメインモデル
 * JPAアノテーションを持たない純粋なドメインオブジェクト。
 */
data class CollectionSchedule(
    val id: CollectionScheduleId,
    val userId: Long,
    /** Quartz Cron式（例: "0 0 8,12,18 * * ?"） */
    val cronExpression: String,
    val enabled: Boolean = true,
    val updatedAt: Instant
)
