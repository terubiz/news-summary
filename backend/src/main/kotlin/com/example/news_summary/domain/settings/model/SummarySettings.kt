package com.example.news_summary.domain.settings.model

import com.example.news_summary.domain.summary.model.SupplementLevel
import com.example.news_summary.domain.summary.model.SummaryMode
import java.time.Instant

/**
 * 要約設定ドメインモデル（集約ルート）
 * JPAアノテーションを持たない純粋なドメインオブジェクト。
 * id: SummarySettingsId により「永続化済み = IDが確定している」ことを型で保証する。
 */
data class SummarySettings(
    val id: SummarySettingsId,
    val userId: Long,
    /** 選択中の株価指数シンボル（例: ["N225", "SPX", "IXIC", "DAX"]） */
    val selectedIndices: List<String> = emptyList(),
    /** 選択中の分析観点（AnalysisPerspective enum 名） */
    val analysisPerspectives: List<String> = emptyList(),
    val supplementLevel: SupplementLevel = SupplementLevel.INTERMEDIATE,
    val summaryMode: SummaryMode = SummaryMode.STANDARD,
    val updatedAt: Instant
)
