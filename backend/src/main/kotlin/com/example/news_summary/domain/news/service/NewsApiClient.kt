package com.example.news_summary.domain.news.service

/**
 * ニュースAPI クライアント（ドメイン層ポート）
 * 外部ニュースAPIへの依存をドメインから分離する。
 */
interface NewsApiClient {
    /**
     * 経済ニュースを検索・取得する
     * @param query 検索キーワード（例: "economy", "stock market"）
     * @return 取得した記事のリスト。接続失敗時は空リストを返す（例外を投げない）
     */
    fun fetchLatestNews(query: String = "economy"): List<RawNewsArticle>
}

/** 外部APIから取得した生の記事データ */
data class RawNewsArticle(
    val title: String,
    val content: String,
    val sourceUrl: String,
    val sourceName: String,
    val publishedAt: String  // ISO 8601 形式
)
