package com.example.news_summary.domain.news.model

import com.example.news_summary.domain.user.model.UserId
import java.time.Instant

data class CollectionLog(
    val id: CollectionLogId? = null,
    val userId: UserId,
    val articleCount: Int = 0,
    val status: String,
    val errorMessage: String? = null,
    val executedAt: Instant? = null
)
