package com.shawnrain.sdash.data.sync

import android.content.Context
import com.shawnrain.sdash.data.sync.room.PendingMutationEntity
import com.shawnrain.sdash.data.sync.room.SyncDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages the local pending mutation queue for V2 sync.
 */
class PendingMutationRepository(context: Context) {
    private val dao = SyncDatabase.getInstance(context).pendingMutationDao()

    suspend fun enqueue(mutation: PendingMutation) = withContext(Dispatchers.IO) {
        val entity = PendingMutationEntity(
            id = mutation.id,
            entityType = mutation.entityType.name,
            entityId = mutation.entityId,
            operation = mutation.operation.name,
            updatedAt = mutation.updatedAt,
            localStateVersion = mutation.localStateVersion,
            deviceId = mutation.deviceId,
            payloadHash = mutation.payloadHash,
            status = mutation.status.name,
            retryCount = mutation.retryCount,
            lastError = mutation.lastError
        )
        dao.insert(entity)
    }

    suspend fun enqueueForRide(rideId: String, localStateVersion: Long, deviceId: String) {
        enqueue(
            PendingMutation(
                entityType = SyncEntityType.RIDE,
                entityId = rideId,
                operation = SyncOperation.UPSERT,
                localStateVersion = localStateVersion,
                deviceId = deviceId
            )
        )
    }

    suspend fun enqueueForSettings(localStateVersion: Long, deviceId: String) {
        enqueue(
            PendingMutation(
                entityType = SyncEntityType.SETTINGS,
                entityId = "settings",
                operation = SyncOperation.UPSERT,
                localStateVersion = localStateVersion,
                deviceId = deviceId
            )
        )
    }

    suspend fun enqueueForVehicleProfile(profileId: String, localStateVersion: Long, deviceId: String) {
        enqueue(
            PendingMutation(
                entityType = SyncEntityType.VEHICLE_PROFILE,
                entityId = profileId,
                operation = SyncOperation.UPSERT,
                localStateVersion = localStateVersion,
                deviceId = deviceId
            )
        )
    }

    suspend fun enqueueForSpeedTest(testId: String, localStateVersion: Long, deviceId: String) {
        enqueue(
            PendingMutation(
                entityType = SyncEntityType.SPEED_TEST,
                entityId = testId,
                operation = SyncOperation.UPSERT,
                localStateVersion = localStateVersion,
                deviceId = deviceId
            )
        )
    }

    suspend fun listPending(limit: Int = 100): List<PendingMutation> = withContext(Dispatchers.IO) {
        dao.listPending(limit).map { it.toDomain() }
    }

    suspend fun markSyncing(ids: List<String>) = withContext(Dispatchers.IO) {
        dao.markSyncing(ids)
    }

    suspend fun markSynced(ids: List<String>) = withContext(Dispatchers.IO) {
        dao.markSynced(ids)
    }

    suspend fun markFailed(ids: List<String>, error: String) = withContext(Dispatchers.IO) {
        dao.markFailed(ids, error)
    }

    suspend fun clearSyncedOlderThan(timestamp: Long) = withContext(Dispatchers.IO) {
        dao.clearSyncedOlderThan(timestamp)
    }

    suspend fun hasPendingMutations(): Boolean = withContext(Dispatchers.IO) {
        dao.countPendingOrSyncing() > 0
    }

    /**
     * Coalesce mutations for the same entity into a single UPSERT.
     * E.g., if a ride is modified multiple times before sync, only send the latest.
     */
    suspend fun getCoalescedPending(limit: Int = 100): List<PendingMutation> = withContext(Dispatchers.IO) {
        val pending = dao.listPending(limit)
        val coalesced = mutableMapOf<String, PendingMutationEntity>()

        for (entity in pending) {
            val key = "${entity.entityType}:${entity.entityId}"
            val existing = coalesced[key]
            if (existing == null || entity.updatedAt > existing.updatedAt) {
                coalesced[key] = entity
            } else if (existing.operation == "UPSERT" && entity.operation == "DELETE") {
                // DELETE wins over UPSERT
                coalesced[key] = entity
            }
        }

        coalesced.values.map { it.toDomain() }
    }

    private fun PendingMutationEntity.toDomain() = PendingMutation(
        id = id,
        entityType = SyncEntityType.valueOf(entityType),
        entityId = entityId,
        operation = SyncOperation.valueOf(operation),
        updatedAt = updatedAt,
        localStateVersion = localStateVersion,
        deviceId = deviceId,
        payloadHash = payloadHash,
        status = MutationStatus.valueOf(status),
        retryCount = retryCount,
        lastError = lastError
    )
}
