package com.shawnrain.sdash.data.gps

import android.location.Location
import kotlin.math.abs
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

private data class GradeWindowSample(
    val cumulativeDistanceMeters: Double,
    val altitudeMeters: Double,
    val timestampMs: Long
)

class GradeEstimator {
    companion object {
        private const val ALTITUDE_EMA_ALPHA = 0.2
        private const val GRADE_EMA_ALPHA = 0.3f
        private const val MIN_ALTITUDE_UPDATE_SPEED_KMH = 1f
        private const val MIN_GRADE_UPDATE_SPEED_KMH = 3f
        private const val MAX_VERTICAL_ACCURACY_METERS = 12f
        private const val MAX_SHORT_HOP_ALTITUDE_JUMP_METERS = 3.0
        private const val MAX_SHORT_HOP_DISTANCE_METERS = 5.0
        private const val GRADE_WINDOW_DISTANCE_METERS = 20.0
        private const val MIN_GRADE_DISTANCE_METERS = 10.0
        private const val GRADE_UPDATE_INTERVAL_MS = 1_000L
        private const val MAX_SAMPLE_GAP_MS = 8_000L
        private const val MAX_ABSOLUTE_GRADE_PERCENT = 15f
        private const val LOW_SPEED_GRADE_DECAY = 0.9f
        private const val MIN_NON_ZERO_GRADE_PERCENT = 0.05f
    }

    private val _currentGradePercent = MutableStateFlow(0f)
    val currentGradePercent: StateFlow<Float> = _currentGradePercent.asStateFlow()

    private val _currentAltitudeMeters = MutableStateFlow<Double?>(null)
    val currentAltitudeMeters: StateFlow<Double?> = _currentAltitudeMeters.asStateFlow()

    private var lastInput: GradeEstimatorInput? = null
    private var filteredAltitudeMeters: Double? = null
    private var cumulativeDistanceMeters = 0.0
    private var lastGradeUpdateAtMs = 0L
    private val gradeWindow = ArrayDeque<GradeWindowSample>()

    fun update(location: Location): GradeEstimate = update(GradeEstimatorInput.fromLocation(location))

    fun update(input: GradeEstimatorInput): GradeEstimate {
        val previous = lastInput
        val distanceFromPrevious = previous?.let {
            haversineMeters(
                startLat = it.latitude,
                startLon = it.longitude,
                endLat = input.latitude,
                endLon = input.longitude
            )
        }?.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
        val durationMs = previous?.let { input.timestampMs - it.timestampMs } ?: 0L

        if (durationMs > MAX_SAMPLE_GAP_MS || durationMs < 0L) {
            resetGradeWindow()
        } else if (distanceFromPrevious > 0.0) {
            cumulativeDistanceMeters += distanceFromPrevious
        }

        maybeUpdateFilteredAltitude(
            input = input,
            distanceFromPreviousMeters = distanceFromPrevious
        )
        maybeAppendWindowSample(input.timestampMs)
        updateGrade(input)

        lastInput = input
        return GradeEstimate(
            currentGradePercent = _currentGradePercent.value,
            currentAltitudeMeters = _currentAltitudeMeters.value
        )
    }

    private fun maybeUpdateFilteredAltitude(
        input: GradeEstimatorInput,
        distanceFromPreviousMeters: Double
    ) {
        val rawAltitude = input.altitudeMeters?.takeIf { it.isFinite() } ?: return
        input.verticalAccuracyMeters?.let {
            if (it > MAX_VERTICAL_ACCURACY_METERS) return
        }
        if ((input.speedKmh.takeIf { it.isFinite() } ?: 0f) < MIN_ALTITUDE_UPDATE_SPEED_KMH) {
            return
        }

        val previousFilteredAltitude = filteredAltitudeMeters
        val shouldRejectShortHopJump = previousFilteredAltitude != null &&
            distanceFromPreviousMeters in 0.0..MAX_SHORT_HOP_DISTANCE_METERS &&
            abs(rawAltitude - previousFilteredAltitude) > MAX_SHORT_HOP_ALTITUDE_JUMP_METERS
        if (shouldRejectShortHopJump) {
            return
        }

        val nextFilteredAltitude = if (previousFilteredAltitude == null) {
            rawAltitude
        } else {
            (ALTITUDE_EMA_ALPHA * rawAltitude) + ((1.0 - ALTITUDE_EMA_ALPHA) * previousFilteredAltitude)
        }

        filteredAltitudeMeters = nextFilteredAltitude
        _currentAltitudeMeters.value = nextFilteredAltitude
    }

