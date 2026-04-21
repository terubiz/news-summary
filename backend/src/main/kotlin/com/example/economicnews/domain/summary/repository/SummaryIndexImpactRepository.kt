package com.example.economicnews.domain.summary.repository

import com.example.economicnews.domain.summary.model.SummaryIndexImpact
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SummaryIndexImpactRepository : JpaRepository<SummaryIndexImpact, Long> {
    fun findBySummaryId(summaryId: Long): List<SummaryIndexImpact>
    fun findByIndexSymbol(indexSymbol: String): List<SummaryIndexImpact>
    fun deleteBySummaryId(summaryId: Long)
}
