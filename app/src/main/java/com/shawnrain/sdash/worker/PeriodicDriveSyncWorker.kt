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
import com.shawnrain.sdash.data.sync.v3.DriveEntityStore
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
    private val v3EntityStore by lazy { DriveEntityStore(driveSyncManager) }

    override suspend fun doWork(): Result {
        AppLogger.d(TAG, "Periodic sync check started")

        return try {
            if (!driveSyncManager.isSignedIn()) {
                AppLogger.d(TAG, "Periodic sync check: not signed in")
                return Result.success()
            }

            val metadata = metadataRepository.getMetadata(applicationContext)
            val v3Manifest = v3EntityStore.fetchManifest()
            if (v3Manifest != null) {
                if (v3Manifest.updatedAt > metadata.lastAppliedRemoteVersion) {
                    AppLogger.i(
                        TAG,
                        "Periodic sync check: V3 remote is newer (${v3Manifest.updatedAt} > ${metadata.lastAppliedRemoteVersion})"
                    )
                    DrivePullWorker.enqueuePull(applicationContext, SyncTriggerReason.PERIODIC_CHECK)
                } else {
                    AppLogger.d(TAG, "Periodic sync check: V3 remote already applied")
                }
                return Result.success()
            }

            val legacyManifest = manifestRepository.fetchRemoteManifest()
            if (legacyManifest != null) {
                AppLogger.i(TAG, "Periodic sync check: legacy V2 state found, enqueue V3 migration")
                DrivePullWorker.enqueuePull(applicationContext, SyncTriggerReason.PERIODIC_CHECK)
            } else {
                AppLogger.d(TAG, "Periodic sync check: no V3 or legacy state found")
            }

            Result.success()
        } catch (e: Exception) {
            AppLogger.w(TAG, "Periodic sync check failed: ${e.message}")
            Result.success() // Don't retry periodic work
        }
    }
}
