package com.shawnrain.sdash.data.sync.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_mutations")
data class PendingMutationEntity(
    @PrimaryKey
    val id: String,
    val entityType: String,  // "SETTINGS", "VEHICLE_PROFILE", "RIDE", "SPEED_TEST"
    val entityId: String,
    val operation: String,   // "UPSERT", "DELETE"
    val updatedAt: Long,
    val localStateVersion: Long,
    val deviceId: String,
    val payloadHash: String?,
    val status: String,      // "PENDING", "SYNCING", "SYNCED", "FAILED"
    val retryCount: Int,
    val lastError: String?
)

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey
    val key: String = "main",  // Single row for now
    val deviceId: String,
    val deviceName: String,
    val localStateVersion: Long,
    val lastPushedLocalVersion: Long,
    val lastAppliedRemoteVersion: Long,
    val lastKnownRemoteVersion: Long,
    val lastPushSuccessAt: Long,
    val lastPullSuccessAt: Long,
    val lastSyncError: String?,
    val migrationVersion: Int
)
