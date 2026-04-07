package com.shawnrain.habe.data.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Data models for Google Drive backup and sync.
 */

@Serializable
data class EncryptedBackup(
    val version: Int = 1,
    val algorithm: String = "AES-256-GCM",
    val salt: String,
    val iv: String,
    val cipherText: String,
    val tag: String
) {
    fun toJson(): String = backupJson.encodeToString(this)

    companion object {
        fun fromJson(str: String): EncryptedBackup =
            backupJson.decodeFromString(str)
    }
}

@Serializable
data class BackupMetadata(
    val fileId: String,
    val fileName: String,
    val createdAt: Long,          // Unix timestamp millis
    val sizeBytes: Long,
    val deviceName: String,
    val appVersion: String,
    val vehicleCount: Int,
    val checksum: String? = null  // SHA-256 of plaintext
)

@Serializable
data class BackupManifest(
    val schemaVersion: Int = 1,
    val latestVersion: Int = 0,
    val backups: List<BackupMetadata> = emptyList(),
    val maxRetainedVersions: Int = 10
) {
    fun toJson(): String = backupJson.encodeToString(this)

    companion object {
        val EMPTY = BackupManifest()
        fun fromJson(str: String): BackupManifest =
            backupJson.decodeFromString(str)
    }
}

data class BackupPreview(
    val exportedAt: Long,
    val vehicleCount: Int,
    val rideCount: Int,
    val speedTestCount: Int,
    val vehicles: List<BackupPreviewVehicle> = emptyList(),
    val recentRides: List<BackupPreviewRide> = emptyList()
)

data class BackupPreviewVehicle(
    val id: String,
    val name: String,
    val totalMileageKm: Float,
    val rideCount: Int
)

data class BackupPreviewRide(
    val id: String,
    val title: String,
    val startedAtMs: Long,
    val distanceKm: Float,
    val durationMinutes: Int,
    val vehicleId: String,
    val vehicleName: String
)

private val backupJson = Json { ignoreUnknownKeys = true }

sealed class SyncState {
    object SignedOut : SyncState()
    object SigningIn : SyncState()
    data class SignedIn(
        val email: String,
        val lastSyncTime: Long? = null,
        val availableBackups: List<BackupMetadata> = emptyList(),
        val hasRemoteUpdate: Boolean = false,
        val remoteDeviceName: String? = null
    ) : SyncState()
    object Syncing : SyncState()
    data class Synced(val uploaded: Boolean, val downloaded: Boolean) : SyncState()
    data class Error(val message: String, val recoverable: Boolean = true) : SyncState()
}

enum class ImportStrategy {
    OVERWRITE_ALL,        // Wipe local, apply remote
    MERGE_VEHICLES,       // Only merge vehicle profiles
    MERGE_SETTINGS,       // Only merge settings
    SMART_MERGE           // Intelligent merge (union vehicles, newer settings, union history)
}

data class SyncConflict(
    val localMetadata: BackupMetadata?,
    val remoteMetadata: BackupMetadata,
    val conflictType: ConflictType
)

enum class ConflictType {
    NEWER_REMOTE,
    NEWER_LOCAL,
    BOTH_MODIFIED,
    SCHEMA_MISMATCH
}

sealed class ConflictResolution {
    object UseRemote : ConflictResolution()
    object UseLocal : ConflictResolution()
    data class Merge(val strategy: ImportStrategy = ImportStrategy.SMART_MERGE) : ConflictResolution()
}
