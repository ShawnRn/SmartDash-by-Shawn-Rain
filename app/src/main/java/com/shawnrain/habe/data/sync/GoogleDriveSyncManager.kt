package com.shawnrain.habe.data.sync

import android.content.Context
import android.os.Build
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.shawnrain.habe.debug.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Manages Google Drive backup operations using the Drive REST API.
 * Uses OkHttp for lightweight API calls instead of the full Google API client.
 */
class GoogleDriveSyncManager(private val context: Context) {

    companion object {
        private const val TAG = "GDriveSync"
        private const val DRIVE_API_BASE = "https://www.googleapis.com"
        private const val UPLOAD_BASE = "https://www.googleapis.com/upload/drive/v3"
        private const val APP_DATA_SCOPE = "oauth2:https://www.googleapis.com/auth/drive.appdata"
        private const val BACKUP_FILE_PREFIX = "habe_backup_"
        private const val METADATA_FILE_NAME = "habe_metadata.json"
        private const val BACKUP_MIME_TYPE = "application/octet-stream"
        private const val MANIFEST_MIME_TYPE = "application/json"
    }

    private val auth = GoogleDriveAuth(context)

    // Auth delegation methods
    fun isSignedIn() = auth.isSignedIn()
    fun getCurrentAccount() = auth.getCurrentAccount()
    suspend fun signOut() = auth.signOut()
    suspend fun silentSignIn() = auth.silentSignIn()
    fun getSignInIntent() = auth.getSignInIntent()

    /**
     * Gets a fresh OAuth access token for Drive API calls.
     */
    private suspend fun getAccessToken(): String = withContext(Dispatchers.IO) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("Not signed in to Google")

        val accountName = account.account?.name
            ?: throw IllegalStateException("Google account has no name")

        // Use GoogleAuthUtil with the account name string
        @Suppress("DEPRECATION")
        GoogleAuthUtil.getToken(context, accountName, APP_DATA_SCOPE)
    }

    /**
     * Gets the encryption password derived from the signed-in Google account email.
     * This ensures all devices signed in to the same account can decrypt each other's backups.
     */
    private suspend fun getEncryptionPassword(): String {
        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("Not signed in to Google")
        val email = account.email ?: throw IllegalStateException("No email in account")

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
        val token = getAccessToken()
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    /**
     * Uploads an encrypted backup JSON string to Google Drive.
     */
    suspend fun uploadBackup(
        backupJson: String,
        progressCallback: ((Float) -> Unit)? = null
    ): Result<BackupMetadata> = withContext(Dispatchers.IO) {
        try {
            // Get password derived from Google account email for cross-device compatibility
            val password = getEncryptionPassword()

            // Encrypt the backup with password-derived key
            val plainBytes = backupJson.toByteArray(Charsets.UTF_8)
            val encrypted = EncryptionService.encryptWithPassword(plainBytes, password)
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
            ) ?: return@withContext Result.failure(Exception("Upload returned null file ID"))

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
                return@withContext Result.failure(Exception("List failed: ${response.code}"))
            }

            val body = response.body?.string() ?: ""
            val backups = parseFileList(body)

            Result.success(backups)
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
            val client = createAuthenticatedClient()
            val password = getEncryptionPassword()

            // Download file content
            val url = "$DRIVE_API_BASE/drive/v3/files/$fileId?alt=media"
            val response = client.newCall(
                okhttp3.Request.Builder().url(url).get().build()
            ).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Download failed: ${response.code}"))
            }

            val encryptedBytes = response.body?.bytes()
                ?: return@withContext Result.failure(Exception("Empty download response"))

            // Parse encrypted payload
            val encryptedJson = String(encryptedBytes, Charsets.UTF_8)
            val encrypted = EncryptedBackup.fromJson(encryptedJson)

            // Decrypt based on version
            val plainBytes = if (encrypted.version >= 2) {
                // Version 2 = password-derived key (cross-device compatible)
                EncryptionService.decryptWithPassword(encrypted, password)
            } else {
                // Version 1 = device-bound key (legacy, only works on same device)
                EncryptionService.decrypt(encrypted)
            }
            Result.success(String(plainBytes, Charsets.UTF_8))
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

            var deletedCount = 0
            expiredBackups.forEach { backup ->
                deleteBackup(backup.fileId).getOrElse { throw it }
                deletedCount += 1
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
        description: String
    ): String? {
        // Build JSON metadata
        val metadataJson = """
            {
                "name": "$fileName",
                "description": "$description",
                "mimeType": "$BACKUP_MIME_TYPE",
                "parents": ["appDataFolder"]
            }
        """.trimIndent()

        val metadataPart = MultipartBody.Part.createFormData(
            "metadata", "metadata.json",
            metadataJson.toRequestBody("application/json".toMediaType())
        )

        val filePart = MultipartBody.Part.createFormData(
            "file", fileName,
            content.toRequestBody(BACKUP_MIME_TYPE.toMediaType())
        )

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addPart(metadataPart)
            .addPart(filePart)
            .build()

        val request = okhttp3.Request.Builder()
            .url("$UPLOAD_BASE/files?uploadType=multipart")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string()
            AppLogger.e(TAG, "Upload failed: ${response.code} $errBody")
            return null
        }

        val responseBody = response.body?.string() ?: return null
        val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(responseBody)
        return jsonElement.jsonObject["id"]?.jsonPrimitive?.content
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
            info.versionName ?: info.versionCode.toString()
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
