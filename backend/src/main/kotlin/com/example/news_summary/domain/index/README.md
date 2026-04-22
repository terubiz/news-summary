# Index ドメイン

## 概要

株価指数データの取得・管理を担う境界コンテキスト。外部株価APIからの指数データ取得、キャッシュ管理、市場閉場時の終値返却を提供する。

**対応要件:** 2.1, 2.2, 2.3, 2.4, 2.5

## クラス一覧

### model/
| クラス | 役割 |
|---|---|
| `IndexDataId.kt` | 値オブジェクト。永続化済みエンティティのIDをnon-nullで保証 |
| `IndexData.kt` | ドメインモデル（集約ルート）。JPAアノテーションなし。id: IndexDataId |

### repository/
| クラス | 役割 |
|---|---|
| `IndexDataRepository.kt` | ドメイン層ポート（インターフェース）。ドメインモデルのみを扱う |

### service/
| クラス | 役割 |
|---|---|
| `IndexAnalyzerService.kt` | 指数分析ドメインサービスインターフェース。fetchLatestIndices(), getCachedIndices() |

## インフラ層（index/infrastructure/）

### persistence/
| クラス | 役割 |
|---|---|
| `IndexDataJpaEntity.kt` | JPA用エンティティ（@Entity, id: Long? = null） |
| `IndexDataJpaRepository.kt` | Spring Data JPA リポジトリ（JpaEntity を扱う） |
| `IndexDataRepositoryImpl.kt` | IndexDataRepository実装。JpaEntity ↔ ドメインモデル変換。idのnullチェックはここで1箇所のみ |

## 機能別処理フロー

### 株価指数データ取得
```
IndexController.getIndices() or NewsCollectorService（収集時に連携）
  → IndexAnalyzerService.fetchLatestIndices(symbols)
    → StockApiClient.getLatestQuotes() [外部API呼び出し]
    → IndexDataRepository.save() [取得データをDB保存]
  ← List<IndexData>

  ※API失敗時:
  → IndexAnalyzerService.getCachedIndices(symbols)
    → IndexDataRepository.findLatestBySymbols() [キャッシュ返却]
    → isStale = true フラグ付与
  ← List<IndexData> (stale)
```

## 関連クラス（他ドメイン）
- `summary/AISummarizerService` — 要約生成時に指数データを参照
- `settings/SummarySettings.selectedIndices` — ユーザーが選択した対象指数

## 設計判断

- **isStale フラグ**: API失敗時にキャッシュデータを返す際、データが古いことをクライアントに明示する（要件2.3）
- **シンボル別の最新データ取得**: `findLatestBySymbol` でシンボルごとに最新1件を取得するサブクエリを使用
- **ドメインモデルとJPAエンティティの分離**: ドメインモデルはJPAアノテーションを持たない純粋なオブジェクト。JpaEntityはインフラ層に配置し、RepositoryImplで変換を行う
