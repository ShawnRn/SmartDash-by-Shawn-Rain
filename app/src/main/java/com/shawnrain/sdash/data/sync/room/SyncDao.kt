package com.shawnrain.sdash.data.sync.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PendingMutationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mutation: PendingMutationEntity)

    @Query("SELECT * FROM pending_mutations WHERE status = :status ORDER BY updatedAt ASC LIMIT :limit")
    suspend fun listByStatus(status: String, limit: Int = 100): List<PendingMutationEntity>

    @Query("SELECT * FROM pending_mutations WHERE status = 'PENDING' ORDER BY updatedAt ASC LIMIT :limit")
    suspend fun listPending(limit: Int = 100): List<PendingMutationEntity>

    @Query("UPDATE pending_mutations SET status = 'SYNCING' WHERE id IN (:ids)")
    suspend fun markSyncing(ids: List<String>)

    @Query("UPDATE pending_mutations SET status = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Query("UPDATE pending_mutations SET status = 'FAILED', lastError = :error, retryCount = retryCount + 1 WHERE id IN (:ids)")
    suspend fun markFailed(ids: List<String>, error: String)

    @Query("DELETE FROM pending_mutations WHERE status = 'SYNCED' AND updatedAt < :timestamp")
    suspend fun clearSyncedOlderThan(timestamp: Long)

    @Query("SELECT COUNT(*) FROM pending_mutations WHERE status = 'PENDING' OR status = 'SYNCING'")
    suspend fun countPendingOrSyncing(): Int

    @Query("SELECT * FROM pending_mutations WHERE entityType = :entityType AND entityId = :entityId AND status = 'PENDING' LIMIT 1")
    suspend fun findPendingForEntity(entityType: String, entityId: String): PendingMutationEntity?
}

@Dao
interface SyncMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: SyncMetadataEntity)

    @Update
    suspend fun update(metadata: SyncMetadataEntity)

    @Query("SELECT * FROM sync_metadata WHERE key = 'main' LIMIT 1")
    suspend fun getMain(): SyncMetadataEntity?
}
