package com.example.news_summary.domain.notification.model

/** 通知チャンネルID値オブジェクト。永続化済みエンティティは必ず非nullのIDを持つことを型で保証する */
data class DeliveryChannelId(val value: Long)
