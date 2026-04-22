# Notification ドメイン

## 概要

通知送信・管理を担う境界コンテキスト。メール・Slack・LINE・Discordの4チャンネルへの要約送信、送信ログ記録、リトライ処理、接続情報の暗号化管理を提供する。

**対応要件:** 5.1〜5.13, 6.1〜6.7

## クラス一覧

### model/
| クラス | 役割 |
|---|---|
| `DeliveryChannelId.kt` | 値オブジェクト。永続化済みエンティティのIDをnon-nullで保証 |
| `DeliveryChannel.kt` | ドメインモデル（集約ルート）。JPAアノテーションなし。id: DeliveryChannelId |
| `DeliveryLogId.kt` | 値オブジェクト。永続化済みエンティティのIDをnon-nullで保証 |
| `DeliveryLog.kt` | ドメインモデル。JPAアノテーションなし。id: DeliveryLogId |
| `ChannelType.kt` | チャンネル種別列挙型（EMAIL/SLACK/LINE/DISCORD） |

### repository/
| クラス | 役割 |
|---|---|
| `DeliveryChannelRepository.kt` | ドメイン層ポート（インターフェース）。ドメインモデルのみを扱う |
| `DeliveryLogRepository.kt` | ドメイン層ポート（インターフェース）。ドメインモデルのみを扱う |

### service/
| クラス | 役割 |
|---|---|
| `NotificationService.kt` | 通知ドメインサービスインターフェース。sendToChannel(), sendToChannels(), retryFailedDeliveries() |

## インフラ層（notification/infrastructure/）

### persistence/
| クラス | 役割 |
|---|---|
| `DeliveryChannelJpaEntity.kt` | JPA用エンティティ（@Entity, id: Long? = null） |
| `DeliveryLogJpaEntity.kt` | JPA用エンティティ |
| `DeliveryChannelJpaRepository.kt` | Spring Data JPA リポジトリ（JpaEntity を扱う） |
| `DeliveryLogJpaRepository.kt` | Spring Data JPA リポジトリ |
| `DeliveryChannelRepositoryImpl.kt` | DeliveryChannelRepository実装。JpaEntity ↔ ドメインモデル変換。idのnullチェックはここで1箇所のみ |
| `DeliveryLogRepositoryImpl.kt` | DeliveryLogRepository実装 |

## 機能別処理フロー

### 単一チャンネル送信
```
NotificationService.sendToChannel(summary, channel)
  → ChannelType に応じたアダプタを選択
    → EmailNotificationAdapter / SlackNotificationAdapter / LineNotificationAdapter / DiscordNotificationAdapter
  → adapter.send(summary, decryptedConfig)
  → DeliveryLogRepository.save() [成功/失敗を記録]
  ← DeliveryResult
```

### 複数チャンネル並行送信（ダッシュボードからの即時送信）
```
Client → NotificationController.sendToMultiple(summaryId, channelIds)
  → NotificationService.sendToChannels(summary, channels)
    → channels.map { async { sendToChannel(summary, it) } } [並行実行]
  ← List<DeliveryResult>
```

### リトライ処理
```
Scheduler → NotificationService.retryFailedDeliveries()
  → DeliveryLogRepository.findRetryTargets() [FAILED かつ retryCount < 3]
  → 各ログに対して再送信（指数バックオフ）
  → 3回失敗後: ユーザーに送信失敗を通知
```

## 関連クラス（他ドメイン）
- `summary/Summary` — 送信対象の要約
- `user/EncryptionService` — チャンネル接続情報の暗号化・復号

## 設計判断

- **ChannelType を notification ドメインに配置**: 通知チャンネルの種別は notification の責務
- **暗号化設定（encryptedConfig）**: AES-256-GCM で暗号化してDB保存。復号は送信時のみ
- **並行送信**: `sendToChannels()` で複数チャンネルへの送信を並行実行。1チャンネルの失敗が他に影響しない
- **リトライ対象の取得**: `findRetryTargets()` で FAILED かつ retryCount < 3 のログを一括取得
- **ドメインモデルとJPAエンティティの分離**: ドメインモデルはJPAアノテーションを持たない純粋なオブジェクト。JpaEntityはインフラ層に配置し、RepositoryImplで変換を行う

## プロパティテスト（予定）
| テスト | 検証内容 |
|---|---|
| Property 5 | 送信処理後は必ずDeliveryLogが記録される |
| Property 6 | DBに保存された接続情報は平文を含まない |
