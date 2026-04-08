package com.shawnrain.sdash.data.sync

import android.content.Context
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.history.RideHistoryRecord
import com.shawnrain.sdash.data.speedtest.SpeedTestRecord
import com.shawnrain.sdash.data.VehicleProfile
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

private val syncJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = false
}

/**
 * Builds, serializes, and deserializes the DriveCurrentState.
 * Acts as a bridge between local DataStore data and the cloud sync format.
 */
class DriveStateSerializer(
    private val context: Context,
    private val settingsRepository: SettingsRepository
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

        // Load settings
        val settings = SyncSettingsSnapshot(
            speedSource = settingsRepository.speedSource.first().name,
            battDataSource = settingsRepository.battDataSource.first().name,
            wheelCircumferenceMm = settingsRepository.wheelCircumference.first(),
            polePairs = settingsRepository.polePairs.first(),
            controllerBrand = settingsRepository.controllerBrand.first(),
            overlayEnabled = settingsRepository.overlayEnabled.first(),
            updatedAt = now,
            updatedByDeviceId = deviceId
        )

        // Load vehicle profiles
        val profiles = settingsRepository.vehicleProfiles.first().map { profile ->
            profile.toSyncSnapshot(deviceId, profile.lastModified.takeIf { it > 0L } ?: now)
        }

        // Load ride history
        val rides = settingsRepository.rideHistory.first().map { ride ->
            ride.toSyncSnapshot(deviceId, now)
        }

        // Load speed test history
        val speedTests = settingsRepository.speedTestHistory.first().map { test ->
            test.toSyncSnapshot(deviceId, now)
        }

        return DriveCurrentState(
            schemaVersion = 2,
            stateVersion = stateVersion,
            updatedAt = now,
            updatedByDeviceId = deviceId,
            updatedByDeviceName = deviceName,
            settings = settings,
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
        batteryCapacityAh = batteryCapacityAh,
        batterySeries = batterySeries,
        wheelCircumferenceMm = wheelCircumferenceMm,
        polePairs = polePairs,
        learnedEfficiencyWhKm = learnedEfficiencyWhKm,
        learnedUsableEnergyRatio = learnedUsableEnergyRatio,
        learnedInternalResistanceOhm = learnedInternalResistanceOhm,
        updatedAt = updatedAt,
        updatedByDeviceId = deviceId
    )

private fun RideHistoryRecord.toSyncSnapshot(deviceId: String, now: Long) =
    SyncRideSnapshot(
        id = id,
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
        sampleCount = samples.size,
        completenessScore = if (samples.isNotEmpty()) 1f else 0.5f
    )

private fun SpeedTestRecord.toSyncSnapshot(deviceId: String, now: Long) =
    SyncSpeedTestSnapshot(
        id = id,
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
        updatedByDeviceId = deviceId
    )
