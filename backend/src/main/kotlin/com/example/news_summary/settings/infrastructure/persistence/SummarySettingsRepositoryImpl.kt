package com.example.news_summary.settings.infrastructure.persistence

import com.example.news_summary.domain.settings.model.NewSummarySettings
import com.example.news_summary.domain.settings.model.SummarySettings
import com.example.news_summary.domain.settings.model.SummarySettingsId
import com.example.news_summary.domain.settings.repository.SummarySettingsRepository
import com.example.news_summary.domain.user.model.UserId
import org.springframework.stereotype.Component

@Component
class SummarySettingsRepositoryImpl(
    private val jpaRepository: SummarySettingsJpaRepository
) : SummarySettingsRepository {

    override fun findByUserId(userId: UserId): SummarySettings? =
        jpaRepository.findByUserId(userId.value).map { it.toDomain() }.orElse(null)

    override fun save(settings: NewSummarySettings): SummarySettings {
        val entity = SummarySettingsJpaEntity(
            userId = settings.userId.value,
            selectedIndices = settings.selectedIndices.toTypedArray(),
            analysisPerspectives = settings.analysisPerspectives.toTypedArray(),
            supplementLevel = settings.supplementLevel,
            summaryMode = settings.summaryMode
        )
        return jpaRepository.save(entity).toDomain()
    }

    override fun update(settings: SummarySettings): SummarySettings {
        val entity = SummarySettingsJpaEntity(
            id = settings.id.value,
            userId = settings.userId.value,
            selectedIndices = settings.selectedIndices.toTypedArray(),
            analysisPerspectives = settings.analysisPerspectives.toTypedArray(),
            supplementLevel = settings.supplementLevel,
            summaryMode = settings.summaryMode,
            updatedAt = settings.updatedAt
        )
        return jpaRepository.save(entity).toDomain()
    }

    private fun SummarySettingsJpaEntity.toDomain(): SummarySettings = SummarySettings(
        id = SummarySettingsId(id ?: throw IllegalStateException("永続化済みSummarySettingsのIDがnullです")),
        userId = UserId(userId),
        selectedIndices = selectedIndices.toList(),
        analysisPerspectives = analysisPerspectives.toList(),
        supplementLevel = supplementLevel,
        summaryMode = summaryMode,
        updatedAt = updatedAt ?: throw IllegalStateException("永続化済みSummarySettingsのupdatedAtがnullです")
    )
}
