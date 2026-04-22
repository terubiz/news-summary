package com.example.news_summary.notification.infrastructure.persistence

import com.example.news_summary.domain.notification.model.DeliveryLog
import com.example.news_summary.domain.notification.model.DeliveryLogId
import com.example.news_summary.domain.notification.model.NewDeliveryLog
import com.example.news_summary.domain.notification.repository.DeliveryLogRepository
import org.springframework.stereotype.Component
import java.util.Optional

/**
 * DeliveryLogRepository のインフラ層実装。
 * JpaEntity ↔ ドメインモデルの変換を一元管理する。
 * id の null → DeliveryLogId 変換はこのクラス内でのみ行われる。
 */
@Component
class DeliveryLogRepositoryImpl(
    private val jpaRepository: DeliveryLogJpaRepository
) : DeliveryLogRepository {

    override fun findById(id: DeliveryLogId): Optional<DeliveryLog> =
        jpaRepository.findById(id.value).map { it.toDomain() }

    override fun findByChannelIdAndSummaryId(channelId: Long, summaryId: Long): Optional<DeliveryLog> =
        jpaRepository.findByChannelIdAndSummaryId(channelId, summaryId).map { it.toDomain() }

    override fun findByChannelIdOrderBySentAtDesc(channelId: Long): List<DeliveryLog> =
        jpaRepository.findByChannelIdOrderBySentAtDesc(channelId).map { it.toDomain() }

    override fun findRetryTargets(): List<DeliveryLog> =
        jpaRepository.findRetryTargets().map { it.toDomain() }

    override fun save(log: NewDeliveryLog): DeliveryLog {
        val entity = DeliveryLogJpaEntity(
            channelId = log.channelId,
            summaryId = log.summaryId,
            status = log.status,
            retryCount = log.retryCount,
            errorMessage = log.errorMessage
        )
        return jpaRepository.save(entity).toDomain()
    }

    /** JpaEntity → ドメインモデル変換。id の null チェックはここで1箇所だけ行う。 */
    private fun DeliveryLogJpaEntity.toDomain(): DeliveryLog = DeliveryLog(
        id = DeliveryLogId(id ?: throw IllegalStateException("永続化済みDeliveryLogのIDがnullです")),
        channelId = channelId,
        summaryId = summaryId,
        status = status,
        retryCount = retryCount,
        errorMessage = errorMessage,
        sentAt = sentAt ?: throw IllegalStateException("永続化済みDeliveryLogのsentAtがnullです")
    )
}
