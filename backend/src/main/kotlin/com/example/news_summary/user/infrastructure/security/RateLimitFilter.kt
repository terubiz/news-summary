package com.example.news_summary.user.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * APIレート制限フィルタ
 * 同一クライアントから1分間に60回超のリクエストで429を返す（要件 8.5）
 */
@Component
class RateLimitFilter(
    @Value("\${app.rate-limit.requests-per-minute:60}") private val requestsPerMinute: Int
) : OncePerRequestFilter() {

    private data class RateRecord(val count: AtomicInteger, val windowStart: Instant)

    private val records = ConcurrentHashMap<String, RateRecord>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val clientKey = resolveClientKey(request)
        val now = Instant.now()

        val record = records.compute(clientKey) { _, existing ->
            when {
                existing == null -> RateRecord(AtomicInteger(1), now)
                now.isAfter(existing.windowStart.plusSeconds(60)) ->
                    RateRecord(AtomicInteger(1), now)
                else -> existing.also { it.count.incrementAndGet() }
            }
        }!!

        if (record.count.get() > requestsPerMinute) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.writer.write("""{"error":"Too Many Requests","message":"1分間のリクエスト上限を超えました"}""")
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveClientKey(request: HttpServletRequest): String {
        val forwarded = request.getHeader("X-Forwarded-For")
        return forwarded?.split(",")?.firstOrNull()?.trim() ?: request.remoteAddr
    }
}
