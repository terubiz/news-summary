package com.example.news_summary.domain.index.repository

import com.example.news_summary.domain.index.model.IndexData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface IndexDataRepository : JpaRepository<IndexData, Long> {
    /** シンボルの最新データを取得する */
    @Query("SELECT i FROM IndexData i WHERE i.symbol = :symbol ORDER BY i.fetchedAt DESC LIMIT 1")
    fun findLatestBySymbol(symbol: String): Optional<IndexData>

    /** 複数シンボルの最新データを取得する */
    @Query("""
        SELECT i FROM IndexData i
        WHERE i.symbol IN :symbols
        AND i.fetchedAt = (
            SELECT MAX(i2.fetchedAt) FROM IndexData i2 WHERE i2.symbol = i.symbol
        )
    """)
    fun findLatestBySymbols(symbols: List<String>): List<IndexData>
}
