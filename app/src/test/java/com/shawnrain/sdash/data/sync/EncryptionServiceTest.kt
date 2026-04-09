package com.shawnrain.sdash.data.sync

import java.util.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.fail
import org.junit.Test

class EncryptionServiceTest {

    @Test
    fun samePasswordWithDifferentSalts_producesDifferentCiphertext() {
        val plainText = "smartdash backup payload".toByteArray()
        val password = "rider@example.com"

        val first = EncryptionService.encryptWithPassword(
            plainText = plainText,
            password = password,
            salt = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        )
        val second = EncryptionService.encryptWithPassword(
            plainText = plainText,
            password = password,
            salt = byteArrayOf(8, 7, 6, 5, 4, 3, 2, 1)
        )

        assertNotEquals(first.salt, second.salt)
        assertNotEquals(first.cipherText, second.cipherText)
        assertArrayEquals(plainText, EncryptionService.decryptWithPassword(first, password))
        assertArrayEquals(plainText, EncryptionService.decryptWithPassword(second, password))
    }

    @Test
    fun randomSaltPayload_canBeDecryptedWithSamePasswordAcrossDevices() {
        val plainText = """{"device":"smartdash-pro","rideCount":42}""".toByteArray()
        val password = "shared-google-account@example.com"
        val salt = ByteArray(16) { index -> (index * 11 + 3).toByte() }

        val encrypted = EncryptionService.encryptWithPassword(
            plainText = plainText,
            password = password,
            salt = salt
        )
        val roundTripPayload = EncryptedBackup.fromJson(encrypted.toJson())

        assertArrayEquals(plainText, EncryptionService.decryptWithPassword(roundTripPayload, password))
    }

    @Test
    fun legacyVersion2Payload_stillDecrypts() {
        val plainText = "legacy-v2-fixed-salt".toByteArray()
        val password = "legacy@example.com"

        val encrypted = EncryptionService.encryptWithPassword(
            plainText = plainText,
            password = password,
            salt = password.toByteArray(),
            version = EncryptionService.VERSION_PASSWORD_FIXED_SALT_LEGACY
        )

        assertArrayEquals(plainText, EncryptionService.decryptWithPassword(encrypted, password))
    }

    @Test
    fun tamperedSaltOrIv_failsDecryption() {
        val plainText = "tamper-check".toByteArray()
        val password = "tamper@example.com"
        val encrypted = EncryptionService.encryptWithPassword(
            plainText = plainText,
            password = password,
            salt = ByteArray(16) { (it + 1).toByte() }
        )

        val tamperedSalt = encrypted.copy(
            salt = Base64.getEncoder().withoutPadding().encodeToString(ByteArray(16) { 99.toByte() })
        )
        val tamperedIvBytes = Base64.getDecoder().decode(encrypted.iv).also { it[0] = (it[0].toInt() xor 0x01).toByte() }
        val tamperedIv = encrypted.copy(
            iv = Base64.getEncoder().withoutPadding().encodeToString(tamperedIvBytes)
        )

        expectDecryptFailure(tamperedSalt, password)
        expectDecryptFailure(tamperedIv, password)
    }

    private fun expectDecryptFailure(payload: EncryptedBackup, password: String) {
        try {
            EncryptionService.decryptWithPassword(payload, password)
            fail("Expected decryptWithPassword to fail for tampered payload")
        } catch (_: Exception) {
            // expected
        }
    }
}
