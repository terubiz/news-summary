package com.example.news_summary.scheduler

import com.example.news_summary.domain.notification.repository.DeliveryChannelRepository
import com.example.news_summary.domain.notification.service.NotificationService
import com.example.news_summary.domain.summary.model.SummaryStatus
import com.example.news_summary.domain.summary.repository.SummaryIndexImpactRepository
import com.example.news_summary.domain.summary.repository.SummaryRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * 通知送信スケジューラジョブ。
 * 各チャンネルの delivery_schedule に基づいて送信をトリガーする。
 * フィルタ条件（対象Stock_Index）に合致する要約のみを送信対象とする。
 */
@Component
class NotificationDeliveryJob(
    private val deliveryChannelRepository: DeliveryChannelRepository,
    private val summaryRepository: SummaryRepository,
    private val summaryIndexImpactRepository: SummaryIndexImpactRepository,
    private val notificationService: NotificationService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 毎分実行し、スケジュールに合致するチャンネルへ通知を送信する。
     * - IMMEDIATE: 新しい要約が生成されるたびに即時送信（AISummarizerServiceから直接呼ばれる想定）
     * - HOURLY: 毎時0分に直近1時間の要約を送信
     * - DAILY_HH:MM: 指定時刻に直近24時間の要約を送信
     */
    @Scheduled(cron = "0 * * * * *")
    fun execute() {
        val now = Instant.now()
        val currentTime = LocalTime.now(ZoneId.of("Asia/Tokyo"))
        val channels = deliveryChannelRepository.findAllByEnabledTrue()

        channels.forEach { channel ->
            try {
                val schedule = channel.deliverySchedule
                val shouldSend = when {
                    schedule == "IMMEDIATE" -> false // 即時送信はイベント駆動で処理
                    schedule == "HOURLY" && currentTime.minute == 0 -> true
                    schedule.startsWith("DAILY_") -> {
                        val scheduledTime = parseScheduledTime(schedule)
                        scheduledTime != null &&
                            currentTime.hour == scheduledTime.hour &&
                            currentTime.minute == scheduledTime.minute
                    }
                    else -> false
                }

                if (!shouldSend) return@forEach

                // 対象期間の要約を取得
                val lookbackPeriod = when {
                    schedule == "HOURLY" -> now.minus(1, ChronoUnit.HOURS)
                    schedule.startsWith("DAILY_") -> now.minus(24, ChronoUnit.HOURS)
                    else -> return@forEach
                }

                val summaries = summaryRepository.findByGeneratedAtAfter(lookbackPeriod)
                    .filter { it.status == SummaryStatus.COMPLETED }

                // フィルタ条件（対象Stock_Index）に合致する要約のみ
                val filteredSummaries = if (channel.filterIndices.isNotEmpty()) {
                    summaries.filter { summary ->
                        val impacts = summaryIndexImpactRepository.findBySummaryId(summary.id.value)
                        impacts.any { impact -> impact.indexSymbol in channel.filterIndices }
                    }
                } else {
                    summaries
                }

                // 各要約を送信
                filteredSummaries.forEach { summary ->
                    notificationService.sendToChannel(summary, channel)
                }

                if (filteredSummaries.isNotEmpty()) {
                    logger.info(
                        "通知送信完了: channelId=${channel.id.value}, " +
                        "schedule=$schedule, summaries=${filteredSummaries.size}件"
                    )
                }
            } catch (e: Exception) {
                logger.error(
                    "通知送信ジョブ失敗: channelId=${channel.id.value}, error=${e.message}", e
                )
            }
        }
    }

    /**
     * 失敗した通知のリトライジョブ。
     * 15分ごとに実行する。
     */
    @Scheduled(cron = "0 */15 * * * *")
    fun retryFailed() {
        logger.info("通知リトライジョブ開始")
        try {
            notificationService.retryFailedDeliveries()
        } catch (e: Exception) {
            logger.error("通知リトライジョブ失敗: ${e.message}", e)
        }
    }

    /** "DAILY_HH:MM" 形式からLocalTimeをパースする */
    private fun parseScheduledTime(schedule: String): LocalTime? = try {
        val timePart = schedule.removePrefix("DAILY_")
        LocalTime.parse(timePart)
    } catch (_: Exception) {
        logger.warn("無効なスケジュール形式: $schedule")
        null
    }
}
