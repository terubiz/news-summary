# User ドメイン

## 概要

ユーザー認証・認可を担う境界コンテキスト。メールアドレス+パスワードによるアカウント管理、JWT発行・検証、リフレッシュトークンローテーション、IPベースのログインブロック、APIレート制限を提供する。

**対応要件:** 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 8.3, 8.5

## クラス一覧

### model/
| クラス | 役割 |
|---|---|
| `UserId.kt` | 値オブジェクト。永続化済みエンティティのIDをnon-nullで保証 |
| `User.kt` | ドメインモデル（集約ルート）。JPAアノテーションなし。id: UserId |
| `RefreshToken.kt` | ドメインモデル。userId: UserId |

### repository/
| クラス | 役割 |
|---|---|
| `UserRepository.kt` | ドメイン層ポート（インターフェース）。ドメインモデルのみを扱う |
| `RefreshTokenRepository.kt` | ドメイン層ポート（インターフェース） |

### service/
| クラス | 役割 |
|---|---|
| `UserDomainService.kt` | ユーザー登録ドメインサービスインターフェース |
| `JwtService.kt` | JWTトークン生成・検証インターフェース |
| `PasswordService.kt` | bcryptハッシュ化・検証インターフェース |

## インフラ層（user/infrastructure/）

### persistence/
| クラス | 役割 |
|---|---|
| `UserJpaEntity.kt` | JPA用エンティティ（@Entity, id: Long? = null） |
| `RefreshTokenJpaEntity.kt` | JPA用エンティティ |
| `UserJpaRepository.kt` | Spring Data JPA リポジトリ（JpaEntity を扱う） |
| `RefreshTokenJpaRepository.kt` | Spring Data JPA リポジトリ |
| `UserRepositoryImpl.kt` | UserRepository実装。JpaEntity ↔ ドメインモデル変換。idのnullチェックはここで1箇所のみ |
| `RefreshTokenRepositoryImpl.kt` | RefreshTokenRepository実装 |

### security/
| クラス | 役割 |
|---|---|
| `JwtServiceImpl.kt` | jjwt使用。HS256署名、アクセストークン生成・検証 |
| `PasswordServiceImpl.kt` | BCryptPasswordEncoder使用 |
| `JwtAuthenticationFilter.kt` | BearerトークンをパースしてSecurityContextに設定 |
| `LoginAttemptService.kt` | IP別ログイン失敗カウンタ（5回失敗→15分ブロック） |
| `RateLimitFilter.kt` | 1分間60リクエスト超で429を返すスライディングウィンドウ |

## アプリケーション層（user/application/usecase/）
| クラス | 役割 |
|---|---|
| `RegisterUserUseCase.kt` | ユーザー登録（メール重複チェック→パスワードハッシュ化→保存） |
| `AuthenticateUserUseCase.kt` | ログイン（パスワード検証→JWT発行→リフレッシュトークン保存）・リフレッシュ（トークンローテーション） |

## API層（api/auth/）
| クラス | 役割 |
|---|---|
| `AuthController.kt` | POST /api/v1/auth/register, login, refresh |

## 機能別処理フロー

### ユーザー登録
```
Client → AuthController.register()
  → RegisterUserUseCase.execute()
    → UserRepository.existsByEmail() [重複チェック]
    → PasswordService.hash() [bcryptハッシュ化]
    → UserRepository.save()
  ← 201 Created
```

### ログイン
```
Client → AuthController.login()
  → LoginAttemptService.isBlocked(ip) [IPブロックチェック]
  → AuthenticateUserUseCase.login()
    → UserRepository.findByEmail()
    → PasswordService.verify() [パスワード照合]
    → JwtService.generateAccessToken()
    → JwtService.generateRefreshToken()
    → RefreshTokenRepository.save() [SHA-256ハッシュで保存]
  ← 200 OK { accessToken, refreshToken }
  ※失敗時: LoginAttemptService.recordFailure(ip) → 401
```

### トークンリフレッシュ
```
Client → AuthController.refresh()
  → AuthenticateUserUseCase.refresh()
    → RefreshTokenRepository.findByTokenHash() [SHA-256照合]
    → 有効期限チェック
    → 旧トークン削除（ローテーション）
    → 新アクセストークン + 新リフレッシュトークン発行
  ← 200 OK { accessToken, refreshToken }
```

### JWT認証フィルタ（全リクエスト共通）
```
Request → RateLimitFilter [60req/min チェック]
  → JwtAuthenticationFilter
    → Authorization: Bearer {token} をパース
    → JwtService.isTokenValid()
    → SecurityContext に userId, email を設定
  → Controller
```

## 設計判断

- **リフレッシュトークンローテーション**: 旧トークンを削除して新トークンを発行する。トークン漏洩時の被害を最小化
- **SHA-256ハッシュ保存**: リフレッシュトークンは平文でDBに保存しない。漏洩時にトークンを再利用できないようにする
- **LoginAttemptService**: ConcurrentHashMapでインメモリ管理。スケールアウト時はRedisに移行する想定
- **エラーメッセージ統一**: ログイン失敗時は「メールアドレスまたはパスワードが正しくありません」で統一し、アカウント存在の有無を隠蔽（要件7.3）

## プロパティテスト
| テスト | 検証内容 |
|---|---|
| `JwtAuthPropertyTest` | Property 8: 任意の無効トークンでSecurityContextに認証情報が設定されない |
| `RateLimitPropertyTest` | Property 9: 60回超のリクエストは429を返す |
