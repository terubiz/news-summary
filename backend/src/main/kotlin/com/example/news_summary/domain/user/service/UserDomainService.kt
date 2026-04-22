package com.example.news_summary.domain.user.service

import com.example.news_summary.domain.user.model.User

/**
 * ユーザードメインサービス
 * ユーザー登録・認証に関するドメインロジックを定義する
 */
interface UserDomainService {
    /** メールアドレスの重複チェック */
    fun isEmailTaken(email: String): Boolean

    /** パスワードをbcryptでハッシュ化する */
    fun hashPassword(rawPassword: String): String

    /** パスワードを検証する */
    fun verifyPassword(rawPassword: String, hashedPassword: String): Boolean

    /** ユーザーを登録する */
    fun register(email: String, rawPassword: String): User
}
