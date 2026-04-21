package com.example.news_summary.api.index

import com.example.news_summary.domain.user.model.UserId
import com.example.news_summary.index.application.usecase.FetchIndexDataUseCase
import com.example.news_summary.settings.application.usecase.ManageSettingsUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

// ---- レスポンス DTO ----

data class IndexDataResponse(
    val symbol: String,
    val currentValue: BigDecimal,
    val changeAmount: BigDecimal,
    val changeRate: BigDecimal,
    val isStale: Boolean
)

// ---- コントローラ ----

@RestController
@RequestMapping("/api/v1/indices")
class IndexController(
    private val fetchIndexDataUseCase: FetchIndexDataUseCase,
    private val manageSettingsUseCase: ManageSettingsUseCase
) {
    /**
     * 最新株価指数データ一覧（要件2.1, 4.4）
     *
     * symbols パラメータが指定されない場合、ユーザーの要約設定から選択中の指数を使用する。
     * デフォルト指数: N225, SPX, IXIC, GDAXI
     */
    @GetMapping
    fun getIndices(
        @RequestParam(required = false) symbols: List<String>?,
        auth: Authentication
    ): ResponseEntity<List<IndexDataResponse>> {
        val userId = UserId(auth.principal as Long)
        val targetSymbols = symbols
            ?: manageSettingsUseCase.getSummarySettings(userId).selectedIndices.ifEmpty {
                listOf("N225", "SPX", "IXIC", "GDAXI")
            }

        val indices = fetchIndexDataUseCase.execute(targetSymbols)
        return ResponseEntity.ok(indices.map {
            IndexDataResponse(
                symbol = it.symbol,
                currentValue = it.currentValue,
                changeAmount = it.changeAmount,
                changeRate = it.changeRate,
                isStale = it.isStale
            )
        })
    }
}
