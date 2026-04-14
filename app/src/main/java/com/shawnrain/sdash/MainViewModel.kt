package com.shawnrain.sdash

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.ContentValues
import android.content.Intent
import android.content.ClipData
import android.location.Location
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewModelScope
import com.shawnrain.sdash.ble.AutoConnectManager
import com.shawnrain.sdash.ble.AutoConnectState
import com.shawnrain.sdash.ble.BleManager
import com.shawnrain.sdash.ble.ConnectionState
import com.shawnrain.sdash.ble.ProtocolParser
import com.shawnrain.sdash.ble.VehicleMetrics
import com.shawnrain.sdash.ble.bms.BmsParser
import com.shawnrain.sdash.ble.bms.BmsMetrics
import com.shawnrain.sdash.ble.protocols.ControllerCapabilities
import com.shawnrain.sdash.data.gps.HeadingTracker
import com.shawnrain.sdash.data.gps.DirectionStabilizer
import com.shawnrain.sdash.data.gps.DirectionInput
import com.shawnrain.sdash.data.gps.DirectionSource
import com.shawnrain.sdash.data.gps.DirectionLabelFormatter
import com.shawnrain.sdash.data.gps.GradeEstimator
import com.shawnrain.sdash.data.gps.GpsTracker
import com.shawnrain.sdash.ble.protocols.WriteFailurePhase
import com.shawnrain.sdash.ble.protocols.ZhikeParameterCatalog
import com.shawnrain.sdash.ble.protocols.ZhikeProtocol
import com.shawnrain.sdash.ble.protocols.ZhikeSettings
import com.shawnrain.sdash.ble.protocols.ZhikeSettingsValidator
import com.shawnrain.sdash.ble.protocols.ZhikePostWriteVerifier
import com.shawnrain.sdash.ble.protocols.ZhikeWriteState
import com.shawnrain.sdash.ble.protocols.ZhikeProtocolEvent
import com.shawnrain.sdash.ble.protocols.syncLegacyFieldsFromWords
import com.shawnrain.sdash.data.SpeedSource
import com.shawnrain.sdash.data.DataSource
import com.shawnrain.sdash.data.AutoCalibrator
import com.shawnrain.sdash.data.GpsCalibrationState
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.RideSession
import com.shawnrain.sdash.data.VehicleProfile
import com.shawnrain.sdash.data.migration.LanBackupQrPayload
import com.shawnrain.sdash.data.migration.LanBackupTransfer
import com.shawnrain.sdash.data.sync.GoogleDriveSyncManager
import com.shawnrain.sdash.data.sync.BackupMetadata
import com.shawnrain.sdash.data.sync.BackupPreview
import com.shawnrain.sdash.data.sync.BackupRetentionPolicy
import com.shawnrain.sdash.data.sync.DriveManifestRepository
import com.shawnrain.sdash.data.sync.DriveStateMerger
import com.shawnrain.sdash.data.sync.DriveStateSerializer
import com.shawnrain.sdash.data.sync.DriveSyncCoordinator
import com.shawnrain.sdash.data.sync.PendingMutationRepository
import com.shawnrain.sdash.data.sync.SyncState
import com.shawnrain.sdash.data.sync.SyncMetadataRepository
import com.shawnrain.sdash.data.sync.SyncTriggerReason
import com.shawnrain.sdash.data.update.AppUpdateManager
import com.shawnrain.sdash.data.update.AppUpdatePackage
import com.shawnrain.sdash.data.update.AppUpdatePreferences
import com.shawnrain.sdash.data.update.AppUpdateState
import com.shawnrain.sdash.data.update.InstalledAppVersion
import com.shawnrain.sdash.data.history.RideHistoryRecord
import com.shawnrain.sdash.data.history.RideHistoryRepository
import com.shawnrain.sdash.data.history.RideHistorySummary
import com.shawnrain.sdash.data.history.RideRecordNormalizer
import com.shawnrain.sdash.data.history.RideMetricSampleSchema
import com.shawnrain.sdash.data.history.RideMetricSample
import com.shawnrain.sdash.data.history.RideTrackPoint
import com.shawnrain.sdash.data.history.computeRideSummaryStats
import com.shawnrain.sdash.data.telemetry.BatteryState
import com.shawnrain.sdash.data.telemetry.BatteryStateEstimator
import com.shawnrain.sdash.data.telemetry.RangeEstimate
import com.shawnrain.sdash.data.telemetry.RangeEstimator
import com.shawnrain.sdash.data.telemetry.TelemetryReplayRunner
import com.shawnrain.sdash.data.telemetry.TelemetrySample
import com.shawnrain.sdash.data.telemetry.SampleQuality
import com.shawnrain.sdash.data.telemetry.RideAccumulator
import com.shawnrain.sdash.data.telemetry.TelemetryStreamProcessor
import com.shawnrain.sdash.debug.AppLogLevel
import com.shawnrain.sdash.debug.AppLogger
import com.shawnrain.sdash.data.speedtest.SpeedTestRecord
import com.shawnrain.sdash.data.speedtest.SpeedTestSessionUiState
import com.shawnrain.sdash.data.speedtest.SpeedTestTrackPoint
import com.shawnrain.sdash.ui.poster.PosterRenderer
import com.shawnrain.sdash.ui.poster.PosterFactory
import com.shawnrain.sdash.ui.poster.PosterSettings
import com.shawnrain.sdash.ui.poster.PosterTemplates
import com.shawnrain.sdash.ui.poster.PosterRendererV2
import com.shawnrain.sdash.ui.text.withDisplaySpacing
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
import kotlinx.coroutines.flow.flowOn
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

