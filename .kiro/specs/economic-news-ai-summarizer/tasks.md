# 実装計画: Economic News AI Summarizer

## 概要

本ドキュメントは、経済ニュースAI要約アプリケーションの実装タスクを定義する。バックエンド（Kotlin + Spring Boot）のドメイン層・インフラ層・APIから始め、フロントエンド（React + TypeScript）、スケジューラ、通知機能の順に実装する。各タスクは前のタスクの成果物を前提として積み上げる構成とする。

## タスク

- [x] 1. プロジェクト基盤のセットアップ
  - バックエンド: Spring Boot 3.x + Kotlin プロジェクト構造を作成する（`com.example.economicnews` パッケージ、`api/`・`domain/`・`infrastructure/`・`scheduler/` レイヤー）
  - フロントエンド: Vite + React 18 + TypeScript プロジェクトを作成し、TanStack Query・Zustand・React Router を追加する
  - Docker Compose ファイルを作成する（PostgreSQL 15、バックエンド、フロントエンドサービス）
  - `build.gradle.kts` に依存関係を追加する（Spring Boot Web・Security・Data JPA・Mail・AI・jjwt・jqwik・Testcontainers・WireMock・Flyway）
  - _要件: 7.1, 7.2, 8.1_

- [-] 2. データベーススキーマとJPAエンティティの実装
  - [ ] 2.1 Flywayマイグレーションファイルを作成する
    - `V1__create_users.sql`・`V2__create_summary_settings.sql`・`V3__create_collection_schedules.sql`・`V4__create_news_articles.sql`・`V5__create_index_data.sql`・`V6__create_summaries.sql`・`V7__create_summary_relations.sql`・`V8__create_delivery_channels.sql`・`V9__create_delivery_logs.sql`・`V10__create_collection_logs.sql`・`V11__create_refresh_tokens.sql` を作成する
    - `source_url` カラムに UNIQUE 制約を追加する（重複排除のため）
    - _要件: 1.3, 1.5, 1.6, 2.4, 5.9_

  - [ ] 2.2 JPAエンティティクラスを実装する
    - `User`・`SummarySettings`・`CollectionSchedule`・`NewsArticle`・`IndexData`・`Summary`・`SummaryIndexImpact`・`DeliveryChannel`・`DeliveryLog`・`CollectionLog`・`RefreshToken` エンティティを作成する
    - `summaries.status`・`supplement_level`・`summary_mode`・`channel_type` の Kotlin enum を定義する
    - _要件: 1.3, 2.4, 3.1, 5.9_

  - [ ] 2.3 Spring Data JPA リポジトリを実装する
    - 各エンティティに対応するリポジトリインターフェースを作成する
    - `NewsArticleRepository.existsBySourceUrlOrTitle()`・`SummaryRepository.findByUserIdAndGeneratedAtAfter()`・`DeliveryLogRepository.findByChannelIdAndSummaryId()` 等のカスタムクエリメソッドを追加する
    - _要件: 1.5, 4.1, 4.3, 5.9_

- [ ] 3. ユーザー認証・認可の実装
  - [ ] 3.1 JWT サービスとパスワードハッシュを実装する
    - `JwtService`（トークン生成・検証・有効期限チェック）を実装する
    - `UserDetailsService` 実装と bcrypt パスワードエンコーダを設定する
    - リフレッシュトークンの生成・保存・検証ロジックを実装する
    - _要件: 7.2, 7.5, 7.6_

  - [ ] 3.2 Spring Security 設定とレート制限を実装する
    - JWT フィルタチェーンを設定し、保護エンドポイントと公開エンドポイントを定義する
    - IP アドレスベースのログイン失敗カウンタと15分ブロック機能を実装する（`RateLimiter`）
    - 1分間60リクエスト制限のレート制限フィルタを実装する
    - _要件: 7.3, 7.4, 7.7, 8.3, 8.5_

  - [ ] 3.3 認証 API コントローラを実装する
    - `POST /api/v1/auth/register`・`POST /api/v1/auth/login`・`POST /api/v1/auth/refresh` エンドポイントを実装する
    - グローバル例外ハンドラ（`@ControllerAdvice`）を実装し、統一エラーレスポンス形式を返す
    - _要件: 7.1, 7.2, 7.3, 7.6_

  - [ ]* 3.4 JWT認証のプロパティテストを書く
    - **プロパティ 8: JWT認証の排他性**
    - **検証対象: 要件 7.4, 8.3**
    - 任意の無効なJWTトークン文字列に対して、保護エンドポイントが401を返すことを検証する

  - [ ]* 3.5 レート制限のプロパティテストを書く
    - **プロパティ 9: レート制限の一貫性**
    - **検証対象: 要件 8.5**
    - 1分間に60回を超えるリクエストに対して、超過分がすべて429を返すことを検証する

