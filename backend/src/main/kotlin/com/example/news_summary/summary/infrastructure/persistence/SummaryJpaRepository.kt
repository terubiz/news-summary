package com.example.news_summary.summary.infrastructure.persistence

import com.example.news_summary.domain.summary.model.SummaryStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

/** Spring Data JPA リポジトリ（インフラ層）。ドメイン層からは直接参照しない。 */
@Repository
interface SummaryJpaRepository : JpaRepository<SummaryJpaEntity, Long> {
    fun findByUserIdOrderByGeneratedAtDesc(userId: Long, pageable: Pageable): Page<SummaryJpaEntity>

    fun findByUserIdAndGeneratedAtAfterOrderByGeneratedAtDesc(
        userId: Long,
        after: Instant
    ): List<SummaryJpaEntity>

    fun findByUserIdAndStatusOrderByGeneratedAtDesc(
        userId: Long,
        status: SummaryStatus
    ): List<SummaryJpaEntity>

    /** キーワード検索（要約テキスト内） */
    @Query("SELECT s FROM SummaryJpaEntity s WHERE s.userId = :userId AND s.summaryText LIKE %:keyword%")
    fun searchByKeyword(userId: Long, keyword: String, pageable: Pageable): Page<SummaryJpaEntity>

    /** Stock_Indexフィルタ（summary_index_impacts テーブルとJOIN） */
    @Query("""
        SELECT DISTINCT s FROM SummaryJpaEntity s 
        JOIN SummaryIndexImpactJpaEntity si ON si.summaryId = s.id 
        WHERE si.indexSymbol = :indexSymbol 
        ORDER BY s.generatedAt DESC
    """)
    fun findByIndexSymbol(indexSymbol: String, pageable: Pageable): Page<SummaryJpaEntity>

    /** 全ユーザー向けページネーション */
    fun findAllByOrderByGeneratedAtDesc(pageable: Pageable): Page<SummaryJpaEntity>

    /** 全ユーザー向けキーワード検索 */
    @Query("SELECT s FROM SummaryJpaEntity s WHERE s.summaryText LIKE %:keyword% ORDER BY s.generatedAt DESC")
    fun searchAllByKeyword(keyword: String, pageable: Pageable): Page<SummaryJpaEntity>

    /** 指定時刻以降に生成された全要約を取得 */
    fun findByGeneratedAtAfterOrderByGeneratedAtDesc(after: Instant): List<SummaryJpaEntity>

    /** リトライ対象（FAILED かつ retryCount < 3）を取得 */
    @Query("SELECT s FROM SummaryJpaEntity s WHERE s.status = 'FAILED' AND s.retryCount < 3")
    fun findRetryTargets(): List<SummaryJpaEntity>
}
