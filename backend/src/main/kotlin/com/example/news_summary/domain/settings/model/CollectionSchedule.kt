package com.example.news_summary.domain.settings.model

import com.example.news_summary.domain.user.model.UserId
import java.time.Instant

/**
 * 収集スケジュールの新規作成用モデル。IDやタイムスタンプを持たない。
 * リポジトリの save(NewCollectionSchedule) で永続化し、CollectionSchedule（ID確定済み）が返る。
 */
data class NewCollectionSchedule(
    val userId: UserId,
    /** Quartz Cron式（例: "0 0 8,12,18 * * ?"） */
    val cronExpression: String,
    val enabled: Boolean = true
)

/**
 * 永続化済み収集スケジュールドメインモデル。
 * id は常に non-null。「このオブジェクトが存在する = DBに保存済み」を型で保証する。
 */
data class CollectionSchedule(
    val id: CollectionScheduleId,
    val userId: UserId,
    /** Quartz Cron式（例: "0 0 8,12,18 * * ?"） */
    val cronExpression: String,
    val enabled: Boolean = true,
    val updatedAt: Instant
)
