package com.example.news_summary.domain.settings.repository

import com.example.news_summary.domain.settings.model.CollectionSchedule
import com.example.news_summary.domain.settings.model.CollectionScheduleId
import java.util.Optional

/**
 * 収集スケジュールリポジトリ（ドメイン層ポート）
 * ドメインモデルのみを扱う。JPA依存なし。
 */
interface CollectionScheduleRepository {
    fun findById(id: CollectionScheduleId): Optional<CollectionSchedule>
    fun findByUserId(userId: Long): Optional<CollectionSchedule>
    fun findByEnabledTrue(): List<CollectionSchedule>
    fun save(schedule: CollectionSchedule): CollectionSchedule
}
