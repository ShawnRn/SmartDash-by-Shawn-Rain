package com.shawnrain.habe

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shawnrain.habe.ble.BleManager
import com.shawnrain.habe.ble.ConnectionState
import com.shawnrain.habe.ble.ProtocolParser
import com.shawnrain.habe.ble.VehicleMetrics
import com.shawnrain.habe.ble.bms.BmsParser
import com.shawnrain.habe.ble.bms.BmsMetrics
import com.shawnrain.habe.data.gps.GpsTracker
import com.shawnrain.habe.data.SpeedSource
import com.shawnrain.habe.data.DataSource
import com.shawnrain.habe.data.AutoCalibrator
import com.shawnrain.habe.data.SettingsRepository
import com.shawnrain.habe.data.RideSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

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

    val speedSource = settingsRepository.speedSource.stateIn(viewModelScope, SharingStarted.Lazily, SpeedSource.CONTROLLER)
    val battDataSource = settingsRepository.battDataSource.stateIn(viewModelScope, SharingStarted.Lazily, DataSource.CONTROLLER)
    val wheelCircumference = settingsRepository.wheelCircumference.stateIn(viewModelScope, SharingStarted.Lazily, 1800f)
    val polePairs = settingsRepository.polePairs.stateIn(viewModelScope, SharingStarted.Lazily, 50)
    val controllerBrand = settingsRepository.controllerBrand.stateIn(viewModelScope, SharingStarted.Lazily, "auto")

    val dashboardItems = settingsRepository.dashboardItems.stateIn(
        viewModelScope, SharingStarted.Lazily, 
        listOf(com.shawnrain.habe.data.MetricType.VOLTAGE, com.shawnrain.habe.data.MetricType.BUS_CURRENT, com.shawnrain.habe.data.MetricType.POWER)
    )

    val metrics: StateFlow<VehicleMetrics> = combine(
        ProtocolParser.metrics,
        BmsParser.metrics,
        gpsTracker.gpsSpeed,
        speedSource,
        battDataSource,
        _isRideActive
    ) { args: Array<Any?> ->
        val bleMetrics = args[0] as VehicleMetrics
        val bmsMetrics = args[1] as BmsMetrics
        val gpsSpeed = args[2] as Float
        val sSource = args[3] as SpeedSource
        val bSource = args[4] as DataSource
        val rideActive = args[5] as Boolean

        val (volt, curr) = if (bSource == DataSource.BMS) {
            bmsMetrics.totalVoltage to bmsMetrics.current
        } else {
            bleMetrics.voltage to bleMetrics.busCurrent
        }

        val speed = if (sSource == SpeedSource.GPS) gpsSpeed else bleMetrics.speedKmH
        val efficiency = if (speed > 5f) (volt * curr) / speed else 0f

        if (rideActive && speed > 2f) {
            if (speed > _maxSpeed) _maxSpeed = speed
            _totalSpeedSum += speed
            _speedSamples++
        }

        bleMetrics.copy(
            voltage = volt,
            busCurrent = curr,
            totalPowerW = volt * curr,
            speedKmH = speed,
            efficiencyWhKm = efficiency,
            tripDistance = _tripDistanceMeters / 1000.0,
            soc = if (bSource == DataSource.BMS) bmsMetrics.soc else bleMetrics.soc
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VehicleMetrics())

    private val _calibrationMessage = MutableStateFlow<String?>(null)
    val calibrationMessage: StateFlow<String?> = _calibrationMessage
    fun clearCalibrationMessage() { _calibrationMessage.value = null }
    val activeProtocolLabel: StateFlow<String> = ProtocolParser.activeProtocolLabel
    val bmsActiveProtocolLabel: StateFlow<String> = BmsParser.activeProtocolLabel
    val bmsMetrics: StateFlow<BmsMetrics> = BmsParser.metrics

    init {
        bleManager.rawData.onEach { ProtocolParser.parse(it) }.launchIn(viewModelScope)
        bmsBleManager.rawData.onEach { BmsParser.parse(it) }.launchIn(viewModelScope)

        // Link external metrics RPM to internal flow for AutoCalibrator
        ProtocolParser.metrics.onEach { _currentRpm.value = it.rpm }.launchIn(viewModelScope)

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

        speedSource.onEach { source ->
            if (source == SpeedSource.GPS) gpsTracker.startTracking() else gpsTracker.stopTracking()
        }.launchIn(viewModelScope)

        AutoCalibrator(
            scope = viewModelScope,
            gpsSpeedFlow = gpsTracker.gpsSpeed,
            metricsRpmFlow = _currentRpm,
            polePairsFlow = polePairs,
            currentCircumferenceFlow = wheelCircumference,
            onCalibrated = { newC ->
                saveWheelCircumference(newC)
                _calibrationMessage.value = "【自动校准】轮径已修正为 ${"%.0f".format(newC)}mm，速度精度已对齐 GPS"
            }
        ).start()
    }

    private val _lastRideSummary = MutableStateFlow<RideSession?>(null)
    val lastRideSummary: StateFlow<RideSession?> = _lastRideSummary.asStateFlow()

    fun startRide() {
        _tripDistanceMeters = 0.0
        _maxSpeed = 0f
        _totalSpeedSum = 0f
        _speedSamples = 0
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

    fun addDashboardItem(type: com.shawnrain.habe.data.MetricType) = viewModelScope.launch { 
        val current = dashboardItems.value.toMutableList()
        current.add(type)
        settingsRepository.saveDashboardItems(current)
    }

    fun removeDashboardItem(index: Int) = viewModelScope.launch {
        val current = dashboardItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            settingsRepository.saveDashboardItems(current)
        }
    }

    fun saveWheelCircumference(value: Float) = viewModelScope.launch { settingsRepository.saveWheelCircumference(value) }
    fun savePolePairs(value: Int) = viewModelScope.launch { settingsRepository.savePolePairs(value) }
    fun saveControllerBrand(value: String) = viewModelScope.launch { settingsRepository.saveControllerBrand(value) }
    fun saveSpeedSource(source: SpeedSource) = viewModelScope.launch { settingsRepository.saveSpeedSource(source) }
    fun saveBattDataSource(source: DataSource) = viewModelScope.launch { settingsRepository.saveBattDataSource(source) }

    val connectionState: StateFlow<ConnectionState> = bleManager.connectionState
    val bmsConnectionState: StateFlow<ConnectionState> = bmsBleManager.connectionState

    // Restore filteredDevices
    private val controllerRegex = Regex("^(AI|AUSI|AP|APL|AS|LB|YUANQU|CONTRO|MDS|ZHIKE|ZK|GEKOO|GK|ZX|ZX-D|DT|DATAI|LANSHI|VOTOL|LANDE|LD|B&Q|GRT|GRET)", RegexOption.IGNORE_CASE)
    @SuppressLint("MissingPermission")
    val filteredDevices = bleManager.scannedDevices.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    ).combine(MutableStateFlow(Unit)) { list, _ ->
        list.filter { device -> device.name?.let { controllerRegex.containsMatchIn(it) } == true }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun startScan() = bleManager.startScan()
    fun stopScan() = bleManager.stopScan()
    fun connect(device: android.bluetooth.BluetoothDevice) = bleManager.connect(device)
    fun connectBms(device: android.bluetooth.BluetoothDevice) = bmsBleManager.connect(device)
    fun disconnect() = bleManager.disconnect()
    fun disconnectBms() = bmsBleManager.disconnect()
}
