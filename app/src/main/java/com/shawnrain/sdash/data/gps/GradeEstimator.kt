package com.shawnrain.sdash.data.gps

import android.location.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GradeEstimatorInput(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double?,
    val speedKmh: Float,
    val timestampMs: Long,
    val verticalAccuracyMeters: Float? = null
) {
    companion object {
        fun fromLocation(location: Location): GradeEstimatorInput {
            val verticalAccuracy = if (
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                location.hasVerticalAccuracy()
            ) {
                location.verticalAccuracyMeters
            } else {
                null
            }
            return GradeEstimatorInput(
                latitude = location.latitude,
                longitude = location.longitude,
                altitudeMeters = location.altitude.takeIf { location.hasAltitude() && it.isFinite() },
                speedKmh = (location.speed * 3.6f).takeIf { it.isFinite() } ?: 0f,
                timestampMs = location.time.takeIf { it > 0L } ?: System.currentTimeMillis(),
                verticalAccuracyMeters = verticalAccuracy?.takeIf { it.isFinite() }
            )
        }
    }
}

data class GradeEstimate(
    val currentGradePercent: Float = 0f,
    val currentAltitudeMeters: Double? = null
)

class GradeEstimator {
    companion object {
        private const val MIN_HORIZONTAL_DISTANCE_METERS = 8.0
        private const val MIN_SEGMENT_DURATION_MS = 500L
        private const val MAX_SEGMENT_DURATION_MS = 8_000L
        private const val MIN_SPEED_KMH = 4f
        private const val MAX_VERTICAL_ACCURACY_METERS = 12f
        private const val MAX_ABSOLUTE_GRADE_PERCENT = 35f
        private const val HOLD_AFTER_INVALID_MS = 3_000L
        private const val DECAY_TO_ZERO_MS = 10_000L
        private val SMOOTHING_WEIGHTS = listOf(1f, 2f, 3f)
    }

    private val _currentGradePercent = MutableStateFlow(0f)
    val currentGradePercent: StateFlow<Float> = _currentGradePercent.asStateFlow()

    private val _currentAltitudeMeters = MutableStateFlow<Double?>(null)
    val currentAltitudeMeters: StateFlow<Double?> = _currentAltitudeMeters.asStateFlow()

    private var lastInput: GradeEstimatorInput? = null
    private var lastValidSegmentAtMs: Long = 0L
    private val recentGrades = ArrayDeque<Float>()

    fun update(location: Location): GradeEstimate = update(GradeEstimatorInput.fromLocation(location))

    fun update(input: GradeEstimatorInput): GradeEstimate {
        input.altitudeMeters?.takeIf { it.isFinite() }?.let { _currentAltitudeMeters.value = it }

        val previous = lastInput
        val nextGrade = if (previous != null && isSegmentUsable(previous, input)) {
            val horizontalDistanceMeters = haversineMeters(
                startLat = previous.latitude,
                startLon = previous.longitude,
                endLat = input.latitude,
                endLon = input.longitude
            )
            val elevationDeltaMeters = (input.altitudeMeters ?: 0.0) - (previous.altitudeMeters ?: 0.0)
            ((elevationDeltaMeters / horizontalDistanceMeters) * 100.0)
                .toFloat()
                .coerceIn(-MAX_ABSOLUTE_GRADE_PERCENT, MAX_ABSOLUTE_GRADE_PERCENT)
        } else {
            null
        }

        if (nextGrade != null && nextGrade.isFinite()) {
            if (recentGrades.size == SMOOTHING_WEIGHTS.size) recentGrades.removeFirst()
            recentGrades.addLast(nextGrade)
            _currentGradePercent.value = smoothedGrade()
            lastValidSegmentAtMs = input.timestampMs
        } else {
            _currentGradePercent.value = decayGrade(input.timestampMs)
        }

        lastInput = input
        return GradeEstimate(
            currentGradePercent = _currentGradePercent.value,
            currentAltitudeMeters = _currentAltitudeMeters.value
        )
    }

    private fun isSegmentUsable(previous: GradeEstimatorInput, current: GradeEstimatorInput): Boolean {
        val previousAltitude = previous.altitudeMeters
        val currentAltitude = current.altitudeMeters
        if (previousAltitude == null || currentAltitude == null) return false
        if (!previousAltitude.isFinite() || !currentAltitude.isFinite()) return false
        if ((previous.speedKmh.takeIf { it.isFinite() } ?: 0f) < MIN_SPEED_KMH) return false
        if ((current.speedKmh.takeIf { it.isFinite() } ?: 0f) < MIN_SPEED_KMH) return false

        val durationMs = current.timestampMs - previous.timestampMs
        if (durationMs !in MIN_SEGMENT_DURATION_MS..MAX_SEGMENT_DURATION_MS) return false

        previous.verticalAccuracyMeters?.let {
            if (it > MAX_VERTICAL_ACCURACY_METERS) return false
        }
        current.verticalAccuracyMeters?.let {
            if (it > MAX_VERTICAL_ACCURACY_METERS) return false
        }

        val horizontalDistanceMeters = haversineMeters(
            startLat = previous.latitude,
            startLon = previous.longitude,
            endLat = current.latitude,
            endLon = current.longitude
        )
        return horizontalDistanceMeters >= MIN_HORIZONTAL_DISTANCE_METERS
    }

    private fun smoothedGrade(): Float {
        val grades = recentGrades.toList()
        if (grades.isEmpty()) return 0f
        val weights = SMOOTHING_WEIGHTS.takeLast(grades.size)
        val weightedSum = grades.zip(weights).sumOf { (grade, weight) -> grade * weight.toDouble() }
        val weightTotal = weights.sum().toDouble().takeIf { it > 0.0 } ?: return grades.last()
        return (weightedSum / weightTotal).toFloat()
    }

    private fun decayGrade(nowMs: Long): Float {
        val current = _currentGradePercent.value
        if (current == 0f || lastValidSegmentAtMs <= 0L) return 0f
        val elapsedMs = (nowMs - lastValidSegmentAtMs).coerceAtLeast(0L)
        return when {
            elapsedMs <= HOLD_AFTER_INVALID_MS -> current
            elapsedMs >= DECAY_TO_ZERO_MS -> 0f
            else -> {
                val remainFraction = 1f - (
                    (elapsedMs - HOLD_AFTER_INVALID_MS).toFloat() /
                        (DECAY_TO_ZERO_MS - HOLD_AFTER_INVALID_MS).toFloat()
                    )
                current * remainFraction.coerceIn(0f, 1f)
            }
        }
    }

    private fun haversineMeters(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double
    ): Double {
        val earthRadiusMeters = 6_371_000.0
        val dLat = Math.toRadians(endLat - startLat)
        val dLon = Math.toRadians(endLon - startLon)
        val startLatRad = Math.toRadians(startLat)
        val endLatRad = Math.toRadians(endLat)
        val a = sin(dLat / 2.0) * sin(dLat / 2.0) +
            cos(startLatRad) * cos(endLatRad) *
            sin(dLon / 2.0) * sin(dLon / 2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return earthRadiusMeters * c
    }
}
