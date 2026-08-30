package com.shawnrain.sdash.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

data class AutomaticSpeedCalibrationState(
    val enabled: Boolean = false,
    val qualifiedSamples: Int = 0,
    val qualifiedDistanceMeters: Float = 0f,
    val estimatedCircumferenceMm: Float? = null,
    val deviationPercent: Float? = null,
    val lastAppliedAtMs: Long = 0L,
    val status: String = "自动校准已关闭"
)

data class AutomaticSpeedCalibrationInput(
    val timestampMs: Long,
    val gpsFixToken: Long,
    val gpsAgeMs: Long,
    val gpsSpeedKmh: Float,
    val gpsHorizontalAccuracyMeters: Float,
    val gpsSpeedAccuracyMetersPerSecond: Float?,
    val controllerSpeedKmh: Float,
    val currentCircumferenceMm: Float,
    val freshControllerSample: Boolean,
    val rpmBasedSpeed: Boolean
)

data class AutomaticSpeedCalibrationAdjustment(
    val previousCircumferenceMm: Float,
    val newCircumferenceMm: Float,
    val observedDeviationPercent: Float,
    val qualifiedDistanceMeters: Float,
    val qualifiedSamples: Int
)

/**
 * Long-running GPS assisted controller-speed calibration.
 *
 * The caller must invoke [observe] only from the fresh controller telemetry chain. GPS fixes are
 * de-duplicated by [AutomaticSpeedCalibrationInput.gpsFixToken], so a single location update can
 * never be counted more than once even when BLE notifications arrive more frequently than GPS.
 */
class AutomaticSpeedCalibrator {
    companion object {
        private const val MIN_SPEED_KMH = 18f
        private const val MAX_SPEED_KMH = 130f
        private const val MAX_GPS_AGE_MS = 1_500L
        private const val MAX_HORIZONTAL_ACCURACY_METERS = 15f
        private const val MAX_SPEED_ACCURACY_MPS = 1.2f
        private const val MIN_FIX_INTERVAL_MS = 250L
        private const val MAX_FIX_INTERVAL_MS = 2_000L
        private const val MAX_STEP_DELTA_KMH = 4f
        private const val MIN_QUALIFIED_SAMPLES = 80
        private const val MIN_QUALIFIED_DISTANCE_METERS = 1_000f
        private const val MAX_WINDOW_SAMPLES = 180
        private const val MAX_RELATIVE_MEDIAN_DEVIATION = 0.015f
        private const val MIN_APPLY_DEVIATION = 0.03f
        private const val MAX_ACCEPTED_DEVIATION = 0.20f
        private const val MAX_SINGLE_ADJUSTMENT = 0.08f
    }

    private val circumferenceCandidates = ArrayDeque<Float>()
    private var lastGpsFixToken = Long.MIN_VALUE
    private var lastAcceptedAtMs = 0L
    private var lastGpsSpeedKmh = 0f
    private var lastControllerSpeedKmh = 0f
    private var qualifiedDistanceMeters = 0f
    private var lastAppliedAtMs = 0L

    private val _state = MutableStateFlow(AutomaticSpeedCalibrationState())
    val state: StateFlow<AutomaticSpeedCalibrationState> = _state.asStateFlow()

    fun setEnabled(enabled: Boolean) {
        if (!enabled) resetWindow()
        _state.value = _state.value.copy(
            enabled = enabled,
            status = if (enabled) "等待可靠 GPS 与控制器速度样本" else "自动校准已关闭"
        )
    }

