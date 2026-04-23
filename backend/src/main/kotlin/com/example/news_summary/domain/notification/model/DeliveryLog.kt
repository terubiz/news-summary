package com.example.news_summary.domain.notification.model

import java.time.Instant

/**
 * 通知送信ログの新規作成用モデル。IDやタイムスタンプを持たない。
 * リポジトリの save(NewDeliveryLog) で永続化し、DeliveryLog（ID確定済み）が返る。
 */
data class NewDeliveryLog(
    val channelId: Long,
    val summaryId: Long,
    /** SUCCESS / FAILED */
    val status: String,
    val retryCount: Int = 0,
    val errorMessage: String? = null
)

/**
 * 永続化済み通知送信ログ ドメインモデル。
 * id は常に non-null。「このオブジェクトが存在する = DBに保存済み」を型で保証する。
 */
data class DeliveryLog(
    val id: DeliveryLogId,
    val channelId: Long,
    val summaryId: Long,
    /** SUCCESS / FAILED */
    val status: String,
    val retryCount: Int = 0,
    val errorMessage: String? = null,
    val sentAt: Instant
)
