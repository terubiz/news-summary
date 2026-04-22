package com.example.news_summary.domain.news.repository

import com.example.news_summary.domain.news.model.NewsArticle
import com.example.news_summary.domain.news.model.NewsArticleId
import java.time.Instant

interface NewsArticleRepository {
    fun findById(id: NewsArticleId): NewsArticle?
    fun findByIds(ids: List<NewsArticleId>): List<NewsArticle>
    fun existsBySourceUrl(sourceUrl: String): Boolean
    fun existsByTitle(title: String): Boolean
    fun findByCollectedAtAfter(after: Instant): List<NewsArticle>
    fun save(article: NewsArticle): NewsArticle
}
