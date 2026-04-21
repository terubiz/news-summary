package com.example.news_summary.domain.news.repository

import com.example.news_summary.domain.news.model.NewsArticle
import com.example.news_summary.domain.news.model.NewsArticleId
import java.time.Instant
import java.util.Optional

/**
 * ニュース記事リポジトリ（ドメイン層ポート）
 * ドメインモデルのみを扱う。JPA依存なし。
 */
interface NewsArticleRepository {
    fun findById(id: NewsArticleId): Optional<NewsArticle>
    fun existsBySourceUrl(sourceUrl: String): Boolean
    fun existsByTitle(title: String): Boolean
    fun findByCollectedAtAfter(after: Instant): List<NewsArticle>
    fun save(article: NewsArticle): NewsArticle
    fun saveNew(title: String, content: String, sourceUrl: String, sourceName: String, publishedAt: Instant): NewsArticle
}
