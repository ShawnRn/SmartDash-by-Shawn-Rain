package com.shawnrain.sdash.data.sync

import com.shawnrain.sdash.data.history.RideMetricSample
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncRideMetricSampleSerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun syncRideMetricSampleRoundTripsNewFields() {
        val sample = SyncRideMetricSample(
            elapsedMs = 1_000L,
            timestampMs = 2_000L,
            speedKmH = 30f,
            powerKw = 1.5f,
            voltage = 72f,
            voltageSag = 1.2f,
            busCurrent = 20f,
            phaseCurrent = 30f,
            controllerTemp = 45f,
            soc = 90f,
            estimatedRangeKm = 40f,
            rpm = 500f,
            efficiencyWhKm = 20f,
            avgEfficiencyWhKm = 21f,
            distanceMeters = 500f,
            gradePercent = 6.5f,
            altitudeMeters = 123.4
        )

        val encoded = json.encodeToString(SyncRideMetricSample.serializer(), sample)
        val decoded = json.decodeFromString(SyncRideMetricSample.serializer(), encoded)

        assertEquals(6.5f, decoded.gradePercent ?: 0f, 0.001f)
        assertEquals(123.4, decoded.altitudeMeters ?: 0.0, 0.001)
    }

    @Test
    fun syncRideMetricSampleDefaultsWhenNewFieldsMissing() {
        val decoded = json.decodeFromString(
            SyncRideMetricSample.serializer(),
            """
            {
              "elapsedMs": 1,
              "timestampMs": 2,
              "speedKmH": 0.0,
              "powerKw": 0.0,
              "voltage": 0.0,
              "voltageSag": 0.0,
              "busCurrent": 0.0,
              "phaseCurrent": 0.0,
              "controllerTemp": 0.0,
              "soc": 0.0,
              "rpm": 0.0,
              "efficiencyWhKm": 0.0,
              "distanceMeters": 0.0
            }
            """.trimIndent()
        )

        assertNull(decoded.gradePercent)
        assertNull(decoded.altitudeMeters)
    }

    @Test
    fun rideMetricSampleRoundTripsThroughSyncSnapshot() {
        val sample = RideMetricSample(
            elapsedMs = 1_000L,
            timestampMs = 2_000L,
            speedKmH = 30f,
            powerKw = 1.5f,
            voltage = 72f,
            voltageSag = 1.2f,
            busCurrent = 20f,
            phaseCurrent = 30f,
            controllerTemp = 45f,
            soc = 90f,
            estimatedRangeKm = 40f,
            rpm = 500f,
            efficiencyWhKm = 20f,
            avgEfficiencyWhKm = 21f,
            avgNetEfficiencyWhKm = 18f,
            avgTractionEfficiencyWhKm = 24f,
            distanceMeters = 500f,
            totalEnergyWh = 12f,
            tractionEnergyWh = 14f,
            regenEnergyWh = 2f,
            recoveredEnergyWh = 2f,
            maxControllerTemp = 47f,
            gradePercent = 6.5f,
            altitudeMeters = 123.4,
            latitude = 23.123456,
            longitude = 113.654321
        )

        val decoded = RideMetricSample.fromSyncSnapshot(sample.toSyncSnapshot())

        assertEquals(sample.gradePercent ?: 0f, decoded.gradePercent ?: 0f, 0.001f)
        assertEquals(sample.altitudeMeters ?: 0.0, decoded.altitudeMeters ?: 0.0, 0.001)
        assertEquals(sample.latitude ?: 0.0, decoded.latitude ?: 0.0, 0.000001)
        assertEquals(sample.longitude ?: 0.0, decoded.longitude ?: 0.0, 0.000001)
    }

    @Test
    fun syncRideSnapshotDefaultsMissingDetailFingerprintFields() {
        val decoded = json.decodeFromString(
            SyncRideSnapshot.serializer(),
            """
            {
              "id": "ride-1",
              "vehicleProfileId": "vehicle-1",
              "title": "Ride",
              "startedAtMs": 1,
              "endedAtMs": 2,
              "durationMs": 1,
              "distanceMeters": 0.0,
              "maxSpeedKmh": 0.0,
              "avgSpeedKmh": 0.0,
              "peakPowerKw": 0.0,
              "totalEnergyWh": 0.0,
              "avgEfficiencyWhKm": 0.0,
              "avgNetEfficiencyWhKm": 0.0,
              "updatedAt": 3,
              "updatedByDeviceId": "device"
            }
            """.trimIndent()
        )

        assertEquals(1, decoded.detailSchemaRevision)
        assertTrue(decoded.detailFingerprint.isEmpty())
    }
}
