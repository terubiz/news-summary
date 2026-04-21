package com.example.economicnews.domain.notification.repository

import com.example.economicnews.domain.notification.model.DeliveryChannel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeliveryChannelRepository : JpaRepository<DeliveryChannel, Long> {
    fun findByUserIdAndEnabledTrue(userId: Long): List<DeliveryChannel>
    fun findByUserId(userId: Long): List<DeliveryChannel>
}
