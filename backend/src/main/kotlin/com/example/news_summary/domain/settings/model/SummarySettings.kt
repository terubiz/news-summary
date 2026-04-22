package com.example.news_summary.domain.settings.model

import com.example.news_summary.domain.summary.model.SupplementLevel
import com.example.news_summary.domain.summary.model.SummaryMode
import com.example.news_summary.domain.user.model.UserId
import java.time.Instant

/**
 * 要約設定の新規作成用モデル。IDやタイムスタンプを持たない。
 * リポジトリの save(NewSummarySettings) で永続化し、SummarySettings（ID確定済み）が返る。
 */
data class NewSummarySettings(
    val userId: UserId,
    val selectedIndices: List<String> = emptyList(),
    val analysisPerspectives: List<String> = emptyList(),
    val supplementLevel: SupplementLevel = SupplementLevel.INTERMEDIATE,
    val summaryMode: SummaryMode = SummaryMode.STANDARD
)

/**
 * 永続化済み要約設定ドメインモデル（集約ルート）。
 * id は常に non-null。「このオブジェクトが存在する = DBに保存済み」を型で保証する。
 */
data class SummarySettings(
    val id: SummarySettingsId,
    val userId: UserId,
    val selectedIndices: List<String> = emptyList(),
    val analysisPerspectives: List<String> = emptyList(),
    val supplementLevel: SupplementLevel = SupplementLevel.INTERMEDIATE,
    val summaryMode: SummaryMode = SummaryMode.STANDARD,
    val updatedAt: Instant
)
