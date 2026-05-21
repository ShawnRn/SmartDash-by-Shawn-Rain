package com.shawnrain.sdash.data.sync

import android.content.Context
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.shawnrain.sdash.debug.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

/**
 * Manages Google Drive backup operations using the Drive REST API.
 * Uses OkHttp for lightweight API calls instead of the full Google API client.
 */
class GoogleDriveSyncManager(private val context: Context) {

    companion object {
        private const val TAG = "GDriveSync"
        private const val DRIVE_API_BASE = "https://www.googleapis.com"
        private const val UPLOAD_BASE = "https://www.googleapis.com/upload/drive/v3"
        private const val BACKUP_FILE_PREFIX = "habe_backup_"
        private const val METADATA_FILE_NAME = "habe_metadata.json"
        private const val BACKUP_MIME_TYPE = "application/octet-stream"
        private const val MANIFEST_MIME_TYPE = "application/json"
        private const val DRIVE_TOKEN_CACHE_MS = 50L * 60L * 1000L
        private const val DRIVE_CONNECT_TIMEOUT_SECONDS = 20L
        private const val DRIVE_READ_TIMEOUT_SECONDS = 60L
        private const val DRIVE_WRITE_TIMEOUT_SECONDS = 60L
        private const val DRIVE_CALL_TIMEOUT_SECONDS = 90L
        private const val DRIVE_TRANSIENT_RETRY_COUNT = 3
        private const val DRIVE_RETRY_BASE_DELAY_MS = 700L
        private const val DRIVE_PRUNE_MAX_DELETES_PER_RUN = 3
        private val appDataMutationMutex = Mutex()
    }

    private val auth = GoogleDriveAuth(context)
    private val tokenCacheMutex = Mutex()
    @Volatile
    private var cachedAccessToken: String? = null
    @Volatile
    private var cachedAccessTokenAtMs: Long = 0L

    // Auth delegation methods
    fun isSignedIn() = auth.isSignedIn()
    fun getCurrentAccount() = auth.getCurrentAccount()
    suspend fun signOut() = auth.signOut()
    suspend fun silentSignIn() = auth.silentSignIn()

