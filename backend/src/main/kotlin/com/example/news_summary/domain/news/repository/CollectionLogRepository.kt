package com.example.news_summary.domain.news.repository

import com.example.news_summary.domain.news.model.CollectionLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface CollectionLogRepository : JpaRepository<CollectionLog, Long> {
    fun findByUserIdOrderByExecutedAtDesc(userId: Long): List<CollectionLog>
    fun findByUserIdAndExecutedAtAfter(userId: Long, after: Instant): List<CollectionLog>
}