data class DriveBackupPreviewUiState(
    val selectedBackup: BackupMetadata? = null,
    val preview: BackupPreview? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

private data class PendingRideStopSnapshot(
    val tripDistanceMeters: Float,
    val lastRideLocation: Location?,
    val maxSpeed: Float,
    val totalSpeedSum: Float,
    val speedSamples: Int,
    val totalEnergyUsedWh: Float,
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

@SuppressLint("MissingPermission")
private fun BluetoothDevice.safeNameOrNull(): String? {
    return runCatching { name }.getOrNull()
}

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
        private const val AUTO_RIDE_STOP_SPEED_KMH = 0.9f
        private const val AUTO_RIDE_STOP_CURRENT_A = 2.5f
        private const val AUTO_RIDE_STOP_RPM = 120f
        private const val AUTO_RIDE_STOP_DELAY_MS = 75_000L
        private const val AUTO_RIDE_STOP_COUNTDOWN_MS = 15_000L
        private const val AUTO_DISCONNECT_STOP_COUNTDOWN_MS = 4000L
        private const val RIDE_SAMPLE_INTERVAL_MS = 1000L
        private const val DISTANCE_FALLBACK_MIN_SPEED_KMH = 1.2f
        private const val DISTANCE_FALLBACK_MIN_RPM = 30f
        private const val DISTANCE_FALLBACK_MIN_CURRENT_A = 1.2f
        private const val DISTANCE_FALLBACK_MAX_DT_SECONDS = 3f
        private const val SPEED_TEST_SAMPLE_INTERVAL_MS = 120L
        private const val HISTORY_LIMIT = 30
        private const val AUTO_RECONNECT_SCAN_WINDOW_MS = 5000L
        private const val AUTO_RECONNECT_SCAN_COOLDOWN_MS = 12000L
        private const val AUTO_RECONNECT_SUPPRESS_MS = 180000L
        private const val GPS_FRESH_TIMEOUT_MS = 3000L
        private const val MIN_VALID_EFFICIENCY_WH_KM = 5.0f
        private const val MAX_VALID_EFFICIENCY_WH_KM = 120.0f
        private const val DEFAULT_STARTUP_EFFICIENCY_WH_KM = 35.0f
        private const val RANGE_REFERENCE_MIN_SPEED_KMH = 12f
        private const val RANGE_BASE_ACCESSORY_POWER_W = 34f
        private const val RANGE_AGGRESSIVE_CURRENT_A = 28f
        private const val RANGE_DISPLAY_UPDATE_STEP_KM = 0.1f
        private const val HISTORY_BACKFILL_MIN_SPEED_KMH = 0.8f
        private const val HISTORY_BACKFILL_MIN_RPM = 30f
        private const val HISTORY_BACKFILL_MIN_CURRENT_A = 1.0f
        private const val HISTORY_BACKFILL_APPLY_MIN_DISTANCE_METERS = 3f
        private const val ENERGY_INTEGRATION_MIN_POWER_W = 35f
        private const val ENERGY_INTEGRATION_MIN_CURRENT_A = 0.8f
        private const val MAX_SOC_SOURCE_DEVIATION_PERCENT = 28f
        private const val SILENT_APP_UPDATE_THROTTLE_MS = 6 * 60 * 60 * 1000L
        private const val RIDE_HISTORY_NORMALIZATION_VERSION_CURRENT = 1
    }

    private val bleManager = BleManager(application)
    private val bmsBleManager = BleManager(application)
    private val settingsRepository = SettingsRepository(application)
    private val rideHistoryRepository = RideHistoryRepository(application)
    private val lanBackupTransfer = LanBackupTransfer()
    private val driveSyncManager = GoogleDriveSyncManager(application)
    private val syncScheduler = com.shawnrain.sdash.data.sync.SyncScheduler(application, settingsRepository)
    private val syncMetadataRepository = SyncMetadataRepository(application)
    private val pendingMutationRepository = PendingMutationRepository(application)
    private val driveStateSerializer = DriveStateSerializer(application, settingsRepository, rideHistoryRepository)
    private val driveStateMerger = DriveStateMerger(application, settingsRepository)
    private val driveManifestRepository = DriveManifestRepository(driveSyncManager)
    private val driveSyncCoordinator = DriveSyncCoordinator(
        context = application,
        driveSyncManager = driveSyncManager,
        settingsRepository = settingsRepository,
        rideHistoryRepository = rideHistoryRepository,
        stateSerializer = driveStateSerializer,
        stateMerger = driveStateMerger,
        manifestRepository = driveManifestRepository,
        metadataRepository = syncMetadataRepository,
        mutationRepository = pendingMutationRepository
    )
    private val appUpdateManager = AppUpdateManager(application)
    private val updatePreferences = AppUpdatePreferences(application)
    private val rideRecordNormalizer = RideRecordNormalizer()
    private val posterFactory = PosterFactory()
    val gpsTracker = GpsTracker(application)
    private val headingTracker = HeadingTracker(application)
    private val directionStabilizer = DirectionStabilizer()
    private val directionLabelFormatter = DirectionLabelFormatter(hysteresisDeg = 12f)
    private val gradeEstimator = GradeEstimator()
    private val autoConnectManager = AutoConnectManager(
        context = application,
        bleManager = bleManager,
        scope = viewModelScope
    )

    private val _currentRpm = MutableStateFlow(0f)
    private val _controllerReportedSpeed = MutableStateFlow(0f)
    private val _latestZhikeSettings = MutableStateFlow<ZhikeSettings?>(null)
    private var autoReconnectAttemptedAddress: String? = null
    private var autoReconnectSuppressedUntilMs = 0L
    private var autoReconnectScanActive = false
    private var autoReconnectWatchdogJob: Job? = null
    private val _latestTelemetrySample = MutableStateFlow<TelemetrySample?>(null)
    val latestTelemetrySample: StateFlow<TelemetrySample?> = _latestTelemetrySample.asStateFlow()
    private var _lastRideHistorySampleAtMs = 0L
    private var _tripDistanceMeters = 0.0f
    private var _rideLastDistanceUpdateAtMs = 0L
    private var _rideLastDistanceSpeedKmh = 0.0f
    private var _rideStopCandidateAtMs: Long? = null
    private var _sessionStartTime = 0L
    private var _rideStartedAtMs = 0L
    private var rideStartVehicleMileageKm = 0.0f
    private var lastVehicleSnapshotAtMs = 0L
    private var lastVehicleSnapshotTripDistanceKm = 0.0f
    private var rideStartSocPercent: Float = Float.NaN
    private var lastRangeReferenceSpeedKmh = 30.0f
    private var _lastRideLocation: Location? = null
    private var _restingVoltageBaseline: Float = Float.NaN
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
    private var _speedTestDistanceMeters = 0.0f
    private var _speedTestPeakPowerKw = 0.0f
    private var _speedTestPeakBusCurrentA = 0.0f
    private var _speedTestMinVoltage = 0.0f
    private var _speedTestMaxSpeed = 0.0f
    private var _speedTestTargetLabel = ""
    private var _speedTestTargetSpeedKmh = 0.0f
    // Track elapsed time when we pass standard tiers during the run
    private val speedTestTierTimes = mutableMapOf<Float, Long>()
    private val speedTestTierOptions = listOf(25f, 50f, 60f, 100f)
    private val speedTestTrackPoints = mutableListOf<SpeedTestTrackPoint>()
 
    private val _isRideActive = MutableStateFlow(false)
    val isRideActive: StateFlow<Boolean> = _isRideActive.asStateFlow()
 
    private val _isZhikeController = MutableStateFlow(false)
    val isZhikeController = _isZhikeController.asStateFlow()

    private val _replayReport = MutableStateFlow<TelemetryReplayRunner.ReplayReport?>(null)
    val replayReport = _replayReport.asStateFlow()
    private val _pendingRideStop = MutableStateFlow<PendingRideStopUiState?>(null)
    val pendingRideStop: StateFlow<PendingRideStopUiState?> = _pendingRideStop.asStateFlow()
    private var estimatorVehicleId: String? = null
    private var estimatorProtocolId: String? = null
    private val batteryStateEstimator = BatteryStateEstimator()
    private val rangeEstimator = RangeEstimator()
    private val _batteryState = MutableStateFlow<BatteryState?>(null)
    private val _rangeEstimate = MutableStateFlow<RangeEstimate?>(null)
    private val telemetryStreamProcessor = TelemetryStreamProcessor()
    private val rideAccumulator = RideAccumulator()
    private val telemetryDispatcher = Dispatchers.Default
    private var lastKnownSocPercent: Float = Float.NaN
    private var lastKnownRangeKm: Float = Float.NaN
    private var lastRangeDisplayCommitDistanceKm: Float = Float.NaN

    val speedSource = settingsRepository.speedSource.stateIn(viewModelScope, SharingStarted.Lazily, SpeedSource.CONTROLLER)
    val battDataSource = settingsRepository.battDataSource.stateIn(viewModelScope, SharingStarted.Lazily, DataSource.CONTROLLER)
    val wheelCircumference = settingsRepository.wheelCircumference.stateIn(viewModelScope, SharingStarted.Lazily, 1800f)
    val polePairs = settingsRepository.polePairs.stateIn(viewModelScope, SharingStarted.Lazily, 50)
    val controllerBrand = settingsRepository.controllerBrand.stateIn(viewModelScope, SharingStarted.Lazily, "auto")
    val logLevel = settingsRepository.logLevel.stateIn(viewModelScope, SharingStarted.Lazily, AppLogLevel.INFO)
    val overlayEnabled = settingsRepository.overlayEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val driveBackupRetentionPolicy = settingsRepository.driveBackupRetentionPolicy.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        BackupRetentionPolicy.KEEP_ALL
    )
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

    private val _dashboardItems = MutableStateFlow<List<com.shawnrain.sdash.data.MetricType>>(emptyList())
    val dashboardItems: StateFlow<List<com.shawnrain.sdash.data.MetricType>> = _dashboardItems.asStateFlow()
    private val _rideOverviewItems = MutableStateFlow<List<com.shawnrain.sdash.data.MetricType>>(emptyList())
    val rideOverviewItems: StateFlow<List<com.shawnrain.sdash.data.MetricType>> = _rideOverviewItems.asStateFlow()

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
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val rideHistory: StateFlow<List<RideHistorySummary>> = settingsRepository.currentVehicleId
        .distinctUntilChanged()
        .flatMapLatest { vehicleId ->
            rideHistoryRepository.observeRideHistorySummaries(vehicleId)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val metrics: StateFlow<VehicleMetrics> = combine(
        ProtocolParser.latestMetrics,
        BmsParser.metrics,
        gpsTracker.gpsSpeed,
        gradeEstimator.currentGradePercent,
        gradeEstimator.currentAltitudeMeters,
        speedSource,
        battDataSource,
        wheelCircumference,
        polePairs,
        _isRideActive,
        ProtocolParser.activeProtocolId,
        _latestZhikeSettings,
        latestTelemetrySample
    ) { args ->
        val bleMetrics = args[0] as VehicleMetrics
        val bmsMetrics = args[1] as BmsMetrics
        val gpsSpeed = args[2] as Float
        val gradePercent = args[3] as Float
        val altitudeMeters = args[4] as Double?
        val sSource = args[5] as SpeedSource
        val bSource = args[6] as DataSource
        val wheelMm = args[7] as Float
        val polePairCount = args[8] as Int
        val rideActive = args[9] as Boolean
        val activeProtocolId = args[10] as String?
        val zhikeSettings = args[11] as ZhikeSettings?
        val sample = args[12] as TelemetrySample?

        calculateVehicleMetrics(
            bleMetrics = bleMetrics,
            bmsMetrics = bmsMetrics,
            gpsSpeed = gpsSpeed,
            gradePercent = gradePercent,
            altitudeMeters = altitudeMeters,
            sSource = sSource,
            bSource = bSource,
            wheelCircumferenceMm = wheelMm,
            polePairCount = polePairCount,
            rideActive = rideActive,
            activeProtocolId = activeProtocolId,
            zhikeSettings = zhikeSettings,
            sample = sample
        )
    }
        .flowOn(telemetryDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VehicleMetrics())

    // --- Direction stabilization: stable heading based on GPS + sensor fusion ---
    val stableDirectionDegrees: StateFlow<Float> = combine(
        metrics,
        gpsTracker.location,
        headingTracker.locationCourseDegrees,
        headingTracker.locationCourseAgeMs,
        headingTracker.locationAccuracyMeters,
        headingTracker.locationStepDistanceMeters,
        headingTracker.sensorHeadingDegrees,
        headingTracker.sensorHeadingAgeMs
    ) { args ->
        val vehicleMetrics = args[0] as VehicleMetrics
        val location = args[1] as android.location.Location?
        val gpsCourse = args[2] as Float?
        val gpsCourseAge = args[3] as Long?
        val gpsAccuracy = args[4] as Float?
        val gpsStepDist = args[5] as Float?
        val sensorHeading = args[6] as Float?
        val sensorAge = args[7] as Long?

        val nowMs = System.currentTimeMillis()
        directionStabilizer.update(
            DirectionInput(
                nowMs = nowMs,
                speedKmh = vehicleMetrics.speedKmH,
                gpsCourseDeg = gpsCourse,
                gpsCourseAgeMs = gpsCourseAge,
                gpsAccuracyM = gpsAccuracy,
                gpsStepDistanceM = gpsStepDist,
                sensorHeadingDeg = sensorHeading,
                sensorAgeMs = sensorAge
            )
        ).stableDirectionDeg
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val rideDirectionLabel: StateFlow<String> = combine(
        metrics,
        stableDirectionDegrees
    ) { vehicleMetrics, stableDirection ->
        when {
            vehicleMetrics.isReverse -> "倒车"
            else -> directionLabelFormatter.format(stableDirection)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "北")

    val isDrivingMode: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

    private fun calculateVehicleMetrics(
        bleMetrics: VehicleMetrics,
        bmsMetrics: BmsMetrics,
        gpsSpeed: Float,
        gradePercent: Float,
        altitudeMeters: Double?,
        sSource: SpeedSource,
        bSource: DataSource,
        wheelCircumferenceMm: Float,
        polePairCount: Int,
        rideActive: Boolean,
        activeProtocolId: String?,
        zhikeSettings: ZhikeSettings?,
        sample: TelemetrySample?
    ): VehicleMetrics {
        val accumulatorState = rideAccumulator.state
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
        val efficiency = if (speed > 5.0f) (powerW / speed) else 0.0f
        val currentVehicleProfile = currentVehicle.value
        val voltageSag = calculateVoltageSag(
            voltage = volt,
            busCurrent = curr,
            speedKmh = speed
        )

        val now = System.currentTimeMillis()
        val liveTripDistanceMeters = authoritativeTripDistanceMeters()
        val distanceKm = if (rideActive) {
            liveTripDistanceMeters / 1000.0f
        } else {
            0.0f
        }
        val fallbackSoc = selectFallbackSocPercent(
            voltage = volt,
            bmsSocPercent = if (bSource == DataSource.BMS) bmsMetrics.soc.takeIf { it in 1.0f..100.0f } else null,
            batterySeries = currentVehicleProfile.batterySeries,
            activeProtocolId = activeProtocolId
        )
        var batteryState = _batteryState.value ?: BatteryState()
        var rangeEstimate = _rangeEstimate.value ?: RangeEstimate()

        sample?.let { s ->
            batteryState = batteryStateEstimator.estimate(
                sample = s,
                accumulator = accumulatorState,
                batteryCapacityAh = currentVehicleProfile.batteryCapacityAh,
                batterySeries = currentVehicleProfile.batterySeries,
                usableEnergyRatio = currentVehicleProfile.learnedUsableEnergyRatio,
                fallbackSocPercent = fallbackSoc
            )
            _batteryState.value = batteryState
            
            val startupEff = currentVehicleProfile.learnedEfficiencyWhKm
                .takeIf { it in MIN_VALID_EFFICIENCY_WH_KM..MAX_VALID_EFFICIENCY_WH_KM }
                ?: DEFAULT_STARTUP_EFFICIENCY_WH_KM

            rangeEstimate = rangeEstimator.estimate(
                sample = s,
                batteryState = batteryState,
                accumulator = accumulatorState,
                batterySeries = currentVehicleProfile.batterySeries,
                batteryCapacityAh = currentVehicleProfile.batteryCapacityAh,
                usableEnergyRatio = currentVehicleProfile.learnedUsableEnergyRatio,
                startupEfficiencyWhKm = startupEff
            )
            _rangeEstimate.value = rangeEstimate
        }

        // 统一采用 Net Wh (Traction - Regen) 作为能效统计口径
        val currentTripEnergyWh = if (rideActive) accumulatorState.netBatteryEnergyWh else 0.0f
        val currentTractionEnergyWh = if (rideActive) accumulatorState.tractionEnergyWh else 0.0f

        val tripAverageNetEfficiencyWhKm = when {
            distanceKm > 0.02f && currentTripEnergyWh > 0.5f -> currentTripEnergyWh / distanceKm
            else -> 0.0f
        }
        val tripAverageTractionEfficiencyWhKm = when {
            distanceKm > 0.02f && currentTractionEnergyWh > 0.5f -> currentTractionEnergyWh / distanceKm
            else -> 0.0f
        }

        // UI 默认显示净能效 (最准，包含回收贡献)
        val avgEff = when {
            tripAverageNetEfficiencyWhKm > 0.1f -> tripAverageNetEfficiencyWhKm
            rangeEstimate.averageWindowEfficiencyWhKm > 0.1f -> rangeEstimate.averageWindowEfficiencyWhKm
            else -> 0.0f
        }
        
        val displaySocPercent = stabilizeSocForDisplay(batteryState.socPercent)
        val estimatedRangeKm = stabilizeRangeForDisplay(
            rawRangeKm = rangeEstimate.estimatedRangeKm,
            remainingEnergyWh = rangeEstimate.remainingEnergyWh,
            learnedEfficiencyWhKm = currentVehicleProfile.learnedEfficiencyWhKm,
            tripDistanceKm = distanceKm,
            rideActive = rideActive
        )

        // Statistics are now handled by RideAccumulator

        return bleMetrics.copy(
            voltage = volt,
            busCurrent = curr,
            voltageSag = voltageSag,
            totalPowerW = powerW,
            speedKmH = speed,
            controllerSpeedKmH = controllerSpeed,
            efficiencyWhKm = efficiency,
            tripDistance = distanceKm,
            soc = displaySocPercent,
            estimatedRangeKm = estimatedRangeKm,
            avgEfficiencyWhKm = avgEff,
            totalEnergyWh = if (rideActive) currentTripEnergyWh else 0.0f,
            recoveredEnergyWh = if (rideActive) accumulatorState.regenEnergyWh else 0.0f,
            peakRegenPowerKw = if (rideActive) accumulatorState.peakRegenPowerKw else 0.0f,
            maxControllerTemp = if (rideActive) accumulatorState.maxControllerTempC else bleMetrics.controllerTemp,
            gradePercent = gradePercent,
            altitudeMeters = altitudeMeters?.toFloat()?.takeIf { it.isFinite() } ?: 0f
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
        // 智科 RPM 已是轮速 RPM（已除以极对数），直接计算
        // 不做极速限制，适配不同电机/轮径组合
        return (rpm * wheelCircumferenceMm * 60f) / 1_000_000f
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
                    rpm = bleMetrics.rpm.toFloat(),
                    wheelCircumferenceMm = wheelCircumferenceMm
                )
                rpmSpeed.takeIf { it > 0.2f } ?: 0f
            }
            else -> {
                if (bleMetrics.speedKmH > 0.0f) {
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
        val controllerSocPercent = estimateControllerSoc(
            voltage = voltage,
            batterySeries = batterySeries,
            activeProtocolId = activeProtocolId
        ).takeIf { it in 1f..100f }

        val bmsSoc = bmsSocPercent?.takeIf { it in 1f..100f }
        if (bmsSoc == null) return controllerSocPercent
        if (controllerSocPercent == null) return bmsSoc

        val deviation = abs(bmsSoc - controllerSocPercent)
        return when {
            deviation <= MAX_SOC_SOURCE_DEVIATION_PERCENT -> bmsSoc
            else -> ((controllerSocPercent * 0.88f) + (bmsSoc * 0.12f)).coerceIn(0f, 100f)
        }
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
            4.15f to 96f,
            4.10f to 91f,
            4.05f to 85f,
            4.00f to 78f,
            3.95f to 71f,
            3.90f to 63f,
            3.85f to 55f,
            3.80f to 47f,
            3.75f to 39f,
            3.70f to 31f,
            3.65f to 24f,
            3.60f to 17f,
            3.55f to 12f,
            3.50f to 8f,
            3.45f to 5f,
            3.40f to 3f,
            3.30f to 1f,
            3.20f to 0f
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
            metrics.speedKmH.coerceAtLeast(0.0f)
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

    // ======== Auto-Connect State ========
    val autoConnectState: StateFlow<AutoConnectState> = autoConnectManager.state

    /**
     * User manually connects to a specific device.
     */
    fun connectToDevice(address: String, name: String? = null, protocolId: String? = null) {
        autoConnectManager.connectTo(address)
    }

    /**
     * User manually disconnects.
     */
    fun disconnectDevice() {
        autoConnectManager.stop()
        bleManager.disconnect()
    }

    // ======== Google Drive Sync State ========
    private val _driveSyncState = MutableStateFlow<SyncState>(SyncState.SignedOut)
    val driveSyncState: StateFlow<SyncState> = _driveSyncState.asStateFlow()
    private val _driveBackupPreview = MutableStateFlow(DriveBackupPreviewUiState())
    val driveBackupPreview: StateFlow<DriveBackupPreviewUiState> = _driveBackupPreview.asStateFlow()
    private val _appUpdateState = MutableStateFlow<AppUpdateState>(
        AppUpdateState.Idle(appUpdateManager.getInstalledVersion())
    )
    val appUpdateState: StateFlow<AppUpdateState> = _appUpdateState.asStateFlow()
    private val _appUpdateLastCheckedAt = MutableStateFlow(0L)
    val appUpdateLastCheckedAt: StateFlow<Long> = _appUpdateLastCheckedAt.asStateFlow()
    private val _posterSettings = MutableStateFlow(PosterSettings())
    val posterSettings: StateFlow<PosterSettings> = _posterSettings.asStateFlow()

    fun checkDriveSignInStatus(): kotlinx.coroutines.Job = viewModelScope.launch {
        if (driveSyncManager.isSignedIn()) {
            val account = driveSyncManager.getCurrentAccount()
            val previous = _driveSyncState.value as? SyncState.SignedIn
            val lastSyncTime = resolveLastDriveSyncTime(previous?.lastSyncTime)
            _driveSyncState.value = SyncState.SignedIn(
                email = account?.email ?: "Unknown",
                lastSyncTime = lastSyncTime,
                availableBackups = previous?.availableBackups.orEmpty(),
                hasRemoteUpdate = false,
                remoteDeviceName = previous?.remoteDeviceName
            )
            // Load available backups
            loadDriveBackups()
            maybeStartAutoDriveSync()
        } else {
            _driveSyncState.value = SyncState.SignedOut
        }
    }

    fun startDriveSignIn() {
        _driveSyncState.value = SyncState.SigningIn
    }

    fun failDriveSignIn(message: String) {
        _driveSyncState.value = SyncState.Error(message)
    }

    fun completeDriveSignIn() {
        viewModelScope.launch {
            try {
                val account = driveSyncManager.getCurrentAccount()
                _driveSyncState.value = SyncState.SignedIn(
                    email = account?.email ?: "Unknown"
                )
                loadDriveBackups()
                syncScheduler.onAuthSuccess()
                maybeStartAutoDriveSync(force = true)
            } catch (e: Exception) {
                _driveSyncState.value = SyncState.Error(e.message ?: "登录失败")
            }
        }
    }

    fun signOutOfDrive() {
        viewModelScope.launch {
            driveSyncManager.signOut()
            syncScheduler.onSignOut()
            autoSyncJob?.cancel()
            hasAutoSyncedThisSession = false
            _driveSyncState.value = SyncState.SignedOut
        }
    }

    fun syncDriveNow(): kotlinx.coroutines.Job = viewModelScope.launch {
        if (!driveSyncCoordinator.isV2Initialized()) {
            driveSyncCoordinator.initializeV2Sync().onFailure { error ->
                _driveSyncState.value = SyncState.Error(error.message ?: "初始化 Google Drive 同步失败")
                _driveSyncMessage.value = "初始化失败：${error.message ?: "未知错误"}"
                return@launch
            }
        }
        runDriveV2SyncNow(
            reason = SyncTriggerReason.MANUAL_SYNC,
            showState = true,
            showMessage = true
        )
    }

    fun forceUploadCurrentDeviceData(): kotlinx.coroutines.Job = viewModelScope.launch {
        if (!driveSyncCoordinator.isV2Initialized()) {
            driveSyncCoordinator.initializeV2Sync().onFailure { error ->
                _driveSyncState.value = SyncState.Error(error.message ?: "初始化 Google Drive 同步失败")
                _driveSyncMessage.value = "初始化失败：${error.message ?: "未知错误"}"
                return@launch
            }
        }
        _driveSyncState.value = SyncState.Syncing
        _driveSyncMessage.value = "正在强制上传当前设备数据…"
        syncScheduler.forceUploadCurrentDeviceData()
    }

    fun uploadToDrive(): kotlinx.coroutines.Job = viewModelScope.launch {
        _driveSyncState.value = SyncState.Syncing
        try {
            val backupJson = settingsRepository.exportBackupJson(rideHistoryRepository)
            val result = driveSyncManager.uploadBackup(backupJson)
            result.fold(
                onSuccess = {
                    val deletedCount = pruneExpiredDriveBackups()
                    val currentState = _driveSyncState.value
                    if (currentState is SyncState.SignedIn) {
                        _driveSyncState.value = currentState.copy(
                            lastSyncTime = System.currentTimeMillis()
                        )
                    }
                    _driveSyncState.value = SyncState.Synced(uploaded = true, downloaded = false)
                    _driveSyncMessage.value = if (deletedCount > 0) {
                        "备份已上传，并清理 $deletedCount 个过期历史版本"
                    } else {
                        "备份已上传到 Google Drive"
                    }
                    loadDriveBackups()
                },
                onFailure = { error ->
                    _driveSyncState.value = SyncState.Error(error.message ?: "上传失败")
                    _driveSyncMessage.value = "上传失败：${error.message}"
                }
            )
        } catch (e: Exception) {
            _driveSyncState.value = SyncState.Error(e.message ?: "上传异常")
            _driveSyncMessage.value = "上传异常：${e.message}"
        }
    }

    fun downloadFromDrive(fileId: String): kotlinx.coroutines.Job = viewModelScope.launch {
        _driveSyncState.value = SyncState.Syncing
        try {
            val result = driveSyncManager.downloadBackup(fileId)
            result.fold(
                onSuccess = { backupJson ->
                    val count = settingsRepository.importBackupJson(backupJson, rideHistoryRepository)
                    _driveSyncState.value = SyncState.Synced(uploaded = false, downloaded = true)
                    _driveSyncMessage.value = "恢复完成：已应用 $count 项资料"
                    loadDriveBackups()
                },
                onFailure = { error ->
                    _driveSyncState.value = SyncState.Error(error.message ?: "下载失败")
                    _driveSyncMessage.value = "下载失败：${error.message}"
                }
            )
        } catch (e: Exception) {
            _driveSyncState.value = SyncState.Error(e.message ?: "下载异常")
            _driveSyncMessage.value = "下载异常：${e.message}"
        }
    }

    fun loadDriveBackups() {
        viewModelScope.launch {
            val result = driveSyncManager.listBackups()
            result.fold(
                onSuccess = { backups ->
                    val currentState = _driveSyncState.value
                    if (currentState is SyncState.SignedIn) {
                        _driveSyncState.value = currentState.copy(
                            lastSyncTime = resolveLastDriveSyncTime(currentState.lastSyncTime),
                            availableBackups = backups,
                            remoteDeviceName = backups.firstOrNull()?.deviceName
                        )
                    }
                },
                onFailure = {
                    AppLogger.w(TAG, "Failed to load Drive backups: ${it.message}")
                }
            )
        }
    }

    fun deleteDriveBackup(fileId: String) {
        viewModelScope.launch {
            val result = driveSyncManager.deleteBackup(fileId)
            result.fold(
                onSuccess = { loadDriveBackups() },
                onFailure = {
                    AppLogger.w(TAG, "Failed to delete Drive backup: ${it.message}")
                }
            )
        }
    }

    fun previewDriveBackup(metadata: BackupMetadata) {
        viewModelScope.launch {
            _driveBackupPreview.value = DriveBackupPreviewUiState(
                selectedBackup = metadata,
                isLoading = true
            )
            try {
                val backupJson = driveSyncManager.downloadBackup(metadata.fileId).getOrElse { throw it }
                val preview = settingsRepository.previewBackupJson(backupJson)
                AppLogger.i(
                    TAG,
                    "Drive preview loaded file=${metadata.fileName} vehicles=${preview.vehicleCount} rides=${preview.rideCount} speedTests=${preview.speedTestCount}"
                )
                _driveBackupPreview.value = DriveBackupPreviewUiState(
                    selectedBackup = metadata,
                    preview = preview,
                    isLoading = false
                )
            } catch (e: Exception) {
                _driveBackupPreview.value = DriveBackupPreviewUiState(
                    selectedBackup = metadata,
                    isLoading = false,
                    errorMessage = e.message ?: "预览失败"
                )
            }
        }
    }

    fun clearDriveBackupPreview() {
        _driveBackupPreview.value = DriveBackupPreviewUiState()
    }

    fun restoreSingleRideFromDriveBackup(fileId: String, rideId: String): kotlinx.coroutines.Job = viewModelScope.launch {
        _driveSyncState.value = SyncState.Syncing
        try {
            val backupJson = driveSyncManager.downloadBackup(fileId).getOrElse { throw it }
            val restored = settingsRepository.restoreRideFromBackupJson(backupJson, rideId, rideHistoryRepository)
            if (restored) {
                _driveSyncState.value = SyncState.Synced(uploaded = false, downloaded = true)
                _driveSyncMessage.value = "已恢复这条行程，并同步对应车辆档案"
            } else {
                _driveSyncState.value = SyncState.Error("未在该历史版本中找到这条行程")
                _driveSyncMessage.value = "恢复失败：未找到目标行程"
            }
            loadDriveBackups()
        } catch (e: Exception) {
            _driveSyncState.value = SyncState.Error(e.message ?: "单条恢复失败")
            _driveSyncMessage.value = "恢复失败：${e.message}"
        }
    }

    private val _driveSyncMessage = MutableStateFlow<String?>(null)
    val driveSyncMessage: StateFlow<String?> = _driveSyncMessage.asStateFlow()
    fun clearDriveSyncMessage() { _driveSyncMessage.value = null }

    // ======== Auto Sync Detection ========
    private var lastKnownRemoteTimestamp: Long = 0L
    private var autoSyncJob: kotlinx.coroutines.Job? = null
    private var hasAutoSyncedThisSession: Boolean = false

    private fun currentDriveEmail(): String {
        val signedIn = _driveSyncState.value as? SyncState.SignedIn
        if (signedIn != null) return signedIn.email
        return driveSyncManager.getCurrentAccount()?.email ?: "Unknown"
    }

    private suspend fun resolveLastDriveSyncTime(fallback: Long? = null): Long? {
        val metadata = runCatching { syncMetadataRepository.getMetadata(getApplication()) }.getOrNull()
        val metadataTime = metadata
            ?.let { maxOf(it.lastPushSuccessAt, it.lastPullSuccessAt) }
            ?.takeIf { it > 0L }
        return metadataTime ?: fallback
    }

    private suspend fun pruneExpiredDriveBackups(): Int {
        val policy = driveBackupRetentionPolicy.value
        return driveSyncManager.pruneBackups(policy).getOrElse { throw it }
    }

    private fun maybeStartAutoDriveSync(force: Boolean = false) {
        if (!driveSyncManager.isSignedIn()) return
        if (!force && hasAutoSyncedThisSession) return
        if (autoSyncJob?.isActive == true) return
        autoSyncJob = viewModelScope.launch {
            if (!driveSyncCoordinator.isV2Initialized()) {
                driveSyncCoordinator.initializeV2Sync()
            }
            runDriveV2SyncNow(
                reason = SyncTriggerReason.APP_FOREGROUND,
                showState = false,
                showMessage = false
            )
        }
    }

    private suspend fun runDriveV2SyncNow(
        reason: SyncTriggerReason,
        showState: Boolean,
        showMessage: Boolean
    ) {
        if (!driveSyncManager.isSignedIn()) {
            _driveSyncState.value = SyncState.SignedOut
            if (showMessage) {
                _driveSyncMessage.value = "请先登录 Google 账号"
            }
            return
        }

        val email = currentDriveEmail()
        if (showState) {
            _driveSyncState.value = SyncState.Syncing
        }

        when (val result = driveSyncCoordinator.runReconcileNow(reason)) {
            is com.shawnrain.sdash.data.sync.SyncRunResult.Success -> {
                hasAutoSyncedThisSession = true
                loadDriveBackups()
                val backups = (driveSyncState.value as? SyncState.SignedIn)?.availableBackups.orEmpty()
                _driveSyncState.value = SyncState.SignedIn(
                    email = email,
                    lastSyncTime = resolveLastDriveSyncTime(System.currentTimeMillis()),
                    availableBackups = backups,
                    hasRemoteUpdate = false,
                    remoteDeviceName = backups.firstOrNull()?.deviceName
                )
                if (showMessage) {
                    _driveSyncMessage.value = formatDriveSyncMessage(result.notes)
                }
            }
            is com.shawnrain.sdash.data.sync.SyncRunResult.Skipped -> {
                loadDriveBackups()
                val currentState = _driveSyncState.value as? SyncState.SignedIn
                _driveSyncState.value = SyncState.SignedIn(
                    email = email,
                    lastSyncTime = resolveLastDriveSyncTime(currentState?.lastSyncTime),
                    availableBackups = currentState?.availableBackups.orEmpty(),
                    hasRemoteUpdate = false,
                    remoteDeviceName = currentState?.remoteDeviceName
                )
                if (showMessage) {
                    _driveSyncMessage.value = result.reason
                }
            }
            is com.shawnrain.sdash.data.sync.SyncRunResult.Failure -> {
                _driveSyncState.value = SyncState.Error(result.error.message ?: "Google Drive 同步失败")
                if (showMessage) {
                    _driveSyncMessage.value = "同步失败：${result.error.message}"
                }
            }
            else -> Unit
        }
    }

    private fun formatDriveSyncMessage(notes: List<String>): String {
        if (notes.isEmpty()) return "Google Drive 双向同步已完成"
        if (notes.size == 1) return notes.first()

        val addedRides = notes.count { it.startsWith("已新增行程：") }
        val addedSpeedTests = notes.count { it.startsWith("已新增测速记录：") }
        val addedProfiles = notes.count { it.startsWith("已新增车辆档案：") }
        val updatedProfiles = notes.count { it.startsWith("已更新车辆档案：") }
        val updatedSpeedTests = notes.count { it.startsWith("已更新测速记录：") }
        val deletedProfiles = notes.count { it.startsWith("已删除车辆档案：") }
        val settingsUpdated = notes.any { it == "已从云端更新设置" }
        val uploaded = notes.any { it == "本地变更已上传到云端" || it == "已将本地完整资料补传到云端" }
        val synced = notes.any { it == "已同步到最新云端数据" }
        val noChanges = notes.any { it == "云端和本地都没有新的资料变更" }

        if (noChanges) return "云端和本地都没有新的资料变更"

        val summaryParts = buildList {
            if (addedRides > 0) add("新增 $addedRides 条行程")
            if (addedSpeedTests > 0) add("新增 $addedSpeedTests 条测速")
            val profileDelta = addedProfiles + updatedProfiles + deletedProfiles
            if (profileDelta > 0) add("更新 $profileDelta 个车辆档案")
            if (updatedSpeedTests > 0) add("更新 $updatedSpeedTests 条测速")
            if (settingsUpdated) add("设置已更新")
            if (uploaded) add("已上传云端")
            if (synced) add("已同步完成")
        }

        return summaryParts.firstOrNull()?.let { first ->
            if (summaryParts.size == 1) first else first
        } ?: "Google Drive 双向同步已完成"
    }

    /**
     * Check for remote updates once (no background polling).
     * Called on app start and when user opens sync settings.
     */
    fun checkForRemoteUpdates() {
        viewModelScope.launch {
            try {
                val manifest = driveManifestRepository.fetchRemoteManifest() ?: return@launch
                val metadata = syncMetadataRepository.getMetadata(getApplication())
                val hasNewUpdate = manifest.stateVersion > metadata.lastAppliedRemoteVersion
                lastKnownRemoteTimestamp = maxOf(lastKnownRemoteTimestamp, manifest.updatedAt)
                if (hasNewUpdate) {
                    val currentState = _driveSyncState.value
                    if (currentState is SyncState.SignedIn) {
                        _driveSyncState.value = currentState.copy(
                            hasRemoteUpdate = true,
                            remoteDeviceName = manifest.updatedByDeviceName
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore background sync errors
            }
        }
    }

    /**
     * Download and MERGE remote backup (not overwrite).
     * Merges vehicle profiles and takes newer settings.
     */
    fun downloadAndMergeFromDrive(fileId: String): kotlinx.coroutines.Job = viewModelScope.launch {
        _driveSyncState.value = SyncState.Syncing
        try {
            val result = driveSyncManager.downloadBackup(fileId)
            result.fold(
                onSuccess = { remoteBackupJson ->
                    // Use merge strategy instead of full overwrite
                    val count = settingsRepository.mergeBackupJson(remoteBackupJson, rideHistoryRepository)
                    lastKnownRemoteTimestamp = System.currentTimeMillis()
                    _driveSyncState.value = SyncState.Synced(uploaded = false, downloaded = true)
                    _driveSyncMessage.value = "合并完成：已同步 $count 项资料"
                    loadDriveBackups()
                },
                onFailure = { error ->
                    _driveSyncState.value = SyncState.Error(error.message ?: "合并失败")
                    _driveSyncMessage.value = "合并失败：${error.message}"
                }
            )
        } catch (e: Exception) {
            _driveSyncState.value = SyncState.Error(e.message ?: "合并异常")
            _driveSyncMessage.value = "合并异常：${e.message}"
        }
    }

    /**
     * Clear remote update flag after user action.
     */
    fun clearRemoteUpdateFlag() {
        val currentState = _driveSyncState.value
        if (currentState is SyncState.SignedIn) {
            _driveSyncState.value = currentState.copy(
                hasRemoteUpdate = false,
                remoteDeviceName = null
            )
        }
    }

    fun saveDriveBackupRetentionPolicy(policy: BackupRetentionPolicy) {
        viewModelScope.launch {
            settingsRepository.saveDriveBackupRetentionPolicy(policy)
            syncScheduler.onSettingsChanged()
            if (driveSyncManager.isSignedIn()) {
                runCatching { pruneExpiredDriveBackups() }
                    .onSuccess { deletedCount ->
                        loadDriveBackups()
                        _driveSyncMessage.value = if (policy == BackupRetentionPolicy.KEEP_ALL) {
                            "历史版本自动清理已关闭"
                        } else if (deletedCount > 0) {
                            "已应用${policy.label}保留规则，并清理 $deletedCount 个过期版本"
                        } else {
                            "已应用${policy.label}保留规则"
                        }
                    }
                    .onFailure {
                        _driveSyncMessage.value = "保留规则已保存，但清理失败：${it.message}"
                    }
            }
        }
    }

    fun checkForAppUpdate(silent: Boolean = false): kotlinx.coroutines.Job = viewModelScope.launch {
        if (silent) {
            val lastCheckedAt = updatePreferences.lastCheckedAt.first()
            if (lastCheckedAt > 0L && System.currentTimeMillis() - lastCheckedAt < SILENT_APP_UPDATE_THROTTLE_MS) {
                _appUpdateLastCheckedAt.value = lastCheckedAt
                return@launch
            }
        }
        val currentVersion = appUpdateManager.getInstalledVersion()
        _appUpdateState.value = AppUpdateState.Checking(currentVersion)
        
        val result = appUpdateManager.checkForUpdate(honorIgnoredTag = silent)
        result.fold(
            onSuccess = { pkg ->
                _appUpdateLastCheckedAt.value = updatePreferences.lastCheckedAt.first()
                _appUpdateState.value = if (pkg != null) {
                    val downloadedPath = appUpdateManager.findDownloadedApkPath(pkg.tag)
                    if (downloadedPath != null) {
                        AppUpdateState.Downloaded(currentVersion, pkg, downloadedPath)
                    } else {
                        AppUpdateState.Available(currentVersion, pkg)
                    }
                } else {
                    AppUpdateState.UpToDate(currentVersion, System.currentTimeMillis())
                }
                
                AppLogger.i(
                    TAG,
                    if (pkg == null) {
                        "App update check complete: already latest ${currentVersion.displayName}"
                    } else {
                        "App update available: ${pkg.versionName} build=${pkg.versionCode} channel=${pkg.channel}"
                    }
                )
            },
            onFailure = { error ->
                _appUpdateLastCheckedAt.value = updatePreferences.lastCheckedAt.first()
                _appUpdateState.value = if (silent) {
                    AppUpdateState.Idle(currentVersion)
                } else {
                    AppUpdateState.Error(
                        currentVersion = currentVersion,
                        message = error.message ?: "检查更新失败",
                        stage = "checking"
                    )
                }
                AppLogger.w(TAG, "App update check failed: ${error.message}")
            }
        )
    }

    fun downloadAppUpdate(pkg: AppUpdatePackage? = null): kotlinx.coroutines.Job? {
        val targetPkg = pkg 
            ?: (appUpdateState.value as? AppUpdateState.Available)?.pkg
            ?: (appUpdateState.value as? AppUpdateState.Error)?.let { if (it.stage == "downloading") (appUpdateState.value as? AppUpdateState.Available)?.pkg else null }
            ?: return null
            
        return viewModelScope.launch {
            val currentVersion = appUpdateManager.getInstalledVersion()
            _appUpdateState.value = AppUpdateState.Downloading(currentVersion, targetPkg, 0f)
            
            val result = appUpdateManager.downloadReleaseApk(targetPkg) { progress ->
                _appUpdateState.value = AppUpdateState.Downloading(currentVersion, targetPkg, progress)
            }
            result.fold(
                onSuccess = { file ->
                    _appUpdateState.value = AppUpdateState.Downloaded(
                        currentVersion = currentVersion,
                        pkg = targetPkg,
                        apkPath = file.absolutePath
                    )
                    AppLogger.i(TAG, "App update downloaded path=${file.absolutePath}")
                },
                onFailure = { error ->
                    _appUpdateState.value = AppUpdateState.Error(
                        currentVersion = currentVersion,
                        message = error.message ?: "下载更新失败",
                        stage = "downloading"
                    )
                    AppLogger.w(TAG, "App update download failed: ${error.message}")
                }
            )
        }
    }

    fun canInstallDownloadedAppUpdate(): Boolean = appUpdateManager.canRequestPackageInstalls()

    fun createInstallDownloadedAppUpdateIntent(): Intent? {
        val state = appUpdateState.value
        val pkg = when (state) {
            is AppUpdateState.Downloaded -> state.pkg
            is AppUpdateState.InstallPermissionRequired -> state.pkg
            else -> null
        } ?: return null
        val apkPath = when (state) {
            is AppUpdateState.Downloaded -> state.apkPath
            is AppUpdateState.InstallPermissionRequired -> state.apkPath
            else -> null
        } ?: return null
        val apkFile = File(apkPath).takeIf { it.exists() } ?: return null
        return runCatching {
            viewModelScope.launch {
                appUpdateManager.markInstallLaunched(pkg.tag)
            }
            appUpdateManager.createInstallIntent(apkFile)
        }.getOrElse { error ->
            AppLogger.e(TAG, "Failed to create install intent", error)
            viewModelScope.launch {
                _appUpdateState.value = AppUpdateState.Error(
                    currentVersion = appUpdateManager.getInstalledVersion(),
                    message = "无法打开系统安装器，请重新下载更新",
                    stage = "install"
                )
            }
            null
        }
    }

    fun createManageUnknownSourcesIntent(): Intent =
        appUpdateManager.createManageUnknownSourcesIntent()

    fun clearAppUpdateError() {
        val current = _appUpdateState.value
        if (current is AppUpdateState.Error) {
            _appUpdateState.value = AppUpdateState.Idle(current.currentVersion)
        }
    }

    fun ignoreCurrentAppUpdate(): Job? {
        val pkg = when (val state = _appUpdateState.value) {
            is AppUpdateState.Available -> state.pkg
            is AppUpdateState.Downloading -> state.pkg
            is AppUpdateState.Downloaded -> state.pkg
            is AppUpdateState.InstallPermissionRequired -> state.pkg
            else -> null
        } ?: return null
        return viewModelScope.launch {
            appUpdateManager.ignoreRelease(pkg.tag)
            _appUpdateState.value = AppUpdateState.UpToDate(
                currentVersion = appUpdateManager.getInstalledVersion(),
                checkedAt = System.currentTimeMillis()
            )
        }
    }

    fun savePosterSettings(settings: PosterSettings) {
        if (_posterSettings.value == settings) return
        _posterSettings.value = settings
        viewModelScope.launch {
            settingsRepository.savePosterSettings(settings)
            syncScheduler.onSettingsChanged()
        }
    }

    val gpsCalibrationState: StateFlow<GpsCalibrationState> = autoCalibrator.state
    val activeProtocolLabel: StateFlow<String> = ProtocolParser.activeProtocolLabel
    val bmsActiveProtocolLabel: StateFlow<String> = BmsParser.activeProtocolLabel
    val bmsMetrics: StateFlow<BmsMetrics> = BmsParser.metrics

    init {
        // Load known controllers from settings and start auto-connect
        viewModelScope.launch {
            val lastAddress = lastControllerDeviceAddress.value
            val lastName = lastControllerDeviceName.value
            val lastProtocol = lastControllerProtocolId.value

            if (!lastAddress.isNullOrBlank()) {
                autoConnectManager.registerController(
                    address = lastAddress,
                    name = lastName,
                    protocolId = lastProtocol,
                    lastConnectedAt = System.currentTimeMillis()
                )
                AppLogger.i(TAG, "启动自动连接：$lastName ($lastAddress)")
            }

            // Start auto-connect (will immediately try to connect to last known device)
            autoConnectManager.start()
        }

        // Wire BLE connection state to auto-connect manager
        bleManager.connectionState.onEach { state ->
            when (state) {
                is ConnectionState.Connected -> {
                    autoConnectManager.onConnected(
                        address = state.device.address,
                        name = safeBluetoothDeviceName(state.device)
                    )
                }
                is ConnectionState.Disconnected -> {
                    val lastAddress = lastControllerDeviceAddress.value
                    if (lastAddress != null) {
                        autoConnectManager.onDisconnected(lastAddress)
                    }
                }
                is ConnectionState.Error -> {
                    val lastAddress = lastControllerDeviceAddress.value
                    if (lastAddress != null) {
                        autoConnectManager.onDisconnected(lastAddress)
                    }
                }
                else -> {}
            }
        }.launchIn(viewModelScope)

        // Check for remote updates on app start (one-time, no polling)
        settingsRepository.currentVehicleId.onEach { _ ->
            if (driveSyncManager.isSignedIn()) {
                checkDriveSignInStatus()
            }
        }.launchIn(viewModelScope)

        settingsRepository.dashboardItems.onEach { items ->
            if (_dashboardItems.value != items) {
                _dashboardItems.value = items
                AppLogger.i(
                    TAG,
                    "仪表排列已从仓库刷新：${items.joinToString(",") { it.name }}"
                )
            }
        }.launchIn(viewModelScope)

        settingsRepository.rideOverviewItems.onEach { items ->
            if (_rideOverviewItems.value != items) {
                _rideOverviewItems.value = items
                AppLogger.i(
                    TAG,
                    "行程概览卡片已从仓库刷新：${items.joinToString(",") { it.name }}"
                )
            }
        }.launchIn(viewModelScope)

        settingsRepository.posterSettings.onEach { settings ->
            _posterSettings.value = settings
        }.launchIn(viewModelScope)

        updatePreferences.lastCheckedAt.onEach { checkedAt ->
            _appUpdateLastCheckedAt.value = checkedAt
        }.launchIn(viewModelScope)

        settingsRepository.logLevel.onEach { level ->
            AppLogger.setMinLevel(level)
        }.launchIn(viewModelScope)

        settingsRepository.speedTestHistory.onEach { history ->
            _speedTestHistory.value = history
        }.launchIn(viewModelScope)

        viewModelScope.launch(Dispatchers.IO) {
            migrateLegacyRideHistoryIfNeeded()
        }

        currentVehicle.onEach { profile ->
            if (estimatorVehicleId != profile.id) {
                estimatorVehicleId = profile.id
                batteryStateEstimator.reset(profile.learnedInternalResistanceOhm)
                rangeEstimator.reset()
                _restingVoltageBaseline = 0.0f
                lastKnownSocPercent = Float.NaN
                lastKnownRangeKm = Float.NaN
                lastRangeDisplayCommitDistanceKm = Float.NaN
            }
        }.launchIn(viewModelScope)

        bleManager.rawData.onEach { ProtocolParser.parse(it) }.launchIn(viewModelScope)
        bmsBleManager.rawData.onEach { BmsParser.parse(it) }.launchIn(viewModelScope)
        ProtocolParser.zhikeSettings.onEach { _latestZhikeSettings.value = it }.launchIn(viewModelScope)
        viewModelScope.launch(telemetryDispatcher) {
            ProtocolParser.metrics.collect { raw ->
                val resolved = raw.copy(
                    controllerSpeedKmH = resolveControllerSpeed(
                        bleMetrics = raw,
                        activeProtocolId = ProtocolParser.activeProtocolId.value,
                        wheelCircumferenceMm = wheelCircumference.value,
                        polePairCount = polePairs.value,
                        zhikeSettings = _latestZhikeSettings.value
                    )
                )
                _currentRpm.value = resolved.rpm.toFloat()
                val sample = telemetryStreamProcessor.process(resolved)
                _latestTelemetrySample.value = sample
                if (_isRideActive.value && !isRidePausedForStop()) {
                    rideAccumulator.accumulate(sample)
                }
            }
        }
        combine(
            ProtocolParser.latestMetrics,
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

        // App lifecycle: trigger sync pull on foreground
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                // App came to foreground: pull any remote updates
                syncScheduler.onAppForeground()
            }
        })

        ProtocolParser.autoConfigUpdates.onEach { (key, value) ->
            if (key == "polePairs") {
                savePolePairs(value)
                _calibrationMessage.value = "已自动从控制器读取极对数: $value"
            }
        }.launchIn(viewModelScope)

        bmsBleManager.connectionState.onEach { state ->
            if (state is ConnectionState.Connected) {
                BmsParser.selectProtocol(state.device.safeNameOrNull() ?: "", emptyList())
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
                        name = state.device.safeNameOrNull() ?: lastControllerDeviceName.value,
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
                batteryStateEstimator.reset(currentVehicle.value.learnedInternalResistanceOhm)
                rangeEstimator.reset()
                _restingVoltageBaseline = 0.0f
                lastRangeDisplayCommitDistanceKm = Float.NaN
            }
            val connected = bleManager.connectionState.value as? ConnectionState.Connected ?: return@onEach
            if (protocolId.isNullOrBlank()) return@onEach
            viewModelScope.launch {
                settingsRepository.saveLastControllerProfile(
                    address = connected.device.address,
                    name = connected.device.safeNameOrNull() ?: lastControllerDeviceName.value,
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
        gradeEstimator.update(location)

        if (_isRideActive.value && !isRidePausedForStop()) {
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
        val speed = metrics.speedKmH.toFloat()
        val distanceSpeedKmh = metrics.controllerSpeedKmH.toFloat().coerceAtLeast(0.0f)
        if (_rideLastDistanceUpdateAtMs <= 0L) {
            _rideLastDistanceUpdateAtMs = now
            _rideLastDistanceSpeedKmh = distanceSpeedKmh
        } else {
            val dtSeconds = (now - _rideLastDistanceUpdateAtMs).coerceAtLeast(0L).toFloat() / 1000.0f
            if (dtSeconds > 0.0f && dtSeconds <= DISTANCE_FALLBACK_MAX_DT_SECONDS) {
                val avgDistanceSpeedKmh = (_rideLastDistanceSpeedKmh + distanceSpeedKmh) * 0.5f
                if (avgDistanceSpeedKmh >= DISTANCE_FALLBACK_MIN_SPEED_KMH) {
                    _tripDistanceMeters += (avgDistanceSpeedKmh / 3.6f) * dtSeconds
                }
            }
            _rideLastDistanceUpdateAtMs = now
            _rideLastDistanceSpeedKmh = distanceSpeedKmh
        }

        if (_pendingRideStop.value != null) {
            if (speed >= AUTO_RIDE_START_SPEED_KMH) {
                _rideStopCandidateAtMs = null
                cancelPendingRideStop("车辆恢复移动，继续记录本次行程")
            } else {
                return
            }
        }

        val nearParkedState = speed <= AUTO_RIDE_STOP_SPEED_KMH &&
            abs(metrics.busCurrent) <= AUTO_RIDE_STOP_CURRENT_A &&
            abs(metrics.rpm) <= AUTO_RIDE_STOP_RPM &&
            !metrics.isReverse
        if (nearParkedState) {
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

        if (now - _lastRideHistorySampleAtMs >= RIDE_SAMPLE_INTERVAL_MS) {
            recordRideSample(metrics, now)
            _lastRideHistorySampleAtMs = now
        }

        maybePersistVehicleSnapshot(now)
    }

    private fun recordRideSample(metrics: VehicleMetrics, timestampMs: Long) {
        val accState = rideAccumulator.state
        rideSamples.add(
            RideMetricSample(
                elapsedMs = timestampMs - _sessionStartTime,
                timestampMs = timestampMs,
                speedKmH = metrics.speedKmH,
                powerKw = metrics.totalPowerW / 1000.0f,
                voltage = metrics.voltage,
                voltageSag = metrics.voltageSag,
                busCurrent = metrics.busCurrent,
                phaseCurrent = metrics.phaseCurrent,
                controllerTemp = metrics.controllerTemp,
                soc = metrics.soc,
                estimatedRangeKm = metrics.estimatedRangeKm,
                rpm = metrics.rpm,
                efficiencyWhKm = metrics.efficiencyWhKm,
                avgEfficiencyWhKm = metrics.avgEfficiencyWhKm,
                avgNetEfficiencyWhKm = when {
                    accState.totalDistanceMeters > 20.0 -> (accState.netBatteryEnergyWh / (accState.totalDistanceMeters / 1000.0)).toFloat()
                    else -> 0.0f
                },
                avgTractionEfficiencyWhKm = when {
                    accState.totalDistanceMeters > 20.0 -> (accState.tractionEnergyWh / (accState.totalDistanceMeters / 1000.0)).toFloat()
                    else -> 0.0f
                },
                distanceMeters = accState.totalDistanceMeters,
                totalEnergyWh = accState.netBatteryEnergyWh,
                tractionEnergyWh = accState.tractionEnergyWh,
                regenEnergyWh = accState.regenEnergyWh,
                recoveredEnergyWh = accState.regenEnergyWh,
                maxControllerTemp = accState.maxControllerTempC,
                gradePercent = metrics.gradePercent,
                altitudeMeters = gradeEstimator.currentAltitudeMeters.value,
                latitude = _lastRideLocation?.latitude,
                longitude = _lastRideLocation?.longitude
            )
        )
    }

    fun replayRide(record: RideHistoryRecord) {
        val currentProfile = currentVehicle.value
        val runner = TelemetryReplayRunner()
        
        viewModelScope.launch(Dispatchers.Default) {
            val report = runner.replay(record, currentProfile)
            _replayReport.value = report
            AppLogger.i("Replay", "Replay finished. Distance: ${report.totalDistanceMeters}m, Net Wh: ${report.netEnergyWh}, Avg Eff: ${report.avgEfficiencyWhKm}")
        }
    }
    
    fun clearReplayReport() {
        _replayReport.value = null
    }

    private fun authoritativeTripDistanceMeters(): Float {
        return rideAccumulator.state.totalDistanceMeters
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
        _speedTestPeakPowerKw = maxOf(_speedTestPeakPowerKw, (metrics.totalPowerW / 1000.0).toFloat())
        _speedTestPeakBusCurrentA = maxOf(_speedTestPeakBusCurrentA, metrics.busCurrent.toFloat())
        _speedTestMinVoltage = if (_speedTestMinVoltage <= 0.0f) {
            metrics.voltage.toFloat()
        } else {
            minOf(_speedTestMinVoltage, metrics.voltage.toFloat())
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
            val elapsedMs = now - _speedTestStartedAtMs
            // Record time for the main target
            speedTestTierTimes[_speedTestTargetSpeedKmh] = elapsedMs

            // Save the main record
            val mainRecord = createSpeedTestRecord(
                _speedTestTargetLabel, _speedTestTargetSpeedKmh.toFloat(), elapsedMs
            )
            
            // Check for other tiers reached during this run
            val updatedHistory = _speedTestHistory.value.toMutableList()
            
            // Add or update records for all reached tiers
            speedTestTierTimes.forEach { (tier, time) ->
                // Check if we already have a record for this tier
                val existingIndex = updatedHistory.indexOfFirst { it.targetSpeedKmh == tier }
                if (existingIndex != -1) {
                    // Update if this run is faster
                    val existing = updatedHistory[existingIndex]
                    if (time < existing.timeMs) {
                        updatedHistory[existingIndex] = createSpeedTestRecord(
                            "0-${tier.toInt()}", tier.toFloat(), time
                        )
                    }
                } else {
                    // Add new tier record
                    updatedHistory.add(createSpeedTestRecord("0-${tier.toInt()}", tier.toFloat(), time))
                }
            }
            
            _speedTestHistory.value = updatedHistory.sortedByDescending { it.timestampMs }.take(HISTORY_LIMIT)
            viewModelScope.launch {
                settingsRepository.saveSpeedTestHistory(_speedTestHistory.value)
                // Auto-sync speed test to Google Drive (V2 sync engine)
                _speedTestHistory.value.firstOrNull()?.let { syncScheduler.onSpeedTestSaved(it.id) }
            }
            _speedTestSession.value = _speedTestSession.value.copy(
                isActive = false,
                elapsedMs = mainRecord.timeMs,
                currentSpeedKmh = speedKmh,
                maxSpeedKmh = _speedTestMaxSpeed,
                peakPowerKw = _speedTestPeakPowerKw,
                peakBusCurrentA = _speedTestPeakBusCurrentA,
                minVoltage = _speedTestMinVoltage,
                distanceMeters = _speedTestDistanceMeters,
                statusText = "完成 ${mainRecord.label}，成绩 ${formatDuration(mainRecord.timeMs)}"
            )
        } else {
            // Not finished yet, check if we passed any lower tiers to track them
            val currentElapsed = now - _speedTestStartedAtMs
            speedTestTierOptions.forEach { tier ->
                if (tier < _speedTestTargetSpeedKmh && speedKmh >= tier) {
                    if (speedTestTierTimes[tier] == null) {
                        speedTestTierTimes[tier] = currentElapsed
                    }
                }
            }
        }
    }

    private fun createSpeedTestRecord(
        label: String,
        targetSpeedKmh: Float,
        timeMs: Long
    ): SpeedTestRecord {
        return SpeedTestRecord(
            id = UUID.randomUUID().toString(),
            label = label,
            targetSpeedKmh = targetSpeedKmh,
            timeMs = timeMs,
            timestampMs = System.currentTimeMillis(),
            maxSpeedKmh = _speedTestMaxSpeed,
            peakPowerKw = _speedTestPeakPowerKw,
            peakBusCurrentA = _speedTestPeakBusCurrentA,
            minVoltage = _speedTestMinVoltage,
            distanceMeters = _speedTestDistanceMeters,
            trackPoints = speedTestTrackPoints.toList()
        )
    }

    private fun startRideInternal(mode: RideStartMode) {
        _tripDistanceMeters = 0.0f
        _sessionStartTime = System.currentTimeMillis()
        _rideStartedAtMs = _sessionStartTime
        rideStartVehicleMileageKm = currentVehicle.value.totalMileageKm
        lastVehicleSnapshotAtMs = _sessionStartTime
        lastVehicleSnapshotTripDistanceKm = 0.0f
        _rideStopCandidateAtMs = null
        clearPendingRideStopState()
        rideStartSocPercent = metrics.value.soc.toFloat().takeIf { it in 1.0f..100.0f } ?: Float.NaN
        lastRangeReferenceSpeedKmh = 30.0f
        lastRangeDisplayCommitDistanceKm = 0.0f
        rideTrackPoints.clear()
        rideSamples.clear()
        rideStartMode = mode
        _autoRideSuppressedUntilStop.value = false
        _lastRideHistorySampleAtMs = 0L
        
        batteryStateEstimator.reset(currentVehicle.value.learnedInternalResistanceOhm)
        rangeEstimator.reset()
        rideAccumulator.reset()
        telemetryStreamProcessor.reset()
        
        _isRideActive.value = true
    }

    fun startRide() {
        startRideInternal(RideStartMode.MANUAL)
    }

    fun stopRide(forceSave: Boolean = false, stopAtMs: Long = System.currentTimeMillis()): Boolean {
        if (!_isRideActive.value) return false
        clearPendingRideStopState()
        val endedAtMs = stopAtMs.coerceAtLeast(_sessionStartTime)
        val finalMetrics = metrics.value
        val accState = rideAccumulator.state
        _isRideActive.value = false
        
        val shouldAppendFinalSample = rideSamples.isEmpty() ||
            rideSamples.last().timestampMs < endedAtMs
        if (shouldAppendFinalSample) {
            recordRideSample(finalMetrics, endedAtMs)
        }
        
        val finalTripDistanceMeters = accState.totalDistanceMeters
        val durationMs = endedAtMs - _sessionStartTime
        val avgSpeed = if (accState.sampleCount > 0) (accState.totalSpeedSum / accState.sampleCount).toFloat() else 0.0f
        val finalEnergyWh = accState.tractionEnergyWh

        val finalAvgNetEfficiency = if (finalTripDistanceMeters > 20.0) {
            (accState.netBatteryEnergyWh / (finalTripDistanceMeters / 1000.0)).toFloat()
        } else {
            0.0f
        }
        val finalAvgTractionEfficiency = if (finalTripDistanceMeters > 20.0) {
            (accState.tractionEnergyWh / (finalTripDistanceMeters / 1000.0)).toFloat()
        } else {
            0.0f
        }
        val dateText = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        _lastRideSummary.value = RideSession(
            date = dateText,
            distanceKm = (finalTripDistanceMeters / 1000.0).toFloat(),
            durationMin = (durationMs / 60000).toInt(),
            maxSpeed = accState.maxSpeedKmh.toFloat(),
            avgSpeed = avgSpeed,
            totalWh = accState.netBatteryEnergyWh.toFloat(),
            avgEfficiency = finalAvgNetEfficiency
        )
        val rawHistoryRecord = RideHistoryRecord(
            id = UUID.randomUUID().toString(),
            title = buildRideTitle(_sessionStartTime),
            startedAtMs = _sessionStartTime,
            endedAtMs = endedAtMs,
            durationMs = durationMs,
            distanceMeters = finalTripDistanceMeters,
            maxSpeedKmh = accState.maxSpeedKmh,
            avgSpeedKmh = avgSpeed,
            peakPowerKw = accState.peakDrivePowerKw,
            totalEnergyWh = accState.netBatteryEnergyWh,
            tractionEnergyWh = accState.tractionEnergyWh,
            regenEnergyWh = accState.regenEnergyWh,
            avgEfficiencyWhKm = finalAvgNetEfficiency,
            avgNetEfficiencyWhKm = finalAvgNetEfficiency,
            avgTractionEfficiencyWhKm = finalAvgTractionEfficiency,
            trackPoints = rideTrackPoints.toList(),
            samples = rideSamples.toList()
        )
        val historyRecord = normalizeRideHistoryRecord(
            record = rawHistoryRecord,
            wheelCircumferenceMm = currentVehicle.value.wheelCircumferenceMm.toFloat(),
            polePairs = currentVehicle.value.polePairs
        )
        val hasMeaningfulData = hasMeaningfulRideData(historyRecord, finalEnergyWh)
        val shouldPersist = if (forceSave) {
            hasMeaningfulData
        } else {
            historyRecord.distanceMeters >= 50.0 || historyRecord.durationMs >= 60_000L
        }
        if (shouldPersist) {
            viewModelScope.launch {
                rideHistoryRepository.upsertRide(currentVehicle.value.id, historyRecord)
                persistVehicleSnapshot()
                // Auto-sync to Google Drive after saving ride (V2 sync engine)
                syncScheduler.onRideSaved(historyRecord.id)
            }
        }
        _lastRideLocation = null
        _rideStopCandidateAtMs = null
        lastVehicleSnapshotTripDistanceKm = 0.0f
        if (rideStartMode == RideStartMode.MANUAL && metrics.value.speedKmH > AUTO_RIDE_STOP_SPEED_KMH) {
            _autoRideSuppressedUntilStop.value = true
        }
        rideStartMode = null
        rideStartSocPercent = Float.NaN
        lastRangeDisplayCommitDistanceKm = Float.NaN
        _tripDistanceMeters = 0.0f
        return shouldPersist
    }

    private fun hasMeaningfulRideData(record: RideHistoryRecord, finalEnergyWh: Float): Boolean {
        if (record.distanceMeters >= 25.0f) return true
        if (record.maxSpeedKmh >= 3.0f) return true
        if (finalEnergyWh >= 5.0f) return true
        if (record.samples.any { sample ->
                sample.speedKmH >= 2.5 ||
                    abs(sample.busCurrent) >= 2.0 ||
                    sample.rpm >= 60.0 ||
                    sample.voltage > 5.0
            }
        ) {
            return true
        }
        return false
    }

    private suspend fun migrateLegacyRideHistoryIfNeeded() {
        val currentVersion = settingsRepository.getRideHistoryStorageMigrationVersion()
        if (currentVersion >= RIDE_HISTORY_NORMALIZATION_VERSION_CURRENT) return

        val profilesById = settingsRepository.vehicleProfiles.first().associateBy { it.id }
        val legacyPayloads = settingsRepository.listLegacyRideHistoryPayloads()
        var migratedRecordCount = 0

        legacyPayloads.forEach { payload ->
            val profile = profilesById[payload.vehicleId]
            val wheelCircumferenceMm = profile?.wheelCircumferenceMm ?: 1800f
            val polePairs = profile?.polePairs ?: 50
            val normalizedRecords = payload.records.map { record ->
                normalizeRideHistoryRecord(
                    record = record,
                    wheelCircumferenceMm = wheelCircumferenceMm,
                    polePairs = polePairs
                )
            }.sortedByDescending { it.startedAtMs }

            if (normalizedRecords.isNotEmpty()) {
                rideHistoryRepository.replaceRidesForVehicle(payload.vehicleId, normalizedRecords)
                migratedRecordCount += normalizedRecords.size
            }
            settingsRepository.clearLegacyRideHistoryForVehicle(payload.vehicleId)
        }

        settingsRepository.saveRideHistoryStorageMigrationVersion(RIDE_HISTORY_NORMALIZATION_VERSION_CURRENT)
        settingsRepository.saveRideHistoryNormalizationVersion(RIDE_HISTORY_NORMALIZATION_VERSION_CURRENT)
        if (migratedRecordCount > 0) {
            AppLogger.i(TAG, "Ride history storage migrated to Room records=$migratedRecordCount")
        }
    }

    fun presentRideHistoryRecord(record: RideHistoryRecord): RideHistoryRecord {
        val profile = currentVehicle.value
        val normalized = normalizeRideHistoryRecord(
            record = record,
            wheelCircumferenceMm = profile.wheelCircumferenceMm.toFloat(),
            polePairs = profile.polePairs
        )
        return rideRecordNormalizer.normalize(normalized)
    }

    suspend fun loadPresentedRideHistoryRecord(id: String): RideHistoryRecord? =
        rideHistoryRepository.loadRideRecord(id)?.let(::presentRideHistoryRecord)

    private fun needsRideHistoryRepair(
        original: RideHistoryRecord,
        candidate: RideHistoryRecord
    ): Boolean {
        if (rideRecordNormalizer.isBroken(original)) return true
        if (candidate == original) return false

        return abs(original.distanceMeters - candidate.distanceMeters) > 10f ||
            abs(original.totalEnergyWh - candidate.totalEnergyWh) > 5f ||
            abs(original.tractionEnergyWh - candidate.tractionEnergyWh) > 5f ||
            abs(original.regenEnergyWh - candidate.regenEnergyWh) > 5f ||
            abs(original.avgNetEfficiencyWhKm - candidate.avgNetEfficiencyWhKm) > 1f ||
            abs(original.maxSpeedKmh - candidate.maxSpeedKmh) > 0.5f
    }

    suspend fun repairRideHistoryRecord(id: String): String {
        val original = rideHistoryRepository.loadRideRecord(id) ?: return "未找到这条行程记录"
        val repaired = presentRideHistoryRecord(original)
        if (!needsRideHistoryRepair(original, repaired)) {
            return "当前记录无需修复"
        }
        val vehicleId = rideHistory.value.firstOrNull { it.id == id }?.vehicleId ?: currentVehicle.value.id
        rideHistoryRepository.upsertRide(vehicleId, repaired)
        syncScheduler.onRideHistoryChanged(id)
        AppLogger.i(TAG, "Ride history repaired id=$id")
        return "已按新版算法修复"
    }

    private fun normalizeRideHistoryRecord(
        record: RideHistoryRecord,
        wheelCircumferenceMm: Float,
        polePairs: Int
    ): RideHistoryRecord {
        val distanceNormalized = backfillRideDistanceFromSamples(
            record = record,
            wheelCircumferenceMm = wheelCircumferenceMm,
            polePairs = polePairs
        )
        val energyNormalizedSamples = normalizeRideEnergySamples(distanceNormalized)
        if (energyNormalizedSamples.isEmpty()) return distanceNormalized

        val finalDistanceMeters = energyNormalizedSamples.lastOrNull()?.distanceMeters?.takeIf { it > 1.0f }
            ?: distanceNormalized.distanceMeters
        val summaryStats = computeRideSummaryStats(distanceNormalized.copy(samples = energyNormalizedSamples))
        val maxSpeedKmh = summaryStats.maxSpeedKmh
        val tractionEnergyWh = maxOf(
            distanceNormalized.tractionEnergyWh.coerceAtLeast(0.0f),
            energyNormalizedSamples.maxOfOrNull { it.tractionEnergyWh } ?: 0.0f
        )
        val regenEnergyWh = maxOf(
            distanceNormalized.regenEnergyWh.coerceAtLeast(0.0f),
            energyNormalizedSamples.maxOfOrNull { maxOf(it.regenEnergyWh, it.recoveredEnergyWh) } ?: 0.0f
        )
        val totalEnergyWh = maxOf(
            distanceNormalized.totalEnergyWh.coerceAtLeast(0.0f),
            energyNormalizedSamples.maxOfOrNull { it.totalEnergyWh } ?: 0.0f,
            tractionEnergyWh - regenEnergyWh
        )
        val avgNetEfficiencyWhKm = if (finalDistanceMeters > 20.0f && totalEnergyWh > 0.01f) {
            totalEnergyWh / (finalDistanceMeters / 1000.0f)
        } else {
            maxOf(distanceNormalized.avgEfficiencyWhKm, distanceNormalized.avgNetEfficiencyWhKm)
        }
        val avgTractionEfficiencyWhKm = if (finalDistanceMeters > 20.0f && tractionEnergyWh > 0.01f) {
            tractionEnergyWh / (finalDistanceMeters / 1000.0f)
        } else {
            distanceNormalized.avgTractionEfficiencyWhKm
        }
        val avgSpeedKmh = summaryStats.avgSpeedKmh
        return distanceNormalized.copy(
            distanceMeters = finalDistanceMeters,
            maxSpeedKmh = maxSpeedKmh,
            avgSpeedKmh = avgSpeedKmh,
            totalEnergyWh = totalEnergyWh,
            tractionEnergyWh = tractionEnergyWh,
            regenEnergyWh = regenEnergyWh,
            avgEfficiencyWhKm = avgNetEfficiencyWhKm,
            avgNetEfficiencyWhKm = avgNetEfficiencyWhKm,
            avgTractionEfficiencyWhKm = avgTractionEfficiencyWhKm,
            samples = energyNormalizedSamples
        )
    }

    private fun backfillRideDistanceFromSamples(
        record: RideHistoryRecord,
        wheelCircumferenceMm: Float,
        polePairs: Int
    ): RideHistoryRecord {
        val samples = record.samples
        if (samples.size < 2) return record

        val hasMovementEvidence = record.maxSpeedKmh >= HISTORY_BACKFILL_MIN_SPEED_KMH ||
            samples.any { sample ->
                sample.speedKmH >= HISTORY_BACKFILL_MIN_SPEED_KMH ||
                    sample.rpm >= HISTORY_BACKFILL_MIN_RPM ||
                    abs(sample.busCurrent) >= HISTORY_BACKFILL_MIN_CURRENT_A
            }
        if (!hasMovementEvidence) return record

        val rpmToSpeedRatio = inferRpmSpeedRatio(
            record = record,
            samples = samples,
            wheelCircumferenceMm = wheelCircumferenceMm,
            polePairs = polePairs
        )

        val fallbackIntervalSeconds = when {
            record.durationMs > 0L && samples.size > 1 -> {
                (record.durationMs / (samples.size - 1).toFloat() / 1000f).coerceIn(0.2f, DISTANCE_FALLBACK_MAX_DT_SECONDS)
            }
            else -> 1f
        }

        var integratedDistanceMeters = 0.0f
        val rebuiltSamples = buildList(samples.size) {
            add(samples.first().copy(distanceMeters = 0.0f))
            for (index in 1 until samples.size) {
                val prev = samples[index - 1]
                val cur = samples[index]
                val deltaByElapsed = (cur.elapsedMs - prev.elapsedMs).toFloat() / 1000.0f
                val deltaByTimestamp = (cur.timestampMs - prev.timestampMs).toFloat() / 1000.0f
                val dtSeconds = when {
                    deltaByElapsed > 0.0f -> deltaByElapsed
                    deltaByTimestamp > 0.0f -> deltaByTimestamp
                    else -> fallbackIntervalSeconds
                }.coerceIn(0.0f, DISTANCE_FALLBACK_MAX_DT_SECONDS)

                val prevSpeed = sampleSpeedKmH(prev, rpmToSpeedRatio)
                val curSpeed = sampleSpeedKmH(cur, rpmToSpeedRatio)
                val avgSpeed = ((prevSpeed + curSpeed) * 0.5f).coerceAtLeast(0.0f)
                if (dtSeconds > 0.0f && avgSpeed >= HISTORY_BACKFILL_MIN_SPEED_KMH) {
                    integratedDistanceMeters += (avgSpeed / 3.6f) * dtSeconds
                }
                add(cur.copy(distanceMeters = integratedDistanceMeters))
            }
        }

        val rebuiltTotalDistance = rebuiltSamples.last().distanceMeters
        if (rebuiltTotalDistance < HISTORY_BACKFILL_APPLY_MIN_DISTANCE_METERS) return record

        val rebuiltAvgEfficiency = if (rebuiltTotalDistance > 20.0f && record.totalEnergyWh > 0.01f) {
            (record.totalEnergyWh / (rebuiltTotalDistance / 1000f))
        } else {
            record.avgEfficiencyWhKm
        }
        val rebuiltAvgSpeed = if (record.durationMs > 0L && rebuiltTotalDistance > 1.0f) {
            ((rebuiltTotalDistance / 1000.0f) / (record.durationMs.toFloat() / 3_600_000.0f))
        } else {
            record.avgSpeedKmh
        }
        if (abs(rebuiltTotalDistance - record.distanceMeters) < 0.5f &&
            abs(rebuiltAvgSpeed - record.avgSpeedKmh) < 0.05f &&
            rebuiltSamples == samples
        ) {
            return record
        }
        return record.copy(
            distanceMeters = rebuiltTotalDistance,
            avgSpeedKmh = rebuiltAvgSpeed,
            avgEfficiencyWhKm = rebuiltAvgEfficiency,
            samples = rebuiltSamples
        )
    }

    private fun sampleSpeedKmH(
        sample: RideMetricSample,
        rpmToSpeedRatio: Float
    ): Float {
        return if (sample.rpm >= HISTORY_BACKFILL_MIN_RPM) {
            (sample.rpm * rpmToSpeedRatio).coerceAtLeast(0f)
        } else if (sample.speedKmH >= HISTORY_BACKFILL_MIN_SPEED_KMH) {
            sample.speedKmH
        } else {
            0f
        }
    }

    private fun calcEfficiency(
        sample: RideMetricSample,
        energy: SampleEnergyTotals,
        record: RideHistoryRecord
    ): Float {
        if (sample.distanceMeters > 10.0f && energy.totalEnergyWh > 0.01f) {
            return energy.totalEnergyWh / (sample.distanceMeters / 1000f)
        }
        if (record.avgEfficiencyWhKm > 0.1f) {
            return record.avgEfficiencyWhKm
        }
        val fallback = sample.avgNetEfficiencyWhKm
        return if (fallback > 0.01f) fallback else sample.avgEfficiencyWhKm
    }

    private fun normalizeRideEnergySamples(record: RideHistoryRecord): List<RideMetricSample> {
        val samples = record.samples
        if (samples.isEmpty()) return samples

        val normalizedEnergy = normalizeSampleEnergySeries(
            samples = samples,
            recordTotalEnergyWh = record.totalEnergyWh
        )
        val firstValidSoc = samples.firstOrNull { it.soc in 1.0f..100.0f }?.soc
        var lastValidSoc = firstValidSoc ?: 0.0f

        return samples.mapIndexed { index, sample ->
            val energy = normalizedEnergy[index]
            val normalizedSoc = sample.soc.takeIf { it in 1.0f..100.0f }?.also {
                lastValidSoc = it
            } ?: lastValidSoc

            val normalizedAvgEfficiencyWhKm = calcEfficiency(sample, energy, record)
            val normalizedAvgNetEfficiencyWhKm = if (sample.distanceMeters > 10.0f && energy.totalEnergyWh > 0.01f) {
                energy.totalEnergyWh / (sample.distanceMeters / 1000f)
            } else {
                normalizedAvgEfficiencyWhKm
            }
            val normalizedAvgTractionEfficiencyWhKm = if (sample.distanceMeters > 10.0f && energy.tractionEnergyWh > 0.01f) {
                energy.tractionEnergyWh / (sample.distanceMeters / 1000f)
            } else {
                sample.avgTractionEfficiencyWhKm
            }

            sample.copy(
                soc = normalizedSoc,
                totalEnergyWh = energy.totalEnergyWh,
                tractionEnergyWh = energy.tractionEnergyWh,
                regenEnergyWh = energy.regenEnergyWh,
                recoveredEnergyWh = energy.regenEnergyWh,
                avgEfficiencyWhKm = normalizedAvgEfficiencyWhKm,
                avgNetEfficiencyWhKm = normalizedAvgNetEfficiencyWhKm,
                avgTractionEfficiencyWhKm = normalizedAvgTractionEfficiencyWhKm
            )
        }
    }

    private data class SampleEnergyTotals(
        val totalEnergyWh: Float,
        val tractionEnergyWh: Float,
        val regenEnergyWh: Float
    )

    private fun shouldPreferRebuiltNetEnergy(
        cumulativeNetEnergyWh: Float,
        rebuiltTractionEnergyWh: Float,
        rebuiltRegenEnergyWh: Float
    ): Boolean {
        val rebuiltNetEnergyWh = (rebuiltTractionEnergyWh - rebuiltRegenEnergyWh).coerceAtLeast(0f)
        if (rebuiltNetEnergyWh <= 0.5f || cumulativeNetEnergyWh <= 0.5f) return false
        val suspiciousVsNet = cumulativeNetEnergyWh > rebuiltNetEnergyWh * 2.5f
        val suspiciousVsTraction = cumulativeNetEnergyWh > rebuiltTractionEnergyWh + 20f
        return suspiciousVsNet || suspiciousVsTraction
    }

    private fun normalizeSampleEnergySeries(
        samples: List<RideMetricSample>,
        recordTotalEnergyWh: Float
    ): List<SampleEnergyTotals> {
        if (samples.isEmpty()) return emptyList()

        val hasCumulativeEnergy = samples.any { it.totalEnergyWh > 0.01f }
        val hasTractionEnergy = samples.any { it.tractionEnergyWh > 0.01f }
        val hasRecoveredEnergy = samples.any { it.recoveredEnergyWh > 0.01f }
        val hasRegenEnergy = samples.any { it.regenEnergyWh > 0.01f }
        val canRebuildTractionEnergy = samples.size > 1 && samples.any {
            it.powerKw > 0.02f || it.busCurrent > 0.2f
        }
        val canRebuildRecoveredEnergy = samples.size > 1 && samples.any {
            it.powerKw < -0.02f || it.busCurrent < -0.2f
        }
        val finalDistanceMeters = samples.lastOrNull()?.distanceMeters?.takeIf { it > 1.0f } ?: 0.0f
        val finalElapsedMs = samples.lastOrNull()?.elapsedMs?.takeIf { it > 0L } ?: 0L

        var energyOffsetWh = 0f
        var tractionOffsetWh = 0f
        var recoveredOffsetWh = 0f
        var previousRawEnergyWh = 0f
        var previousRawTractionWh = 0f
        var previousRawRecoveredWh = 0f
        var runningTotalEnergyWh = 0f
        var runningTractionWh = 0f
        var runningRecoveredWh = 0f
        var previousSample: RideMetricSample? = null

        return samples.map { sample ->
            val progress = when {
                finalDistanceMeters > 1.0f && sample.distanceMeters > 0.0f ->
                    (sample.distanceMeters / finalDistanceMeters).coerceIn(0.0f, 1.0f)
                finalElapsedMs > 0L ->
                    (sample.elapsedMs.toFloat() / finalElapsedMs.toFloat()).coerceIn(0f, 1f)
                else -> 0f
            }

            val rawTotalEnergyWh = when {
                hasCumulativeEnergy -> sample.totalEnergyWh.coerceAtLeast(0f)
                recordTotalEnergyWh > 0.01f -> (recordTotalEnergyWh * progress).coerceAtLeast(0f)
                else -> 0f
            }
            if (hasCumulativeEnergy && rawTotalEnergyWh + 0.05f < previousRawEnergyWh) {
                energyOffsetWh += previousRawEnergyWh
            }
            previousRawEnergyWh = rawTotalEnergyWh
            runningTotalEnergyWh = if (hasCumulativeEnergy) {
                maxOf(
                    runningTotalEnergyWh,
                    (energyOffsetWh + rawTotalEnergyWh).coerceAtLeast(0f)
                )
            } else {
                runningTotalEnergyWh + integrateEnergyDeltaWh(previousSample, sample, recovering = false)
            }

            val rawTractionEnergyWh = when {
                hasTractionEnergy -> sample.tractionEnergyWh.coerceAtLeast(0f)
                recordTotalEnergyWh > 0.01f -> (recordTotalEnergyWh * progress).coerceAtLeast(0f)
                else -> 0f
            }
            if (hasTractionEnergy && rawTractionEnergyWh + 0.05f < previousRawTractionWh) {
                tractionOffsetWh += previousRawTractionWh
            }
            previousRawTractionWh = rawTractionEnergyWh
            runningTractionWh = if (canRebuildTractionEnergy) {
                runningTractionWh + integrateEnergyDeltaWh(previousSample, sample, recovering = false)
            } else if (hasTractionEnergy) {
                maxOf(
                    runningTractionWh,
                    (tractionOffsetWh + rawTractionEnergyWh).coerceAtLeast(0f)
                )
            } else {
                maxOf(runningTractionWh, runningTotalEnergyWh)
            }

            val rawRecoveredEnergyWh = sample.recoveredEnergyWh.coerceAtLeast(0f)
            if (hasRecoveredEnergy && rawRecoveredEnergyWh + 0.05f < previousRawRecoveredWh) {
                recoveredOffsetWh += previousRawRecoveredWh
            }
            previousRawRecoveredWh = rawRecoveredEnergyWh
            runningRecoveredWh = if (canRebuildRecoveredEnergy) {
                runningRecoveredWh + integrateEnergyDeltaWh(previousSample, sample, recovering = true)
            } else if (hasRecoveredEnergy || hasRegenEnergy) {
                maxOf(
                    runningRecoveredWh,
                    (recoveredOffsetWh + maxOf(sample.regenEnergyWh, rawRecoveredEnergyWh)).coerceAtLeast(0f)
                )
            } else {
                0f
            }

            val rebuiltNetEnergyWh = (runningTractionWh - runningRecoveredWh).coerceAtLeast(0f)
            val netEnergyWh = when {
                hasCumulativeEnergy && shouldPreferRebuiltNetEnergy(
                    cumulativeNetEnergyWh = runningTotalEnergyWh,
                    rebuiltTractionEnergyWh = runningTractionWh,
                    rebuiltRegenEnergyWh = runningRecoveredWh
                ) -> rebuiltNetEnergyWh
                hasCumulativeEnergy -> runningTotalEnergyWh
                canRebuildTractionEnergy || canRebuildRecoveredEnergy -> rebuiltNetEnergyWh
                else -> runningTotalEnergyWh.coerceAtLeast(0f)
            }

            previousSample = sample
            SampleEnergyTotals(
                totalEnergyWh = netEnergyWh,
                tractionEnergyWh = runningTractionWh.coerceAtLeast(0f),
                regenEnergyWh = runningRecoveredWh.coerceAtLeast(0f)
            )
        }
    }

    private fun integrateEnergyDeltaWh(
        previous: RideMetricSample?,
        current: RideMetricSample,
        recovering: Boolean
    ): Float {
        if (previous == null) return 0f
        val deltaMs = when {
            current.timestampMs > previous.timestampMs -> current.timestampMs - previous.timestampMs
            current.elapsedMs > previous.elapsedMs -> current.elapsedMs - previous.elapsedMs
            else -> 0L
        }
        val deltaHours = (deltaMs / 3_600_000f).coerceIn(0f, 5f / 3600f)
        if (deltaHours <= 0f) return 0f

        val powerW = (current.powerKw * 1000.0f)
        val directionMatches = if (recovering) powerW < 0f else powerW > 0f
        if (!directionMatches) return 0f

        val absPowerW = abs(powerW)
        val absCurrentA = maxOf(abs(previous.busCurrent), abs(current.busCurrent))
        val moving = maxOf(previous.speedKmH, current.speedKmH) >= HISTORY_BACKFILL_MIN_SPEED_KMH ||
            maxOf(previous.rpm, current.rpm) >= HISTORY_BACKFILL_MIN_RPM
        val hasMeaningfulLoad = absPowerW >= ENERGY_INTEGRATION_MIN_POWER_W ||
            absCurrentA >= ENERGY_INTEGRATION_MIN_CURRENT_A ||
            moving
        if (!hasMeaningfulLoad) return 0f

        return absPowerW * deltaHours
    }

    private fun inferRpmSpeedRatio(
        record: RideHistoryRecord,
        samples: List<RideMetricSample>,
        wheelCircumferenceMm: Float,
        polePairs: Int
    ): Float {
        val correlatedRatios = samples.mapNotNull { sample ->
            if (sample.speedKmH >= HISTORY_BACKFILL_MIN_SPEED_KMH && sample.rpm >= HISTORY_BACKFILL_MIN_RPM) {
                (sample.speedKmH / sample.rpm).takeIf { !it.isNaN() && !it.isInfinite() && it > 0f }
            } else {
                null
            }
        }
        if (correlatedRatios.isNotEmpty()) {
            val sorted = correlatedRatios.sorted()
            return sorted[sorted.size / 2].coerceIn(0.0005f, 0.6f)
        }

        val maxSampleRpm = samples.maxOfOrNull { it.rpm } ?: 0f
        if (record.maxSpeedKmh >= HISTORY_BACKFILL_MIN_SPEED_KMH && maxSampleRpm >= HISTORY_BACKFILL_MIN_RPM) {
            return (record.maxSpeedKmh / maxSampleRpm).coerceIn(0.0005f, 0.6f)
        }

        val directRatio = (wheelCircumferenceMm.coerceIn(500f, 5000f) * 60f) / 1_000_000f
        val poleAdjustedRatio = if (polePairs > 0) directRatio / polePairs else directRatio
        if (maxSampleRpm >= HISTORY_BACKFILL_MIN_RPM && record.maxSpeedKmh > 0.0f) {
            val directError = abs((maxSampleRpm * directRatio) - record.maxSpeedKmh)
            val poleError = abs((maxSampleRpm * poleAdjustedRatio) - record.maxSpeedKmh)
            return if (directError <= poleError) {
                directRatio.coerceIn(0.0005f, 0.6f)
            } else {
                poleAdjustedRatio.coerceIn(0.0005f, 0.6f)
            }
        }
        return directRatio.coerceIn(0.0005f, 0.6f)
    }

    private fun calculateMaxSpeedForSamples(samples: List<RideMetricSample>): Float {
        if (samples.isEmpty()) return 0.0f
        return samples.maxOfOrNull { it.speedKmH } ?: 0.0f
    }

    private fun calculateAvgSpeedForSamples(samples: List<RideMetricSample>, durationMs: Long): Float {
        if (samples.isEmpty() || durationMs <= 0) return 0.0f
        val distance = samples.last().distanceMeters
        return (distance / 1000.0f) / (durationMs.toFloat() / 3_600_000.0f)
    }

    private fun calculateMaxRpmForSamples(samples: List<RideMetricSample>): Float {
        if (samples.isEmpty()) return 0.0f
        return samples.maxOfOrNull { it.rpm } ?: 0.0f
    }

    private fun maybePersistVehicleSnapshot(now: Long) {
        if (!_isRideActive.value) return
        val tripDistanceKm = authoritativeTripDistanceMeters() / 1000.0f
        val dueByTime = now - lastVehicleSnapshotAtMs >= 3 * 60 * 1000L
        val dueByDistance = (tripDistanceKm - lastVehicleSnapshotTripDistanceKm) >= 1f
        if (!dueByTime && !dueByDistance) return

        lastVehicleSnapshotAtMs = now
        lastVehicleSnapshotTripDistanceKm = tripDistanceKm
        viewModelScope.launch {
            persistVehicleSnapshot()
        }
    }

    private suspend fun persistVehicleSnapshot() {
        val tripDistanceKm = authoritativeTripDistanceMeters() / 1000.0f
        val absoluteMileageKm = rideStartVehicleMileageKm + tripDistanceKm
        val learnedResistance: Float = _batteryState.value?.learnedInternalResistanceOhm ?: 0.0f
        val observedEfficiencyWhKm = observeCurrentRideEfficiencyWhKm()
        val currentProfile = currentVehicle.value
        val consumedEnergy: Float = rideAccumulator.state.tractionEnergyWh
        val observedUsableEnergyRatio = calculateLearnedUsableEnergyRatio(
            profile = currentProfile,
            consumedEnergyWh = consumedEnergy
        )
        settingsRepository.updateCurrentVehicle { profile ->
            val newMileage = absoluteMileageKm.coerceAtLeast(profile.totalMileageKm)
            val newResistance = if (learnedResistance > 0.0f) learnedResistance else profile.learnedInternalResistanceOhm
            val newEfficiency = blendLearnedEfficiencyWhKm(
                previousLearnedWhKm = profile.learnedEfficiencyWhKm,
                observedWhKm = observedEfficiencyWhKm,
                tripDistanceKm = tripDistanceKm
            )
            val newUsableRatio = blendLearnedUsableEnergyRatio(
                previousRatio = profile.learnedUsableEnergyRatio,
                observedRatio = observedUsableEnergyRatio,
                tripDistanceKm = tripDistanceKm
            )
            val updated: VehicleProfile = profile.copy(
                totalMileageKm = newMileage,
                learnedInternalResistanceOhm = newResistance,
                learnedEfficiencyWhKm = newEfficiency,
                learnedUsableEnergyRatio = newUsableRatio
            )
            updated
        }
    }

    private fun observeCurrentRideEfficiencyWhKm(): Float {
        val tripDistanceKm = authoritativeTripDistanceMeters() / 1000.0
        if (tripDistanceKm < 0.25) return 0.0f
        val totalEnergyWh = rideAccumulator.state.tractionEnergyWh
        if (totalEnergyWh <= 1.0f) return 0.0f
        return totalEnergyWh / tripDistanceKm.toFloat()
    }

    private fun resolveBaseEfficiencyWhKm(
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
                (shortTerm * shortWeight) + (learned * (1.0f - shortWeight))
            }
            shortTerm != null -> shortTerm
            learned != null -> learned
            else -> 0f
        }
    }

    private fun resolveRangeReferenceSpeedKmh(currentSpeedKmh: Float): Float {
        if (currentSpeedKmh >= RANGE_REFERENCE_MIN_SPEED_KMH) {
            val previous = lastRangeReferenceSpeedKmh.takeIf { it >= RANGE_REFERENCE_MIN_SPEED_KMH }
                ?: currentSpeedKmh
            lastRangeReferenceSpeedKmh =
                ((previous * 0.82f) + (currentSpeedKmh * 0.18f)).coerceIn(12f, 85f)
        }
        return lastRangeReferenceSpeedKmh.coerceAtLeast(RANGE_REFERENCE_MIN_SPEED_KMH)
    }

    private fun applyRangeConditionAdjustments(
        baseEfficiencyWhKm: Float,
        rangeReferenceSpeedKmh: Float,
        filteredCurrentA: Float,
        learnedInternalResistanceOhm: Float
    ): Float {
        val base = baseEfficiencyWhKm.takeIf { it in MIN_VALID_EFFICIENCY_WH_KM..MAX_VALID_EFFICIENCY_WH_KM }
            ?: return 0f

        val speedKmh = rangeReferenceSpeedKmh.coerceAtLeast(RANGE_REFERENCE_MIN_SPEED_KMH)
        val accessoryWhKm = (RANGE_BASE_ACCESSORY_POWER_W / speedKmh).coerceIn(0.4f, 3.4f)
        val dischargeCurrentA = filteredCurrentA.coerceAtLeast(0f)
        val internalLossW = dischargeCurrentA * dischargeCurrentA * learnedInternalResistanceOhm.coerceAtLeast(0f)
        val internalLossWhKm = (internalLossW / speedKmh).coerceIn(0f, 12f)
        val aggressionPenaltyWhKm = ((dischargeCurrentA - RANGE_AGGRESSIVE_CURRENT_A) / 28.0f)
            .coerceIn(0f, 1.2f) * 2.3f

        return (base + accessoryWhKm + internalLossWhKm + aggressionPenaltyWhKm).coerceIn(MIN_VALID_EFFICIENCY_WH_KM, 150f)
    }

    private fun blendLearnedEfficiencyWhKm(
        previousLearnedWhKm: Float,
        observedWhKm: Float,
        tripDistanceKm: Float
    ): Float {
        // NaN guard: sanitize inputs
        val safePrevious = previousLearnedWhKm.takeIf { it.isFinite() } ?: 0f
        val safeObserved = observedWhKm.takeIf { it.isFinite() } ?: 0f
        val safeDistance = tripDistanceKm.takeIf { it.isFinite() } ?: 0f

        val observed = safeObserved.takeIf { it in MIN_VALID_EFFICIENCY_WH_KM..MAX_VALID_EFFICIENCY_WH_KM }
            ?: return safePrevious.coerceAtLeast(0f)
        if (safeDistance < 0.25f) return safePrevious.coerceAtLeast(0f)

        val previous = safePrevious.takeIf { it in MIN_VALID_EFFICIENCY_WH_KM..MAX_VALID_EFFICIENCY_WH_KM }
        if (previous == null) return observed

        val updateWeight = (safeDistance / 25f).coerceIn(0.05f, 0.35f)
        val result = (previous * (1.0f - updateWeight)) + (observed * updateWeight)
        return result.takeIf { it.isFinite() } ?: observed
    }

    private fun calculateLearnedUsableEnergyRatio(
        profile: com.shawnrain.sdash.data.VehicleProfile,
        consumedEnergyWh: Float
    ): Float {
        val startSoc = rideStartSocPercent.takeIf { it.isFinite() } ?: return profile.learnedUsableEnergyRatio
        val endSoc = _batteryState.value?.socPercent?.toFloat()?.takeIf { it.isFinite() }
            ?: return profile.learnedUsableEnergyRatio
        if (endSoc !in 0f..100f) return profile.learnedUsableEnergyRatio
        val socDropPercent = (startSoc - endSoc).coerceAtLeast(0f)
        if (socDropPercent < 4f || consumedEnergyWh < 80f) return profile.learnedUsableEnergyRatio

        val nominalPackEnergyWh = profile.batteryCapacityAh.coerceAtLeast(1f) *
            profile.batterySeries.coerceAtLeast(1) * 3.7f
        if (nominalPackEnergyWh <= 1f) return profile.learnedUsableEnergyRatio

        val impliedPackEnergyWh = consumedEnergyWh / (socDropPercent / 100f)
        val result = (impliedPackEnergyWh / nominalPackEnergyWh).coerceIn(0.72f, 0.98f)
        return result.takeIf { it.isFinite() } ?: profile.learnedUsableEnergyRatio
    }

    private fun blendLearnedUsableEnergyRatio(
        previousRatio: Float,
        observedRatio: Float,
        tripDistanceKm: Float
    ): Float {
        // NaN guard: sanitize inputs
        val safePrevious = previousRatio.takeIf { it.isFinite() } ?: 0.9f
        val safeObserved = observedRatio.takeIf { it.isFinite() } ?: 0.9f
        val safeDistance = tripDistanceKm.takeIf { it.isFinite() } ?: 0f

        val previous = safePrevious.coerceIn(0.72f, 0.98f)
        val observed = safeObserved.coerceIn(0.72f, 0.98f)
        val weight = (safeDistance / 40f).coerceIn(0.03f, 0.16f)
        val result = (previous * (1f - weight)) + (observed * weight)
        return result.takeIf { it.isFinite() } ?: previous
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
        learnedEfficiencyWhKm: Float,
        tripDistanceKm: Float,
        rideActive: Boolean
    ): Float {
        val resolvedRangeKm = if (rawRangeKm > 0.1f) {
            rawRangeKm
        } else {
            val startupEfficiency = learnedEfficiencyWhKm
                .takeIf { it in MIN_VALID_EFFICIENCY_WH_KM..MAX_VALID_EFFICIENCY_WH_KM }
                ?: DEFAULT_STARTUP_EFFICIENCY_WH_KM
            val startupRange = if (startupEfficiency > 0f) {
                remainingEnergyWh / startupEfficiency
            } else {
                0f
            }
            startupRange.takeIf { it > 0.1f } ?: (lastKnownRangeKm.takeIf { it > 0.1f } ?: 0f)
        }

        if (!rideActive) {
            lastKnownRangeKm = resolvedRangeKm
            return resolvedRangeKm
        }

        val normalizedDistanceKm = tripDistanceKm.coerceAtLeast(0f)
        val shownRangeKm = lastKnownRangeKm.takeIf { it > 0.1f }
        if (shownRangeKm == null) {
            lastKnownRangeKm = resolvedRangeKm
            lastRangeDisplayCommitDistanceKm = normalizedDistanceKm
            return resolvedRangeKm
        }

        if (!lastRangeDisplayCommitDistanceKm.isFinite()) {
            lastRangeDisplayCommitDistanceKm = normalizedDistanceKm
        }
        val distanceSinceCommitKm = (normalizedDistanceKm - lastRangeDisplayCommitDistanceKm).coerceAtLeast(0f)
        if (distanceSinceCommitKm >= RANGE_DISPLAY_UPDATE_STEP_KM) {
            lastKnownRangeKm = resolvedRangeKm
            lastRangeDisplayCommitDistanceKm = normalizedDistanceKm
        }
        return lastKnownRangeKm
    }

    private fun Double.isFiniteValue(): Boolean = !this.isNaN() && !this.isInfinite()

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
            val saved = stopRide(forceSave = true, stopAtMs = finishAtMs)
            _calibrationMessage.value = if (saved) {
                if (reason == RideStopReason.PARKED) {
                    "车辆已停稳，已自动结束并保存本次行程"
                } else {
                    "控制器连接已断开，已自动结束并保存本次行程"
                }
            } else {
                "已结束本次行程（未保存，数据不足）"
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
        val saved = stopRide(forceSave = true, stopAtMs = finishAtMs)
        _calibrationMessage.value = if (saved) "已结束并保存本次行程" else "已结束本次行程（未保存，数据不足）"
    }

    fun toggleRideTracking() {
        if (_isRideActive.value) {
            val saved = stopRide(forceSave = true)
            _calibrationMessage.value = if (saved) "已手动结束并保存本次行程" else "已结束本次行程（未保存，数据不足）"
        } else {
            val connection = bleManager.connectionState.value
            if (connection !is ConnectionState.Connected) {
                _calibrationMessage.value = "请先连接控制器后再开始记录"
                return
            }
            val metricsSnapshot = metrics.value
            val hasTelemetry = metricsSnapshot.voltage > 5f ||
                abs(metricsSnapshot.busCurrent) > 0.5f ||
                metricsSnapshot.rpm > 10f ||
                metricsSnapshot.speedKmH > 0.5f
            if (!hasTelemetry) {
                _calibrationMessage.value = "控制器已连接但暂无有效数据，请稍候再试"
                return
            }
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
        speedTestTierTimes.clear()
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
            syncScheduler.onSpeedTestHistoryChanged()
        }
    }

    fun deleteRideHistoryRecord(id: String) {
        viewModelScope.launch {
            rideHistoryRepository.deleteRide(id)
            syncScheduler.onRideHistoryChanged(id)
        }
    }

    suspend fun mergeRideHistoryRecords(ids: Set<String>): RideHistoryRecord? {
        val selected = rideHistoryRepository.loadRideRecords(ids)
            .sortedBy { it.startedAtMs }
        if (selected.size < 2) return null

        var elapsedOffsetMs = 0L
        var distanceOffsetMeters = 0f
        var totalEnergyOffsetWh = 0f
        var recoveredEnergyOffsetWh = 0f
        var runningMaxControllerTemp = 0f
        val mergedSamples = buildList {
            selected.forEach { record ->
                val normalizedSamples = normalizeRecordSamplesForMerge(record)
                if (normalizedSamples.isEmpty()) {
                    elapsedOffsetMs += record.durationMs
                    distanceOffsetMeters += record.distanceMeters
                    totalEnergyOffsetWh += record.totalEnergyWh.coerceAtLeast(0f)
                    return@forEach
                }

                var localEnergyWh = 0f
                var localRecoveredWh = 0f
                normalizedSamples.forEach { sample ->
                    localEnergyWh = maxOf(localEnergyWh, sample.totalEnergyWh.coerceAtLeast(0f))
                    localRecoveredWh = maxOf(localRecoveredWh, sample.recoveredEnergyWh.coerceAtLeast(0f))
                    val mergedDistanceMeters = distanceOffsetMeters + sample.distanceMeters
                    val mergedTotalEnergyWh = totalEnergyOffsetWh + localEnergyWh
                    val mergedRecoveredWh = recoveredEnergyOffsetWh + localRecoveredWh
                    runningMaxControllerTemp = maxOf(
                        runningMaxControllerTemp,
                        sample.controllerTemp,
                        sample.maxControllerTemp
                    )
                    val mergedAvgEfficiencyWhKm = if (mergedDistanceMeters > 10f && mergedTotalEnergyWh > 0.01f) {
                        (mergedTotalEnergyWh / (mergedDistanceMeters / 1000f)).toFloat()
                    } else {
                        sample.avgEfficiencyWhKm
                    }
                    add(
                        sample.copy(
                            elapsedMs = elapsedOffsetMs + sample.elapsedMs,
                            distanceMeters = mergedDistanceMeters,
                            totalEnergyWh = mergedTotalEnergyWh.toFloat(),
                            recoveredEnergyWh = mergedRecoveredWh.toFloat(),
                            avgEfficiencyWhKm = mergedAvgEfficiencyWhKm,
                            maxControllerTemp = runningMaxControllerTemp.toFloat()
                        )
                    )
                }
                elapsedOffsetMs += record.durationMs
                distanceOffsetMeters += record.distanceMeters
                totalEnergyOffsetWh += localEnergyWh.takeIf { it > 0f } ?: record.totalEnergyWh.coerceAtLeast(0f)
                recoveredEnergyOffsetWh += localRecoveredWh
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
        val distanceMeters = selected.fold(0f) { acc, r -> acc + r.distanceMeters }
        val totalEnergyWh = mergedSamples.lastOrNull()?.totalEnergyWh
            ?: selected.fold(0f) { acc, r -> acc + r.totalEnergyWh }
        val maxSpeedKmh = selected.maxOf { it.maxSpeedKmh }
        val peakPowerKw = selected.maxOf { it.peakPowerKw }
        val avgSpeedKmh = when {
            mergedSamples.isNotEmpty() -> mergedSamples.map { it.speedKmH }.average().toFloat()
            durationMs > 0L -> ((distanceMeters / 1000f) / (durationMs.toFloat() / 3600000f)).coerceAtLeast(0f)
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
            totalEnergyWh = totalEnergyWh.toFloat(),
            avgEfficiencyWhKm = avgEfficiencyWhKm,
            avgNetEfficiencyWhKm = avgEfficiencyWhKm,
            trackPoints = mergedTrackPoints,
            samples = mergedSamples
        )
        val vehicleId = rideHistory.value.firstOrNull { it.id in ids }?.vehicleId ?: currentVehicle.value.id
        ids.forEach { rideHistoryRepository.deleteRide(it) }
        rideHistoryRepository.upsertRide(vehicleId, mergedRecord)
        syncScheduler.onRideHistoryChanged(mergedRecord.id)
        _calibrationMessage.value = "已合并 ${selected.size} 条行程记录"
        return mergedRecord
    }

    private fun normalizeRecordSamplesForMerge(record: RideHistoryRecord): List<RideMetricSample> {
        val rawSamples = record.samples
        if (rawSamples.isEmpty()) return emptyList()

        val normalizedEnergy = normalizeSampleEnergySeries(
            samples = rawSamples,
            recordTotalEnergyWh = record.totalEnergyWh
        )
        return rawSamples.mapIndexed { index, sample ->
            val energy = normalizedEnergy[index]
            sample.copy(
                totalEnergyWh = energy.totalEnergyWh,
                tractionEnergyWh = energy.tractionEnergyWh,
                regenEnergyWh = energy.regenEnergyWh,
                recoveredEnergyWh = energy.regenEnergyWh
            )
        }
    }

    fun createSpeedTestShareIntent(
        record: SpeedTestRecord,
        settings: PosterSettings = PosterSettings()
    ): Intent {
        val bitmap = renderSpeedTestPoster(record, settings)
        val uri = exportBitmap(bitmap, "speedtest-${record.id}.png")
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "SmartDash ${record.label} 加速成绩")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newUri(
                getApplication<Application>().contentResolver,
                "SmartDash speed test poster",
                uri
            )
        }
        return Intent.createChooser(shareIntent, "分享加速成绩")
    }

    fun createRideShareIntent(
        record: RideHistoryRecord,
        settings: PosterSettings = PosterSettings()
    ): Intent {
        val bitmap = renderRidePoster(record, settings)
        val uri = exportBitmap(bitmap, "ride-${record.id}.png")
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "SmartDash ${record.title} 行程记录")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newUri(
                getApplication<Application>().contentResolver,
                "SmartDash ride poster",
                uri
            )
        }
        return Intent.createChooser(shareIntent, "分享行程记录")
    }

    fun saveRidePosterToGallery(
        record: RideHistoryRecord,
        settings: PosterSettings = PosterSettings()
    ): String {
        val bitmap = renderRidePoster(record, settings)
        val fileName = "habe-ride-${record.id}.png"
        saveBitmapToGallery(bitmap, fileName)
        return fileName
    }

    fun saveSpeedTestPosterToGallery(
        record: SpeedTestRecord,
        settings: PosterSettings = PosterSettings()
    ): String {
        val bitmap = renderSpeedTestPoster(record, settings)
        val fileName = "habe-speedtest-${record.id}.png"
        saveBitmapToGallery(bitmap, fileName)
        return fileName
    }

    fun createRideCsvShareIntent(record: RideHistoryRecord): Intent {
        val presentedRecord = presentRideHistoryRecord(record)
        val fileName = buildRideCsvFileName(presentedRecord)
        val csvUri = exportText(buildRideCsv(presentedRecord), fileName)
        AppLogger.i(TAG, "导出行程 CSV file=$fileName samples=${presentedRecord.samples.size}")
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, csvUri)
            putExtra(Intent.EXTRA_SUBJECT, "SmartDash ${record.title} 原始数据")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newUri(
                getApplication<Application>().contentResolver,
                "SmartDash ride csv",
                csvUri
            )
        }
        return Intent.createChooser(shareIntent, "导出行程原始数据")
    }

    private fun renderRidePoster(record: RideHistoryRecord, settings: PosterSettings): android.graphics.Bitmap {
        val presentedRecord = presentRideHistoryRecord(record)
        val template = PosterTemplates.byId(settings.defaultTemplate)
        val spec = posterFactory.buildRidePosterSpec(
            record = presentedRecord,
            aspectRatio = settings.defaultAspectRatio,
            template = template
        ).copy(
            options = posterFactory.buildRidePosterSpec(presentedRecord, settings.defaultAspectRatio, template).options.copy(
                showTrack = settings.showTrack,
                showWatermark = settings.showWatermark
            )
        )
        return PosterRendererV2(getApplication()).render(spec)
    }

    private fun renderSpeedTestPoster(record: SpeedTestRecord, settings: PosterSettings): android.graphics.Bitmap {
        val template = PosterTemplates.byId(settings.defaultTemplate)
        val spec = posterFactory.buildSpeedTestPosterSpec(
            record = record,
            aspectRatio = settings.defaultAspectRatio,
            template = template
        ).copy(
            options = posterFactory.buildSpeedTestPosterSpec(record, settings.defaultAspectRatio, template).options.copy(
                showTrack = settings.showTrack,
                showWatermark = settings.showWatermark
            )
        )
        return PosterRendererV2(getApplication()).render(spec)
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
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/SmartDash")
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
        return RideCsvExporter.build(
            normalizedSamples = normalizeRideEnergySamples(record),
            numberFormatter = ::csvNumber
        )
    }

    private fun csvNumber(value: Number?): String {
        if (value == null) return ""
        return String.format(Locale.US, "%.3f", value.toDouble())
    }

    private fun buildRideTitle(startedAtMs: Long): String {
        val date = Date(startedAtMs)
        return SimpleDateFormat("M月d日 HH:mm", Locale.getDefault()).format(date).withDisplaySpacing()
    }

    private fun buildRideCsvFileName(record: RideHistoryRecord): String {
        val datePart = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(record.startedAtMs))
        val timePart = SimpleDateFormat("HHmm", Locale.US).format(Date(record.startedAtMs))
        val durationPart = formatFileDuration(record.durationMs)
        val titlePart = sanitizeExportFileSegment(record.title)
        return buildString {
            append("smartdash-ride-")
            append(datePart)
            append('-')
            append(timePart)
            append('-')
            append(durationPart)
            if (titlePart.isNotBlank()) {
                append('-')
                append(titlePart)
            }
            append(".csv")
        }
    }

    private fun formatFileDuration(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0L) {
            String.format(Locale.US, "%02dh%02dm%02ds", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02dm%02ds", minutes, seconds)
        }
    }

    private fun sanitizeExportFileSegment(raw: String?): String {
        return raw
            ?.trim()
            ?.replace(Regex("[\\\\/:*?\"<>|]+"), "-")
            ?.replace(Regex("\\s+"), "-")
            ?.replace(Regex("-+"), "-")
            ?.trim('-')
            ?.take(48)
            .orEmpty()
    }

    private fun formatDuration(durationMs: Long): String {
        return String.format(Locale.getDefault(), "%.2f s", durationMs / 1000f)
    }

    fun addDashboardItem(type: com.shawnrain.sdash.data.MetricType) {
        val current = _dashboardItems.value.toMutableList()
        current.add(type)
        _dashboardItems.value = current
        viewModelScope.launch {
            settingsRepository.saveDashboardItems(current)
            syncScheduler.onSettingsChanged()
        }
    }

    fun removeDashboardItem(index: Int) {
        val current = _dashboardItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _dashboardItems.value = current
            viewModelScope.launch {
                settingsRepository.saveDashboardItems(current)
                syncScheduler.onSettingsChanged()
            }
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
                syncScheduler.onSettingsChanged()
            }
        }
    }

    fun addRideOverviewItem(type: com.shawnrain.sdash.data.MetricType) {
        val current = _rideOverviewItems.value.toMutableList()
        if (type in current) return
        current.add(type)
        _rideOverviewItems.value = current
        viewModelScope.launch {
            settingsRepository.saveRideOverviewItems(current)
            syncScheduler.onSettingsChanged()
        }
    }

    fun removeRideOverviewItem(type: com.shawnrain.sdash.data.MetricType) {
        val current = _rideOverviewItems.value.toMutableList()
        if (current.size <= 1) return
        if (current.remove(type)) {
            _rideOverviewItems.value = current
            viewModelScope.launch {
                settingsRepository.saveRideOverviewItems(current)
                syncScheduler.onSettingsChanged()
            }
        }
    }

    fun moveRideOverviewItem(from: Int, to: Int) {
        val current = _rideOverviewItems.value.toMutableList()
        if (from in current.indices && to in current.indices) {
            val item = current.removeAt(from)
            current.add(to, item)
            _rideOverviewItems.value = current
            viewModelScope.launch {
                settingsRepository.saveRideOverviewItems(current)
                syncScheduler.onSettingsChanged()
            }
        }
    }

    fun saveWheelCircumference(value: Float): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.saveWheelCircumference(value)
        syncScheduler.onSettingsChanged()
    }
    fun savePolePairs(value: Int): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.savePolePairs(value)
        syncScheduler.onSettingsChanged()
    }
    fun saveCurrentVehicleWheelArchive(rimSize: String? = null, tireSpecLabel: String? = null): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.saveCurrentVehicleWheelArchive(rimSize = rimSize, tireSpecLabel = tireSpecLabel)
        syncScheduler.onSettingsChanged()
    }
    fun saveControllerBrand(value: String): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.saveControllerBrand(value)
        syncScheduler.onSettingsChanged()
    }
    fun saveSpeedSource(source: SpeedSource): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.saveSpeedSource(source)
        syncScheduler.onSettingsChanged()
    }
    fun saveBattDataSource(source: DataSource): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.saveBattDataSource(source)
        syncScheduler.onSettingsChanged()
    }
    fun selectVehicle(id: String): kotlinx.coroutines.Job = viewModelScope.launch {
        autoReconnectAttemptedAddress = null
        settingsRepository.saveCurrentVehicleId(id)
        syncScheduler.onSettingsChanged()
        val vehicle = vehicleProfiles.value.firstOrNull { it.id == id } ?: return@launch
        _calibrationMessage.value = "已切换到 ${vehicle.name}"
    }
    fun saveVehicleProfile(profile: VehicleProfile): kotlinx.coroutines.Job = viewModelScope.launch {
        val existing = vehicleProfiles.value.any { it.id == profile.id }
        settingsRepository.upsertVehicleProfile(profile)
        syncScheduler.onVehicleProfileChanged(profile.id)
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
        syncScheduler.onVehicleProfileChanged(id)
        _calibrationMessage.value = "已删除车辆档案：${target.name}"
    }
    fun saveLogLevel(level: AppLogLevel): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.saveLogLevel(level)
        _calibrationMessage.value = "日志级别已切换为 ${level.name}"
    }
    fun saveOverlayEnabled(enabled: Boolean): kotlinx.coroutines.Job = viewModelScope.launch {
        settingsRepository.saveOverlayEnabled(enabled)
        syncScheduler.onSettingsChanged()
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
            ) { settingsRepository.exportBackupJson(rideHistoryRepository) }
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
                val importedCount = settingsRepository.importBackupJson(payload, rideHistoryRepository)
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
            // App 回到前台：立即尝试直连最后设备
            lastControllerDeviceAddress.value?.let { address ->
                autoConnectManager.onAppForeground()
                tryAutoReconnect(address, reason = "app-foreground")
            }
        } else {
            // App 进入后台：启动低功耗后台扫描
            autoConnectManager.onAppBackground()
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
    val controllerCapabilities: StateFlow<ControllerCapabilities?> = combine(
        ProtocolParser.activeProtocolId,
        _latestZhikeSettings
    ) { protocolId, zhikeSettings ->
        protocolId?.let { currentProtocolId ->
            ControllerCapabilities(
                protocol = currentProtocolId,
                firmwareVersion = if (currentProtocolId == "zhike") extractZhikeFirmwareVersion(zhikeSettings) else null,
                hardwareVersion = if (currentProtocolId == "zhike") extractZhikeHardwareVersion(zhikeSettings) else null,
                featureFlags = buildSet {
                    if (zhikeSettings?.loadedFromController == true) {
                        add("zhike_settings_loaded")
                    }
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

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
                name = device.safeNameOrNull(),
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

    // 智科写入状态机
    private val _zhikeWriteState = MutableStateFlow<ZhikeWriteState>(ZhikeWriteState.Idle)
    val zhikeWriteState: StateFlow<ZhikeWriteState> = _zhikeWriteState.asStateFlow()

    // 智科校验结果
    private val _zhikeValidationResult = MutableStateFlow<ZhikeSettingsValidator.ValidationResult?>(null)
    val zhikeValidationResult: StateFlow<ZhikeSettingsValidator.ValidationResult?> = _zhikeValidationResult.asStateFlow()

    // 写入前参数快照（用于回读核对）
    private var _preWriteSettingsSnapshot: ZhikeSettings? = null
    
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

        // 执行本地校验
        val validationResult = ZhikeSettingsValidator.validate(settings)
        _zhikeValidationResult.value = validationResult

        if (validationResult.hasBlockingIssues) {
            val errorSummary = validationResult.errors.joinToString("; ") { it.message }
            return "参数校验失败：$errorSummary"
        }

        // 保存写入前快照（用于回读核对）
        _preWriteSettingsSnapshot = _latestZhikeSettings.value?.copy()

        // 启动完整写入工作流
        viewModelScope.launch {
            executeZhikeWriteWorkflow(settings)
        }
        return null
    }

    /**
     * 执行完整的智科写入工作流
     * handshake → AA210001 → wait ready → write packets → wait ack → read back → verify
     * 带写入节流：写入进行中时重复调用会被忽略
     */
    private var _isWriteInProgress = false

    private suspend fun executeZhikeWriteWorkflow(settings: ZhikeSettings) {
        // 写入节流：防止重复触发
        if (_isWriteInProgress) {
            AppLogger.w("ZhikeWrite", "写入已在进行中，忽略本次请求")
            return
        }
        _isWriteInProgress = true

        try {
            // Phase 1: Handshake
            _zhikeWriteState.value = ZhikeWriteState.Handshaking
            bleManager.handshakeZhike()
            delay(300)

            // Phase 2: Enter write mode (AA210001)
            _zhikeWriteState.value = ZhikeWriteState.EnteringWriteMode
            bleManager.requestZhikeWriteMode()
            delay(400)

            // Phase 3: Ready to write
            _zhikeWriteState.value = ZhikeWriteState.ReadyToWrite
            delay(200)

            // Phase 4: Write packets
            _zhikeWriteState.value = ZhikeWriteState.WritingPackets
            val protocol = ZhikeProtocol()
            val data = protocol.encodeSettings(settings)
            bleManager.writeInPackets(data)
            delay(800)

            // Phase 5: Waiting for ACK
            _zhikeWriteState.value = ZhikeWriteState.WaitingWriteAck
            delay(500)

            // Phase 6: Verify by reading back
            _zhikeWriteState.value = ZhikeWriteState.Verifying
            delay(300)
            bleManager.handshakeZhike()
            delay(180)
            bleManager.sendZhikeAuxCommand("0102")
            delay(220)
            bleManager.sendZhikeMainCommand("AA110001")
            delay(600)

            // 回读核对
            val afterSettings = _latestZhikeSettings.value
            val beforeSettings = _preWriteSettingsSnapshot
            if (beforeSettings != null && afterSettings != null && afterSettings.loadedFromController) {
                val verifyResult = ZhikePostWriteVerifier.verify(
                    before = beforeSettings,
                    after = afterSettings,
                    modifiedKeys = listOf(
                        "bus_current", "phase_current", "motor_direction",
                        "under_voltage", "over_voltage", "weak_magnet_current",
                        "regen_current", "bluetooth_password", "pole_pairs",
                        "phase_shift_angle", "sensor_type", "hall_sequence"
                    )
                )
                if (!verifyResult.success) {
                    _zhikeWriteState.value = ZhikeWriteState.Failed(
                        reason = verifyResult.message,
                        phase = WriteFailurePhase.VERIFY_MISMATCH
                    )
                    delay(5000)
                    _zhikeWriteState.value = ZhikeWriteState.Idle
                    return
                }
                AppLogger.i("ZhikeWrite", "回读核对: ${verifyResult.message}")
            }

            _zhikeWriteState.value = ZhikeWriteState.Succeeded
            delay(2000)
            _zhikeWriteState.value = ZhikeWriteState.Idle
        } catch (e: Exception) {
            val phase = when {
                e.message?.contains("握手", ignoreCase = true) == true -> WriteFailurePhase.HANDSHAKE_FAILED
                e.message?.contains("写入模式", ignoreCase = true) == true -> WriteFailurePhase.ENTER_WRITE_MODE_TIMEOUT
                e.message?.contains("发送", ignoreCase = true) == true -> WriteFailurePhase.PACKET_SEND_FAILED
                e.message?.contains("ACK", ignoreCase = true) == true -> WriteFailurePhase.ACK_TIMEOUT
                e.message?.contains("拒绝", ignoreCase = true) == true -> WriteFailurePhase.CONTROLLER_REJECTED
                else -> WriteFailurePhase.UNKNOWN_ERROR
            }
            _zhikeWriteState.value = ZhikeWriteState.Failed(e.message ?: "未知错误", phase)
            AppLogger.e("ZhikeWrite", "写入流程异常: ${phase.label}", e)
            delay(5000)
            _zhikeWriteState.value = ZhikeWriteState.Idle
        } finally {
            _isWriteInProgress = false
        }
    }

    /**
     * 应用安全参数预设（保守默认值）
     */
    fun applySafeZhikePresets(): ZhikeSettings {
        val current = _latestZhikeSettings.value ?: ZhikeSettings()
        val safe = current.copy()

        // 保守安全值
        ZhikeParameterCatalog.findDefinition("bus_current")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(safe, def, 25.0).rawWords.copyInto(safe.rawWords)
        }
        ZhikeParameterCatalog.findDefinition("phase_current")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(safe, def, 80.0).rawWords.copyInto(safe.rawWords)
        }
        ZhikeParameterCatalog.findDefinition("weak_magnet_current")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(safe, def, 15.0).rawWords.copyInto(safe.rawWords)
        }
        ZhikeParameterCatalog.findDefinition("under_voltage")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(safe, def, 39.0).rawWords.copyInto(safe.rawWords)
        }
        ZhikeParameterCatalog.findDefinition("over_voltage")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(safe, def, 84.0).rawWords.copyInto(safe.rawWords)
        }
        ZhikeParameterCatalog.findDefinition("power_reduction_start_voltage")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(safe, def, 42.0).rawWords.copyInto(safe.rawWords)
        }
        ZhikeParameterCatalog.findDefinition("power_reduction_end_voltage")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(safe, def, 41.0).rawWords.copyInto(safe.rawWords)
        }
        ZhikeParameterCatalog.findDefinition("regenerative_voltage_limit")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(safe, def, 80.0).rawWords.copyInto(safe.rawWords)
        }

        safe.syncLegacyFieldsFromWords()
        return safe
    }

    /**
     * 恢复智科控制器出厂设置
     */
    fun resetZhikeFactorySettings() {
        viewModelScope.launch {
            try {
                bleManager.handshakeZhike()
                delay(300)
                // 发送恢复出厂设置命令 (具体命令字取决于协议文档)
                bleManager.sendZhikeMainCommand("AA150001")
                delay(500)
                // 重新读取参数
                readZhikeSettings()
            } catch (e: Exception) {
                AppLogger.e("ZhikeReset", "恢复出厂设置失败", e)
            }
        }
    }

    /**
     * 修改智科蓝牙密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 错误信息，null 表示成功
     */
    fun changeZhikePassword(oldPassword: Int, newPassword: Int): String? {
        if (activeProtocolId.value != "zhike") {
            return "当前未连接智科控制器"
        }
        if (newPassword !in 0..9999) {
            return "新密码必须是 0-9999 的数字"
        }
        if (oldPassword == newPassword) {
            return "新密码与旧密码相同"
        }

        val currentSettings = _latestZhikeSettings.value
        if (currentSettings == null || !currentSettings.loadedFromController) {
            return "请先刷新并读取控制器参数"
        }

        // 校验旧密码
        if (currentSettings.bluetoothPassword != oldPassword) {
            return "当前密码错误"
        }

        viewModelScope.launch {
            try {
                // 先握手
                bleManager.handshakeZhike()
                delay(300)

                // 修改密码需要写入参数，使用编码后的数据
                val protocol = ZhikeProtocol()
                val updatedSettings = currentSettings.copy(
                    bluetoothPassword = newPassword,
                    originalBluetoothPassword = newPassword
                )
                // 更新 rawWords 中的密码字段
                ZhikeParameterCatalog.findDefinition("bluetooth_password")?.let { def ->
                    ZhikeParameterCatalog.updateNumeric(updatedSettings, def, newPassword.toDouble())
                }
                val data = protocol.encodeSettings(updatedSettings)
                bleManager.writeInPackets(data)
                delay(800)

                // 重新读取确认
                readZhikeSettings()
            } catch (e: Exception) {
                AppLogger.e("ZhikePassword", "修改密码失败", e)
            }
        }
        return null
    }

    fun getActiveProtocolId(): String? = ProtocolParser.getActiveProtocolId()

    override fun onCleared() {
        AppLogger.i(TAG, "MainViewModel 被清理，清理 BLE 资源")
        // Disconnect BLE managers to prevent lingering connections
        bleManager.disconnect()
        bmsBleManager.disconnect()
        runCatching { lanBackupTransfer.stop() }
        headingTracker.stop()
        gpsTracker.stopTracking()
        super.onCleared()
    }

    private fun extractZhikeFirmwareVersion(settings: ZhikeSettings?): Int? {
        if (settings == null) return null
        // 低字节 Word 30 (官方协议 n = 255 & e[30])
        val word30 = settings.rawWords.getOrElse(30) { 0 }
        val ver = word30 and 0xFF
        return if (ver > 0) ver else null
    }

    private fun extractZhikeFirmwareVersionLabel(settings: ZhikeSettings?): String? {
        return settings?.firmwareVersion
    }

    private fun extractZhikeHardwareVersion(settings: ZhikeSettings?): String? {
        // 预留硬件版本入口，后续若协议补齐可直接接到 capability 模型。
        return null
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

    @SuppressLint("MissingPermission")
    private fun safeBluetoothDeviceName(device: BluetoothDevice): String? {
        return runCatching { device.name }.getOrNull()
    }
}

internal object RideCsvExporter {
    fun build(
        normalizedSamples: List<RideMetricSample>,
        numberFormatter: (Number?) -> String
    ): String {
        val header = RideMetricSampleSchema.csvColumns.joinToString(",") { it.header }
        val rows = buildList {
            add(header)
            normalizedSamples.forEach { sample ->
                add(
                    RideMetricSampleSchema.csvColumns.joinToString(",") { column ->
                        column.value(sample, numberFormatter)
                    }
                )
            }
        }
        return rows.joinToString("\n")
    }
}
