package com.example.economicnews.user.infrastructure.security

import com.example.economicnews.domain.user.service.JwtService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtServiceImpl(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.access-token-expiration}") private val accessTokenExpiration: Long,
) : JwtService {

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    override fun generateAccessToken(userId: Long, email: String): String {
        val now = Date()
        val expiry = Date(now.time + accessTokenExpiration)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact()
    }

    override fun generateRefreshToken(): String {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    override fun extractUserId(token: String): Long =
        parseClaims(token).subject.toLong()

    override fun extractEmail(token: String): String =
        parseClaims(token)["email"] as String

    override fun isTokenValid(token: String): Boolean = try {
        !isTokenExpired(token)
    } catch (e: JwtException) {
        false
    }

    override fun isTokenExpired(token: String): Boolean =
        parseClaims(token).expiration.before(Date())

    private fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
