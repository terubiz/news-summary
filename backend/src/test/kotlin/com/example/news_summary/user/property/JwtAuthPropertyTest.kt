package com.example.news_summary.user.property

import com.example.news_summary.user.infrastructure.security.JwtServiceImpl
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.StringLength
import org.assertj.core.api.Assertions.assertThat
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import com.example.news_summary.user.infrastructure.security.JwtAuthenticationFilter

/**
 * Feature: economic-news-ai-summarizer, Property 8: JWT認証の排他性
 *
 * 任意の無効なJWTトークン文字列に対して、JwtAuthenticationFilter が
 * SecurityContext に認証情報を設定しないことを検証する。
 * （保護エンドポイントへの401返却はSecurityConfigが担うため、
 *   フィルタ単体では「認証情報が設定されないこと」を検証する）
 */
class JwtAuthPropertyTest {

    private val jwtService = JwtServiceImpl(
        secret = "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256",
        accessTokenExpiration = 3_600_000L
    )
    private val filter = JwtAuthenticationFilter(jwtService)

    @Property(tries = 100)
    fun `無効なトークンではSecurityContextに認証情報が設定されない`(
        @ForAll @StringLength(min = 1, max = 500) invalidToken: String
    ) {
        // Arrange
        SecurityContextHolder.clearContext()
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer $invalidToken")
        }
        val response = MockHttpServletResponse()
        var filterChainCalled = false

        // Act
        filter.doFilter(request, response) { _, _ -> filterChainCalled = true }

        // Assert: フィルタチェーンは継続されるが認証情報は設定されない
        assertThat(filterChainCalled).isTrue()
        assertThat(SecurityContextHolder.getContext().authentication).isNull()

        SecurityContextHolder.clearContext()
    }

    @Property(tries = 100)
    fun `Authorizationヘッダーなしではフィルタチェーンが継続される`(
        @ForAll @StringLength(min = 1, max = 100) path: String
    ) {
        // Arrange
        SecurityContextHolder.clearContext()
        val request = MockHttpServletRequest().apply {
            requestURI = "/$path"
            // Authorization ヘッダーなし
        }
        val response = MockHttpServletResponse()
        var filterChainCalled = false

        // Act
        filter.doFilter(request, response) { _, _ -> filterChainCalled = true }

        // Assert
        assertThat(filterChainCalled).isTrue()
        assertThat(SecurityContextHolder.getContext().authentication).isNull()

        SecurityContextHolder.clearContext()
    }
}
