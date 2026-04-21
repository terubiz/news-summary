package com.example.news_summary.domain.notification.repository

import com.example.news_summary.domain.notification.model.DeliveryLog
import com.example.news_summary.domain.notification.model.DeliveryLogId
import java.util.Optional

/**
 * 送信ログリポジトリ（ドメイン層ポート）
 * ドメインモデルのみを扱う。JPA依存なし。
 */
interface DeliveryLogRepository {
    fun findById(id: DeliveryLogId): Optional<DeliveryLog>
    fun findByChannelIdAndSummaryId(channelId: Long, summaryId: Long): Optional<DeliveryLog>
    fun findByChannelIdOrderBySentAtDesc(channelId: Long): List<DeliveryLog>

    /** リトライ対象（FAILED かつ retryCount < 3）を取得 */
    fun findRetryTargets(): List<DeliveryLog>

    fun save(channelId: Long, summaryId: Long, status: String, retryCount: Int, errorMessage: String?): DeliveryLog
}
