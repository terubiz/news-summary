package com.example.news_summary.shared.infrastructure.sse

import com.example.news_summary.domain.shared.service.SsePublisher
import com.example.news_summary.domain.summary.model.Summary
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList

/**
 * SSEイベント発行サービスのインフラ層実装。
 * SseEmitter を管理し、要約生成完了時にクライアントへイベントを送信する。
 */
@Component
class SsePublisherImpl : SsePublisher {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    /** 新しいSSE接続を登録する */
    fun subscribe(): SseEmitter {
        val emitter = SseEmitter(0L) // タイムアウトなし
        emitters.add(emitter)

        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }

        logger.info("SSEクライアント接続: 現在${emitters.size}件")
        return emitter
    }

    override fun publishSummaryCreated(summary: Summary) {
        val data = SseSummaryEvent(
            id = summary.id.value,
            summaryText = summary.summaryText,
            status = summary.status.name,
            generatedAt = summary.generatedAt.toString()
        )

        val deadEmitters = mutableListOf<SseEmitter>()
        emitters.forEach { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("summary-created")
                        .data(data)
                )
            } catch (e: Exception) {
                logger.debug("SSE送信失敗（クライアント切断）: ${e.message}")
                deadEmitters.add(emitter)
            }
        }
        emitters.removeAll(deadEmitters.toSet())

        if (emitters.isNotEmpty()) {
            logger.info("SSEイベント送信完了: summaryId=${summary.id.value}, 送信先=${emitters.size}件")
        }
    }

    /** SSEイベントデータ */
    data class SseSummaryEvent(
        val id: Long,
        val summaryText: String,
        val status: String,
        val generatedAt: String
    )
}
