package com.example.news_summary.domain.notification.repository

import com.example.news_summary.domain.notification.model.DeliveryLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface DeliveryLogRepository : JpaRepository<DeliveryLog, Long> {
    fun findByChannelIdAndSummaryId(channelId: Long, summaryId: Long): Optional<DeliveryLog>
    fun findByChannelIdOrderBySentAtDesc(channelId: Long): List<DeliveryLog>

    /** リトライ対象（FAILED かつ retryCount < 3）を取得 */
    @Query("SELECT d FROM DeliveryLog d WHERE d.status = 'FAILED' AND d.retryCount < 3")
    fun findRetryTargets(): List<DeliveryLog>
}
