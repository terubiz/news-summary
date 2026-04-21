package com.example.news_summary.domain.settings.model

import com.example.news_summary.domain.summary.model.SupplementLevel
import com.example.news_summary.domain.summary.model.SummaryMode
import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

/** 要約設定エンティティ（集約ルート） */
@Entity
@Table(
    name = "summary_settings",
    indexes = [Index(name = "idx_summary_settings_user_id", columnList = "user_id")]
)
data class SummarySettings(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    /** 選択中の株価指数シンボル（例: ["N225", "SPX", "IXIC", "DAX"]） */
    @Column(name = "selected_indices", nullable = false, columnDefinition = "varchar(50)[]")
    val selectedIndices: Array<String> = emptyArray(),

    /** 選択中の分析観点（AnalysisPerspective enum 名） */
    @Column(name = "analysis_perspectives", nullable = false, columnDefinition = "varchar(100)[]")
    val analysisPerspectives: Array<String> = emptyArray(),

    @Enumerated(EnumType.STRING)
    @Column(name = "supplement_level", nullable = false, length = 50)
    val supplementLevel: SupplementLevel = SupplementLevel.INTERMEDIATE,

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_mode", nullable = false, length = 50)
    val summaryMode: SummaryMode = SummaryMode.STANDARD,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SummarySettings) return false
        return id == other.id &&
            userId == other.userId &&
            selectedIndices.contentEquals(other.selectedIndices) &&
            analysisPerspectives.contentEquals(other.analysisPerspectives) &&
            supplementLevel == other.supplementLevel &&
            summaryMode == other.summaryMode
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + userId.hashCode()
        result = 31 * result + selectedIndices.contentHashCode()
        result = 31 * result + analysisPerspectives.contentHashCode()
        result = 31 * result + supplementLevel.hashCode()
        result = 31 * result + summaryMode.hashCode()
        return result
    }
}
