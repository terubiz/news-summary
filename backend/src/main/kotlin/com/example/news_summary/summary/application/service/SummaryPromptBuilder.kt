package com.example.news_summary.summary.application.service

import com.example.news_summary.domain.news.model.NewsArticle
import com.example.news_summary.domain.settings.model.AnalysisPerspective
import com.example.news_summary.domain.settings.model.SummarySettings
import com.example.news_summary.domain.summary.model.SupplementLevel
import org.springframework.stereotype.Component

/**
 * AI要約生成用プロンプトビルダー。
 * 補足レベル・文字数モード・分析観点・用語解説指示・株価指数名を
 * 動的に組み立ててLLMに渡すプロンプトを構築する。
 *
 * 株価指数の最新値はAI側がGoogle Search等で検索して取得する。
 * アプリ側は指数名のリストのみを渡す。
 */
@Component
class SummaryPromptBuilder {

    /**
     * プロンプトを構築する。
     * @param articles 要約対象のニュース記事リスト
     * @param indexNames 参照する株価指数の名前リスト（例: ["日経225", "S&P500"]）
     * @param settings 要約設定（補足レベル・文字数モード・分析観点）
     * @return LLMに渡すプロンプト文字列
     */
    fun build(
        articles: List<NewsArticle>,
        indexNames: List<String>,
        settings: SummarySettings
    ): String {
        val sb = StringBuilder()

        sb.appendLine("あなたは経済ニュースの専門アナリストです。")
        sb.appendLine("以下のニュース記事を分析し、株価指数への影響を含めた要約を日本語で生成してください。")
        sb.appendLine()
        sb.appendLine("【重要な注意事項】")
        sb.appendLine("記事の中には、最近公開されたものの内容が過去の出来事のみを扱っているものがあります。")
        sb.appendLine("現在進行中のニュースや最新の動向に焦点を当て、過去の出来事だけを再報道した記事は要約から除外してください。")
        sb.appendLine("ただし、過去の出来事が現在の市場動向に直接関連している場合は含めてください。")
        sb.appendLine()

        // 文字数モード指示
        val charLimit = settings.summaryMode.charLimit
        sb.appendLine("【文字数制限】")
        sb.appendLine("要約本文は${charLimit}文字以内で記述してください。")
        sb.appendLine()

        // 補足レベル別指示
        sb.appendLine("【補足レベル】")
        sb.appendLine(supplementLevelInstruction(settings.supplementLevel))
        sb.appendLine()

        // 分析観点の注入
        if (settings.analysisPerspectives.isNotEmpty()) {
            sb.appendLine("【分析観点】")
            sb.appendLine("以下の観点を重点的に分析してください：")
            settings.analysisPerspectives.forEach { perspectiveStr ->
                val displayName = try {
                    AnalysisPerspective.valueOf(perspectiveStr).displayName
                } catch (_: IllegalArgumentException) {
                    perspectiveStr
                }
                sb.appendLine("- $displayName")
            }
            sb.appendLine()
        }

        // 用語解説セクション指示（ADVANCED以外）
        if (settings.supplementLevel != SupplementLevel.ADVANCED) {
            sb.appendLine("【用語解説】")
            sb.appendLine("要約の末尾に用語解説セクションを追加してください。")
            sb.appendLine("形式: 【用語名】: 説明文（50文字以内）")
            sb.appendLine("専門用語や略語について、初心者にもわかるよう簡潔に解説してください。")
            sb.appendLine()
        }

        // 株価指数（AI側が検索して最新値を取得する）
        if (indexNames.isNotEmpty()) {
            sb.appendLine("【対象株価指数】")
            sb.appendLine("以下の株価指数について、Google検索で最新の値動きを確認し、要約に含めてください：")
            indexNames.forEach { name ->
                sb.appendLine("- $name")
            }
            sb.appendLine()
        }

        // 出力形式指示
        sb.appendLine("【出力形式】")
        sb.appendLine("以下のJSON形式で出力してください：")
        sb.appendLine("""
            |{
            |  "summaryText": "要約本文（${charLimit}文字以内）",
            |  "indexImpacts": [
            |    {
            |      "indexSymbol": "指数名（例: 日経225, S&P500, NASDAQ, DAX）",
            |      "impactDirection": "BULLISH または BEARISH または NEUTRAL"
            |    }
            |  ]
            |}
        """.trimMargin())
        sb.appendLine()

        // ニュース記事
        sb.appendLine("【ニュース記事】")
        articles.forEachIndexed { i, article ->
            sb.appendLine("--- 記事${i + 1} ---")
            sb.appendLine("タイトル: ${article.title}")
            sb.appendLine("ソース: ${article.sourceName}")
            sb.appendLine("公開日時: ${article.publishedAt}")
            sb.appendLine("本文: ${article.content}")
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun supplementLevelInstruction(level: SupplementLevel): String = when (level) {
        SupplementLevel.BEGINNER ->
            "初心者向けの要約を生成してください。基本的な因果関係を含め、なぜそのニュースが株価に影響するのかをわかりやすく説明してください。"
        SupplementLevel.INTERMEDIATE ->
            "中級者向けの要約を生成してください。市場背景を中心に、マクロ経済の文脈や関連する経済指標との関係を説明してください。"
        SupplementLevel.ADVANCED ->
            "上級者向けの簡潔な分析のみを生成してください。冗長な説明は不要です。核心的な市場インパクトと投資判断に直結する情報に絞ってください。"
    }
}
