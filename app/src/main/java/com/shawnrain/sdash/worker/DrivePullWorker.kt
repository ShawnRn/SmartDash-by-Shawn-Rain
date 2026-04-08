package com.shawnrain.sdash.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.sync.DriveManifestRepository
import com.shawnrain.sdash.data.sync.DriveStateMerger
import com.shawnrain.sdash.data.sync.DriveStateSerializer
import com.shawnrain.sdash.data.sync.DriveSyncCoordinator
import com.shawnrain.sdash.data.sync.GoogleDriveSyncManager
import com.shawnrain.sdash.data.sync.PendingMutationRepository
import com.shawnrain.sdash.data.sync.SyncMetadataRepository
import com.shawnrain.sdash.data.sync.SyncTriggerReason
import com.shawnrain.sdash.debug.AppLogger
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for pulling and merging remote state from Google Drive.
 *
 * Triggered by:
 * - App foreground
 * - Auth success
 * - Periodic check
 * - Manual refresh
 */
class DrivePullWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DrivePullWorker"
        const val KEY_REASON = "reason"
        const val WORK_NAME = "drive_pull_worker"

        /**
         * Enqueue a pull worker via WorkManager.
         */
        fun enqueuePull(context: Context, reason: SyncTriggerReason) {
            val workRequest = OneTimeWorkRequestBuilder<DrivePullWorker>()
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
            AppLogger.i(TAG, "Pull worker enqueued: reason=$reason")
        }
    }

    private val driveSyncManager by lazy { GoogleDriveSyncManager(applicationContext) }
    private val settingsRepository by lazy { SettingsRepository(applicationContext) }
    private val metadataRepository by lazy { SyncMetadataRepository(applicationContext) }
    private val mutationRepository by lazy { PendingMutationRepository(applicationContext) }
    private val stateSerializer by lazy { DriveStateSerializer(applicationContext, settingsRepository) }
    private val stateMerger by lazy { DriveStateMerger(applicationContext, settingsRepository) }
    private val manifestRepository by lazy { DriveManifestRepository(applicationContext, driveSyncManager) }
    private val coordinator by lazy {
        DriveSyncCoordinator(
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
            val result = coordinator.runPullNow(reason)
            when (result) {
                is com.shawnrain.sdash.data.sync.SyncRunResult.Success -> {
                    AppLogger.i(TAG, "Worker success: ${result.notes.joinToString("; ")}")
                    Result.success()
                }
                is com.shawnrain.sdash.data.sync.SyncRunResult.Skipped -> {
                    AppLogger.i(TAG, "Worker skipped: ${result.reason}")
                    Result.success()
                }
                is com.shawnrain.sdash.data.sync.SyncRunResult.Failure -> {
                    AppLogger.w(TAG, "Worker failed: ${result.error.message}")
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
                else -> Result.success()
            }
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
