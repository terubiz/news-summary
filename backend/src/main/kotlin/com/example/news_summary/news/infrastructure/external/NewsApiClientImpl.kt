package com.example.news_summary.news.infrastructure.external

import com.example.news_summary.domain.news.service.NewsApiClient
import com.example.news_summary.domain.news.service.RawNewsArticle
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * NewsAPI (https://newsapi.org) クライアント実装。
 * 接続失敗時はエラーをログに記録し、空リストを返す（要件1.4）。
 *
 * NOTE: NewsAPI無料プランは過去30日分の記事のみ取得可能。
 * fromDateが30日以上前の場合でもエラーにはならないが、結果が空になる可能性がある。
 */
@Component
class NewsApiClientImpl(
    @Value("\${app.news-api.key}") private val apiKey: String,
    @Value("\${app.news-api.base-url}") private val baseUrl: String
) : NewsApiClient {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val restTemplate = RestTemplate()

    override fun fetchLatestNews(query: String, fromDate: LocalDate?): List<RawNewsArticle> {
        return try {
            val fromParam = fromDate?.let {
                "&from=${it.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
            } ?: ""
            val url = "$baseUrl/everything?q=$query&language=en&sortBy=publishedAt&pageSize=50${fromParam}&apiKey=$apiKey"
            logger.debug("NewsAPI リクエスト: from={}", fromDate)
            val response = restTemplate.getForObject(url, NewsApiResponse::class.java)

            response?.articles?.mapNotNull { article ->
                // title, url が null の記事はスキップ
                if (article.title.isNullOrBlank() || article.url.isNullOrBlank()) return@mapNotNull null
                RawNewsArticle(
                    title = article.title,
                    content = article.content ?: article.description ?: "",
                    sourceUrl = article.url,
                    sourceName = article.source?.name ?: "Unknown",
                    publishedAt = article.publishedAt ?: ""
                )
            } ?: emptyList()
        } catch (e: Exception) {
            logger.error("NewsAPI接続失敗: ${e.message}", e)
            emptyList()
        }
    }
}

// ---- NewsAPI レスポンスマッピング ----

@JsonIgnoreProperties(ignoreUnknown = true)
data class NewsApiResponse(
    val status: String?,
    val totalResults: Int?,
    val articles: List<NewsApiArticle>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NewsApiArticle(
    val source: NewsApiSource?,
    val title: String?,
    val description: String?,
    val url: String?,
    val content: String?,
    @JsonProperty("publishedAt") val publishedAt: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NewsApiSource(val name: String?)
