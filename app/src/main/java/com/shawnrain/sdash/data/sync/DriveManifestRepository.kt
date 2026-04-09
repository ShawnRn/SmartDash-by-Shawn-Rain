package com.shawnrain.sdash.data.sync

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
     * Only V2 manifests are considered valid here. Legacy V1 backup files remain
     * accessible from the backup history UI, but they are not treated as a V2
     * bidirectional sync state.
     */
    suspend fun fetchRemoteManifest(): DriveChangeManifest? = withContext(Dispatchers.IO) {
        try {
            val manifestJsonStr = driveSyncManager.downloadTextFileByName(MANIFEST_FILE_NAME).getOrNull()
            if (manifestJsonStr != null) {
                return@withContext manifestJson.decodeFromString<DriveChangeManifest>(manifestJsonStr)
            }
            AppLogger.i(TAG, "No V2 manifest found in Drive appDataFolder")
            null
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
    suspend fun uploadCurrentState(fileName: String, stateBytes: ByteArray): Result<String> = withContext(Dispatchers.IO) {
        try {
            // The state is already encrypted by the caller
            driveSyncManager.uploadRawFile(fileName, stateBytes)
            AppLogger.i(TAG, "Current state uploaded: ${stateBytes.size} bytes -> $fileName")
            Result.success(fileName)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to upload current state", e)
            Result.failure(e)
        }
    }

    /**
     * Download the current state (encrypted) from Google Drive.
     */
    suspend fun downloadCurrentState(fileName: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val resolvedFileName = fileName.ifBlank { CURRENT_STATE_FILE_NAME }
            val stateBytes = driveSyncManager.downloadRawFile(resolvedFileName).getOrNull()
                ?: if (resolvedFileName != CURRENT_STATE_FILE_NAME) {
                    driveSyncManager.downloadRawFile(CURRENT_STATE_FILE_NAME).getOrNull()
                } else {
                    null
                }
            if (stateBytes != null) {
                AppLogger.i(TAG, "Current state downloaded: ${stateBytes.size} bytes <- $resolvedFileName")
                Result.success(stateBytes)
            } else {
                Result.failure(Exception("No V2 current state found"))
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
