package com.shawnrain.sdash.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.BackoffPolicy
import androidx.work.workDataOf
import com.shawnrain.sdash.data.sync.SyncTriggerReason
import com.shawnrain.sdash.debug.AppLogger
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for reconciling local and remote state conflicts.
 * In V2, this is essentially a pull-then-push cycle.
 */
class DriveReconcileWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DriveReconcileWorker"
        const val KEY_REASON = "reason"
        const val WORK_NAME = "drive_reconcile_worker"

        /**
         * Schedule a one-time reconcile work.
         */
        fun scheduleReconcile(context: Context, reason: SyncTriggerReason) {
            val workRequest = OneTimeWorkRequestBuilder<DriveReconcileWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .setInputData(workDataOf(KEY_REASON to reason.name))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            AppLogger.i(TAG, "Reconcile scheduled: reason=$reason")
        }
    }

    private val driveSyncManager by lazy { com.shawnrain.sdash.data.sync.GoogleDriveSyncManager(applicationContext) }
    private val settingsRepository by lazy { com.shawnrain.sdash.data.SettingsRepository(applicationContext) }
    private val metadataRepository by lazy { com.shawnrain.sdash.data.sync.SyncMetadataRepository(applicationContext) }
    private val mutationRepository by lazy { com.shawnrain.sdash.data.sync.PendingMutationRepository(applicationContext) }
    private val stateSerializer by lazy { com.shawnrain.sdash.data.sync.DriveStateSerializer(applicationContext, settingsRepository) }
    private val stateMerger by lazy { com.shawnrain.sdash.data.sync.DriveStateMerger(applicationContext, settingsRepository) }
    private val manifestRepository by lazy { com.shawnrain.sdash.data.sync.DriveManifestRepository(driveSyncManager) }
    private val coordinator by lazy {
        com.shawnrain.sdash.data.sync.DriveSyncCoordinator(
            context = applicationContext,
            driveSyncManager = driveSyncManager,
            settingsRepository = settingsRepository,
            stateSerializer = stateSerializer,
            stateMerger = stateMerger,
            manifestRepository = manifestRepository,
            metadataRepository = metadataRepository,
            mutationRepository = mutationRepository
        )
    }

    override suspend fun doWork(): Result {
        val reasonStr = inputData.getString(KEY_REASON) ?: "WORKER"
        val reason = try {
            SyncTriggerReason.valueOf(reasonStr)
        } catch (e: IllegalArgumentException) {
            SyncTriggerReason.PERIODIC_CHECK
        }

        AppLogger.i(TAG, "Worker started: reason=$reason")

        return try {
            val result = coordinator.runReconcileNow(reason)
            AppLogger.i(TAG, "Reconcile finished: $result")
            Result.success()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Worker exception", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

}
