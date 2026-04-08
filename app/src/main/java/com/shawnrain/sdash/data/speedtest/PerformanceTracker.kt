package com.shawnrain.sdash.data.speedtest

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PerformanceRun(
    val type: String,
    val timeMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

class PerformanceTracker {
    private val _runs = MutableStateFlow<List<PerformanceRun>>(emptyList())
    val runs: StateFlow<List<PerformanceRun>> = _runs.asStateFlow()

    private val _currentRunTime = MutableStateFlow(0L)
    val currentRunTime: StateFlow<Long> = _currentRunTime.asStateFlow()

    private var isTesting = false
    private var startTime = 0L
    private var targetSpeed = 100.0f
    private var startSpeed = 0.0f

    fun onSpeedUpdate(speed: Float) {
        if (!isTesting) {
            // Auto start if speed increases from 0 to > 2km/h
            if (speed > 2.0f && speed < 5.0f) {
                startTest(100.0f)
            }
            return
        }

        // Calculate elapsed
        val elapsed = System.currentTimeMillis() - startTime
        _currentRunTime.value = elapsed

        if (speed >= targetSpeed) {
            // Test Finished!
            // Interpolate for better accuracy: 
            // We hit targetSpeed at 'elapsed' ms. But we likely passed it slightly earlier.
            // (Assuming linear accel between last and this sample is too complex without previous sample)
            
            val finalRun = PerformanceRun("0-$targetSpeed", elapsed)
            _runs.value += finalRun
            isTesting = false
        }
    }

    private fun startTest(target: Float) {
        isTesting = true
        startTime = System.currentTimeMillis()
        targetSpeed = target
    }

    fun reset() {
        isTesting = false
        _currentRunTime.value = 0L
    }
}
