package com.example.news_summary.domain.settings.model

import com.example.news_summary.domain.user.model.UserId
import java.time.Instant

data class CollectionSchedule(
    val id: CollectionScheduleId? = null,
    val userId: UserId,
    /** Quartz Cron式（例: "0 0 8,12,18 * * ?"） */
    val cronExpression: String,
    val enabled: Boolean = true,
    val updatedAt: Instant? = null
)
