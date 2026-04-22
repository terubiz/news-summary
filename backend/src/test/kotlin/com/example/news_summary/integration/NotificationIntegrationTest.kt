package com.example.news_summary.integration

import com.example.news_summary.domain.news.model.NewsArticle
import com.example.news_summary.domain.news.model.NewsArticleId
import com.example.news_summary.domain.news.repository.NewsArticleRepository
import com.example.news_summary.domain.notification.model.*
import com.example.news_summary.domain.notification.repository.DeliveryLogRepository
import com.example.news_summary.domain.notification.service.DeliveryResult
import com.example.news_summary.domain.notification.service.NotificationSender
import com.example.news_summary.domain.summary.model.*
import com.example.news_summary.domain.summary.repository.SummaryIndexImpactRepository
import com.example.news_summary.domain.user.model.UserId
import com.example.news_summary.notification.application.usecase.SendNotificationUseCase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.Instant

/**
 * 通知送信統合テスト。
 *
 * NotificationSender をモック（成功・失敗シナリオ）し、
 * sendToChannel() / sendToChannels() 後に DeliveryLog が記録されることを検証する。
 * @SpringBootTest は使わず、モックベースで軽量に検証する。
 */
@DisplayName("通知送信統合テスト")
class NotificationIntegrationTest {

    private lateinit var slackSender: NotificationSender
    private lateinit var emailSender: NotificationSender
    private lateinit var deliveryLogRepository: DeliveryLogRepository
    private lateinit var summaryIndexImpactRepository: SummaryIndexImpactRepository
    private lateinit var newsArticleRepository: NewsArticleRepository
    private lateinit var useCase: SendNotificationUseCase

    private val testSummary = Summary(
        id = SummaryId(1L),
        userId = 1L,
        summaryText = "テスト要約テキスト",
        supplementLevel = SupplementLevel.INTERMEDIATE,
        summaryMode = SummaryMode.STANDARD,
        status = SummaryStatus.COMPLETED,
        retryCount = 0,
        generatedAt = Instant.now(),
        sourceArticleIds = setOf(NewsArticleId(10L))
    )

    private val slackChannel = DeliveryChannel(
        id = DeliveryChannelId(1L),
        userId = UserId(1L),
        channelType = ChannelType.SLACK,
        encryptedConfig = "encrypted-slack-config",
        deliverySchedule = "IMMEDIATE",
        enabled = true
    )

    private val emailChannel = DeliveryChannel(
        id = DeliveryChannelId(2L),
        userId = UserId(1L),
        channelType = ChannelType.EMAIL,
        encryptedConfig = "encrypted-email-config",
        deliverySchedule = "IMMEDIATE",
        enabled = true
    )

    @BeforeEach
    fun setUp() {
        slackSender = mock {
            on { channelType } doReturn ChannelType.SLACK
        }
        emailSender = mock {
            on { channelType } doReturn ChannelType.EMAIL
        }

        deliveryLogRepository = mock()
        summaryIndexImpactRepository = mock()
        newsArticleRepository = mock()

        useCase = SendNotificationUseCase(
            senders = listOf(slackSender, emailSender),
            deliveryLogRepository = deliveryLogRepository,
            summaryIndexImpactRepository = summaryIndexImpactRepository,
            newsArticleRepository = newsArticleRepository
        )

        // デフォルトモック設定
        whenever(summaryIndexImpactRepository.findBySummaryId(any())).thenReturn(emptyList())
        whenever(newsArticleRepository.findById(any())).thenReturn(
            NewsArticle(
                id = NewsArticleId(10L),
                title = "テスト記事",
                content = "テスト内容",
                sourceUrl = "https://example.com/article",
                sourceName = "TestSource",
                publishedAt = Instant.now()
            )
        )

        // DeliveryLog保存のモック
        var logIdCounter = 100L
        whenever(deliveryLogRepository.save(any(), any(), any(), any(), anyOrNull())).thenAnswer { invocation ->
            DeliveryLog(
                id = DeliveryLogId(logIdCounter++),
                channelId = invocation.getArgument(0),
                summaryId = invocation.getArgument(1),
                status = invocation.getArgument(2),
                retryCount = invocation.getArgument(3),
                errorMessage = invocation.getArgument(4),
                sentAt = Instant.now()
            )
        }
    }

    // -------------------------------------------------------
    // 成功シナリオ: sendToChannel() 後に DeliveryLog が記録される
    // -------------------------------------------------------

    @Test
    @DisplayName("sendToChannel()成功時にDeliveryLogがSUCCESSで記録される")
    fun `should record SUCCESS delivery log when sendToChannel succeeds`() {
        // Arrange: Slack送信成功
        doNothing().whenever(slackSender).send(any(), any(), any(), any())

        // Act
        val result = useCase.sendToChannel(testSummary, slackChannel)

        // Assert
        assertTrue(result.success)
        assertEquals(1L, result.channelId)

        // DeliveryLog が SUCCESS で記録された
        verify(deliveryLogRepository).save(
            eq(1L),       // channelId
            eq(1L),       // summaryId
            eq("SUCCESS"),
            eq(0),        // retryCount
            isNull()      // errorMessage
        )
    }

    // -------------------------------------------------------
    // 失敗シナリオ: 送信失敗時に DeliveryLog が FAILED で記録される
    // -------------------------------------------------------

