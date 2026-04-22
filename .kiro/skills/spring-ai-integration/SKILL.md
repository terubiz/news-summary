---
name: spring-ai-integration
description: Spring AI integration patterns for LLM-powered features using Anthropic Claude. Use when integrating Claude Sonnet, building AI-powered services, or implementing prompt-based workflows. Covers ChatClient, prompt engineering, and error handling.
---

## Spring AI + Anthropic Claude 統合パターン

### ChatClient セットアップ

```kotlin
@Configuration
class AiConfig {
    @Bean
    fun chatClient(chatClientBuilder: ChatClient.Builder): ChatClient {
        return chatClientBuilder.build()
    }
}
```

`application.yml` 設定：

```yaml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      chat:
        options:
          model: claude-sonnet-4-5
          max-tokens: 2048
          temperature: 0.7
```

### プロンプトエンジニアリング

- 構造化されたプロンプトに明確な指示を含める
- Few-shot学習のための例を含める
- 出力フォーマット（JSON、プレーンテキスト等）を明示する
- Claudeは日本語の品質が高く、経済ニュース要約に適している

```kotlin
val response = chatClient.prompt()
    .user { u -> u.text("""
        以下のニュース記事を日本語で要約してください。
        
        記事: {article}
        
        フォーマット: {charLimit}文字以内、株価指数への影響を含める。
    """.trimIndent())
    .param("article", articleText)
    .param("charLimit", charLimit.toString())
    }
    .call()
    .content()
```

### 動的プロンプト構築

- ユーザー設定（補足レベル・文字数モード・分析観点）に基づいてプロンプトを構築
- 補足レベル別の指示文を切り替える
- API呼び出し前にプロンプト長を検証する

### エラーハンドリング

- `AnthropicApiException` をキャッチしてAPIエラーを処理する
- 指数バックオフによるリトライロジックを実装する
- 失敗したプロンプトをデバッグ用にログ記録する
- API失敗時のフォールバックレスポンスを提供する

### コスト最適化

- 適切な場合はレスポンスをキャッシュする
- 長いレスポンスにはストリーミングを使用する
- トークン使用量を監視する
- シンプルなタスクには軽量モデルの使用を検討する

### テスト

- ユニットテストでは `ChatClient` をモック化する
- プロンプトレスポンスのテストフィクスチャを使用する
- プロンプト構築ロジックを個別にテストする
- 出力パースを検証する
