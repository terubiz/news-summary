package com.example.news_summary.domain.summary.repository

import com.example.news_summary.domain.summary.model.SummaryIndexImpact
import com.example.news_summary.domain.summary.model.SummaryIndexImpactId
import java.util.Optional

/**
 * 要約指数影響リポジトリ（ドメイン層ポート）
 * ドメインモデルのみを扱う。JPA依存なし。
 */
interface SummaryIndexImpactRepository {
    fun findById(id: SummaryIndexImpactId): Optional<SummaryIndexImpact>
    fun findBySummaryId(summaryId: Long): List<SummaryIndexImpact>
    fun findByIndexSymbol(indexSymbol: String): List<SummaryIndexImpact>
    fun deleteBySummaryId(summaryId: Long)
    fun saveAll(impacts: List<SummaryIndexImpact>): List<SummaryIndexImpact>
}
