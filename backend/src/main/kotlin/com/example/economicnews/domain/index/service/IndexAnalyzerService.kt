package com.example.economicnews.domain.index.service

import com.example.economicnews.domain.index.model.IndexData

/**
 * 株価指数分析ドメインサービス
 * 外部APIからの指数データ取得・キャッシュ管理を担う
 */
interface IndexAnalyzerService {
    /**
     * 指定シンボルの最新指数データを外部APIから取得する
     * @param symbols 取得対象のシンボルリスト（例: ["N225", "SPX", "IXIC", "DAX"]）
     */
    fun fetchLatestIndices(symbols: List<String>): List<IndexData>

    /**
     * キャッシュから指数データを取得する（API失敗時のフォールバック）
     * @return is_stale=true のデータを含む可能性がある
     */
    fun getCachedIndices(symbols: List<String>): List<IndexData>
}
