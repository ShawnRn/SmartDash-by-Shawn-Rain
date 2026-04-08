package com.shawnrain.sdash.data.sync

import android.content.Context
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.debug.AppLogger
import com.shawnrain.sdash.worker.DrivePullWorker
import com.shawnrain.sdash.worker.DrivePushWorker
import com.shawnrain.sdash.worker.PeriodicDriveSyncWorker
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
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

        @Volatile
        private var settingsDebounceJob: Job? = null

        @Volatile
        private var lastForegroundPullAtMs: Long = 0L
    }

    private val metadataRepository = SyncMetadataRepository(context)
    private val mutationRepository = PendingMutationRepository(context)

    /**
     * Call this when a ride is saved.
     * Enqueues a mutation for the ride and schedules a push.
     */
    suspend fun onRideSaved(rideId: String) = withContext(Dispatchers.IO) {
        try {
            val metadata = metadataRepository.getMetadata(context)
            val newVersion = metadata.localStateVersion + 1

            mutationRepository.enqueueForRide(rideId, newVersion, metadata.deviceId)
            metadataRepository.incrementLocalStateVersion(context)

            DrivePushWorker.enqueuePush(context, SyncTriggerReason.RIDE_ENDED)
            AppLogger.i(TAG, "Ride saved sync scheduled: rideId=$rideId")
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to schedule ride sync: ${e.message}")
        }
    }

    /**
     * Call this when a speed test is saved.
     */
    suspend fun onSpeedTestSaved(testId: String) = withContext(Dispatchers.IO) {
        try {
            val metadata = metadataRepository.getMetadata(context)
            val newVersion = metadata.localStateVersion + 1

            mutationRepository.enqueueForSpeedTest(testId, newVersion, metadata.deviceId)
            metadataRepository.incrementLocalStateVersion(context)

            DrivePushWorker.enqueuePush(context, SyncTriggerReason.SPEED_TEST_COMPLETED)
            AppLogger.i(TAG, "Speed test saved sync scheduled: testId=$testId")
        } catch (e: Exception) {
            AppLogger.w(TAG, "Failed to schedule speed test sync: ${e.message}")
        }
    }

    /**
     * Call this when settings change.
     * Debounces multiple settings changes within 30 seconds.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun onSettingsChanged() {
        settingsDebounceJob?.cancel()

        settingsDebounceJob = GlobalScope.launch(Dispatchers.IO) {
            try {
                kotlinx.coroutines.delay(30_000)

                val metadata = metadataRepository.getMetadata(context)
                val newVersion = metadata.localStateVersion + 1

                mutationRepository.enqueueForSettings(newVersion, metadata.deviceId)
                metadataRepository.incrementLocalStateVersion(context)

                DrivePushWorker.enqueuePush(context, SyncTriggerReason.SETTINGS_CHANGED)
                AppLogger.i(TAG, "Settings change sync scheduled (debounced)")
            } catch (e: kotlinx.coroutines.CancellationException) {
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
    @OptIn(DelicateCoroutinesApi::class)
    fun onAuthSuccess() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                AppLogger.i(TAG, "Auth success: initializing V2 sync")

                val driveSyncManager = GoogleDriveSyncManager(context)
                val stateSerializer = DriveStateSerializer(context, settingsRepository)
                val stateMerger = DriveStateMerger(context, settingsRepository)
                val manifestRepository = DriveManifestRepository(context, driveSyncManager)
                val coordinator = DriveSyncCoordinator(
                    context = context,
                    driveSyncManager = driveSyncManager,
                    settingsRepository = settingsRepository,
                    stateSerializer = stateSerializer,
                    stateMerger = stateMerger,
                    manifestRepository = manifestRepository,
                    metadataRepository = metadataRepository,
                    mutationRepository = mutationRepository
                )

                coordinator.initializeV2Sync()

                // Start periodic sync
                PeriodicDriveSyncWorker.enqueuePeriodicSync(context)

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
        DrivePullWorker.enqueuePull(context, SyncTriggerReason.MANUAL_SYNC)
    }

    /**
     * Check if there are pending mutations waiting to be synced.
     */
    suspend fun hasPendingMutations(): Boolean {
        return mutationRepository.hasPendingMutations()
    }
}
