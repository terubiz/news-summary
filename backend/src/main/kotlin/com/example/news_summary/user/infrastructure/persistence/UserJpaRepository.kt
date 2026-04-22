package com.example.news_summary.user.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

/** Spring Data JPA リポジトリ（インフラ層）。ドメイン層からは直接参照しない。 */
@Repository
interface UserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    fun findByEmail(email: String): Optional<UserJpaEntity>
    fun existsByEmail(email: String): Boolean
}
