package com.example.news_summary.settings.infrastructure.persistence

import com.example.news_summary.domain.summary.model.SupplementLevel
import com.example.news_summary.domain.summary.model.SummaryMode
import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

/** JPA用要約設定エンティティ。ドメインモデルとは分離されている。 */
@Entity
@Table(
    name = "summary_settings",
    indexes = [Index(name = "idx_summary_settings_user_id", columnList = "user_id")]
)
class SummarySettingsJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,

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
)
