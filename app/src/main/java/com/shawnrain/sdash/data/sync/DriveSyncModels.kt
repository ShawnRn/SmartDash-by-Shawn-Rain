package com.shawnrain.sdash.data.sync

import kotlinx.serialization.Serializable

/**
 * V2 Sync Engine Data Models
 * Defines the structures used for Drive-based sync state, manifests, and mutations.
 */

// ─── Cloud State ───────────────────────────────────────────────

@Serializable
data class DriveCurrentState(
    val schemaVersion: Int = 2,
    val stateVersion: Long,
    val updatedAt: Long,
    val updatedByDeviceId: String,
    val updatedByDeviceName: String,
    val activeVehicleProfileId: String = "",
    val settings: SyncSettingsSnapshot = SyncSettingsSnapshot(),
    val vehicleSettings: List<SyncVehicleSettingsSnapshot> = emptyList(),
    val vehicleProfiles: List<SyncVehicleProfileSnapshot> = emptyList(),
    val rides: List<SyncRideSnapshot> = emptyList(),
    val speedTests: List<SyncSpeedTestSnapshot> = emptyList()
)

@Serializable
data class SyncSettingsSnapshot(
    val speedSource: String = "CONTROLLER",
    val battDataSource: String = "CONTROLLER",
    val wheelCircumferenceMm: Float = 1800f,
    val polePairs: Int = 50,
    val controllerBrand: String = "auto",
    val overlayEnabled: Boolean = false,
    val dashboardItems: List<String> = emptyList(),
    val rideOverviewItems: List<String> = emptyList(),
    val driveBackupRetention: String = "KEEP_ALL",
    val posterTemplateId: String = "performance_dark",
    val posterAspectRatio: String = "STORY_9_16",
    val posterShowTrack: Boolean = true,
    val posterShowWatermark: Boolean = true,
    val learnedEfficiencyWhKm: Float = 35f,
    val learnedUsableEnergyRatio: Float = 0.85f,
    val updatedAt: Long = 0L,
    val updatedByDeviceId: String = ""
)

@Serializable
data class SyncVehicleSettingsSnapshot(
    val vehicleProfileId: String,
    val speedSource: String = "CONTROLLER",
    val battDataSource: String = "CONTROLLER",
    val wheelCircumferenceMm: Float = 1800f,
    val polePairs: Int = 50,
    val controllerBrand: String = "auto",
    val dashboardItems: List<String> = emptyList(),
    val rideOverviewItems: List<String> = emptyList(),
    val updatedAt: Long = 0L,
    val updatedByDeviceId: String = ""
)

@Serializable
data class SyncVehicleProfileSnapshot(
    val id: String,
    val name: String,
    val batteryCapacityAh: Float,
    val batterySeries: Int,
    val wheelCircumferenceMm: Float,
    val polePairs: Int,
    val learnedEfficiencyWhKm: Float,
    val learnedUsableEnergyRatio: Float,
    val learnedInternalResistanceOhm: Float,
    val updatedAt: Long,
    val updatedByDeviceId: String,
    val isDeleted: Boolean = false
)

@Serializable
data class SyncRideSnapshot(
    val id: String,
    val vehicleProfileId: String,
    val title: String,
    val startedAtMs: Long,
    val endedAtMs: Long,
    val durationMs: Long,
    val distanceMeters: Float,
    val maxSpeedKmh: Float,
    val avgSpeedKmh: Float,
    val peakPowerKw: Float,
    val totalEnergyWh: Float,
    val tractionEnergyWh: Float = 0f,
    val regenEnergyWh: Float = 0f,
    val avgEfficiencyWhKm: Float,
    val avgNetEfficiencyWhKm: Float,
    val avgTractionEfficiencyWhKm: Float = 0f,
    val maxControllerTemp: Float = 0f,
    val updatedAt: Long,
    val updatedByDeviceId: String,
    val summaryRevision: Int = 1,
    val completenessScore: Float = 1f,
    val sampleCount: Int = 0,
    val trackPoints: List<SyncRideTrackPoint> = emptyList(),
    val samples: List<SyncRideMetricSample> = emptyList(),
    val isDeleted: Boolean = false
)

@Serializable
data class SyncSpeedTestSnapshot(
    val id: String,
    val vehicleProfileId: String,
    val label: String,
    val startedAtMs: Long,
    val endedAtMs: Long,
    val targetSpeedKmh: Float,
    val achievedSpeedKmh: Float,
    val timeToTargetMs: Long,
    val distanceMeters: Float,
    val peakPowerKw: Float,
    val peakBusCurrentA: Float,
    val minVoltageV: Float,
    val updatedAt: Long,
    val updatedByDeviceId: String,
    val trackPoints: List<SyncSpeedTestTrackPoint> = emptyList(),
    val isDeleted: Boolean = false
)

