package com.example.news_summary.notification.infrastructure.persistence

import com.example.news_summary.domain.notification.model.DeliveryChannel
import com.example.news_summary.domain.notification.model.DeliveryChannelId
import com.example.news_summary.domain.notification.repository.DeliveryChannelRepository
import org.springframework.stereotype.Component
import java.util.Optional

/**
 * DeliveryChannelRepository のインフラ層実装。
 * JpaEntity ↔ ドメインモデルの変換を一元管理する。
 * id の null → DeliveryChannelId 変換はこのクラス内でのみ行われる。
 */
@Component
class DeliveryChannelRepositoryImpl(
    private val jpaRepository: DeliveryChannelJpaRepository
) : DeliveryChannelRepository {

    override fun findById(id: DeliveryChannelId): Optional<DeliveryChannel> =
        jpaRepository.findById(id.value).map { it.toDomain() }

    override fun findByUserIdAndEnabledTrue(userId: Long): List<DeliveryChannel> =
        jpaRepository.findByUserIdAndEnabledTrue(userId).map { it.toDomain() }

    override fun findByUserId(userId: Long): List<DeliveryChannel> =
        jpaRepository.findByUserId(userId).map { it.toDomain() }

    override fun save(channel: DeliveryChannel): DeliveryChannel {
        val entity = DeliveryChannelJpaEntity(
            id = channel.id.value,
            userId = channel.userId,
            channelType = channel.channelType,
            encryptedConfig = channel.encryptedConfig,
            deliverySchedule = channel.deliverySchedule,
            filterIndices = channel.filterIndices.toTypedArray(),
            enabled = channel.enabled,
            createdAt = channel.createdAt
        )
        return jpaRepository.save(entity).toDomain()
    }

    /** JpaEntity → ドメインモデル変換。id の null チェックはここで1箇所だけ行う。 */
    private fun DeliveryChannelJpaEntity.toDomain(): DeliveryChannel = DeliveryChannel(
        id = DeliveryChannelId(id ?: throw IllegalStateException("永続化済みDeliveryChannelのIDがnullです")),
        userId = userId,
        channelType = channelType,
        encryptedConfig = encryptedConfig,
        deliverySchedule = deliverySchedule,
        filterIndices = filterIndices.toList(),
        enabled = enabled,
        createdAt = createdAt ?: throw IllegalStateException("永続化済みDeliveryChannelのcreatedAtがnullです")
    )
}
