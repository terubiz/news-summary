package com.example.news_summary.domain.summary.model

import com.example.news_summary.domain.news.model.NewsArticle
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "summaries",
    indexes = [
        Index(name = "idx_summaries_user_id", columnList = "user_id"),
        Index(name = "idx_summaries_status", columnList = "status"),
        Index(name = "idx_summaries_generated_at", columnList = "generated_at"),
        Index(name = "idx_summaries_user_generated", columnList = "user_id, generated_at")
    ]
)
data class Summary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "summary_text", nullable = false, columnDefinition = "TEXT")
    val summaryText: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "supplement_level", nullable = false, length = 50)
    val supplementLevel: SupplementLevel,

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_mode", nullable = false, length = 50)
    val summaryMode: SummaryMode,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val status: SummaryStatus = SummaryStatus.PENDING,

    @Column(name = "retry_count", nullable = false)
    val retryCount: Int = 0,

    @CreationTimestamp
    @Column(name = "generated_at", nullable = false, updatable = false)
    val generatedAt: Instant? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "summary_source_articles",
        joinColumns = [JoinColumn(name = "summary_id")],
        inverseJoinColumns = [JoinColumn(name = "article_id")]
    )
    val sourceArticles: MutableSet<NewsArticle> = mutableSetOf()
)
