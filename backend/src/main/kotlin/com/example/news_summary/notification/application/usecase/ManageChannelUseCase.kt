package com.example.news_summary.notification.application.usecase

import com.example.news_summary.domain.notification.model.ChannelType
import com.example.news_summary.domain.notification.model.DeliveryChannel
import com.example.news_summary.domain.notification.model.DeliveryChannelId
import com.example.news_summary.domain.notification.model.NewDeliveryChannel
import com.example.news_summary.domain.notification.repository.DeliveryChannelRepository
import com.example.news_summary.domain.shared.service.EncryptionService
import com.example.news_summary.domain.user.model.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class CreateChannelCommand(
    val channelType: ChannelType,
    val config: String,           // 平文のJSON設定
    val deliverySchedule: String,
    val filterIndices: List<String>
)

data class UpdateChannelCommand(
    val config: String?,
    val deliverySchedule: String?,
    val filterIndices: List<String>?,
    val enabled: Boolean?
)

data class ChannelDto(
    val id: Long,
    val channelType: ChannelType,
    val config: String,           // 復号済みの平文JSON
    val deliverySchedule: String,
    val filterIndices: List<String>,
    val enabled: Boolean
)

@Service
class ManageChannelUseCase(
    private val channelRepository: DeliveryChannelRepository,
    private val encryptionService: EncryptionService
) {
    fun getChannels(userId: UserId): List<ChannelDto> =
        channelRepository.findByUserId(userId).map { it.toDto() }

    @Transactional
    fun createChannel(userId: UserId, command: CreateChannelCommand): ChannelDto {
        validateConfig(command.channelType, command.config)
        val channel = NewDeliveryChannel(
            userId = userId,
            channelType = command.channelType,
            encryptedConfig = encryptionService.encrypt(command.config),
            deliverySchedule = command.deliverySchedule,
            filterIndices = command.filterIndices
        )
        return channelRepository.save(channel).toDto()
    }

    @Transactional
    fun updateChannel(userId: UserId, channelId: DeliveryChannelId, command: UpdateChannelCommand): ChannelDto {
        val existing = channelRepository.findById(channelId)
            ?: throw NoSuchElementException("チャンネルが見つかりません")
        require(existing.userId == userId) { "このチャンネルへのアクセス権がありません" }

        val updated = existing.copy(
            encryptedConfig = command.config?.let {
                validateConfig(existing.channelType, it)
                encryptionService.encrypt(it)
            } ?: existing.encryptedConfig,
            deliverySchedule = command.deliverySchedule ?: existing.deliverySchedule,
            filterIndices = command.filterIndices ?: existing.filterIndices,
            enabled = command.enabled ?: existing.enabled
        )
        return channelRepository.update(updated).toDto()
    }

    @Transactional
    fun deleteChannel(userId: UserId, channelId: DeliveryChannelId) {
        val existing = channelRepository.findById(channelId)
            ?: throw NoSuchElementException("チャンネルが見つかりません")
        require(existing.userId == userId) { "このチャンネルへのアクセス権がありません" }
        channelRepository.delete(channelId)
    }

    fun testConnection(userId: UserId, channelId: DeliveryChannelId): Boolean {
        val channel = channelRepository.findById(channelId)
            ?: throw NoSuchElementException("チャンネルが見つかりません")
        require(channel.userId == userId) { "このチャンネルへのアクセス権がありません" }
        // 接続テストの実装はタスク12（通知アダプタ）で行う
        // ここでは設定の復号が成功することを確認する
        return try {
            encryptionService.decrypt(channel.encryptedConfig)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** チャンネルタイプに応じた設定値のバリデーション（要件6.5） */
    private fun validateConfig(channelType: ChannelType, config: String) {
        when (channelType) {
            ChannelType.SLACK -> {
                require(config.contains("webhookUrl")) { "Slack設定にはwebhookUrlが必要です" }
                require(config.contains("hooks.slack.com")) { "有効なSlack Webhook URLを入力してください" }
            }
            ChannelType.DISCORD -> {
                require(config.contains("webhookUrl")) { "Discord設定にはwebhookUrlが必要です" }
                require(
                    config.contains("discord.com/api/webhooks") || config.contains("discordapp.com/api/webhooks")
                ) { "有効なDiscord Webhook URLを入力してください" }
            }
            ChannelType.LINE -> {
                require(config.contains("channelAccessToken")) { "LINE設定にはchannelAccessTokenが必要です" }
                require(config.contains("userId")) { "LINE設定にはuserIdが必要です" }
            }
            ChannelType.EMAIL -> {
                require(config.contains("toAddress")) { "メール設定にはtoAddressが必要です" }
            }
        }
    }

    private fun DeliveryChannel.toDto(): ChannelDto = ChannelDto(
        id = id.value,
        channelType = channelType,
        config = encryptionService.decrypt(encryptedConfig),
        deliverySchedule = deliverySchedule,
        filterIndices = filterIndices,
        enabled = enabled
    )
}
