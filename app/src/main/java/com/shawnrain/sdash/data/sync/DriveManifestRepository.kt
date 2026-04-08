package com.shawnrain.sdash.data.sync

import android.content.Context
import com.shawnrain.sdash.data.sync.GoogleDriveSyncManager
import com.shawnrain.sdash.debug.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

/**
 * Manages reading/writing the change manifest on Google Drive.
 */
class DriveManifestRepository(
    private val context: Context,
    private val driveSyncManager: GoogleDriveSyncManager
) {
    companion object {
        private const val TAG = "DriveManifestRepo"
        private const val MANIFEST_FILE_NAME = "change_manifest.json"
        private const val CURRENT_STATE_FILE_NAME = "current_state.json.enc"
    }

    private val manifestJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    /**
     * Fetch the remote manifest from Google Drive.
     * Falls back to parsing the latest backup file if manifest doesn't exist yet.
     */
    suspend fun fetchRemoteManifest(): DriveChangeManifest? = withContext(Dispatchers.IO) {
        try {
            val backups = driveSyncManager.listBackups().getOrNull() ?: return@withContext null
            if (backups.isEmpty()) return@withContext null

            // Check if there's an existing manifest file
            val manifestFile = backups.find { it.fileName.contains("manifest") }
            if (manifestFile != null) {
                val manifestJsonStr = driveSyncManager.downloadTextFile(manifestFile.fileId).getOrNull()
                if (manifestJsonStr != null) {
                    return@withContext manifestJson.decodeFromString<DriveChangeManifest>(manifestJsonStr)
                }
            }

            // Fallback: derive manifest from latest backup metadata
            val latest = backups.first()
            DriveChangeManifest(
                schemaVersion = 1,  // Old format
                stateVersion = latest.createdAt,
                updatedAt = latest.createdAt,
                updatedByDeviceId = "unknown",
                updatedByDeviceName = latest.deviceName,
                latestRideId = null,
                latestRideEndedAt = null,
                entityCounters = EntityCounters()
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to fetch remote manifest", e)
            null
        }
    }

    /**
     * Upload the manifest to Google Drive.
     */
    suspend fun uploadRemoteManifest(manifest: DriveChangeManifest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val manifestJsonStr = manifestJson.encodeToString(manifest)
            driveSyncManager.uploadTextFile(MANIFEST_FILE_NAME, manifestJsonStr)
            AppLogger.i(TAG, "Manifest uploaded: stateVersion=${manifest.stateVersion}")
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to upload manifest", e)
            Result.failure(e)
        }
    }

    /**
     * Upload the current state (encrypted) to Google Drive.
     */
    suspend fun uploadCurrentState(stateBytes: ByteArray): Result<String> = withContext(Dispatchers.IO) {
        try {
            // The state is already encrypted by the caller
            driveSyncManager.uploadRawFile(CURRENT_STATE_FILE_NAME, stateBytes)
            AppLogger.i(TAG, "Current state uploaded: ${stateBytes.size} bytes")
            Result.success(CURRENT_STATE_FILE_NAME)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to upload current state", e)
            Result.failure(e)
        }
    }

    /**
     * Download the current state (encrypted) from Google Drive.
     */
    suspend fun downloadCurrentState(): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val stateBytes = driveSyncManager.downloadRawFile(CURRENT_STATE_FILE_NAME).getOrNull()
            if (stateBytes != null) {
                AppLogger.i(TAG, "Current state downloaded: ${stateBytes.size} bytes")
                Result.success(stateBytes)
            } else {
                // Fallback: try to get from latest backup
                val backups = driveSyncManager.listBackups().getOrNull() ?: return@withContext Result.failure(Exception("No backups found"))
                if (backups.isEmpty()) return@withContext Result.failure(Exception("No backups found"))
                val latest = backups.first()
                val backupContent = driveSyncManager.downloadBackup(latest.fileId)
                backupContent.map { it.toByteArray(Charsets.UTF_8) }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to download current state", e)
            Result.failure(e)
        }
    }

    /**
     * Compute SHA-256 checksum of state bytes.
     */
    fun computeChecksum(stateBytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(stateBytes)
        return hash.joinToString("") { "%02x".format(it) }
    }
}
