package com.shawnrain.sdash.data.sync

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM encryption service for cross-device backup encryption.
 *
 * Unlike device-bound KeyStore keys, this uses a password-derived key
 * so that backups created on one device can be decrypted on another.
 *
 * Key derivation: Uses the Google account email as a salt to derive
 * a consistent AES key across devices signed in to the same account.
 */
object EncryptionService {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val PBKDF2_ITERATIONS = 100_000
    private const val KEY_SIZE_BITS = 256

    // Device-bound key for local-only encryption (legacy fallback)
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val DEVICE_KEY_ALIAS = "habe_backup_key_v1"

    private val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

    /**
     * Encrypts plaintext using a password-derived key.
     * The same password on any device will produce decryptable output.
     *
     * @param plainText The data to encrypt
     * @param password  Derivation password (e.g., Google account email)
     */
    fun encryptWithPassword(plainText: ByteArray, password: String): EncryptedBackup {
        val salt = password.toByteArray(Charsets.UTF_8)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val cipherTextWithTag = cipher.doFinal(plainText)

        val tagLength = GCM_TAG_LENGTH_BITS / 8 // 16 bytes
        val cipherText = cipherTextWithTag.copyOfRange(0, cipherTextWithTag.size - tagLength)
        val tag = cipherTextWithTag.copyOfRange(cipherTextWithTag.size - tagLength, cipherTextWithTag.size)

        return EncryptedBackup(
            version = 2, // Version 2 = password-derived, cross-device compatible
            algorithm = "AES-256-GCM",
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP),
            cipherText = Base64.encodeToString(cipherText, Base64.NO_WRAP),
            tag = Base64.encodeToString(tag, Base64.NO_WRAP)
        )
    }

    /**
     * Decrypts a password-encrypted backup.
     */
    fun decryptWithPassword(encrypted: EncryptedBackup, password: String): ByteArray {
        val salt = Base64.decode(encrypted.salt, Base64.NO_WRAP)
        val iv = Base64.decode(encrypted.iv, Base64.NO_WRAP)
        val cipherText = Base64.decode(encrypted.cipherText, Base64.NO_WRAP)
        val tag = Base64.decode(encrypted.tag, Base64.NO_WRAP)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val cipherTextWithTag = cipherText + tag
        return cipher.doFinal(cipherTextWithTag)
    }

    /**
     * Derives an AES-256 key from a password using PBKDF2.
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = javax.crypto.spec.PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_SIZE_BITS
        )
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    // ======== Legacy device-bound encryption (for backwards compatibility) ========

    fun getOrCreateDeviceKey(): SecretKey {
        return if (keyStore.containsAlias(DEVICE_KEY_ALIAS)) {
            keyStore.getKey(DEVICE_KEY_ALIAS, null) as SecretKey
        } else {
            createDeviceKey()
        }
    }

    private fun createDeviceKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        val spec = KeyGenParameterSpec.Builder(
            DEVICE_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(256)
            setUserAuthenticationRequired(false)
            setRandomizedEncryptionRequired(true)
        }.build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun encrypt(plainText: ByteArray): EncryptedBackup {
        val key = getOrCreateDeviceKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val cipherTextWithTag = cipher.doFinal(plainText)

        val tagLength = GCM_TAG_LENGTH_BITS / 8
        val cipherText = cipherTextWithTag.copyOfRange(0, cipherTextWithTag.size - tagLength)
        val tag = cipherTextWithTag.copyOfRange(cipherTextWithTag.size - tagLength, cipherTextWithTag.size)

        return EncryptedBackup(
            version = 1, // Version 1 = device-bound (legacy)
            salt = "",
            iv = Base64.encodeToString(iv, Base64.NO_WRAP),
            cipherText = Base64.encodeToString(cipherText, Base64.NO_WRAP),
            tag = Base64.encodeToString(tag, Base64.NO_WRAP)
        )
    }

    fun decrypt(encrypted: EncryptedBackup): ByteArray {
        // Version 2 = password-derived, use password decryptor
        if (encrypted.version >= 2) {
            // Fall back to device key for v2 with empty salt (shouldn't happen)
            throw IllegalStateException("Use decryptWithPassword for version 2 backups")
        }

        val key = getOrCreateDeviceKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)

        val iv = Base64.decode(encrypted.iv, Base64.NO_WRAP)
        val cipherText = Base64.decode(encrypted.cipherText, Base64.NO_WRAP)
        val tag = Base64.decode(encrypted.tag, Base64.NO_WRAP)

        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val cipherTextWithTag = cipherText + tag
        return cipher.doFinal(cipherTextWithTag)
    }

    fun generateTransferPassword(): String {
        val bytes = ByteArray(16)
        java.security.SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE).take(12)
    }
}
