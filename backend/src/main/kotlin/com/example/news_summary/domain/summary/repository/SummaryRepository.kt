package com.example.news_summary.domain.summary.repository

import com.example.news_summary.domain.summary.model.Summary
import com.example.news_summary.domain.summary.model.SummaryId
import com.example.news_summary.domain.summary.model.SummaryStatus
import java.time.Instant
import java.util.Optional

/**
 * 要約リポジトリ（ドメイン層ポート）
 * ドメインモデルのみを扱う。JPA依存なし。
 */
interface SummaryRepository {
    fun findById(id: SummaryId): Optional<Summary>
    fun findByUserIdOrderByGeneratedAtDesc(userId: Long, page: Int, size: Int): PageResult<Summary>
    fun findByUserIdAndGeneratedAtAfterOrderByGeneratedAtDesc(userId: Long, after: Instant): List<Summary>
    fun findByUserIdAndStatusOrderByGeneratedAtDesc(userId: Long, status: SummaryStatus): List<Summary>

    /** キーワード検索（要約テキスト内） */
    fun searchByKeyword(userId: Long, keyword: String, page: Int, size: Int): PageResult<Summary>

    /** Stock_Indexフィルタ付きページネーション */
    fun findByIndexSymbol(indexSymbol: String, page: Int, size: Int): PageResult<Summary>

    /** 全ユーザー向けページネーション（管理用） */
    fun findAllOrderByGeneratedAtDesc(page: Int, size: Int): PageResult<Summary>

    /** 全ユーザー向けキーワード検索 */
    fun searchAllByKeyword(keyword: String, page: Int, size: Int): PageResult<Summary>

    /** 指定時刻以降に生成された全要約を取得 */
    fun findByGeneratedAtAfter(after: Instant): List<Summary>

    /** リトライ対象（FAILED かつ retryCount < 3）を取得 */
    fun findRetryTargets(): List<Summary>

    fun save(summary: Summary): Summary
}

/** ページネーション結果 */
data class PageResult<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)
