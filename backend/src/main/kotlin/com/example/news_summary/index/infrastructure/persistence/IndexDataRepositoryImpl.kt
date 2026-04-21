package com.example.news_summary.index.infrastructure.persistence

import com.example.news_summary.domain.index.model.IndexData
import com.example.news_summary.domain.index.model.IndexDataId
import com.example.news_summary.domain.index.repository.IndexDataRepository
import org.springframework.stereotype.Component

@Component
class IndexDataRepositoryImpl(
    private val jpaRepository: IndexDataJpaRepository
) : IndexDataRepository {

    override fun findLatestBySymbol(symbol: String): IndexData? =
        jpaRepository.findLatestBySymbol(symbol).map { it.toDomain() }.orElse(null)

    override fun findLatestBySymbols(symbols: List<String>): List<IndexData> =
        jpaRepository.findLatestBySymbols(symbols).map { it.toDomain() }

    override fun save(indexData: IndexData): IndexData {
        val entity = IndexDataJpaEntity(
            id = indexData.id?.value,
            symbol = indexData.symbol,
            currentValue = indexData.currentValue,
            changeAmount = indexData.changeAmount,
            changeRate = indexData.changeRate,
            isStale = indexData.isStale,
            fetchedAt = indexData.fetchedAt
        )
        return jpaRepository.save(entity).toDomain()
    }

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
