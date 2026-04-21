package com.example.news_summary.news.infrastructure.persistence

import com.example.news_summary.domain.news.model.CollectionLog
import com.example.news_summary.domain.news.model.CollectionLogId
import com.example.news_summary.domain.news.repository.CollectionLogRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Optional

/**
 * CollectionLogRepository のインフラ層実装。
 * JpaEntity ↔ ドメインモデルの変換を一元管理する。
 * id の null → CollectionLogId 変換はこのクラス内でのみ行われる。
 */
@Component
class CollectionLogRepositoryImpl(
    private val jpaRepository: CollectionLogJpaRepository
) : CollectionLogRepository {

    override fun findById(id: CollectionLogId): Optional<CollectionLog> =
        jpaRepository.findById(id.value).map { it.toDomain() }

    override fun findByUserIdOrderByExecutedAtDesc(userId: Long): List<CollectionLog> =
        jpaRepository.findByUserIdOrderByExecutedAtDesc(userId).map { it.toDomain() }

    override fun findByUserIdAndExecutedAtAfter(userId: Long, after: Instant): List<CollectionLog> =
        jpaRepository.findByUserIdAndExecutedAtAfter(userId, after).map { it.toDomain() }

    override fun save(userId: Long, articleCount: Int, status: String, errorMessage: String?): CollectionLog {
        val entity = CollectionLogJpaEntity(
            userId = userId,
            articleCount = articleCount,
            status = status,
            errorMessage = errorMessage
        )
        return jpaRepository.save(entity).toDomain()
    }

    /** JpaEntity → ドメインモデル変換。id の null チェックはここで1箇所だけ行う。 */
    private fun CollectionLogJpaEntity.toDomain(): CollectionLog = CollectionLog(
        id = CollectionLogId(id ?: throw IllegalStateException("永続化済みCollectionLogのIDがnullです")),
        userId = userId,
        articleCount = articleCount,
        status = status,
        errorMessage = errorMessage,
        executedAt = executedAt ?: throw IllegalStateException("永続化済みCollectionLogのexecutedAtがnullです")
    )
}
