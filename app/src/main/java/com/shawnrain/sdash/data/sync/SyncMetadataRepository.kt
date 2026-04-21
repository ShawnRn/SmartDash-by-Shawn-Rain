package com.shawnrain.sdash.data.sync

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.shawnrain.sdash.data.sync.room.SyncDatabase
import com.shawnrain.sdash.data.sync.room.SyncMetadataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages sync metadata: deviceId, version tracking, push/pull timestamps.
 */
class SyncMetadataRepository(context: Context) {
    private val dao = SyncDatabase.getInstance(context).syncMetadataDao()

    /**
     * Get or create the stable device ID for this installation.
     * Uses Android ID as a stable identifier.
     */
    suspend fun getDeviceId(context: Context): String = withContext(Dispatchers.IO) {
        val existing = dao.getMain()
        if (existing != null) {
            existing.deviceId
        } else {
            val newDeviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: java.util.UUID.randomUUID().toString()
            val entity = SyncMetadataEntity(
                deviceId = newDeviceId,
                deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim().ifBlank { "Android Device" },
                localStateVersion = 0L,
                lastPushedLocalVersion = 0L,
                lastAppliedRemoteVersion = 0L,
                lastKnownRemoteVersion = 0L,
                lastPushSuccessAt = 0L,
                lastPullSuccessAt = 0L,
                lastSyncError = null,
                migrationVersion = 2  // V2 sync engine
            )
            dao.insert(entity)
            newDeviceId
        }
    }

    suspend fun getMetadata(context: Context): SyncMetadata = withContext(Dispatchers.IO) {
        val entity = dao.getMain()
        if (entity != null) {
            SyncMetadata(
                deviceId = entity.deviceId,
                deviceName = entity.deviceName,
                localStateVersion = entity.localStateVersion,
                lastPushedLocalVersion = entity.lastPushedLocalVersion,
                lastAppliedRemoteVersion = entity.lastAppliedRemoteVersion,
                lastKnownRemoteVersion = entity.lastKnownRemoteVersion,
                lastPushSuccessAt = entity.lastPushSuccessAt,
                lastPullSuccessAt = entity.lastPullSuccessAt,
                lastSyncError = entity.lastSyncError,
                migrationVersion = entity.migrationVersion
            )
        } else {
            // Initialize on first access
            getDeviceId(context)
            val newEntity = dao.getMain()!!
            SyncMetadata(
                deviceId = newEntity.deviceId,
                deviceName = newEntity.deviceName,
                localStateVersion = newEntity.localStateVersion,
                lastPushedLocalVersion = newEntity.lastPushedLocalVersion,
                lastAppliedRemoteVersion = newEntity.lastAppliedRemoteVersion,
                lastKnownRemoteVersion = newEntity.lastKnownRemoteVersion,
                lastPushSuccessAt = newEntity.lastPushSuccessAt,
                lastPullSuccessAt = newEntity.lastPullSuccessAt,
                lastSyncError = newEntity.lastSyncError,
                migrationVersion = newEntity.migrationVersion
            )
        }
    }

    suspend fun incrementLocalStateVersion(context: Context): Long = withContext(Dispatchers.IO) {
        val meta = getMetadata(context)
        val newVersion = meta.localStateVersion + 1
        updateEntity(context) { it.copy(localStateVersion = newVersion) }
        newVersion
    }

    suspend fun recordPushSuccess(context: Context, version: Long) = withContext(Dispatchers.IO) {
        updateEntity(context) {
            it.copy(
                localStateVersion = maxOf(it.localStateVersion, version),
                lastPushedLocalVersion = version,
                lastPushSuccessAt = System.currentTimeMillis(),
                lastSyncError = null
            )
        }
    }

    suspend fun recordPullSuccess(context: Context, remoteVersion: Long) = withContext(Dispatchers.IO) {
        updateEntity(context) {
            it.copy(
                lastAppliedRemoteVersion = remoteVersion,
                lastKnownRemoteVersion = remoteVersion,
                lastPullSuccessAt = System.currentTimeMillis(),
                lastSyncError = null
            )
        }
    }

    suspend fun recordRemoteDiscovered(context: Context, remoteVersion: Long) = withContext(Dispatchers.IO) {
        updateEntity(context) {
            it.copy(
                lastKnownRemoteVersion = remoteVersion,
                lastSyncError = null
            )
        }
    }

    suspend fun recordSyncError(context: Context, error: String) = withContext(Dispatchers.IO) {
        updateEntity(context) { it.copy(lastSyncError = error) }
    }

    suspend fun updateMigrationVersion(context: Context, migrationVersion: Int) = withContext(Dispatchers.IO) {
        updateEntity(context) {
            if (it.migrationVersion >= migrationVersion) it else it.copy(migrationVersion = migrationVersion)
        }
    }

    private suspend fun updateEntity(context: Context, transform: (SyncMetadata) -> SyncMetadata) {
        val meta = getMetadata(context)
        val updated = transform(meta)
        val entity = SyncMetadataEntity(
            deviceId = updated.deviceId,
            deviceName = updated.deviceName,
            localStateVersion = updated.localStateVersion,
            lastPushedLocalVersion = updated.lastPushedLocalVersion,
            lastAppliedRemoteVersion = updated.lastAppliedRemoteVersion,
            lastKnownRemoteVersion = updated.lastKnownRemoteVersion,
            lastPushSuccessAt = updated.lastPushSuccessAt,
            lastPullSuccessAt = updated.lastPullSuccessAt,
            lastSyncError = updated.lastSyncError,
            migrationVersion = updated.migrationVersion
        )
        dao.update(entity)
    }
}