- [ ] 4. チェックポイント - 認証基盤の確認
  - すべてのテストが通過することを確認し、疑問点があればユーザーに確認する。

- [ ] 5. 暗号化サービスと送信チャンネル設定の実装
  - [ ] 5.1 暗号化サービスを実装する
    - AES-256-GCM を使用した `EncryptionService`（暗号化・復号）を実装する
    - `DeliveryChannel.encrypted_config` の保存・取得時に自動的に暗号化・復号する仕組みを実装する
    - _要件: 6.6_

  - [ ]* 5.2 暗号化のプロパティテストを書く
    - **プロパティ 6: 接続情報の暗号化保存**
    - **検証対象: 要件 6.6**
    - 任意のチャンネル設定に対して、DB に保存された `encrypted_config` が平文のAPIキー・Webhook URLを含まないことを検証する

  - [ ] 5.3 通知設定 API コントローラを実装する
    - `GET/POST /api/v1/channels`・`PUT/DELETE /api/v1/channels/{id}`・`POST /api/v1/channels/{id}/test` エンドポイントを実装する
    - チャンネル追加時の接続テスト実行と結果返却ロジックを実装する
    - 無効な接続情報に対する具体的エラーメッセージ返却を実装する
    - _要件: 6.1, 6.2, 6.3, 6.4, 6.5, 6.7_

- [ ] 6. 要約設定 API の実装
  - [ ] 6.1 要約設定サービスと API を実装する
    - `SummarySettingsService`（設定取得・保存）を実装する
    - `GET /api/v1/settings/summary`・`PUT /api/v1/settings/summary` エンドポイントを実装する
    - `GET /api/v1/settings/schedule`・`PUT /api/v1/settings/schedule` エンドポイントを実装する
    - 選択可能な分析観点（`INTEREST_RATE`・`GEOPOLITICAL_RISK` 等8種 + `CUSTOM`）の定義を実装する
    - _要件: 9.1, 9.2, 9.3, 9.4, 9.6, 9.7, 9.9, 9.10_

  - [ ]* 6.2 要約設定反映のプロパティテストを書く
    - **プロパティ 7: 要約設定の反映**
    - **検証対象: 要件 9.7, 9.8**
    - 任意の `SummaryMode` と `SupplementLevel` の組み合わせに対して、保存後に生成された要約が設定パラメータを反映していることを検証する

- [ ] 7. ニュース収集機能の実装
  - [ ] 7.1 NewsAPI クライアントを実装する
    - `NewsApiClient`（NewsAPI / GNews API への HTTP クライアント）を実装する
    - 接続失敗時のエラーログ記録と他ソース継続処理を実装する
    - _要件: 1.2, 1.4_

  - [ ] 7.2 ニュース収集サービスを実装する
    - `NewsCollectorService.collectNews()` を実装する（記事取得・重複チェック・DB保存・収集ログ記録）
    - `isDuplicate()` メソッドを実装する（同一 URL または同一タイトルの判定）
    - 収集完了後に AI 要約処理をトリガーするロジックを実装する
    - _要件: 1.2, 1.3, 1.4, 1.5, 1.6, 1.7_

  - [ ]* 7.3 ニュース記事重複排除のプロパティテストを書く
    - **プロパティ 1: ニュース記事の重複排除**
    - **検証対象: 要件 1.5**
    - 任意のニュース記事コレクションに対して、同一 URL または同一タイトルの記事が1件のみ保存されることを検証する

