# News ドメイン

## 概要

経済ニュースの収集・管理を担う境界コンテキスト。外部ニュースAPIからの記事検索・取得、重複排除、収集ログ記録を提供する。収集完了後にAI要約処理をトリガーするドメインイベントを発行する。

**対応要件:** 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7

## クラス一覧

### model/
| クラス | 役割 |
|---|---|
| `NewsArticle.kt` | ニュース記事エンティティ（集約ルート）。title, content, sourceUrl(UNIQUE), publishedAt |
| `CollectionLog.kt` | 収集実行ログエンティティ。articleCount, status, errorMessage |

### repository/
| クラス | 役割 |
|---|---|
| `NewsArticleRepository.kt` | 記事リポジトリポート。existsBySourceUrl, existsByTitle, findByCollectedAtAfter |
| `CollectionLogRepository.kt` | 収集ログリポジトリポート。findByUserIdOrderByExecutedAtDesc |

### service/
| クラス | 役割 |
|---|---|
| `NewsCollectorService.kt` | 収集ドメインサービスインターフェース。collectNews(), isDuplicate() |

## 機能別処理フロー

### ニュース収集（スケジュール実行）
```
Scheduler(NewsCollectionJob) → NewsCollectorService.collectNews(userId)
  → NewsApiClient.fetchLatestNews() [外部API呼び出し]
  → 各記事に対して:
    → isDuplicate(article) [sourceUrl or title の重複チェック]
    → NewsArticleRepository.save() [重複なしの場合のみ]
  → CollectionLogRepository.save() [実行ログ記録]
  → DomainEvent: NewsCollectedEvent を発行
    → AISummarizerService.summarize() をトリガー
```

## 関連クラス（他ドメイン）
- `summary/AISummarizerService` — 収集完了後にAI要約をトリガー
- `settings/CollectionSchedule` — 収集時刻のCron式を参照

## 設計判断

- **sourceUrl に UNIQUE 制約**: DB レベルで重複を防止。アプリケーション層の `isDuplicate()` と二重チェック
- **CollectionLog を news ドメインに配置**: 収集実行のログは news の責務。notification ドメインではない
- **ドメインイベントによる疎結合**: 収集完了 → 要約生成を直接呼び出さず、イベント経由で連携する設計（将来的にイベントバスに移行可能）
