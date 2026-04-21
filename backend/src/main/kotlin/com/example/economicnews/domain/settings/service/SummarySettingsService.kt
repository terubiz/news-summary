package com.example.economicnews.domain.settings.service

import com.example.economicnews.domain.settings.model.CollectionSchedule
import com.example.economicnews.domain.settings.model.SummarySettings

/**
 * 要約設定ドメインサービス
 * ユーザーごとの要約設定・収集スケジュール管理を担う
 */
interface SummarySettingsService {
    /** ユーザーの要約設定を取得する（存在しない場合はデフォルト値を返す） */
    fun getByUserId(userId: Long): SummarySettings

    /** 要約設定を保存する */
    fun save(settings: SummarySettings): SummarySettings

    /** 収集スケジュールを取得する */
    fun getScheduleByUserId(userId: Long): CollectionSchedule?

    /** 収集スケジュールを保存する */
    fun saveSchedule(schedule: CollectionSchedule): CollectionSchedule
}
