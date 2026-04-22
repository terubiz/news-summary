# News ドメイン

## 概要

経済ニュースの収集・管理を担う境界コンテキスト。外部ニュースAPIからの記事検索・取得、重複排除、収集ログ記録を提供する。収集完了後にAI要約処理をトリガーするドメインイベントを発行する。

**対応要件:** 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7

## クラス一覧

### model/
| クラス | 役割 |
|---|---|
| `NewsArticleId.kt` | 値オブジェクト。永続化済みエンティティのIDをnon-nullで保証 |
| `NewsArticle.kt` | ドメインモデル（集約ルート）。JPAアノテーションなし。id: NewsArticleId |
| `CollectionLogId.kt` | 値オブジェクト。永続化済みエンティティのIDをnon-nullで保証 |
| `CollectionLog.kt` | ドメインモデル。JPAアノテーションなし。id: CollectionLogId |

### repository/
| クラス | 役割 |
|---|---|
| `NewsArticleRepository.kt` | ドメイン層ポート（インターフェース）。ドメインモデルのみを扱う |
| `CollectionLogRepository.kt` | ドメイン層ポート（インターフェース）。ドメインモデルのみを扱う |

### service/
| クラス | 役割 |
|---|---|
| `NewsCollectorService.kt` | 収集ドメインサービスインターフェース。collectNews(), isDuplicate() |

## インフラ層（news/infrastructure/）

### persistence/
| クラス | 役割 |
|---|---|
| `NewsArticleJpaEntity.kt` | JPA用エンティティ（@Entity, id: Long? = null） |
| `CollectionLogJpaEntity.kt` | JPA用エンティティ |
| `NewsArticleJpaRepository.kt` | Spring Data JPA リポジトリ（JpaEntity を扱う） |
| `CollectionLogJpaRepository.kt` | Spring Data JPA リポジトリ |
| `NewsArticleRepositoryImpl.kt` | NewsArticleRepository実装。JpaEntity ↔ ドメインモデル変換。idのnullチェックはここで1箇所のみ |
| `CollectionLogRepositoryImpl.kt` | CollectionLogRepository実装。JpaEntity ↔ ドメインモデル変換 |

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
- **ドメインモデルとJPAエンティティの分離**: ドメインモデルはJPAアノテーションを持たない純粋なオブジェクト。JpaEntityはインフラ層に配置し、RepositoryImplで変換を行う