    fun observe(input: AutomaticSpeedCalibrationInput): AutomaticSpeedCalibrationAdjustment? {
        if (!_state.value.enabled) return null
        if (!input.rpmBasedSpeed) {
            updateStatus("当前控制器未使用轮径推算速度，暂不自动调整")
            return null
        }
        if (!input.freshControllerSample || input.gpsFixToken == lastGpsFixToken) return null
        lastGpsFixToken = input.gpsFixToken

        if (!isReliable(input)) {
            updateStatus("等待可靠 GPS：保持 18km/h 以上稳定直线行驶")
            return null
        }

        val intervalMs = if (lastAcceptedAtMs > 0L) input.timestampMs - lastAcceptedAtMs else 0L
        if (lastAcceptedAtMs > 0L && intervalMs !in MIN_FIX_INTERVAL_MS..MAX_FIX_INTERVAL_MS) {
            resetMotionBaseline(input)
            updateStatus("GPS 采样间隔不稳定，正在重新建立窗口")
            return null
        }

        if (lastAcceptedAtMs > 0L &&
            (abs(input.gpsSpeedKmh - lastGpsSpeedKmh) > MAX_STEP_DELTA_KMH ||
                abs(input.controllerSpeedKmh - lastControllerSpeedKmh) > MAX_STEP_DELTA_KMH)
        ) {
            resetMotionBaseline(input)
            updateStatus("正在加减速，稳定后继续学习")
            return null
        }

        val ratio = input.gpsSpeedKmh / input.controllerSpeedKmh
        if (!ratio.isFinite() || ratio !in 0.8f..1.2f) {
            resetMotionBaseline(input)
            updateStatus("GPS 与控制器偏差异常，已忽略本次样本")
            return null
        }

        if (intervalMs > 0L) {
            qualifiedDistanceMeters += input.gpsSpeedKmh / 3.6f * (intervalMs / 1000f)
        }
        val candidate = input.currentCircumferenceMm * ratio
        if (candidate.isFinite() && candidate in 500f..5000f) {
            circumferenceCandidates.addLast(candidate)
            while (circumferenceCandidates.size > MAX_WINDOW_SAMPLES) {
                circumferenceCandidates.removeFirst()
            }
        }
        resetMotionBaseline(input)

        val estimate = median(circumferenceCandidates)
        val deviation = estimate?.let { (it / input.currentCircumferenceMm) - 1f }
        _state.value = _state.value.copy(
            qualifiedSamples = circumferenceCandidates.size,
            qualifiedDistanceMeters = qualifiedDistanceMeters,
            estimatedCircumferenceMm = estimate,
            deviationPercent = deviation?.times(100f),
            status = buildProgressStatus()
        )

        if (circumferenceCandidates.size < MIN_QUALIFIED_SAMPLES ||
            qualifiedDistanceMeters < MIN_QUALIFIED_DISTANCE_METERS ||
            estimate == null || deviation == null
        ) {
            return null
        }

        val relativeMad = medianAbsoluteDeviation(circumferenceCandidates, estimate) / estimate
        if (!relativeMad.isFinite() || relativeMad > MAX_RELATIVE_MEDIAN_DEVIATION) {
            updateStatus("样本仍有波动，继续积累可靠里程")
            return null
        }
        if (abs(deviation) < MIN_APPLY_DEVIATION) {
            updateStatus("控制器速度已准确，无需调整")
            trimWindowToRecentSamples()
            return null
        }
        if (abs(deviation) > MAX_ACCEPTED_DEVIATION) {
            updateStatus("偏差过大，已停止自动应用，请手动校准确认")
            resetWindow()
            return null
        }

        val boundedDeviation = deviation.coerceIn(-MAX_SINGLE_ADJUSTMENT, MAX_SINGLE_ADJUSTMENT)
        val newCircumference = (input.currentCircumferenceMm * (1f + boundedDeviation))
            .coerceIn(500f, 5000f)
        val adjustment = AutomaticSpeedCalibrationAdjustment(
            previousCircumferenceMm = input.currentCircumferenceMm,
            newCircumferenceMm = newCircumference,
            observedDeviationPercent = deviation * 100f,
            qualifiedDistanceMeters = qualifiedDistanceMeters,
            qualifiedSamples = circumferenceCandidates.size
        )
        lastAppliedAtMs = input.timestampMs
        resetWindow()
        _state.value = _state.value.copy(
            enabled = true,
            lastAppliedAtMs = lastAppliedAtMs,
            estimatedCircumferenceMm = newCircumference,
            deviationPercent = adjustment.observedDeviationPercent,
            status = "已自动校准至 ${newCircumference.toInt()}mm"
        )
        return adjustment
    }

    private fun isReliable(input: AutomaticSpeedCalibrationInput): Boolean {
        val speedAccuracy = input.gpsSpeedAccuracyMetersPerSecond
        return input.gpsAgeMs in 0L..MAX_GPS_AGE_MS &&
            input.gpsSpeedKmh.isFinite() && input.gpsSpeedKmh in MIN_SPEED_KMH..MAX_SPEED_KMH &&
            input.controllerSpeedKmh.isFinite() && input.controllerSpeedKmh in MIN_SPEED_KMH..MAX_SPEED_KMH &&
            input.currentCircumferenceMm.isFinite() && input.currentCircumferenceMm in 500f..5000f &&
            input.gpsHorizontalAccuracyMeters.isFinite() &&
            input.gpsHorizontalAccuracyMeters in 0f..MAX_HORIZONTAL_ACCURACY_METERS &&
            (speedAccuracy == null ||
                (speedAccuracy.isFinite() && speedAccuracy in 0f..MAX_SPEED_ACCURACY_MPS))
    }

    private fun resetMotionBaseline(input: AutomaticSpeedCalibrationInput) {
        lastAcceptedAtMs = input.timestampMs
        lastGpsSpeedKmh = input.gpsSpeedKmh
        lastControllerSpeedKmh = input.controllerSpeedKmh
    }

    private fun buildProgressStatus(): String {
        val distance = qualifiedDistanceMeters.toInt()
        return when {
            circumferenceCandidates.size < MIN_QUALIFIED_SAMPLES ->
                "可靠样本 ${circumferenceCandidates.size}/$MIN_QUALIFIED_SAMPLES · ${distance}m"
            qualifiedDistanceMeters < MIN_QUALIFIED_DISTANCE_METERS ->
                "可靠里程 ${distance}/${MIN_QUALIFIED_DISTANCE_METERS.toInt()}m"
            else -> "正在验证长期偏差稳定性"
        }
    }

    private fun trimWindowToRecentSamples() {
        while (circumferenceCandidates.size > MIN_QUALIFIED_SAMPLES / 2) {
            circumferenceCandidates.removeFirst()
        }
        qualifiedDistanceMeters *= 0.5f
    }

    private fun updateStatus(status: String) {
        _state.value = _state.value.copy(status = status)
    }

    private fun resetWindow() {
        circumferenceCandidates.clear()
        qualifiedDistanceMeters = 0f
        lastGpsFixToken = Long.MIN_VALUE
        lastAcceptedAtMs = 0L
        lastGpsSpeedKmh = 0f
        lastControllerSpeedKmh = 0f
        _state.value = _state.value.copy(
            qualifiedSamples = 0,
            qualifiedDistanceMeters = 0f,
            estimatedCircumferenceMm = null,
            deviationPercent = null,
            lastAppliedAtMs = lastAppliedAtMs
        )
    }

    private fun median(values: Collection<Float>): Float? {
        if (values.isEmpty()) return null
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2f
        } else {
            sorted[middle]
        }
    }

    private fun medianAbsoluteDeviation(values: Collection<Float>, center: Float): Float {
        return median(values.map { abs(it - center) }) ?: Float.MAX_VALUE
    }
}
