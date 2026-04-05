package com.shawnrain.habe

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shawnrain.habe.ble.BleManager
import com.shawnrain.habe.ble.ConnectionState
import com.shawnrain.habe.ble.ProtocolParser
import com.shawnrain.habe.ble.VehicleMetrics
import com.shawnrain.habe.ble.bms.BmsParser
import com.shawnrain.habe.ble.bms.BmsMetrics
import com.shawnrain.habe.data.gps.HeadingTracker
import com.shawnrain.habe.data.gps.GpsTracker
import com.shawnrain.habe.ble.protocols.ZhikeProtocol
import com.shawnrain.habe.ble.protocols.ZhikeSettings
import com.shawnrain.habe.data.SpeedSource
import com.shawnrain.habe.data.DataSource
import com.shawnrain.habe.data.AutoCalibrator
import com.shawnrain.habe.data.GpsCalibrationState
import com.shawnrain.habe.data.RideEnergyEstimator
import com.shawnrain.habe.data.SettingsRepository
import com.shawnrain.habe.data.RideSession
import com.shawnrain.habe.data.VehicleProfile
import com.shawnrain.habe.data.migration.LanBackupQrPayload
import com.shawnrain.habe.data.migration.LanBackupTransfer
import com.shawnrain.habe.data.history.RideHistoryRecord
import com.shawnrain.habe.data.history.RideMetricSample
import com.shawnrain.habe.data.history.RideTrackPoint
import com.shawnrain.habe.debug.AppLogLevel
import com.shawnrain.habe.debug.AppLogger
import com.shawnrain.habe.data.speedtest.SpeedTestRecord
import com.shawnrain.habe.data.speedtest.SpeedTestSessionUiState
import com.shawnrain.habe.data.speedtest.SpeedTestTrackPoint
import com.shawnrain.habe.ui.poster.PosterRenderer
import com.shawnrain.habe.ui.text.withDisplaySpacing
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PendingRideStopUiState(
    val title: String,
    val message: String,
    val remainingSeconds: Int,
    val isDisconnect: Boolean
)

data class LanBackupShareUiState(
    val isSharing: Boolean = false,
    val host: String = "",
    val port: Int = 0,
    val code: String = ""
)

