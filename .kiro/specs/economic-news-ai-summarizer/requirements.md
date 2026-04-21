# 要件定義ドキュメント

## はじめに

本ドキュメントは、世界の経済ニュースをAIが要約し、日経225やS&P500などの株価指数の値動き要因と関連付けて提示するWebアプリケーションの要件を定義する。ユーザーは最新の経済ニュースを株価への影響という観点から効率的に把握でき、要約結果をメール・Slack・LINEなどの外部サービスへ送信することができる。

フロントエンドはReact、バックエンドはKotlin + Spring Bootで構築し、AI要約機能はLLM APIを利用する。

---

## 用語集

- **System**: 本アプリケーション全体（Economic News AI Summarizer）
- **News_Collector**: 外部ニュースソースから経済ニュースを収集するバックエンドコンポーネント
- **AI_Summarizer**: LLM APIを呼び出してニュースを要約するバックエンドコンポーネント
- **Index_Analyzer**: 株価指数データを取得し、値動き要因を分析するバックエンドコンポーネント
- **Notification_Service**: 要約結果を外部サービスへ送信するバックエンドコンポーネント
- **Dashboard**: ユーザーが要約結果を閲覧するReactフロントエンド画面
- **Stock_Index**: 日経225、S&P500、NASDAQ、DAXなどの株価指数
- **Summary**: AI_Summarizerが生成した、ニュースと株価指数の値動き要因を含む要約テキスト
- **Delivery_Channel**: 要約結果の送信先（メール、Slack、LINE、Discord等）
- **User**: 本アプリケーションを利用するエンドユーザー

---

## 要件

### 要件1: 経済ニュースの収集

**ユーザーストーリー:** Userとして、設定した時刻に自動的に最新の世界経済ニュースを検索・収集してほしい。そうすることで、複数のニュースソースを手動で確認する手間を省くことができる。

#### 受け入れ基準

1. THE System SHALL ユーザーがニュース収集を実行する時刻（例：毎日8:00、12:00、18:00）を設定できる機能を提供する。
2. WHEN 設定された収集時刻が到来したとき、THE News_Collector SHALL ニュース検索APIを呼び出して最新の経済ニュースを検索・取得する。
3. WHEN ニュース記事が取得されたとき、THE News_Collector SHALL 記事タイトル・本文・公開日時・ソースURLをデータベースに保存する。
4. IF ニュースソースへの接続が失敗したとき、THEN THE News_Collector SHALL エラーをログに記録し、他のニュースソースの収集処理を継続する。
5. THE News_Collector SHALL 重複する記事（同一URLまたは同一タイトル）をデータベースに保存しない。
6. THE News_Collector SHALL 収集実行日時・取得件数・成否をデータベースに記録する。
7. WHEN ニュース収集が完了したとき、THE News_Collector SHALL 収集した記事に対してAI要約処理を自動的にトリガーする。

---

### 要件2: 株価指数データの取得

**ユーザーストーリー:** Userとして、日経225やS&P500などの株価指数の最新データを参照してほしい。そうすることで、ニュースと株価の動きを関連付けて理解できる。

#### 受け入れ基準

1. THE Index_Analyzer SHALL 日経225、S&P500、NASDAQ Composite、DAXの現在値・前日比・前日比率を取得する。
2. WHEN 株価指数データの取得が要求されたとき、THE Index_Analyzer SHALL 外部株価データAPIから最新の指数データを取得する。
3. IF 株価データAPIへの接続が失敗したとき、THEN THE Index_Analyzer SHALL 最後に取得した有効なデータをキャッシュから返し、データが古いことをレスポンスに含める。
4. THE Index_Analyzer SHALL 取得した株価指数データを取得日時とともにデータベースに保存する。
5. WHEN 市場が閉場中のとき、THE Index_Analyzer SHALL 直近の終値データを返す。

---

### 要件3: AIによるニュース要約と株価影響分析

**ユーザーストーリー:** Userとして、経済ニュースを株価指数の値動き要因と関連付けてAIが要約してほしい。また、設定した補足レベルと文字数に応じた要約を生成してほしい。そうすることで、自分の知識レベルや用途に合った形で市場への影響を把握できる。

