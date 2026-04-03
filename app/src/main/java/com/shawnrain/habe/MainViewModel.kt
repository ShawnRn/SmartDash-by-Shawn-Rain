package com.shawnrain.habe

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.location.Location
import android.net.Uri
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
import com.shawnrain.habe.data.SettingsRepository
import com.shawnrain.habe.data.RideSession
import com.shawnrain.habe.data.history.RideHistoryRecord
import com.shawnrain.habe.data.history.RideMetricSample
import com.shawnrain.habe.data.history.RideTrackPoint
import com.shawnrain.habe.debug.AppLogLevel
import com.shawnrain.habe.debug.AppLogger
import com.shawnrain.habe.overlay.OverlayHudController
import com.shawnrain.habe.overlay.OverlayHudState
import com.shawnrain.habe.overlay.OverlayHudStore
import com.shawnrain.habe.data.speedtest.SpeedTestRecord
import com.shawnrain.habe.data.speedtest.SpeedTestSessionUiState
import com.shawnrain.habe.data.speedtest.SpeedTestTrackPoint
import com.shawnrain.habe.ui.poster.PosterRenderer
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
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

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private enum class RideStartMode {
        MANUAL,
        AUTO
    }

    companion object {
        private const val TAG = "MainViewModel"
        private const val AUTO_RIDE_START_SPEED_KMH = 4f
        private const val AUTO_RIDE_STOP_SPEED_KMH = 1.2f
        private const val AUTO_RIDE_STOP_DELAY_MS = 20000L
        private const val RIDE_SAMPLE_INTERVAL_MS = 1000L
        private const val SPEED_TEST_SAMPLE_INTERVAL_MS = 120L
        private const val HISTORY_LIMIT = 30
    }

    private val bleManager = BleManager(application)
    private val bmsBleManager = BleManager(application)
    private val settingsRepository = SettingsRepository(application)
    val gpsTracker = GpsTracker(application)
    private val headingTracker = HeadingTracker(application)

    private val _currentRpm = MutableStateFlow(0f)
    private val _controllerReportedSpeed = MutableStateFlow(0f)
    private val _latestZhikeSettings = MutableStateFlow<ZhikeSettings?>(null)
    private var overlayPreferenceLoaded = false
    private var cachedOverlayEnabled = false
    private var autoReconnectAttemptedAddress: String? = null
    private var _tripDistanceMeters = 0.0
    private var _restingVoltageBaseline = 0f
    private var _lastRideLocation: Location? = null
    private var _rideStopCandidateAtMs: Long? = null
    private var _rideLastSampleAtMs = 0L
    private var _rideStartedAtMs = 0L
    private var _ridePeakPowerKw = 0f
    private var _rideEnergyWh = 0f
    private var _rideLastEnergyUpdateAtMs = 0L
    private val rideTrackPoints = mutableListOf<RideTrackPoint>()
    private val rideSamples = mutableListOf<RideMetricSample>()
    private var rideStartMode: RideStartMode? = null
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
    private var _maxSpeed = 0f
    private var _totalSpeedSum = 0f
    private var _speedSamples = 0
    private var _sessionStartTime = 0L
    private var _lastEnergyUpdateTime = 0L
    private var _totalEnergyUsedWh = 0.0

    val speedSource = settingsRepository.speedSource.stateIn(viewModelScope, SharingStarted.Lazily, SpeedSource.CONTROLLER)
    val battDataSource = settingsRepository.battDataSource.stateIn(viewModelScope, SharingStarted.Lazily, DataSource.CONTROLLER)
    val wheelCircumference = settingsRepository.wheelCircumference.stateIn(viewModelScope, SharingStarted.Lazily, 1800f)
    val polePairs = settingsRepository.polePairs.stateIn(viewModelScope, SharingStarted.Lazily, 50)
    val controllerBrand = settingsRepository.controllerBrand.stateIn(viewModelScope, SharingStarted.Lazily, "auto")
    val logLevel = settingsRepository.logLevel.stateIn(viewModelScope, SharingStarted.Lazily, AppLogLevel.INFO)
    val overlayEnabled = settingsRepository.overlayEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
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
        val voltageSag = calculateVoltageSag(
            voltage = volt,
            busCurrent = curr,
            speedKmh = speed
        )

        // Energy Tracking
        val now = System.currentTimeMillis()
        if (rideActive && _lastEnergyUpdateTime > 0) {
            val deltaH = (now - _lastEnergyUpdateTime) / 3600000.0
            _totalEnergyUsedWh += powerW * deltaH
        }
        _lastEnergyUpdateTime = now

        val distanceKm = _tripDistanceMeters / 1000.0
        val avgEff = if (distanceKm > 0.02) (_totalEnergyUsedWh / distanceKm).toFloat() else 0f

        if (rideActive && speed > 2f) {
            if (speed > _maxSpeed) _maxSpeed = speed
            _totalSpeedSum += speed
            _speedSamples++
        }

        val estimatedControllerSoc = if (bSource == DataSource.BMS) {
            bmsMetrics.soc
        } else {
            bleMetrics.soc.takeIf { it in 1f..100f } ?: estimateControllerSoc(volt)
        }

        return bleMetrics.copy(
            voltage = volt,
            busCurrent = curr,
            voltageSag = voltageSag,
            totalPowerW = powerW,
            speedKmH = speed,
            efficiencyWhKm = efficiency,
            tripDistance = distanceKm,
            soc = estimatedControllerSoc,
            avgEfficiencyWhKm = avgEff
        )
    }

    private fun calculateControllerSpeed(rpm: Float, polePairCount: Int, wheelCircumferenceMm: Float): Float {
        if (rpm <= 0f || polePairCount <= 0 || wheelCircumferenceMm <= 0f) return 0f
        val wheelRpm = rpm / polePairCount
        return (wheelRpm * wheelCircumferenceMm * 60f) / 1_000_000f
    }

    private fun calculateZhikeControllerSpeed(
        rpm: Float,
        wheelCircumferenceMm: Float,
        zhikeSettings: ZhikeSettings?
    ): Float {
        if (rpm <= 0f || wheelCircumferenceMm <= 0f) return 0f
        val coefficient = zhikeSettings?.speedCoefficient?.takeIf { it in 500..15_000 }
        val rpmScale = (coefficient?.div(100f) ?: 1f).coerceIn(0.1f, 20f)
        return ((rpm * wheelCircumferenceMm * 60f) / 1_000_000f * rpmScale).coerceIn(0f, 160f)
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
                    wheelCircumferenceMm = wheelCircumferenceMm,
                    zhikeSettings = zhikeSettings
                )
                when {
                    rpmSpeed > 0.5f -> rpmSpeed
                    bleMetrics.speedKmH > 0f -> bleMetrics.speedKmH.coerceAtLeast(0f)
                    else -> 0f
                }
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

    private fun estimateControllerSoc(voltage: Float): Float {
        if (voltage <= 0f) return 0f

        val zhikeSettings = _latestZhikeSettings.value
        if (zhikeSettings != null && zhikeSettings.overVoltage > zhikeSettings.underVoltage) {
            val minVoltage = zhikeSettings.underVoltage.toFloat()
            val maxVoltage = zhikeSettings.overVoltage.toFloat()
            return (((voltage - minVoltage) / (maxVoltage - minVoltage)) * 100f).coerceIn(0f, 100f)
        }

        return when {
            voltage >= 81f -> linearSoc(voltage, 60f, 84f)
            voltage >= 66f -> linearSoc(voltage, 56f, 72f)
            voltage >= 49f -> linearSoc(voltage, 42f, 58f)
            voltage >= 35f -> linearSoc(voltage, 31f, 44f)
            else -> 0f
        }
    }

    private fun linearSoc(voltage: Float, emptyVoltage: Float, fullVoltage: Float): Float {
        if (fullVoltage <= emptyVoltage) return 0f
        return (((voltage - emptyVoltage) / (fullVoltage - emptyVoltage)) * 100f).coerceIn(0f, 100f)
    }

    private val _calibrationMessage = MutableStateFlow<String?>(null)
    val calibrationMessage: StateFlow<String?> = _calibrationMessage
    fun clearCalibrationMessage() { _calibrationMessage.value = null }
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

        settingsRepository.logLevel.onEach { level ->
            AppLogger.setMinLevel(level)
        }.launchIn(viewModelScope)

        settingsRepository.overlayEnabled.onEach { enabled ->
            overlayPreferenceLoaded = true
            cachedOverlayEnabled = enabled
        }.launchIn(viewModelScope)

        settingsRepository.speedTestHistory.onEach { history ->
            _speedTestHistory.value = history
        }.launchIn(viewModelScope)

        settingsRepository.rideHistory.onEach { history ->
            _rideHistory.value = history
        }.launchIn(viewModelScope)

        bleManager.rawData.onEach { ProtocolParser.parse(it) }.launchIn(viewModelScope)
        bmsBleManager.rawData.onEach { BmsParser.parse(it) }.launchIn(viewModelScope)
        metrics.onEach { metrics ->
            OverlayHudStore.update(
                OverlayHudState(
                    speedKmh = metrics.speedKmH,
                    powerKw = metrics.totalPowerW / 1000f,
                    avgEfficiencyWhKm = metrics.avgEfficiencyWhKm
                )
            )
        }.launchIn(viewModelScope)

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
                viewModelScope.launch {
                    settingsRepository.saveLastControllerProfile(
                        address = state.device.address,
                        name = state.device.name ?: lastControllerDeviceName.value,
                        protocolId = activeProtocolId.value ?: lastControllerProtocolId.value
                    )
                }
            }
            if (state is ConnectionState.Disconnected && _isRideActive.value) {
                stopRide(forceSave = true)
                _calibrationMessage.value = "检测到控制器断开，已自动结束并保存本次行程"
            }
        }.launchIn(viewModelScope)

        ProtocolParser.activeProtocolId.onEach { protocolId ->
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
            }
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
        if (_isRideActive.value) {
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
        if (_rideLastEnergyUpdateAtMs > 0L) {
            val deltaHours = (now - _rideLastEnergyUpdateAtMs) / 3_600_000f
            _rideEnergyWh += (metrics.totalPowerW * deltaHours)
        }
        _rideLastEnergyUpdateAtMs = now
        _ridePeakPowerKw = maxOf(_ridePeakPowerKw, metrics.totalPowerW / 1000f)

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
                    rpm = metrics.rpm,
                    efficiencyWhKm = metrics.efficiencyWhKm,
                    distanceMeters = _tripDistanceMeters.toFloat(),
                    latitude = _lastRideLocation?.latitude,
                    longitude = _lastRideLocation?.longitude
                )
            )
            _rideLastSampleAtMs = now
        }

        if (speed <= AUTO_RIDE_STOP_SPEED_KMH) {
            val stopCandidate = _rideStopCandidateAtMs
            if (stopCandidate == null) {
                _rideStopCandidateAtMs = now
            } else if (now - stopCandidate >= AUTO_RIDE_STOP_DELAY_MS) {
                stopRide()
            }
        } else {
            _rideStopCandidateAtMs = null
        }
    }

    private fun updateSpeedTestSession(metrics: VehicleMetrics, now: Long) {
        val session = _speedTestSession.value
        if (!session.isActive) return

        if (session.isStandby) {
            if (metrics.speedKmH > 0.5f) {
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

        _speedTestMaxSpeed = maxOf(_speedTestMaxSpeed, metrics.speedKmH)
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
                currentSpeedKmh = metrics.speedKmH,
                maxSpeedKmh = _speedTestMaxSpeed,
                peakPowerKw = _speedTestPeakPowerKw,
                peakBusCurrentA = _speedTestPeakBusCurrentA,
                minVoltage = _speedTestMinVoltage,
                distanceMeters = _speedTestDistanceMeters,
                statusText = "正在冲刺 ${_speedTestTargetLabel}"
            )
            _speedTestLastSampleAtMs = now
        }

        if (metrics.speedKmH >= _speedTestTargetSpeedKmh) {
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
                currentSpeedKmh = metrics.speedKmH,
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
        _ridePeakPowerKw = 0f
        _rideEnergyWh = 0f
        _rideLastEnergyUpdateAtMs = _sessionStartTime
        _rideLastSampleAtMs = 0L
        _rideStopCandidateAtMs = null
        _lastRideLocation = null
        rideTrackPoints.clear()
        rideSamples.clear()
        rideStartMode = mode
        _autoRideSuppressedUntilStop.value = false
        _isRideActive.value = true
    }

    fun startRide() {
        startRideInternal(RideStartMode.MANUAL)
    }

    fun stopRide(forceSave: Boolean = false) {
        if (!_isRideActive.value) return
        _isRideActive.value = false
        val durationMs = System.currentTimeMillis() - _sessionStartTime
        val avgSpeed = if (_speedSamples > 0) _totalSpeedSum / _speedSamples else 0f
        val dateText = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        _lastRideSummary.value = RideSession(
            date = dateText,
            distanceKm = _tripDistanceMeters / 1000.0,
            durationMin = (durationMs / 60000).toInt(),
            maxSpeed = _maxSpeed,
            avgSpeed = avgSpeed,
            totalWh = _rideEnergyWh,
            avgEfficiency = if (_tripDistanceMeters > 20.0) (_rideEnergyWh / (_tripDistanceMeters / 1000.0)).toFloat() else 0f
        )
        val historyRecord = RideHistoryRecord(
            id = UUID.randomUUID().toString(),
            title = buildRideTitle(_sessionStartTime),
            startedAtMs = _sessionStartTime,
            endedAtMs = System.currentTimeMillis(),
            durationMs = durationMs,
            distanceMeters = _tripDistanceMeters.toFloat(),
            maxSpeedKmh = _maxSpeed,
            avgSpeedKmh = avgSpeed,
            peakPowerKw = _ridePeakPowerKw,
            totalEnergyWh = _rideEnergyWh,
            avgEfficiencyWhKm = if (_tripDistanceMeters > 20.0) (_rideEnergyWh / (_tripDistanceMeters / 1000.0)).toFloat() else 0f,
            trackPoints = rideTrackPoints.toList(),
            samples = rideSamples.toList()
        )
        if (forceSave || historyRecord.distanceMeters >= 50f || historyRecord.durationMs >= 60_000L) {
            val updated = listOf(historyRecord) + _rideHistory.value
            _rideHistory.value = updated.take(HISTORY_LIMIT)
            viewModelScope.launch {
                settingsRepository.saveRideHistory(_rideHistory.value)
            }
        }
        _lastRideLocation = null
        _rideStopCandidateAtMs = null
        if (rideStartMode == RideStartMode.MANUAL && metrics.value.speedKmH > AUTO_RIDE_STOP_SPEED_KMH) {
            _autoRideSuppressedUntilStop.value = true
        }
        rideStartMode = null
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
        if (metrics.value.speedKmH > 5f) {
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

    private fun buildRideTitle(startedAtMs: Long): String {
        val date = Date(startedAtMs)
        return SimpleDateFormat("M月d日 HH:mm", Locale.getDefault()).format(date)
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

    fun saveWheelCircumference(value: Float): kotlinx.coroutines.Job = viewModelScope.launch { settingsRepository.saveWheelCircumference(value) }
    fun savePolePairs(value: Int): kotlinx.coroutines.Job = viewModelScope.launch { settingsRepository.savePolePairs(value) }
    fun saveControllerBrand(value: String): kotlinx.coroutines.Job = viewModelScope.launch { settingsRepository.saveControllerBrand(value) }
    fun saveSpeedSource(source: SpeedSource): kotlinx.coroutines.Job = viewModelScope.launch { settingsRepository.saveSpeedSource(source) }
    fun saveBattDataSource(source: DataSource): kotlinx.coroutines.Job = viewModelScope.launch { settingsRepository.saveBattDataSource(source) }
    fun saveLogLevel(level: AppLogLevel): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.saveLogLevel(level)
        _calibrationMessage.value = "日志级别已切换为 ${level.name}"
    }
    fun saveOverlayEnabled(enabled: Boolean): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.saveOverlayEnabled(enabled)
        _calibrationMessage.value = if (enabled) {
            "后台悬浮仪表已开启"
        } else {
            OverlayHudController.stop(getApplication())
            "后台悬浮仪表已关闭"
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
    fun handleAppVisibilityChanged(isForeground: Boolean) {
        if (isForeground) {
            OverlayHudController.stop(getApplication())
            lastControllerDeviceAddress.value?.let { address ->
                tryAutoReconnect(address, reason = "app-foreground")
            }
            return
        }

        if (overlayPreferenceLoaded) {
            if (cachedOverlayEnabled) {
                OverlayHudController.start(getApplication())
            }
            return
        }

        viewModelScope.launch {
            if (settingsRepository.isOverlayEnabled()) {
                OverlayHudController.start(getApplication())
            }
        }
    }

    private fun tryAutoReconnect(address: String, reason: String) {
        if (address.isBlank()) return
        if (autoReconnectAttemptedAddress == address) return
        if (bleManager.connectionState.value !is ConnectionState.Disconnected) return
        val rememberedName = lastControllerDeviceName.value?.takeIf { it.isNotBlank() }
        val rememberedProtocolId = lastControllerProtocolId.value?.takeIf { it.isNotBlank() }
        val started = bleManager.connect(address, rememberedName, rememberedProtocolId)
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
        autoReconnectAttemptedAddress = address
        bleManager.connect(
            address = address,
            nameHint = lastControllerDeviceName.value,
            protocolIdHint = lastControllerProtocolId.value
        )
    }
    fun forgetRememberedController() {
        val rememberedAddress = lastControllerDeviceAddress.value
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
    fun disconnect(): Unit = bleManager.disconnect()
    fun disconnectBms(): Unit = bmsBleManager.disconnect()

    // --- Zhike Settings Actions ---
    val zhikeSettings: kotlinx.coroutines.flow.SharedFlow<ZhikeSettings> = ProtocolParser.zhikeSettings
    
    fun readZhikeSettings() {
        bleManager.sendZhikeMainCommand("AA110001")
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

        if (settings.bluetoothPassword != parsedPassword) {
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
