package com.example.news_summary.domain.news.model

/** ニュース記事ID値オブジェクト。永続化済みエンティティは必ず非nullのIDを持つことを型で保証する */
data class NewsArticleId(val value: Long)
