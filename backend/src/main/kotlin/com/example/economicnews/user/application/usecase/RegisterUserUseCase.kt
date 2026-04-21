package com.example.economicnews.user.application.usecase

import com.example.economicnews.domain.user.model.User
import com.example.economicnews.domain.user.repository.UserRepository
import com.example.economicnews.domain.user.service.PasswordService
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
        val user = User(
            email = command.email,
            passwordHash = passwordService.hash(command.rawPassword)
        )
        return userRepository.save(user)
    }
}