- [ ] 8. 株価指数データ取得機能の実装
  - [ ] 8.1 株価指数クライアントとサービスを実装する
    - `StockApiClient`（Alpha Vantage API / Yahoo Finance API への HTTP クライアント）を実装する
    - `IndexAnalyzerService.fetchLatestIndices()` を実装する（日経225・S&P500・NASDAQ・DAX の取得）
    - `getCachedIndices()` を実装する（API 失敗時のキャッシュ返却と `is_stale=true` フラグ付与）
    - 市場閉場中の直近終値返却ロジックを実装する
    - _要件: 2.1, 2.2, 2.3, 2.4, 2.5_

  - [ ] 8.2 株価指数 API コントローラを実装する
    - `GET /api/v1/indices` エンドポイントを実装する
    - _要件: 2.1, 4.4_

- [ ] 9. AI要約生成機能の実装
  - [ ] 9.1 プロンプトビルダーを実装する
    - `SummaryPromptBuilder.build()` を実装する（補足レベル・文字数モード・分析観点・用語解説指示を動的に組み立て）
    - 補足レベル別のプロンプト指示文（BEGINNER・INTERMEDIATE・ADVANCED）を実装する
    - 用語解説セクション付加指示（ADVANCED 以外）を実装する
    - _要件: 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10, 3.11, 3.12_

  - [ ] 9.2 AI要約サービスを実装する
    - `AISummarizerService.summarize()` を Spring AI `ChatClient` を使って実装する
    - 複数記事の統合要約ロジックを実装する
    - 要約結果（`Summary`・`SummaryIndexImpact`・`SummarySourceArticle`）の DB 保存を実装する
    - LLM API 失敗時のエラーログ記録・ステータス `FAILED` 保存・最大3回リトライ（指数バックオフ）を実装する
    - `retryFailedSummaries()` を実装する
    - _要件: 3.1, 3.2, 3.13, 3.14, 3.15, 3.16, 9.8_

  - [ ]* 9.3 要約文字数制約のプロパティテストを書く
    - **プロパティ 2: 要約文字数制約**
    - **検証対象: 要件 3.4, 9.6**
    - 任意のニュース記事セットと `SummaryMode` に対して、要約本文の文字数が設定モードの上限（SHORT: 150字・STANDARD: 300字・DETAILED: 600字）を超えないことを検証する

  - [ ]* 9.4 上級者向け用語解説省略のプロパティテストを書く
    - **プロパティ 3: 上級者向け設定での用語解説省略**
    - **検証対象: 要件 3.12**
    - 任意のニュース記事セットに対して、`ADVANCED` 設定では要約テキストに「用語解説」セクションが含まれないことを検証する

  - [ ]* 9.5 用語解説文字数制約のプロパティテストを書く
    - **プロパティ 4: 用語解説の文字数制約**
    - **検証対象: 要件 3.10**
    - 任意のニュース記事セットに対して、用語解説セクションの各項目説明文が50文字以内であることを検証する

  - [ ] 9.6 要約 API コントローラを実装する
    - `GET /api/v1/summaries`（ページネーション・Stock_Index フィルタ・キーワード検索）を実装する
    - `GET /api/v1/summaries/{id}` を実装する
    - _要件: 4.1, 4.2, 4.3, 4.5, 4.6, 8.1, 8.2, 8.4_

- [ ] 10. チェックポイント - バックエンドコア機能の確認
  - すべてのテストが通過することを確認し、疑問点があればユーザーに確認する。

- [ ] 11. SSEリアルタイム更新の実装
  - [ ] 11.1 SSE エンドポイントと発行サービスを実装する
    - `SsePublisher.publishSummaryCreated()` と `subscribe()` を実装する
    - `GET /api/v1/summaries/stream` SSE エンドポイントを実装する
    - 新しい要約が DB に保存された後に SSE イベントを発行するフックを `AISummarizerService` に追加する
    - _要件: 4.7_

