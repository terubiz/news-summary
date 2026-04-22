package com.example.news_summary.domain.user.service

/**
 * パスワード管理サービス（ドメインサービス）
 * bcrypt によるハッシュ化・検証を担う
 */
interface PasswordService {
    /** パスワードを bcrypt でハッシュ化する */
    fun hash(rawPassword: String): String

    /** 平文パスワードとハッシュを照合する */
    fun verify(rawPassword: String, hashedPassword: String): Boolean
}
