package com.example.news_summary.news.infrastructure.persistence

import com.example.news_summary.domain.news.model.CollectionLog
import com.example.news_summary.domain.news.model.CollectionLogId
import com.example.news_summary.domain.news.model.NewCollectionLog
import com.example.news_summary.domain.news.repository.CollectionLogRepository
import com.example.news_summary.domain.user.model.UserId
import org.springframework.stereotype.Component

@Component
class CollectionLogRepositoryImpl(
    private val jpaRepository: CollectionLogJpaRepository
) : CollectionLogRepository {

    override fun findByUserIdOrderByExecutedAtDesc(userId: UserId): List<CollectionLog> =
        jpaRepository.findByUserIdOrderByExecutedAtDesc(userId.value).map { it.toDomain() }

    override fun save(log: NewCollectionLog): CollectionLog {
        val entity = CollectionLogJpaEntity(
            userId = log.userId.value,
            articleCount = log.articleCount,
            status = log.status,
            errorMessage = log.errorMessage
        )
        return jpaRepository.save(entity).toDomain()
    }

    private fun CollectionLogJpaEntity.toDomain(): CollectionLog = CollectionLog(
        id = CollectionLogId(id ?: throw IllegalStateException("永続化済みCollectionLogのIDがnullです")),
        userId = UserId(userId),
        articleCount = articleCount,
        status = status,
        errorMessage = errorMessage,
        executedAt = executedAt ?: throw IllegalStateException("永続化済みCollectionLogのexecutedAtがnullです")
    )
}