@Serializable
data class SyncRideTrackPoint(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class SyncRideMetricSample(
    val elapsedMs: Long,
    val timestampMs: Long,
    val speedKmH: Float,
    val powerKw: Float,
    val voltage: Float,
    val voltageSag: Float,
    val busCurrent: Float,
    val phaseCurrent: Float,
    val controllerTemp: Float,
    val soc: Float,
    val estimatedRangeKm: Float = 0.0f,
    val rpm: Float,
    val efficiencyWhKm: Float,
    val avgEfficiencyWhKm: Float = 0.0f,
    val avgNetEfficiencyWhKm: Float = 0.0f,
    val avgTractionEfficiencyWhKm: Float = 0.0f,
    val distanceMeters: Float,
    val totalEnergyWh: Float = 0.0f,
    val tractionEnergyWh: Float = 0.0f,
    val regenEnergyWh: Float = 0.0f,
    val recoveredEnergyWh: Float = 0.0f,
    val maxControllerTemp: Float = 0.0f,
    val gradePercent: Float = 0.0f,
    val altitudeMeters: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class SyncSpeedTestTrackPoint(
    val latitude: Double,
    val longitude: Double
)

// ─── Cloud Manifest ────────────────────────────────────────────

@Serializable
data class DriveChangeManifest(
    val schemaVersion: Int = 2,
    val stateVersion: Long,
    val updatedAt: Long,
    val updatedByDeviceId: String,
    val updatedByDeviceName: String,
    val checksum: String = "",
    val latestRideId: String? = null,
    val latestRideEndedAt: Long? = null,
    val latestSpeedTestId: String? = null,
    val entityCounters: EntityCounters = EntityCounters(),
    val currentStateFileName: String = "current_state.json.enc"
)

@Serializable
data class EntityCounters(
    val rideCount: Int = 0,
    val speedTestCount: Int = 0,
    val vehicleProfileCount: Int = 0
)

// ─── Local Mutation Queue ──────────────────────────────────────

enum class SyncEntityType {
    SETTINGS,
    VEHICLE_PROFILE,
    RIDE,
    SPEED_TEST
}

enum class SyncOperation {
    UPSERT,
    DELETE
}

enum class MutationStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED
}

data class PendingMutation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val entityType: SyncEntityType,
    val entityId: String,
    val operation: SyncOperation,
    val updatedAt: Long = System.currentTimeMillis(),
    val localStateVersion: Long,
    val deviceId: String,
    val payloadHash: String? = null,
    val status: MutationStatus = MutationStatus.PENDING,
    val retryCount: Int = 0,
    val lastError: String? = null
)

// ─── Sync Metadata ─────────────────────────────────────────────

data class SyncMetadata(
    val deviceId: String,
    val deviceName: String,
    val localStateVersion: Long = 0L,
    val lastPushedLocalVersion: Long = 0L,
    val lastAppliedRemoteVersion: Long = 0L,
    val lastKnownRemoteVersion: Long = 0L,
    val lastPushSuccessAt: Long = 0L,
    val lastPullSuccessAt: Long = 0L,
    val lastSyncError: String? = null,
    val migrationVersion: Int = 0
)

// ─── Merge Result ──────────────────────────────────────────────

data class MergeResult(
    val mergedState: DriveCurrentState,
    val changedLocally: Boolean,
    val changedRemotely: Boolean,
    val conflictCount: Int,
    val notes: List<String>,
    val ridesMerged: Int = 0,
    val profilesMerged: Int = 0,
    val settingsUpdated: Boolean = false
)

// ─── Sync Trigger Reasons ──────────────────────────────────────

enum class SyncTriggerReason {
    RIDE_ENDED,
    SPEED_TEST_COMPLETED,
    SETTINGS_CHANGED,
    VEHICLE_PROFILE_CHANGED,
    APP_FOREGROUND,
    AUTH_SUCCESS,
    NETWORK_RESTORED,
    PERIODIC_CHECK,
    USER_REFRESH,
    MANUAL_SYNC
}

// ─── Sync Run Result ───────────────────────────────────────────

sealed class SyncRunResult {
    object Idle : SyncRunResult()
    data class Success(val reason: SyncTriggerReason, val notes: List<String> = emptyList()) : SyncRunResult()
    data class Skipped(val reason: String) : SyncRunResult()
    data class Failure(val reason: SyncTriggerReason, val error: Throwable, val notes: List<String> = emptyList()) : SyncRunResult()
}

// ─── Sync State for UI ─────────────────────────────────────────

sealed class SyncStateV2 {
    object Idle : SyncStateV2()
    data class Pulling(val reason: SyncTriggerReason) : SyncStateV2()
    data class Pushing(val reason: SyncTriggerReason) : SyncStateV2()
    data class Reconciling(val reason: SyncTriggerReason) : SyncStateV2()
    data class Synced(val at: Long, val reason: SyncTriggerReason) : SyncStateV2()
    data class Error(val message: String, val reason: SyncTriggerReason) : SyncStateV2()
}
