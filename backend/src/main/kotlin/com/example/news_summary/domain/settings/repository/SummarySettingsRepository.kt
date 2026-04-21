package com.example.news_summary.domain.settings.repository

import com.example.news_summary.domain.settings.model.SummarySettings
import com.example.news_summary.domain.settings.model.SummarySettingsId
import java.util.Optional

/**
 * 要約設定リポジトリ（ドメイン層ポート）
 * ドメインモデルのみを扱う。JPA依存なし。
 */
interface SummarySettingsRepository {
    fun findById(id: SummarySettingsId): Optional<SummarySettings>
    fun findByUserId(userId: Long): Optional<SummarySettings>
    fun save(settings: SummarySettings): SummarySettings
}
