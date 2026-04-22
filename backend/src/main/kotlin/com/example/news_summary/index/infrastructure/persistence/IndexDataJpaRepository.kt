package com.example.news_summary.index.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

/** Spring Data JPA リポジトリ（インフラ層）。ドメイン層からは直接参照しない。 */
@Repository
interface IndexDataJpaRepository : JpaRepository<IndexDataJpaEntity, Long> {
    /** シンボルの最新データを取得する */
    @Query("SELECT i FROM IndexDataJpaEntity i WHERE i.symbol = :symbol ORDER BY i.fetchedAt DESC LIMIT 1")
    fun findLatestBySymbol(symbol: String): Optional<IndexDataJpaEntity>

    /** 複数シンボルの最新データを取得する */
    @Query("""
        SELECT i FROM IndexDataJpaEntity i
        WHERE i.symbol IN :symbols
        AND i.fetchedAt = (
            SELECT MAX(i2.fetchedAt) FROM IndexDataJpaEntity i2 WHERE i2.symbol = i.symbol
        )
    """)
    fun findLatestBySymbols(symbols: List<String>): List<IndexDataJpaEntity>
}
