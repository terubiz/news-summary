package com.example.news_summary.domain.settings.model

import com.example.news_summary.domain.summary.model.SupplementLevel
import com.example.news_summary.domain.summary.model.SummaryMode
import com.example.news_summary.domain.user.model.UserId
import java.time.Instant

/**
 * 要約設定ドメインモデル（集約ルート）
 * 新規作成時は id = null を許容する（save後にIDが確定する）。
 */
data class SummarySettings(
    val id: SummarySettingsId? = null,
    val userId: UserId,
    val selectedIndices: List<String> = emptyList(),
    val analysisPerspectives: List<String> = emptyList(),
    val supplementLevel: SupplementLevel = SupplementLevel.INTERMEDIATE,
    val summaryMode: SummaryMode = SummaryMode.STANDARD,
    val updatedAt: Instant? = null
)
