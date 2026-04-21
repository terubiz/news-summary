package com.example.news_summary.domain.settings.repository

import com.example.news_summary.domain.settings.model.SummarySettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SummarySettingsRepository : JpaRepository<SummarySettings, Long> {
    fun findByUserId(userId: Long): Optional<SummarySettings>
}