#### 受け入れ基準

1. WHEN 新しいニュース記事が収集されたとき、THE AI_Summarizer SHALL LLM APIを呼び出してニュースの要約を生成する。
2. THE AI_Summarizer SHALL 要約に、対象ニュースが影響を与える可能性のある Stock_Index の名称と影響方向（上昇要因・下落要因・中立）を含める。
3. THE AI_Summarizer SHALL 要約テキストを日本語で生成する。
4. THE AI_Summarizer SHALL 管理画面で設定された文字数モードに従い、要約本文の文字数を制御する。
5. THE AI_Summarizer SHALL 管理画面で設定された補足レベルに従い、要約の説明深度を変える。
6. WHERE 補足レベルが「初心者向け」に設定されているとき、THE AI_Summarizer SHALL 経済の基本的な因果関係（例：「金利が上がると借入コストが増加し企業収益が圧迫されるため株価が下がりやすい」）を要約本文内に含める。
7. WHERE 補足レベルが「中級者向け」に設定されているとき、THE AI_Summarizer SHALL 基本的な因果関係の説明は省略し、指数への影響分析と市場背景の説明を中心とした要約を生成する。
8. WHERE 補足レベルが「上級者向け」に設定されているとき、THE AI_Summarizer SHALL 因果関係や用語説明を省略し、指数への影響と要因の簡潔な分析のみを含む要約を生成する。
9. THE AI_Summarizer SHALL 要約本文中に登場する経済・株式の専門用語を検出し、各用語の平易な説明を要約末尾に「用語解説」セクションとして付加する。
10. THE AI_Summarizer SHALL 用語解説の各項目を「【用語名】: 説明文」の形式で記載し、1用語あたり50文字以内で説明する。
11. IF 要約本文中に専門用語が存在しないとき、THEN THE AI_Summarizer SHALL 用語解説セクションを付加しない。
12. WHERE 補足レベルが「上級者向け」に設定されているとき、THE AI_Summarizer SHALL 用語解説セクションを付加しない。
13. IF LLM APIの呼び出しが失敗したとき、THEN THE AI_Summarizer SHALL エラーをログに記録し、該当記事の要約ステータスを「失敗」として保存する。
14. WHEN 要約生成が失敗した記事が存在するとき、THE AI_Summarizer SHALL 最大3回まで再試行する。
15. THE AI_Summarizer SHALL 要約結果に、参照したニュース記事のソースURLを含める。
16. WHERE 複数のニュース記事が同一トピックに関連するとき、THE AI_Summarizer SHALL それらを統合した単一の要約を生成する。

---

### 要件4: 管理画面（ダッシュボード）

**ユーザーストーリー:** Userとして、AIが生成した要約の閲覧と、要約に関わる各種設定を一つの管理画面で行いたい。そうすることで、自分のニーズに合わせた経済分析を効率的に受け取ることができる。

#### 受け入れ基準

1. THE Dashboard SHALL 最新の Summary を新しい順に一覧表示する。
2. THE Dashboard SHALL 各 Summary に対して、要約テキスト・関連 Stock_Index・影響方向・生成日時・ソースURLを表示する。
3. WHEN ユーザーがダッシュボードを開いたとき、THE Dashboard SHALL 直近24時間以内に生成された Summary を表示する。
4. THE Dashboard SHALL 各 Stock_Index の現在値・前日比・前日比率をダッシュボード上部に表示する。
5. WHEN ユーザーが Stock_Index でフィルタリングを指定したとき、THE Dashboard SHALL 指定された Stock_Index に関連する Summary のみを表示する。
6. WHEN ユーザーがキーワード検索を実行したとき、THE Dashboard SHALL 要約テキストにキーワードを含む Summary を表示する。
7. THE Dashboard SHALL 新しい Summary が生成されたとき、ページを手動でリロードすることなく表示を更新する。
8. IF バックエンドAPIへの接続が失敗したとき、THEN THE Dashboard SHALL エラーメッセージをユーザーに表示し、最後に取得したデータを表示し続ける。

