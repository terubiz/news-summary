package com.example.news_summary.settings.application.usecase

import com.example.news_summary.domain.settings.model.CollectionSchedule
import com.example.news_summary.domain.settings.model.SummarySettings
import com.example.news_summary.domain.settings.repository.CollectionScheduleRepository
import com.example.news_summary.domain.settings.repository.SummarySettingsRepository
import com.example.news_summary.domain.summary.model.SummaryMode
import com.example.news_summary.domain.summary.model.SupplementLevel
import com.example.news_summary.domain.user.model.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class UpdateSummarySettingsCommand(
    val selectedIndices: List<String>,
    val analysisPerspectives: List<String>,
    val supplementLevel: SupplementLevel,
    val summaryMode: SummaryMode
)

data class UpdateScheduleCommand(
    val cronExpression: String,
    val enabled: Boolean
)

@Service
class ManageSettingsUseCase(
    private val settingsRepository: SummarySettingsRepository,
    private val scheduleRepository: CollectionScheduleRepository
) {
    /** 要約設定を取得する。存在しない場合はデフォルト値を返す（要件9.10） */
    fun getSummarySettings(userId: UserId): SummarySettings =
        settingsRepository.findByUserId(userId)
            ?: SummarySettings(userId = userId)

    @Transactional
    fun updateSummarySettings(userId: UserId, command: UpdateSummarySettingsCommand): SummarySettings {
        val existing = settingsRepository.findByUserId(userId)
        val settings = (existing ?: SummarySettings(userId = userId)).copy(
            selectedIndices = command.selectedIndices,
            analysisPerspectives = command.analysisPerspectives,
            supplementLevel = command.supplementLevel,
            summaryMode = command.summaryMode
        )
        return settingsRepository.save(settings)
    }

    fun getSchedule(userId: UserId): CollectionSchedule? =
        scheduleRepository.findByUserId(userId)

    @Transactional
    fun updateSchedule(userId: UserId, command: UpdateScheduleCommand): CollectionSchedule {
        val existing = scheduleRepository.findByUserId(userId)
        val schedule = (existing ?: CollectionSchedule(userId = userId, cronExpression = "")).copy(
            cronExpression = command.cronExpression,
            enabled = command.enabled
        )
        return scheduleRepository.save(schedule)
    }
}
