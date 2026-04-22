package com.example.news_summary.domain.notification.model

import com.example.news_summary.domain.user.model.UserId
import java.time.Instant

/**
 * 通知送信チャンネル ドメインモデル（集約ルート）
 * JPAアノテーションを持たない純粋なドメインオブジェクト。
 * id: DeliveryChannelId により「永続化済み = IDが確定している」ことを型で保証する。
 * 新規作成時は id = null を許容する（save後にIDが確定する）。
 */
data class DeliveryChannel(
    val id: DeliveryChannelId? = null,
    val userId: UserId,
    val channelType: ChannelType,
    /** AES-256-GCM で暗号化された接続設定（JSON） */
    val encryptedConfig: String,
    /** 送信スケジュール: IMMEDIATE / HOURLY / DAILY_HH:MM */
    val deliverySchedule: String = "IMMEDIATE",
    val filterIndices: List<String> = emptyList(),
    val enabled: Boolean = true,
    val createdAt: Instant? = null
)
