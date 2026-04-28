package com.shawnrain.sdash.data.sync

import android.content.Context
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.history.RideHistoryRepository
import com.shawnrain.sdash.data.history.RideHistoryRecord
import com.shawnrain.sdash.data.history.computeRideDetailFingerprint
import com.shawnrain.sdash.data.history.RideMetricSample
import com.shawnrain.sdash.data.history.RideMetricSampleSchema
import com.shawnrain.sdash.data.history.RideTrackPoint
import com.shawnrain.sdash.data.speedtest.SpeedTestRecord
import com.shawnrain.sdash.data.speedtest.SpeedTestTrackPoint
import com.shawnrain.sdash.data.VehicleProfile
import com.shawnrain.sdash.ui.poster.PosterSettings
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

private val syncJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = false
    // 强制使用确定性排序并允许宽松输入
    coerceInputValues = true
}

private const val RIDE_DETAIL_SCHEMA_REVISION = 2

/**
 * Builds, serializes, and deserializes the DriveCurrentState.
 * Acts as a bridge between local DataStore data and the cloud sync format.
 */
class DriveStateSerializer(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val rideHistoryRepository: RideHistoryRepository
) {
    /**
     * Build the current state snapshot from local data sources.
     */
    suspend fun buildCurrentState(
        stateVersion: Long,
        deviceId: String,
        deviceName: String
    ): DriveCurrentState {
        val now = System.currentTimeMillis()
        val activeVehicleId = settingsRepository.currentVehicleId.first()
        val posterSettings = settingsRepository.posterSettings.first()

        // Load settings
        val settings = SyncSettingsSnapshot(
            speedSource = settingsRepository.speedSource.first().name,
            battDataSource = settingsRepository.battDataSource.first().name,
            wheelCircumferenceMm = settingsRepository.wheelCircumference.first(),
            polePairs = settingsRepository.polePairs.first(),
            controllerBrand = settingsRepository.controllerBrand.first(),
            logLevel = settingsRepository.logLevel.first().name,
            overlayEnabled = settingsRepository.overlayEnabled.first(),
            dashboardItems = settingsRepository.dashboardItems.first().map { it.name },
            rideOverviewItems = settingsRepository.rideOverviewItems.first().map { it.name },
            driveBackupRetention = settingsRepository.driveBackupRetentionPolicy.first().name,
            autoRideStopEnabled = settingsRepository.autoRideStopEnabled.first(),
            autoRideStopDelaySeconds = settingsRepository.autoRideStopDelaySeconds.first(),
            posterTemplateId = posterSettings.defaultTemplate,
            posterAspectRatio = posterSettings.defaultAspectRatio.name,
            posterShowTrack = posterSettings.showTrack,
            posterShowWatermark = posterSettings.showWatermark,
            updatedAt = now,
            updatedByDeviceId = deviceId
        )
        val vehicleSettings = settingsRepository.buildSyncVehicleSettingsSnapshots(deviceId)

        // Load vehicle profiles
        val profiles = settingsRepository.vehicleProfiles.first().map { profile ->
            profile.toSyncSnapshot(deviceId, profile.lastModified.takeIf { it > 0L } ?: now)
        }

        // Load ride history from Room-backed repository
        val rides = rideHistoryRepository.listRideHistorySummaries().mapNotNull { summary ->
            rideHistoryRepository
                .loadRideRecord(summary.id)
                ?.toSyncSnapshot(deviceId, now, summary.vehicleId.ifBlank { activeVehicleId })
        }

        // Load speed test history
        val speedTests = settingsRepository.speedTestHistory.first().map { test ->
            test.toSyncSnapshot(deviceId, now, activeVehicleId)
        }

        return DriveCurrentState(
            schemaVersion = 2,
            stateVersion = stateVersion,
            updatedAt = now,
            updatedByDeviceId = deviceId,
            updatedByDeviceName = deviceName,
            activeVehicleProfileId = activeVehicleId,
            settings = settings,
            vehicleSettings = vehicleSettings,
            vehicleProfiles = profiles,
            rides = rides,
            speedTests = speedTests
        )
    }

    /**
     * Serialize state to a JSON byte array.
     */
    fun serialize(state: DriveCurrentState): ByteArray {
        return syncJson.encodeToString(state).toByteArray(Charsets.UTF_8)
    }

    /**
     * Deserialize a JSON byte array back to DriveCurrentState.
     */
    fun deserialize(bytes: ByteArray): DriveCurrentState {
        return syncJson.decodeFromString(String(bytes, Charsets.UTF_8))
    }

    /**
     * Compute SHA-256 checksum of the plaintext state JSON.
     */
    fun computeChecksum(stateBytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(stateBytes)
        return hash.joinToString("") { "%02x".format(it) }
    }
}

