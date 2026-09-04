package com.shawnrain.sdash.data.lifecycle

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppResourcePolicyTest {
    @Test
    fun backgroundIdleReleasesEveryPassiveResource() {
        val decision = AppResourcePolicy.evaluate(snapshot(isAppForeground = false))

        assertFalse(decision.keepGpsTracking)
        assertFalse(decision.keepHeadingSensor)
        assertFalse(decision.keepTelemetryPolling)
        assertFalse(decision.allowControllerReconnect)
        assertTrue(decision.releaseIdleConnectionsAfterGrace)
    }

    @Test
    fun activeRideKeepsRequiredBackgroundLinksButNotHeadingSensor() {
        val decision = AppResourcePolicy.evaluate(
            snapshot(isAppForeground = false, isRideActive = true)
        )

        assertTrue(decision.keepGpsTracking)
        assertTrue(decision.keepTelemetryPolling)
        assertTrue(decision.allowControllerReconnect)
        assertFalse(decision.keepHeadingSensor)
        assertFalse(decision.releaseIdleConnectionsAfterGrace)
    }

    @Test
    fun automaticCalibrationDoesNotKeepGpsAliveWhileParked() {
        val parked = AppResourcePolicy.evaluate(
            snapshot(
                isAutomaticSpeedCalibrationEnabled = true,
                isControllerConnected = true,
                isControllerMoving = false
            )
        )
        val moving = AppResourcePolicy.evaluate(
            snapshot(
                isAutomaticSpeedCalibrationEnabled = true,
                isControllerConnected = true,
                isControllerMoving = true
            )
        )

        assertFalse(parked.keepGpsTracking)
        assertTrue(moving.keepGpsTracking)
    }

    @Test
    fun passiveGpsSpeedNeverKeepsBackgroundLocationAlive() {
        val foreground = AppResourcePolicy.evaluate(snapshot(usesGpsSpeed = true))
        val background = AppResourcePolicy.evaluate(
            snapshot(isAppForeground = false, usesGpsSpeed = true)
        )

        assertTrue(foreground.keepGpsTracking)
        assertFalse(background.keepGpsTracking)
    }

    private fun snapshot(
        isAppForeground: Boolean = true,
        isRideActive: Boolean = false,
        isSpeedTestActive: Boolean = false,
        isSpeedTestGpsWarmingUp: Boolean = false,
        isGpsCalibrationActive: Boolean = false,
        isDashcamRecording: Boolean = false,
        usesGpsSpeed: Boolean = false,
        isAutomaticSpeedCalibrationEnabled: Boolean = false,
        isControllerConnected: Boolean = false,
        isControllerMoving: Boolean = false
    ) = AppResourceSnapshot(
        isAppForeground = isAppForeground,
        isRideActive = isRideActive,
        isSpeedTestActive = isSpeedTestActive,
        isSpeedTestGpsWarmingUp = isSpeedTestGpsWarmingUp,
        isGpsCalibrationActive = isGpsCalibrationActive,
        isDashcamRecording = isDashcamRecording,
        usesGpsSpeed = usesGpsSpeed,
        isAutomaticSpeedCalibrationEnabled = isAutomaticSpeedCalibrationEnabled,
        isControllerConnected = isControllerConnected,
        isControllerMoving = isControllerMoving
    )
}
