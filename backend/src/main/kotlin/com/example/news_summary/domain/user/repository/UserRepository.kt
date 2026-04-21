package com.example.news_summary.domain.user.repository

import com.example.news_summary.domain.user.model.User
import com.example.news_summary.domain.user.model.UserId
import java.util.Optional

/**
 * ユーザーリポジトリ（ドメイン層ポート）
 * ドメインモデルのみを扱う。JPA依存なし。
 */
interface UserRepository {
    fun findById(id: UserId): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun save(email: String, passwordHash: String): User
}
