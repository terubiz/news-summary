package com.example.economicnews.domain.notification.service

import com.example.economicnews.domain.notification.model.DeliveryChannel
import com.example.economicnews.domain.summary.model.Summary

/**
 * 通知送信ドメインサービス
 * 各チャンネル（Email/Slack/LINE/Discord）への要約送信を担う
 */
interface NotificationService {
    /**
     * 単一チャンネルへ要約を送信する
     * @return 送信結果（成功/失敗・エラーメッセージ）
     */
    fun sendToChannel(summary: Summary, channel: DeliveryChannel): DeliveryResult

    /**
     * 複数チャンネルへ並行送信する（ダッシュボードからの即時送信）
     */
    fun sendToChannels(summary: Summary, channels: List<DeliveryChannel>): List<DeliveryResult>

    /**
     * 失敗した送信を再試行する（最大3回）
     */
    fun retryFailedDeliveries()
}

data class DeliveryResult(
    val channelId: Long,
    val success: Boolean,
    val errorMessage: String? = null
)
