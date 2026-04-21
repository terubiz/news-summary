package com.example.news_summary.domain.news.repository

import com.example.news_summary.domain.news.model.CollectionLog
import com.example.news_summary.domain.news.model.CollectionLogId
import java.time.Instant
import java.util.Optional

/**
 * 収集ログリポジトリ（ドメイン層ポート）
 * ドメインモデルのみを扱う。JPA依存なし。
 */
interface CollectionLogRepository {
    fun findById(id: CollectionLogId): Optional<CollectionLog>
    fun findByUserIdOrderByExecutedAtDesc(userId: Long): List<CollectionLog>
    fun findByUserIdAndExecutedAtAfter(userId: Long, after: Instant): List<CollectionLog>
    fun save(userId: Long, articleCount: Int, status: String, errorMessage: String?): CollectionLog
}
