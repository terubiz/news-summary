package com.example.news_summary.notification.infrastructure.persistence

import com.example.news_summary.domain.notification.model.DeliveryChannel
import com.example.news_summary.domain.notification.model.DeliveryChannelId
import com.example.news_summary.domain.notification.repository.DeliveryChannelRepository
import com.example.news_summary.domain.user.model.UserId
import org.springframework.stereotype.Component

@Component
class DeliveryChannelRepositoryImpl(
    private val jpaRepository: DeliveryChannelJpaRepository
) : DeliveryChannelRepository {

    override fun findById(id: DeliveryChannelId): DeliveryChannel? =
        jpaRepository.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findByUserId(userId: UserId): List<DeliveryChannel> =
        jpaRepository.findByUserId(userId.value).map { it.toDomain() }

    override fun findByUserIdAndEnabledTrue(userId: UserId): List<DeliveryChannel> =
        jpaRepository.findByUserIdAndEnabledTrue(userId.value).map { it.toDomain() }

    override fun save(channel: DeliveryChannel): DeliveryChannel {
        val entity = DeliveryChannelJpaEntity(
            id = channel.id?.value,
            userId = channel.userId.value,
            channelType = channel.channelType,
            encryptedConfig = channel.encryptedConfig,
            deliverySchedule = channel.deliverySchedule,
            filterIndices = channel.filterIndices.toTypedArray(),
            enabled = channel.enabled,
            createdAt = channel.createdAt
        )
        return jpaRepository.save(entity).toDomain()
    }

    override fun delete(id: DeliveryChannelId) {
        jpaRepository.deleteById(id.value)
    }

    /** JpaEntity → ドメインモデル変換。id の null チェックはここで1箇所だけ行う。 */
    private fun DeliveryChannelJpaEntity.toDomain(): DeliveryChannel = DeliveryChannel(
        id = DeliveryChannelId(id ?: throw IllegalStateException("永続化済みDeliveryChannelのIDがnullです")),
        userId = UserId(userId),
        channelType = channelType,
        encryptedConfig = encryptedConfig,
        deliverySchedule = deliverySchedule,
        filterIndices = filterIndices.toList(),
        enabled = enabled,
        createdAt = createdAt ?: throw IllegalStateException("永続化済みDeliveryChannelのcreatedAtがnullです")
    )
}
