package com.shawnrain.sdash.data

import android.location.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.abs

data class GpsCalibrationState(
    val isRunning: Boolean = false,
    val gpsDistanceMeters: Float = 0f,
    val controllerDistanceMeters: Float = 0f,
    val durationSec: Int = 0,
    val stableSamples: Int = 0,
    val totalSamples: Int = 0,
    val gpsSpeedKmh: Float = 0f,
    val controllerSpeedKmh: Float = 0f,
    val suggestedCircumferenceMm: Float? = null,
    val deltaPercent: Float? = null,
    val hint: String = "保持 15km/h 以上匀速行驶 200-500 米以生成推荐轮径"
)

/**
 * GPS-assisted wheel circumference calibration.
 *
 * The session compares GPS speed with motor RPM under stable cruising conditions,
 * derives a recommended wheel circumference, and exposes detailed session state for UI.
 */
class AutoCalibrator(
    private val scope: CoroutineScope,
    private val gpsSpeedFlow: StateFlow<Float>,
    private val controllerSpeedFlow: StateFlow<Float>,
    private val metricsRpmFlow: StateFlow<Float>,
    private val polePairsFlow: StateFlow<Int>,
    private val currentCircumferenceFlow: StateFlow<Float>,
    private val locationFlow: StateFlow<Location?>
) {
    companion object {
        private const val MIN_SPEED_KMH = 15f
        private const val SAMPLE_INTERVAL_MS = 800L
        private const val STABLE_WINDOW_SAMPLES = 8
        private const val MAX_JITTER_RATIO = 0.12f
        private const val MAX_STEP_DELTA_KMH = 3.5f
        private const val MAX_LAG_SAMPLES = 3
        private const val MIN_READY_DISTANCE_METERS = 200f
        private const val MAX_READY_DISTANCE_METERS = 500f
        private const val MIN_STABLE_WINDOWS = 2
    }

    private data class TimedSpeedSample(
        val timestampMs: Long,
        val gpsSpeedKmh: Float,
        val controllerSpeedKmh: Float
    )

    private data class Session(
        val startTs: Long,
        var lastUpdateTs: Long,
        var lastLocation: Location?,
        var lastRecordedSampleTs: Long = 0L,
        var gpsDistanceMeters: Float = 0f,
        var controllerDistanceMeters: Float = 0f,
        var stableSamples: Int = 0,
        var totalSamples: Int = 0,
        val suggestions: MutableList<Float> = mutableListOf(),
        val speedSamples: MutableList<TimedSpeedSample> = mutableListOf()
    )
    private var observingStarted = false
    private var session: Session? = null

    private val _state = MutableStateFlow(GpsCalibrationState())
    val state: StateFlow<GpsCalibrationState> = _state.asStateFlow()

    fun startObserving() {
        if (observingStarted) return
        observingStarted = true

        combine(
            gpsSpeedFlow,
            controllerSpeedFlow,
            metricsRpmFlow,
            polePairsFlow,
            currentCircumferenceFlow,
            locationFlow
        ) { values ->
            CalibrationInputs(
                gpsSpeedKmh = values[0] as Float,
                controllerSpeedKmh = values[1] as Float,
                rpm = values[2] as Float,
                polePairs = values[3] as Int,
                currentCircumferenceMm = values[4] as Float,
                location = values[5] as Location?
            )
        }.onEach(::handleInputs).launchIn(scope)
    }

    fun startSession() {
        val now = System.currentTimeMillis()
        session = Session(
            startTs = now,
            lastUpdateTs = now,
            lastLocation = locationFlow.value
        )
        _state.value = GpsCalibrationState(isRunning = true)
    }

    fun stopSession() {
        val currentState = _state.value
        session = null
        _state.value = currentState.copy(
            isRunning = false,
            hint = if (currentState.suggestedCircumferenceMm != null) {
                "校准已停止，可直接应用推荐轮径"
            } else {
                "GPS 校准已停止"
            }
        )
    }

    private fun handleInputs(inputs: CalibrationInputs) {
        val activeSession = session ?: return
        val now = System.currentTimeMillis()
        val controllerSpeed = inputs.controllerSpeedKmh
            .takeIf { it > 0.5f }
            ?: calculateControllerSpeed(
                rpm = inputs.rpm,
                polePairs = inputs.polePairs,
                wheelCircumferenceMm = inputs.currentCircumferenceMm
            )

        val elapsedSeconds = ((now - activeSession.startTs) / 1000L).toInt().coerceAtLeast(0)
        val deltaSeconds = ((now - activeSession.lastUpdateTs).coerceAtLeast(0L) / 1000f)
        if (deltaSeconds > 0f) {
            activeSession.controllerDistanceMeters += (controllerSpeed / 3.6f) * deltaSeconds
            activeSession.lastUpdateTs = now
        }

        updateGpsDistance(activeSession, inputs.location)
        activeSession.totalSamples += 1
        maybeRecordSpeedSample(
            activeSession = activeSession,
            now = now,
            gpsSpeed = inputs.gpsSpeedKmh,
            controllerSpeed = controllerSpeed
        )

        val suggestedCircumference = updateStableSuggestion(
            currentCircumferenceMm = inputs.currentCircumferenceMm,
            activeSession = activeSession
        )

        val readySuggestion = if (
            activeSession.gpsDistanceMeters >= MIN_READY_DISTANCE_METERS &&
            activeSession.stableSamples >= MIN_STABLE_WINDOWS
        ) {
            suggestedCircumference
        } else {
            null
        }

        val deltaPercent = readySuggestion?.let {
            ((it / inputs.currentCircumferenceMm) - 1f) * 100f
        }?.takeIf { it.isFinite() }

        _state.value = GpsCalibrationState(
            isRunning = true,
            gpsDistanceMeters = activeSession.gpsDistanceMeters,
            controllerDistanceMeters = activeSession.controllerDistanceMeters,
            durationSec = elapsedSeconds,
            stableSamples = activeSession.stableSamples,
            totalSamples = activeSession.totalSamples,
            gpsSpeedKmh = inputs.gpsSpeedKmh,
            controllerSpeedKmh = controllerSpeed,
            suggestedCircumferenceMm = readySuggestion,
            deltaPercent = deltaPercent,
            hint = buildHint(
                gpsSpeedKmh = inputs.gpsSpeedKmh,
                gpsDistanceMeters = activeSession.gpsDistanceMeters,
                stableSamples = activeSession.stableSamples,
                suggestedCircumferenceMm = readySuggestion
            )
        )
    }

    private fun updateGpsDistance(activeSession: Session, location: Location?) {
        if (location == null) return
        val previous = activeSession.lastLocation
        activeSession.lastLocation = location
        if (previous == null) return

        val distance = previous.distanceTo(location)
        if (distance.isFinite() && distance in 0.5f..120f) {
            activeSession.gpsDistanceMeters += distance
        }
    }

    private fun updateStableSuggestion(
        currentCircumferenceMm: Float,
        activeSession: Session
    ): Float? {
        if (currentCircumferenceMm <= 0f) {
            return robustSuggestion(activeSession.suggestions)
        }
        val alignedWindow = bestAlignedWindow(activeSession.speedSamples)
            ?: return robustSuggestion(activeSession.suggestions)
        val derivedCircumference = currentCircumferenceMm * alignedWindow.speedRatio
        if (derivedCircumference !in 500f..5000f) {
            return robustSuggestion(activeSession.suggestions)
        }

        val referenceSuggestion = robustSuggestion(activeSession.suggestions)
        if (referenceSuggestion == null || abs(derivedCircumference - referenceSuggestion) / referenceSuggestion <= 0.08f) {
            activeSession.stableSamples += 1
            activeSession.suggestions += derivedCircumference
            if (activeSession.suggestions.size > 12) {
                activeSession.suggestions.removeAt(0)
            }
        }

        return robustSuggestion(activeSession.suggestions)
    }

    private fun maybeRecordSpeedSample(
        activeSession: Session,
        now: Long,
        gpsSpeed: Float,
        controllerSpeed: Float
    ) {
        if (now - activeSession.lastRecordedSampleTs < SAMPLE_INTERVAL_MS) return
        activeSession.lastRecordedSampleTs = now
        activeSession.speedSamples += TimedSpeedSample(
            timestampMs = now,
            gpsSpeedKmh = gpsSpeed.coerceAtLeast(0f),
            controllerSpeedKmh = controllerSpeed.coerceAtLeast(0f)
        )
        if (activeSession.speedSamples.size > 36) {
            activeSession.speedSamples.removeAt(0)
        }
    }

    private data class StableWindow(val speedRatio: Float)

    private fun bestAlignedWindow(samples: List<TimedSpeedSample>): StableWindow? {
        if (samples.size < STABLE_WINDOW_SAMPLES + MAX_LAG_SAMPLES) return null
        var bestWindow: StableWindow? = null
        var bestScore = Float.MAX_VALUE

        for (lag in 0..MAX_LAG_SAMPLES) {
            val gpsWindow = samples.dropLast(lag).takeLast(STABLE_WINDOW_SAMPLES)
            val controllerWindow = samples.takeLast(STABLE_WINDOW_SAMPLES)
            if (gpsWindow.size < STABLE_WINDOW_SAMPLES || controllerWindow.size < STABLE_WINDOW_SAMPLES) continue

            val gpsValues = gpsWindow.map { it.gpsSpeedKmh }
            val controllerValues = controllerWindow.map { it.controllerSpeedKmh }
            val gpsAvg = gpsValues.average().toFloat()
            val controllerAvg = controllerValues.average().toFloat()
            if (gpsAvg < MIN_SPEED_KMH || controllerAvg < MIN_SPEED_KMH) continue

            val gpsJitter = normalizedJitter(gpsValues, gpsAvg)
            val controllerJitter = normalizedJitter(controllerValues, controllerAvg)
            val gpsStepDelta = maxStepDelta(gpsValues)
            val controllerStepDelta = maxStepDelta(controllerValues)
            if (
                gpsJitter > MAX_JITTER_RATIO ||
                controllerJitter > MAX_JITTER_RATIO ||
                gpsStepDelta > MAX_STEP_DELTA_KMH ||
                controllerStepDelta > MAX_STEP_DELTA_KMH
            ) {
                continue
            }

            val ratio = (gpsAvg / controllerAvg).takeIf { it.isFinite() && it in 0.4f..2.2f } ?: continue
            val score = gpsJitter + controllerJitter + (gpsStepDelta + controllerStepDelta) / 10f + lag * 0.015f
            if (score < bestScore) {
                bestScore = score
                bestWindow = StableWindow(speedRatio = ratio)
            }
        }
        return bestWindow
    }

    private fun normalizedJitter(values: List<Float>, average: Float): Float {
        if (values.isEmpty() || average <= 0f) return Float.MAX_VALUE
        return ((values.maxOrNull() ?: average) - (values.minOrNull() ?: average)) / average
    }

    private fun maxStepDelta(values: List<Float>): Float {
        return values.zipWithNext { a, b -> abs(a - b) }.maxOrNull() ?: 0f
    }

    private fun buildHint(
        gpsSpeedKmh: Float,
        gpsDistanceMeters: Float,
        stableSamples: Int,
        suggestedCircumferenceMm: Float?
    ): String {
        return when {
            gpsSpeedKmh < MIN_SPEED_KMH -> "请保持 15km/h 以上匀速巡航，便于采样 GPS 与电机转速"
            gpsDistanceMeters < MIN_READY_DISTANCE_METERS -> {
                val remain = (MIN_READY_DISTANCE_METERS - gpsDistanceMeters).coerceAtLeast(0f).toInt()
                "已采集 ${gpsDistanceMeters.toInt()}m，建议继续骑行约 ${remain}m"
            }
            stableSamples < MIN_STABLE_WINDOWS -> "已达到基础距离，等待 GPS 与转速波动进一步稳定"
            suggestedCircumferenceMm != null && gpsDistanceMeters <= MAX_READY_DISTANCE_METERS ->
                "推荐轮径周长 ${suggestedCircumferenceMm.toInt()}mm，可直接应用"
            suggestedCircumferenceMm != null ->
                "推荐值已稳定，继续骑行也可进一步微调"
            else -> "继续保持匀速直线骑行，正在计算推荐轮径"
        }
    }

    private fun calculateControllerSpeed(rpm: Float, polePairs: Int, wheelCircumferenceMm: Float): Float {
        if (rpm <= 0f || polePairs <= 0 || wheelCircumferenceMm <= 0f) return 0f
        val wheelRpm = rpm / polePairs
        return (wheelRpm * wheelCircumferenceMm * 60f) / 1_000_000f
    }

    private fun robustSuggestion(values: List<Float>): Float? {
        if (values.isEmpty()) return null
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2f
        } else {
            sorted[middle]
        }
    }

    private data class CalibrationInputs(
        val gpsSpeedKmh: Float,
        val controllerSpeedKmh: Float,
        val rpm: Float,
        val polePairs: Int,
        val currentCircumferenceMm: Float,
        val location: Location?
    )
}