---

### 要件9: 要約設定の管理

**ユーザーストーリー:** Userとして、AIが要約を生成する際に参照する指数・分析観点・補足レベル・文字数を管理画面から自由に設定したい。そうすることで、自分の知識レベルや用途に合わせた要約を受け取ることができる。

#### 受け入れ基準

1. THE Dashboard SHALL ユーザーが要約に使用する Stock_Index（日経225、S&P500、NASDAQ、DAX等）を複数選択できる設定画面を提供する。
2. THE Dashboard SHALL ユーザーが要約に絡める分析観点を複数選択できる設定画面を提供する。
3. THE System SHALL 選択可能な分析観点として少なくとも以下を提供する：金利・中央銀行政策、地政学リスク・戦争・紛争、有力者・政治家の発言、企業決算・業績、為替・通貨政策、エネルギー・資源価格、インフレ・物価指標、雇用統計・経済指標。
4. THE Dashboard SHALL ユーザーが要約の補足レベルを以下の3段階から選択できる設定を提供する：「初心者向け」・「中級者向け」・「上級者向け」。
5. THE Dashboard SHALL 各補足レベルの説明をUI上に表示する（例：初心者向け＝経済の基本的な因果関係を含む、中級者向け＝市場背景と影響分析を中心、上級者向け＝簡潔な分析のみ）。
6. THE Dashboard SHALL ユーザーが要約の文字数モードを以下のパターンから選択できる設定を提供する：「短め（150文字以内）」・「標準（300文字以内）」・「詳細（600文字以内）」。
7. WHEN ユーザーが要約設定を保存したとき、THE System SHALL 選択された Stock_Index・分析観点・補足レベル・文字数モードをデータベースに保存する。
8. WHEN AI_Summarizer が要約を生成するとき、THE AI_Summarizer SHALL 保存された設定に基づき、選択された Stock_Index・分析観点・補足レベル・文字数モードを反映した要約を生成する。
9. THE Dashboard SHALL ユーザーが分析観点のカスタム項目（任意のテキスト）を追加できる機能を提供する。
10. WHEN ユーザーが要約設定を変更したとき、THE System SHALL 次回のニュース収集・要約生成から新しい設定を適用する。
11. THE Dashboard SHALL 現在有効な要約設定（選択中の Stock_Index・分析観点・補足レベル・文字数モード）を常に画面上に表示する。

---

### 要件5: 要約結果の外部サービスへの送信

**ユーザーストーリー:** Userとして、AIが生成した要約をメール・Slack・LINE・Discordなどの外部サービスへ送信してほしい。また、ダッシュボードから送信先を複数選択して一括送信できるようにしてほしい。そうすることで、アプリを開かなくても経済動向を受け取ることができる。

#### 受け入れ基準

1. THE Notification_Service SHALL メール・Slack・LINE・Discordの各 Delivery_Channel へ Summary を送信する機能を提供する。
2. WHEN ユーザーが送信設定を保存したとき、THE System SHALL 指定された Delivery_Channel・送信スケジュール・フィルタ条件を保存する。
3. WHEN 送信スケジュールが到来したとき、THE Notification_Service SHALL 設定されたフィルタ条件に合致する Summary を指定の Delivery_Channel へ送信する。
4. THE Dashboard SHALL ユーザーが要約を即時送信する際に、送信先 Delivery_Channel を複数選択できる機能を提供する。
5. WHEN ユーザーが複数の Delivery_Channel を選択して送信を実行したとき、THE Notification_Service SHALL 選択されたすべての Delivery_Channel へ並行して Summary を送信する。
6. THE Notification_Service SHALL 送信するメッセージに、要約テキスト・関連 Stock_Index・影響方向・ソースURLを含める。
7. IF 外部サービスへの送信が失敗したとき、THEN THE Notification_Service SHALL エラーをログに記録し、最大3回まで再送信を試みる。
8. IF 3回の再送信がすべて失敗したとき、THEN THE Notification_Service SHALL ユーザーに送信失敗を通知する。
9. THE Notification_Service SHALL 各送信の実行日時・送信先・成否をデータベースに記録する。
10. WHERE Slack が Delivery_Channel として設定されているとき、THE Notification_Service SHALL Slack Incoming Webhook を使用してメッセージを送信する。
11. WHERE LINE が Delivery_Channel として設定されているとき、THE Notification_Service SHALL LINE Messaging API を使用してメッセージを送信する。
12. WHERE メールが Delivery_Channel として設定されているとき、THE Notification_Service SHALL SMTP または SendGrid API を使用してメールを送信する。
13. WHERE Discord が Delivery_Channel として設定されているとき、THE Notification_Service SHALL Discord Webhook を使用してメッセージを送信する。

