package com.shawnrain.habe

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shawnrain.habe.ble.BleManager
import com.shawnrain.habe.ble.ConnectionState
import com.shawnrain.habe.ble.ProtocolParser
import com.shawnrain.habe.ble.VehicleMetrics
import com.shawnrain.habe.ble.bms.BmsParser
import com.shawnrain.habe.ble.bms.BmsMetrics
import com.shawnrain.habe.data.gps.GpsTracker
import com.shawnrain.habe.ble.protocols.ZhikeProtocol
import com.shawnrain.habe.ble.protocols.ZhikeSettings
import com.shawnrain.habe.data.SpeedSource
import com.shawnrain.habe.data.DataSource
import com.shawnrain.habe.data.AutoCalibrator
import com.shawnrain.habe.data.GpsCalibrationState
import com.shawnrain.habe.data.SettingsRepository
import com.shawnrain.habe.data.RideSession
import com.shawnrain.habe.debug.AppLogLevel
import com.shawnrain.habe.debug.AppLogger
import com.shawnrain.habe.overlay.OverlayHudController
import com.shawnrain.habe.overlay.OverlayHudState
import com.shawnrain.habe.overlay.OverlayHudStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private val bleManager = BleManager(application)
    private val bmsBleManager = BleManager(application)
    private val settingsRepository = SettingsRepository(application)
    val gpsTracker = GpsTracker(application)

    private val _currentRpm = MutableStateFlow(0f)
    private var _tripDistanceMeters = 0.0
    private var _lastLocation: android.location.Location? = null

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
    val overlayEnabled = settingsRepository.overlayEnabled.stateIn(viewModelScope, SharingStarted.Lazily, false)
    private val autoCalibrator = AutoCalibrator(
        scope = viewModelScope,
        gpsSpeedFlow = gpsTracker.gpsSpeed,
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

    val metrics: StateFlow<VehicleMetrics> = combine(
        ProtocolParser.metrics,
        BmsParser.metrics,
        gpsTracker.gpsSpeed,
        speedSource,
        battDataSource,
        wheelCircumference,
        polePairs,
        _isRideActive
    ) { args ->
        val bleMetrics = args[0] as VehicleMetrics
        val bmsMetrics = args[1] as BmsMetrics
        val gpsSpeed = args[2] as Float
        val sSource = args[3] as SpeedSource
        val bSource = args[4] as DataSource
        val wheelMm = args[5] as Float
        val polePairCount = args[6] as Int
        val rideActive = args[7] as Boolean

        calculateVehicleMetrics(bleMetrics, bmsMetrics, gpsSpeed, sSource, bSource, wheelMm, polePairCount, rideActive)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VehicleMetrics())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val isDrivingMode: StateFlow<Boolean> = metrics
        .map { it.speedKmH }
        .distinctUntilChanged()
        .flatMapLatest { speed -> createDrivingModeFlow(speed) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private fun calculateVehicleMetrics(
        bleMetrics: VehicleMetrics,
        bmsMetrics: BmsMetrics,
        gpsSpeed: Float,
        sSource: SpeedSource,
        bSource: DataSource,
        wheelCircumferenceMm: Float,
        polePairCount: Int,
        rideActive: Boolean
    ): VehicleMetrics {
        val (volt, curr) = if (bSource == DataSource.BMS) {
            bmsMetrics.totalVoltage to bmsMetrics.current
        } else {
            bleMetrics.voltage to bleMetrics.busCurrent
        }

        val controllerSpeed = if (bleMetrics.speedKmH > 0f) {
            bleMetrics.speedKmH
        } else {
            calculateControllerSpeed(bleMetrics.rpm, polePairCount, wheelCircumferenceMm)
        }

        val speed = if (sSource == SpeedSource.GPS) gpsSpeed else controllerSpeed
        val powerW = volt * curr
        val efficiency = if (speed > 5f) powerW / speed else 0f

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

        return bleMetrics.copy(
            voltage = volt,
            busCurrent = curr,
            totalPowerW = powerW,
            speedKmH = speed,
            efficiencyWhKm = efficiency,
            tripDistance = distanceKm,
            soc = if (bSource == DataSource.BMS) bmsMetrics.soc else bleMetrics.soc,
            avgEfficiencyWhKm = avgEff
        )
    }

    private fun calculateControllerSpeed(rpm: Float, polePairCount: Int, wheelCircumferenceMm: Float): Float {
        if (rpm <= 0f || polePairCount <= 0 || wheelCircumferenceMm <= 0f) return 0f
        val wheelRpm = rpm / polePairCount
        return (wheelRpm * wheelCircumferenceMm * 60f) / 1_000_000f
    }

    private fun createDrivingModeFlow(speed: Float) = flow {
        if (speed > 5.0f) {
            delay(3000) // Wait for 3 seconds of speed > 5
            emit(true)
        } else if (speed < 0.2f) { // Consider 0.2 as stop
            emit(false)
        }
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

        // Link external metrics RPM to internal flow for AutoCalibrator
        ProtocolParser.metrics.onEach { _currentRpm.value = it.rpm }.launchIn(viewModelScope)
        autoCalibrator.startObserving()

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

        gpsTracker.location.onEach { location ->
            location?.let {
                val last = _lastLocation
                if (last != null) _tripDistanceMeters += last.distanceTo(it).toDouble()
                _lastLocation = it
            }
        }.launchIn(viewModelScope)

        combine(speedSource, gpsCalibrationState) { source, calibration ->
            source == SpeedSource.GPS || calibration.isRunning
        }.onEach { shouldTrack ->
            if (shouldTrack) gpsTracker.startTracking() else gpsTracker.stopTracking()
        }.launchIn(viewModelScope)
    }

    private val _lastRideSummary = MutableStateFlow<RideSession?>(null)
    val lastRideSummary: StateFlow<RideSession?> = _lastRideSummary.asStateFlow()

    fun startRide() {
        _tripDistanceMeters = 0.0
        _maxSpeed = 0f
        _totalSpeedSum = 0f
        _speedSamples = 0
        _totalEnergyUsedWh = 0.0
        _lastEnergyUpdateTime = System.currentTimeMillis()
        _sessionStartTime = System.currentTimeMillis()
        _isRideActive.value = true
    }

    fun stopRide() {
        if (!_isRideActive.value) return
        _isRideActive.value = false
        val durationMs = System.currentTimeMillis() - _sessionStartTime
        val avgSpeed = if (_speedSamples > 0) _totalSpeedSum / _speedSamples else 0f
        _lastRideSummary.value = RideSession(
            date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            distanceKm = _tripDistanceMeters / 1000.0,
            durationMin = (durationMs / 60000).toInt(),
            maxSpeed = _maxSpeed,
            avgSpeed = avgSpeed
        )
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
            return
        }

        if (overlayEnabled.value) {
            OverlayHudController.start(getApplication())
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
    fun connect(device: android.bluetooth.BluetoothDevice): Unit = bleManager.connect(device)
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
}
