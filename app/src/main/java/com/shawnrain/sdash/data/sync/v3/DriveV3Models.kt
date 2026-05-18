package com.shawnrain.sdash.data.sync.v3

import kotlinx.serialization.Serializable

@Serializable
data class DriveV3Manifest(
    val schemaVersion: Int = 3,
    val updatedAt: Long,
    val updatedByDeviceId: String,
    val updatedByDeviceName: String,
    val settingsEntry: String = "v3/settings/current.json.enc",
    val settingsFingerprint: String = "",
    val entities: List<DriveV3EntityRef> = emptyList(),
    val counters: DriveV3Counters = DriveV3Counters()
)

@Serializable
data class DriveV3EntityRef(
    val type: String,
    val id: String,
    val vehicleId: String = "",
    val entryName: String,
    val updatedAt: Long,
    val fingerprint: String = "",
    val deletedAt: Long = 0L
)

@Serializable
data class DriveV3Counters(
    val vehicleProfileCount: Int = 0,
    val controllerBindingCount: Int = 0,
    val rideCount: Int = 0,
    val speedTestCount: Int = 0
)

data class SyncHealthSnapshot(
    val schemaVersion: Int,
    val localPendingCount: Int,
    val remoteRideCount: Int,
    val remoteVehicleCount: Int,
    val remoteControllerBindingCount: Int,
    val lastRemoteUpdatedAt: Long,
    val lastError: String?
)

data class DriveV3PublishResult(
    val manifest: DriveV3Manifest,
    val uploadedEntityCount: Int,
    val rideCount: Int,
    val vehicleProfileCount: Int,
    val controllerBindingCount: Int,
    val speedTestCount: Int
)

data class DriveV3PullResult(
    val manifest: DriveV3Manifest,
    val settingsMerged: Int,
    val ridesMerged: Int,
    val ridesSkipped: Int
)
