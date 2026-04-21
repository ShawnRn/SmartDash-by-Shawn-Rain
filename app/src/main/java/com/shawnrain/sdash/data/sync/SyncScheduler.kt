package com.shawnrain.sdash.data.sync

import android.content.Context
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.history.RideHistoryRepository
import com.shawnrain.sdash.debug.AppLogger
import com.shawnrain.sdash.worker.DrivePullWorker
import com.shawnrain.sdash.worker.DrivePushWorker
import com.shawnrain.sdash.worker.DriveReconcileWorker
import com.shawnrain.sdash.worker.PeriodicDriveSyncWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * High-level sync scheduler that business code calls to trigger sync events.
 *
 * This is the main entry point for:
 * - Ride end → enqueue mutation + schedule push
 * - Speed test complete → enqueue mutation + schedule push
 * - Settings change → enqueue mutation + schedule push (debounced)
 * - Vehicle profile change → enqueue mutation + schedule push
 * - App foreground → schedule pull
 * - Auth success → schedule pull + periodic sync
 * - Manual sync → reconcile (pull then push)
 */
class SyncScheduler(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    companion object {
        private const val TAG = "SyncScheduler"
        private const val FOREGROUND_PULL_COOLDOWN_MS = 10 * 60 * 1000L
        private const val SYNC_PAYLOAD_REVISION = 3

        @Volatile
        private var settingsDebounceJob: Job? = null

        @Volatile
        private var lastForegroundPullAtMs: Long = 0L
    }

    private val schedulerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val metadataRepository = SyncMetadataRepository(context)
    private val mutationRepository = PendingMutationRepository(context)

    /**
     * Call this when a ride is saved.
     * Enqueues a mutation for the ride and schedules a push.
     */
    suspend fun onRideSaved(rideId: String) = withContext(Dispatchers.IO) {
        enqueueRideHistoryMutation(rideId, SyncTriggerReason.RIDE_ENDED)
    }

    suspend fun onRideHistoryChanged(entityId: String = "ride_history") = withContext(Dispatchers.IO) {
        enqueueRideHistoryMutation(entityId, SyncTriggerReason.RIDE_ENDED)
    }

    private suspend fun enqueueRideHistoryMutation(entityId: String, reason: SyncTriggerReason) {
        try {
            val metadata = metadataRepository.getMetadata(context)
            val newVersion = metadata.localStateVersion + 1

            mutationRepository.enqueueForRide(entityId, newVersion, metadata.deviceId)
            metadataRepository.incrementLocalStateVersion(context)

            DrivePushWorker.enqueuePush(context, reason)
            AppLogger.i(TAG, "Ride history sync scheduled: entityId=$entityId reason=$reason")
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to schedule ride history sync: ${e.message}")
        }
    }

    /**
     * Call this when a speed test is saved.
     */
    suspend fun onSpeedTestSaved(testId: String) = withContext(Dispatchers.IO) {
        enqueueSpeedTestMutation(testId, SyncTriggerReason.SPEED_TEST_COMPLETED)
    }

    suspend fun onSpeedTestHistoryChanged(entityId: String = "speed_test_history") = withContext(Dispatchers.IO) {
        enqueueSpeedTestMutation(entityId, SyncTriggerReason.SPEED_TEST_COMPLETED)
    }

    private suspend fun enqueueSpeedTestMutation(entityId: String, reason: SyncTriggerReason) {
        try {
            val metadata = metadataRepository.getMetadata(context)
            val newVersion = metadata.localStateVersion + 1

            mutationRepository.enqueueForSpeedTest(entityId, newVersion, metadata.deviceId)
            metadataRepository.incrementLocalStateVersion(context)

            DrivePushWorker.enqueuePush(context, reason)
            AppLogger.i(TAG, "Speed test sync scheduled: entityId=$entityId reason=$reason")
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to schedule speed test sync: ${e.message}")
        }
    }

    /**
     * Call this when settings change.
     * Debounces multiple settings changes within 30 seconds.
     */
    fun onSettingsChanged() {
        settingsDebounceJob?.cancel()

        settingsDebounceJob = schedulerScope.launch {
            try {
                kotlinx.coroutines.delay(30_000)
                enqueueSettingsMutation(pushImmediately = true)
                AppLogger.i(TAG, "Settings change sync scheduled (debounced)")
            } catch (e: CancellationException) {
                AppLogger.d(TAG, "Settings change sync debounced")
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to schedule settings sync: ${e.message}")
            }
        }
    }

    /**
     * Call this when a vehicle profile changes.
     */
    suspend fun onVehicleProfileChanged(profileId: String) = withContext(Dispatchers.IO) {
        try {
            val metadata = metadataRepository.getMetadata(context)
            val newVersion = metadata.localStateVersion + 1

            mutationRepository.enqueueForVehicleProfile(profileId, newVersion, metadata.deviceId)
            metadataRepository.incrementLocalStateVersion(context)

            DrivePushWorker.enqueuePush(context, SyncTriggerReason.VEHICLE_PROFILE_CHANGED)
            AppLogger.i(TAG, "Vehicle profile change sync scheduled: profileId=$profileId")
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to schedule vehicle profile sync: ${e.message}")
        }
    }

    /**
     * Call this when the app comes to foreground.
     * Schedules a pull to get any remote updates.
     */
    fun onAppForeground() {
        val now = System.currentTimeMillis()
        schedulerScope.launch {
            flushPendingSettingsMutationIfNeeded(SyncTriggerReason.APP_FOREGROUND)
            ensurePayloadRefreshScheduledIfNeeded(SyncTriggerReason.APP_FOREGROUND)
            ensureLocalPushScheduledIfNeeded(SyncTriggerReason.APP_FOREGROUND)
        }
        if (now - lastForegroundPullAtMs < FOREGROUND_PULL_COOLDOWN_MS) {
            AppLogger.d(TAG, "Skip foreground pull: still in cooldown")
            return
        }
        val signedIn = runCatching { GoogleDriveSyncManager(context).isSignedIn() }.getOrDefault(false)
        if (!signedIn) {
            AppLogger.d(TAG, "Skip foreground pull: Drive not signed in")
            return
        }
        lastForegroundPullAtMs = now
        DrivePullWorker.enqueuePull(context, SyncTriggerReason.APP_FOREGROUND)
    }

    /**
     * Call this when user signs in to Google Drive.
     * Initializes V2 sync and starts periodic sync.
     */
    fun onAuthSuccess() {
        schedulerScope.launch {
            try {
                AppLogger.i(TAG, "Auth success: initializing V2 sync")
                flushPendingSettingsMutationIfNeeded(SyncTriggerReason.AUTH_SUCCESS)

                val driveSyncManager = GoogleDriveSyncManager(context)
                val rideHistoryRepository = RideHistoryRepository(context)
                val stateSerializer = DriveStateSerializer(context, settingsRepository, rideHistoryRepository)
                val stateMerger = DriveStateMerger(context, settingsRepository)
                val manifestRepository = DriveManifestRepository(driveSyncManager)
                val coordinator = DriveSyncCoordinator(
                    context = context,
                    driveSyncManager = driveSyncManager,
                    settingsRepository = settingsRepository,
                    rideHistoryRepository = rideHistoryRepository,
                    stateSerializer = stateSerializer,
                    stateMerger = stateMerger,
                    manifestRepository = manifestRepository,
                    metadataRepository = metadataRepository,
                    mutationRepository = mutationRepository
                )

                coordinator.initializeV2Sync()

                // Start periodic sync
                PeriodicDriveSyncWorker.enqueuePeriodicSync(context)
                ensurePayloadRefreshScheduledIfNeeded(SyncTriggerReason.AUTH_SUCCESS)
                ensureLocalPushScheduledIfNeeded(SyncTriggerReason.AUTH_SUCCESS)

                // Also do an immediate pull
                DrivePullWorker.enqueuePull(context, SyncTriggerReason.AUTH_SUCCESS)
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to initialize V2 sync on auth: ${e.message}")
            }
        }
    }

    /**
     * Call this when user signs out.
     * Cancels all workers.
     */
    fun onSignOut() {
        PeriodicDriveSyncWorker.cancelPeriodicSync(context)
        AppLogger.i(TAG, "Sign out: cancelled all sync workers")
    }

    /**
     * Call this for manual sync (pull then push).
     */
    fun onManualSync() {
        schedulerScope.launch {
            try {
                val pendingSettingsJob = settingsDebounceJob
                if (pendingSettingsJob?.isActive == true) {
                    pendingSettingsJob.cancel()
                    enqueueSettingsMutation(pushImmediately = false)
                    AppLogger.i(TAG, "Manual sync flushed pending settings changes before reconcile")
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to flush pending settings before manual sync: ${e.message}")
            } finally {
                DriveReconcileWorker.scheduleReconcile(context, SyncTriggerReason.MANUAL_SYNC)
            }
        }
    }

    fun forceUploadCurrentDeviceData() {
        schedulerScope.launch {
            try {
                val signedIn = runCatching { GoogleDriveSyncManager(context).isSignedIn() }.getOrDefault(false)
                if (!signedIn) {
                    AppLogger.w(TAG, "Skip forced upload: Drive not signed in")
                    return@launch
                }
                val metadata = metadataRepository.getMetadata(context)
                val newVersion = metadata.localStateVersion + 1
                mutationRepository.enqueueForSettings(newVersion, metadata.deviceId)
                metadataRepository.incrementLocalStateVersion(context)
                metadataRepository.updateMigrationVersion(context, SYNC_PAYLOAD_REVISION)
                DrivePushWorker.enqueuePush(context, SyncTriggerReason.MANUAL_SYNC)
                AppLogger.i(TAG, "Forced upload scheduled for current device data")
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to schedule forced upload: ${e.message}")
            }
        }
    }

    private suspend fun enqueueSettingsMutation(pushImmediately: Boolean) = withContext(Dispatchers.IO) {
        val metadata = metadataRepository.getMetadata(context)
        val newVersion = metadata.localStateVersion + 1

        mutationRepository.enqueueForSettings(newVersion, metadata.deviceId)
        metadataRepository.incrementLocalStateVersion(context)

        if (pushImmediately) {
            DrivePushWorker.enqueuePush(context, SyncTriggerReason.SETTINGS_CHANGED)
        }
    }

    /**
     * Check if there are pending mutations waiting to be synced.
     */
    suspend fun hasPendingMutations(): Boolean {
        return mutationRepository.hasPendingMutations()
    }

    private suspend fun flushPendingSettingsMutationIfNeeded(reason: SyncTriggerReason) = withContext(Dispatchers.IO) {
        val pendingJob = settingsDebounceJob
        if (pendingJob?.isActive != true) return@withContext

        pendingJob.cancel()
        settingsDebounceJob = null
        enqueueSettingsMutation(pushImmediately = true)
        AppLogger.i(TAG, "Flushed pending settings sync before foreground/auth sync: reason=$reason")
    }

    private suspend fun ensurePayloadRefreshScheduledIfNeeded(reason: SyncTriggerReason) = withContext(Dispatchers.IO) {
        val signedIn = runCatching { GoogleDriveSyncManager(context).isSignedIn() }.getOrDefault(false)
        if (!signedIn) return@withContext

        val metadata = metadataRepository.getMetadata(context)
        if (metadata.migrationVersion >= SYNC_PAYLOAD_REVISION) return@withContext

        val newVersion = metadata.localStateVersion + 1
        mutationRepository.enqueueForSettings(newVersion, metadata.deviceId)
        metadataRepository.incrementLocalStateVersion(context)
        metadataRepository.updateMigrationVersion(context, SYNC_PAYLOAD_REVISION)

        DrivePushWorker.enqueuePush(context, reason)
        AppLogger.i(
            TAG,
            "Sync payload refresh scheduled: revision=$SYNC_PAYLOAD_REVISION reason=$reason"
        )
    }

    private suspend fun ensureLocalPushScheduledIfNeeded(reason: SyncTriggerReason) = withContext(Dispatchers.IO) {
        val signedIn = runCatching { GoogleDriveSyncManager(context).isSignedIn() }.getOrDefault(false)
        if (!signedIn) return@withContext

        val metadata = metadataRepository.getMetadata(context)
        val hasPending = mutationRepository.hasPendingMutations()
        val localAhead = metadata.localStateVersion > metadata.lastPushedLocalVersion
        if (!hasPending && !localAhead) return@withContext

        val remoteManifest = runCatching {
            DriveManifestRepository(GoogleDriveSyncManager(context)).fetchRemoteManifest()
        }.getOrNull()
        val remoteAhead = remoteManifest?.stateVersion?.let { it > metadata.lastAppliedRemoteVersion } == true
        if (remoteAhead) {
            AppLogger.i(
                TAG,
                "Skip local recovery push: remote newer stateVersion=${remoteManifest?.stateVersion} " +
                    "lastApplied=${metadata.lastAppliedRemoteVersion} reason=$reason"
            )
            return@withContext
        }

        DrivePushWorker.enqueuePush(context, reason)
        AppLogger.i(
            TAG,
            "Scheduled recovery push: localVersion=${metadata.localStateVersion} " +
                "lastPushed=${metadata.lastPushedLocalVersion} hasPending=$hasPending reason=$reason"
        )
    }
}
