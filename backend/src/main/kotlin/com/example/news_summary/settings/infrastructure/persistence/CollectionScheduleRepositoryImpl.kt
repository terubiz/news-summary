package com.example.news_summary.settings.infrastructure.persistence

import com.example.news_summary.domain.settings.model.CollectionSchedule
import com.example.news_summary.domain.settings.model.CollectionScheduleId
import com.example.news_summary.domain.settings.repository.CollectionScheduleRepository
import org.springframework.stereotype.Component
import java.util.Optional

/**
 * CollectionScheduleRepository のインフラ層実装。
 * JpaEntity ↔ ドメインモデルの変換を一元管理する。
 * id の null → CollectionScheduleId 変換はこのクラス内でのみ行われる。
 */
@Component
class CollectionScheduleRepositoryImpl(
    private val jpaRepository: CollectionScheduleJpaRepository
) : CollectionScheduleRepository {

    override fun findById(id: CollectionScheduleId): Optional<CollectionSchedule> =
        jpaRepository.findById(id.value).map { it.toDomain() }

    override fun findByUserId(userId: Long): Optional<CollectionSchedule> =
        jpaRepository.findByUserId(userId).map { it.toDomain() }

    override fun findByEnabledTrue(): List<CollectionSchedule> =
        jpaRepository.findByEnabledTrue().map { it.toDomain() }

    override fun save(schedule: CollectionSchedule): CollectionSchedule {
        val entity = CollectionScheduleJpaEntity(
            id = schedule.id.value,
            userId = schedule.userId,
            cronExpression = schedule.cronExpression,
            enabled = schedule.enabled,
            updatedAt = schedule.updatedAt
        )
        return jpaRepository.save(entity).toDomain()
    }

    /** JpaEntity → ドメインモデル変換。id の null チェックはここで1箇所だけ行う。 */
    private fun CollectionScheduleJpaEntity.toDomain(): CollectionSchedule = CollectionSchedule(
        id = CollectionScheduleId(id ?: throw IllegalStateException("永続化済みCollectionScheduleのIDがnullです")),
        userId = userId,
        cronExpression = cronExpression,
        enabled = enabled,
        updatedAt = updatedAt ?: throw IllegalStateException("永続化済みCollectionScheduleのupdatedAtがnullです")
    )
}
