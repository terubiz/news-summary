package com.example.news_summary.domain.notification.model

import java.time.Instant

/**
 * 通知送信ログ ドメインモデル
 * JPAアノテーションを持たない純粋なドメインオブジェクト。
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
