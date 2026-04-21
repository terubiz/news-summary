package com.example.news_summary.domain.shared.event

import com.example.news_summary.domain.news.model.NewsArticleId
import com.example.news_summary.domain.user.model.UserId

/**
 * ニュース収集完了ドメインイベント。
 * 収集完了後にAI要約処理をトリガーするために発行される。
 */
data class NewsCollectedEvent(
    val userId: UserId,
    val articleIds: List<NewsArticleId>
)
