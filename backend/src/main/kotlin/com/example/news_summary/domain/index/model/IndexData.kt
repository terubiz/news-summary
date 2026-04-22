package com.example.news_summary.domain.index.model

import java.math.BigDecimal
import java.time.Instant

data class IndexData(
    val id: IndexDataId? = null,
    val symbol: String,
    val currentValue: BigDecimal,
    val changeAmount: BigDecimal,
    val changeRate: BigDecimal,
    val isStale: Boolean = false,
    val fetchedAt: Instant? = null
)
