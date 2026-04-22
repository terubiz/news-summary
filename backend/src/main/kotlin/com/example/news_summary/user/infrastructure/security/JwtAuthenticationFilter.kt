package com.example.news_summary.user.infrastructure.security

import com.example.news_summary.domain.user.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Authorization ヘッダーまたはクエリパラメータからトークンを取得
        // SSE (EventSource) は Authorization ヘッダーを送れないため、?token= で渡す
        val token = extractToken(request)

        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            if (jwtService.isTokenValid(token)) {
                val userId = jwtService.extractUserId(token)
                val email = jwtService.extractEmail(token)
                val auth = UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_USER"))
                ).apply {
                    details = email
                }
                SecurityContextHolder.getContext().authentication = auth
            }
        } catch (e: Exception) {
            logger.debug("JWT validation failed: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        // 1. Authorization ヘッダーから取得
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.removePrefix("Bearer ")
        }
        // 2. クエリパラメータから取得（SSE用）
        return request.getParameter("token")
    }
}
