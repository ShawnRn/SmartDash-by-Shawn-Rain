package com.shawnrain.sdash.data.sync

import android.content.Context
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.sync.room.SyncDatabase
import com.shawnrain.sdash.debug.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * V2 Sync Engine - Main coordinator for push, pull, and reconcile operations.
 *
 * Orchestrates:
 * - Building local state snapshots
 * - Managing the pending mutation queue
 * - Pushing changes to Google Drive
 * - Pulling and merging remote changes
 * - Reconciling conflicts
 */
class DriveSyncCoordinator(
    private val context: Context,
    private val driveSyncManager: GoogleDriveSyncManager,
    private val settingsRepository: SettingsRepository,
    private val stateSerializer: DriveStateSerializer,
    private val stateMerger: DriveStateMerger,
    private val manifestRepository: DriveManifestRepository,
    private val metadataRepository: SyncMetadataRepository,
    private val mutationRepository: PendingMutationRepository
) {
    companion object {
        private const val TAG = "DriveSyncCoord"
        const val SYNC_ENGINE_VERSION = 2
    }

    private val _syncState = MutableStateFlow<SyncStateV2>(SyncStateV2.Idle)
    val syncState: StateFlow<SyncStateV2> = _syncState.asStateFlow()

    /**
     * Schedule a push (enqueue WorkManager worker or run immediately).
     * For now, runs push directly. In a future iteration, this can delegate to WorkManager.
     */
    suspend fun schedulePush(reason: SyncTriggerReason) {
        AppLogger.i(TAG, "Push scheduled: reason=$reason")
        runPushNow(reason)
    }

    /**
     * Schedule a pull.
     */
    suspend fun schedulePull(reason: SyncTriggerReason) {
        AppLogger.i(TAG, "Pull scheduled: reason=$reason")
        runPullNow(reason)
    }

    /**
     * Run a full reconcile cycle.
     */
    suspend fun runReconcileNow(reason: SyncTriggerReason): SyncRunResult {
        return runPullNow(reason)
    }

    /**
     * Execute a push: upload pending changes to Google Drive.
     *
     * Flow:
     * 1. Check signed-in status
     * 2. Get pending mutations
     * 3. Build current state
     * 4. Encrypt and upload
     * 5. Update manifest
     * 6. Mark mutations as synced
     */
    suspend fun runPushNow(reason: SyncTriggerReason): SyncRunResult = withContext(Dispatchers.IO) {
        try {
            if (!driveSyncManager.isSignedIn()) {
                return@withContext SyncRunResult.Skipped("Not signed in to Google Drive")
            }

            _syncState.value = SyncStateV2.Pushing(reason)

            val metadata = metadataRepository.getMetadata(context)
            val pendingMutations = mutationRepository.getCoalescedPending()

            if (pendingMutations.isEmpty()) {
                // Nothing to push, but still update current state if remote is behind
                val remoteManifest = manifestRepository.fetchRemoteManifest()
                if (remoteManifest != null && remoteManifest.stateVersion > metadata.lastPushedLocalVersion) {
                    // Remote is ahead, skip push - let pull handle it
                    return@withContext SyncRunResult.Skipped(
                        reason = "Remote is ahead (remote=${remoteManifest.stateVersion}, local=${metadata.lastPushedLocalVersion})"
                    )
                }
            }

            // Build current state
            val newStateVersion = metadata.localStateVersion + 1
            val currentState = stateSerializer.buildCurrentState(
                stateVersion = newStateVersion,
                deviceId = metadata.deviceId,
                deviceName = metadata.deviceName
            )

            // Serialize and encrypt
            val stateBytes = stateSerializer.serialize(currentState)
            val checksum = stateSerializer.computeChecksum(stateBytes)

            // Upload current state (already encrypted by driveSyncManager)
            // For V2, we need to encrypt before uploading raw
            val password = deriveEncryptionPassword()
            val encrypted = com.shawnrain.sdash.data.sync.EncryptionService.encryptWithPassword(stateBytes, password)
            val encryptedPayload = encrypted.toJson().toByteArray(Charsets.UTF_8)

            manifestRepository.uploadCurrentState(encryptedPayload).getOrElse { throw it }

            // Build and upload manifest
            val manifest = DriveChangeManifest(
                schemaVersion = SYNC_ENGINE_VERSION,
                stateVersion = newStateVersion,
                updatedAt = System.currentTimeMillis(),
                updatedByDeviceId = metadata.deviceId,
                updatedByDeviceName = metadata.deviceName,
                checksum = checksum,
                entityCounters = EntityCounters(
                    rideCount = currentState.rides.size,
                    speedTestCount = currentState.speedTests.size,
                    vehicleProfileCount = currentState.vehicleProfiles.size
                )
            )
            manifestRepository.uploadRemoteManifest(manifest).getOrElse { throw it }

            // Mark mutations as synced
            if (pendingMutations.isNotEmpty()) {
                mutationRepository.markSynced(pendingMutations.map { it.id })
            }

            // Update metadata
            metadataRepository.recordPushSuccess(context, newStateVersion)
            metadataRepository.incrementLocalStateVersion(context)

            _syncState.value = SyncStateV2.Synced(System.currentTimeMillis(), reason)

            AppLogger.i(TAG, "Push complete: stateVersion=$newStateVersion, mutations=${pendingMutations.size}")
            SyncRunResult.Success(
                reason = reason,
                notes = listOf("Pushed $newStateVersion, ${pendingMutations.size} mutations synced")
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Push failed", e)
            metadataRepository.recordSyncError(context, e.message ?: "Unknown error")
            _syncState.value = SyncStateV2.Error(e.message ?: "Push failed", reason)
            SyncRunResult.Failure(reason, e)
        }
    }

    /**
     * Execute a pull: download and merge remote state.
     *
     * Flow:
     * 1. Check signed-in status
     * 2. Fetch remote manifest
     * 3. Compare versions
     * 4. Download current state if newer
     * 5. Decrypt and merge
     * 6. Apply merged state locally
     * 7. If local has pending mutations, trigger push after pull
     */
    suspend fun runPullNow(reason: SyncTriggerReason): SyncRunResult = withContext(Dispatchers.IO) {
        try {
            if (!driveSyncManager.isSignedIn()) {
                return@withContext SyncRunResult.Skipped("Not signed in to Google Drive")
            }

            _syncState.value = SyncStateV2.Pulling(reason)

            val metadata = metadataRepository.getMetadata(context)

            // Fetch remote manifest
            val remoteManifest = manifestRepository.fetchRemoteManifest()
                ?: return@withContext SyncRunResult.Skipped("No remote manifest found")

            // Check if remote is newer than what we've applied
            if (remoteManifest.stateVersion <= metadata.lastAppliedRemoteVersion) {
                _syncState.value = SyncStateV2.Idle
                return@withContext SyncRunResult.Skipped(
                    "Remote not newer (remote=${remoteManifest.stateVersion}, applied=${metadata.lastAppliedRemoteVersion})"
                )
            }

            // Download current state
            val encryptedStateBytes = manifestRepository.downloadCurrentState().getOrElse { throw it }

            // Decrypt
            val password = deriveEncryptionPassword()
            val encryptedJson = String(encryptedStateBytes, Charsets.UTF_8)
            val encrypted = com.shawnrain.sdash.data.sync.EncryptedBackup.fromJson(encryptedJson)
            val stateBytes = if (encrypted.version >= 2) {
                com.shawnrain.sdash.data.sync.EncryptionService.decryptWithPassword(encrypted, password)
            } else {
                com.shawnrain.sdash.data.sync.EncryptionService.decrypt(encrypted)
            }

            val remoteState = stateSerializer.deserialize(stateBytes)

            // Verify checksum
            val expectedChecksum = remoteManifest.checksum
            if (expectedChecksum.isNotEmpty()) {
                val actualChecksum = stateSerializer.computeChecksum(stateBytes)
                if (actualChecksum != expectedChecksum) {
                    throw IllegalStateException("Checksum mismatch: expected=$expectedChecksum, actual=$actualChecksum")
                }
            }

            // Build local state for merging
            val localState = stateSerializer.buildCurrentState(
                stateVersion = metadata.localStateVersion,
                deviceId = metadata.deviceId,
                deviceName = metadata.deviceName
            )

            // Merge
            val mergeResult = stateMerger.merge(localState, remoteState, metadata)

            if (mergeResult.changedRemotely) {
                // Apply merged state to local
                applyMergedState(mergeResult)

                // Update metadata
                metadataRepository.recordPullSuccess(context, remoteManifest.stateVersion)
            }

            _syncState.value = SyncStateV2.Synced(System.currentTimeMillis(), reason)

            val notes = mergeResult.notes + listOf("Pulled version ${remoteManifest.stateVersion}")
            AppLogger.i(TAG, "Pull complete: ${notes.joinToString("; ")}")
            SyncRunResult.Success(reason = reason, notes = notes)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Pull failed", e)
            metadataRepository.recordSyncError(context, e.message ?: "Unknown error")
            _syncState.value = SyncStateV2.Error(e.message ?: "Pull failed", reason)
            SyncRunResult.Failure(reason, e)
        }
    }

    /**
     * Apply the merged state to local data stores.
     */
    private suspend fun applyMergedState(mergeResult: MergeResult) {
        val merged = mergeResult.mergedState

        // Apply settings
        if (mergeResult.settingsUpdated) {
            // TODO: Apply settings from merged.settings to SettingsRepository
            AppLogger.i(TAG, "Settings merged from remote")
        }

        // Apply vehicle profiles
        if (mergeResult.profilesMerged > 0) {
            // TODO: Apply vehicle profiles to SettingsRepository
            AppLogger.i(TAG, "${mergeResult.profilesMerged} vehicle profiles merged")
        }

        // Apply rides
        if (mergeResult.ridesMerged > 0) {
            // TODO: Apply rides to SettingsRepository
            AppLogger.i(TAG, "${mergeResult.ridesMerged} rides merged")
        }
    }

    /**
     * Derive encryption password from Google account email (same as V1).
     */
    private suspend fun deriveEncryptionPassword(): String {
        val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("Not signed in to Google")
        val email = account.email ?: throw IllegalStateException("No email in account")

        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(email.toByteArray(Charsets.UTF_8))
        return java.util.Base64.getEncoder().encodeToString(hash)
    }

    /**
     * Check if V2 sync is initialized.
     */
    suspend fun isV2Initialized(): Boolean {
        return try {
            val metadata = metadataRepository.getMetadata(context)
            metadata.migrationVersion >= SYNC_ENGINE_VERSION
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Initialize V2 sync for the first time.
     */
    suspend fun initializeV2Sync(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val metadata = metadataRepository.getMetadata(context)
            if (metadata.migrationVersion >= SYNC_ENGINE_VERSION) {
                return@withContext Result.success(Unit) // Already initialized
            }

            // Update migration version
            metadataRepository.incrementLocalStateVersion(context)

            // Build and upload initial state
            val newStateVersion = 1L
            val currentState = stateSerializer.buildCurrentState(
                stateVersion = newStateVersion,
                deviceId = metadata.deviceId,
                deviceName = metadata.deviceName
            )

            val stateBytes = stateSerializer.serialize(currentState)
            val checksum = stateSerializer.computeChecksum(stateBytes)

            val password = deriveEncryptionPassword()
            val encrypted = com.shawnrain.sdash.data.sync.EncryptionService.encryptWithPassword(stateBytes, password)
            val encryptedPayload = encrypted.toJson().toByteArray(Charsets.UTF_8)

            manifestRepository.uploadCurrentState(encryptedPayload).getOrElse { throw it }

            val manifest = DriveChangeManifest(
                schemaVersion = SYNC_ENGINE_VERSION,
                stateVersion = newStateVersion,
                updatedAt = System.currentTimeMillis(),
                updatedByDeviceId = metadata.deviceId,
                updatedByDeviceName = metadata.deviceName,
                checksum = checksum,
                entityCounters = EntityCounters(
                    rideCount = currentState.rides.size,
                    speedTestCount = currentState.speedTests.size,
                    vehicleProfileCount = currentState.vehicleProfiles.size
                )
            )
            manifestRepository.uploadRemoteManifest(manifest).getOrElse { throw it }

            metadataRepository.recordPushSuccess(context, newStateVersion)
            AppLogger.i(TAG, "V2 sync initialized")
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e(TAG, "V2 sync initialization failed", e)
            Result.failure(e)
        }
    }
}