---

### 要件6: 送信設定の管理

**ユーザーストーリー:** Userとして、外部サービスへの送信設定を管理したい。そうすることで、自分のニーズに合わせた通知を受け取ることができる。

#### 受け入れ基準

1. THE Dashboard SHALL ユーザーが Delivery_Channel（メール・Slack・LINE・Discord）の追加・編集・削除を行える設定画面を提供する。
2. WHEN ユーザーが新しい Delivery_Channel を追加したとき、THE System SHALL 接続テストを実行し、結果をユーザーに表示する。
3. THE System SHALL 各 Delivery_Channel に対して、送信スケジュール（即時・毎時・毎日指定時刻）を設定できる機能を提供する。
4. THE System SHALL 各 Delivery_Channel に対して、対象 Stock_Index によるフィルタ条件を設定できる機能を提供する。
5. IF ユーザーが無効な接続情報（Webhook URL、APIキー等）を入力したとき、THEN THE System SHALL 具体的なエラーメッセージを表示し、設定を保存しない。
6. THE System SHALL Delivery_Channel の接続情報（APIキー、Webhook URL等）を暗号化してデータベースに保存する。
7. WHERE Discord が Delivery_Channel として追加されるとき、THE System SHALL Discord Webhook URL を接続情報として受け付ける。

---

### 要件7: ユーザー認証と認可

**ユーザーストーリー:** Userとして、自分の設定とデータが保護されてほしい。そうすることで、他のユーザーに設定を変更されることなく安全に利用できる。

#### 受け入れ基準

1. THE System SHALL ユーザーがメールアドレスとパスワードでアカウントを作成できる機能を提供する。
2. WHEN ユーザーがログインを試みたとき、THE System SHALL 認証情報を検証し、有効な場合はJWTアクセストークンを発行する。
3. IF 無効な認証情報でログインが試みられたとき、THEN THE System SHALL 認証失敗のエラーメッセージを返し、アカウント情報を開示しない。
4. WHILE ユーザーが認証済みセッションを持つとき、THE System SHALL そのユーザー自身の送信設定と送信履歴のみへのアクセスを許可する。
5. THE System SHALL パスワードをbcryptアルゴリズムでハッシュ化してデータベースに保存する。
6. WHEN JWTアクセストークンの有効期限が切れたとき、THE System SHALL リフレッシュトークンを使用して新しいアクセストークンを発行する。
7. IF 同一IPアドレスから5回連続でログインに失敗したとき、THEN THE System SHALL そのIPアドレスからのログイン試行を15分間ブロックする。

---

### 要件8: APIの提供

**ユーザーストーリー:** Userとして、外部システムからも要約データにアクセスしたい。そうすることで、他のツールやシステムと連携できる。

#### 受け入れ基準

1. THE System SHALL RESTful APIエンドポイントを通じて Summary の一覧取得・個別取得を提供する。
2. THE System SHALL APIレスポンスをJSON形式で返す。
3. WHEN APIリクエストに有効なJWTトークンが含まれないとき、THE System SHALL HTTPステータスコード401を返す。
4. THE System SHALL 一覧取得APIにページネーション（page・size パラメータ）を実装する。
5. WHEN 同一クライアントから1分間に60回を超えるAPIリクエストが送信されたとき、THE System SHALL HTTPステータスコード429を返す。