- [ ] 12. 通知送信機能の実装
  - [ ] 12.1 チャンネル別送信アダプタを実装する
    - `EmailNotificationAdapter`（Spring Mail / SendGrid API）を実装する
    - `SlackNotificationAdapter`（Incoming Webhook HTTP POST）を実装する
    - `LineNotificationAdapter`（LINE Messaging API）を実装する
    - `DiscordNotificationAdapter`（Discord Webhook HTTP POST）を実装する
    - 各アダプタで送信メッセージに要約テキスト・関連 Stock_Index・影響方向・ソースURLを含める
    - _要件: 5.1, 5.6, 5.10, 5.11, 5.12, 5.13_

  - [ ] 12.2 通知サービスを実装する
    - `NotificationService.sendToChannel()` と `sendToChannels()` を実装する（並行送信）
    - 送信失敗時のエラーログ記録・最大3回リトライ（指数バックオフ）を実装する
    - 3回失敗後のユーザー通知ロジックを実装する
    - 送信ログ（`DeliveryLog`）の記録を実装する
    - `retryFailedDeliveries()` を実装する
    - _要件: 5.3, 5.5, 5.7, 5.8, 5.9_

  - [ ]* 12.3 通知送信ログ完全性のプロパティテストを書く
    - **プロパティ 5: 通知送信ログの完全性**
    - **検証対象: 要件 5.7**
    - 任意のチャンネルと要約の組み合わせに対して、送信処理（成功・失敗を問わず）後に必ず `DeliveryLog` レコードが記録されることを検証する

- [ ] 13. スケジューラの実装
  - [ ] 13.1 ニュース収集ジョブを実装する
    - `NewsCollectionJob`（`@Scheduled` + Quartz）を実装する
    - ユーザーごとの `CollectionSchedule.cron_expression` に基づいてジョブをスケジュールする
    - 収集完了後に AI 要約処理を自動トリガーするロジックを実装する
    - _要件: 1.1, 1.2, 1.7_

  - [ ] 13.2 通知送信ジョブを実装する
    - `NotificationDeliveryJob`（`@Scheduled`）を実装する
    - 各チャンネルの `delivery_schedule` 設定（即時・毎時・毎日指定時刻）に基づいて送信をトリガーする
    - フィルタ条件（対象 Stock_Index）に合致する要約のみを送信対象とする
    - _要件: 5.2, 5.3, 6.3, 6.4_

- [ ] 14. チェックポイント - バックエンド全体の確認
  - すべてのテストが通過することを確認し、疑問点があればユーザーに確認する。

- [ ] 15. フロントエンド: 認証画面の実装
  - [ ] 15.1 認証ページコンポーネントを実装する
    - `LoginPage.tsx`（メールアドレス・パスワードフォーム、バリデーション）を実装する
    - 会員登録フォームを実装する
    - JWT トークンの保存・リフレッシュ処理を実装する（`useAuth` フック）
    - Zustand の `settingsStore` に認証状態を追加する
    - `vercel-ui-skills` を参照してクリーンなライトモードUIを適用する
    - _要件: 7.1, 7.2, 7.3_

- [ ] 16. フロントエンド: ダッシュボード画面の実装
  - [ ] 16.1 株価指数ティッカーコンポーネントを実装する
    - `IndexTicker.tsx`（現在値・前日比・前日比率の表示）を実装する
    - `useIndexData.ts` フック（TanStack Query）を実装する
    - `stripe-ui-skills` を参照して `tabular-nums`・上昇/下落カラートークンを適用する
    - _要件: 4.4_

  - [ ] 16.2 要約一覧・カードコンポーネントを実装する
    - `SummaryList.tsx`・`SummaryCard.tsx`（要約テキスト・関連 Stock_Index・影響方向・生成日時・ソースURL表示）を実装する
    - `SummaryFilter.tsx`（Stock_Index フィルタ・キーワード検索）を実装する
    - `useSummaries.ts` フック（TanStack Query、ページネーション・フィルタ対応）を実装する
    - `linear-ui-skills` を参照してダークモードのデータ密度の高いレイアウトを適用する
    - _要件: 4.1, 4.2, 4.3, 4.5, 4.6_

  - [ ] 16.3 SSEリアルタイム更新フックを実装する
    - `useSseStream.ts` フックを実装する（SSE 接続・イベント受信・TanStack Query キャッシュ無効化）
    - `DashboardPage.tsx` に `useSseStream` を組み込み、新しい要約が生成されると自動更新する
    - バックエンドAPI接続失敗時のエラーメッセージ表示と最終取得データ維持を実装する
    - _要件: 4.7, 4.8_