    @Test
    @DisplayName("sendToChannel()失敗時にDeliveryLogがFAILEDで記録される")
    fun `should record FAILED delivery log when sendToChannel fails`() {
        // Arrange: Slack送信失敗
        doThrow(RuntimeException("Slack API接続エラー"))
            .whenever(slackSender).send(any(), any(), any(), any())

        // Act
        val result = useCase.sendToChannel(testSummary, slackChannel)

        // Assert
        assertFalse(result.success)
        assertEquals(1L, result.channelId)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("Slack API接続エラー"))

        // DeliveryLog が FAILED で記録された
        verify(deliveryLogRepository).save(
            eq(1L),
            eq(1L),
            eq("FAILED"),
            eq(0),
            eq("Slack API接続エラー")
        )
    }

    // -------------------------------------------------------
    // 複数チャンネル並行送信: sendToChannels()
    // -------------------------------------------------------

    @Test
    @DisplayName("sendToChannels()で複数チャンネルに並行送信される")
    fun `should send to multiple channels in parallel`() {
        // Arrange: 両方成功
        doNothing().whenever(slackSender).send(any(), any(), any(), any())
        doNothing().whenever(emailSender).send(any(), any(), any(), any())

        // Act
        val results = useCase.sendToChannels(testSummary, listOf(slackChannel, emailChannel))

        // Assert: 2件とも成功
        assertEquals(2, results.size)
        assertTrue(results.all { it.success })

        // 各チャンネルの DeliveryLog が記録された
        verify(deliveryLogRepository).save(eq(1L), eq(1L), eq("SUCCESS"), eq(0), isNull())
        verify(deliveryLogRepository).save(eq(2L), eq(1L), eq("SUCCESS"), eq(0), isNull())
    }

    @Test
    @DisplayName("sendToChannels()で一部失敗しても他のチャンネルは送信される")
    fun `should continue sending to other channels when one fails`() {
        // Arrange: Slack失敗、Email成功
        doThrow(RuntimeException("Slack障害"))
            .whenever(slackSender).send(any(), any(), any(), any())
        doNothing().whenever(emailSender).send(any(), any(), any(), any())

        // Act
        val results = useCase.sendToChannels(testSummary, listOf(slackChannel, emailChannel))

        // Assert: Slack失敗、Email成功
        assertEquals(2, results.size)
        val slackResult = results.find { it.channelId == 1L }
        val emailResult = results.find { it.channelId == 2L }

        assertNotNull(slackResult)
        assertFalse(slackResult!!.success)

        assertNotNull(emailResult)
        assertTrue(emailResult!!.success)

        // 両方の DeliveryLog が記録された
        verify(deliveryLogRepository).save(eq(1L), eq(1L), eq("FAILED"), eq(0), eq("Slack障害"))
        verify(deliveryLogRepository).save(eq(2L), eq(1L), eq("SUCCESS"), eq(0), isNull())
    }

    // -------------------------------------------------------
    // リトライ: retryFailedDeliveries()
    // -------------------------------------------------------

    @Test
    @DisplayName("retryFailedDeliveries()で失敗した通知がretryCount++で再試行される")
    fun `should retry failed deliveries with incremented retryCount`() {
        // Arrange: リトライ対象のログ
        val failedLog = DeliveryLog(
            id = DeliveryLogId(50L),
            channelId = 1L,
            summaryId = 1L,
            status = "FAILED",
            retryCount = 0,
            errorMessage = "前回の失敗",
            sentAt = Instant.now()
        )
        whenever(deliveryLogRepository.findRetryTargets()).thenReturn(listOf(failedLog))

        // Act
        useCase.retryFailedDeliveries()

        // Assert: retryCount=1 で SUCCESS として保存される
        verify(deliveryLogRepository).save(
            eq(1L),
            eq(1L),
            eq("SUCCESS"),
            eq(1),        // retryCount が +1
            isNull()
        )
    }

    @Test
    @DisplayName("リトライ対象がない場合は何もしない")
    fun `should do nothing when no retry targets exist`() {
        whenever(deliveryLogRepository.findRetryTargets()).thenReturn(emptyList())

        useCase.retryFailedDeliveries()

        // save は呼ばれない（findRetryTargets以外）
        verify(deliveryLogRepository, never()).save(any(), any(), any(), any(), anyOrNull())
    }

    @Test
    @DisplayName("未対応のチャンネル種別の場合はFAILEDで記録される")
    fun `should record FAILED when channel type is not supported`() {
        // Arrange: LINE チャンネル（送信アダプタ未登録）
        val lineChannel = DeliveryChannel(
            id = DeliveryChannelId(3L),
            userId = UserId(1L),
            channelType = ChannelType.LINE,
            encryptedConfig = "encrypted-line-config",
            deliverySchedule = "IMMEDIATE",
            enabled = true
        )

        // Act
        val result = useCase.sendToChannel(testSummary, lineChannel)

        // Assert
        assertFalse(result.success)
        assertTrue(result.errorMessage!!.contains("未対応"))

        // FAILED で記録
        verify(deliveryLogRepository).save(
            eq(3L),
            eq(1L),
            eq("FAILED"),
            eq(0),
            argThat<String> { this.contains("未対応") }
        )
    }
}