    private fun maybeAppendWindowSample(timestampMs: Long) {
        val altitude = filteredAltitudeMeters ?: return
        val last = gradeWindow.lastOrNull()
        if (last == null || cumulativeDistanceMeters > last.cumulativeDistanceMeters) {
            gradeWindow.addLast(
                GradeWindowSample(
                    cumulativeDistanceMeters = cumulativeDistanceMeters,
                    altitudeMeters = altitude,
                    timestampMs = timestampMs
                )
            )
        } else if (last.timestampMs != timestampMs && abs(altitude - last.altitudeMeters) > 0.01) {
            gradeWindow.removeLast()
            gradeWindow.addLast(
                last.copy(
                    altitudeMeters = altitude,
                    timestampMs = timestampMs
                )
            )
        }

        while (gradeWindow.size > 2) {
            val second = gradeWindow.elementAt(1)
            if (cumulativeDistanceMeters - second.cumulativeDistanceMeters > GRADE_WINDOW_DISTANCE_METERS) {
                gradeWindow.removeFirst()
            } else {
                break
            }
        }
    }

    private fun updateGrade(input: GradeEstimatorInput) {
        val speedKmh = input.speedKmh.takeIf { it.isFinite() } ?: 0f
        if (speedKmh < MIN_GRADE_UPDATE_SPEED_KMH) {
            _currentGradePercent.value = decayedGrade(_currentGradePercent.value)
            return
        }

        if (input.timestampMs - lastGradeUpdateAtMs < GRADE_UPDATE_INTERVAL_MS) {
            return
        }

        val latest = gradeWindow.lastOrNull() ?: return
        val start = gradeWindow.firstOrNull { sample ->
            latest.cumulativeDistanceMeters - sample.cumulativeDistanceMeters <= GRADE_WINDOW_DISTANCE_METERS
        } ?: return

        val coveredDistanceMeters = latest.cumulativeDistanceMeters - start.cumulativeDistanceMeters
        if (coveredDistanceMeters < MIN_GRADE_DISTANCE_METERS) {
            return
        }

        val rawGradePercent = (((latest.altitudeMeters - start.altitudeMeters) / coveredDistanceMeters) * 100.0)
            .toFloat()
            .coerceIn(-MAX_ABSOLUTE_GRADE_PERCENT, MAX_ABSOLUTE_GRADE_PERCENT)

        val previousGrade = _currentGradePercent.value
        val smoothedGrade = if (previousGrade == 0f) {
            rawGradePercent
        } else {
            (GRADE_EMA_ALPHA * rawGradePercent) + ((1f - GRADE_EMA_ALPHA) * previousGrade)
        }

        _currentGradePercent.value = smoothedGrade
            .coerceIn(-MAX_ABSOLUTE_GRADE_PERCENT, MAX_ABSOLUTE_GRADE_PERCENT)
            .takeUnless { abs(it) < MIN_NON_ZERO_GRADE_PERCENT }
            ?: 0f
        lastGradeUpdateAtMs = input.timestampMs
    }

    private fun decayedGrade(currentGrade: Float): Float {
        val next = currentGrade * LOW_SPEED_GRADE_DECAY
        return if (abs(next) < MIN_NON_ZERO_GRADE_PERCENT) 0f else next
    }

    private fun resetGradeWindow() {
        gradeWindow.clear()
        cumulativeDistanceMeters = 0.0
        lastGradeUpdateAtMs = 0L
        _currentGradePercent.value = 0f
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
