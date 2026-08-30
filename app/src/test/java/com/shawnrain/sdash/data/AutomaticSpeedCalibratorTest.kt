package com.shawnrain.sdash.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomaticSpeedCalibratorTest {
    @Test
    fun appliesStableLongTermFivePercentCorrection() {
        val calibrator = AutomaticSpeedCalibrator()
        calibrator.setEnabled(true)
        var adjustment: AutomaticSpeedCalibrationAdjustment? = null

        repeat(170) { index ->
            adjustment = adjustment ?: calibrator.observe(
                reliableInput(
                    index = index,
                    gpsSpeedKmh = 50f,
                    controllerSpeedKmh = 50f / 1.05f
                )
            )
        }

        val result = adjustment
        assertNotNull(result)
        assertEquals(1890f, result!!.newCircumferenceMm, 1.5f)
        assertTrue(result.qualifiedDistanceMeters >= 1_000f)
        assertTrue(result.qualifiedSamples >= 80)
    }

    @Test
    fun ignoresRepeatedGpsFixAndUnreliableAccuracy() {
        val calibrator = AutomaticSpeedCalibrator()
        calibrator.setEnabled(true)
        val reliable = reliableInput(index = 1, gpsSpeedKmh = 40f, controllerSpeedKmh = 38f)

        assertNull(calibrator.observe(reliable))
        assertNull(calibrator.observe(reliable.copy(timestampMs = reliable.timestampMs + 500L)))
        assertNull(
            calibrator.observe(
                reliableInput(index = 2, gpsSpeedKmh = 40f, controllerSpeedKmh = 38f)
                    .copy(gpsHorizontalAccuracyMeters = 30f)
            )
        )
        assertEquals(1, calibrator.state.value.qualifiedSamples)
    }

    private fun reliableInput(
        index: Int,
        gpsSpeedKmh: Float,
        controllerSpeedKmh: Float
    ) = AutomaticSpeedCalibrationInput(
        timestampMs = 1_000L + index * 500L,
        gpsFixToken = index.toLong(),
        gpsAgeMs = 100L,
        gpsSpeedKmh = gpsSpeedKmh,
        gpsHorizontalAccuracyMeters = 4f,
        gpsSpeedAccuracyMetersPerSecond = 0.25f,
        controllerSpeedKmh = controllerSpeedKmh,
        currentCircumferenceMm = 1800f,
        freshControllerSample = true,
        rpmBasedSpeed = true
    )
}
