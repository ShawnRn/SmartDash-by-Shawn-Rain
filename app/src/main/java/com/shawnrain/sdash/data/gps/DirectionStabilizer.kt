package com.shawnrain.sdash.data.gps

import kotlin.math.abs

/**
 * 稳定化方向显示器：把「偏车头朝向」转成「偏稳定行进方向」。
 * 适用于两轮车仪表，消除修把、低速漂移、传感器抖动造成的 UI 抽动。
 *
 * 处理顺序：
 * 1. 速度分层冻结 → 2. 来源质量门控 → 3. 死区 → 4. 速度分层指数平滑 → 5. 转向速率限制
 */
class DirectionStabilizer {

    // --- State ---
    private var initialized = false
    private var stableDirectionDeg = 0f
    private var lastUpdateMs = 0L

    fun reset() {
        initialized = false
        stableDirectionDeg = 0f
        lastUpdateMs = 0L
    }

    fun update(input: DirectionInput): DirectionOutput {
        val dtSec = ((input.nowMs - lastUpdateMs).coerceAtLeast(0L) / 1000f)
            .takeIf { it > 0f } ?: 0.016f

        val gpsCourse = getUsableGpsCourse(input)
        val sensorHeading = getUsableSensorHeading(input)

        // --- Step 1: Select direction source ---
        val source = when {
            input.speedKmh < FREEZE_SPEED_KMH && initialized -> DirectionSource.FROZEN
            gpsCourse != null -> DirectionSource.GPS_COURSE
            sensorHeading != null && input.speedKmh >= LOW_SPEED_KMH -> DirectionSource.SENSOR_HEADING
            initialized -> DirectionSource.FROZEN
            sensorHeading != null -> DirectionSource.SENSOR_HEADING
            else -> DirectionSource.NONE
        }

        val target = when (source) {
            DirectionSource.GPS_COURSE -> gpsCourse ?: stableDirectionDeg
            DirectionSource.SENSOR_HEADING -> sensorHeading ?: stableDirectionDeg
            DirectionSource.FROZEN, DirectionSource.NONE -> stableDirectionDeg
        }

        // --- Step 2: First-time initialization ---
        if (!initialized) {
            stableDirectionDeg = normalize(target)
            initialized = true
            lastUpdateMs = input.nowMs
            return DirectionOutput(stableDirectionDeg, source, source == DirectionSource.FROZEN)
        }

        // --- Step 3: Dead band ---
        val delta = abs(shortestAngleDelta(stableDirectionDeg, target))
        if (delta < DEAD_BAND_DEG) {
            lastUpdateMs = input.nowMs
            return DirectionOutput(stableDirectionDeg, DirectionSource.FROZEN, true)
        }

        // --- Step 4: Speed-layered exponential smoothing ---
        val alpha = alphaForSpeed(input.speedKmh)
        val smoothed = lerpAngle(stableDirectionDeg, target, alpha)

        // --- Step 5: Max turn rate limit ---
        val maxStep = maxTurnRateForSpeed(input.speedKmh) * dtSec
        stableDirectionDeg = stepTowardAngle(stableDirectionDeg, smoothed, maxStep)

        lastUpdateMs = input.nowMs
        return DirectionOutput(normalize(stableDirectionDeg), source, false)
    }

    // --- Source quality gates ---
    private fun getUsableGpsCourse(input: DirectionInput): Float? {
        val deg = input.gpsCourseDeg ?: return null
        val age = input.gpsCourseAgeMs ?: return null
        val acc = input.gpsAccuracyM ?: Float.MAX_VALUE
        val step = input.gpsStepDistanceM ?: Float.MAX_VALUE
        if (!deg.isFinite()) return null
        if (age !in 0..GPS_MAX_AGE_MS) return null
        if (acc > GPS_MAX_ACCURACY_M) return null
        if (input.speedKmh < FREEZE_SPEED_KMH) return null
        if (input.speedKmh < LOW_SPEED_KMH && step < GPS_MIN_STEP_DISTANCE_M) return null
        return deg
    }

    private fun getUsableSensorHeading(input: DirectionInput): Float? {
        val deg = input.sensorHeadingDeg ?: return null
        val age = input.sensorAgeMs ?: return null
        if (!deg.isFinite()) return null
        if (age !in 0..SENSOR_MAX_AGE_MS) return null
        return deg
    }

    // --- Speed-layered smoothing parameters ---
    private fun alphaForSpeed(speedKmh: Float): Float = when {
        speedKmh < 3f -> 0f
        speedKmh < 8f -> 0.06f
        speedKmh < 15f -> 0.12f
        speedKmh < 25f -> 0.20f
        else -> 0.30f
    }

    private fun maxTurnRateForSpeed(speedKmh: Float): Float = when {
        speedKmh < 8f -> 35f
        speedKmh < 15f -> 50f
        speedKmh < 25f -> 70f
        else -> 90f
    }

    // --- Angle utilities ---
    private fun normalize(value: Float): Float = ((value % 360f) + 360f) % 360f

    private fun shortestAngleDelta(from: Float, to: Float): Float {
        val normalized = ((to - from + 540f) % 360f) - 180f
        return if (normalized == -180f) 180f else normalized
    }

    private fun lerpAngle(current: Float, target: Float, alpha: Float): Float {
        val delta = shortestAngleDelta(current, target)
        return normalize(current + delta * alpha.coerceIn(0f, 1f))
    }

    private fun stepTowardAngle(current: Float, target: Float, maxStep: Float): Float {
        val delta = shortestAngleDelta(current, target)
        val clamped = delta.coerceIn(-maxStep, maxStep)
        return normalize(current + clamped)
    }

    companion object {
        // Speed thresholds
        private const val FREEZE_SPEED_KMH = 3.0f
        private const val LOW_SPEED_KMH = 8.0f
        private const val MID_SPEED_KMH = 15.0f
        private const val HIGH_SPEED_KMH = 25.0f

        // Freshness / quality
        private const val GPS_MAX_AGE_MS = 2500L
        private const val SENSOR_MAX_AGE_MS = 1500L
        private const val GPS_MAX_ACCURACY_M = 25f
        private const val GPS_MIN_STEP_DISTANCE_M = 3f

        // Stabilization
        private const val DEAD_BAND_DEG = 5f
    }
}

// --- Data classes ---
enum class DirectionSource {
    GPS_COURSE,
    SENSOR_HEADING,
    FROZEN,
    NONE
}

data class DirectionInput(
    val nowMs: Long,
    val speedKmh: Float,
    val gpsCourseDeg: Float?,
    val gpsCourseAgeMs: Long?,
    val gpsAccuracyM: Float?,
    val gpsStepDistanceM: Float?,
    val sensorHeadingDeg: Float?,
    val sensorAgeMs: Long?
)

data class DirectionOutput(
    val stableDirectionDeg: Float,
    val source: DirectionSource,
    val isFrozen: Boolean
)
