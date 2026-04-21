package com.example.news_summary.domain.settings.repository

import com.example.news_summary.domain.settings.model.CollectionSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CollectionScheduleRepository : JpaRepository<CollectionSchedule, Long> {
    fun findByUserId(userId: Long): Optional<CollectionSchedule>
    fun findByEnabledTrue(): List<CollectionSchedule>
}
