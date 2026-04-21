package com.example.economicnews.domain.settings.repository

import com.example.economicnews.domain.settings.model.SummarySettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SummarySettingsRepository : JpaRepository<SummarySettings, Long> {
    fun findByUserId(userId: Long): Optional<SummarySettings>
}
