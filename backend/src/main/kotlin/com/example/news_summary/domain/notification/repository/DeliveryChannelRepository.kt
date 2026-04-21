package com.example.news_summary.domain.notification.repository

import com.example.news_summary.domain.notification.model.DeliveryChannel
import com.example.news_summary.domain.notification.model.DeliveryChannelId
import com.example.news_summary.domain.user.model.UserId

/**
 * 通知チャンネルリポジトリ（ドメイン層ポート）
 * ドメインモデルのみを扱う。JPA依存なし。
 */
interface DeliveryChannelRepository {
    fun findById(id: DeliveryChannelId): DeliveryChannel?
    fun findByUserId(userId: UserId): List<DeliveryChannel>
    fun findByUserIdAndEnabledTrue(userId: UserId): List<DeliveryChannel>
    fun save(channel: DeliveryChannel): DeliveryChannel
    fun delete(id: DeliveryChannelId)
}