private data class PendingRideStopSnapshot(
    val tripDistanceMeters: Double,
    val lastRideLocation: Location?,
    val maxSpeed: Float,
    val totalSpeedSum: Float,
    val speedSamples: Int,
    val totalEnergyUsedWh: Double,
    val lastEnergyUpdateTime: Long,
    val ridePeakPowerKw: Float,
    val ridePeakRegenKw: Float,
    val rideEnergyWh: Float,
    val rideRecoveredEnergyWh: Float,
    val rideMaxControllerTemp: Float,
    val rideLastEnergyUpdateAtMs: Long,
    val rideLastSampleAtMs: Long,
    val trackPointCount: Int,
    val sampleCount: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private enum class RideStartMode {
        MANUAL,
        AUTO
    }

    private enum class RideStopReason {
        PARKED,
        DISCONNECTED
    }

    companion object {
        private const val TAG = "MainViewModel"
        private const val AUTO_RIDE_START_SPEED_KMH = 4f
        private const val AUTO_RIDE_STOP_SPEED_KMH = 1.2f
        private const val AUTO_RIDE_STOP_DELAY_MS = 20000L
        private const val AUTO_RIDE_STOP_COUNTDOWN_MS = 10000L
        private const val AUTO_DISCONNECT_STOP_COUNTDOWN_MS = 4000L
        private const val RIDE_SAMPLE_INTERVAL_MS = 1000L
        private const val SPEED_TEST_SAMPLE_INTERVAL_MS = 120L
        private const val HISTORY_LIMIT = 30
        private const val AUTO_RECONNECT_SCAN_WINDOW_MS = 5000L
        private const val AUTO_RECONNECT_SCAN_COOLDOWN_MS = 12000L
        private const val AUTO_RECONNECT_SUPPRESS_MS = 180000L
        private const val GPS_FRESH_TIMEOUT_MS = 3000L
        private const val MIN_VALID_EFFICIENCY_WH_KM = 5f
        private const val MAX_VALID_EFFICIENCY_WH_KM = 120f
        private const val DEFAULT_STARTUP_EFFICIENCY_WH_KM = 35f
    }

    private val bleManager = BleManager(application)
    private val bmsBleManager = BleManager(application)
    private val settingsRepository = SettingsRepository(application)
    private val lanBackupTransfer = LanBackupTransfer()
    val gpsTracker = GpsTracker(application)
    private val headingTracker = HeadingTracker(application)

    private val _currentRpm = MutableStateFlow(0f)
    private val _controllerReportedSpeed = MutableStateFlow(0f)
    private val _latestZhikeSettings = MutableStateFlow<ZhikeSettings?>(null)
    private var autoReconnectAttemptedAddress: String? = null
    private var autoReconnectSuppressedUntilMs = 0L
    private var autoReconnectScanActive = false
    private var autoReconnectWatchdogJob: Job? = null
    private var _tripDistanceMeters = 0.0
    private var _restingVoltageBaseline = 0f
    private var _lastRideLocation: Location? = null
    private var _rideStopCandidateAtMs: Long? = null
    private var _rideLastSampleAtMs = 0L
    private var _rideStartedAtMs = 0L
    private var _ridePeakPowerKw = 0f
    private var _ridePeakRegenKw = 0f
    private var _rideEnergyWh = 0f
    private var _rideRecoveredEnergyWh = 0f
    private var _rideMaxControllerTemp = 0f
    private var _rideLastEnergyUpdateAtMs = 0L
    private val rideTrackPoints = mutableListOf<RideTrackPoint>()
    private val rideSamples = mutableListOf<RideMetricSample>()
    private var rideStartMode: RideStartMode? = null
    private var pendingRideStopReason: RideStopReason? = null
    private var pendingRideStopCutoffAtMs = 0L
    private var pendingRideStopSnapshot: PendingRideStopSnapshot? = null
    private var pendingRideStopJob: Job? = null
    private var _speedTestLastLocation: Location? = null
    private var _speedTestStartedAtMs = 0L
    private var _speedTestLastSampleAtMs = 0L
    private var _speedTestDistanceMeters = 0f
    private var _speedTestPeakPowerKw = 0f
    private var _speedTestPeakBusCurrentA = 0f
    private var _speedTestMinVoltage = 0f
    private var _speedTestMaxSpeed = 0f
    private var _speedTestTargetLabel = ""
    private var _speedTestTargetSpeedKmh = 0f
    private val speedTestTrackPoints = mutableListOf<SpeedTestTrackPoint>()

    private val _isRideActive = MutableStateFlow(false)
    val isRideActive: StateFlow<Boolean> = _isRideActive.asStateFlow()
    private val _pendingRideStop = MutableStateFlow<PendingRideStopUiState?>(null)
    val pendingRideStop: StateFlow<PendingRideStopUiState?> = _pendingRideStop.asStateFlow()
    private var _maxSpeed = 0f
    private var _totalSpeedSum = 0f
    private var _speedSamples = 0
    private var _sessionStartTime = 0L
    private var _lastEnergyUpdateTime = 0L
    private var _totalEnergyUsedWh = 0.0
    private var rideStartVehicleMileageKm = 0f
    private var lastVehicleSnapshotAtMs = 0L
    private var lastVehicleSnapshotTripDistanceKm = 0f
    private var estimatorVehicleId: String? = null
    private var estimatorProtocolId: String? = null
    private val rideEnergyEstimator = RideEnergyEstimator()
    private var lastKnownSocPercent: Float = Float.NaN
    private var lastKnownRangeKm: Float = Float.NaN

    val speedSource = settingsRepository.speedSource.stateIn(viewModelScope, SharingStarted.Lazily, SpeedSource.CONTROLLER)
    val battDataSource = settingsRepository.battDataSource.stateIn(viewModelScope, SharingStarted.Lazily, DataSource.CONTROLLER)
    val wheelCircumference = settingsRepository.wheelCircumference.stateIn(viewModelScope, SharingStarted.Lazily, 1800f)
    val polePairs = settingsRepository.polePairs.stateIn(viewModelScope, SharingStarted.Lazily, 50)
    val controllerBrand = settingsRepository.controllerBrand.stateIn(viewModelScope, SharingStarted.Lazily, "auto")
    val logLevel = settingsRepository.logLevel.stateIn(viewModelScope, SharingStarted.Lazily, AppLogLevel.INFO)
    val overlayEnabled = settingsRepository.overlayEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val vehicleProfiles = settingsRepository.vehicleProfiles.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        listOf(VehicleProfile.default())
    )
    val currentVehicle = settingsRepository.currentVehicleProfile.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        VehicleProfile.default()
    )
    val lastControllerDeviceAddress = settingsRepository.lastControllerDeviceAddress.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )
    val lastControllerDeviceName = settingsRepository.lastControllerDeviceName.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )
    val lastControllerProtocolId = settingsRepository.lastControllerProtocolId.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )
    private val autoCalibrator = AutoCalibrator(
        scope = viewModelScope,
        gpsSpeedFlow = gpsTracker.gpsSpeed,
        controllerSpeedFlow = _controllerReportedSpeed,
        metricsRpmFlow = _currentRpm,
        polePairsFlow = polePairs,
        currentCircumferenceFlow = wheelCircumference,
        locationFlow = gpsTracker.location
    )

    private val _dashboardItems = MutableStateFlow<List<com.shawnrain.habe.data.MetricType>>(emptyList())
    val dashboardItems: StateFlow<List<com.shawnrain.habe.data.MetricType>> = _dashboardItems.asStateFlow()
    private val _rideOverviewItems = MutableStateFlow<List<com.shawnrain.habe.data.MetricType>>(emptyList())
    val rideOverviewItems: StateFlow<List<com.shawnrain.habe.data.MetricType>> = _rideOverviewItems.asStateFlow()

    // Persistent Scan State for Connection Screen UI
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()
    private val _autoRideSuppressedUntilStop = MutableStateFlow(false)
    private val _speedTestSession = MutableStateFlow(SpeedTestSessionUiState())
    val speedTestSession: StateFlow<SpeedTestSessionUiState> = _speedTestSession.asStateFlow()
    private val _speedTestHistory = MutableStateFlow<List<SpeedTestRecord>>(emptyList())
    val speedTestHistory: StateFlow<List<SpeedTestRecord>> = _speedTestHistory.asStateFlow()
    private val _rideHistory = MutableStateFlow<List<RideHistoryRecord>>(emptyList())
    val rideHistory: StateFlow<List<RideHistoryRecord>> = _rideHistory.asStateFlow()

    val metrics: StateFlow<VehicleMetrics> = combine(
        ProtocolParser.metrics,
        BmsParser.metrics,
        gpsTracker.gpsSpeed,
        speedSource,
        battDataSource,
        wheelCircumference,
        polePairs,
        _isRideActive,
        ProtocolParser.activeProtocolId,
        _latestZhikeSettings
    ) { args ->
        val bleMetrics = args[0] as VehicleMetrics
        val bmsMetrics = args[1] as BmsMetrics
        val gpsSpeed = args[2] as Float
        val sSource = args[3] as SpeedSource
        val bSource = args[4] as DataSource
        val wheelMm = args[5] as Float
        val polePairCount = args[6] as Int
        val rideActive = args[7] as Boolean
        val activeProtocolId = args[8] as String?
        val zhikeSettings = args[9] as ZhikeSettings?

        calculateVehicleMetrics(
            bleMetrics = bleMetrics,
            bmsMetrics = bmsMetrics,
            gpsSpeed = gpsSpeed,
            sSource = sSource,
            bSource = bSource,
            wheelCircumferenceMm = wheelMm,
            polePairCount = polePairCount,
            rideActive = rideActive,
            activeProtocolId = activeProtocolId,
            zhikeSettings = zhikeSettings
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VehicleMetrics())
    val rideDirectionLabel: StateFlow<String> = combine(
        metrics,
        headingTracker.headingDegrees
    ) { vehicleMetrics, headingDegrees ->
        when {
            vehicleMetrics.isReverse -> "倒车"
            else -> formatHeadingLabel(headingDegrees)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "北")

    val isDrivingMode: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

    private fun calculateVehicleMetrics(
        bleMetrics: VehicleMetrics,
        bmsMetrics: BmsMetrics,
        gpsSpeed: Float,
        sSource: SpeedSource,
        bSource: DataSource,
        wheelCircumferenceMm: Float,
        polePairCount: Int,
        rideActive: Boolean,
        activeProtocolId: String?,
        zhikeSettings: ZhikeSettings?
    ): VehicleMetrics {
        val (volt, curr) = if (bSource == DataSource.BMS) {
            bmsMetrics.totalVoltage to bmsMetrics.current
        } else {
            bleMetrics.voltage to bleMetrics.busCurrent
        }

        val controllerSpeed = resolveControllerSpeed(
            bleMetrics = bleMetrics,
            activeProtocolId = activeProtocolId,
            wheelCircumferenceMm = wheelCircumferenceMm,
            polePairCount = polePairCount,
            zhikeSettings = zhikeSettings
        )

        val speed = if (sSource == SpeedSource.GPS) gpsSpeed else controllerSpeed
        val powerW = volt * curr
        val efficiency = if (speed > 5f) powerW / speed else 0f
        val currentVehicleProfile = currentVehicle.value
        val voltageSag = calculateVoltageSag(
            voltage = volt,
            busCurrent = curr,
            speedKmh = speed
        )

        // Energy Tracking
        val now = System.currentTimeMillis()
        val ridePausedForStop = isRidePausedForStop()
        if (rideActive && !ridePausedForStop && _lastEnergyUpdateTime > 0) {
            val deltaH = (now - _lastEnergyUpdateTime) / 3600000.0
            if (powerW >= 0f) {
                _totalEnergyUsedWh += powerW * deltaH
            } else {
                _rideRecoveredEnergyWh += (-powerW * deltaH).toFloat()
                _ridePeakRegenKw = maxOf(_ridePeakRegenKw, -powerW / 1000f)
            }
        }
        _lastEnergyUpdateTime = now
        if (rideActive && !ridePausedForStop) {
            _rideMaxControllerTemp = maxOf(_rideMaxControllerTemp, bleMetrics.controllerTemp)
        }

        val distanceKm = _tripDistanceMeters / 1000.0
        val fallbackSoc = selectFallbackSocPercent(
            voltage = volt,
            bmsSocPercent = if (bSource == DataSource.BMS) bmsMetrics.soc.takeIf { it in 1f..100f } else null,
            batterySeries = currentVehicleProfile.batterySeries,
            activeProtocolId = activeProtocolId
        )
        val rideEstimate = rideEnergyEstimator.estimate(
            rawVoltage = volt,
            rawCurrent = curr,
            distanceKm = distanceKm,
            batterySeries = currentVehicleProfile.batterySeries,
            batteryCapacityAh = currentVehicleProfile.batteryCapacityAh,
            fallbackSocPercent = fallbackSoc,
            nowMs = now
        )
        val currentTripEnergyWh = maxOf(_rideEnergyWh, _totalEnergyUsedWh.toFloat())
        val tripAverageEfficiencyWhKm = when {
            distanceKm > 0.02 && currentTripEnergyWh > 0.5f -> (currentTripEnergyWh / distanceKm).toFloat()
            else -> 0f
        }
        val avgEff = when {
            tripAverageEfficiencyWhKm > 0.1f -> tripAverageEfficiencyWhKm
            rideEstimate.averageWindowEfficiencyWhKm > 0.1f -> rideEstimate.averageWindowEfficiencyWhKm
            else -> 0f
        }
        val rangeBasisEfficiencyWhKm = when {
            tripAverageEfficiencyWhKm > 8f && distanceKm >= 0.6 -> tripAverageEfficiencyWhKm
            rideEstimate.averageWindowEfficiencyWhKm > 8f -> rideEstimate.averageWindowEfficiencyWhKm
            else -> avgEff
        }
        val blendedRangeEfficiencyWhKm = blendRangeEfficiencyWhKm(
            shortTermEfficiencyWhKm = rangeBasisEfficiencyWhKm,
            learnedEfficiencyWhKm = currentVehicleProfile.learnedEfficiencyWhKm,
            tripDistanceKm = distanceKm.toFloat()
        )
        val rawEstimatedRangeKm = if (blendedRangeEfficiencyWhKm > 0.1f) {
            (rideEstimate.remainingEnergyWh / blendedRangeEfficiencyWhKm).coerceAtLeast(0f)
        } else {
            0f
        }
        val displaySocPercent = stabilizeSocForDisplay(rideEstimate.displaySocPercent)
        val estimatedRangeKm = stabilizeRangeForDisplay(
            rawRangeKm = rawEstimatedRangeKm,
            remainingEnergyWh = rideEstimate.remainingEnergyWh,
            learnedEfficiencyWhKm = currentVehicleProfile.learnedEfficiencyWhKm
        )

        if (rideActive && speed > 2f) {
            if (speed > _maxSpeed) _maxSpeed = speed
            _totalSpeedSum += speed
            _speedSamples++
        }

        return bleMetrics.copy(
            voltage = volt,
            busCurrent = curr,
            voltageSag = voltageSag,
            totalPowerW = powerW,
            speedKmH = speed,
            efficiencyWhKm = efficiency,
            tripDistance = distanceKm,
            soc = displaySocPercent,
            estimatedRangeKm = estimatedRangeKm,
            avgEfficiencyWhKm = avgEff,
            totalEnergyWh = if (rideActive || _tripDistanceMeters > 0.0) currentTripEnergyWh else 0f,
            recoveredEnergyWh = if (rideActive) _rideRecoveredEnergyWh else 0f,
            peakRegenPowerKw = if (rideActive) _ridePeakRegenKw else 0f,
            maxControllerTemp = if (rideActive) _rideMaxControllerTemp else bleMetrics.controllerTemp
        )
    }

    private fun calculateControllerSpeed(rpm: Float, polePairCount: Int, wheelCircumferenceMm: Float): Float {
        if (rpm <= 0f || polePairCount <= 0 || wheelCircumferenceMm <= 0f) return 0f
        val wheelRpm = rpm / polePairCount
        return (wheelRpm * wheelCircumferenceMm * 60f) / 1_000_000f
    }

    private fun calculateZhikeControllerSpeed(
        rpm: Float,
        wheelCircumferenceMm: Float
    ): Float {
        if (rpm <= 0f || wheelCircumferenceMm <= 0f) return 0f
        return ((rpm * wheelCircumferenceMm * 60f) / 1_000_000f).coerceIn(0f, 160f)
    }

    private fun resolveControllerSpeed(
        bleMetrics: VehicleMetrics,
        activeProtocolId: String?,
        wheelCircumferenceMm: Float,
        polePairCount: Int,
        zhikeSettings: ZhikeSettings?
    ): Float {
        return when (activeProtocolId) {
            "zhike" -> {
                val rpmSpeed = calculateZhikeControllerSpeed(
                    rpm = bleMetrics.rpm,
                    wheelCircumferenceMm = wheelCircumferenceMm
                )
                rpmSpeed.takeIf { it > 0.2f } ?: 0f
            }
            else -> {
                if (bleMetrics.speedKmH > 0f) {
                    bleMetrics.speedKmH
                } else {
                    calculateControllerSpeed(bleMetrics.rpm, polePairCount, wheelCircumferenceMm)
                }
            }
        }
    }

    private fun calculateVoltageSag(
        voltage: Float,
        busCurrent: Float,
        speedKmh: Float
    ): Float {
        if (voltage <= 0f) return 0f
        val absCurrent = kotlin.math.abs(busCurrent)
        if (absCurrent < 1.5f && speedKmh < 3f) {
            _restingVoltageBaseline = voltage
            return 0f
        }
        if (_restingVoltageBaseline <= 0f) {
            _restingVoltageBaseline = voltage
        }
        return (_restingVoltageBaseline - voltage).coerceAtLeast(0f)
    }

    private fun selectFallbackSocPercent(
        voltage: Float,
        bmsSocPercent: Float?,
        batterySeries: Int,
        activeProtocolId: String?
    ): Float? {
        if (bmsSocPercent != null) return bmsSocPercent
        return estimateControllerSoc(
            voltage = voltage,
            batterySeries = batterySeries,
            activeProtocolId = activeProtocolId
        ).takeIf { it in 1f..100f }
    }

    private fun estimateControllerSoc(
        voltage: Float,
        batterySeries: Int,
        activeProtocolId: String?
    ): Float {
        if (voltage <= 0f) return 0f

        val normalizedSeries = inferBatterySeriesFromVoltage(
            voltage = voltage,
            configuredSeries = batterySeries,
            zhikeSettings = if (activeProtocolId == "zhike") _latestZhikeSettings.value else null
        )
        val ocvSoc = socFromPackVoltage(
            packVoltage = voltage,
            batterySeries = normalizedSeries
        ).takeIf { it in 0f..100f }

        return when {
            ocvSoc != null -> ocvSoc
            voltage >= 81f -> linearSoc(voltage, 60f, 84f)
            voltage >= 66f -> linearSoc(voltage, 56f, 72f)
            voltage >= 49f -> linearSoc(voltage, 42f, 58f)
            voltage >= 35f -> linearSoc(voltage, 31f, 44f)
            else -> 0f
        }
    }

    private fun inferBatterySeriesFromVoltage(
        voltage: Float,
        configuredSeries: Int,
        zhikeSettings: ZhikeSettings?
    ): Int {
        if (voltage <= 0f) return configuredSeries.coerceAtLeast(1)

        val candidates = (8..24).toList()
        val plausibleByVoltage = candidates.filter { series ->
            val minPackVoltage = series * 3.0f
            val maxPackVoltage = series * 4.25f
            voltage in minPackVoltage..maxPackVoltage
        }

        val configured = configuredSeries.coerceAtLeast(1)
        if (configured in plausibleByVoltage) {
            return configured
        }

        val inferredFromZhikeParam = zhikeSettings
            ?.rawWords
            ?.getOrNull(46)
            ?.let { word -> word and 0x3F }
            ?.takeIf { it in plausibleByVoltage }
        if (inferredFromZhikeParam != null) {
            return inferredFromZhikeParam
        }

        val inferredFromZhikeVoltage = zhikeSettings
            ?.takeIf { it.overVoltage > it.underVoltage }
            ?.let { settings ->
                (settings.overVoltage.toFloat() / 4.2f).roundToInt()
            }
            ?.takeIf { it in plausibleByVoltage }
        if (inferredFromZhikeVoltage != null) {
            return inferredFromZhikeVoltage
        }

        return plausibleByVoltage.minByOrNull { series ->
            abs(voltage - (series * 3.7f))
        } ?: configured.coerceIn(8, 24)
    }

    private fun socFromPackVoltage(packVoltage: Float, batterySeries: Int): Float {
        val series = batterySeries.coerceAtLeast(1)
        val cellVoltage = packVoltage / series
        val table = listOf(
            4.20f to 100f,
            4.10f to 92f,
            4.00f to 82f,
            3.92f to 72f,
            3.85f to 62f,
            3.79f to 52f,
            3.73f to 42f,
            3.68f to 32f,
            3.63f to 22f,
            3.58f to 14f,
            3.50f to 8f,
            3.40f to 3f,
            3.30f to 0f
        )
        if (cellVoltage >= table.first().first) return table.first().second
        if (cellVoltage <= table.last().first) return table.last().second
        table.zipWithNext().forEach { (high, low) ->
            if (cellVoltage <= high.first && cellVoltage >= low.first) {
                val progress = (cellVoltage - low.first) / (high.first - low.first)
                return low.second + ((high.second - low.second) * progress)
            }
        }
        return 0f
    }

    private fun linearSoc(voltage: Float, emptyVoltage: Float, fullVoltage: Float): Float {
        if (fullVoltage <= emptyVoltage) return 0f
        return (((voltage - emptyVoltage) / (fullVoltage - emptyVoltage)) * 100f).coerceIn(0f, 100f)
    }

    private fun isRecentGpsAvailable(now: Long = System.currentTimeMillis()): Boolean {
        val lastLocation = gpsTracker.location.value ?: return false
        val ageMs = now - lastLocation.time
        return ageMs in 0..GPS_FRESH_TIMEOUT_MS
    }

    private fun currentSpeedForSpeedTest(metrics: VehicleMetrics, now: Long): Float {
        return if (isRecentGpsAvailable(now)) {
            gpsTracker.gpsSpeed.value.coerceAtLeast(0f)
        } else {
            metrics.speedKmH.coerceAtLeast(0f)
        }
    }

    private val _calibrationMessage = MutableStateFlow<String?>(null)
    val calibrationMessage: StateFlow<String?> = _calibrationMessage
    fun clearCalibrationMessage() { _calibrationMessage.value = null }
    private val _lanBackupShare = MutableStateFlow(LanBackupShareUiState())
    val lanBackupShare: StateFlow<LanBackupShareUiState> = _lanBackupShare.asStateFlow()
    private val _lanBackupRestoring = MutableStateFlow(false)
    val lanBackupRestoring: StateFlow<Boolean> = _lanBackupRestoring.asStateFlow()
    private val _lanBackupMessage = MutableStateFlow<String?>(null)
    val lanBackupMessage: StateFlow<String?> = _lanBackupMessage.asStateFlow()
    fun clearLanBackupMessage() { _lanBackupMessage.value = null }
    fun showLanBackupMessage(message: String) { _lanBackupMessage.value = message }
    val gpsCalibrationState: StateFlow<GpsCalibrationState> = autoCalibrator.state
    val activeProtocolLabel: StateFlow<String> = ProtocolParser.activeProtocolLabel
    val bmsActiveProtocolLabel: StateFlow<String> = BmsParser.activeProtocolLabel
    val bmsMetrics: StateFlow<BmsMetrics> = BmsParser.metrics

    init {
        settingsRepository.dashboardItems.onEach { items ->
            if (_dashboardItems.value.isEmpty()) {
                _dashboardItems.value = items
            }
        }.launchIn(viewModelScope)

        settingsRepository.rideOverviewItems.onEach { items ->
            if (_rideOverviewItems.value.isEmpty()) {
                _rideOverviewItems.value = items
            }
        }.launchIn(viewModelScope)

        settingsRepository.logLevel.onEach { level ->
            AppLogger.setMinLevel(level)
        }.launchIn(viewModelScope)

        settingsRepository.speedTestHistory.onEach { history ->
            _speedTestHistory.value = history
        }.launchIn(viewModelScope)

        settingsRepository.rideHistory.onEach { history ->
            _rideHistory.value = history
        }.launchIn(viewModelScope)

        currentVehicle.onEach { profile ->
            if (estimatorVehicleId != profile.id) {
                estimatorVehicleId = profile.id
                rideEnergyEstimator.reset(profile.learnedInternalResistanceOhm)
                _restingVoltageBaseline = 0f
                lastKnownSocPercent = Float.NaN
                lastKnownRangeKm = Float.NaN
            }
        }.launchIn(viewModelScope)

        bleManager.rawData.onEach { ProtocolParser.parse(it) }.launchIn(viewModelScope)
        bmsBleManager.rawData.onEach { BmsParser.parse(it) }.launchIn(viewModelScope)
        ProtocolParser.zhikeSettings.onEach { _latestZhikeSettings.value = it }.launchIn(viewModelScope)
        ProtocolParser.metrics.onEach {
            _currentRpm.value = it.rpm
        }.launchIn(viewModelScope)
        combine(
            ProtocolParser.metrics,
            ProtocolParser.activeProtocolId,
            wheelCircumference,
            polePairs,
            _latestZhikeSettings
        ) { bleMetrics, activeProtocolId, wheelMm, polePairCount, zhikeSettings ->
            resolveControllerSpeed(
                bleMetrics = bleMetrics,
                activeProtocolId = activeProtocolId,
                wheelCircumferenceMm = wheelMm,
                polePairCount = polePairCount,
                zhikeSettings = zhikeSettings
            )
        }.onEach { resolvedSpeed ->
            _controllerReportedSpeed.value = resolvedSpeed
        }.launchIn(viewModelScope)
        autoCalibrator.startObserving()
        headingTracker.start()

        ProtocolParser.autoConfigUpdates.onEach { (key, value) ->
            if (key == "polePairs") {
                savePolePairs(value)
                _calibrationMessage.value = "已自动从控制器读取极对数: $value"
            }
        }.launchIn(viewModelScope)

        bmsBleManager.connectionState.onEach { state ->
            if (state is ConnectionState.Connected) {
                BmsParser.selectProtocol(state.device.name ?: "", emptyList())
            } else if (state is ConnectionState.Disconnected) {
                BmsParser.reset()
            }
        }.launchIn(viewModelScope)

        bleManager.connectionState.onEach { state ->
            if (state is ConnectionState.Connected) {
                autoReconnectAttemptedAddress = state.device.address
                stopAutoReconnectWatchdog()
                if (pendingRideStopReason == RideStopReason.DISCONNECTED) {
                    cancelPendingRideStop("控制器已恢复连接，继续记录本次行程")
                }
                viewModelScope.launch {
                    settingsRepository.saveLastControllerProfile(
                        address = state.device.address,
                        name = state.device.name ?: lastControllerDeviceName.value,
                        protocolId = activeProtocolId.value ?: lastControllerProtocolId.value
                    )
                }
            }
            if (state is ConnectionState.Disconnected && _isRideActive.value) {
                beginPendingRideStop(
                    reason = RideStopReason.DISCONNECTED,
                    cutoffAtMs = System.currentTimeMillis()
                )
            }
            if (state is ConnectionState.Disconnected) {
                lastControllerDeviceAddress.value?.let { address ->
                    startAutoReconnectWatchdog(address, reason = "disconnect")
                }
            }
        }.launchIn(viewModelScope)

        ProtocolParser.activeProtocolId.onEach { protocolId ->
            if (protocolId != estimatorProtocolId && !_isRideActive.value) {
                estimatorProtocolId = protocolId
                rideEnergyEstimator.reset(currentVehicle.value.learnedInternalResistanceOhm)
                _restingVoltageBaseline = 0f
            }
            val connected = bleManager.connectionState.value as? ConnectionState.Connected ?: return@onEach
            if (protocolId.isNullOrBlank()) return@onEach
            viewModelScope.launch {
                settingsRepository.saveLastControllerProfile(
                    address = connected.device.address,
                    name = connected.device.name ?: lastControllerDeviceName.value,
                    protocolId = protocolId
                )
            }
        }.launchIn(viewModelScope)

        settingsRepository.lastControllerDeviceAddress.onEach { address ->
            if (!address.isNullOrBlank()) {
                tryAutoReconnect(address, reason = "startup")
                startAutoReconnectWatchdog(address, reason = "startup")
            }
        }.launchIn(viewModelScope)

        bleManager.scannedDevices.onEach { devices ->
            val address = lastControllerDeviceAddress.value ?: return@onEach
            if (!isAutoReconnectAllowed(address)) return@onEach
            val target = devices.firstOrNull { it.address == address } ?: return@onEach
            AppLogger.i(TAG, "守护扫描发现记忆控制器，准备自动连接 address=$address")
            stopAutoReconnectWatchdog()
            autoReconnectAttemptedAddress = address
            bleManager.connect(target)
        }.launchIn(viewModelScope)

        gpsTracker.location.onEach { location ->
            location?.let {
                headingTracker.updateFromLocation(it)
                handleLocationUpdate(it)
            }
        }.launchIn(viewModelScope)

        combine(speedSource, gpsCalibrationState, _isRideActive, _speedTestSession) { source, calibration, rideActive, speedTest ->
            source == SpeedSource.GPS || calibration.isRunning || rideActive || speedTest.isActive
        }.onEach { shouldTrack ->
            if (shouldTrack) gpsTracker.startTracking() else gpsTracker.stopTracking()
        }.launchIn(viewModelScope)

        metrics.onEach { handleMetricsTick(it) }.launchIn(viewModelScope)

        metrics.map { it.speedKmH <= AUTO_RIDE_STOP_SPEED_KMH }
            .distinctUntilChanged()
            .onEach { isStopped ->
                if (isStopped && _autoRideSuppressedUntilStop.value) {
                    _autoRideSuppressedUntilStop.value = false
                    AppLogger.d(TAG, "车辆已停止，恢复自动行程开始资格")
                }
            }
            .launchIn(viewModelScope)
    }

    private val _lastRideSummary = MutableStateFlow<RideSession?>(null)
    val lastRideSummary: StateFlow<RideSession?> = _lastRideSummary.asStateFlow()

    private fun handleLocationUpdate(location: Location) {
        if (_isRideActive.value && !isRidePausedForStop()) {
            val lastRideLocation = _lastRideLocation
            if (lastRideLocation != null) {
                _tripDistanceMeters += lastRideLocation.distanceTo(location).toDouble()
            }
            _lastRideLocation = location
            val point = RideTrackPoint(location.latitude, location.longitude)
            if (rideTrackPoints.lastOrNull() != point) {
                rideTrackPoints.add(point)
            }
        }

        if (_speedTestSession.value.isActive) {
            val lastSpeedTestLocation = _speedTestLastLocation
            if (lastSpeedTestLocation != null) {
                _speedTestDistanceMeters += lastSpeedTestLocation.distanceTo(location)
            }
            _speedTestLastLocation = location
            val point = SpeedTestTrackPoint(location.latitude, location.longitude)
            if (speedTestTrackPoints.lastOrNull() != point) {
                speedTestTrackPoints.add(point)
            }
        }
    }

    private fun handleMetricsTick(metrics: VehicleMetrics) {
        val now = System.currentTimeMillis()

        if (_isRideActive.value) {
            updateRideSession(metrics, now)
        } else if (
            !_autoRideSuppressedUntilStop.value &&
            bleManager.connectionState.value is ConnectionState.Connected &&
            metrics.speedKmH >= AUTO_RIDE_START_SPEED_KMH
        ) {
            startRideInternal(RideStartMode.AUTO)
            updateRideSession(metrics, now)
        }

        if (_speedTestSession.value.isActive) {
            updateSpeedTestSession(metrics, now)
        }
    }

    private fun updateRideSession(metrics: VehicleMetrics, now: Long) {
        val speed = metrics.speedKmH
        if (_pendingRideStop.value != null) {
            if (speed >= AUTO_RIDE_START_SPEED_KMH) {
                _rideStopCandidateAtMs = null
                cancelPendingRideStop("车辆恢复移动，继续记录本次行程")
            } else {
                return
            }
        }

        if (speed <= AUTO_RIDE_STOP_SPEED_KMH) {
            val stopCandidate = _rideStopCandidateAtMs
            if (stopCandidate == null) {
                _rideStopCandidateAtMs = now
            } else if (now - stopCandidate >= AUTO_RIDE_STOP_DELAY_MS) {
                beginPendingRideStop(
                    reason = RideStopReason.PARKED,
                    cutoffAtMs = now
                )
            }
        } else {
            _rideStopCandidateAtMs = null
        }

        if (_rideLastEnergyUpdateAtMs > 0L) {
            val deltaHours = (now - _rideLastEnergyUpdateAtMs) / 3_600_000f
            if (metrics.totalPowerW >= 0f) {
                _rideEnergyWh += (metrics.totalPowerW * deltaHours)
            } else {
                _rideRecoveredEnergyWh += (-metrics.totalPowerW * deltaHours)
                _ridePeakRegenKw = maxOf(_ridePeakRegenKw, -metrics.totalPowerW / 1000f)
            }
        }
        _rideLastEnergyUpdateAtMs = now
        _ridePeakPowerKw = maxOf(_ridePeakPowerKw, metrics.totalPowerW / 1000f)
        _rideMaxControllerTemp = maxOf(_rideMaxControllerTemp, metrics.controllerTemp)

        if (now - _rideLastSampleAtMs >= RIDE_SAMPLE_INTERVAL_MS) {
            rideSamples.add(
                RideMetricSample(
                    elapsedMs = now - _rideStartedAtMs,
                    timestampMs = now,
                    speedKmH = metrics.speedKmH,
                    powerKw = metrics.totalPowerW / 1000f,
                    voltage = metrics.voltage,
                    voltageSag = metrics.voltageSag,
                    busCurrent = metrics.busCurrent,
                    phaseCurrent = metrics.phaseCurrent,
                    motorTemp = metrics.motorTemp,
                    controllerTemp = metrics.controllerTemp,
                    soc = metrics.soc,
                    estimatedRangeKm = metrics.estimatedRangeKm,
                    rpm = metrics.rpm,
                    efficiencyWhKm = metrics.efficiencyWhKm,
                    avgEfficiencyWhKm = metrics.avgEfficiencyWhKm,
                    distanceMeters = _tripDistanceMeters.toFloat(),
                    totalEnergyWh = metrics.totalEnergyWh,
                    recoveredEnergyWh = metrics.recoveredEnergyWh,
                    maxControllerTemp = metrics.maxControllerTemp,
                    latitude = _lastRideLocation?.latitude,
                    longitude = _lastRideLocation?.longitude
                )
            )
            _rideLastSampleAtMs = now
        }

        maybePersistVehicleSnapshot(now)
    }

    private fun updateSpeedTestSession(metrics: VehicleMetrics, now: Long) {
        val session = _speedTestSession.value
        if (!session.isActive) return
        val speedKmh = currentSpeedForSpeedTest(metrics, now)

        if (session.isStandby) {
            if (speedKmh > 0.5f) {
                // 起步，解除 Standby 开始计时
                _speedTestStartedAtMs = now
                _speedTestSession.value = session.copy(
                    isStandby = false,
                    statusText = "正在冲刺 ${_speedTestTargetLabel}"
                )
            } else {
                return // 等待起步中，不记录数据
            }
        }

        _speedTestMaxSpeed = maxOf(_speedTestMaxSpeed, speedKmh)
        _speedTestPeakPowerKw = maxOf(_speedTestPeakPowerKw, metrics.totalPowerW / 1000f)
        _speedTestPeakBusCurrentA = maxOf(_speedTestPeakBusCurrentA, metrics.busCurrent)
        _speedTestMinVoltage = if (_speedTestMinVoltage <= 0f) {
            metrics.voltage
        } else {
            minOf(_speedTestMinVoltage, metrics.voltage)
        }

        if (now - _speedTestLastSampleAtMs >= SPEED_TEST_SAMPLE_INTERVAL_MS) {
            _speedTestSession.value = _speedTestSession.value.copy(
                elapsedMs = now - _speedTestStartedAtMs,
                currentSpeedKmh = speedKmh,
                maxSpeedKmh = _speedTestMaxSpeed,
                peakPowerKw = _speedTestPeakPowerKw,
                peakBusCurrentA = _speedTestPeakBusCurrentA,
                minVoltage = _speedTestMinVoltage,
                distanceMeters = _speedTestDistanceMeters,
                statusText = "正在冲刺 ${_speedTestTargetLabel}"
            )
            _speedTestLastSampleAtMs = now
        }

        if (speedKmh >= _speedTestTargetSpeedKmh) {
            val record = SpeedTestRecord(
                id = UUID.randomUUID().toString(),
                label = _speedTestTargetLabel,
                targetSpeedKmh = _speedTestTargetSpeedKmh,
                timeMs = now - _speedTestStartedAtMs,
                timestampMs = now,
                maxSpeedKmh = _speedTestMaxSpeed,
                peakPowerKw = _speedTestPeakPowerKw,
                peakBusCurrentA = _speedTestPeakBusCurrentA,
                minVoltage = _speedTestMinVoltage,
                distanceMeters = _speedTestDistanceMeters,
                trackPoints = speedTestTrackPoints.toList()
            )
            val updated = listOf(record) + _speedTestHistory.value
            _speedTestHistory.value = updated.take(HISTORY_LIMIT)
            viewModelScope.launch {
                settingsRepository.saveSpeedTestHistory(_speedTestHistory.value)
            }
            _speedTestSession.value = _speedTestSession.value.copy(
                isActive = false,
                elapsedMs = record.timeMs,
                currentSpeedKmh = speedKmh,
                maxSpeedKmh = _speedTestMaxSpeed,
                peakPowerKw = _speedTestPeakPowerKw,
                peakBusCurrentA = _speedTestPeakBusCurrentA,
                minVoltage = _speedTestMinVoltage,
                distanceMeters = _speedTestDistanceMeters,
                statusText = "完成 ${record.label}，成绩 ${formatDuration(record.timeMs)}"
            )
        }
    }

    private fun startRideInternal(mode: RideStartMode) {
        _tripDistanceMeters = 0.0
        _maxSpeed = 0f
        _totalSpeedSum = 0f
        _speedSamples = 0
        _totalEnergyUsedWh = 0.0
        _lastEnergyUpdateTime = System.currentTimeMillis()
        _sessionStartTime = System.currentTimeMillis()
        _rideStartedAtMs = _sessionStartTime
        rideStartVehicleMileageKm = currentVehicle.value.totalMileageKm
        lastVehicleSnapshotAtMs = _sessionStartTime
        lastVehicleSnapshotTripDistanceKm = 0f
        _ridePeakPowerKw = 0f
        _ridePeakRegenKw = 0f
        _rideEnergyWh = 0f
        _rideRecoveredEnergyWh = 0f
        _rideMaxControllerTemp = 0f
        _rideLastEnergyUpdateAtMs = _sessionStartTime
        _rideLastSampleAtMs = 0L
        _rideStopCandidateAtMs = null
        clearPendingRideStopState()
        _lastRideLocation = null
        rideTrackPoints.clear()
        rideSamples.clear()
        rideStartMode = mode
        _autoRideSuppressedUntilStop.value = false
        rideEnergyEstimator.reset(currentVehicle.value.learnedInternalResistanceOhm)
        _isRideActive.value = true
    }

    fun startRide() {
        startRideInternal(RideStartMode.MANUAL)
    }

    fun stopRide(forceSave: Boolean = false, stopAtMs: Long = System.currentTimeMillis()) {
        if (!_isRideActive.value) return
        clearPendingRideStopState()
        _isRideActive.value = false
        val endedAtMs = stopAtMs.coerceAtLeast(_sessionStartTime)
        val durationMs = endedAtMs - _sessionStartTime
        val avgSpeed = if (_speedSamples > 0) _totalSpeedSum / _speedSamples else 0f
        val finalEnergyWh = maxOf(_rideEnergyWh, _totalEnergyUsedWh.toFloat())
        val finalAvgEfficiencyWhKm = if (_tripDistanceMeters > 20.0) {
            (finalEnergyWh / (_tripDistanceMeters / 1000.0)).toFloat()
        } else {
            0f
        }
        val dateText = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        _lastRideSummary.value = RideSession(
            date = dateText,
            distanceKm = _tripDistanceMeters / 1000.0,
            durationMin = (durationMs / 60000).toInt(),
            maxSpeed = _maxSpeed,
            avgSpeed = avgSpeed,
            totalWh = finalEnergyWh,
            avgEfficiency = finalAvgEfficiencyWhKm
        )
        val historyRecord = RideHistoryRecord(
            id = UUID.randomUUID().toString(),
            title = buildRideTitle(_sessionStartTime),
            startedAtMs = _sessionStartTime,
            endedAtMs = endedAtMs,
            durationMs = durationMs,
            distanceMeters = _tripDistanceMeters.toFloat(),
            maxSpeedKmh = _maxSpeed,
            avgSpeedKmh = avgSpeed,
            peakPowerKw = _ridePeakPowerKw,
            totalEnergyWh = finalEnergyWh,
            avgEfficiencyWhKm = finalAvgEfficiencyWhKm,
            trackPoints = rideTrackPoints.toList(),
            samples = rideSamples.toList()
        )
        if (forceSave || historyRecord.distanceMeters >= 50f || historyRecord.durationMs >= 60_000L) {
            val updated = listOf(historyRecord) + _rideHistory.value
            _rideHistory.value = updated.take(HISTORY_LIMIT)
            viewModelScope.launch {
                settingsRepository.saveRideHistory(_rideHistory.value)
                persistVehicleSnapshot()
            }
        }
        _lastRideLocation = null
        _rideStopCandidateAtMs = null
        lastVehicleSnapshotTripDistanceKm = 0f
        if (rideStartMode == RideStartMode.MANUAL && metrics.value.speedKmH > AUTO_RIDE_STOP_SPEED_KMH) {
            _autoRideSuppressedUntilStop.value = true
        }
        rideStartMode = null
    }

    private fun maybePersistVehicleSnapshot(now: Long) {
        if (!_isRideActive.value) return
        val tripDistanceKm = (_tripDistanceMeters / 1000.0).toFloat()
        val dueByTime = now - lastVehicleSnapshotAtMs >= 3 * 60 * 1000L
        val dueByDistance = tripDistanceKm - lastVehicleSnapshotTripDistanceKm >= 1f
        if (!dueByTime && !dueByDistance) return

        lastVehicleSnapshotAtMs = now
        lastVehicleSnapshotTripDistanceKm = tripDistanceKm
        viewModelScope.launch {
            persistVehicleSnapshot()
        }
    }

    private suspend fun persistVehicleSnapshot() {
        val tripDistanceKm = (_tripDistanceMeters / 1000.0).toFloat()
        val absoluteMileageKm = rideStartVehicleMileageKm + tripDistanceKm
        val learnedResistance = rideEnergyEstimator.currentLearnedInternalResistanceOhm()
        val observedEfficiencyWhKm = observeCurrentRideEfficiencyWhKm()
        settingsRepository.updateCurrentVehicle { profile ->
            profile.copy(
                totalMileageKm = absoluteMileageKm.coerceAtLeast(profile.totalMileageKm),
                learnedInternalResistanceOhm = if (learnedResistance > 0f) {
                    learnedResistance
                } else {
                    profile.learnedInternalResistanceOhm
                },
                learnedEfficiencyWhKm = blendLearnedEfficiencyWhKm(
                    previousLearnedWhKm = profile.learnedEfficiencyWhKm,
                    observedWhKm = observedEfficiencyWhKm,
                    tripDistanceKm = tripDistanceKm
                )
            )
        }
    }

    private fun observeCurrentRideEfficiencyWhKm(): Float {
        val tripDistanceKm = (_tripDistanceMeters / 1000.0).toFloat()
        if (tripDistanceKm < 0.25f) return 0f
        val totalEnergyWh = maxOf(_rideEnergyWh, _totalEnergyUsedWh.toFloat())
        if (totalEnergyWh <= 1f) return 0f
        return (totalEnergyWh / tripDistanceKm).toFloat()
    }

    private fun blendRangeEfficiencyWhKm(
        shortTermEfficiencyWhKm: Float,
        learnedEfficiencyWhKm: Float,
        tripDistanceKm: Float
    ): Float {
        val shortTerm = shortTermEfficiencyWhKm
            .takeIf { it in MIN_VALID_EFFICIENCY_WH_KM..MAX_VALID_EFFICIENCY_WH_KM }
        val learned = learnedEfficiencyWhKm
            .takeIf { it in MIN_VALID_EFFICIENCY_WH_KM..MAX_VALID_EFFICIENCY_WH_KM }
        return when {
            shortTerm != null && learned != null -> {
                val shortWeight = (tripDistanceKm / 4.0f).coerceIn(0.15f, 0.8f)
                (shortTerm * shortWeight) + (learned * (1f - shortWeight))
            }
            shortTerm != null -> shortTerm
            learned != null -> learned
            else -> 0f
        }
    }

    private fun blendLearnedEfficiencyWhKm(
        previousLearnedWhKm: Float,
        observedWhKm: Float,
        tripDistanceKm: Float
    ): Float {
        val observed = observedWhKm.takeIf { it in MIN_VALID_EFFICIENCY_WH_KM..MAX_VALID_EFFICIENCY_WH_KM }
            ?: return previousLearnedWhKm.coerceAtLeast(0f)
        if (tripDistanceKm < 0.25f) return previousLearnedWhKm.coerceAtLeast(0f)

        val previous = previousLearnedWhKm
            .takeIf { it in MIN_VALID_EFFICIENCY_WH_KM..MAX_VALID_EFFICIENCY_WH_KM }
        if (previous == null) return observed

        val updateWeight = (tripDistanceKm / 25f).coerceIn(0.05f, 0.35f)
        return (previous * (1f - updateWeight)) + (observed * updateWeight)
    }

    private fun stabilizeSocForDisplay(rawSocPercent: Float): Float {
        val normalized = rawSocPercent.takeIf { it in 1f..100f }
        if (normalized != null) {
            lastKnownSocPercent = normalized
            return normalized
        }
        return lastKnownSocPercent.takeIf { it in 1f..100f } ?: 0f
    }

    private fun stabilizeRangeForDisplay(
        rawRangeKm: Float,
        remainingEnergyWh: Float,
        learnedEfficiencyWhKm: Float
    ): Float {
        if (rawRangeKm > 0.1f) {
            lastKnownRangeKm = rawRangeKm
            return rawRangeKm
        }

        val startupEfficiency = learnedEfficiencyWhKm
            .takeIf { it in MIN_VALID_EFFICIENCY_WH_KM..MAX_VALID_EFFICIENCY_WH_KM }
            ?: DEFAULT_STARTUP_EFFICIENCY_WH_KM
        val startupRange = if (remainingEnergyWh > 1f) {
            (remainingEnergyWh / startupEfficiency).coerceAtLeast(0f)
        } else {
            0f
        }
        if (startupRange > 0.1f) {
            lastKnownRangeKm = startupRange
            return startupRange
        }
        return lastKnownRangeKm.takeIf { it > 0.1f } ?: 0f
    }

    private fun isRidePausedForStop(): Boolean {
        return _pendingRideStop.value != null
    }

    private fun beginPendingRideStop(reason: RideStopReason, cutoffAtMs: Long) {
        if (!_isRideActive.value) return
        if (_pendingRideStop.value != null && pendingRideStopReason == reason) return

        clearPendingRideStopState()
        pendingRideStopReason = reason
        pendingRideStopCutoffAtMs = cutoffAtMs
        pendingRideStopJob = viewModelScope.launch {
            val countdownMs = if (reason == RideStopReason.DISCONNECTED) {
                AUTO_DISCONNECT_STOP_COUNTDOWN_MS
            } else {
                AUTO_RIDE_STOP_COUNTDOWN_MS
            }
            val deadlineAtMs = System.currentTimeMillis() + countdownMs
            while (true) {
                val remainingMs = deadlineAtMs - System.currentTimeMillis()
                val remainingSeconds = ceil(remainingMs.coerceAtLeast(0L) / 1000.0).toInt()
                _pendingRideStop.value = PendingRideStopUiState(
                    title = if (reason == RideStopReason.PARKED) "检测到车辆已停稳" else "控制器连接已断开",
                    message = if (reason == RideStopReason.PARKED) {
                        "将在 ${remainingSeconds} 秒后自动结束并保存本次行程"
                    } else {
                        "若 ${remainingSeconds} 秒内未恢复连接，将自动结束并保存本次行程"
                    },
                    remainingSeconds = remainingSeconds,
                    isDisconnect = reason == RideStopReason.DISCONNECTED
                )
                if (remainingMs <= 0L) break
                delay(250)
            }
            val finishAtMs = pendingRideStopCutoffAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
            stopRide(forceSave = true, stopAtMs = finishAtMs)
            _calibrationMessage.value = if (reason == RideStopReason.PARKED) {
                "车辆已停稳，已自动结束并保存本次行程"
            } else {
                "控制器连接已断开，已自动结束并保存本次行程"
            }
        }
    }

    private fun cancelPendingRideStop(message: String? = null) {
        clearPendingRideStopState()
        message?.let { _calibrationMessage.value = it }
    }

    private fun clearPendingRideStopState() {
        pendingRideStopJob?.cancel()
        pendingRideStopJob = null
        pendingRideStopReason = null
        pendingRideStopCutoffAtMs = 0L
        _pendingRideStop.value = null
    }

    fun cancelRideStopCountdown() {
        _rideStopCandidateAtMs = null
        cancelPendingRideStop("已继续记录本次行程")
    }

    fun confirmRideStopCountdownNow() {
        val finishAtMs = pendingRideStopCutoffAtMs.takeIf { it > 0L } ?: System.currentTimeMillis()
        clearPendingRideStopState()
        stopRide(forceSave = true, stopAtMs = finishAtMs)
        _calibrationMessage.value = "已结束并保存本次行程"
    }

    fun toggleRideTracking() {
        if (_isRideActive.value) {
            stopRide(forceSave = true)
            _calibrationMessage.value = "已手动结束并保存本次行程"
        } else {
            startRide()
            _calibrationMessage.value = "已开始记录本次行程"
        }
    }

    fun startSpeedTest(label: String, targetSpeedKmh: Float): String? {
        gpsTracker.startTracking()
        if (!isRecentGpsAvailable()) {
            return "请先等待 GPS 定位稳定后再开始测试"
        }
        val gpsSpeedKmh = gpsTracker.gpsSpeed.value.coerceAtLeast(0f)
        if (gpsSpeedKmh > 5f) {
            return "请先减速到 5 km/h 以下再开始测试"
        }

        _speedTestStartedAtMs = -1L
        _speedTestLastSampleAtMs = 0L
        _speedTestDistanceMeters = 0f
        _speedTestPeakPowerKw = 0f
        _speedTestPeakBusCurrentA = 0f
        _speedTestMinVoltage = 0f
        _speedTestMaxSpeed = 0f
        _speedTestTargetLabel = label
        _speedTestTargetSpeedKmh = targetSpeedKmh
        _speedTestLastLocation = null
        speedTestTrackPoints.clear()
        _speedTestSession.value = SpeedTestSessionUiState(
            isActive = true,
            isStandby = true,
            targetLabel = label,
            targetSpeedKmh = targetSpeedKmh,
            statusText = "准备就绪 / 请起步"
        )
        return null
    }

    fun stopSpeedTest() {
        if (!_speedTestSession.value.isActive) return
        _speedTestSession.value = _speedTestSession.value.copy(
            isActive = false,
            statusText = "已手动停止"
        )
    }

    fun deleteSpeedTestRecord(id: String) {
        _speedTestHistory.value = _speedTestHistory.value.filterNot { it.id == id }
        viewModelScope.launch {
            settingsRepository.saveSpeedTestHistory(_speedTestHistory.value)
        }
    }

    fun deleteRideHistoryRecord(id: String) {
        _rideHistory.value = _rideHistory.value.filterNot { it.id == id }
        viewModelScope.launch {
            settingsRepository.saveRideHistory(_rideHistory.value)
        }
    }

    fun mergeRideHistoryRecords(ids: Set<String>): RideHistoryRecord? {
        val selected = _rideHistory.value
            .filter { it.id in ids }
            .sortedBy { it.startedAtMs }
        if (selected.size < 2) return null

        var elapsedOffsetMs = 0L
        var distanceOffsetMeters = 0f
        val mergedSamples = buildList {
            selected.forEach { record ->
                record.samples.forEach { sample ->
                    add(
                        sample.copy(
                            elapsedMs = elapsedOffsetMs + sample.elapsedMs,
                            distanceMeters = distanceOffsetMeters + sample.distanceMeters
                        )
                    )
                }
                elapsedOffsetMs += record.durationMs
                distanceOffsetMeters += record.distanceMeters
            }
        }

        val mergedTrackPoints = buildList {
            var lastPoint: RideTrackPoint? = null
            selected.forEach { record ->
                record.trackPoints.forEach { point ->
                    if (lastPoint != point) {
                        add(point)
                        lastPoint = point
                    }
                }
            }
        }

        val startedAtMs = selected.first().startedAtMs
        val endedAtMs = selected.last().endedAtMs
        val durationMs = selected.sumOf { it.durationMs }
        val distanceMeters = selected.sumOf { it.distanceMeters.toDouble() }.toFloat()
        val totalEnergyWh = selected.sumOf { it.totalEnergyWh.toDouble() }.toFloat()
        val maxSpeedKmh = selected.maxOf { it.maxSpeedKmh }
        val peakPowerKw = selected.maxOf { it.peakPowerKw }
        val avgSpeedKmh = when {
            mergedSamples.isNotEmpty() -> mergedSamples.map { it.speedKmH.toDouble() }.average().toFloat()
            durationMs > 0L -> ((distanceMeters / 1000f) / (durationMs / 3600000f)).coerceAtLeast(0f)
            else -> 0f
        }
        val avgEfficiencyWhKm = if (distanceMeters > 20f) {
            totalEnergyWh / (distanceMeters / 1000f)
        } else {
            0f
        }

        val mergedRecord = RideHistoryRecord(
            id = UUID.randomUUID().toString(),
            title = buildRideTitle(startedAtMs),
            startedAtMs = startedAtMs,
            endedAtMs = endedAtMs,
            durationMs = durationMs,
            distanceMeters = distanceMeters,
            maxSpeedKmh = maxSpeedKmh,
            avgSpeedKmh = avgSpeedKmh,
            peakPowerKw = peakPowerKw,
            totalEnergyWh = totalEnergyWh,
            avgEfficiencyWhKm = avgEfficiencyWhKm,
            trackPoints = mergedTrackPoints,
            samples = mergedSamples
        )

        val updated = listOf(mergedRecord) + _rideHistory.value.filterNot { it.id in ids }
        _rideHistory.value = updated.take(HISTORY_LIMIT)
        viewModelScope.launch {
            settingsRepository.saveRideHistory(_rideHistory.value)
        }
        _calibrationMessage.value = "已合并 ${selected.size} 条行程记录"
        return mergedRecord
    }

    fun createSpeedTestShareIntent(record: SpeedTestRecord): Intent {
        val bitmap = PosterRenderer(getApplication()).renderSpeedTest(record)
        val uri = exportBitmap(bitmap, "speedtest-${record.id}.png")
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Habe ${record.label} 加速成绩")
            putExtra(Intent.EXTRA_TEXT, "Habe ${record.label} 加速成绩：${formatDuration(record.timeMs)}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(shareIntent, "分享加速成绩")
    }

    fun createRideShareIntent(record: RideHistoryRecord): Intent {
        val bitmap = PosterRenderer(getApplication()).renderRideHistory(record)
        val uri = exportBitmap(bitmap, "ride-${record.id}.png")
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Habe ${record.title} 行程记录")
            putExtra(Intent.EXTRA_TEXT, "Habe 行程记录：${record.title}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(shareIntent, "分享行程记录")
    }

    fun saveRidePosterToGallery(record: RideHistoryRecord): String {
        val bitmap = PosterRenderer(getApplication()).renderRideHistory(record)
        val fileName = "habe-ride-${record.id}.png"
        saveBitmapToGallery(bitmap, fileName)
        return fileName
    }

    fun createRideCsvShareIntent(record: RideHistoryRecord): Intent {
        val csvUri = exportText(buildRideCsv(record), "ride-${record.id}.csv")
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, csvUri)
            putExtra(Intent.EXTRA_SUBJECT, "Habe ${record.title} 原始数据")
            putExtra(Intent.EXTRA_TEXT, "Habe 行程原始数据：${record.title}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(shareIntent, "导出行程原始数据")
    }

    private fun exportBitmap(bitmap: android.graphics.Bitmap, fileName: String): Uri {
        val exportDir = File(getApplication<Application>().cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        FileOutputStream(file).use { output ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)
        }
        return FileProvider.getUriForFile(
            getApplication(),
            "${getApplication<Application>().packageName}.fileprovider",
            file
        )
    }

    private fun saveBitmapToGallery(bitmap: android.graphics.Bitmap, fileName: String): Uri {
        val app = getApplication<Application>()
        val resolver = app.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Habe")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("无法创建相册文件")
        runCatching {
            resolver.openOutputStream(uri)?.use { output ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)
            } ?: error("无法写入相册文件")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
        }.getOrElse { throwable ->
            resolver.delete(uri, null, null)
            throw throwable
        }
        return uri
    }

    private fun exportText(content: String, fileName: String): Uri {
        val exportDir = File(getApplication<Application>().cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        file.writeText(content)
        return FileProvider.getUriForFile(
            getApplication(),
            "${getApplication<Application>().packageName}.fileprovider",
            file
        )
    }

    private fun buildRideCsv(record: RideHistoryRecord): String {
        val header = listOf(
            "elapsed_ms",
            "timestamp_ms",
            "speed_kmh",
            "power_kw",
            "voltage_v",
            "voltage_sag_v",
            "bus_current_a",
            "phase_current_a",
            "motor_temp_c",
            "controller_temp_c",
            "soc_percent",
            "estimated_range_km",
            "rpm",
            "efficiency_wh_km",
            "avg_efficiency_wh_km",
            "distance_m",
            "total_energy_wh",
            "recovered_energy_wh",
            "max_controller_temp_c",
            "latitude",
            "longitude"
        ).joinToString(",")
        val rows = buildList {
            add(header)
            record.samples.forEach { sample ->
                add(
                    listOf(
                        sample.elapsedMs.toString(),
                        sample.timestampMs.toString(),
                        csvFloat(sample.speedKmH),
                        csvFloat(sample.powerKw),
                        csvFloat(sample.voltage),
                        csvFloat(sample.voltageSag),
                        csvFloat(sample.busCurrent),
                        csvFloat(sample.phaseCurrent),
                        csvFloat(sample.motorTemp),
                        csvFloat(sample.controllerTemp),
                        csvFloat(sample.soc),
                        csvFloat(sample.estimatedRangeKm),
                        csvFloat(sample.rpm),
                        csvFloat(sample.efficiencyWhKm),
                        csvFloat(sample.avgEfficiencyWhKm),
                        csvFloat(sample.distanceMeters),
                        csvFloat(sample.totalEnergyWh),
                        csvFloat(sample.recoveredEnergyWh),
                        csvFloat(sample.maxControllerTemp),
                        sample.latitude?.toString().orEmpty(),
                        sample.longitude?.toString().orEmpty()
                    ).joinToString(",")
                )
            }
        }
        return rows.joinToString("\n")
    }

    private fun csvFloat(value: Float): String = String.format(Locale.US, "%.3f", value)

    private fun buildRideTitle(startedAtMs: Long): String {
        val date = Date(startedAtMs)
        return SimpleDateFormat("M月d日 HH:mm", Locale.getDefault()).format(date).withDisplaySpacing()
    }

    private fun formatDuration(durationMs: Long): String {
        return String.format(Locale.getDefault(), "%.2f s", durationMs / 1000f)
    }

    fun addDashboardItem(type: com.shawnrain.habe.data.MetricType) {
        val current = _dashboardItems.value.toMutableList()
        current.add(type)
        _dashboardItems.value = current
        viewModelScope.launch { settingsRepository.saveDashboardItems(current) }
    }

    fun removeDashboardItem(index: Int) {
        val current = _dashboardItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _dashboardItems.value = current
            viewModelScope.launch { settingsRepository.saveDashboardItems(current) }
        }
    }

    fun moveDashboardItem(from: Int, to: Int) {
        val current = _dashboardItems.value.toMutableList()
        if (from in current.indices && to in current.indices) {
            val item = current.removeAt(from)
            current.add(to, item)
            _dashboardItems.value = current // Immediate UI update
            
            // Persist in background
            viewModelScope.launch {
                settingsRepository.saveDashboardItems(current)
            }
        }
    }

    fun addRideOverviewItem(type: com.shawnrain.habe.data.MetricType) {
        val current = _rideOverviewItems.value.toMutableList()
        if (type in current) return
        current.add(type)
        _rideOverviewItems.value = current
        viewModelScope.launch { settingsRepository.saveRideOverviewItems(current) }
    }

    fun removeRideOverviewItem(type: com.shawnrain.habe.data.MetricType) {
        val current = _rideOverviewItems.value.toMutableList()
        if (current.size <= 1) return
        if (current.remove(type)) {
            _rideOverviewItems.value = current
            viewModelScope.launch { settingsRepository.saveRideOverviewItems(current) }
        }
    }

    fun moveRideOverviewItem(from: Int, to: Int) {
        val current = _rideOverviewItems.value.toMutableList()
        if (from in current.indices && to in current.indices) {
            val item = current.removeAt(from)
            current.add(to, item)
            _rideOverviewItems.value = current
            viewModelScope.launch { settingsRepository.saveRideOverviewItems(current) }
        }
    }

    fun saveWheelCircumference(value: Float): kotlinx.coroutines.Job = viewModelScope.launch { settingsRepository.saveWheelCircumference(value) }
    fun savePolePairs(value: Int): kotlinx.coroutines.Job = viewModelScope.launch { settingsRepository.savePolePairs(value) }
    fun saveCurrentVehicleWheelArchive(rimSize: String? = null, tireSpecLabel: String? = null): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.saveCurrentVehicleWheelArchive(rimSize = rimSize, tireSpecLabel = tireSpecLabel)
    }
    fun saveControllerBrand(value: String): kotlinx.coroutines.Job = viewModelScope.launch { settingsRepository.saveControllerBrand(value) }
    fun saveSpeedSource(source: SpeedSource): kotlinx.coroutines.Job = viewModelScope.launch { settingsRepository.saveSpeedSource(source) }
    fun saveBattDataSource(source: DataSource): kotlinx.coroutines.Job = viewModelScope.launch { settingsRepository.saveBattDataSource(source) }
    fun selectVehicle(id: String): kotlinx.coroutines.Job = viewModelScope.launch {
        autoReconnectAttemptedAddress = null
        settingsRepository.saveCurrentVehicleId(id)
        val vehicle = vehicleProfiles.value.firstOrNull { it.id == id } ?: return@launch
        _calibrationMessage.value = "已切换到 ${vehicle.name}"
    }
    fun saveVehicleProfile(profile: VehicleProfile): kotlinx.coroutines.Job = viewModelScope.launch {
        val existing = vehicleProfiles.value.any { it.id == profile.id }
        settingsRepository.upsertVehicleProfile(profile)
        _calibrationMessage.value = if (existing) {
            "已更新车辆档案：${profile.name}"
        } else {
            "已新增车辆档案：${profile.name}"
        }
    }
    fun deleteVehicleProfile(id: String): kotlinx.coroutines.Job = viewModelScope.launch {
        val target = vehicleProfiles.value.firstOrNull { it.id == id } ?: return@launch
        autoReconnectAttemptedAddress = null
        settingsRepository.deleteVehicleProfile(id)
        _calibrationMessage.value = "已删除车辆档案：${target.name}"
    }
    fun saveLogLevel(level: AppLogLevel): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.saveLogLevel(level)
        _calibrationMessage.value = "日志级别已切换为 ${level.name}"
    }
    fun saveOverlayEnabled(enabled: Boolean): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.saveOverlayEnabled(enabled)
        _calibrationMessage.value = if (enabled) {
            "后台画中画仪表已开启"
        } else {
            "后台画中画仪表已关闭"
        }
    }
    fun clearLogs() {
        AppLogger.clear()
        _calibrationMessage.value = "调试日志已清空"
    }
    fun exportLogs(): Uri {
        val uri = AppLogger.exportLogs(getApplication())
        _calibrationMessage.value = "日志已导出，可直接分享"
        return uri
    }
    fun createLogShareIntent(): Intent {
        AppLogger.i(TAG, "用户请求分享调试日志")
        return Intent.createChooser(AppLogger.createShareIntent(getApplication()), "分享调试日志")
    }

    fun startLanBackupShare(): kotlinx.coroutines.Job = viewModelScope.launch {
        if (_lanBackupShare.value.isSharing) return@launch
        val code = Random.nextInt(100000, 999999).toString()
        runCatching {
            lanBackupTransfer.start(
                scope = viewModelScope,
                code = code,
                onRestoreApplied = {
                    _lanBackupShare.value = LanBackupShareUiState()
                    _lanBackupMessage.value = "新设备恢复成功，已自动停止发送"
                }
            ) { settingsRepository.exportBackupJson() }
        }.onSuccess { offer ->
            _lanBackupShare.value = LanBackupShareUiState(
                isSharing = true,
                host = offer.host,
                port = offer.port,
                code = offer.code
            )
            _lanBackupMessage.value = "局域网迁移已开启，请在新设备输入地址与配对码"
        }.onFailure { error ->
            _lanBackupMessage.value = "开启迁移失败：${error.message ?: "未知错误"}"
        }
    }

    fun stopLanBackupShare(): kotlinx.coroutines.Job = viewModelScope.launch {
        runCatching { lanBackupTransfer.stop() }
        _lanBackupShare.value = LanBackupShareUiState()
        _lanBackupMessage.value = "局域网迁移已关闭"
    }

    fun currentLanBackupQrPayload(): String? {
        val state = _lanBackupShare.value
        if (!state.isSharing || state.host.isBlank() || state.port !in 1..65535 || state.code.isBlank()) {
            return null
        }
        return LanBackupQrPayload(
            host = state.host,
            port = state.port,
            code = state.code
        ).encodeToQrText()
    }

    fun restoreFromLanBackup(
        host: String,
        portText: String,
        code: String
    ): kotlinx.coroutines.Job = viewModelScope.launch {
        val cleanHost = host.trim()
        val cleanCode = code.trim()
        val port = portText.trim().toIntOrNull()
        if (cleanHost.isBlank() || cleanCode.isBlank() || port == null || port !in 1..65535) {
            _lanBackupMessage.value = "请输入正确的地址、端口和配对码"
            return@launch
        }
        if (_lanBackupRestoring.value) return@launch
        _lanBackupRestoring.value = true
        _lanBackupMessage.value = "二维码识别成功，正在恢复数据..."
        try {
            val count = withContext(Dispatchers.IO) {
                val payload = lanBackupTransfer.pull(cleanHost, port, cleanCode)
                val importedCount = settingsRepository.importBackupJson(payload)
                runCatching { lanBackupTransfer.notifyRestoreApplied(cleanHost, port, cleanCode) }
                importedCount
            }
            _lanBackupMessage.value = "恢复完成：已应用 $count 项资料"
            autoReconnectAttemptedAddress = null
        } catch (error: Throwable) {
            _lanBackupMessage.value = "恢复失败：${error.message ?: "网络或配对码错误"}"
        } finally {
            _lanBackupRestoring.value = false
        }
    }
    fun handleAppVisibilityChanged(isForeground: Boolean) {
        if (isForeground) {
            lastControllerDeviceAddress.value?.let { address ->
                tryAutoReconnect(address, reason = "app-foreground")
            }
        }
    }

    private fun tryAutoReconnect(address: String, reason: String) {
        if (address.isBlank()) return
        if (!isAutoReconnectAllowed(address)) return
        if (autoReconnectAttemptedAddress == address) return
        if (bleManager.connectionState.value !is ConnectionState.Disconnected) return
        val rememberedName = lastControllerDeviceName.value?.takeIf { it.isNotBlank() }
        val rememberedProtocolId = lastControllerProtocolId.value?.takeIf { it.isNotBlank() }
        val started = runCatching {
            bleManager.connect(address, rememberedName, rememberedProtocolId)
        }.onFailure { error ->
            AppLogger.e(TAG, "自动重连失败 reason=$reason address=$address", error)
        }.getOrDefault(false)
        if (started) {
            autoReconnectAttemptedAddress = address
            AppLogger.i(TAG, "自动重连已发起 reason=$reason address=$address name=${rememberedName ?: "-"} protocol=${rememberedProtocolId ?: "-"}")
        }
    }

    fun startGpsCalibration() = autoCalibrator.startSession()
    fun stopGpsCalibration() = autoCalibrator.stopSession()
    fun applyGpsCalibrationSuggestion() {
        val suggestion = gpsCalibrationState.value.suggestedCircumferenceMm ?: return
        viewModelScope.launch {
            settingsRepository.saveWheelCircumference(suggestion)
            _calibrationMessage.value = "GPS 校准已应用，轮径周长更新为 ${"%.0f".format(suggestion)}mm"
        }
        autoCalibrator.stopSession()
    }

    val connectionState: StateFlow<ConnectionState> = bleManager.connectionState
    val bmsConnectionState: StateFlow<ConnectionState> = bmsBleManager.connectionState
    val activeProtocolId: StateFlow<String?> = ProtocolParser.activeProtocolId

    // Restore filteredDevices
    private val controllerRegex = Regex("^(AI|AUSI|AP|APL|AS|LB|YUANQU|CONTRO|MDS|ZHIKE|ZK|GEKOO|GK|ZX|ZX-D|DT|DATAI|LANSHI|VOTOL|LANDE|LD|B&Q|GRT|GRET)", RegexOption.IGNORE_CASE)
    @SuppressLint("MissingPermission")
    val filteredDevices = bleManager.scannedDevices.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    ).combine(MutableStateFlow(Unit)) { list, _ ->
        list.filter { device -> device.name?.let { controllerRegex.containsMatchIn(it) } == true }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun startScan() {
        _isScanning.value = true
        _hasSearched.value = true
        bleManager.startScan()
    }

    fun stopScan() {
        _isScanning.value = false
        bleManager.stopScan()
    }
    fun connect(device: android.bluetooth.BluetoothDevice) {
        autoReconnectSuppressedUntilMs = 0L
        stopAutoReconnectWatchdog()
        viewModelScope.launch {
            settingsRepository.saveLastControllerProfile(
                address = device.address,
                name = device.name,
                protocolId = activeProtocolId.value ?: lastControllerProtocolId.value
            )
        }
        autoReconnectAttemptedAddress = device.address
        bleManager.connect(device)
    }
    fun connectRememberedController() {
        val address = lastControllerDeviceAddress.value ?: return
        autoReconnectSuppressedUntilMs = 0L
        stopAutoReconnectWatchdog()
        autoReconnectAttemptedAddress = address
        bleManager.connect(
            address = address,
            nameHint = lastControllerDeviceName.value,
            protocolIdHint = lastControllerProtocolId.value
        )
    }
    fun forgetRememberedController() {
        val rememberedAddress = lastControllerDeviceAddress.value
        suppressAutoReconnect()
        stopAutoReconnectWatchdog()
        viewModelScope.launch {
            settingsRepository.clearLastControllerDevice()
            if (connectionState.value is ConnectionState.Connected &&
                (connectionState.value as ConnectionState.Connected).device.address == rememberedAddress
            ) {
                bleManager.disconnect()
            }
        }
    }
    fun connectBms(device: android.bluetooth.BluetoothDevice): Unit = bmsBleManager.connect(device)
    fun disconnect(): Unit {
        suppressAutoReconnect()
        stopAutoReconnectWatchdog()
        bleManager.disconnect()
    }
    fun disconnectBms(): Unit = bmsBleManager.disconnect()

    private fun suppressAutoReconnect(durationMs: Long = AUTO_RECONNECT_SUPPRESS_MS) {
        autoReconnectSuppressedUntilMs = System.currentTimeMillis() + durationMs
    }

    private fun isAutoReconnectAllowed(address: String): Boolean {
        if (address.isBlank()) return false
        if (System.currentTimeMillis() < autoReconnectSuppressedUntilMs) return false
        return bleManager.connectionState.value is ConnectionState.Disconnected
    }

    private fun startAutoReconnectWatchdog(address: String, reason: String) {
        if (!isAutoReconnectAllowed(address)) return
        if (autoReconnectWatchdogJob?.isActive == true) return
        autoReconnectWatchdogJob = viewModelScope.launch {
            AppLogger.i(TAG, "自动连接守护已启动 reason=$reason address=$address")
            while (isAutoReconnectAllowed(address)) {
                if (!autoReconnectScanActive) {
                    autoReconnectScanActive = true
                    startScan()
                    AppLogger.d(TAG, "自动连接守护开启扫描窗口 address=$address")
                }
                delay(AUTO_RECONNECT_SCAN_WINDOW_MS)
                if (autoReconnectScanActive) {
                    autoReconnectScanActive = false
                    stopScan()
                }
                delay(AUTO_RECONNECT_SCAN_COOLDOWN_MS)
            }
            autoReconnectScanActive = false
        }
    }

    private fun stopAutoReconnectWatchdog() {
        autoReconnectWatchdogJob?.cancel()
        autoReconnectWatchdogJob = null
        if (autoReconnectScanActive) {
            autoReconnectScanActive = false
            stopScan()
        }
    }

    // --- Zhike Settings Actions ---
    val zhikeSettings: kotlinx.coroutines.flow.SharedFlow<ZhikeSettings> = ProtocolParser.zhikeSettings
    val latestZhikeSettings: StateFlow<ZhikeSettings?> = _latestZhikeSettings.asStateFlow()
    
    fun readZhikeSettings() {
        viewModelScope.launch {
            bleManager.handshakeZhike()
            delay(180)
            bleManager.sendZhikeAuxCommand("0102")
            delay(220)
            bleManager.sendZhikeMainCommand("AA110001")
        }
    }

    fun writeZhikeSettings(settings: ZhikeSettings, password: String): String? {
        if (activeProtocolId.value != "zhike") {
            return "当前未连接智科控制器"
        }

        val trimmedPassword = password.trim()
        val parsedPassword = trimmedPassword.toIntOrNull()
            ?: return "请输入 1-4 位数字蓝牙密码"

        if (trimmedPassword.length !in 1..4 || parsedPassword !in 0..9999) {
            return "请输入 0-9999 的蓝牙密码"
        }

        if (!settings.loadedFromController) {
            return "请先刷新并读取控制器参数"
        }

        if (settings.originalBluetoothPassword != parsedPassword) {
            return "蓝牙密码错误"
        }

        val protocol = ZhikeProtocol()
        val data = protocol.encodeSettings(settings)
        viewModelScope.launch {
            bleManager.handshakeZhike()
            delay(300)
            bleManager.writeInPackets(data)
            _calibrationMessage.value = "蓝牙密码验证通过，正在写入智科参数"
            delay(1500)
            readZhikeSettings()
        }
        return null
    }

    fun getActiveProtocolId(): String? = ProtocolParser.getActiveProtocolId()

    override fun onCleared() {
        runCatching { kotlinx.coroutines.runBlocking { lanBackupTransfer.stop() } }
        headingTracker.stop()
        gpsTracker.stopTracking()
        super.onCleared()
    }

    private fun formatHeadingLabel(degrees: Float): String {
        val normalized = ((degrees % 360f) + 360f) % 360f
        return when {
            normalized < 22.5f || normalized >= 337.5f -> "北"
            normalized < 67.5f -> "东北"
            normalized < 112.5f -> "东"
            normalized < 157.5f -> "东南"
            normalized < 202.5f -> "南"
            normalized < 247.5f -> "西南"
            normalized < 292.5f -> "西"
            else -> "西北"
        }
    }
}
