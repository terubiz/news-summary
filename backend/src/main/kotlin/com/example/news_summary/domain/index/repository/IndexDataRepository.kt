package com.example.news_summary.domain.index.repository

import com.example.news_summary.domain.index.model.IndexData
import com.example.news_summary.domain.index.model.IndexDataId
import java.util.Optional

/**
 * 株価指数データリポジトリ（ドメイン層ポート）
 * ドメインモデルのみを扱う。JPA依存なし。
 */
interface IndexDataRepository {
    fun findById(id: IndexDataId): Optional<IndexData>

    /** シンボルの最新データを取得する */
    fun findLatestBySymbol(symbol: String): Optional<IndexData>

    /** 複数シンボルの最新データを取得する */
    fun findLatestBySymbols(symbols: List<String>): List<IndexData>

    fun save(indexData: IndexData): IndexData
    fun saveNew(symbol: String, currentValue: java.math.BigDecimal, changeAmount: java.math.BigDecimal, changeRate: java.math.BigDecimal, isStale: Boolean): IndexData
}
