package com.example.news_summary.user.infrastructure.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * ログイン失敗カウンタ・IPブロック管理
 * 同一IPから5回連続失敗で15分間ブロック（要件 7.7）
 */
@Service
class LoginAttemptService(
    @Value("\${app.rate-limit.login-failure-threshold:5}") private val failureThreshold: Int,
    @Value("\${app.rate-limit.login-block-duration-minutes:15}") private val blockDurationMinutes: Long
) {
    private data class AttemptRecord(val count: Int, val blockedUntil: Instant?)

    private val attempts = ConcurrentHashMap<String, AttemptRecord>()

    fun isBlocked(ip: String): Boolean {
        val record = attempts[ip] ?: return false
        val blockedUntil = record.blockedUntil ?: return false
        return if (Instant.now().isBefore(blockedUntil)) {
            true
        } else {
            attempts.remove(ip)
            false
        }
    }

    fun recordFailure(ip: String) {
        val current = attempts[ip] ?: AttemptRecord(0, null)
        val newCount = current.count + 1
        val blockedUntil = if (newCount >= failureThreshold) {
            Instant.now().plusSeconds(blockDurationMinutes * 60)
        } else null
        attempts[ip] = AttemptRecord(newCount, blockedUntil)
    }

    fun recordSuccess(ip: String) {
        attempts.remove(ip)
    }
}
