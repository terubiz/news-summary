package com.example.news_summary.summary.infrastructure.persistence

import com.example.news_summary.domain.summary.model.NewSummaryIndexImpact
import com.example.news_summary.domain.summary.model.SummaryIndexImpact
import com.example.news_summary.domain.summary.model.SummaryIndexImpactId
import com.example.news_summary.domain.summary.repository.SummaryIndexImpactRepository
import org.springframework.stereotype.Component
import java.util.Optional

/**
 * SummaryIndexImpactRepository のインフラ層実装。
 * JpaEntity ↔ ドメインモデルの変換を一元管理する。
 * id の null → SummaryIndexImpactId 変換はこのクラス内でのみ行われる。
 */
@Component
class SummaryIndexImpactRepositoryImpl(
    private val jpaRepository: SummaryIndexImpactJpaRepository
) : SummaryIndexImpactRepository {

    override fun findById(id: SummaryIndexImpactId): Optional<SummaryIndexImpact> =
        jpaRepository.findById(id.value).map { it.toDomain() }

    override fun findBySummaryId(summaryId: Long): List<SummaryIndexImpact> =
        jpaRepository.findBySummaryId(summaryId).map { it.toDomain() }

    override fun findByIndexSymbol(indexSymbol: String): List<SummaryIndexImpact> =
        jpaRepository.findByIndexSymbol(indexSymbol).map { it.toDomain() }

    override fun deleteBySummaryId(summaryId: Long) =
        jpaRepository.deleteBySummaryId(summaryId)

    override fun saveAll(impacts: List<NewSummaryIndexImpact>): List<SummaryIndexImpact> {
        val entities = impacts.map {
            SummaryIndexImpactJpaEntity(
                summaryId = it.summaryId,
                indexSymbol = it.indexSymbol,
                impactDirection = it.impactDirection
            )
        }
        return jpaRepository.saveAll(entities).map { it.toDomain() }
    }

    /** JpaEntity → ドメインモデル変換。id の null チェックはここで1箇所だけ行う。 */
    private fun SummaryIndexImpactJpaEntity.toDomain(): SummaryIndexImpact = SummaryIndexImpact(
        id = SummaryIndexImpactId(id ?: throw IllegalStateException("永続化済みSummaryIndexImpactのIDがnullです")),
        summaryId = summaryId,
        indexSymbol = indexSymbol,
        impactDirection = impactDirection
    )
}
