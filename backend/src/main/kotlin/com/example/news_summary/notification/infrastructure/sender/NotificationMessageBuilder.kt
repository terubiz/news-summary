package com.example.news_summary.notification.infrastructure.sender

import com.example.news_summary.domain.index.model.StockSymbol
import com.example.news_summary.domain.summary.model.Summary
import com.example.news_summary.domain.summary.model.SummaryIndexImpact

/**
 * 通知メッセージ構築ユーティリティ。
 * 各アダプタで共通のメッセージフォーマットを提供する。
 */
object NotificationMessageBuilder {

    /** プレーンテキスト形式のメッセージを構築する */
    fun buildPlainText(
        summary: Summary,
        impacts: List<SummaryIndexImpact>,
        sourceUrls: List<String>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("📰 経済ニュース要約")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine()
        sb.appendLine(summary.summaryText)
        sb.appendLine()

        if (impacts.isNotEmpty()) {
            sb.appendLine("📊 株価指数への影響:")
            impacts.forEach { impact ->
                val displayName = StockSymbol.displayNameOf(impact.indexSymbol)
                val arrow = when (impact.impactDirection.name) {
                    "BULLISH" -> "📈 上昇要因"
                    "BEARISH" -> "📉 下落要因"
                    else -> "➡️ 中立"
                }
                sb.appendLine("  $displayName ($arrow)")
            }
            sb.appendLine()
        }

        if (sourceUrls.isNotEmpty()) {
            sb.appendLine("🔗 ソース:")
            sourceUrls.forEach { url ->
                sb.appendLine("  $url")
            }
        }

        sb.appendLine()
        sb.appendLine("生成日時: ${summary.generatedAt}")
        return sb.toString()
    }

    /** HTML形式のメッセージを構築する（Email用） */
    fun buildHtml(
        summary: Summary,
        impacts: List<SummaryIndexImpact>,
        sourceUrls: List<String>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("<html><body>")
        sb.appendLine("<h2>📰 経済ニュース要約</h2>")
        sb.appendLine("<hr/>")
        sb.appendLine("<p>${summary.summaryText.replace("\n", "<br/>")}</p>")

        if (impacts.isNotEmpty()) {
            sb.appendLine("<h3>📊 株価指数への影響</h3>")
            sb.appendLine("<ul>")
            impacts.forEach { impact ->
                val displayName = StockSymbol.displayNameOf(impact.indexSymbol)
                val arrow = when (impact.impactDirection.name) {
                    "BULLISH" -> "📈 上昇要因"
                    "BEARISH" -> "📉 下落要因"
                    else -> "➡️ 中立"
                }
                sb.appendLine("<li><strong>$displayName</strong> ($arrow)</li>")
            }
            sb.appendLine("</ul>")
        }

        if (sourceUrls.isNotEmpty()) {
            sb.appendLine("<h3>🔗 ソース</h3>")
            sb.appendLine("<ul>")
            sourceUrls.forEach { url ->
                sb.appendLine("<li><a href=\"$url\">$url</a></li>")
            }
            sb.appendLine("</ul>")
        }

        sb.appendLine("<p><small>生成日時: ${summary.generatedAt}</small></p>")
        sb.appendLine("</body></html>")
        return sb.toString()
    }
}
