package com.example.news_summary.domain.notification.service

import com.example.news_summary.domain.notification.model.DeliveryChannel
import com.example.news_summary.domain.summary.model.Summary
import com.example.news_summary.domain.summary.model.SummaryIndexImpact

/**
 * 通知送信アダプタの共通インターフェース。
 * 各チャンネル（Email/Slack/LINE/Discord）の送信アダプタが実装する。
 */
interface NotificationSender {
    /** このアダプタが対応するチャンネル種別 */
    val channelType: com.example.news_summary.domain.notification.model.ChannelType

    /**
     * 要約を送信する。
     * @param channel 送信先チャンネル設定
     * @param summary 送信する要約
     * @param impacts 関連する株価指数影響
     * @param sourceUrls 要約元記事のURL一覧
     * @throws Exception 送信失敗時
     */
    fun send(
        channel: DeliveryChannel,
        summary: Summary,
        impacts: List<SummaryIndexImpact>,
        sourceUrls: List<String>
    )
}
