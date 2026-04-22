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
 * LINE通知送信アダプタ。
 * LINE Messaging API を使ってプッシュメッセージを送信する。
 */
@Component
class LineNotificationAdapter(
    private val encryptionService: EncryptionService,
    private val objectMapper: ObjectMapper
) : NotificationSender {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val restTemplate = RestTemplate()
    override val channelType = ChannelType.LINE

    companion object {
        private const val LINE_API_URL = "https://api.line.me/v2/bot/message/push"
    }

    override fun send(
        channel: DeliveryChannel,
        summary: Summary,
        impacts: List<SummaryIndexImpact>,
        sourceUrls: List<String>
    ) {
        val config = parseConfig(channel.encryptedConfig)
        val accessToken = config["accessToken"] ?: throw IllegalArgumentException("LINE Access Tokenが設定されていません")
        val userId = config["userId"] ?: throw IllegalArgumentException("LINE User IDが設定されていません")

        val message = NotificationMessageBuilder.buildPlainText(summary, impacts, sourceUrls)
        val payload = mapOf(
            "to" to userId,
            "messages" to listOf(
                mapOf(
                    "type" to "text",
                    "text" to message
                )
            )
        )

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(accessToken)
        }
        val request = HttpEntity(objectMapper.writeValueAsString(payload), headers)

        restTemplate.postForEntity(LINE_API_URL, request, String::class.java)
        logger.info("LINE送信完了: summaryId=${summary.id.value}")
    }

    private fun parseConfig(encryptedConfig: String): Map<String, String> {
        val decrypted = encryptionService.decrypt(encryptedConfig)
        @Suppress("UNCHECKED_CAST")
        return objectMapper.readValue(decrypted, Map::class.java) as Map<String, String>
    }
}
