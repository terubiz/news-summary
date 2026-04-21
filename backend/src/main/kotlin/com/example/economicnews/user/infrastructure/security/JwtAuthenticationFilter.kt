package com.example.economicnews.user.infrastructure.security

import com.example.economicnews.domain.user.service.JwtService
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
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.removePrefix("Bearer ")

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
            // 無効なトークンは無視してフィルタチェーンを継続
            logger.debug("JWT validation failed: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }
}
