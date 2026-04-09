package com.shawnrain.sdash.data.gps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GradeEstimatorTest {
    @Test
    fun altitudeUsesEmaFilteringInsteadOfRawGpsAltitude() {
        val estimator = GradeEstimator()

        estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0, speedKmh = 8f))
        val result = estimator.update(sample(timestampMs = 1_500L, latitude = 30.00012, altitudeMeters = 110.0, speedKmh = 8f))

        assertNotNull(result.currentAltitudeMeters)
        assertEquals(102.0, result.currentAltitudeMeters ?: 0.0, 0.001)
    }

    @Test
    fun lowSpeedFreezeKeepsAltitudeStable() {
        val estimator = GradeEstimator()

        estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0, speedKmh = 8f))
        val moving = estimator.update(sample(timestampMs = 1_500L, latitude = 30.00012, altitudeMeters = 105.0, speedKmh = 8f))
        val stopped = estimator.update(sample(timestampMs = 3_000L, latitude = 30.00020, altitudeMeters = 120.0, speedKmh = 0.5f))

        assertEquals(moving.currentAltitudeMeters ?: 0.0, stopped.currentAltitudeMeters ?: 0.0, 0.001)
    }

    @Test
    fun shortDistanceAltitudeJumpIsIgnored() {
        val estimator = GradeEstimator()

        estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0, speedKmh = 8f))
        val result = estimator.update(sample(timestampMs = 1_500L, latitude = 30.00001, altitudeMeters = 108.0, speedKmh = 8f))

        assertEquals(100.0, result.currentAltitudeMeters ?: 0.0, 0.001)
    }

    @Test
    fun gradeUsesFilteredAltitudeOverSlidingDistanceWindow() {
        val estimator = GradeEstimator()

        var result = estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0, speedKmh = 12f))
        result = estimator.update(sample(timestampMs = 1_500L, latitude = 30.00012, altitudeMeters = 101.0, speedKmh = 12f))
        result = estimator.update(sample(timestampMs = 3_000L, latitude = 30.00024, altitudeMeters = 102.0, speedKmh = 12f))
        result = estimator.update(sample(timestampMs = 4_500L, latitude = 30.00036, altitudeMeters = 103.0, speedKmh = 12f))

        assertTrue(result.currentAltitudeMeters != null && result.currentAltitudeMeters!! > 100.0)
        assertTrue(result.currentGradePercent in 1.0f..8.0f)
    }

    @Test
    fun lowSpeedGradeDecaysInsteadOfJumping() {
        val estimator = GradeEstimator()

        estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0, speedKmh = 12f))
        estimator.update(sample(timestampMs = 1_500L, latitude = 30.00012, altitudeMeters = 101.0, speedKmh = 12f))
        estimator.update(sample(timestampMs = 3_000L, latitude = 30.00024, altitudeMeters = 102.0, speedKmh = 12f))
        val moving = estimator.update(sample(timestampMs = 4_500L, latitude = 30.00036, altitudeMeters = 103.0, speedKmh = 12f))

        val slowed = estimator.update(sample(timestampMs = 6_000L, latitude = 30.00040, altitudeMeters = 103.5, speedKmh = 2.0f))

        assertTrue(moving.currentGradePercent > 0f)
        assertTrue(slowed.currentGradePercent in 0f..moving.currentGradePercent)
    }

    @Test
    fun poorVerticalAccuracyDoesNotUpdateAltitudeOrGrade() {
        val estimator = GradeEstimator()

        estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0, speedKmh = 10f))
        val result = estimator.update(
            sample(
                timestampMs = 1_500L,
                latitude = 30.00012,
                altitudeMeters = 108.0,
                speedKmh = 10f,
                verticalAccuracyMeters = 20f
            )
        )

        assertEquals(100.0, result.currentAltitudeMeters ?: 0.0, 0.001)
        assertEquals(0f, result.currentGradePercent, 0.001f)
    }

    @Test
    fun badVerticalAccuracyDoesNotStretchOldAltitudeAcrossGradeWindow() {
        val estimator = GradeEstimator()

        estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0, speedKmh = 12f))
        estimator.update(sample(timestampMs = 1_500L, latitude = 30.00012, altitudeMeters = 101.0, speedKmh = 12f))
        val baseline = estimator.update(sample(timestampMs = 3_000L, latitude = 30.00024, altitudeMeters = 102.0, speedKmh = 12f))

        val badAccuracy = estimator.update(
            sample(
                timestampMs = 4_500L,
                latitude = 30.00036,
                altitudeMeters = 110.0,
                speedKmh = 12f,
                verticalAccuracyMeters = 20f
            )
        )
        val recovered = estimator.update(sample(timestampMs = 6_000L, latitude = 30.00048, altitudeMeters = 103.0, speedKmh = 12f))

        assertEquals(baseline.currentAltitudeMeters ?: 0.0, badAccuracy.currentAltitudeMeters ?: 0.0, 0.001)
        assertTrue(recovered.currentGradePercent > 0.5f)
    }

    @Test
    fun lowSpeedFreezeDoesNotStretchFrozenAltitudeAcrossWindow() {
        val estimator = GradeEstimator()

        estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0, speedKmh = 12f))
        estimator.update(sample(timestampMs = 1_500L, latitude = 30.00012, altitudeMeters = 101.0, speedKmh = 12f))
        estimator.update(sample(timestampMs = 3_000L, latitude = 30.00024, altitudeMeters = 102.0, speedKmh = 12f))
        val moving = estimator.update(sample(timestampMs = 4_500L, latitude = 30.00036, altitudeMeters = 103.0, speedKmh = 12f))

        val lowSpeed = estimator.update(sample(timestampMs = 6_000L, latitude = 30.00046, altitudeMeters = 106.0, speedKmh = 0.5f))
        val recovered = estimator.update(sample(timestampMs = 7_500L, latitude = 30.00058, altitudeMeters = 104.0, speedKmh = 12f))

        assertTrue(lowSpeed.currentGradePercent in 0f..moving.currentGradePercent)
        assertEquals(moving.currentAltitudeMeters ?: 0.0, lowSpeed.currentAltitudeMeters ?: 0.0, 0.001)
        assertTrue(recovered.currentGradePercent > 0.5f)
    }

    private fun sample(
        timestampMs: Long,
        latitude: Double = 30.0,
        longitude: Double = 120.0,
        altitudeMeters: Double? = 100.0,
        speedKmh: Float = 12f,
        verticalAccuracyMeters: Float? = 3f
    ) = GradeEstimatorInput(
        latitude = latitude,
        longitude = longitude,
        altitudeMeters = altitudeMeters,
        speedKmh = speedKmh,
        timestampMs = timestampMs,
        verticalAccuracyMeters = verticalAccuracyMeters
    )
}
