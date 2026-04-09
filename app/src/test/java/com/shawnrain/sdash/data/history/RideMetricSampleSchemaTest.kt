package com.shawnrain.sdash.data.history

import com.shawnrain.sdash.RideCsvExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RideMetricSampleSchemaTest {
    @Test
    fun rideMetricSampleValueMapRoundTripsNewFields() {
        val sample = sample(
            gradePercent = 5.5f,
            altitudeMeters = 123.4,
            latitude = 23.123456,
            longitude = 113.654321
        )
        val decoded = RideMetricSampleSchema.fromValueMap(RideMetricSampleSchema.toValueMap(sample))

        assertEquals(5.5f, decoded.gradePercent ?: 0f, 0.001f)
        assertEquals(123.4, decoded.altitudeMeters ?: 0.0, 0.001)
        assertEquals(23.123456, decoded.latitude ?: 0.0, 0.000001)
        assertEquals(113.654321, decoded.longitude ?: 0.0, 0.000001)
    }

    @Test
    fun rideMetricSampleValueMapKeepsMissingNullableFieldsAsNull() {
        val decoded = RideMetricSampleSchema.fromValueMap(
            mapOf(
                "elapsedMs" to 1L,
                "timestampMs" to 2L,
                "speedKmH" to 0.0,
                "powerKw" to 0.0,
                "voltage" to 0.0,
                "voltageSag" to 0.0,
                "busCurrent" to 0.0,
                "phaseCurrent" to 0.0,
                "controllerTemp" to 0.0,
                "soc" to 0.0,
                "rpm" to 0.0,
                "efficiencyWhKm" to 0.0,
                "distanceMeters" to 0.0
            )
        )

        assertNull(decoded.gradePercent)
        assertNull(decoded.altitudeMeters)
        assertNull(decoded.latitude)
        assertNull(decoded.longitude)
    }

    @Test
    fun rideCsvExporterIncludesGradeAndAltitudeColumnsAndValues() {
        val sample = sample(
            gradePercent = 6.5f,
            altitudeMeters = 123.4,
            latitude = 23.123456,
            longitude = 113.654321
        )

        val csv = RideCsvExporter.build(
            normalizedSamples = listOf(sample),
            numberFormatter = { value ->
                if (value == null) "" else String.format(java.util.Locale.US, "%.3f", value.toDouble())
            }
        )

        val lines = csv.lines()
        assertTrue(lines.first().contains("grade_percent"))
        assertTrue(lines.first().contains("altitude_m"))
        assertTrue(lines[1].contains("6.500"))
        assertTrue(lines[1].contains("123.400"))
    }

    private fun sample(
        gradePercent: Float? = null,
        altitudeMeters: Double? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) = RideMetricSample(
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
        gradePercent = gradePercent,
        altitudeMeters = altitudeMeters,
        latitude = latitude,
        longitude = longitude
    )
}
