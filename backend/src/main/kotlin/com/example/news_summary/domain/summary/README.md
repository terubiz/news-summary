# Summary ドメイン

## 概要

AI要約生成・管理を担う境界コンテキスト。LLM API（Anthropic Claude Sonnet）を使ったニュース要約、株価指数への影響分析、補足レベル・文字数モードに応じた要約生成、用語解説セクション付加を提供する。

**対応要件:** 3.1〜3.16, 4.1〜4.6, 8.1, 8.2, 8.4, 9.8

## クラス一覧

### model/
| クラス | 役割 |
|---|---|
| `Summary.kt` | 要約エンティティ（集約ルート）。summaryText, supplementLevel, summaryMode, status, retryCount, sourceArticles(ManyToMany) |
| `SummaryIndexImpact.kt` | 指数影響エンティティ。indexSymbol, impactDirection |
| `SupplementLevel.kt` | 補足レベル列挙型（BEGINNER/INTERMEDIATE/ADVANCED） |
| `SummaryMode.kt` | 文字数モード列挙型（SHORT:150字/STANDARD:300字/DETAILED:600字） |
| `SummaryStatus.kt` | 要約ステータス列挙型（PENDING/IN_PROGRESS/COMPLETED/FAILED） |
| `ImpactDirection.kt` | 影響方向列挙型（BULLISH/BEARISH/NEUTRAL） |

### repository/
| クラス | 役割 |
|---|---|
| `SummaryRepository.kt` | 要約リポジトリポート。findByUserId, searchByKeyword, findRetryTargets |
| `SummaryIndexImpactRepository.kt` | 影響リポジトリポート。findBySummaryId, findByIndexSymbol |

### service/
| クラス | 役割 |
|---|---|
| `AISummarizerService.kt` | AI要約ドメインサービスインターフェース。summarize(), retryFailedSummaries() |

## 機能別処理フロー

### AI要約生成
```
NewsCollectedEvent → AISummarizerService.summarize(articles, indices, settings, userId)
  → SummaryPromptBuilder.build() [プロンプト動的構築]
    → 補足レベル別指示文の選択
    → 文字数モード別の上限設定
    → 分析観点の注入
    → 用語解説セクション指示（ADVANCED以外）
  → AnthropicChatClient.call(prompt) [Claude Sonnet API呼び出し]
  → 要約テキストのパース
  → SummaryRepository.save() [要約本体]
  → SummaryIndexImpactRepository.saveAll() [指数影響]
  → SsePublisher.publishSummaryCreated() [リアルタイム通知]

  ※API失敗時:
  → status = FAILED, retryCount++ で保存
  → retryFailedSummaries() で最大3回リトライ（指数バックオフ）
```

### 要約一覧取得
```
Client → SummaryController.getSummaries(page, size, indexFilter, keyword)
  → SummaryRepository.findByUserId() or searchByKeyword()
  → SummaryIndexImpactRepository.findBySummaryId() [各要約の影響情報]
  ← Page<SummaryDto>
```

## 関連クラス（他ドメイン）
- `news/NewsArticle` — 要約元の記事（ManyToMany）
- `index/IndexData` — 要約生成時に参照する指数データ
- `settings/SummarySettings` — 補足レベル・文字数モード・分析観点の設定
- `shared/SsePublisher` — 新規要約のリアルタイム通知

## 設計判断

- **enum をsummaryドメインに配置**: SupplementLevel, SummaryMode, SummaryStatus, ImpactDirection は要約の振る舞いを定義するため、summary ドメインに所属
- **ManyToMany（sourceArticles）**: 1つの要約が複数記事を参照し、1つの記事が複数要約に含まれる可能性がある
- **リトライ対象の取得**: `findRetryTargets()` で FAILED かつ retryCount < 3 の要約を一括取得

## プロパティテスト（予定）
| テスト | 検証内容 |
|---|---|
| Property 2 | 要約本文の文字数が設定モードの上限を超えない |
| Property 3 | ADVANCED設定では用語解説セクションが含まれない |
| Property 4 | 用語解説の各項目説明文が50文字以内 |
