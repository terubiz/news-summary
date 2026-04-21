# Summary ドメイン

## 概要

AI要約生成・管理を担う境界コンテキスト。LLM API（Anthropic Claude Sonnet）を使ったニュース要約、株価指数への影響分析、補足レベル・文字数モードに応じた要約生成、用語解説セクション付加を提供する。

**対応要件:** 3.1〜3.16, 4.1〜4.6, 8.1, 8.2, 8.4, 9.8

## クラス一覧

### model/
| クラス | 役割 |
|---|---|
| `SummaryId.kt` | 値オブジェクト。永続化済みエンティティのIDをnon-nullで保証 |
| `Summary.kt` | ドメインモデル（集約ルート）。JPAアノテーションなし。id: SummaryId。sourceArticleIds で記事IDを保持 |
| `SummaryIndexImpactId.kt` | 値オブジェクト。永続化済みエンティティのIDをnon-nullで保証 |
| `SummaryIndexImpact.kt` | ドメインモデル。JPAアノテーションなし。id: SummaryIndexImpactId |
| `SupplementLevel.kt` | 補足レベル列挙型（BEGINNER/INTERMEDIATE/ADVANCED） |
| `SummaryMode.kt` | 文字数モード列挙型（SHORT:150字/STANDARD:300字/DETAILED:600字） |
| `SummaryStatus.kt` | 要約ステータス列挙型（PENDING/IN_PROGRESS/COMPLETED/FAILED） |
| `ImpactDirection.kt` | 影響方向列挙型（BULLISH/BEARISH/NEUTRAL） |

### repository/
| クラス | 役割 |
|---|---|
| `SummaryRepository.kt` | ドメイン層ポート（インターフェース）。ドメインモデルのみを扱う |
| `SummaryIndexImpactRepository.kt` | ドメイン層ポート（インターフェース）。ドメインモデルのみを扱う |

### service/
| クラス | 役割 |
|---|---|
| `AISummarizerService.kt` | AI要約ドメインサービスインターフェース。summarize(), retryFailedSummaries() |

## インフラ層（summary/infrastructure/）

### persistence/
| クラス | 役割 |
|---|---|
| `SummaryJpaEntity.kt` | JPA用エンティティ（@Entity, id: Long? = null）。sourceArticles(ManyToMany)を管理 |
| `SummaryIndexImpactJpaEntity.kt` | JPA用エンティティ |
| `SummaryJpaRepository.kt` | Spring Data JPA リポジトリ（JpaEntity を扱う） |
| `SummaryIndexImpactJpaRepository.kt` | Spring Data JPA リポジトリ |
| `SummaryRepositoryImpl.kt` | SummaryRepository実装。JpaEntity ↔ ドメインモデル変換。idのnullチェックはここで1箇所のみ |
| `SummaryIndexImpactRepositoryImpl.kt` | SummaryIndexImpactRepository実装 |

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
  ← PageResult<Summary>
```

## 関連クラス（他ドメイン）
- `news/NewsArticle` — 要約元の記事（ManyToMany、JpaEntity側で管理）
- `index/IndexData` — 要約生成時に参照する指数データ
- `settings/SummarySettings` — 補足レベル・文字数モード・分析観点の設定
- `shared/SsePublisher` — 新規要約のリアルタイム通知

## 設計判断

- **enum をsummaryドメインに配置**: SupplementLevel, SummaryMode, SummaryStatus, ImpactDirection は要約の振る舞いを定義するため、summary ドメインに所属
- **ManyToMany（sourceArticles）**: JpaEntity側で管理。ドメインモデルではNewsArticleIdのSetとして保持
- **リトライ対象の取得**: `findRetryTargets()` で FAILED かつ retryCount < 3 の要約を一括取得
- **ドメインモデルとJPAエンティティの分離**: ドメインモデルはJPAアノテーションを持たない純粋なオブジェクト。JpaEntityはインフラ層に配置し、RepositoryImplで変換を行う

## プロパティテスト（予定）
| テスト | 検証内容 |
|---|---|
| Property 2 | 要約本文の文字数が設定モードの上限を超えない |
| Property 3 | ADVANCED設定では用語解説セクションが含まれない |
| Property 4 | 用語解説の各項目説明文が50文字以内 |
