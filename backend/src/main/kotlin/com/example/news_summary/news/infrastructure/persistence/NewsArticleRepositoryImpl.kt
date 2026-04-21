package com.example.news_summary.news.infrastructure.persistence

import com.example.news_summary.domain.news.model.NewsArticle
import com.example.news_summary.domain.news.model.NewsArticleId
import com.example.news_summary.domain.news.repository.NewsArticleRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class NewsArticleRepositoryImpl(
    private val jpaRepository: NewsArticleJpaRepository
) : NewsArticleRepository {

    override fun findById(id: NewsArticleId): NewsArticle? =
        jpaRepository.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findByIds(ids: List<NewsArticleId>): List<NewsArticle> =
        jpaRepository.findAllById(ids.map { it.value }).map { it.toDomain() }

    override fun existsBySourceUrl(sourceUrl: String): Boolean =
        jpaRepository.existsBySourceUrl(sourceUrl)

    override fun existsByTitle(title: String): Boolean =
        jpaRepository.existsByTitle(title)

    override fun findByCollectedAtAfter(after: Instant): List<NewsArticle> =
        jpaRepository.findByCollectedAtAfter(after).map { it.toDomain() }

    override fun save(article: NewsArticle): NewsArticle {
        val entity = NewsArticleJpaEntity(
            id = article.id?.value,
            title = article.title,
            content = article.content,
            sourceUrl = article.sourceUrl,
            sourceName = article.sourceName,
            publishedAt = article.publishedAt,
            collectedAt = article.collectedAt
        )
        return jpaRepository.save(entity).toDomain()
    }

    private fun NewsArticleJpaEntity.toDomain(): NewsArticle = NewsArticle(
        id = NewsArticleId(id ?: throw IllegalStateException("永続化済みNewsArticleのIDがnullです")),
        title = title,
        content = content,
        sourceUrl = sourceUrl,
        sourceName = sourceName,
        publishedAt = publishedAt,
        collectedAt = collectedAt ?: throw IllegalStateException("永続化済みNewsArticleのcollectedAtがnullです")
    )
}
