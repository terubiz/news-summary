package com.example.news_summary.domain.settings.service

import com.example.news_summary.domain.settings.model.CollectionSchedule
import com.example.news_summary.domain.settings.model.NewCollectionSchedule
import com.example.news_summary.domain.settings.model.NewSummarySettings
import com.example.news_summary.domain.settings.model.SummarySettings

/**
 * 要約設定ドメインサービス
 * ユーザーごとの要約設定・収集スケジュール管理を担う
 */
interface SummarySettingsService {
    /** ユーザーの要約設定を取得する（存在しない場合はデフォルト値を返す） */
    fun getByUserId(userId: Long): SummarySettings

    /** 要約設定を新規保存する */
    fun save(settings: NewSummarySettings): SummarySettings

    /** 要約設定を更新する */
    fun update(settings: SummarySettings): SummarySettings

    /** 収集スケジュールを取得する */
    fun getScheduleByUserId(userId: Long): CollectionSchedule?

    /** 収集スケジュールを新規保存する */
    fun saveSchedule(schedule: NewCollectionSchedule): CollectionSchedule

    /** 収集スケジュールを更新する */
    fun updateSchedule(schedule: CollectionSchedule): CollectionSchedule
}
