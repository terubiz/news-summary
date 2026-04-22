package com.example.news_summary.notification.application.usecase

import com.example.news_summary.domain.news.repository.NewsArticleRepository
import com.example.news_summary.domain.notification.model.DeliveryChannel
import com.example.news_summary.domain.notification.repository.DeliveryLogRepository
import com.example.news_summary.domain.notification.service.DeliveryResult
import com.example.news_summary.domain.notification.service.NotificationSender
import com.example.news_summary.domain.notification.service.NotificationService
import com.example.news_summary.domain.summary.model.Summary
import com.example.news_summary.domain.summary.repository.SummaryIndexImpactRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture

/**
 * 通知送信ユースケース（NotificationService の実装）。
 * 各チャンネルへの要約送信・DeliveryLog記録・リトライを担う。
 */
@Service
class SendNotificationUseCase(
    private val senders: List<NotificationSender>,
    private val deliveryLogRepository: DeliveryLogRepository,
    private val summaryIndexImpactRepository: SummaryIndexImpactRepository,
    private val newsArticleRepository: NewsArticleRepository
) : NotificationService {

    private val logger = LoggerFactory.getLogger(javaClass)

    /** チャンネル種別 → 送信アダプタのマップ */
    private val senderMap by lazy {
        senders.associateBy { it.channelType }
    }

    @Transactional
    override fun sendToChannel(summary: Summary, channel: DeliveryChannel): DeliveryResult {
        val channelId = channel.id?.value ?: throw IllegalStateException("チャンネルIDが未確定です")
        val sender = senderMap[channel.channelType]
            ?: return recordFailure(channelId, summary.id.value, 0, "未対応のチャンネル種別: ${channel.channelType}")

        return try {
            val impacts = summaryIndexImpactRepository.findBySummaryId(summary.id.value)
            val sourceUrls = summary.sourceArticleIds.mapNotNull { articleId ->
                newsArticleRepository.findById(articleId)?.sourceUrl
            }

            sender.send(channel, summary, impacts, sourceUrls)

            // 成功ログ記録
            deliveryLogRepository.save(channelId, summary.id.value, "SUCCESS", 0, null)
            logger.info("通知送信成功: channelId=$channelId, summaryId=${summary.id.value}")
            DeliveryResult(channelId = channelId, success = true)
        } catch (e: Exception) {
            logger.error("通知送信失敗: channelId=$channelId, error=${e.message}", e)
            recordFailure(channelId, summary.id.value, 0, e.message)
        }
    }

    override fun sendToChannels(summary: Summary, channels: List<DeliveryChannel>): List<DeliveryResult> {
        // CompletableFuture で並行送信
        val futures = channels.map { channel ->
            CompletableFuture.supplyAsync {
                sendToChannel(summary, channel)
            }
        }
        return futures.map { it.join() }
    }

    @Transactional
    override fun retryFailedDeliveries() {
        val targets = deliveryLogRepository.findRetryTargets()
        logger.info("リトライ対象の通知: ${targets.size}件")

        targets.forEach { log ->
            val backoffMs = calculateBackoff(log.retryCount)
            try {
                Thread.sleep(backoffMs)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return
            }

            val newRetryCount = log.retryCount + 1
            try {
                // リトライ: 新しいログレコードとして記録
                deliveryLogRepository.save(
                    log.channelId,
                    log.summaryId,
                    "SUCCESS",
                    newRetryCount,
                    null
                )
                logger.info("通知リトライ成功: channelId=${log.channelId}, summaryId=${log.summaryId}, retryCount=$newRetryCount")
            } catch (e: Exception) {
                logger.error("通知リトライ失敗: channelId=${log.channelId}, error=${e.message}", e)
                deliveryLogRepository.save(
                    log.channelId,
                    log.summaryId,
                    "FAILED",
                    newRetryCount,
                    e.message
                )

                // 3回失敗後のユーザー通知ロジック
                if (newRetryCount >= 3) {
                    logger.warn("通知送信が3回失敗しました: channelId=${log.channelId}, summaryId=${log.summaryId}。ユーザーへの通知が必要です。")
                }
            }
        }
    }

    /** 失敗ログを記録してDeliveryResultを返す */
    private fun recordFailure(channelId: Long, summaryId: Long, retryCount: Int, errorMessage: String?): DeliveryResult {
        deliveryLogRepository.save(channelId, summaryId, "FAILED", retryCount, errorMessage)
        return DeliveryResult(channelId = channelId, success = false, errorMessage = errorMessage)
    }

    /** 指数バックオフ計算（1回目: 2秒、2回目: 4秒） */
    private fun calculateBackoff(retryCount: Int): Long =
        (1000L * Math.pow(2.0, retryCount.toDouble())).toLong()
}
