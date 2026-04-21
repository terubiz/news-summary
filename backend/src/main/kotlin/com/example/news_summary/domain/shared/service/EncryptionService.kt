package com.example.news_summary.domain.shared.service

/**
 * 暗号化サービス（共有ドメインサービス）
 * 通知チャンネルの接続情報等、機密データの暗号化・復号を担う。
 * 複数ドメインから利用されるため shared/ に配置。
 */
interface EncryptionService {
    /** 平文を暗号化する。戻り値はBase64エンコードされた暗号文（IV含む） */
    fun encrypt(plainText: String): String

    /** 暗号文を復号する。入力はBase64エンコードされた暗号文（IV含む） */
    fun decrypt(cipherText: String): String
}
