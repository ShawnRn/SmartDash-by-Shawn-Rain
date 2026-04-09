package com.shawnrain.sdash.data.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class DriveStateMergerTest {
    @Test
    fun selectRideWinnerPrefersRicherRideDetailsWhenFingerprintChanges() {
        val localRide = ride(
            updatedAt = 100L,
            detailSchemaRevision = 1,
            detailFingerprint = "",
            samples = listOf(sample())
        )
        val remoteRide = ride(
            updatedAt = 90L,
            detailSchemaRevision = 2,
            detailFingerprint = "remote-rich",
            samples = listOf(sample(gradePercent = 6.5f, altitudeMeters = 123.4, latitude = 23.1, longitude = 113.3))
        )

        val winner = DriveStateMerger.selectRideWinner(localRide, remoteRide)

        assertEquals(remoteRide, winner)
    }

    @Test
    fun selectRideWinnerFallsBackToNewerRideWhenCoverageIsEqualButFingerprintDiffers() {
        val localRide = ride(
            updatedAt = 100L,
            detailSchemaRevision = 2,
            detailFingerprint = "local-a",
            samples = listOf(sample(gradePercent = 2.5f))
        )
        val remoteRide = ride(
            updatedAt = 200L,
            detailSchemaRevision = 2,
            detailFingerprint = "remote-b",
            samples = listOf(sample(gradePercent = 3.0f))
        )

        val winner = DriveStateMerger.selectRideWinner(localRide, remoteRide)

        assertEquals(remoteRide, winner)
    }

    private fun ride(
        updatedAt: Long,
        detailSchemaRevision: Int,
        detailFingerprint: String,
        samples: List<SyncRideMetricSample>
    ) = SyncRideSnapshot(
        id = "ride-1",
        vehicleProfileId = "vehicle-1",
        title = "Ride",
        startedAtMs = 1L,
        endedAtMs = 2L,
        durationMs = 1L,
        distanceMeters = 10f,
        maxSpeedKmh = 20f,
        avgSpeedKmh = 15f,
        peakPowerKw = 1.5f,
        totalEnergyWh = 5f,
        avgEfficiencyWhKm = 20f,
        avgNetEfficiencyWhKm = 18f,
        updatedAt = updatedAt,
        updatedByDeviceId = "device",
        detailSchemaRevision = detailSchemaRevision,
        detailFingerprint = detailFingerprint,
        sampleCount = samples.size,
        samples = samples
    )

    private fun sample(
        gradePercent: Float? = null,
        altitudeMeters: Double? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) = SyncRideMetricSample(
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
