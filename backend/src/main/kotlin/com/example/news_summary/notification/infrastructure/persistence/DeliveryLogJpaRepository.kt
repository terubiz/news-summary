package com.example.news_summary.notification.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

/** Spring Data JPA リポジトリ（インフラ層）。ドメイン層からは直接参照しない。 */
@Repository
interface DeliveryLogJpaRepository : JpaRepository<DeliveryLogJpaEntity, Long> {
    fun findByChannelIdAndSummaryId(channelId: Long, summaryId: Long): Optional<DeliveryLogJpaEntity>
    fun findByChannelIdOrderBySentAtDesc(channelId: Long): List<DeliveryLogJpaEntity>

    /** リトライ対象（FAILED かつ retryCount < 3）を取得 */
    @Query("SELECT d FROM DeliveryLogJpaEntity d WHERE d.status = 'FAILED' AND d.retryCount < 3")
    fun findRetryTargets(): List<DeliveryLogJpaEntity>
}
