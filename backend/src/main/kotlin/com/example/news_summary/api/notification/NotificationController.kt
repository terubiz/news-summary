package com.example.news_summary.api.notification

import com.example.news_summary.domain.notification.model.ChannelType
import com.example.news_summary.domain.notification.model.DeliveryChannelId
import com.example.news_summary.domain.user.model.UserId
import com.example.news_summary.notification.application.usecase.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

// ---- リクエスト/レスポンス DTO ----

data class CreateChannelRequest(
    @field:NotNull val channelType: ChannelType,
    @field:NotBlank val config: String,
    @field:NotBlank val deliverySchedule: String = "IMMEDIATE",
    val filterIndices: List<String> = emptyList()
)

data class UpdateChannelRequest(
    val config: String?,
    val deliverySchedule: String?,
    val filterIndices: List<String>?,
    val enabled: Boolean?
)

data class TestConnectionResponse(val success: Boolean)

// ---- コントローラ ----

@RestController
@RequestMapping("/api/v1/channels")
class NotificationController(
    private val manageChannelUseCase: ManageChannelUseCase
) {
    /** 送信チャンネル一覧取得（要件6.1） */
    @GetMapping
    fun getChannels(auth: Authentication): ResponseEntity<List<ChannelDto>> {
        val userId = UserId(auth.principal as Long)
        return ResponseEntity.ok(manageChannelUseCase.getChannels(userId))
    }

    /** チャンネル追加（要件6.1, 6.5, 6.6） */
    @PostMapping
    fun createChannel(
        @Valid @RequestBody request: CreateChannelRequest,
        auth: Authentication
    ): ResponseEntity<ChannelDto> {
        val userId = UserId(auth.principal as Long)
        val command = CreateChannelCommand(
            channelType = request.channelType,
            config = request.config,
            deliverySchedule = request.deliverySchedule,
            filterIndices = request.filterIndices
        )
        val created = manageChannelUseCase.createChannel(userId, command)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    /** チャンネル更新（要件6.1） */
    @PutMapping("/{id}")
    fun updateChannel(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateChannelRequest,
        auth: Authentication
    ): ResponseEntity<ChannelDto> {
        val userId = UserId(auth.principal as Long)
        val command = UpdateChannelCommand(
            config = request.config,
            deliverySchedule = request.deliverySchedule,
            filterIndices = request.filterIndices,
            enabled = request.enabled
        )
        val updated = manageChannelUseCase.updateChannel(userId, DeliveryChannelId(id), command)
        return ResponseEntity.ok(updated)
    }

    /** チャンネル削除（要件6.1） */
    @DeleteMapping("/{id}")
    fun deleteChannel(
        @PathVariable id: Long,
        auth: Authentication
    ): ResponseEntity<Void> {
        val userId = UserId(auth.principal as Long)
        manageChannelUseCase.deleteChannel(userId, DeliveryChannelId(id))
        return ResponseEntity.noContent().build()
    }

    /** 接続テスト（要件6.2） */
    @PostMapping("/{id}/test")
    fun testConnection(
        @PathVariable id: Long,
        auth: Authentication
    ): ResponseEntity<TestConnectionResponse> {
        val userId = UserId(auth.principal as Long)
        val success = manageChannelUseCase.testConnection(userId, DeliveryChannelId(id))
        return ResponseEntity.ok(TestConnectionResponse(success))
    }
}
