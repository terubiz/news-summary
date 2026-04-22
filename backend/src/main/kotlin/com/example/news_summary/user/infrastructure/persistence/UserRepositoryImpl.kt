package com.example.news_summary.user.infrastructure.persistence

import com.example.news_summary.domain.user.model.User
import com.example.news_summary.domain.user.model.UserId
import com.example.news_summary.domain.user.repository.UserRepository
import org.springframework.stereotype.Component
import java.util.Optional

/**
 * UserRepository のインフラ層実装。
 * JpaEntity ↔ ドメインモデルの変換を一元管理する。
 * id の null → UserId 変換はこのクラス内でのみ行われる。
 */
@Component
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository
) : UserRepository {

    override fun findById(id: UserId): Optional<User> =
        jpaRepository.findById(id.value).map { it.toDomain() }

    override fun findByEmail(email: String): Optional<User> =
        jpaRepository.findByEmail(email).map { it.toDomain() }

    override fun existsByEmail(email: String): Boolean =
        jpaRepository.existsByEmail(email)

    override fun save(email: String, passwordHash: String): User {
        val entity = UserJpaEntity(email = email, passwordHash = passwordHash)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    /** JpaEntity → ドメインモデル変換。id の null チェックはここで1箇所だけ行う。 */
    private fun UserJpaEntity.toDomain(): User = User(
        id = UserId(id ?: throw IllegalStateException("永続化済みUserのIDがnullです")),
        email = email,
        passwordHash = passwordHash,
        createdAt = createdAt ?: throw IllegalStateException("永続化済みUserのcreatedAtがnullです"),
        updatedAt = updatedAt ?: throw IllegalStateException("永続化済みUserのupdatedAtがnullです")
    )
}
