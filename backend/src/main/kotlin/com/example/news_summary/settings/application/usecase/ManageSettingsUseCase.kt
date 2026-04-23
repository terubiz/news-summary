package com.example.news_summary.settings.application.usecase

import com.example.news_summary.domain.settings.model.CollectionSchedule
import com.example.news_summary.domain.settings.model.NewCollectionSchedule
import com.example.news_summary.domain.settings.model.NewSummarySettings
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
    /** 要約設定を取得する。存在しない場合はデフォルト値で新規作成して返す（要件9.10） */
    fun getSummarySettings(userId: UserId): SummarySettings {
        return settingsRepository.findByUserId(userId)
            ?: settingsRepository.save(NewSummarySettings(userId = userId))
    }

    @Transactional
    fun updateSummarySettings(userId: UserId, command: UpdateSummarySettingsCommand): SummarySettings {
        val existing = settingsRepository.findByUserId(userId)
        return if (existing != null) {
            settingsRepository.update(existing.copy(
                selectedIndices = command.selectedIndices,
                analysisPerspectives = command.analysisPerspectives,
                supplementLevel = command.supplementLevel,
                summaryMode = command.summaryMode
            ))
        } else {
            settingsRepository.save(NewSummarySettings(
                userId = userId,
                selectedIndices = command.selectedIndices,
                analysisPerspectives = command.analysisPerspectives,
                supplementLevel = command.supplementLevel,
                summaryMode = command.summaryMode
            ))
        }
    }

    fun getSchedule(userId: UserId): CollectionSchedule? =
        scheduleRepository.findByUserId(userId)

    @Transactional
    fun updateSchedule(userId: UserId, command: UpdateScheduleCommand): CollectionSchedule {
        val existing = scheduleRepository.findByUserId(userId)
        return if (existing != null) {
            scheduleRepository.update(existing.copy(
                cronExpression = command.cronExpression,
                enabled = command.enabled
            ))
        } else {
            scheduleRepository.save(NewCollectionSchedule(
                userId = userId,
                cronExpression = command.cronExpression,
                enabled = command.enabled
            ))
        }
    }
}
