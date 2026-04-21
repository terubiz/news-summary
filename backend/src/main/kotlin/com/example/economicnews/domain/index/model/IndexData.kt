package com.example.economicnews.domain.index.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "index_data",
    indexes = [
        Index(name = "idx_index_data_symbol", columnList = "symbol"),
        Index(name = "idx_index_data_fetched_at", columnList = "fetched_at"),
        Index(name = "idx_index_data_symbol_fetched_at", columnList = "symbol, fetched_at")
    ]
)
data class IndexData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 50)
    val symbol: String,

    @Column(name = "current_value", nullable = false, precision = 18, scale = 4)
    val currentValue: BigDecimal,

    @Column(name = "change_amount", nullable = false, precision = 18, scale = 4)
    val changeAmount: BigDecimal,

    @Column(name = "change_rate", nullable = false, precision = 10, scale = 6)
    val changeRate: BigDecimal,

    @Column(name = "is_stale", nullable = false)
    val isStale: Boolean = false,

    @CreationTimestamp
    @Column(name = "fetched_at", nullable = false, updatable = false)
    val fetchedAt: Instant? = null
)
