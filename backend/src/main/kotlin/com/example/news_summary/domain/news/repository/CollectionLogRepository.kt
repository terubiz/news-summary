package com.example.news_summary.domain.news.repository

import com.example.news_summary.domain.news.model.CollectionLog
import com.example.news_summary.domain.user.model.UserId

interface CollectionLogRepository {
    fun findByUserIdOrderByExecutedAtDesc(userId: UserId): List<CollectionLog>
    fun save(log: CollectionLog): CollectionLog
}
