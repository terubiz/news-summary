package com.example.news_summary.domain.settings.repository

import com.example.news_summary.domain.settings.model.SummarySettings
import com.example.news_summary.domain.user.model.UserId

interface SummarySettingsRepository {
    fun findByUserId(userId: UserId): SummarySettings?
    fun save(settings: SummarySettings): SummarySettings
}
