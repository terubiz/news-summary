package com.example.news_summary.index.infrastructure.persistence

import com.example.news_summary.domain.index.model.IndexData
import com.example.news_summary.domain.index.model.IndexDataId
import com.example.news_summary.domain.index.repository.IndexDataRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.Optional

/**
 * IndexDataRepository のインフラ層実装。
 * JpaEntity ↔ ドメインモデルの変換を一元管理する。
 * id の null → IndexDataId 変換はこのクラス内でのみ行われる。
 */
@Component
class IndexDataRepositoryImpl(
    private val jpaRepository: IndexDataJpaRepository
) : IndexDataRepository {

    override fun findById(id: IndexDataId): Optional<IndexData> =
        jpaRepository.findById(id.value).map { it.toDomain() }

    override fun findLatestBySymbol(symbol: String): Optional<IndexData> =
        jpaRepository.findLatestBySymbol(symbol).map { it.toDomain() }

    override fun findLatestBySymbols(symbols: List<String>): List<IndexData> =
        jpaRepository.findLatestBySymbols(symbols).map { it.toDomain() }

    override fun save(indexData: IndexData): IndexData {
        val entity = IndexDataJpaEntity(
            id = indexData.id.value,
            symbol = indexData.symbol,
            currentValue = indexData.currentValue,
            changeAmount = indexData.changeAmount,
            changeRate = indexData.changeRate,
            isStale = indexData.isStale,
            fetchedAt = indexData.fetchedAt
        )
        return jpaRepository.save(entity).toDomain()
    }

    override fun saveNew(symbol: String, currentValue: BigDecimal, changeAmount: BigDecimal, changeRate: BigDecimal, isStale: Boolean): IndexData {
        val entity = IndexDataJpaEntity(
            symbol = symbol,
            currentValue = currentValue,
            changeAmount = changeAmount,
            changeRate = changeRate,
            isStale = isStale
        )
        return jpaRepository.save(entity).toDomain()
    }

    /** JpaEntity → ドメインモデル変換。id の null チェックはここで1箇所だけ行う。 */
    private fun IndexDataJpaEntity.toDomain(): IndexData = IndexData(
        id = IndexDataId(id ?: throw IllegalStateException("永続化済みIndexDataのIDがnullです")),
        symbol = symbol,
        currentValue = currentValue,
        changeAmount = changeAmount,
        changeRate = changeRate,
        isStale = isStale,
        fetchedAt = fetchedAt ?: throw IllegalStateException("永続化済みIndexDataのfetchedAtがnullです")
    )
}
