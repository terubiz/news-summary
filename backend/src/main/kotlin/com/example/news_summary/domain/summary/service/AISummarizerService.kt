package com.example.news_summary.domain.summary.service

import com.example.news_summary.domain.news.model.NewsArticle
import com.example.news_summary.domain.settings.model.SummarySettings
import com.example.news_summary.domain.summary.model.Summary

/**
 * AI要約生成ドメインサービス（ポート）
 *
 * LLMを使ったニュース要約・株価影響分析を担う。
 * 株価指数の値はAI側が検索して取得する（Google Search Grounding等）。
 * アプリ側は指数名のリストのみを渡す。
 *
 * 将来的に別AIプロバイダー（OpenAI, Claude等）に切り替える場合は、
 * このインターフェースの新しい具象クラスを作成する。
 */
interface AISummarizerService {
    /**
     * ニュース記事を要約する
     * @param articles 要約対象の記事リスト（複数の場合は統合要約）
     * @param indexNames 参照する株価指数の名前リスト（例: ["日経225", "S&P500"]）。AI側が最新値を検索する
     * @param settings 要約設定（補足レベル・文字数モード・分析観点）
     * @param userId 要約を生成するユーザーID
     */
    fun summarize(
        articles: List<NewsArticle>,
        indexNames: List<String>,
        settings: SummarySettings,
        userId: Long
    ): Summary

    /**
     * 失敗した要約を再試行する（最大3回）
     */
    fun retryFailedSummaries()
}
