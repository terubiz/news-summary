package com.example.news_summary.notification.infrastructure.sender

import com.example.news_summary.domain.notification.model.ChannelType
import com.example.news_summary.domain.notification.model.DeliveryChannel
import com.example.news_summary.domain.notification.service.NotificationSender
import com.example.news_summary.domain.shared.service.EncryptionService
import com.example.news_summary.domain.summary.model.Summary
import com.example.news_summary.domain.summary.model.SummaryIndexImpact
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

/**
 * Slack通知送信アダプタ。
 * Incoming Webhook URL に HTTP POST でメッセージを送信する。
 */
@Component
class SlackNotificationAdapter(
    private val encryptionService: EncryptionService,
    private val objectMapper: ObjectMapper
) : NotificationSender {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val restTemplate = RestTemplate()
    override val channelType = ChannelType.SLACK

    override fun send(
        channel: DeliveryChannel,
        summary: Summary,
        impacts: List<SummaryIndexImpact>,
        sourceUrls: List<String>
    ) {
        val config = parseConfig(channel.encryptedConfig)
        val webhookUrl = config["webhookUrl"] ?: throw IllegalArgumentException("Slack Webhook URLが設定されていません")

        val message = NotificationMessageBuilder.buildPlainText(summary, impacts, sourceUrls)
        val payload = mapOf("text" to message)

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(objectMapper.writeValueAsString(payload), headers)

        restTemplate.postForEntity(webhookUrl, request, String::class.java)
        logger.info("Slack送信完了: summaryId=${summary.id.value}")
    }

    private fun parseConfig(encryptedConfig: String): Map<String, String> {
        val decrypted = encryptionService.decrypt(encryptedConfig)
        @Suppress("UNCHECKED_CAST")
        return objectMapper.readValue(decrypted, Map::class.java) as Map<String, String>
    }
}
