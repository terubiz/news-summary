package com.example.economicnews.domain.news.repository

import com.example.economicnews.domain.news.model.NewsArticle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface NewsArticleRepository : JpaRepository<NewsArticle, Long> {
    fun existsBySourceUrl(sourceUrl: String): Boolean
    fun existsByTitle(title: String): Boolean
    fun findByCollectedAtAfter(after: Instant): List<NewsArticle>
}
