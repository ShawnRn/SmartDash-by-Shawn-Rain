package com.shawnrain.sdash.data.sync

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

        assertEquals(6.5f, decoded.gradePercent, 0.001f)
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

        assertEquals(0f, decoded.gradePercent, 0.001f)
        assertNull(decoded.altitudeMeters)
    }
}
