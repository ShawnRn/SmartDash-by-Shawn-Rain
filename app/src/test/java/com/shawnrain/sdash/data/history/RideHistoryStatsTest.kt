package com.shawnrain.sdash.data.history

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RideHistoryStatsTest {
    @Test
    fun computeRideSummaryStatsHidesGradeWhenAltitudeMissing() {
        val record = RideHistoryRecord(
            id = "legacy",
            title = "Legacy Ride",
            startedAtMs = 0L,
            endedAtMs = 1_000L,
            durationMs = 1_000L,
            distanceMeters = 100f,
            maxSpeedKmh = 20f,
            avgSpeedKmh = 15f,
            peakPowerKw = 1.2f,
            totalEnergyWh = 5f,
            avgEfficiencyWhKm = 50f,
            trackPoints = emptyList(),
            samples = listOf(
                RideMetricSample(
                    elapsedMs = 0L,
                    timestampMs = 0L,
                    speedKmH = 0f,
                    powerKw = 0f,
                    voltage = 50f,
                    voltageSag = 0f,
                    busCurrent = 0f,
                    phaseCurrent = 0f,
                    controllerTemp = 30f,
                    soc = 80f,
                    rpm = 0f,
                    efficiencyWhKm = 0f,
                    distanceMeters = 0f,
                    gradePercent = 0f,
                    altitudeMeters = null
                ),
                RideMetricSample(
                    elapsedMs = 1_000L,
                    timestampMs = 1_000L,
                    speedKmH = 20f,
                    powerKw = 1f,
                    voltage = 50f,
                    voltageSag = 0f,
                    busCurrent = 10f,
                    phaseCurrent = 20f,
                    controllerTemp = 35f,
                    soc = 79f,
                    rpm = 300f,
                    efficiencyWhKm = 20f,
                    distanceMeters = 100f,
                    gradePercent = 0f,
                    altitudeMeters = null
                )
            )
        )

        val stats = computeRideSummaryStats(record)

        assertFalse(stats.hasAltitudeData)
        assertFalse(stats.hasGradeData)
    }

    @Test
    fun computeRideSummaryStatsKeepsGradeWhenAltitudeExists() {
        val record = RideHistoryRecord(
            id = "new",
            title = "New Ride",
            startedAtMs = 0L,
            endedAtMs = 1_000L,
            durationMs = 1_000L,
            distanceMeters = 100f,
            maxSpeedKmh = 20f,
            avgSpeedKmh = 15f,
            peakPowerKw = 1.2f,
            totalEnergyWh = 5f,
            avgEfficiencyWhKm = 50f,
            trackPoints = emptyList(),
            samples = listOf(
                RideMetricSample(
                    elapsedMs = 0L,
                    timestampMs = 0L,
                    speedKmH = 0f,
                    powerKw = 0f,
                    voltage = 50f,
                    voltageSag = 0f,
                    busCurrent = 0f,
                    phaseCurrent = 0f,
                    controllerTemp = 30f,
                    soc = 80f,
                    rpm = 0f,
                    efficiencyWhKm = 0f,
                    distanceMeters = 0f,
                    gradePercent = 0f,
                    altitudeMeters = 100.0
                ),
                RideMetricSample(
                    elapsedMs = 1_000L,
                    timestampMs = 1_000L,
                    speedKmH = 20f,
                    powerKw = 1f,
                    voltage = 50f,
                    voltageSag = 0f,
                    busCurrent = 10f,
                    phaseCurrent = 20f,
                    controllerTemp = 35f,
                    soc = 79f,
                    rpm = 300f,
                    efficiencyWhKm = 20f,
                    distanceMeters = 100f,
                    gradePercent = 0f,
                    altitudeMeters = 101.0
                )
            )
        )

        val stats = computeRideSummaryStats(record)

        assertTrue(stats.hasAltitudeData)
        assertTrue(stats.hasGradeData)
    }
}
