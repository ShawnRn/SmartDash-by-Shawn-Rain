package com.shawnrain.sdash.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.sync.DriveManifestRepository
import com.shawnrain.sdash.data.sync.GoogleDriveSyncManager
import com.shawnrain.sdash.data.sync.SyncMetadataRepository
import com.shawnrain.sdash.data.sync.SyncTriggerReason
import com.shawnrain.sdash.debug.AppLogger
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that checks for remote updates on Google Drive.
 *
 * Runs every 30 minutes (minimum allowed by WorkManager).
 * Only reads the manifest to check if there's a newer version,
 * then enqueues a full PullWorker if needed.
 */
class PeriodicDriveSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "PeriodicDriveSync"
        const val WORK_NAME = "periodic_drive_sync"
        private const val SYNC_INTERVAL_MINUTES = 30L

        /**
         * Enqueue the periodic sync worker.
         * Only active when user is signed in and sync is enabled.
         */
        fun enqueuePeriodicSync(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<PeriodicDriveSyncWorker>(
                SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            AppLogger.i(TAG, "Periodic sync worker enqueued (every $SYNC_INTERVAL_MINUTES min)")
        }

        /**
         * Cancel the periodic sync worker.
         */
        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            AppLogger.i(TAG, "Periodic sync worker cancelled")
        }
    }

    private val driveSyncManager by lazy { GoogleDriveSyncManager(applicationContext) }
    private val metadataRepository by lazy { SyncMetadataRepository(applicationContext) }
    private val manifestRepository by lazy { DriveManifestRepository(driveSyncManager) }

    override suspend fun doWork(): Result {
        AppLogger.d(TAG, "Periodic sync check started")

        return try {
            if (!driveSyncManager.isSignedIn()) {
                AppLogger.d(TAG, "Periodic sync check: not signed in")
                return Result.success()
            }

            val metadata = metadataRepository.getMetadata(applicationContext)

            // Only check manifest, don't download full state
            val remoteManifest = manifestRepository.fetchRemoteManifest()
            if (remoteManifest != null && remoteManifest.stateVersion > metadata.lastAppliedRemoteVersion) {
                AppLogger.i(TAG, "Periodic sync check: remote is newer (${remoteManifest.stateVersion} > ${metadata.lastAppliedRemoteVersion})")
                // Enqueue a pull worker
                DrivePullWorker.enqueuePull(applicationContext, SyncTriggerReason.PERIODIC_CHECK)
            } else {
                AppLogger.d(TAG, "Periodic sync check: no new updates")
            }

            Result.success()
        } catch (e: Exception) {
            AppLogger.w(TAG, "Periodic sync check failed: ${e.message}")
            Result.success() // Don't retry periodic work
        }
    }
}
