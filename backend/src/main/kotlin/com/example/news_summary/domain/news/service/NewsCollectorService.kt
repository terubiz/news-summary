package com.example.news_summary.domain.news.service

import com.example.news_summary.domain.news.model.NewNewsArticle

/**
 * ニュース収集ドメインサービス
 * 外部ニュースAPIからの記事取得・重複排除・保存を担う
 */
interface NewsCollectorService {
    /**
     * ニュースを収集する
     * @param userId 収集を実行するユーザーID
     * @return 収集結果（保存件数・スキップ件数・エラー情報）
     */
    fun collectNews(userId: Long): CollectionResult

    /**
     * 記事が重複しているか判定する（URL または タイトルの一致）
     */
    fun isDuplicate(article: NewNewsArticle): Boolean
}

data class CollectionResult(
    val savedCount: Int,
    val skippedCount: Int,
    val errorCount: Int,
    val errors: List<String> = emptyList()
)
