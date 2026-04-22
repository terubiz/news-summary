package com.example.news_summary.shared.infrastructure.security

import com.example.news_summary.domain.shared.service.EncryptionService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM 暗号化サービス実装。
 *
 * 暗号文フォーマット: Base64( IV(12bytes) + cipherText + authTag(16bytes) )
 * IVは暗号化のたびにランダム生成し、暗号文の先頭に付加する。
 * これにより同じ平文でも毎回異なる暗号文が生成される。
 */
@Service
class AesEncryptionService(
    @Value("\${app.encryption.key}") private val encryptionKey: String
) : EncryptionService {

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12  // GCM推奨IV長
        private const val TAG_LENGTH = 128 // 認証タグ長（ビット）
        private const val KEY_LENGTH = 32  // AES-256 = 32バイト
    }

    private val secretKey: SecretKeySpec by lazy {
        val keyBytes = encryptionKey.toByteArray(Charsets.UTF_8)
        require(keyBytes.size >= KEY_LENGTH) {
            "暗号化キーは${KEY_LENGTH}バイト以上必要です（現在: ${keyBytes.size}バイト）"
        }
        SecretKeySpec(keyBytes.copyOf(KEY_LENGTH), "AES")
    }

    override fun encrypt(plainText: String): String {
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(ALGORITHM).apply {
            init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH, iv))
        }
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // IV + 暗号文を結合してBase64エンコード
        val combined = iv + encrypted
        return Base64.getEncoder().encodeToString(combined)
    }

    override fun decrypt(cipherText: String): String {
        val combined = Base64.getDecoder().decode(cipherText)
        require(combined.size > IV_LENGTH) { "暗号文が不正です（長さ不足）" }

        val iv = combined.copyOfRange(0, IV_LENGTH)
        val encrypted = combined.copyOfRange(IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance(ALGORITHM).apply {
            init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH, iv))
        }
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }
}
