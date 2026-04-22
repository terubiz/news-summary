package com.example.news_summary.notification.infrastructure.sender

import com.example.news_summary.domain.notification.model.ChannelType
import com.example.news_summary.domain.notification.model.DeliveryChannel
import com.example.news_summary.domain.notification.service.NotificationSender
import com.example.news_summary.domain.shared.service.EncryptionService
import com.example.news_summary.domain.summary.model.Summary
import com.example.news_summary.domain.summary.model.SummaryIndexImpact
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

/**
 * Email通知送信アダプタ。
 * Spring Mail を使ってメールを送信する。
 */
@Component
class EmailNotificationAdapter(
    private val mailSender: JavaMailSender,
    private val encryptionService: EncryptionService,
    private val objectMapper: ObjectMapper
) : NotificationSender {

    private val logger = LoggerFactory.getLogger(javaClass)
    override val channelType = ChannelType.EMAIL

    override fun send(
        channel: DeliveryChannel,
        summary: Summary,
        impacts: List<SummaryIndexImpact>,
        sourceUrls: List<String>
    ) {
        val config = parseConfig(channel.encryptedConfig)
        val toAddress = config["email"] ?: throw IllegalArgumentException("メールアドレスが設定されていません")

        val htmlContent = NotificationMessageBuilder.buildHtml(summary, impacts, sourceUrls)

        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setTo(toAddress)
        helper.setSubject("📰 経済ニュース要約 - ${summary.generatedAt}")
        helper.setText(htmlContent, true)

        mailSender.send(message)
        logger.info("Email送信完了: to=$toAddress, summaryId=${summary.id.value}")
    }

    private fun parseConfig(encryptedConfig: String): Map<String, String> {
        val decrypted = encryptionService.decrypt(encryptedConfig)
        @Suppress("UNCHECKED_CAST")
        return objectMapper.readValue(decrypted, Map::class.java) as Map<String, String>
    }
}
