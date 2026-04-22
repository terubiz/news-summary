package com.example.news_summary.domain.index.repository

import com.example.news_summary.domain.index.model.IndexData

interface IndexDataRepository {
    fun findLatestBySymbol(symbol: String): IndexData?
    fun findLatestBySymbols(symbols: List<String>): List<IndexData>
    fun save(indexData: IndexData): IndexData
}
