package com.example.news_summary.domain.news.service

import java.time.LocalDate

/**
 * ニュースAPI クライアント（ドメイン層ポート）
 * 外部ニュースAPIへの依存をドメインから分離する。
 */
interface NewsApiClient {
    /**
     * 経済ニュースを検索・取得する
     * @param query 検索キーワード（例: "economy", "stock market"）
     * @param fromDate 記事の取得開始日（この日以降の記事を取得）。nullの場合はAPI側のデフォルト
     * @return 取得した記事のリスト。接続失敗時は空リストを返す（例外を投げない）
     */
    fun fetchLatestNews(query: String = "economy", fromDate: LocalDate? = null): List<RawNewsArticle>
}

/** 外部APIから取得した生の記事データ */
data class RawNewsArticle(
    val title: String,
    val content: String,
    val sourceUrl: String,
    val sourceName: String,
    val publishedAt: String  // ISO 8601 形式
)
