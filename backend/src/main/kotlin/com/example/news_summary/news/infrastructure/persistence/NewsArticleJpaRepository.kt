package com.example.news_summary.news.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

/** Spring Data JPA リポジトリ（インフラ層）。ドメイン層からは直接参照しない。 */
@Repository
interface NewsArticleJpaRepository : JpaRepository<NewsArticleJpaEntity, Long> {
    fun existsBySourceUrl(sourceUrl: String): Boolean
    fun existsByTitle(title: String): Boolean
    fun findByCollectedAtAfter(after: Instant): List<NewsArticleJpaEntity>
}
