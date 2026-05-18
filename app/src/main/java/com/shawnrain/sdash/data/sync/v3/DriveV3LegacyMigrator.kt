package com.shawnrain.sdash.data.sync.v3

import android.content.Context
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.history.RideHistoryRepository
import com.shawnrain.sdash.data.sync.DriveManifestRepository
import com.shawnrain.sdash.data.sync.DriveStateMerger
import com.shawnrain.sdash.data.sync.DriveStateSerializer
import com.shawnrain.sdash.data.sync.EncryptedBackup
import com.shawnrain.sdash.data.sync.EncryptionService
import com.shawnrain.sdash.data.sync.GoogleDriveSyncManager
import com.shawnrain.sdash.data.sync.SyncMetadataRepository
import com.shawnrain.sdash.debug.AppLogger
import java.security.MessageDigest
import java.util.Base64

class DriveV3LegacyMigrator(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val rideHistoryRepository: RideHistoryRepository,
    private val driveSyncManager: GoogleDriveSyncManager,
    private val metadataRepository: SyncMetadataRepository,
    private val legacyManifestRepository: DriveManifestRepository,
    private val stateSerializer: DriveStateSerializer,
    private val stateMerger: DriveStateMerger,
    private val entityStore: DriveEntityStore,
    private val v3Coordinator: DriveV3Coordinator
) {
    suspend fun pullOrMigrate(): DriveV3PullResult? {
        if (entityStore.fetchManifest() != null) {
            return v3Coordinator.pullAndMergeRemoteSnapshot()
        }
        migrateLegacyV2IfPresent()
        return null
    }

    suspend fun reconcileAndPublish(): DriveV3PublishResult {
        if (entityStore.fetchManifest() == null) {
            migrateLegacyV2IfPresent()
        } else {
            v3Coordinator.pullAndMergeRemoteSnapshot()
        }
        return v3Coordinator.publishFullSnapshot()
    }

    suspend fun ensureV3Exists(): Boolean {
        if (entityStore.fetchManifest() != null) return true
        val migrated = migrateLegacyV2IfPresent()
        if (!migrated) {
            v3Coordinator.publishFullSnapshot()
        }
        return true
    }

    private suspend fun migrateLegacyV2IfPresent(): Boolean {
        val legacyManifest = legacyManifestRepository.fetchRemoteManifest() ?: return false
        val encryptedBytes = legacyManifestRepository
            .downloadCurrentState(legacyManifest.currentStateFileName)
            .getOrElse { throw it }
        val encrypted = EncryptedBackup.fromJson(encryptedBytes.toString(Charsets.UTF_8))
        val plainBytes = if (encrypted.version >= EncryptionService.VERSION_PASSWORD_FIXED_SALT_LEGACY) {
            EncryptionService.decryptWithPassword(encrypted, deriveEncryptionPassword())
        } else {
            EncryptionService.decrypt(encrypted)
        }
        val remoteState = stateSerializer.deserialize(plainBytes)
        val metadata = metadataRepository.getMetadata(context)
        val localState = stateSerializer.buildCurrentState(
            stateVersion = metadata.localStateVersion,
            deviceId = metadata.deviceId,
            deviceName = metadata.deviceName
        )
        val mergeResult = stateMerger.merge(localState, remoteState, metadata)
        settingsRepository.applyDriveSyncState(mergeResult.mergedState, rideHistoryRepository)
        metadataRepository.recordPullSuccess(context, legacyManifest.stateVersion)
        AppLogger.i(
            TAG,
            "Migrated legacy V2 Drive state to local data: " +
                "remoteVersion=${legacyManifest.stateVersion} notes=${mergeResult.notes.joinToString("; ")}"
        )
        return true
    }

    private fun deriveEncryptionPassword(): String {
        val email = driveSyncManager.getCurrentAccount()?.email
            ?: throw IllegalStateException("尚未登录 Google Drive")
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(email.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hash)
    }

    companion object {
        private const val TAG = "DriveV3Migrator"
    }
}
