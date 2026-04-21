package com.example.economicnews.user.application.usecase

import com.example.economicnews.domain.user.model.RefreshToken
import com.example.economicnews.domain.user.repository.RefreshTokenRepository
import com.example.economicnews.domain.user.repository.UserRepository
import com.example.economicnews.domain.user.service.JwtService
import com.example.economicnews.domain.user.service.PasswordService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant

data class LoginCommand(val email: String, val rawPassword: String)

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)

@Service
class AuthenticateUserUseCase(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val passwordService: PasswordService,
    @Value("\${app.jwt.refresh-token-expiration}") private val refreshTokenExpiration: Long
) {
    @Transactional
    fun login(command: LoginCommand): TokenPair {
        val user = userRepository.findByEmail(command.email)
            .orElseThrow { IllegalArgumentException("メールアドレスまたはパスワードが正しくありません") }

        if (!passwordService.verify(command.rawPassword, user.passwordHash)) {
            throw IllegalArgumentException("メールアドレスまたはパスワードが正しくありません")
        }

        val accessToken = jwtService.generateAccessToken(user.id!!, user.email)
        val rawRefreshToken = jwtService.generateRefreshToken()
        val tokenHash = sha256(rawRefreshToken)

        refreshTokenRepository.save(
            RefreshToken(
                userId = user.id,
                tokenHash = tokenHash,
                expiresAt = Instant.now().plusMillis(refreshTokenExpiration)
            )
        )

        return TokenPair(accessToken = accessToken, refreshToken = rawRefreshToken)
    }

    @Transactional
    fun refresh(rawRefreshToken: String): TokenPair {
        val tokenHash = sha256(rawRefreshToken)
        val stored = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow { IllegalArgumentException("無効なリフレッシュトークンです") }

        if (stored.expiresAt.isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored)
            throw IllegalArgumentException("リフレッシュトークンの有効期限が切れています")
        }

        val user = userRepository.findById(stored.userId)
            .orElseThrow { IllegalStateException("ユーザーが見つかりません") }

        // 旧トークンを削除して新しいトークンを発行（ローテーション）
        refreshTokenRepository.delete(stored)
        val newAccessToken = jwtService.generateAccessToken(user.id!!, user.email)
        val newRawRefreshToken = jwtService.generateRefreshToken()
        val newTokenHash = sha256(newRawRefreshToken)

        refreshTokenRepository.save(
            RefreshToken(
                userId = user.id,
                tokenHash = newTokenHash,
                expiresAt = Instant.now().plusMillis(refreshTokenExpiration)
            )
        )

        return TokenPair(accessToken = newAccessToken, refreshToken = newRawRefreshToken)
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
