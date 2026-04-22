package com.example.news_summary.domain.settings.repository

import com.example.news_summary.domain.settings.model.CollectionSchedule
import com.example.news_summary.domain.user.model.UserId

interface CollectionScheduleRepository {
    fun findByUserId(userId: UserId): CollectionSchedule?
    fun findByEnabledTrue(): List<CollectionSchedule>
    fun save(schedule: CollectionSchedule): CollectionSchedule
}
