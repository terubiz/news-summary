package com.example.economicnews.domain.news.model

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "news_articles",
    indexes = [
        Index(name = "idx_news_articles_source_url", columnList = "source_url"),
        Index(name = "idx_news_articles_title", columnList = "title"),
        Index(name = "idx_news_articles_collected_at", columnList = "collected_at"),
        Index(name = "idx_news_articles_published_at", columnList = "published_at")
    ]
)
data class NewsArticle(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 500)
    val title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(name = "source_url", nullable = false, unique = true, length = 2048)
    val sourceUrl: String,

    @Column(name = "source_name", nullable = false, length = 255)
    val sourceName: String,

    @Column(name = "published_at", nullable = false)
    val publishedAt: Instant,

    @CreationTimestamp
    @Column(name = "collected_at", nullable = false, updatable = false)
    val collectedAt: Instant? = null
)
