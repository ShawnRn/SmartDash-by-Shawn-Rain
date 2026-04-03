package com.shawnrain.habe.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Auto-calibrates wheel circumference by comparing GPS speed with controller RPM.
 *
 * Algorithm:
 *  1. Wait until GPS speed > MIN_SPEED_THRESHOLD (steady cruise, not stop-and-go)
 *  2. Collect samples where both GPS and RPM are stable (< 5% jitter over STABLE_WINDOW)
 *  3. Derive actual circumference: C_mm = (GPS_km/h * 1_000_000) / (RPM * 60)
 *  4. If the derived value differs from current setting by > 3%, auto-save the new value.
 */
class AutoCalibrator(
    private val scope: CoroutineScope,
    private val gpsSpeedFlow: StateFlow<Float>,
    private val metricsRpmFlow: StateFlow<Float>,
    private val polePairsFlow: StateFlow<Int>,
    private val currentCircumferenceFlow: StateFlow<Float>,
    private val onCalibrated: suspend (newCircumferenceMm: Float) -> Unit
) {
    companion object {
        private const val TAG = "AutoCalibrator"
        private const val MIN_SPEED_KMH = 15f
        private const val STABLE_WINDOW_SAMPLES = 6  // ~3 sec at 2 Hz GPS
        private const val MAX_JITTER_RATIO = 0.05f    // 5%
        private const val SIGNIFICANT_DIFF_RATIO = 0.03f // 3%
    }

    private val gpsHistory = ArrayDeque<Float>(STABLE_WINDOW_SAMPLES)
    private val rpmHistory = ArrayDeque<Float>(STABLE_WINDOW_SAMPLES)
    private var calibrated = false

    fun start() {
        combine(gpsSpeedFlow, metricsRpmFlow) { gps, rpm ->
            gps to rpm
        }.onEach { (gpsSpeed, rpm) ->
            if (calibrated) return@onEach
            if (gpsSpeed < MIN_SPEED_KMH || rpm <= 0f) {
                gpsHistory.clear()
                rpmHistory.clear()
                return@onEach
            }

            gpsHistory.addLast(gpsSpeed)
            rpmHistory.addLast(rpm)
            if (gpsHistory.size > STABLE_WINDOW_SAMPLES) gpsHistory.removeFirst()
            if (rpmHistory.size > STABLE_WINDOW_SAMPLES) rpmHistory.removeFirst()

            if (gpsHistory.size == STABLE_WINDOW_SAMPLES && rpmHistory.size == STABLE_WINDOW_SAMPLES) {
                val gpsAvg = gpsHistory.average().toFloat()
                val rpmAvg = rpmHistory.average().toFloat()
                val gpsJitter = (gpsHistory.max() - gpsHistory.min()) / gpsAvg
                val rpmJitter = (rpmHistory.max() - rpmHistory.min()) / rpmAvg

                if (gpsJitter < MAX_JITTER_RATIO && rpmJitter < MAX_JITTER_RATIO) {
                    // Derive real circumference
                    // Speed(km/h) = RPM * C(mm) * 60 / 1_000_000
                    // => C(mm) = Speed(km/h) * 1_000_000 / (RPM * 60)
                    val polePairs = polePairsFlow.value
                    val effectiveRpm = if (polePairs > 0) rpmAvg / polePairs else rpmAvg
                    val derivedC = (gpsAvg * 1_000_000f) / (effectiveRpm * 60f)
                    val currentC = currentCircumferenceFlow.value

                    if (derivedC > 500f && derivedC < 5000f) { // sanity range for wheel circumference
                        val diff = abs(derivedC - currentC) / currentC
                        if (diff > SIGNIFICANT_DIFF_RATIO) {
                            Log.i(TAG, "Auto-calibrated: old=$currentC -> new=$derivedC (diff=${(diff*100).toInt()}%)")
                            calibrated = true
                            scope.launch { onCalibrated(derivedC) }
                        }
                    }
                }
            }
        }.launchIn(scope)
    }
}
