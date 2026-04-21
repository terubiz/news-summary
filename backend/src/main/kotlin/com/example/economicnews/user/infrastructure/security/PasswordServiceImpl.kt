package com.example.economicnews.user.infrastructure.security

import com.example.economicnews.domain.user.service.PasswordService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class PasswordServiceImpl : PasswordService {

    private val encoder = BCryptPasswordEncoder()

    override fun hash(rawPassword: String): String = encoder.encode(rawPassword)

    override fun verify(rawPassword: String, hashedPassword: String): Boolean =
        encoder.matches(rawPassword, hashedPassword)
}
