package com.example.news_summary.api.news

import com.example.news_summary.domain.user.model.UserId
import com.example.news_summary.news.application.usecase.CollectNewsUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class CollectionRequest(
    val fromDays: Int = 1  // 何日前からの記事を取得するか（1〜365）
)

data class CollectionResponse(
    val savedCount: Int,
    val skippedCount: Int,
    val errorCount: Int,
    val message: String
)

/**
 * ニュース収集の手動実行エンドポイント。
 * ダッシュボードの「収集＆要約」ボタンから呼ばれる。
 */
@RestController
@RequestMapping("/api/v1/collect")
class NewsCollectionController(
    private val collectNewsUseCase: CollectNewsUseCase
) {
    @PostMapping
    fun collectNow(
        @RequestBody request: CollectionRequest,
        auth: Authentication
    ): ResponseEntity<CollectionResponse> {
        val userId = UserId(auth.principal as Long)
        val result = collectNewsUseCase.execute(userId, request.fromDays)
        return ResponseEntity.ok(
            CollectionResponse(
                savedCount = result.savedCount,
                skippedCount = result.skippedCount,
                errorCount = result.errorCount,
                message = "収集完了: ${result.savedCount}件保存, ${result.skippedCount}件スキップ"
            )
        )
    }
}
