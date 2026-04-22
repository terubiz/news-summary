package com.example.news_summary.api.settings

import com.example.news_summary.domain.settings.model.AnalysisPerspective
import com.example.news_summary.domain.summary.model.SummaryMode
import com.example.news_summary.domain.summary.model.SupplementLevel
import com.example.news_summary.domain.user.model.UserId
import com.example.news_summary.settings.application.usecase.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

// ---- リクエスト/レスポンス DTO ----

data class SummarySettingsResponse(
    val selectedIndices: List<String>,
    val analysisPerspectives: List<String>,
    val supplementLevel: SupplementLevel,
    val summaryMode: SummaryMode,
    val availablePerspectives: List<PerspectiveOption>
)

data class PerspectiveOption(val name: String, val displayName: String)

data class UpdateSummarySettingsRequest(
    @field:NotNull val selectedIndices: List<String>,
    @field:NotNull val analysisPerspectives: List<String>,
    @field:NotNull val supplementLevel: SupplementLevel,
    @field:NotNull val summaryMode: SummaryMode
)

data class ScheduleResponse(
    val cronExpression: String,
    val enabled: Boolean
)

data class UpdateScheduleRequest(
    @field:NotBlank val cronExpression: String,
    val enabled: Boolean = true
)

// ---- コントローラ ----

@RestController
@RequestMapping("/api/v1/settings")
class SettingsController(
    private val manageSettingsUseCase: ManageSettingsUseCase
) {
    /** 要約設定取得（要件9.11: 現在有効な設定を常に表示） */
    @GetMapping("/summary")
    fun getSummarySettings(auth: Authentication): ResponseEntity<SummarySettingsResponse> {
        val userId = UserId(auth.principal as Long)
        val settings = manageSettingsUseCase.getSummarySettings(userId)
        return ResponseEntity.ok(SummarySettingsResponse(
            selectedIndices = settings.selectedIndices,
            analysisPerspectives = settings.analysisPerspectives,
            supplementLevel = settings.supplementLevel,
            summaryMode = settings.summaryMode,
            availablePerspectives = AnalysisPerspective.entries.map {
                PerspectiveOption(it.name, it.displayName)
            }
        ))
    }

    /** 要約設定更新（要件9.7, 9.10: 次回の収集・要約生成から反映） */
    @PutMapping("/summary")
    fun updateSummarySettings(
        @Valid @RequestBody request: UpdateSummarySettingsRequest,
        auth: Authentication
    ): ResponseEntity<SummarySettingsResponse> {
        val userId = UserId(auth.principal as Long)
        val settings = manageSettingsUseCase.updateSummarySettings(
            userId,
            UpdateSummarySettingsCommand(
                selectedIndices = request.selectedIndices,
                analysisPerspectives = request.analysisPerspectives,
                supplementLevel = request.supplementLevel,
                summaryMode = request.summaryMode
            )
        )
        return ResponseEntity.ok(SummarySettingsResponse(
            selectedIndices = settings.selectedIndices,
            analysisPerspectives = settings.analysisPerspectives,
            supplementLevel = settings.supplementLevel,
            summaryMode = settings.summaryMode,
            availablePerspectives = AnalysisPerspective.entries.map {
                PerspectiveOption(it.name, it.displayName)
            }
        ))
    }

    /** 収集スケジュール取得（要件1.1） */
    @GetMapping("/schedule")
    fun getSchedule(auth: Authentication): ResponseEntity<ScheduleResponse?> {
        val userId = UserId(auth.principal as Long)
        val schedule = manageSettingsUseCase.getSchedule(userId)
        return ResponseEntity.ok(schedule?.let {
            ScheduleResponse(it.cronExpression, it.enabled)
        })
    }

    /** 収集スケジュール更新（要件1.1） */
    @PutMapping("/schedule")
    fun updateSchedule(
        @Valid @RequestBody request: UpdateScheduleRequest,
        auth: Authentication
    ): ResponseEntity<ScheduleResponse> {
        val userId = UserId(auth.principal as Long)
        val schedule = manageSettingsUseCase.updateSchedule(
            userId,
            UpdateScheduleCommand(request.cronExpression, request.enabled)
        )
        return ResponseEntity.ok(ScheduleResponse(schedule.cronExpression, schedule.enabled))
    }
}
