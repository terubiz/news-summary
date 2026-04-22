package com.example.news_summary.domain.shared.service

import com.example.news_summary.domain.summary.model.Summary

/**
 * SSEイベント発行サービス（ドメイン層ポート）
 * 要約生成完了時にリアルタイムでクライアントへ通知する。
 */
interface SsePublisher {
    /** 新しい要約が生成されたことをSSEで通知する */
    fun publishSummaryCreated(summary: Summary)
}
