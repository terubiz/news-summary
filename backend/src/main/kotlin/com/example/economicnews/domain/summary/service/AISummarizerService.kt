package com.example.economicnews.domain.summary.service

import com.example.economicnews.domain.index.model.IndexData
import com.example.economicnews.domain.news.model.NewsArticle
import com.example.economicnews.domain.settings.model.SummarySettings
import com.example.economicnews.domain.summary.model.Summary

/**
 * AI要約生成ドメインサービス
 * LLM APIを使ったニュース要約・株価影響分析を担う
 */
interface AISummarizerService {
    /**
     * ニュース記事を要約する
     * @param articles 要約対象の記事リスト（複数の場合は統合要約）
     * @param indices 参照する株価指数データ
     * @param settings 要約設定（補足レベル・文字数モード・分析観点）
     * @param userId 要約を生成するユーザーID
     */
    fun summarize(
        articles: List<NewsArticle>,
        indices: List<IndexData>,
        settings: SummarySettings,
        userId: Long
    ): Summary

    /**
     * 失敗した要約を再試行する（最大3回）
     */
    fun retryFailedSummaries()
}
