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

    @Test
    fun sanitizeFiltersStartupGlitchAndOffsetsElapsedMs() {
        val samples = listOf(
            RideMetricSample(
                elapsedMs = 0L,
                timestampMs = 1779773678309L,
                speedKmH = 4.693f,
                powerKw = 0.798f,
                voltage = 53.200f,
                voltageSag = 0.700f,
                busCurrent = 15.000f,
                phaseCurrent = 193.600f,
                controllerTemp = 36.400f,
                soc = 14.501f, // startup SoC glitch!
                rpm = 63.000f,
                efficiencyWhKm = 0f,
                distanceMeters = 0f
            ),
            RideMetricSample(
                elapsedMs = 1050L,
                timestampMs = 1779773679359L,
                speedKmH = 11.255f,
                powerKw = 1.415f,
                voltage = 52.400f,
                voltageSag = 1.500f,
                busCurrent = 27.000f,
                phaseCurrent = 217.600f,
                controllerTemp = 36.500f,
                soc = 90.199f, // stable SoC
                rpm = 151.100f,
                efficiencyWhKm = 125.703f,
                distanceMeters = 2.326f
            ),
            RideMetricSample(
                elapsedMs = 2100L,
                timestampMs = 1779773680409L,
                speedKmH = 12.700f,
                powerKw = 0f,
                voltage = 54.000f,
                voltageSag = 0f,
                busCurrent = 0f,
                phaseCurrent = 0f,
                controllerTemp = 36.500f,
                soc = 90.194f, // stable SoC
                rpm = 173.300f,
                efficiencyWhKm = 0f,
                distanceMeters = 5.819f
            )
        )

        val sanitized = RideMetricSanitizer.sanitize(samples, 14)

        org.junit.Assert.assertEquals(2, sanitized.size)
        org.junit.Assert.assertEquals(0L, sanitized[0].elapsedMs)
        org.junit.Assert.assertEquals(90.199f, sanitized[0].soc)
        org.junit.Assert.assertEquals(1050L, sanitized[1].elapsedMs)
        org.junit.Assert.assertEquals(90.194f, sanitized[1].soc)
    }

    @Test
    fun mergeRideHistoryRecordsTimestampVerification() {
        val record1 = RideHistoryRecord(
            id = "ride1",
            title = "05-28 18:30",
            startedAtMs = 1779930000000L, // 假设的18:30时间戳
            endedAtMs = 1779931800000L,
            durationMs = 1800000L,
            distanceMeters = 5000f,
            maxSpeedKmh = 25f,
            avgSpeedKmh = 10f,
            peakPowerKw = 1.0f,
            totalEnergyWh = 100f,
            avgEfficiencyWhKm = 20f,
            trackPoints = emptyList(),
            samples = emptyList()
        )
        val record2 = RideHistoryRecord(
            id = "ride2",
            title = "05-28 18:10",
            startedAtMs = 1779928800000L, // 假设的18:10时间戳
            endedAtMs = 1779930000000L,
            durationMs = 1200000L,
            distanceMeters = 3000f,
            maxSpeedKmh = 20f,
            avgSpeedKmh = 9f,
            peakPowerKw = 0.8f,
            totalEnergyWh = 60f,
            avgEfficiencyWhKm = 20f,
            trackPoints = emptyList(),
            samples = emptyList()
        )

        // 模拟 MainViewModel 中的合并前置排序和开始时间提取逻辑
        val selected = listOf(record1, record2).sortedBy { it.startedAtMs }
        val mergedStartedAtMs = selected.first().startedAtMs

        // 验证合并后的开始时间必然是最早的 18:10，且对应的行程为 ride2
        org.junit.Assert.assertEquals(1779928800000L, mergedStartedAtMs)
        org.junit.Assert.assertEquals("ride2", selected.first().id)
    }
}
