package com.shawnrain.sdash.data.gps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals(104.0, result.currentAltitudeMeters ?: 0.0, 0.1)
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
        // 0.00004 deg is approx 4.4 meters. Total distance will be ~17.6m spread across 5 samples.
        // Use smaller altitude delta to keep grade in reasonable range (e.g. 0.2m per 4.4m approx 4.5%)
        result = estimator.update(sample(timestampMs = 1500L, latitude = 30.00004, altitudeMeters = 100.2, speedKmh = 12f))
        result = estimator.update(sample(timestampMs = 3000L, latitude = 30.00008, altitudeMeters = 100.4, speedKmh = 12f))
        result = estimator.update(sample(timestampMs = 4500L, latitude = 30.00012, altitudeMeters = 100.6, speedKmh = 12f))
        result = estimator.update(sample(timestampMs = 6000L, latitude = 30.00016, altitudeMeters = 100.8, speedKmh = 12f))

        assertTrue(result.currentAltitudeMeters != null && result.currentAltitudeMeters!! > 100.0)
        assertTrue("Grade should be positive, was ${result.currentGradePercent}", result.currentGradePercent in 0.5f..10.0f)
    }

    @Test
    fun lowSpeedGradeDecaysInsteadOfJumping() {
        val estimator = GradeEstimator()

        estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0, speedKmh = 12f))
        estimator.update(sample(timestampMs = 1500L, latitude = 30.00004, altitudeMeters = 100.2, speedKmh = 12f))
        estimator.update(sample(timestampMs = 3000L, latitude = 30.00008, altitudeMeters = 100.4, speedKmh = 12f))
        val moving = estimator.update(sample(timestampMs = 4500L, latitude = 30.00012, altitudeMeters = 100.6, speedKmh = 12f))

        val slowed = estimator.update(sample(timestampMs = 6000L, latitude = 30.00013, altitudeMeters = 100.7, speedKmh = 2.0f))

        assertTrue("Moving grade should be positive", moving.currentGradePercent > 0.1f)
        assertTrue("Slowed grade should decay", slowed.currentGradePercent in 0f..moving.currentGradePercent)
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
        estimator.update(sample(timestampMs = 1500L, latitude = 30.00004, altitudeMeters = 100.2, speedKmh = 12f))
        val baseline = estimator.update(sample(timestampMs = 3000L, latitude = 30.00008, altitudeMeters = 100.4, speedKmh = 12f))

        val badAccuracy = estimator.update(
            sample(
                timestampMs = 4500L,
                latitude = 30.00012,
                altitudeMeters = 110.0,
                speedKmh = 12f,
                verticalAccuracyMeters = 20f
            )
        )
        val recovered = estimator.update(sample(timestampMs = 6000L, latitude = 30.00016, altitudeMeters = 100.8, speedKmh = 12f))

        assertEquals(baseline.currentAltitudeMeters ?: 0.0, badAccuracy.currentAltitudeMeters ?: 0.0, 0.001)
        assertTrue("Recovered grade should be positive", recovered.currentGradePercent > 0.1f)
    }

    @Test
    fun lowSpeedFreezeDoesNotStretchFrozenAltitudeAcrossWindow() {
        val estimator = GradeEstimator()

        estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0, speedKmh = 12f))
        estimator.update(sample(timestampMs = 1500L, latitude = 30.00004, altitudeMeters = 100.2, speedKmh = 12f))
        estimator.update(sample(timestampMs = 3000L, latitude = 30.00008, altitudeMeters = 100.4, speedKmh = 12f))
        val moving = estimator.update(sample(timestampMs = 4500L, latitude = 30.00012, altitudeMeters = 100.6, speedKmh = 12f))

        val lowSpeed = estimator.update(sample(timestampMs = 6000L, latitude = 30.00013, altitudeMeters = 106.0, speedKmh = 0.5f))
        val recovered = estimator.update(sample(timestampMs = 7500L, latitude = 30.00017, altitudeMeters = 100.8, speedKmh = 12f))

        assertTrue(lowSpeed.currentGradePercent in 0f..moving.currentGradePercent)
        assertEquals(moving.currentAltitudeMeters ?: 0.0, lowSpeed.currentAltitudeMeters ?: 0.0, 0.001)
        assertTrue(recovered.currentGradePercent > 0.1f)
    }

    @Test
    fun longGapResetsGradeWindowButKeepsAltitudeFilterContinuous() {
        val estimator = GradeEstimator()

        estimator.update(sample(timestampMs = 0L, altitudeMeters = 100.0, speedKmh = 12f))
        estimator.update(sample(timestampMs = 1500L, latitude = 30.00004, altitudeMeters = 100.2, speedKmh = 12f))
        val beforeGap = estimator.update(sample(timestampMs = 3000L, latitude = 30.00008, altitudeMeters = 100.4, speedKmh = 12f))

        // Jump 0.00010 deg ≈ 11m to avoid "short hop" jump rejection
        val afterGap = estimator.update(sample(timestampMs = 20_000L, latitude = 30.00020, altitudeMeters = 101.5, speedKmh = 12f))
        val resumed = estimator.update(sample(timestampMs = 21_500L, latitude = 30.00026, altitudeMeters = 101.8, speedKmh = 12f))

        assertTrue(beforeGap.currentAltitudeMeters != null)
        assertTrue(afterGap.currentAltitudeMeters != null)
        assertTrue("Altitude should increase after gap", (afterGap.currentAltitudeMeters ?: 0.0) > (beforeGap.currentAltitudeMeters ?: 0.0))
        assertEquals("Grade should be 0 immediately after gap", 0f, afterGap.currentGradePercent, 0.001f)
        assertTrue("Grade should recover after resumes", resumed.currentGradePercent > 0.1f)
    }

    @Test
    fun gentleSlopeWithSmallAltitudeNoiseStaysStable() {
        val estimator = GradeEstimator()
        val grades = mutableListOf<Float>()

        var timestampMs = 0L
        val altitudes = listOf(100.0, 100.1, 100.2, 100.3, 100.4, 100.5, 100.6, 100.7, 100.8)
        altitudes.forEachIndexed { index, altitude ->
            val result = estimator.update(
                sample(
                    timestampMs = timestampMs,
                    latitude = 30.0 + (index * 0.00005),
                    altitudeMeters = altitude,
                    speedKmh = 12f
                )
            )
            grades += result.currentGradePercent
            timestampMs += 1_500L
        }

        val nonZeroGrades = grades.filter { kotlin.math.abs(it) > 0.05f }
        assertTrue("Should have some non-zero grades", nonZeroGrades.isNotEmpty())
        assertTrue("All grades should be reasonable", nonZeroGrades.all { it in 0f..5f })

        val signFlips = nonZeroGrades.zipWithNext().count { (a, b) -> a > 0f && b < 0f || a < 0f && b > 0f }
        assertEquals("Should have no sign flips on a constant climb", 0, signFlips)
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
