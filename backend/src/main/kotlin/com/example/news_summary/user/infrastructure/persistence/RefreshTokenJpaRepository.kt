package com.example.news_summary.user.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

/** Spring Data JPA リポジトリ（インフラ層） */
@Repository
interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenJpaEntity, Long> {
    fun findByTokenHash(tokenHash: String): Optional<RefreshTokenJpaEntity>

    @Modifying
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.userId = :userId")
    fun deleteAllByUserId(userId: Long)
}