- [ ] 17. フロントエンド: 要約設定画面の実装
  - [ ] 17.1 要約設定フォームを実装する
    - `SummarySettingsPage.tsx`・`SummarySettingsForm.tsx` を実装する
    - Stock_Index 複数選択（日経225・S&P500・NASDAQ・DAX）を実装する
    - 分析観点の複数選択（8種 + カスタム項目追加）を実装する
    - 補足レベル選択（3段階、各レベルの説明文表示）を実装する
    - 文字数モード選択（短め・標準・詳細）を実装する
    - 現在有効な設定の常時表示を実装する
    - `vercel-ui-skills` を参照してミニマルなライトモードUIを適用する
    - _要件: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.9, 9.10, 9.11_

- [ ] 18. フロントエンド: 通知設定画面の実装
  - [ ] 18.1 送信チャンネル設定フォームを実装する
    - `NotificationSettingsPage.tsx`・`ChannelSettingsForm.tsx` を実装する
    - チャンネル追加・編集・削除 UI を実装する（メール・Slack・LINE・Discord）
    - 接続テスト実行と結果表示を実装する
    - 送信スケジュール設定（即時・毎時・毎日指定時刻）を実装する
    - 対象 Stock_Index フィルタ設定を実装する
    - `vercel-ui-skills` を参照してミニマルなライトモードUIを適用する
    - _要件: 6.1, 6.2, 6.3, 6.4, 6.5, 6.7_

  - [ ] 18.2 複数送信先選択UIを実装する
    - `ChannelMultiSelector.tsx`（複数チャンネル選択・即時送信）を実装する
    - ダッシュボードから要約を即時送信する機能を実装する
    - _要件: 5.4, 5.5_

- [ ] 19. チェックポイント - フロントエンド全体の確認
  - すべてのテストが通過することを確認し、疑問点があればユーザーに確認する。

- [ ] 20. フロントエンドコード品質診断
  - [ ] 20.1 react-doctor でコード品質を検証する
    - `npx -y react-doctor@latest .` を実行してコード品質スコアを確認する
    - スコア75以上を目標とし、指摘された問題を修正する
    - _要件: 4.1, 4.7_

- [ ] 21. 統合テストの実装
  - [ ]* 21.1 ニュース収集エンドツーエンド統合テストを書く
    - Testcontainers（PostgreSQL）+ WireMock（NewsAPI モック）を使ったエンドツーエンドテストを実装する
    - 収集ジョブ起動 → 記事取得 → 重複排除 → DB 保存 → AI 要約トリガーの一連フローを検証する
    - _要件: 1.2, 1.3, 1.5, 1.7_

  - [ ]* 21.2 LLM API リトライ統合テストを書く
    - WireMock で OpenAI API の失敗・成功シナリオをモックし、最大3回リトライと指数バックオフを検証する
    - _要件: 3.13, 3.14_

  - [ ]* 21.3 通知送信統合テストを書く
    - WireMock で Slack・LINE・Discord・Email の送信エンドポイントをモックし、並行送信・リトライ・ログ記録を検証する
    - _要件: 5.5, 5.7, 5.8, 5.9_

  - [ ]* 21.4 SSEストリーム統合テストを書く
    - SSE エンドポイントへの接続・新規要約イベント受信・切断処理を検証する
    - _要件: 4.7_

- [ ] 22. 最終チェックポイント - 全テスト通過の確認
  - すべてのテストが通過することを確認し、疑問点があればユーザーに確認する。

## 注意事項

- `*` が付いたサブタスクはオプションであり、MVP を優先する場合はスキップ可能
- 各タスクは対応する要件番号を参照しており、トレーサビリティを確保している
- プロパティテストは `@Property(tries = 100)` で最低100回のイテレーションを実行する
- バックエンド実装時は `.kiro/skills/spring-boot-kotlin/`・`.kiro/skills/spring-ai-integration/`・`.kiro/skills/postgresql-jpa/`・`.kiro/skills/junit5-property-testing/` を参照する
- フロントエンド実装時は `.kiro/skills/react-typescript/`・`.kiro/skills/linear-ui-skills/`・`.kiro/skills/vercel-ui-skills/`・`.kiro/skills/stripe-ui-skills/` を参照する
- チェックポイントでは `./gradlew test`（バックエンド）と `npm run test -- --run`（フロントエンド）を実行して全テストの通過を確認する
