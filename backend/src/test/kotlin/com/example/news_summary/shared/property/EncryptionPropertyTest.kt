package com.example.news_summary.shared.property

import com.example.news_summary.shared.infrastructure.security.AesEncryptionService
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.StringLength
import org.assertj.core.api.Assertions.assertThat

/**
 * Feature: economic-news-ai-summarizer, Property 6: 接続情報の暗号化保存
 *
 * 任意のDeliveryChannel設定に対して、暗号化後の文字列が
 * 平文のAPIキー・Webhook URLを含まないことを検証する。
 */
class EncryptionPropertyTest {

    private val encryptionService = AesEncryptionService(
        encryptionKey = "test-encryption-key-32bytes-long!" // 32バイト
    )

    @Property(tries = 100)
    fun `暗号化後の文字列は平文を含まない`(
        @ForAll @StringLength(min = 5, max = 500) plainText: String
    ) {
        val encrypted = encryptionService.encrypt(plainText)

        // 暗号文はBase64エンコードされており、平文をそのまま含まない
        assertThat(encrypted).doesNotContain(plainText)
        // 暗号文は平文と異なる
        assertThat(encrypted).isNotEqualTo(plainText)
    }

    @Property(tries = 100)
    fun `暗号化と復号のラウンドトリップで元の平文に戻る`(
        @ForAll @StringLength(min = 1, max = 500) plainText: String
    ) {
        val encrypted = encryptionService.encrypt(plainText)
        val decrypted = encryptionService.decrypt(encrypted)

        assertThat(decrypted).isEqualTo(plainText)
    }

    @Property(tries = 100)
    fun `同じ平文でも暗号化のたびに異なる暗号文が生成される`(
        @ForAll @StringLength(min = 5, max = 200) plainText: String
    ) {
        val encrypted1 = encryptionService.encrypt(plainText)
        val encrypted2 = encryptionService.encrypt(plainText)

        // IVがランダムなので同じ平文でも異なる暗号文になる
        assertThat(encrypted1).isNotEqualTo(encrypted2)
    }
}
