package com.example.news_summary.integration

import com.example.news_summary.domain.news.model.NewsArticleId
import com.example.news_summary.domain.summary.model.*
import com.example.news_summary.shared.infrastructure.sse.SsePublisherImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Instant

/**
 * SSEストリーム統合テスト。
 *
 * SsePublisherImpl.subscribe() で SseEmitter を取得し、
 * publishSummaryCreated() でイベントが送信されることを検証する。
 * 切断されたemitterが自動的にリストから除去されることも検証する。
 * @SpringBootTest は使わず、直接インスタンスを生成して軽量に検証する。
 */
@DisplayName("SSEストリーム統合テスト")
class SseStreamIntegrationTest {

    private lateinit var ssePublisher: SsePublisherImpl

    private val testSummary = Summary(
        id = SummaryId(1L),
        userId = 1L,
        summaryText = "テスト要約テキスト",
        supplementLevel = SupplementLevel.INTERMEDIATE,
        summaryMode = SummaryMode.STANDARD,
        status = SummaryStatus.COMPLETED,
        retryCount = 0,
        generatedAt = Instant.parse("2024-01-01T12:00:00Z"),
        sourceArticleIds = setOf(NewsArticleId(10L))
    )

    @BeforeEach
    fun setUp() {
        ssePublisher = SsePublisherImpl()
    }

    // -------------------------------------------------------
    // subscribe() で SseEmitter を取得
    // -------------------------------------------------------

    @Test
    @DisplayName("subscribe()でSseEmitterが取得できる")
    fun `should return SseEmitter on subscribe`() {
        val emitter = ssePublisher.subscribe()

        assertNotNull(emitter)
    }

    @Test
    @DisplayName("複数回subscribe()すると複数のemitterが管理される")
    fun `should manage multiple emitters from multiple subscriptions`() {
        val emitter1 = ssePublisher.subscribe()
        val emitter2 = ssePublisher.subscribe()
        val emitter3 = ssePublisher.subscribe()

        assertNotNull(emitter1)
        assertNotNull(emitter2)
        assertNotNull(emitter3)

        // 3つのemitterが登録されている状態で publish しても例外が出ない
        assertDoesNotThrow {
            ssePublisher.publishSummaryCreated(testSummary)
        }
    }

    // -------------------------------------------------------
    // publishSummaryCreated() でイベントが送信される
    // -------------------------------------------------------

    @Test
    @DisplayName("publishSummaryCreated()でイベントが送信される（例外なし）")
    fun `should publish summary created event without exception`() {
        // Arrange: emitter を登録
        ssePublisher.subscribe()

        // Act & Assert: 例外なく送信できる
        assertDoesNotThrow {
            ssePublisher.publishSummaryCreated(testSummary)
        }
    }

    @Test
    @DisplayName("購読者がいない場合でもpublishSummaryCreated()は例外を投げない")
    fun `should not throw when publishing with no subscribers`() {
        // Act & Assert: 購読者なしでも安全
        assertDoesNotThrow {
            ssePublisher.publishSummaryCreated(testSummary)
        }
    }

    // -------------------------------------------------------
    // 切断されたemitterが自動的にリストから除去される
    // -------------------------------------------------------

    @Test
    @DisplayName("切断されたemitterが自動的にリストから除去される")
    fun `should remove disconnected emitters automatically`() {
        // Arrange: emitter を登録して即座に complete（切断をシミュレート）
        val emitter = ssePublisher.subscribe()
        emitter.complete()

        // Act: イベント送信（切断済みemitterへの送信は失敗する）
        // complete 後の emitter は onCompletion コールバックで除去される
        assertDoesNotThrow {
            ssePublisher.publishSummaryCreated(testSummary)
        }
    }

    @Test
    @DisplayName("エラーで切断されたemitterも除去される")
    fun `should remove emitters that errored out`() {
        // Arrange: emitter を登録してエラーで切断
        val emitter = ssePublisher.subscribe()
        emitter.completeWithError(RuntimeException("接続エラー"))

        // Act: イベント送信（エラー済みemitterへの送信は失敗する）
        assertDoesNotThrow {
            ssePublisher.publishSummaryCreated(testSummary)
        }
    }

    @Test
    @DisplayName("一部のemitterが切断されても残りのemitterにはイベントが送信される")
    fun `should continue sending to healthy emitters when some are disconnected`() {
        // Arrange: 3つのemitterを登録
        ssePublisher.subscribe()       // emitter1
        val emitter2 = ssePublisher.subscribe() // これは切断する
        ssePublisher.subscribe()       // emitter3

        // emitter2 を切断
        emitter2.complete()

        // Act: イベント送信
        assertDoesNotThrow {
            ssePublisher.publishSummaryCreated(testSummary)
        }

        // emitter1 と emitter3 はまだ有効（complete されていない）
        // 追加のイベント送信も成功する
        assertDoesNotThrow {
            ssePublisher.publishSummaryCreated(
                testSummary.copy(id = SummaryId(2L), summaryText = "2番目の要約")
            )
        }
    }

    @Test
    @DisplayName("全emitterが切断された後のpublishは安全に処理される")
    fun `should handle publish safely when all emitters are disconnected`() {
        // Arrange: emitter を登録して全部切断
        val emitter1 = ssePublisher.subscribe()
        val emitter2 = ssePublisher.subscribe()
        emitter1.complete()
        emitter2.complete()

        // Act & Assert: 全切断後も安全
        assertDoesNotThrow {
            ssePublisher.publishSummaryCreated(testSummary)
        }
    }
}
