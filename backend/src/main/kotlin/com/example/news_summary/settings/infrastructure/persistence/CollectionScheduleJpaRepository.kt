package com.example.news_summary.settings.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

/** Spring Data JPA リポジトリ（インフラ層）。ドメイン層からは直接参照しない。 */
@Repository
interface CollectionScheduleJpaRepository : JpaRepository<CollectionScheduleJpaEntity, Long> {
    fun findByUserId(userId: Long): Optional<CollectionScheduleJpaEntity>
    fun findByEnabledTrue(): List<CollectionScheduleJpaEntity>
}
