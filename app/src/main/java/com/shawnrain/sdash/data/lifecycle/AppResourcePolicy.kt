package com.shawnrain.sdash.data.lifecycle

/**
 * Snapshot used to decide which expensive device resources are allowed to stay active.
 *
 * The policy deliberately separates an explicit user task (ride, speed test, calibration or
 * dashcam recording) from passive dashboard features. Explicit tasks may continue in the
 * background; passive features never keep hardware awake after the app leaves the foreground.
 */
data class AppResourceSnapshot(
    val isAppForeground: Boolean,
    val isRideActive: Boolean,
    val isSpeedTestActive: Boolean,
    val isSpeedTestGpsWarmingUp: Boolean,
    val isGpsCalibrationActive: Boolean,
    val isDashcamRecording: Boolean,
    val usesGpsSpeed: Boolean,
    val isAutomaticSpeedCalibrationEnabled: Boolean,
    val isControllerConnected: Boolean,
    val isControllerMoving: Boolean
)

data class AppResourceDecision(
    val keepGpsTracking: Boolean,
    val keepHeadingSensor: Boolean,
    val keepTelemetryPolling: Boolean,
    val allowControllerReconnect: Boolean,
    val releaseIdleConnectionsAfterGrace: Boolean,
    val hasExplicitBackgroundTask: Boolean
)

object AppResourcePolicy {
    fun evaluate(snapshot: AppResourceSnapshot): AppResourceDecision {
        val hasExplicitTask = snapshot.isRideActive ||
            snapshot.isSpeedTestActive ||
            snapshot.isSpeedTestGpsWarmingUp ||
            snapshot.isGpsCalibrationActive ||
            snapshot.isDashcamRecording

        val automaticCalibrationNeedsGps = snapshot.isAppForeground &&
            snapshot.isAutomaticSpeedCalibrationEnabled &&
            snapshot.isControllerConnected &&
            snapshot.isControllerMoving

        val keepGps = snapshot.isRideActive ||
            (snapshot.isAppForeground && (
                snapshot.isSpeedTestActive ||
                    snapshot.isSpeedTestGpsWarmingUp ||
                    snapshot.isGpsCalibrationActive ||
                    snapshot.usesGpsSpeed ||
                    automaticCalibrationNeedsGps
                ))

        return AppResourceDecision(
            keepGpsTracking = keepGps,
            keepHeadingSensor = snapshot.isAppForeground,
            keepTelemetryPolling = snapshot.isAppForeground || hasExplicitTask,
            allowControllerReconnect = snapshot.isAppForeground || hasExplicitTask,
            releaseIdleConnectionsAfterGrace = !snapshot.isAppForeground && !hasExplicitTask,
            hasExplicitBackgroundTask = hasExplicitTask
        )
    }
}
