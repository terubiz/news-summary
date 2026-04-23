package com.example.news_summary.domain.user.repository

import com.example.news_summary.domain.user.model.NewRefreshToken
import com.example.news_summary.domain.user.model.RefreshToken
import com.example.news_summary.domain.user.model.UserId
import java.util.Optional

/**
 * リフレッシュトークンリポジトリ（ドメイン層ポート）
 * ドメインモデルのみを扱う。JPA依存なし。
 */
interface RefreshTokenRepository {
    fun findByTokenHash(tokenHash: String): Optional<RefreshToken>
    fun save(token: NewRefreshToken): RefreshToken
    fun delete(token: RefreshToken)
    fun deleteAllByUserId(userId: UserId)
}
