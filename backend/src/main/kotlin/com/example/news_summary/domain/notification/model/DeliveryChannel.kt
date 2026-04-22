package com.example.news_summary.domain.notification.model

import com.example.news_summary.domain.user.model.UserId
import java.time.Instant

/**
 * 通知チャンネルの新規作成用モデル。IDやタイムスタンプを持たない。
 * リポジトリの save(NewDeliveryChannel) で永続化し、DeliveryChannel（ID確定済み）が返る。
 */
data class NewDeliveryChannel(
    val userId: UserId,
    val channelType: ChannelType,
    /** AES-256-GCM で暗号化された接続設定（JSON） */
    val encryptedConfig: String,
    /** 送信スケジュール: IMMEDIATE / HOURLY / DAILY_HH:MM */
    val deliverySchedule: String = "IMMEDIATE",
    val filterIndices: List<String> = emptyList()
)

/**
 * 永続化済み通知送信チャンネル ドメインモデル（集約ルート）。
 * id は常に non-null。「このオブジェクトが存在する = DBに保存済み」を型で保証する。
 */
data class DeliveryChannel(
    val id: DeliveryChannelId,
    val userId: UserId,
    val channelType: ChannelType,
    /** AES-256-GCM で暗号化された接続設定（JSON） */
    val encryptedConfig: String,
    /** 送信スケジュール: IMMEDIATE / HOURLY / DAILY_HH:MM */
    val deliverySchedule: String = "IMMEDIATE",
    val filterIndices: List<String> = emptyList(),
    val enabled: Boolean = true,
    val createdAt: Instant
)
