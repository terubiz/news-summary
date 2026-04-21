package com.example.news_summary.domain.notification.repository

import com.example.news_summary.domain.notification.model.DeliveryChannel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeliveryChannelRepository : JpaRepository<DeliveryChannel, Long> {
    fun findByUserIdAndEnabledTrue(userId: Long): List<DeliveryChannel>
    fun findByUserId(userId: Long): List<DeliveryChannel>
}
