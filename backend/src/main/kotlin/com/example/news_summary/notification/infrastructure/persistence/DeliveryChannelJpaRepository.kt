package com.example.news_summary.notification.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/** Spring Data JPA リポジトリ（インフラ層）。ドメイン層からは直接参照しない。 */
@Repository
interface DeliveryChannelJpaRepository : JpaRepository<DeliveryChannelJpaEntity, Long> {
    fun findByUserIdAndEnabledTrue(userId: Long): List<DeliveryChannelJpaEntity>
    fun findByUserId(userId: Long): List<DeliveryChannelJpaEntity>
}
