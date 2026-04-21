package com.example.news_summary.user.infrastructure.persistence

import com.example.news_summary.domain.user.model.RefreshToken
import com.example.news_summary.domain.user.model.UserId
import com.example.news_summary.domain.user.repository.RefreshTokenRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Component
class RefreshTokenRepositoryImpl(
    private val jpaRepository: RefreshTokenJpaRepository
) : RefreshTokenRepository {

    override fun findByTokenHash(tokenHash: String): Optional<RefreshToken> =
        jpaRepository.findByTokenHash(tokenHash).map { it.toDomain() }

    override fun save(token: RefreshToken): RefreshToken {
        val entity = RefreshTokenJpaEntity(
            id = token.id,
            userId = token.userId.value,
            tokenHash = token.tokenHash,
            expiresAt = token.expiresAt
        )
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun delete(token: RefreshToken) {
        token.id?.let { jpaRepository.deleteById(it) }
    }

    @Transactional
    override fun deleteAllByUserId(userId: UserId) {
        jpaRepository.deleteAllByUserId(userId.value)
    }

    private fun RefreshTokenJpaEntity.toDomain(): RefreshToken = RefreshToken(
        id = id,
        userId = UserId(userId),
        tokenHash = tokenHash,
        expiresAt = expiresAt,
        createdAt = createdAt
    )
}