    /**
     * Gets a fresh OAuth access token for Drive API calls.
     */
    private suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        auth.getAccessToken()
    }

    private suspend fun getCachedAccessToken(): String {
        val cached = cachedAccessToken
        val ageMs = System.currentTimeMillis() - cachedAccessTokenAtMs
        if (!cached.isNullOrBlank() && ageMs in 0 until DRIVE_TOKEN_CACHE_MS) {
            return cached
        }

        return tokenCacheMutex.withLock {
            val secondCached = cachedAccessToken
            val secondAgeMs = System.currentTimeMillis() - cachedAccessTokenAtMs
            if (!secondCached.isNullOrBlank() && secondAgeMs in 0 until DRIVE_TOKEN_CACHE_MS) {
                return@withLock secondCached
            }

            getAccessToken().also { token ->
                cachedAccessToken = token
                cachedAccessTokenAtMs = System.currentTimeMillis()
            }
        }
    }

    /**
     * Gets the encryption password derived from the signed-in Google account email.
     * This ensures all devices signed in to the same account can decrypt each other's backups.
     */
    private suspend fun getEncryptionPassword(): String {
        val account = auth.getCurrentAccount()
            ?: throw IllegalStateException("Not signed in to Google")
        val email = account.email

        // Derive a consistent password from the account email
        // Using SHA-256 hash ensures the password is not the raw email
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(email.toByteArray(Charsets.UTF_8))
        return java.util.Base64.getEncoder().encodeToString(hash)
    }

    /**
     * Creates an authenticated OkHttpClient that auto-adds the OAuth token.
     */
    private suspend fun createAuthenticatedClient(): OkHttpClient {
        val token = getCachedAccessToken()
        return OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .protocols(listOf(Protocol.HTTP_1_1))
            .connectionPool(ConnectionPool(0, 1, TimeUnit.SECONDS))
            .connectTimeout(DRIVE_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DRIVE_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(DRIVE_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(DRIVE_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    private suspend fun <T> withDriveRetry(
        operation: String,
        block: suspend () -> T
    ): T {
        var lastError: Exception? = null
        repeat(DRIVE_TRANSIENT_RETRY_COUNT) { attemptIndex ->
            try {
                return block()
            } catch (e: Exception) {
                if (!e.isTransientDriveNetworkFailure() || attemptIndex == DRIVE_TRANSIENT_RETRY_COUNT - 1) {
                    throw e
                }
                lastError = e
                val nextAttempt = attemptIndex + 2
                val delayMs = DRIVE_RETRY_BASE_DELAY_MS * (attemptIndex + 1)
                AppLogger.w(
                    TAG,
                    "Transient Drive network failure during $operation; retrying attempt=$nextAttempt/${DRIVE_TRANSIENT_RETRY_COUNT}: ${e.message}"
                )
                delay(delayMs)
            }
        }
        throw lastError ?: IllegalStateException("Drive retry exhausted: $operation")
    }

    private fun Throwable.isTransientDriveNetworkFailure(): Boolean {
        if (this is SocketTimeoutException || this is EOFException || this is SSLHandshakeException || this is SSLException || this is IOException) {
            return true
        }
        val message = message.orEmpty()
        return message.contains("Required SETTINGS preface", ignoreCase = true) ||
            message.contains("connection closed", ignoreCase = true) ||
            message.contains("stream was reset", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true)
    }

    /**
     * Uploads an encrypted backup JSON string to Google Drive.
     */
    suspend fun uploadBackup(
        backupJson: String,
        progressCallback: ((Float) -> Unit)? = null
    ): Result<BackupMetadata> = withContext(Dispatchers.IO) {
        try {
            withDriveRetry("upload backup") {
                // Get password derived from Google account email for cross-device compatibility
                val password = getEncryptionPassword()

                // Encrypt the backup with password-derived key
                val plainBytes = backupJson.toByteArray(Charsets.UTF_8)
                val encrypted = EncryptionService.encryptWithPassword(
                    plainText = plainBytes,
                    password = password,
                    salt = EncryptionService.generateSalt()
                )
                val encryptedPayload = encrypted.toJson().toByteArray(Charsets.UTF_8)

                val client = createAuthenticatedClient()
                val timestamp = System.currentTimeMillis()
                val fileName = "${BACKUP_FILE_PREFIX}${timestamp}.json.enc"

                // Upload using multipart upload (metadata + content)
                val fileId = uploadFile(
                    client = client,
                    fileName = fileName,
                    content = encryptedPayload,
                    description = "SmartDash Backup - ${getDeviceName()} - $timestamp"
                ) ?: return@withDriveRetry Result.failure(Exception("Upload returned null file ID"))

                val metadata = BackupMetadata(
                    fileId = fileId,
                    fileName = fileName,
                    createdAt = timestamp,
                    sizeBytes = encryptedPayload.size.toLong(),
                    deviceName = getDeviceName(),
                    appVersion = getAppVersion(),
                    vehicleCount = 0
                )

                AppLogger.i(TAG, "Backup uploaded: $fileName ($fileId)")
                Result.success(metadata)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Upload failed", e)
            Result.failure(e)
        }
    }

    /**
     * Lists all available backups from Google Drive appDataFolder.
     *
     * Reliability takes priority over storage trimming here:
     * we never auto-delete historical versions during listing because doing so
     * can permanently remove the only recoverable copy of a ride/session.
     */
    suspend fun listBackups(): Result<List<BackupMetadata>> = withContext(Dispatchers.IO) {
        try {
            withDriveRetry("list backups") {
                val client = createAuthenticatedClient()

                // Query for habe backup files
                val query = "name contains '${BACKUP_FILE_PREFIX}' and trashed = false"
                val fields = "files(id,name,createdTime,size,description)"
                val url = "$DRIVE_API_BASE/drive/v3/files" +
                        "?q=$query&orderBy=createdTime desc&spaces=appDataFolder&fields=$fields"

                val response = client.newCall(
                    okhttp3.Request.Builder().url(url).get().build()
                ).execute()

                if (!response.isSuccessful) {
                    return@withDriveRetry Result.failure(Exception("List failed: ${response.code}"))
                }

                val body = response.body?.string() ?: ""
                val backups = parseFileList(body)

                Result.success(backups)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "List backups failed", e)
            Result.failure(e)
        }
    }

    /**
     * Downloads and decrypts a specific backup from Google Drive.
     */
    suspend fun downloadBackup(
        fileId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            withDriveRetry("download backup") {
                val client = createAuthenticatedClient()
                val password = getEncryptionPassword()

                // Download file content
                val url = "$DRIVE_API_BASE/drive/v3/files/$fileId?alt=media"
                val response = client.newCall(
                    okhttp3.Request.Builder().url(url).get().build()
                ).execute()

                if (!response.isSuccessful) {
                    return@withDriveRetry Result.failure(Exception("Download failed: ${response.code}"))
                }

                val encryptedBytes = response.body?.bytes()
                    ?: return@withDriveRetry Result.failure(Exception("Empty download response"))

                // Parse encrypted payload
                val encryptedJson = String(encryptedBytes, Charsets.UTF_8)
                val encrypted = EncryptedBackup.fromJson(encryptedJson)

                // Decrypt based on version
                val plainBytes = if (encrypted.version >= EncryptionService.VERSION_PASSWORD_FIXED_SALT_LEGACY) {
                    // Password-derived key (cross-device compatible)
                    EncryptionService.decryptWithPassword(encrypted, password)
                } else {
                    // Version 1 = device-bound key (legacy, only works on same device)
                    EncryptionService.decrypt(encrypted)
                }
                Result.success(String(plainBytes, Charsets.UTF_8))
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Download failed", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a backup file from Google Drive.
     */
    suspend fun deleteBackup(fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            withDriveRetry("delete backup") {
                val client = createAuthenticatedClient()
                val url = "$DRIVE_API_BASE/drive/v3/files/$fileId"

                val response = client.newCall(
                    okhttp3.Request.Builder().url(url).delete().build()
                ).execute()

                if (response.isSuccessful) {
                    AppLogger.i(TAG, "Deleted backup: $fileId")
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Delete failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Delete failed", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes expired historical backups according to the chosen retention policy.
     * The newest backup is always preserved even if it falls outside the retention window.
     */
    suspend fun pruneBackups(policy: BackupRetentionPolicy): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val retentionDays = policy.retentionDays ?: return@withContext Result.success(0)
            val backups = listBackups().getOrElse { throw it }
            if (backups.size <= 1) {
                return@withContext Result.success(0)
            }

            val cutoffMs = System.currentTimeMillis() - retentionDays * 24L * 60L * 60L * 1000L
            val expiredBackups = backups
                .drop(1) // newest version is always preserved
                .filter { it.createdAt < cutoffMs }
                .take(DRIVE_PRUNE_MAX_DELETES_PER_RUN)

            var deletedCount = 0
            expiredBackups.forEach { backup ->
                deleteBackup(backup.fileId)
                    .onSuccess { deletedCount += 1 }
                    .onFailure { error ->
                        AppLogger.w(TAG, "Deferred backup pruning for ${backup.fileId}: ${error.message}")
                    }
            }

            if (deletedCount > 0) {
                AppLogger.i(TAG, "Pruned $deletedCount expired Drive backups with policy=${policy.name}")
            }
            Result.success(deletedCount)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Prune backups failed", e)
            Result.failure(e)
        }
    }

    // ======== Private Helpers ========

    private suspend fun uploadFile(
        client: OkHttpClient,
        fileName: String,
        content: ByteArray,
        description: String,
        mimeType: String = BACKUP_MIME_TYPE,
        existingFileId: String? = null
    ): String? {
        // Build JSON metadata
        val request = if (existingFileId != null) {
            okhttp3.Request.Builder()
                .url("$UPLOAD_BASE/files/$existingFileId?uploadType=media")
                .patch(content.toRequestBody(mimeType.toMediaType()))
                .build()
        } else {
            val metadataJson = """
                {
                    "name": "$fileName",
                    "description": "$description",
                    "mimeType": "$mimeType",
                    "parents": ["appDataFolder"]
                }
            """.trimIndent()

            val metadataPart = MultipartBody.Part.createFormData(
                "metadata", "metadata.json",
                metadataJson.toRequestBody("application/json".toMediaType())
            )

            val filePart = MultipartBody.Part.createFormData(
                "file", fileName,
                content.toRequestBody(mimeType.toMediaType())
            )

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(metadataPart)
                .addPart(filePart)
                .build()

            okhttp3.Request.Builder()
                .url("$UPLOAD_BASE/files?uploadType=multipart")
                .post(body)
                .build()
        }

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string()
            AppLogger.e(TAG, "Upload failed: ${response.code} $errBody")
            if (response.code >= 500) {
                throw IOException("Upload failed: ${response.code} $errBody")
            }
            return null
        }

        val responseBody = response.body?.string() ?: return null
        val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(responseBody)
        return jsonElement.jsonObject["id"]?.jsonPrimitive?.content
    }

    private data class DriveNamedFile(
        val id: String,
        val name: String
    )

    private suspend fun findFilesByName(
        client: OkHttpClient,
        fileName: String
    ): List<DriveNamedFile> = withContext(Dispatchers.IO) {
        val query = "name = '$fileName' and trashed = false"
        val fields = "files(id,name,createdTime)"
        val url = "$DRIVE_API_BASE/drive/v3/files" +
            "?q=$query&orderBy=createdTime desc&spaces=appDataFolder&fields=$fields"

        val response = client.newCall(
            okhttp3.Request.Builder().url(url).get().build()
        ).execute()

        if (!response.isSuccessful) {
            if (response.code >= 500) {
                throw IOException("Query failed: ${response.code}")
            }
            throw IllegalStateException("Query failed: ${response.code}")
        }

        val body = response.body?.string() ?: return@withContext emptyList()
        val root = kotlinx.serialization.json.Json.parseToJsonElement(body)
        val files = root.jsonObject["files"]?.jsonArray ?: return@withContext emptyList()
        files.mapNotNull { file ->
            val fileObject = file.jsonObject
            val id = fileObject["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val name = fileObject["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            DriveNamedFile(id = id, name = name)
        }
    }

    private suspend fun deleteFileById(
        client: OkHttpClient,
        fileId: String
    ) = withContext(Dispatchers.IO) {
        val url = "$DRIVE_API_BASE/drive/v3/files/$fileId"
        val response = client.newCall(
            okhttp3.Request.Builder().url(url).delete().build()
        ).execute()
        if (response.code == 404) {
            AppLogger.d(TAG, "Drive file already gone during delete: $fileId")
            return@withContext
        }
        if (!response.isSuccessful) {
            if (response.code >= 500) {
                throw IOException("Delete failed: ${response.code}")
            }
            throw IllegalStateException("Delete failed: ${response.code}")
        }
    }

    private suspend fun cleanupDuplicateFilesBestEffort(
        client: OkHttpClient,
        fileName: String,
        duplicates: List<DriveNamedFile>
    ) {
        duplicates.forEach { duplicate ->
            runCatching {
                deleteFileById(client, duplicate.id)
            }.onSuccess {
                AppLogger.i(TAG, "Removed stale duplicate Drive file: $fileName (${duplicate.id})")
            }.onFailure { error ->
                AppLogger.w(TAG, "Deferred duplicate cleanup for $fileName: ${error.message}")
            }
        }
    }

    private fun parseFileList(responseJson: String): List<BackupMetadata> {
        return try {
            val root = kotlinx.serialization.json.Json.parseToJsonElement(responseJson)
            val files = root.jsonObject["files"]?.jsonArray ?: return emptyList()

            files.mapNotNull { fileObj ->
                val obj = fileObj.jsonObject
                val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val size = obj["size"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L

                val createdAt = try {
                    val timeStr = obj["createdTime"]?.jsonPrimitive?.content
                    java.time.Instant.parse(timeStr).toEpochMilli()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }

                val deviceName = obj["description"]?.jsonPrimitive?.content
                    ?.split(" - ")?.getOrNull(1) ?: "Unknown"

                BackupMetadata(
                    fileId = id,
                    fileName = name,
                    createdAt = createdAt,
                    sizeBytes = size,
                    deviceName = deviceName,
                    appVersion = "Unknown",
                    vehicleCount = 0
                )
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to parse file list", e)
            emptyList()
        }
    }

    private fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}".trim().ifBlank { "Android Device" }
    }

    private fun getAppVersion(): String {
        return try {
            val pm = context.packageManager
            val info = pm.getPackageInfo(context.packageName, 0)
            info.versionName ?: PackageInfoCompat.getLongVersionCode(info).toString()
        } catch (e: Exception) {
            "Unknown"
        }
    }

    // ======== V2 Sync Helper Methods ========

    /**
     * Upload a raw (unencrypted) file to Drive appDataFolder.
     * Used for manifest and current state files.
     */
    suspend fun uploadRawFile(
        fileName: String,
        content: ByteArray,
        mimeType: String = MANIFEST_MIME_TYPE,
        existingFileIdHint: String? = null,
        skipLookup: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileId = withDriveRetry("upload raw file $fileName") {
                appDataMutationMutex.withLock {
                    val client = createAuthenticatedClient()
                    val uploadedId = when {
                        !existingFileIdHint.isNullOrBlank() -> {
                            val updatedId = runCatching {
                                uploadFile(
                                    client = client,
                                    fileName = fileName,
                                    content = content,
                                    description = "SmartDash Drive Sync - $fileName",
                                    mimeType = mimeType,
                                    existingFileId = existingFileIdHint
                                )
                            }.getOrElse { error ->
                                if (error.isTransientDriveNetworkFailure()) {
                                    throw error
                                }
                                AppLogger.w(TAG, "Failed to update hinted Drive file id for $fileName: ${error.message}")
                                null
                            }
                            updatedId ?: if (skipLookup) {
                                uploadFile(
                                    client = client,
                                    fileName = fileName,
                                    content = content,
                                    description = "SmartDash Drive Sync - $fileName",
                                    mimeType = mimeType
                                )
                            } else {
                                null
                            }
                        }
                        skipLookup -> {
                            uploadFile(
                                client = client,
                                fileName = fileName,
                                content = content,
                                description = "SmartDash Drive Sync - $fileName",
                                mimeType = mimeType
                            )
                        }
                        else -> {
                            val existingFiles = findFilesByName(client, fileName)
                            val primary = existingFiles.firstOrNull()
                            val duplicates = existingFiles.drop(1)

                            val resolvedUpload = when {
                                primary == null -> {
                                    uploadFile(
                                        client = client,
                                        fileName = fileName,
                                        content = content,
                                        description = "SmartDash Drive Sync - $fileName",
                                        mimeType = mimeType
                                    )
                                }
                                else -> {
                                    val updatedId = try {
                                        uploadFile(
                                            client = client,
                                            fileName = fileName,
                                            content = content,
                                            description = "SmartDash Drive Sync - $fileName",
                                            mimeType = mimeType,
                                            existingFileId = primary.id
                                        )
                                    } catch (error: Exception) {
                                        if (error.isTransientDriveNetworkFailure()) {
                                            throw error
                                        }
                                        AppLogger.w(TAG, "Failed updating existing Drive file; will create a replacement: $fileName (${error.message})")
                                        null
                                    }

                                    updatedId ?: uploadFile(
                                        client = client,
                                        fileName = fileName,
                                        content = content,
                                        description = "SmartDash Drive Sync - $fileName",
                                        mimeType = mimeType
                                    )
                                }
                            }

                            if (duplicates.isNotEmpty()) {
                                cleanupDuplicateFilesBestEffort(client, fileName, duplicates)
                            }

                            resolvedUpload
                        }
                    }

                    uploadedId
                }
            }
            if (fileId != null) {
                Result.success(fileId)
            } else {
                Result.failure(Exception("Upload returned null file ID"))
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "uploadRawFile failed", e)
            Result.failure(e)
        }
    }

    /**
     * Upload a text file (JSON manifest) to Drive appDataFolder.
     */
    suspend fun uploadTextFile(
        fileName: String,
        content: String
    ): Result<String> = withContext(Dispatchers.IO) {
        uploadRawFile(fileName, content.toByteArray(Charsets.UTF_8))
    }

    /**
     * Download a raw file from Drive appDataFolder by name.
     */
    suspend fun downloadRawFile(fileName: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            withDriveRetry("download raw file $fileName") {
                val client = createAuthenticatedClient()
                val files = findFilesByName(client, fileName)
                val file = files.firstOrNull()
                    ?: return@withDriveRetry Result.failure(Exception("File not found: $fileName"))
                val fileId = file.id

                // Download the file content
                val downloadUrl = "$DRIVE_API_BASE/drive/v3/files/$fileId?alt=media"
                val downloadResponse = client.newCall(
                    okhttp3.Request.Builder().url(downloadUrl).get().build()
                ).execute()

                if (!downloadResponse.isSuccessful) {
                    return@withDriveRetry Result.failure(Exception("Download failed: ${downloadResponse.code}"))
                }

                val bytes = downloadResponse.body?.bytes()
                    ?: return@withDriveRetry Result.failure(Exception("Empty download response"))

                Result.success(bytes)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "downloadRawFile failed: $fileName", e)
            Result.failure(e)
        }
    }

    /**
     * Download a text file from Drive by fileId.
     */
    suspend fun downloadTextFile(fileId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            withDriveRetry("download text file") {
                val client = createAuthenticatedClient()
                val url = "$DRIVE_API_BASE/drive/v3/files/$fileId?alt=media"
                val response = client.newCall(
                    okhttp3.Request.Builder().url(url).get().build()
                ).execute()

                if (!response.isSuccessful) {
                    return@withDriveRetry Result.failure(Exception("Download failed: ${response.code}"))
                }

                val body = response.body?.string()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty download response"))
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "downloadTextFile failed: $fileId", e)
            Result.failure(e)
        }
    }

    /**
     * Download a text file from Drive appDataFolder by name.
     */
    suspend fun downloadTextFileByName(fileName: String): Result<String> = withContext(Dispatchers.IO) {
        downloadRawFile(fileName).map { bytes -> String(bytes, Charsets.UTF_8) }
    }
}
