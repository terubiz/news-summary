package com.example.news_summary.summary.infrastructure.persistence

import com.example.news_summary.domain.summary.model.ImpactDirection
import jakarta.persistence.*

/** JPA用要約指数影響エンティティ。ドメインモデルとは分離されている。 */
@Entity
@Table(
    name = "summary_index_impacts",
    indexes = [
        Index(name = "idx_summary_index_impacts_summary_id", columnList = "summary_id"),
        Index(name = "idx_summary_index_impacts_symbol", columnList = "index_symbol")
    ]
)
class SummaryIndexImpactJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "summary_id", nullable = false)
    val summaryId: Long = 0,

    @Column(name = "index_symbol", nullable = false, length = 50)
    val indexSymbol: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "impact_direction", nullable = false, length = 50)
    val impactDirection: ImpactDirection = ImpactDirection.NEUTRAL
)
