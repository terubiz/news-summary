package com.example.news_summary.domain.notification.repository

import com.example.news_summary.domain.notification.model.DeliveryChannel
import com.example.news_summary.domain.notification.model.DeliveryChannelId
import java.util.Optional

/**
 * 通知チャンネルリポジトリ（ドメイン層ポート）
 * ドメインモデルのみを扱う。JPA依存なし。
 */
interface DeliveryChannelRepository {
    fun findById(id: DeliveryChannelId): Optional<DeliveryChannel>
    fun findByUserIdAndEnabledTrue(userId: Long): List<DeliveryChannel>
    fun findByUserId(userId: Long): List<DeliveryChannel>
    fun save(channel: DeliveryChannel): DeliveryChannel
}
