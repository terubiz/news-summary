package com.example.news_summary.user.property

import com.example.news_summary.user.infrastructure.security.RateLimitFilter
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

/**
 * Feature: economic-news-ai-summarizer, Property 9: レート制限の一貫性
 *
 * 同一IPから1分間に60回を超えるリクエストが来た場合、
 * 超過分はすべて429 Too Many Requests を返すことを検証する。
 */
class RateLimitPropertyTest {

    @Property(tries = 50)
    fun `60回超のリクエストは429を返す`(
        @ForAll @IntRange(min = 61, max = 100) requestCount: Int
    ) {
        // Arrange: 各プロパティ試行で独立したフィルタインスタンスを使用
        val filter = RateLimitFilter(requestsPerMinute = 60)
        val clientIp = "192.168.1.1"
        val statusCodes = mutableListOf<Int>()

        // Act: requestCount 回リクエストを送信
        repeat(requestCount) {
            val request = MockHttpServletRequest().apply {
                remoteAddr = clientIp
            }
            val response = MockHttpServletResponse()
            filter.doFilter(request, response) { _, _ -> /* no-op */ }
            statusCodes.add(response.status)
        }

        // Assert: 最初の60回は200（フィルタ通過）、それ以降は429
        val passedRequests = statusCodes.take(60)
        val blockedRequests = statusCodes.drop(60)

        assertThat(passedRequests).allMatch { it == HttpStatus.OK.value() }
        assertThat(blockedRequests).allMatch { it == HttpStatus.TOO_MANY_REQUESTS.value() }
        assertThat(blockedRequests).hasSize(requestCount - 60)
    }

    @Property(tries = 50)
    fun `異なるIPは独立してカウントされる`(
        @ForAll @IntRange(min = 1, max = 60) requestsPerIp: Int
    ) {
        // Arrange
        val filter = RateLimitFilter(requestsPerMinute = 60)
        val ips = listOf("10.0.0.1", "10.0.0.2", "10.0.0.3")

        // Act: 各IPから requestsPerIp 回リクエスト（合計は60以下/IP）
        ips.forEach { ip ->
            repeat(requestsPerIp) {
                val request = MockHttpServletRequest().apply { remoteAddr = ip }
                val response = MockHttpServletResponse()
                filter.doFilter(request, response) { _, _ -> }
                // 60回以内なのでブロックされないはず
                assertThat(response.status).isEqualTo(HttpStatus.OK.value())
            }
        }
    }
}
