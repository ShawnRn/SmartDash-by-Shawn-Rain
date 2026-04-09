package com.shawnrain.sdash.data.gps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GradeEstimatorTest {
    @Test
    fun uphillSegmentReturnsPositiveGrade() {
        val estimator = GradeEstimator()
        estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0))

        val result = estimator.update(sample(timestampMs = 2_000L, latitude = 30.0001, altitudeMeters = 102.0))

        assertTrue(result.currentGradePercent > 0f)
    }

    @Test
    fun downhillSegmentReturnsNegativeGrade() {
        val estimator = GradeEstimator()
        estimator.update(sample(timestampMs = 0L, altitudeMeters = 102.0))

        val result = estimator.update(sample(timestampMs = 2_000L, latitude = 30.0001, altitudeMeters = 100.0))

        assertTrue(result.currentGradePercent < 0f)
    }

    @Test
    fun invalidShortDistanceDoesNotUpdateGrade() {
        val estimator = GradeEstimator()
        estimator.update(sample(timestampMs = 0L))

        val result = estimator.update(sample(timestampMs = 2_000L, latitude = 30.00001))

        assertEquals(0f, result.currentGradePercent, 0.001f)
    }

    @Test
    fun invalidLowSpeedDoesNotUpdateGrade() {
        val estimator = GradeEstimator()
        estimator.update(sample(timestampMs = 0L, speedKmh = 3f))

        val result = estimator.update(sample(timestampMs = 2_000L, latitude = 30.0001, altitudeMeters = 102.0, speedKmh = 3f))

        assertEquals(0f, result.currentGradePercent, 0.001f)
    }

    @Test
    fun poorVerticalAccuracyRejectsUpdate() {
        val estimator = GradeEstimator()
        estimator.update(sample(timestampMs = 0L, verticalAccuracyMeters = 20f))

        val result = estimator.update(
            sample(
                timestampMs = 2_000L,
                latitude = 30.0001,
                altitudeMeters = 102.0,
                verticalAccuracyMeters = 20f
            )
        )

        assertEquals(0f, result.currentGradePercent, 0.001f)
    }

    @Test
    fun invalidSegmentsHoldThenDecayToZero() {
        val estimator = GradeEstimator()
        estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0))
        val valid = estimator.update(sample(timestampMs = 2_000L, latitude = 30.0001, altitudeMeters = 102.0))
        assertTrue(valid.currentGradePercent > 0f)

        val held = estimator.update(sample(timestampMs = 4_000L, altitudeMeters = null))
        assertEquals(valid.currentGradePercent, held.currentGradePercent, 0.001f)

        val decayed = estimator.update(sample(timestampMs = 12_000L, altitudeMeters = null))
        assertTrue(decayed.currentGradePercent < held.currentGradePercent)

        val zeroed = estimator.update(sample(timestampMs = 20_000L, altitudeMeters = null))
        assertEquals(0f, zeroed.currentGradePercent, 0.001f)
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
