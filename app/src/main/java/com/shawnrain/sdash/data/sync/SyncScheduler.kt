package com.shawnrain.sdash.data.sync

import android.content.Context
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.history.RideHistoryRepository
import com.shawnrain.sdash.data.sync.v3.DriveEntityStore
import com.shawnrain.sdash.data.sync.v3.DriveV3Coordinator
import com.shawnrain.sdash.data.sync.v3.DriveV3LegacyMigrator
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
 * - App foreground → schedule V3 pull/migration
 * - Auth success → one-time V3 reconcile
 * - Manual sync → V3 reconcile (pull then push)
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

    suspend fun onRideDeleted(rideId: String) = withContext(Dispatchers.IO) {
        try {
            val metadata = metadataRepository.getMetadata(context)
            val newVersion = metadata.localStateVersion + 1

            mutationRepository.enqueueDeleteForRide(rideId, newVersion, metadata.deviceId)
            metadataRepository.incrementLocalStateVersion(context)

            DrivePushWorker.enqueuePush(context, SyncTriggerReason.RIDE_ENDED)
            AppLogger.i(TAG, "Ride deletion sync scheduled: rideId=$rideId")
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to schedule ride deletion sync: ${e.message}")
        }
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
                kotlinx.coroutines.delay(5_000)
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

    suspend fun onVehicleProfileDeleted(profileId: String) = withContext(Dispatchers.IO) {
        try {
            val metadata = metadataRepository.getMetadata(context)
            val newVersion = metadata.localStateVersion + 1

            mutationRepository.enqueueDeleteForVehicleProfile(profileId, newVersion, metadata.deviceId)
            metadataRepository.incrementLocalStateVersion(context)

            DrivePushWorker.enqueuePush(context, SyncTriggerReason.VEHICLE_PROFILE_CHANGED)
            AppLogger.i(TAG, "Vehicle profile deletion sync scheduled: profileId=$profileId")
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to schedule vehicle profile deletion sync: ${e.message}")
        }
    }

    /**
     * Call this when the app goes to background.
     * Flushes any pending settings changes and schedules a background push.
     * Also attempts an immediate process-level synchronization as a fast path.
     */
    fun onAppBackground() {
        schedulerScope.launch {
            try {
                AppLogger.i(TAG, "App went to background: flushing pending mutations")
                flushPendingSettingsMutationIfNeeded(SyncTriggerReason.APP_BACKGROUND)

                val signedIn = runCatching { GoogleDriveSyncManager(context).isSignedIn() }.getOrDefault(false)
                if (signedIn && mutationRepository.hasPendingMutations()) {
                    // Dual-track sync: Fast path in-process backup upload before process gets suspended
                    kotlinx.coroutines.withTimeoutOrNull(8_000) {
                        AppLogger.i(TAG, "Starting fast-path process-level background sync...")
                        val driveSyncManager = GoogleDriveSyncManager(context)
                        val rideHistoryRepository = RideHistoryRepository(context)
                        val stateSerializer = DriveStateSerializer(context, settingsRepository, rideHistoryRepository)
                        val entityStore = DriveEntityStore(driveSyncManager)
                        val coordinator = DriveV3Coordinator(
                            context = context,
                            settingsRepository = settingsRepository,
                            rideHistoryRepository = rideHistoryRepository,
                            driveSyncManager = driveSyncManager,
                            metadataRepository = metadataRepository,
                            mutationRepository = mutationRepository,
                            entityStore = entityStore
                        )
                        val stateMerger = DriveStateMerger(context, settingsRepository)
                        val manifestRepository = DriveManifestRepository(driveSyncManager)
                        val migrator = DriveV3LegacyMigrator(
                            context = context,
                            settingsRepository = settingsRepository,
                            rideHistoryRepository = rideHistoryRepository,
                            driveSyncManager = driveSyncManager,
                            metadataRepository = metadataRepository,
                            legacyManifestRepository = manifestRepository,
                            stateSerializer = stateSerializer,
                            stateMerger = stateMerger,
                            entityStore = entityStore,
                            v3Coordinator = coordinator
                        )

                        val result = migrator.reconcileAndPublish()
                        AppLogger.i(
                            TAG,
                            "Fast-path background sync success: uploaded entities=${result.uploadedEntityCount}"
                        )
                    } ?: AppLogger.w(TAG, "Fast-path background sync timed out (8s limit reached)")
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Error in background fast-path sync: ${e.message}")
            } finally {
                // WorkManager fallback to guarantee completion
                ensureLocalPushScheduledIfNeeded(SyncTriggerReason.APP_BACKGROUND)
            }
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
     * Initializes V3 sync and starts periodic sync.
     */
    fun onAuthSuccess() {
        schedulerScope.launch {
            try {
                AppLogger.i(TAG, "Auth success: reconciling V3 sync snapshot")
                flushPendingSettingsMutationIfNeeded(SyncTriggerReason.AUTH_SUCCESS)

                val driveSyncManager = GoogleDriveSyncManager(context)
                val rideHistoryRepository = RideHistoryRepository(context)
                val stateSerializer = DriveStateSerializer(context, settingsRepository, rideHistoryRepository)
                val manifestRepository = DriveManifestRepository(driveSyncManager)
                val entityStore = DriveEntityStore(driveSyncManager)
                val coordinator = DriveV3Coordinator(
                    context = context,
                    settingsRepository = settingsRepository,
                    rideHistoryRepository = rideHistoryRepository,
                    driveSyncManager = driveSyncManager,
                    metadataRepository = metadataRepository,
                    mutationRepository = mutationRepository,
                    entityStore = entityStore
                )
                val migrator = DriveV3LegacyMigrator(
                    context = context,
                    settingsRepository = settingsRepository,
                    rideHistoryRepository = rideHistoryRepository,
                    driveSyncManager = driveSyncManager,
                    metadataRepository = metadataRepository,
                    legacyManifestRepository = manifestRepository,
                    stateSerializer = stateSerializer,
                    stateMerger = DriveStateMerger(context, settingsRepository),
                    entityStore = entityStore,
                    v3Coordinator = coordinator
                )

                migrator.reconcileAndPublish()

                // Remove jobs persisted by older builds. Foreground pulls and mutation-driven
                // pushes provide freshness without waking an otherwise idle app every 30 minutes.
                PeriodicDriveSyncWorker.cancelPeriodicSync(context)
                ensurePayloadRefreshScheduledIfNeeded(SyncTriggerReason.AUTH_SUCCESS)
                ensureLocalPushScheduledIfNeeded(SyncTriggerReason.AUTH_SUCCESS)
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to initialize V3 sync on auth: ${e.message}")
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

        DrivePushWorker.enqueuePush(context, reason)
        AppLogger.i(
            TAG,
            "Scheduled V3 recovery push: localVersion=${metadata.localStateVersion} " +
                "lastPushed=${metadata.lastPushedLocalVersion} hasPending=$hasPending reason=$reason"
        )
    }
}
