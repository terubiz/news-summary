package com.example.news_summary.summary.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/** Spring Data JPA リポジトリ（インフラ層）。ドメイン層からは直接参照しない。 */
@Repository
interface SummaryIndexImpactJpaRepository : JpaRepository<SummaryIndexImpactJpaEntity, Long> {
    fun findBySummaryId(summaryId: Long): List<SummaryIndexImpactJpaEntity>
    fun findByIndexSymbol(indexSymbol: String): List<SummaryIndexImpactJpaEntity>
    fun deleteBySummaryId(summaryId: Long)
}
