package com.example.news_summary.api.summary

import com.example.news_summary.domain.summary.model.SummaryId
import com.example.news_summary.domain.summary.repository.SummaryIndexImpactRepository
import com.example.news_summary.domain.summary.repository.SummaryRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 要約 API コントローラ。
 * GET /api/v1/summaries: ページネーション・Stock_Indexフィルタ・キーワード検索
 * GET /api/v1/summaries/{id}: 要約詳細（SummaryIndexImpact含む）
 */
@RestController
@RequestMapping("/api/v1/summaries")
class SummaryController(
    private val summaryRepository: SummaryRepository,
    private val summaryIndexImpactRepository: SummaryIndexImpactRepository
) {

    /**
     * 要約一覧取得（ページネーション・フィルタ・検索対応）
     */
    @GetMapping
    fun listSummaries(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) indexSymbol: String?,
        @RequestParam(required = false) keyword: String?
    ): ResponseEntity<SummaryListResponse> {
        val result = when {
            !keyword.isNullOrBlank() -> summaryRepository.searchAllByKeyword(keyword, page, size)
            !indexSymbol.isNullOrBlank() -> summaryRepository.findByIndexSymbol(indexSymbol, page, size)
            else -> summaryRepository.findAllOrderByGeneratedAtDesc(page, size)
        }

        val items = result.content.map { summary ->
            val impacts = summaryIndexImpactRepository.findBySummaryId(summary.id.value)
            SummaryResponse(
                id = summary.id.value,
                summaryText = summary.summaryText,
                supplementLevel = summary.supplementLevel.name,
                summaryMode = summary.summaryMode.name,
                status = summary.status.name,
                generatedAt = summary.generatedAt.toString(),
                indexImpacts = impacts.map { impact ->
                    IndexImpactResponse(
                        id = impact.id.value,
                        indexSymbol = impact.indexSymbol,
                        impactDirection = impact.impactDirection.name
                    )
                }
            )
        }

        return ResponseEntity.ok(
            SummaryListResponse(
                content = items,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                page = result.page,
                size = result.size
            )
        )
    }

    /**
     * 要約詳細取得（SummaryIndexImpact含む）
     */
    @GetMapping("/{id}")
    fun getSummary(@PathVariable id: Long): ResponseEntity<SummaryResponse> {
        val summary = summaryRepository.findById(SummaryId(id))
            .orElse(null) ?: return ResponseEntity.notFound().build()

        val impacts = summaryIndexImpactRepository.findBySummaryId(summary.id.value)

        return ResponseEntity.ok(
            SummaryResponse(
                id = summary.id.value,
                summaryText = summary.summaryText,
                supplementLevel = summary.supplementLevel.name,
                summaryMode = summary.summaryMode.name,
                status = summary.status.name,
                generatedAt = summary.generatedAt.toString(),
                indexImpacts = impacts.map { impact ->
                    IndexImpactResponse(
                        id = impact.id.value,
                        indexSymbol = impact.indexSymbol,
                        impactDirection = impact.impactDirection.name
                    )
                }
            )
        )
    }
}

/** 要約一覧レスポンス */
data class SummaryListResponse(
    val content: List<SummaryResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)

/** 要約レスポンス */
data class SummaryResponse(
    val id: Long,
    val summaryText: String,
    val supplementLevel: String,
    val summaryMode: String,
    val status: String,
    val generatedAt: String,
    val indexImpacts: List<IndexImpactResponse> = emptyList()
)

/** 指数影響レスポンス */
data class IndexImpactResponse(
    val id: Long,
    val indexSymbol: String,
    val impactDirection: String
)
