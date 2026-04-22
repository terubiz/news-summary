package com.example.news_summary.user.application.usecase

import com.example.news_summary.domain.user.model.User
import com.example.news_summary.domain.user.repository.UserRepository
import com.example.news_summary.domain.user.service.PasswordService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class RegisterUserCommand(val email: String, val rawPassword: String)

@Service
class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val passwordService: PasswordService
) {
    @Transactional
    fun execute(command: RegisterUserCommand): User {
        if (userRepository.existsByEmail(command.email)) {
            throw IllegalArgumentException("このメールアドレスは既に使用されています")
        }
        return userRepository.save(
            email = command.email,
            passwordHash = passwordService.hash(command.rawPassword)
        )
    }
}
