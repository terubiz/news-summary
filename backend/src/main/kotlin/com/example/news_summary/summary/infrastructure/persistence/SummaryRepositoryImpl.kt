package com.example.news_summary.summary.infrastructure.persistence

import com.example.news_summary.domain.news.model.NewsArticleId
import com.example.news_summary.domain.summary.model.Summary
import com.example.news_summary.domain.summary.model.SummaryId
import com.example.news_summary.domain.summary.model.SummaryStatus
import com.example.news_summary.domain.summary.repository.PageResult
import com.example.news_summary.domain.summary.repository.SummaryRepository
import com.example.news_summary.news.infrastructure.persistence.NewsArticleJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Optional

/**
 * SummaryRepository のインフラ層実装。
 * JpaEntity ↔ ドメインモデルの変換を一元管理する。
 * id の null → SummaryId 変換はこのクラス内でのみ行われる。
 */
@Component
class SummaryRepositoryImpl(
    private val jpaRepository: SummaryJpaRepository,
    private val articleJpaRepository: NewsArticleJpaRepository
) : SummaryRepository {

    override fun findById(id: SummaryId): Optional<Summary> =
        jpaRepository.findById(id.value).map { it.toDomain() }

    override fun findByUserIdOrderByGeneratedAtDesc(userId: Long, page: Int, size: Int): PageResult<Summary> {
        val pageable = PageRequest.of(page, size)
        val result = jpaRepository.findByUserIdOrderByGeneratedAtDesc(userId, pageable)
        return PageResult(
            content = result.content.map { it.toDomain() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            page = result.number,
            size = result.size
        )
    }

    override fun findByUserIdAndGeneratedAtAfterOrderByGeneratedAtDesc(userId: Long, after: Instant): List<Summary> =
        jpaRepository.findByUserIdAndGeneratedAtAfterOrderByGeneratedAtDesc(userId, after).map { it.toDomain() }

    override fun findByUserIdAndStatusOrderByGeneratedAtDesc(userId: Long, status: SummaryStatus): List<Summary> =
        jpaRepository.findByUserIdAndStatusOrderByGeneratedAtDesc(userId, status).map { it.toDomain() }

    override fun searchByKeyword(userId: Long, keyword: String, page: Int, size: Int): PageResult<Summary> {
        val pageable = PageRequest.of(page, size)
        val result = jpaRepository.searchByKeyword(userId, keyword, pageable)
        return PageResult(
            content = result.content.map { it.toDomain() },
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            page = result.number,
            size = result.size
        )
    }

    override fun findRetryTargets(): List<Summary> =
        jpaRepository.findRetryTargets().map { it.toDomain() }

    override fun save(summary: Summary): Summary {
        val articleEntities = if (summary.sourceArticleIds.isNotEmpty()) {
            articleJpaRepository.findAllById(summary.sourceArticleIds.map { it.value }).toMutableSet()
        } else {
            mutableSetOf()
        }
        val entity = SummaryJpaEntity(
            id = summary.id.value,
            userId = summary.userId,
            summaryText = summary.summaryText,
            supplementLevel = summary.supplementLevel,
            summaryMode = summary.summaryMode,
            status = summary.status,
            retryCount = summary.retryCount,
            generatedAt = summary.generatedAt,
            sourceArticles = articleEntities
        )
        return jpaRepository.save(entity).toDomain()
    }

    /** JpaEntity → ドメインモデル変換。id の null チェックはここで1箇所だけ行う。 */
    private fun SummaryJpaEntity.toDomain(): Summary = Summary(
        id = SummaryId(id ?: throw IllegalStateException("永続化済みSummaryのIDがnullです")),
        userId = userId,
        summaryText = summaryText,
        supplementLevel = supplementLevel,
        summaryMode = summaryMode,
        status = status,
        retryCount = retryCount,
        generatedAt = generatedAt ?: throw IllegalStateException("永続化済みSummaryのgeneratedAtがnullです"),
        sourceArticleIds = sourceArticles.mapNotNull { it.id }.map { NewsArticleId(it) }.toSet()
    )
}
