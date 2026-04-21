package com.example.news_summary.domain.notification.model

import java.time.Instant

/**
 * 通知送信チャンネル ドメインモデル（集約ルート）
 * JPAアノテーションを持たない純粋なドメインオブジェクト。
 * id: DeliveryChannelId により「永続化済み = IDが確定している」ことを型で保証する。
 */
data class DeliveryChannel(
    val id: DeliveryChannelId,
    val userId: Long,
    val channelType: ChannelType,
    /** AES-256-GCM で暗号化された接続設定（JSON） */
    val encryptedConfig: String,
    /** 送信スケジュール: IMMEDIATE / HOURLY / DAILY_HH:MM */
    val deliverySchedule: String = "IMMEDIATE",
    val filterIndices: List<String> = emptyList(),
    val enabled: Boolean = true,
    val createdAt: Instant
)