// ─── Extension functions to convert domain models to sync snapshots ─────────

private fun VehicleProfile.toSyncSnapshot(deviceId: String, updatedAt: Long) =
    SyncVehicleProfileSnapshot(
        id = id,
        name = name,
        macAddress = macAddress,
        batteryCapacityAh = batteryCapacityAh,
        batterySeries = batterySeries,
        wheelCircumferenceMm = wheelCircumferenceMm,
        wheelRimSize = wheelRimSize,
        tireSpecLabel = tireSpecLabel,
        polePairs = polePairs,
        totalMileageKm = totalMileageKm,
        learnedEfficiencyWhKm = learnedEfficiencyWhKm,
        learnedUsableEnergyRatio = learnedUsableEnergyRatio,
        learnedInternalResistanceOhm = learnedInternalResistanceOhm,
        updatedAt = updatedAt,
        updatedByDeviceId = deviceId
    )

private fun RideHistoryRecord.toSyncSnapshot(deviceId: String, now: Long, vehicleProfileId: String) =
    SyncRideSnapshot(
        id = id,
        vehicleProfileId = vehicleProfileId,
        title = title,
        startedAtMs = startedAtMs,
        endedAtMs = endedAtMs,
        durationMs = durationMs,
        distanceMeters = distanceMeters,
        maxSpeedKmh = maxSpeedKmh,
        avgSpeedKmh = avgSpeedKmh,
        peakPowerKw = peakPowerKw,
        totalEnergyWh = totalEnergyWh,
        tractionEnergyWh = tractionEnergyWh,
        regenEnergyWh = regenEnergyWh,
        avgEfficiencyWhKm = avgEfficiencyWhKm,
        avgNetEfficiencyWhKm = avgNetEfficiencyWhKm,
        avgTractionEfficiencyWhKm = avgTractionEfficiencyWhKm,
        updatedAt = now,
        updatedByDeviceId = deviceId,
        detailSchemaRevision = RIDE_DETAIL_SCHEMA_REVISION,
        detailFingerprint = computeRideDetailFingerprint(trackPoints, samples),
        sampleCount = samples.size,
        completenessScore = when {
            samples.isNotEmpty() -> 1f
            trackPoints.isNotEmpty() -> 0.75f
            else -> 0.5f
        },
        trackPoints = trackPoints.map { it.toSyncSnapshot() },
        samples = samples.map { it.toSyncSnapshot() }
    )

private fun SpeedTestRecord.toSyncSnapshot(deviceId: String, now: Long, vehicleProfileId: String) =
    SyncSpeedTestSnapshot(
        id = id,
        vehicleProfileId = vehicleProfileId,
        label = label,
        startedAtMs = timestampMs,
        endedAtMs = timestampMs + timeMs,
        targetSpeedKmh = targetSpeedKmh,
        achievedSpeedKmh = maxSpeedKmh,
        timeToTargetMs = timeMs,
        distanceMeters = distanceMeters,
        peakPowerKw = peakPowerKw,
        peakBusCurrentA = peakBusCurrentA,
        minVoltageV = minVoltage,
        updatedAt = now,
        updatedByDeviceId = deviceId,
        trackPoints = trackPoints.map { it.toSyncSnapshot() }
    )

private fun RideTrackPoint.toSyncSnapshot() = SyncRideTrackPoint(
    latitude = latitude,
    longitude = longitude
)

private fun SpeedTestTrackPoint.toSyncSnapshot() = SyncSpeedTestTrackPoint(
    latitude = latitude,
    longitude = longitude
)
