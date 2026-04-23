package com.example.news_summary.settings.infrastructure.persistence

import com.example.news_summary.domain.settings.model.CollectionSchedule
import com.example.news_summary.domain.settings.model.CollectionScheduleId
import com.example.news_summary.domain.settings.model.NewCollectionSchedule
import com.example.news_summary.domain.settings.repository.CollectionScheduleRepository
import com.example.news_summary.domain.user.model.UserId
import org.springframework.stereotype.Component

@Component
class CollectionScheduleRepositoryImpl(
    private val jpaRepository: CollectionScheduleJpaRepository
) : CollectionScheduleRepository {

    override fun findByUserId(userId: UserId): CollectionSchedule? =
        jpaRepository.findByUserId(userId.value).map { it.toDomain() }.orElse(null)

    override fun findByEnabledTrue(): List<CollectionSchedule> =
        jpaRepository.findByEnabledTrue().map { it.toDomain() }

    override fun save(schedule: NewCollectionSchedule): CollectionSchedule {
        val entity = CollectionScheduleJpaEntity(
            userId = schedule.userId.value,
            cronExpression = schedule.cronExpression,
            enabled = schedule.enabled
        )
        return jpaRepository.save(entity).toDomain()
    }

    override fun update(schedule: CollectionSchedule): CollectionSchedule {
        val entity = CollectionScheduleJpaEntity(
            id = schedule.id.value,
            userId = schedule.userId.value,
            cronExpression = schedule.cronExpression,
            enabled = schedule.enabled,
            updatedAt = schedule.updatedAt
        )
        return jpaRepository.save(entity).toDomain()
    }

    private fun CollectionScheduleJpaEntity.toDomain(): CollectionSchedule = CollectionSchedule(
        id = CollectionScheduleId(id ?: throw IllegalStateException("永続化済みCollectionScheduleのIDがnullです")),
        userId = UserId(userId),
        cronExpression = cronExpression,
        enabled = enabled,
        updatedAt = updatedAt ?: throw IllegalStateException("永続化済みCollectionScheduleのupdatedAtがnullです")
    )
}
