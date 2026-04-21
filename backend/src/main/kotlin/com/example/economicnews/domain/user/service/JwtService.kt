package com.example.economicnews.domain.user.service

/**
 * JWT トークン管理サービス（ドメインサービス）
 * アクセストークン・リフレッシュトークンの生成・検証を担う
 */
interface JwtService {
    /** アクセストークンを生成する */
    fun generateAccessToken(userId: Long, email: String): String

    /** リフレッシュトークンを生成する（ランダム文字列） */
    fun generateRefreshToken(): String

    /** トークンからユーザーIDを取得する */
    fun extractUserId(token: String): Long

    /** トークンからメールアドレスを取得する */
    fun extractEmail(token: String): String

    /** トークンが有効かどうか検証する */
    fun isTokenValid(token: String): Boolean

    /** トークンの有効期限が切れているか確認する */
    fun isTokenExpired(token: String): Boolean
}
