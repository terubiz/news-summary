package com.example.news_summary.domain.user.model

import java.time.Instant

/**
 * ユーザードメインモデル（集約ルート）
 * JPAアノテーションを持たない純粋なドメインオブジェクト。
 * id: UserId により「永続化済み = IDが確定している」ことを型で保証する。
 */
data class User(
    val id: UserId,
    val email: String,
    val passwordHash: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
