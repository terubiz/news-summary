package com.example.news_summary.api.auth

import com.example.news_summary.user.application.usecase.AuthenticateUserUseCase
import com.example.news_summary.user.application.usecase.LoginCommand
import com.example.news_summary.user.application.usecase.RegisterUserCommand
import com.example.news_summary.user.application.usecase.RegisterUserUseCase
import com.example.news_summary.user.infrastructure.security.LoginAttemptService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// ---- リクエスト/レスポンス DTO ----

data class RegisterRequest(
    @field:Email(message = "有効なメールアドレスを入力してください")
    @field:NotBlank
    val email: String,

    @field:Size(min = 8, message = "パスワードは8文字以上で入力してください")
    @field:NotBlank
    val password: String
)

data class LoginRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class RefreshRequest(
    @field:NotBlank val refreshToken: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)

data class RegisterResponse(val message: String)

// ---- コントローラ ----

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val authenticateUserUseCase: AuthenticateUserUseCase,
    private val loginAttemptService: LoginAttemptService
) {

    /** ユーザー登録（要件 7.1） */
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        registerUserUseCase.execute(RegisterUserCommand(request.email, request.password))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(RegisterResponse("アカウントを作成しました"))
    }

    /** ログイン・JWTトークン発行（要件 7.2, 7.3, 7.7） */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AuthResponse> {
        val ip = resolveClientIp(httpRequest)

        // IPブロックチェック（要件 7.7）
        if (loginAttemptService.isBlocked(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build()
        }

        return try {
            val tokens = authenticateUserUseCase.login(LoginCommand(request.email, request.password))
            loginAttemptService.recordSuccess(ip)
            ResponseEntity.ok(AuthResponse(tokens.accessToken, tokens.refreshToken))
        } catch (e: IllegalArgumentException) {
            loginAttemptService.recordFailure(ip)
            // アカウント情報を開示しない（要件 7.3）
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }

    /** リフレッシュトークンによるアクセストークン更新（要件 7.6） */
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<AuthResponse> {
        val tokens = authenticateUserUseCase.refresh(request.refreshToken)
        return ResponseEntity.ok(AuthResponse(tokens.accessToken, tokens.refreshToken))
    }

    private fun resolveClientIp(request: HttpServletRequest): String {
        val forwarded = request.getHeader("X-Forwarded-For")
        return forwarded?.split(",")?.firstOrNull()?.trim() ?: request.remoteAddr
    }
}
