package com.shawnrain.habe.data

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
    private val metricsRpmFlow: StateFlow<Float>,
    private val polePairsFlow: StateFlow<Int>,
    private val currentCircumferenceFlow: StateFlow<Float>,
    private val locationFlow: StateFlow<Location?>
) {
    companion object {
        private const val MIN_SPEED_KMH = 15f
        private const val STABLE_WINDOW_SAMPLES = 6
        private const val MAX_JITTER_RATIO = 0.05f
        private const val MIN_READY_DISTANCE_METERS = 200f
        private const val MAX_READY_DISTANCE_METERS = 500f
        private const val MIN_STABLE_WINDOWS = 2
    }

    private data class Session(
        val startTs: Long,
        var lastUpdateTs: Long,
        var lastLocation: Location?,
        var gpsDistanceMeters: Float = 0f,
        var controllerDistanceMeters: Float = 0f,
        var stableSamples: Int = 0,
        var totalSamples: Int = 0,
        val suggestions: MutableList<Float> = mutableListOf()
    )

    private val gpsHistory = ArrayDeque<Float>(STABLE_WINDOW_SAMPLES)
    private val rpmHistory = ArrayDeque<Float>(STABLE_WINDOW_SAMPLES)
    private var observingStarted = false
    private var session: Session? = null

    private val _state = MutableStateFlow(GpsCalibrationState())
    val state: StateFlow<GpsCalibrationState> = _state.asStateFlow()

    fun startObserving() {
        if (observingStarted) return
        observingStarted = true

        combine(
            gpsSpeedFlow,
            metricsRpmFlow,
            polePairsFlow,
            currentCircumferenceFlow,
            locationFlow
        ) { gpsSpeed, rpm, polePairs, circumference, location ->
            CalibrationInputs(gpsSpeed, rpm, polePairs, circumference, location)
        }.onEach(::handleInputs).launchIn(scope)
    }

    fun startSession() {
        val now = System.currentTimeMillis()
        gpsHistory.clear()
        rpmHistory.clear()
        session = Session(
            startTs = now,
            lastUpdateTs = now,
            lastLocation = locationFlow.value
        )
        _state.value = GpsCalibrationState(isRunning = true)
    }

    fun stopSession() {
        val currentState = _state.value
        gpsHistory.clear()
        rpmHistory.clear()
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
        val controllerSpeed = calculateControllerSpeed(
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

        val suggestedCircumference = updateStableSuggestion(
            gpsSpeed = inputs.gpsSpeedKmh,
            rpm = inputs.rpm,
            polePairs = inputs.polePairs,
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
        gpsSpeed: Float,
        rpm: Float,
        polePairs: Int,
        currentCircumferenceMm: Float,
        activeSession: Session
    ): Float? {
        if (gpsSpeed < MIN_SPEED_KMH || rpm <= 0f || polePairs <= 0 || currentCircumferenceMm <= 0f) {
            gpsHistory.clear()
            rpmHistory.clear()
            return averageSuggestion(activeSession.suggestions)
        }

        gpsHistory.addLast(gpsSpeed)
        rpmHistory.addLast(rpm)
        if (gpsHistory.size > STABLE_WINDOW_SAMPLES) gpsHistory.removeFirst()
        if (rpmHistory.size > STABLE_WINDOW_SAMPLES) rpmHistory.removeFirst()

        if (gpsHistory.size < STABLE_WINDOW_SAMPLES || rpmHistory.size < STABLE_WINDOW_SAMPLES) {
            return averageSuggestion(activeSession.suggestions)
        }

        val gpsAvg = gpsHistory.average().toFloat()
        val rpmAvg = rpmHistory.average().toFloat()
        if (gpsAvg <= 0f || rpmAvg <= 0f) return averageSuggestion(activeSession.suggestions)

        val gpsJitter = ((gpsHistory.maxOrNull() ?: gpsAvg) - (gpsHistory.minOrNull() ?: gpsAvg)) / gpsAvg
        val rpmJitter = ((rpmHistory.maxOrNull() ?: rpmAvg) - (rpmHistory.minOrNull() ?: rpmAvg)) / rpmAvg
        if (gpsJitter >= MAX_JITTER_RATIO || rpmJitter >= MAX_JITTER_RATIO) {
            return averageSuggestion(activeSession.suggestions)
        }

        val effectiveRpm = rpmAvg / polePairs
        if (effectiveRpm <= 0f) return averageSuggestion(activeSession.suggestions)

        val derivedCircumference = (gpsAvg * 1_000_000f) / (effectiveRpm * 60f)
        if (derivedCircumference !in 500f..5000f) {
            return averageSuggestion(activeSession.suggestions)
        }

        val lastSuggestion = activeSession.suggestions.lastOrNull()
        if (lastSuggestion == null || abs(derivedCircumference - lastSuggestion) / lastSuggestion <= 0.08f) {
            activeSession.stableSamples += 1
            activeSession.suggestions += derivedCircumference
            if (activeSession.suggestions.size > 8) {
                activeSession.suggestions.removeFirst()
            }
        }

        return averageSuggestion(activeSession.suggestions)
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

    private fun averageSuggestion(values: List<Float>): Float? {
        if (values.isEmpty()) return null
        return values.sum() / values.size
    }

    private data class CalibrationInputs(
        val gpsSpeedKmh: Float,
        val rpm: Float,
        val polePairs: Int,
        val currentCircumferenceMm: Float,
        val location: Location?
    )
}
