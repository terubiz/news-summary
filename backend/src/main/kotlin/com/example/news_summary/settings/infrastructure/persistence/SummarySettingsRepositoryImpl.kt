package com.example.news_summary.settings.infrastructure.persistence

import com.example.news_summary.domain.settings.model.SummarySettings
import com.example.news_summary.domain.settings.model.SummarySettingsId
import com.example.news_summary.domain.settings.repository.SummarySettingsRepository
import org.springframework.stereotype.Component
import java.util.Optional

/**
 * SummarySettingsRepository のインフラ層実装。
 * JpaEntity ↔ ドメインモデルの変換を一元管理する。
 * id の null → SummarySettingsId 変換はこのクラス内でのみ行われる。
 */
@Component
class SummarySettingsRepositoryImpl(
    private val jpaRepository: SummarySettingsJpaRepository
) : SummarySettingsRepository {

    override fun findById(id: SummarySettingsId): Optional<SummarySettings> =
        jpaRepository.findById(id.value).map { it.toDomain() }

    override fun findByUserId(userId: Long): Optional<SummarySettings> =
        jpaRepository.findByUserId(userId).map { it.toDomain() }

    override fun save(settings: SummarySettings): SummarySettings {
        val entity = SummarySettingsJpaEntity(
            id = settings.id.value,
            userId = settings.userId,
            selectedIndices = settings.selectedIndices.toTypedArray(),
            analysisPerspectives = settings.analysisPerspectives.toTypedArray(),
            supplementLevel = settings.supplementLevel,
            summaryMode = settings.summaryMode,
            updatedAt = settings.updatedAt
        )
        return jpaRepository.save(entity).toDomain()
    }

    /** JpaEntity → ドメインモデル変換。id の null チェックはここで1箇所だけ行う。 */
    private fun SummarySettingsJpaEntity.toDomain(): SummarySettings = SummarySettings(
        id = SummarySettingsId(id ?: throw IllegalStateException("永続化済みSummarySettingsのIDがnullです")),
        userId = userId,
        selectedIndices = selectedIndices.toList(),
        analysisPerspectives = analysisPerspectives.toList(),
        supplementLevel = supplementLevel,
        summaryMode = summaryMode,
        updatedAt = updatedAt ?: throw IllegalStateException("永続化済みSummarySettingsのupdatedAtがnullです")
    )
}
