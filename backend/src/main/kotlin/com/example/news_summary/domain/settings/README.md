# Settings ドメイン

## 概要

要約設定・収集スケジュール管理を担う境界コンテキスト。ユーザーごとの対象株価指数選択、分析観点選択、補足レベル・文字数モード設定、ニュース収集時刻のCron式管理を提供する。

**対応要件:** 1.1, 9.1〜9.11

## クラス一覧

### model/
| クラス | 役割 |
|---|---|
| `SummarySettingsId.kt` | 値オブジェクト。永続化済みエンティティのIDをnon-nullで保証 |
| `SummarySettings.kt` | ドメインモデル（集約ルート）。JPAアノテーションなし。id: SummarySettingsId |
| `CollectionScheduleId.kt` | 値オブジェクト。永続化済みエンティティのIDをnon-nullで保証 |
| `CollectionSchedule.kt` | ドメインモデル。JPAアノテーションなし。id: CollectionScheduleId |
| `AnalysisPerspective.kt` | 分析観点列挙型（INTEREST_RATE〜CUSTOM の9種） |

### repository/
| クラス | 役割 |
|---|---|
| `SummarySettingsRepository.kt` | ドメイン層ポート（インターフェース）。ドメインモデルのみを扱う |
| `CollectionScheduleRepository.kt` | ドメイン層ポート（インターフェース）。ドメインモデルのみを扱う |

### service/
| クラス | 役割 |
|---|---|
| `SummarySettingsService.kt` | 設定ドメインサービスインターフェース。getByUserId(), save(), getScheduleByUserId(), saveSchedule() |

## インフラ層（settings/infrastructure/）

### persistence/
| クラス | 役割 |
|---|---|
| `SummarySettingsJpaEntity.kt` | JPA用エンティティ（@Entity, id: Long? = null） |
| `CollectionScheduleJpaEntity.kt` | JPA用エンティティ |
| `SummarySettingsJpaRepository.kt` | Spring Data JPA リポジトリ（JpaEntity を扱う） |
| `CollectionScheduleJpaRepository.kt` | Spring Data JPA リポジトリ |
| `SummarySettingsRepositoryImpl.kt` | SummarySettingsRepository実装。JpaEntity ↔ ドメインモデル変換。idのnullチェックはここで1箇所のみ |
| `CollectionScheduleRepositoryImpl.kt` | CollectionScheduleRepository実装 |

## 機能別処理フロー

### 要約設定の取得・保存
```
Client → SettingsController.getSummarySettings()
  → SummarySettingsService.getByUserId(userId)
    → SummarySettingsRepository.findByUserId()
    → 存在しない場合はデフォルト値を返す
  ← SummarySettingsDto

Client → SettingsController.updateSummarySettings(request)
  → SummarySettingsService.save(settings)
    → SummarySettingsRepository.save()
  ← 200 OK
  ※次回のニュース収集・要約生成から新しい設定を適用（要件 9.10）
```

### 収集スケジュールの取得・保存
```
Client → SettingsController.getSchedule()
  → SummarySettingsService.getScheduleByUserId(userId)
    → CollectionScheduleRepository.findByUserId()
  ← CollectionScheduleDto

Client → SettingsController.updateSchedule(request)
  → SummarySettingsService.saveSchedule(schedule)
    → CollectionScheduleRepository.save()
    → Quartz ジョブの再スケジュール
  ← 200 OK
```

## 関連クラス（他ドメイン）
- `summary/AISummarizerService` — 要約生成時に SummarySettings を参照
- `summary/SupplementLevel`, `summary/SummaryMode` — 設定値の型定義は summary ドメインに所属
- `scheduler/NewsCollectionJob` — CollectionSchedule の cronExpression を参照

## 設計判断

- **AnalysisPerspective を settings ドメインに配置**: 分析観点の選択肢は設定の責務。summary ドメインはこの値を受け取って使うだけ
- **SupplementLevel/SummaryMode は summary ドメインに配置**: 要約の振る舞いを定義する型なので summary に所属。settings はこれらを参照する（ドメイン間の依存方向: settings → summary）
- **デフォルト値**: 設定が存在しない場合は INTERMEDIATE / STANDARD をデフォルトとして返す
- **ドメインモデルとJPAエンティティの分離**: ドメインモデルはJPAアノテーションを持たない純粋なオブジェクト。JpaEntityはインフラ層に配置し、RepositoryImplで変換を行う
