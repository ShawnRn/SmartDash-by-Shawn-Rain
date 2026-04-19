package com.shawnrain.sdash.data.sync

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
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
    const val VERSION_DEVICE_BOUND_LEGACY = 1
    const val VERSION_PASSWORD_FIXED_SALT_LEGACY = 2
    const val VERSION_PASSWORD_RANDOM_SALT = 3
    const val VERSION_PASSWORD_RANDOM_SALT_GZIP = 4

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val PBKDF2_ITERATIONS = 100_000
    private const val KEY_SIZE_BITS = 256

    // Device-bound key for local-only encryption (legacy fallback)
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val DEVICE_KEY_ALIAS = "habe_backup_key_v1"

    private val base64Encoder = Base64.getEncoder().withoutPadding()
    private val base64Decoder = Base64.getDecoder()
    private val secureRandom = SecureRandom()

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
    }

    /**
     * Encrypts plaintext using a password-derived key.
     * The same password on any device will produce decryptable output.
     *
     * @param plainText The data to encrypt
     * @param password  Derivation password (e.g., Google account email)
     */
    fun encryptWithPassword(
        plainText: ByteArray,
        password: String,
        salt: ByteArray,
        version: Int = VERSION_PASSWORD_RANDOM_SALT
    ): EncryptedBackup {
        require(version >= VERSION_PASSWORD_FIXED_SALT_LEGACY) {
            "Password-derived backups must use a password-backed version"
        }
        require(salt.isNotEmpty()) { "Salt must not be empty" }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val payload = when (version) {
            VERSION_PASSWORD_RANDOM_SALT_GZIP -> gzip(plainText)
            else -> plainText
        }

        val iv = cipher.iv
        val cipherTextWithTag = cipher.doFinal(payload)

        val tagLength = GCM_TAG_LENGTH_BITS / 8 // 16 bytes
        val cipherText = cipherTextWithTag.copyOfRange(0, cipherTextWithTag.size - tagLength)
        val tag = cipherTextWithTag.copyOfRange(cipherTextWithTag.size - tagLength, cipherTextWithTag.size)

        return EncryptedBackup(
            version = version,
            algorithm = "AES-256-GCM",
            salt = encodeBase64(salt),
            iv = encodeBase64(iv),
            cipherText = encodeBase64(cipherText),
            tag = encodeBase64(tag)
        )
    }

    /**
     * Decrypts a password-encrypted backup.
     */
    fun decryptWithPassword(encrypted: EncryptedBackup, password: String): ByteArray {
        val salt = when (encrypted.version) {
            VERSION_PASSWORD_FIXED_SALT_LEGACY -> password.toByteArray(Charsets.UTF_8)
            VERSION_PASSWORD_RANDOM_SALT,
            VERSION_PASSWORD_RANDOM_SALT_GZIP -> decodeBase64(encrypted.salt)
            else -> throw IllegalArgumentException("Unsupported password backup version: ${encrypted.version}")
        }
        val iv = decodeBase64(encrypted.iv)
        val cipherText = decodeBase64(encrypted.cipherText)
        val tag = decodeBase64(encrypted.tag)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val cipherTextWithTag = cipherText + tag
        val plainBytes = cipher.doFinal(cipherTextWithTag)
        return when (encrypted.version) {
            VERSION_PASSWORD_RANDOM_SALT_GZIP -> gunzip(plainBytes)
            else -> plainBytes
        }
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
            version = VERSION_DEVICE_BOUND_LEGACY,
            salt = "",
            iv = encodeBase64(iv),
            cipherText = encodeBase64(cipherText),
            tag = encodeBase64(tag)
        )
    }

    fun decrypt(encrypted: EncryptedBackup): ByteArray {
        if (encrypted.version >= VERSION_PASSWORD_FIXED_SALT_LEGACY) {
            throw IllegalStateException("Use decryptWithPassword for password-derived backups")
        }

        val key = getOrCreateDeviceKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)

        val iv = decodeBase64(encrypted.iv)
        val cipherText = decodeBase64(encrypted.cipherText)
        val tag = decodeBase64(encrypted.tag)

        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val cipherTextWithTag = cipherText + tag
        return cipher.doFinal(cipherTextWithTag)
    }

    fun generateSalt(lengthBytes: Int = 16): ByteArray {
        require(lengthBytes > 0) { "Salt length must be positive" }
        return ByteArray(lengthBytes).also(secureRandom::nextBytes)
    }

    fun generateTransferPassword(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).take(12)
    }

    private fun gzip(bytes: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { gzip ->
            gzip.write(bytes)
        }
        return output.toByteArray()
    }

    private fun gunzip(bytes: ByteArray): ByteArray =
        GZIPInputStream(ByteArrayInputStream(bytes)).use { input ->
            input.readBytes()
        }

    private fun encodeBase64(bytes: ByteArray): String = base64Encoder.encodeToString(bytes)

    private fun decodeBase64(encoded: String): ByteArray = base64Decoder.decode(encoded)
}
