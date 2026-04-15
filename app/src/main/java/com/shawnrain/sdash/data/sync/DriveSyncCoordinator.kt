@file:Suppress("DEPRECATION")

package com.shawnrain.sdash.data.sync

import android.content.Context
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.history.RideHistoryRepository
import com.shawnrain.sdash.data.sync.room.SyncDatabase
import com.shawnrain.sdash.debug.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val rideHistoryRepository: RideHistoryRepository,
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
    private val pushMutex = Mutex()

    private data class PreparedStateUpload(
        val stateVersion: Long,
        val checksum: String,
        val encryptedPayload: ByteArray,
        val stateFileName: String,
        val currentState: DriveCurrentState
    )

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
        val pullResult = runPullNow(reason)
        if (pullResult is SyncRunResult.Failure) return pullResult
        return if (mutationRepository.hasPendingMutations() || shouldForceFullStatePush(reason)) {
            runPushNow(reason)
        } else {
            pullResult
        }
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
        pushMutex.withLock {
            try {
                if (!driveSyncManager.isSignedIn()) {
                    return@withContext SyncRunResult.Skipped("尚未登录 Google Drive")
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
                            reason = "检测到云端有较新的资料，已优先同步云端内容"
                        )
                    }
                }

                val preparedUpload = prepareStateUpload(metadata)
                manifestRepository.uploadCurrentState(
                    preparedUpload.stateFileName,
                    preparedUpload.encryptedPayload
                ).getOrElse { throw it }

                // Build and upload manifest
                val manifest = buildManifest(preparedUpload, metadata)
                manifestRepository.uploadRemoteManifest(manifest).getOrElse { throw it }

                // Mark mutations as synced
                if (pendingMutations.isNotEmpty()) {
                    mutationRepository.markSynced(pendingMutations.map { it.id })
                }

                // Update metadata
                metadataRepository.recordPushSuccess(context, preparedUpload.stateVersion)

                _syncState.value = SyncStateV2.Synced(System.currentTimeMillis(), reason)

                AppLogger.i(
                    TAG,
                    "Push complete: stateVersion=${preparedUpload.stateVersion}, mutations=${pendingMutations.size}"
                )
                SyncRunResult.Success(
                    reason = reason,
                    notes = listOf(
                        if (pendingMutations.isEmpty()) {
                            "已将本地完整资料补传到云端"
                        } else {
                            "本地变更已上传到云端"
                        }
                    )
                )
            } catch (e: Exception) {
                AppLogger.e(TAG, "Push failed", e)
                metadataRepository.recordSyncError(context, e.message ?: "Unknown error")
                _syncState.value = SyncStateV2.Error(e.message ?: "Push failed", reason)
                SyncRunResult.Failure(reason, e)
            }
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
                return@withContext SyncRunResult.Skipped("尚未登录 Google Drive")
            }

            _syncState.value = SyncStateV2.Pulling(reason)

            val metadata = metadataRepository.getMetadata(context)

            // Fetch remote manifest
            val remoteManifest = manifestRepository.fetchRemoteManifest()
                ?: return@withContext SyncRunResult.Skipped("云端尚未初始化同步状态")

            if (remoteManifest.schemaVersion < SYNC_ENGINE_VERSION) {
                _syncState.value = SyncStateV2.Idle
                return@withContext SyncRunResult.Skipped("检测到旧版云端备份，但双向同步状态尚未初始化")
            }

            // Check if remote is newer than what we've applied
            if (remoteManifest.stateVersion <= metadata.lastAppliedRemoteVersion) {
                _syncState.value = SyncStateV2.Idle
                return@withContext SyncRunResult.Skipped(
                    "云端没有新内容，本地也没有待上传的变更"
                )
            }

            // Resolve encryption password before downloading the state payload.
            val password = deriveEncryptionPassword()

            // Download current state
            val encryptedStateBytes = manifestRepository.downloadCurrentState(remoteManifest.currentStateFileName).getOrElse {
                _syncState.value = SyncStateV2.Idle
                return@withContext SyncRunResult.Skipped(it.message ?: "云端缺少当前同步状态文件")
            }

            // Decrypt
            val stateBytes = decryptStateBytes(encryptedStateBytes, password)

            // Verify checksum
            val expectedChecksum = remoteManifest.checksum
            if (expectedChecksum.isNotEmpty()) {
                val actualChecksum = stateSerializer.computeChecksum(stateBytes)
                if (actualChecksum != expectedChecksum) {
                    AppLogger.w(
                        TAG,
                        "Checksum mismatch: expected=$expectedChecksum, actual=$actualChecksum, " +
                                "size=${stateBytes.size}, remoteEnvSize=${encryptedStateBytes.size}"
                    )
                    val refreshedManifest = manifestRepository.fetchRemoteManifest()
                    if (refreshedManifest != null && refreshedManifest.stateVersion >= remoteManifest.stateVersion) {
                        val retriedBytes = manifestRepository
                            .downloadCurrentState(refreshedManifest.currentStateFileName)
                            .getOrElse { throw it }
                        val retriedStateBytes = decryptStateBytes(retriedBytes, password)
                        val retriedChecksum = stateSerializer.computeChecksum(retriedStateBytes)
                        if (retriedChecksum != refreshedManifest.checksum) {
                            AppLogger.e(
                                TAG,
                                "Retry checksum mismatch: expected=${refreshedManifest.checksum}, " +
                                        "actual=$retriedChecksum, size=${retriedStateBytes.size}"
                            )
                            throw IllegalStateException(
                                "Checksum mismatch: expected=${refreshedManifest.checksum}, actual=$retriedChecksum"
                            )
                        }
                        return@withContext applyPulledState(
                            remoteManifest = refreshedManifest,
                            stateBytes = retriedStateBytes,
                            metadata = metadata,
                            reason = reason
                        )
                    }
                    throw IllegalStateException("Checksum mismatch: expected=$expectedChecksum, actual=$actualChecksum")
                }
            }
            applyPulledState(
                remoteManifest = remoteManifest,
                stateBytes = stateBytes,
                metadata = metadata,
                reason = reason
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Pull failed", e)
            metadataRepository.recordSyncError(context, e.message ?: "Unknown error")
            _syncState.value = SyncStateV2.Error(e.message ?: "Pull failed", reason)
            SyncRunResult.Failure(reason, e)
        }
    }

    private suspend fun prepareStateUpload(metadata: SyncMetadata): PreparedStateUpload {
        val stateVersion = maxOf(metadata.localStateVersion, metadata.lastPushedLocalVersion) + 1
        val currentState = stateSerializer.buildCurrentState(
            stateVersion = stateVersion,
            deviceId = metadata.deviceId,
            deviceName = metadata.deviceName
        )
        val stateBytes = stateSerializer.serialize(currentState)
        val checksum = stateSerializer.computeChecksum(stateBytes)
        val password = deriveEncryptionPassword()
        val encrypted = com.shawnrain.sdash.data.sync.EncryptionService.encryptWithPassword(
            plainText = stateBytes,
            password = password,
            salt = com.shawnrain.sdash.data.sync.EncryptionService.generateSalt()
        )
        return PreparedStateUpload(
            stateVersion = stateVersion,
            checksum = checksum,
            encryptedPayload = encrypted.toJson().toByteArray(Charsets.UTF_8),
            stateFileName = "current_state_v${stateVersion}.json.enc",
            currentState = currentState
        )
    }

    private fun buildManifest(
        preparedUpload: PreparedStateUpload,
        metadata: SyncMetadata
    ): DriveChangeManifest {
        return DriveChangeManifest(
            schemaVersion = SYNC_ENGINE_VERSION,
            stateVersion = preparedUpload.stateVersion,
            updatedAt = System.currentTimeMillis(),
            updatedByDeviceId = metadata.deviceId,
            updatedByDeviceName = metadata.deviceName,
            checksum = preparedUpload.checksum,
            entityCounters = EntityCounters(
                rideCount = preparedUpload.currentState.rides.size,
                speedTestCount = preparedUpload.currentState.speedTests.size,
                vehicleProfileCount = preparedUpload.currentState.vehicleProfiles.size
            ),
            currentStateFileName = preparedUpload.stateFileName
        )
    }

    private fun decryptStateBytes(
        encryptedStateBytes: ByteArray,
        password: String
    ): ByteArray {
        val encryptedJson = String(encryptedStateBytes, Charsets.UTF_8)
        val encrypted = com.shawnrain.sdash.data.sync.EncryptedBackup.fromJson(encryptedJson)
        return if (encrypted.version >= com.shawnrain.sdash.data.sync.EncryptionService.VERSION_PASSWORD_FIXED_SALT_LEGACY) {
            com.shawnrain.sdash.data.sync.EncryptionService.decryptWithPassword(encrypted, password)
        } else {
            com.shawnrain.sdash.data.sync.EncryptionService.decrypt(encrypted)
        }
    }

    /**
     * Apply the merged state to local data stores.
     */
    private suspend fun applyMergedState(mergeResult: MergeResult) {
        settingsRepository.applyDriveSyncState(mergeResult.mergedState, rideHistoryRepository)
        if (mergeResult.settingsUpdated) AppLogger.i(TAG, "Settings merged from remote")
        if (mergeResult.profilesMerged > 0) AppLogger.i(TAG, "${mergeResult.profilesMerged} vehicle profiles merged")
        if (mergeResult.ridesMerged > 0) AppLogger.i(TAG, "${mergeResult.ridesMerged} rides merged")
    }

    private suspend fun applyPulledState(
        remoteManifest: DriveChangeManifest,
        stateBytes: ByteArray,
        metadata: SyncMetadata,
        reason: SyncTriggerReason
    ): SyncRunResult {
        val remoteState = stateSerializer.deserialize(stateBytes)

        val localState = stateSerializer.buildCurrentState(
            stateVersion = metadata.localStateVersion,
            deviceId = metadata.deviceId,
            deviceName = metadata.deviceName
        )

        val mergeResult = stateMerger.merge(localState, remoteState, metadata)

        if (mergeResult.changedRemotely) {
            applyMergedState(mergeResult)
            metadataRepository.recordPullSuccess(context, remoteManifest.stateVersion)
        }

        if (mutationRepository.hasPendingMutations()) {
            AppLogger.i(TAG, "Local pending mutations remain after pull, scheduling follow-up push")
            runPushNow(SyncTriggerReason.NETWORK_RESTORED)
        }

        _syncState.value = SyncStateV2.Synced(System.currentTimeMillis(), reason)

        val notes = if (
            mergeResult.notes.size == 1 &&
            mergeResult.notes.first() == "云端和本地都没有新的资料变更"
        ) {
            listOf("云端和本地都没有新的资料变更")
        } else {
            mergeResult.notes + listOf("已同步到最新云端数据")
        }
        AppLogger.i(TAG, "Pull complete: ${notes.joinToString("; ")}")
        return SyncRunResult.Success(reason = reason, notes = notes)
    }

    /**
     * Derive encryption password from Google account email (same as V1).
     */
    private suspend fun deriveEncryptionPassword(): String {
        val account = driveSyncManager.getCurrentAccount()
            ?: throw IllegalStateException("Not signed in to Google")
        val email = account.email.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("No email in account")

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
            metadata.migrationVersion >= SYNC_ENGINE_VERSION &&
                (metadata.lastPushedLocalVersion > 0L || metadata.lastKnownRemoteVersion > 0L)
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
            if (metadata.migrationVersion >= SYNC_ENGINE_VERSION &&
                (metadata.lastPushedLocalVersion > 0L || metadata.lastKnownRemoteVersion > 0L)
            ) {
                return@withContext Result.success(Unit) // Already initialized
            }

            val remoteManifest = manifestRepository.fetchRemoteManifest()
            if (remoteManifest != null && remoteManifest.schemaVersion >= SYNC_ENGINE_VERSION) {
                metadataRepository.recordRemoteDiscovered(context, remoteManifest.stateVersion)
                AppLogger.i(
                    TAG,
                    "V2 sync already exists remotely, skip local bootstrap and pull remote stateVersion=${remoteManifest.stateVersion}"
                )
                return@withContext Result.success(Unit)
            }

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
            val encrypted = com.shawnrain.sdash.data.sync.EncryptionService.encryptWithPassword(
                plainText = stateBytes,
                password = password,
                salt = com.shawnrain.sdash.data.sync.EncryptionService.generateSalt()
            )
            val encryptedPayload = encrypted.toJson().toByteArray(Charsets.UTF_8)

            val stateFileName = "current_state_v${newStateVersion}.json.enc"
            manifestRepository.uploadCurrentState(stateFileName, encryptedPayload).getOrElse { throw it }

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
                ),
                currentStateFileName = stateFileName
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

    private suspend fun shouldForceFullStatePush(reason: SyncTriggerReason): Boolean = withContext(Dispatchers.IO) {
        if (reason != SyncTriggerReason.MANUAL_SYNC &&
            reason != SyncTriggerReason.AUTH_SUCCESS &&
            reason != SyncTriggerReason.APP_FOREGROUND
        ) {
            return@withContext false
        }

        val metadata = metadataRepository.getMetadata(context)
        val remoteManifest = manifestRepository.fetchRemoteManifest()
        val localState = stateSerializer.buildCurrentState(
            stateVersion = metadata.localStateVersion,
            deviceId = metadata.deviceId,
            deviceName = metadata.deviceName
        )

        val localHasMeaningfulData = localState.rides.isNotEmpty() ||
            localState.speedTests.isNotEmpty() ||
            localState.vehicleProfiles.size > 1

        if (!localHasMeaningfulData) return@withContext false
        if (metadata.localStateVersion > metadata.lastPushedLocalVersion) return@withContext true
        if (metadata.lastPushedLocalVersion == 0L) return@withContext true
        if (remoteManifest == null) return@withContext true

        val remoteLooksEmpty = remoteManifest.entityCounters.rideCount == 0 &&
            remoteManifest.entityCounters.speedTestCount == 0 &&
            remoteManifest.entityCounters.vehicleProfileCount <= 1

        remoteLooksEmpty
    }
}
