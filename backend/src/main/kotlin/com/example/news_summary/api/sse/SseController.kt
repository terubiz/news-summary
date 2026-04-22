package com.example.news_summary.api.sse

import com.example.news_summary.shared.infrastructure.sse.SsePublisherImpl
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * SSEリアルタイム更新エンドポイント。
 * クライアントはこのエンドポイントに接続し、新しい要約が生成されるとイベントを受信する。
 */
@RestController
@RequestMapping("/api/v1/summaries")
class SseController(
    private val ssePublisher: SsePublisherImpl
) {

    /**
     * SSEストリームエンドポイント。
     * クライアントはこのエンドポイントに接続し、要約生成イベントをリアルタイムで受信する。
     */
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamSummaries(): SseEmitter {
        return ssePublisher.subscribe()
    }
}
