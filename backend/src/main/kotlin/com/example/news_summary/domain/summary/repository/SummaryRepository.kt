package com.example.news_summary.domain.summary.repository

import com.example.news_summary.domain.common.SummaryStatus
import com.example.news_summary.domain.summary.model.Summary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface SummaryRepository : JpaRepository<Summary, Long> {
    fun findByUserIdOrderByGeneratedAtDesc(userId: Long, pageable: Pageable): Page<Summary>

    fun findByUserIdAndGeneratedAtAfterOrderByGeneratedAtDesc(
        userId: Long,
        after: Instant
    ): List<Summary>

    fun findByUserIdAndStatusOrderByGeneratedAtDesc(
        userId: Long,
        status: SummaryStatus
    ): List<Summary>

    /** キーワード検索（要約テキスト内） */
    @Query("SELECT s FROM Summary s WHERE s.userId = :userId AND s.summaryText LIKE %:keyword%")
    fun searchByKeyword(userId: Long, keyword: String, pageable: Pageable): Page<Summary>

    /** リトライ対象（FAILED かつ retryCount < 3）を取得 */
    @Query("SELECT s FROM Summary s WHERE s.status = 'FAILED' AND s.retryCount < 3")
    fun findRetryTargets(): List<Summary>
}
