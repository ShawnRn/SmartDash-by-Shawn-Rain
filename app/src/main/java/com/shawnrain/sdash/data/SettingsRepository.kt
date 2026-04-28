package com.shawnrain.sdash.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.shawnrain.sdash.data.history.RideHistoryRecord
import com.shawnrain.sdash.data.history.RideHistoryRepository
import com.shawnrain.sdash.data.history.RideMetricSample
import com.shawnrain.sdash.data.history.RideMetricSampleSchema
import com.shawnrain.sdash.data.history.RideTrackPoint
import com.shawnrain.sdash.data.speedtest.SpeedTestRecord
import com.shawnrain.sdash.data.speedtest.SpeedTestTrackPoint
import com.shawnrain.sdash.data.sync.BackupPreview
import com.shawnrain.sdash.data.sync.BackupPreviewRide
import com.shawnrain.sdash.data.sync.BackupPreviewVehicle
import com.shawnrain.sdash.data.sync.BackupRetentionPolicy
import com.shawnrain.sdash.data.sync.DriveCurrentState
import com.shawnrain.sdash.data.sync.SyncRideSnapshot
import com.shawnrain.sdash.data.sync.SyncSettingsSnapshot
import com.shawnrain.sdash.data.sync.SyncSpeedTestSnapshot
import com.shawnrain.sdash.data.sync.SyncVehicleSettingsSnapshot
import com.shawnrain.sdash.data.sync.SyncVehicleProfileSnapshot
import com.shawnrain.sdash.debug.AppLogLevel
import com.shawnrain.sdash.debug.AppLogger
import com.shawnrain.sdash.ui.poster.PosterSettings
import com.shawnrain.sdash.ui.poster.PosterTemplates
import com.shawnrain.sdash.ui.poster.model.PosterAspectRatio
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "habe_settings")

enum class MetricType(val title: String, val unit: String) {
    SPEED("速度", "km/h"),
    GRADE("坡度", "%"),
    ALTITUDE("海拔高度", "m"),
    VOLTAGE("电压", "V"),
    VOLTAGE_SAG("压降", "V"),
    BUS_CURRENT("母线电流", "A"),
    PHASE_CURRENT("相电流", "A"),
    POWER("实时功率", "kW"),
    TEMP("控制器温度", "°C"),
    MAX_CONTROLLER_TEMP("控制器最高温度", "°C"),
    SOC("电量 (预估)", "%"),
    RANGE("剩余续航", "km"),
    RPM("转速", "rpm"),
    EFFICIENCY("平均能耗", "Wh/km"),
    TRIP_DISTANCE("本次里程", "km"),
    TOTAL_ENERGY("总能耗", "Wh"),
    PEAK_REGEN_POWER("最大回收功率", "W"),
    RECOVERED_ENERGY("总回收能量", "Wh"),
    MEDIA_CONTROL("媒体控制", "")
}

enum class SpeedSource(val title: String) {
    CONTROLLER("控制器蓝牙"),
    GPS("手机 GPS")
}

enum class DataSource(val title: String) {
    CONTROLLER("主控制器"),
    BMS("独立 BMS")
}

class SettingsRepository(private val context: Context) {
    data class LegacyRideHistoryPayload(
        val vehicleId: String,
        val records: List<RideHistoryRecord>
    )

    companion object {
        private const val SYNC_META_PREFIX = "__sync_mtime__"
        val CURRENT_VEHICLE_ID = stringPreferencesKey("current_vehicle_id")
        val VEHICLE_LIST = stringPreferencesKey("vehicle_list_json")
        val LAST_CONTROLLER_DEVICE_ADDRESS = stringPreferencesKey("last_controller_device_address")
        val LAST_CONTROLLER_DEVICE_NAME = stringPreferencesKey("last_controller_device_name")
        val LAST_CONTROLLER_PROTOCOL_ID = stringPreferencesKey("last_controller_protocol_id")
        
        private fun vKey(id: String, key: String) = stringPreferencesKey("v_${id}_$key")
        private fun vKeyF(id: String, key: String) = floatPreferencesKey("v_${id}_$key")
        private fun vKeyI(id: String, key: String) = intPreferencesKey("v_${id}_$key")
        
        const val K_WHEEL = "wheel"
        const val K_POLE = "pole"
        const val K_BRAND = "brand"
        const val K_SPEED_SRC = "speed_src"
        const val K_BATT_SRC = "batt_src"
        const val K_DASH_ITEMS = "dash_items"
        const val K_RIDE_OVERVIEW_ITEMS = "ride_overview_items"
        const val K_SPEEDTEST_HISTORY = "speedtest_history"
        const val K_RIDE_HISTORY = "ride_history"
        const val K_LAST_CONTROLLER_DEVICE_ADDRESS = "last_controller_device_address"
        const val K_LAST_CONTROLLER_DEVICE_NAME = "last_controller_device_name"
        const val K_LAST_CONTROLLER_PROTOCOL_ID = "last_controller_protocol_id"
        const val K_POSTER_TEMPLATE = "poster_template"
        const val K_POSTER_ASPECT_RATIO = "poster_aspect_ratio"
        const val K_POSTER_SHOW_TRACK = "poster_show_track"
        const val K_POSTER_SHOW_WATERMARK = "poster_show_watermark"
        val LOG_LEVEL = stringPreferencesKey("log_level")
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val DRIVE_BACKUP_RETENTION = stringPreferencesKey("drive_backup_retention")
        val AUTO_RIDE_STOP_ENABLED = booleanPreferencesKey("auto_ride_stop_enabled")
        val AUTO_RIDE_STOP_DELAY_SECONDS = intPreferencesKey("auto_ride_stop_delay_seconds")
        val RIDE_HISTORY_NORMALIZATION_VERSION = intPreferencesKey("ride_history_normalization_version")
        val RIDE_HISTORY_STORAGE_MIGRATION_VERSION = intPreferencesKey("ride_history_storage_migration_version")

        private fun syncMetaName(name: String): String = "$SYNC_META_PREFIX$name"
        private fun syncMetaKey(name: String) = longPreferencesKey(syncMetaName(name))
    }

    private fun loadVehicleProfiles(raw: String?): List<VehicleProfile> {
        return VehicleProfile.listFromJson(raw)
            .distinctBy { it.id }
            .ifEmpty { listOf(VehicleProfile.default()) }
    }

    private fun <T> Preferences.safeGet(key: Preferences.Key<T>): T? =
        runCatching { this[key] }.getOrNull()

    private fun markSyncedAt(
        pref: androidx.datastore.preferences.core.MutablePreferences,
        vararg names: String,
        updatedAt: Long = System.currentTimeMillis()
    ) {
        names.distinct().forEach { name ->
            pref[syncMetaKey(name)] = updatedAt
        }
    }

    private fun readLocalModifiedAt(
        pref: Preferences,
        name: String
    ): Long = pref.safeGet(syncMetaKey(name)) ?: 0L

    private fun remoteModifiedAt(
        remoteEntriesByName: Map<String, BackupEntry>,
        name: String
    ): Long {
        val metaName = syncMetaName(name)
        val meta = remoteEntriesByName[metaName] ?: return 0L
        return when (meta.kind) {
            BackupValueKind.LONG -> meta.value as Long
            BackupValueKind.INT -> (meta.value as Int).toLong()
            BackupValueKind.DOUBLE -> (meta.value as Double).toLong()
            BackupValueKind.FLOAT -> (meta.value as Float).toLong()
            else -> 0L
        }
    }

    private fun finiteOr(value: Float, default: Float): Float =
        if (value.isFinite()) value else default

    private fun normalizedProfile(
        profile: VehicleProfile,
        updatedAt: Long? = null
    ): VehicleProfile {
        val resolvedLastModified = updatedAt ?: profile.lastModified.takeIf { it > 0L } ?: System.currentTimeMillis()

        val safeBatteryCapacityAh = finiteOr(profile.batteryCapacityAh, 50f).coerceAtLeast(1f)
        val safeWheelCircumferenceMm = finiteOr(profile.wheelCircumferenceMm, 1800f).coerceIn(500f, 5000f)
        val safeTotalMileageKm = finiteOr(profile.totalMileageKm, 0f).coerceAtLeast(0f)
        val safeLearnedInternalResistanceOhm = finiteOr(profile.learnedInternalResistanceOhm, 0f).coerceAtLeast(0f)
        val safeLearnedEfficiencyWhKm = finiteOr(profile.learnedEfficiencyWhKm, 0f).coerceAtLeast(0f)
        val safeLearnedUsableEnergyRatio = finiteOr(profile.learnedUsableEnergyRatio, 0.9f).coerceIn(0.72f, 0.98f)

        return profile.copy(
            name = profile.name.trim().ifBlank { "未命名车辆" },
            macAddress = profile.macAddress.trim(),
            batterySeries = profile.batterySeries.coerceAtLeast(1),
            batteryCapacityAh = safeBatteryCapacityAh,
            wheelCircumferenceMm = safeWheelCircumferenceMm,
            wheelRimSize = profile.wheelRimSize.trim().ifBlank { "10寸" },
            tireSpecLabel = profile.tireSpecLabel.trim(),
            polePairs = profile.polePairs.coerceAtLeast(1),
            totalMileageKm = safeTotalMileageKm,
            learnedInternalResistanceOhm = safeLearnedInternalResistanceOhm,
            learnedEfficiencyWhKm = safeLearnedEfficiencyWhKm,
            learnedUsableEnergyRatio = safeLearnedUsableEnergyRatio,
            lastModified = resolvedLastModified
        )
    }

    private val preferencesFlow: Flow<Preferences> = context.dataStore.data
        .catch { emit(emptyPreferences()) }

    val vehicleProfiles: Flow<List<VehicleProfile>> = preferencesFlow.map { pref ->
        loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
    }.distinctUntilChanged()

    private val storedCurrentVehicleId: Flow<String?> = preferencesFlow.map { pref ->
        pref.safeGet(CURRENT_VEHICLE_ID)
    }.distinctUntilChanged()

    val currentVehicleId: Flow<String> = combine(
        vehicleProfiles,
        storedCurrentVehicleId
    ) { profiles, storedId ->
        profiles.firstOrNull { it.id == storedId }?.id ?: profiles.first().id
    }.distinctUntilChanged()

    val currentVehicleProfile: Flow<VehicleProfile> = combine(
        vehicleProfiles,
        currentVehicleId
    ) { profiles, currentId ->
        profiles.firstOrNull { it.id == currentId } ?: profiles.first()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val wheelCircumference: Flow<Float> = combine(
        currentVehicleProfile,
        preferencesFlow
    ) { currentProfile, pref ->
        currentProfile.wheelCircumferenceMm
                .takeIf { it in 500f..5000f }
                ?: pref.safeGet(vKeyF(currentProfile.id, K_WHEEL))
                ?: 1800f
    }.distinctUntilChanged()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val polePairs: Flow<Int> = combine(
        currentVehicleProfile,
        preferencesFlow
    ) { currentProfile, pref ->
        currentProfile.polePairs.takeIf { it > 0 } ?: pref.safeGet(vKeyI(currentProfile.id, K_POLE)) ?: 50
    }.distinctUntilChanged()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val controllerBrand: Flow<String> = currentVehicleId.flatMapLatest { id ->
        preferencesFlow.map { it.safeGet(vKey(id, K_BRAND)) ?: "auto" }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val speedSource: Flow<SpeedSource> = currentVehicleId.flatMapLatest { id ->
        preferencesFlow.map { pref ->
            val name = pref.safeGet(vKey(id, K_SPEED_SRC)) ?: SpeedSource.CONTROLLER.name
            try { SpeedSource.valueOf(name) } catch(e: Exception) { SpeedSource.CONTROLLER }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val battDataSource: Flow<DataSource> = currentVehicleId.flatMapLatest { id ->
        preferencesFlow.map { pref ->
            val name = pref.safeGet(vKey(id, K_BATT_SRC)) ?: DataSource.CONTROLLER.name
            try { DataSource.valueOf(name) } catch(e: Exception) { DataSource.CONTROLLER }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val dashboardItems: Flow<List<MetricType>> = currentVehicleId.flatMapLatest { id ->
        preferencesFlow.map { pref ->
            val raw = pref.safeGet(vKey(id, K_DASH_ITEMS))
            val resolved = if (raw.isNullOrEmpty()) {
                listOf(MetricType.SOC, MetricType.RANGE, MetricType.POWER, MetricType.MEDIA_CONTROL)
            } else {
                raw.split(",").mapNotNull { try { MetricType.valueOf(it) } catch (e: Exception) { null } }
            }
            resolved.ifEmpty { listOf(MetricType.SOC, MetricType.RANGE, MetricType.POWER, MetricType.EFFICIENCY) }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val rideOverviewItems: Flow<List<MetricType>> = currentVehicleId.flatMapLatest { id ->
        preferencesFlow.map { pref ->
            val raw = pref.safeGet(vKey(id, K_RIDE_OVERVIEW_ITEMS))
            val parsed = raw
                ?.split(",")
                ?.mapNotNull { token ->
                    try {
                        MetricType.valueOf(token)
                    } catch (_: Exception) {
                        null
                    }
                }
                .orEmpty()
            if (parsed.isNotEmpty()) parsed else MetricType.entries.toList()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val speedTestHistory: Flow<List<SpeedTestRecord>> = currentVehicleId.flatMapLatest { id ->
        preferencesFlow.map { pref ->
            SpeedTestRecord.listFromJson(pref.safeGet(vKey(id, K_SPEEDTEST_HISTORY)))
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val rideHistory: Flow<List<RideHistoryRecord>> = currentVehicleId.flatMapLatest { id ->
        preferencesFlow.map { pref ->
            RideHistoryRecord.listFromJson(pref.safeGet(vKey(id, K_RIDE_HISTORY)))
        }
    }

    val logLevel: Flow<AppLogLevel> = preferencesFlow.map { pref ->
        AppLogLevel.fromName(pref.safeGet(LOG_LEVEL) ?: AppLogLevel.DEBUG.name)
    }.distinctUntilChanged()

    val overlayEnabled: Flow<Boolean> = preferencesFlow.map { pref ->
        pref.safeGet(OVERLAY_ENABLED) ?: false
    }.distinctUntilChanged()

    val driveBackupRetentionPolicy: Flow<BackupRetentionPolicy> = preferencesFlow.map { pref ->
        BackupRetentionPolicy.fromName(pref.safeGet(DRIVE_BACKUP_RETENTION))
    }.distinctUntilChanged()

    val autoRideStopEnabled: Flow<Boolean> = preferencesFlow.map { pref ->
        pref.safeGet(AUTO_RIDE_STOP_ENABLED) ?: true
    }.distinctUntilChanged()

    val autoRideStopDelaySeconds: Flow<Int> = preferencesFlow.map { pref ->
        (pref.safeGet(AUTO_RIDE_STOP_DELAY_SECONDS) ?: 75).coerceIn(15, 600)
    }.distinctUntilChanged()

    val rideHistoryNormalizationVersion: Flow<Int> = preferencesFlow.map { pref ->
        pref.safeGet(RIDE_HISTORY_NORMALIZATION_VERSION) ?: 0
    }.distinctUntilChanged()

    val posterSettings: Flow<PosterSettings> = preferencesFlow.map { pref ->
        val defaults = PosterSettings()
        val aspectRatio = pref.safeGet(stringPreferencesKey(K_POSTER_ASPECT_RATIO))
            ?.let { runCatching { PosterAspectRatio.valueOf(it) }.getOrNull() }
            ?: defaults.defaultAspectRatio
        val templateId = pref.safeGet(stringPreferencesKey(K_POSTER_TEMPLATE))
            ?.takeIf { it.isNotBlank() }
            ?.let { PosterTemplates.byId(it).id }
            ?: defaults.defaultTemplate
        PosterSettings(
            defaultAspectRatio = aspectRatio,
            defaultTemplate = templateId,
            showTrack = pref.safeGet(booleanPreferencesKey(K_POSTER_SHOW_TRACK)) ?: defaults.showTrack,
            showWatermark = pref.safeGet(booleanPreferencesKey(K_POSTER_SHOW_WATERMARK)) ?: defaults.showWatermark
        )
    }

    val lastControllerDeviceAddress: Flow<String?> = combine(
        preferencesFlow,
        currentVehicleId
    ) { pref, currentId ->
        pref.safeGet(vKey(currentId, K_LAST_CONTROLLER_DEVICE_ADDRESS))
            ?.takeIf { it.isNotBlank() }
            ?: pref.safeGet(LAST_CONTROLLER_DEVICE_ADDRESS)?.takeIf { it.isNotBlank() }
    }.distinctUntilChanged()

    val lastControllerDeviceName: Flow<String?> = combine(
        preferencesFlow,
        currentVehicleId
    ) { pref, currentId ->
        pref.safeGet(vKey(currentId, K_LAST_CONTROLLER_DEVICE_NAME))
            ?.takeIf { it.isNotBlank() }
            ?: pref.safeGet(LAST_CONTROLLER_DEVICE_NAME)?.takeIf { it.isNotBlank() }
    }.distinctUntilChanged()

    val lastControllerProtocolId: Flow<String?> = combine(
        preferencesFlow,
        currentVehicleId
    ) { pref, currentId ->
        pref.safeGet(vKey(currentId, K_LAST_CONTROLLER_PROTOCOL_ID))
            ?.takeIf { it.isNotBlank() }
            ?: pref.safeGet(LAST_CONTROLLER_PROTOCOL_ID)?.takeIf { it.isNotBlank() }
    }.distinctUntilChanged()

    suspend fun saveCurrentVehicleId(id: String) = context.dataStore.edit { pref ->
        val profiles = loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
        pref[CURRENT_VEHICLE_ID] = profiles.firstOrNull { it.id == id }?.id ?: profiles.first().id
        markSyncedAt(pref, CURRENT_VEHICLE_ID.name)
    }

    suspend fun saveVehicleProfiles(profiles: List<VehicleProfile>) {
        val sanitized = profiles
            .map { profile -> normalizedProfile(profile) }
            .distinctBy { it.id }
            .ifEmpty { listOf(VehicleProfile.default()) }
        runCatching {
            context.dataStore.edit { pref ->
                pref[VEHICLE_LIST] = VehicleProfile.listToJson(sanitized)
                val currentId = pref.safeGet(CURRENT_VEHICLE_ID)
                pref[CURRENT_VEHICLE_ID] = sanitized.firstOrNull { it.id == currentId }?.id ?: sanitized.first().id
                markSyncedAt(pref, VEHICLE_LIST.name, CURRENT_VEHICLE_ID.name)
            }
        }.onFailure { error ->
            AppLogger.e("SettingsRepository", "saveVehicleProfiles failed", error)
        }
    }

    suspend fun upsertVehicleProfile(profile: VehicleProfile) {
        val current = vehicleProfiles.first().toMutableList()
        val normalized = normalizedProfile(profile, updatedAt = System.currentTimeMillis())
        val existingIndex = current.indexOfFirst { it.id == normalized.id }
        if (existingIndex >= 0) {
            current[existingIndex] = normalized
        } else {
            current.add(0, normalized)
        }
        saveVehicleProfiles(current)
        saveCurrentVehicleId(normalized.id)
    }

    suspend fun deleteVehicleProfile(id: String) {
        val updated = vehicleProfiles.first().filterNot { it.id == id }
        saveVehicleProfiles(updated)
    }

    suspend fun updateCurrentVehicle(transform: (VehicleProfile) -> VehicleProfile) {
        val currentId = currentVehicleId.first()
        val profiles = vehicleProfiles.first().toMutableList()
        val currentIndex = profiles.indexOfFirst { it.id == currentId }.takeIf { it >= 0 } ?: 0
        profiles[currentIndex] = normalizedProfile(
            transform(profiles[currentIndex]),
            updatedAt = System.currentTimeMillis()
        )
        saveVehicleProfiles(profiles)
    }

    suspend fun incrementCurrentVehicleMileage(distanceKm: Float) {
        if (distanceKm <= 0f) return
        updateCurrentVehicle { profile ->
            profile.copy(totalMileageKm = profile.totalMileageKm + distanceKm)
        }
    }

    suspend fun saveCurrentVehicleLearnedInternalResistance(valueOhm: Float) {
        updateCurrentVehicle { profile ->
            profile.copy(learnedInternalResistanceOhm = valueOhm.coerceAtLeast(0f))
        }
    }

    suspend fun saveWheelCircumference(value: Float) {
        val id = currentVehicleId.first()
        val normalized = value.coerceIn(500f, 5000f)
        context.dataStore.edit {
            it[vKeyF(id, K_WHEEL)] = normalized
            markSyncedAt(it, "v_${id}_$K_WHEEL")
        }
        updateCurrentVehicle { profile ->
            profile.copy(wheelCircumferenceMm = normalized)
        }
    }

    suspend fun savePolePairs(value: Int) {
        val id = currentVehicleId.first()
        val normalized = value.coerceAtLeast(1)
        context.dataStore.edit {
            it[vKeyI(id, K_POLE)] = normalized
            markSyncedAt(it, "v_${id}_$K_POLE")
        }
        updateCurrentVehicle { profile ->
            profile.copy(polePairs = normalized)
        }
    }

    suspend fun saveCurrentVehicleWheelArchive(rimSize: String? = null, tireSpecLabel: String? = null) {
        updateCurrentVehicle { profile ->
            profile.copy(
                wheelRimSize = rimSize?.trim()?.ifBlank { profile.wheelRimSize } ?: profile.wheelRimSize,
                tireSpecLabel = tireSpecLabel?.trim() ?: profile.tireSpecLabel
            )
        }
    }

    suspend fun saveControllerBrand(value: String) {
        val id = currentVehicleId.first()
        context.dataStore.edit {
            it[vKey(id, K_BRAND)] = value
            markSyncedAt(it, "v_${id}_$K_BRAND")
        }
    }

    suspend fun saveSpeedSource(source: SpeedSource) {
        val id = currentVehicleId.first()
        context.dataStore.edit {
            it[vKey(id, K_SPEED_SRC)] = source.name
            markSyncedAt(it, "v_${id}_$K_SPEED_SRC")
        }
    }

    suspend fun saveBattDataSource(source: DataSource) {
        val id = currentVehicleId.first()
        context.dataStore.edit {
            it[vKey(id, K_BATT_SRC)] = source.name
            markSyncedAt(it, "v_${id}_$K_BATT_SRC")
        }
    }

    suspend fun saveDashboardItems(items: List<MetricType>) {
        val id = currentVehicleId.first()
        context.dataStore.edit {
            it[vKey(id, K_DASH_ITEMS)] = items.joinToString(",") { item -> item.name }
            markSyncedAt(it, "v_${id}_$K_DASH_ITEMS")
        }
    }

    suspend fun saveDriveBackupRetentionPolicy(policy: BackupRetentionPolicy) {
        context.dataStore.edit {
            it[DRIVE_BACKUP_RETENTION] = policy.name
            markSyncedAt(it, DRIVE_BACKUP_RETENTION.name)
        }
    }

    suspend fun saveAutoRideStopEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[AUTO_RIDE_STOP_ENABLED] = enabled
            markSyncedAt(it, AUTO_RIDE_STOP_ENABLED.name)
        }
    }

    suspend fun saveAutoRideStopDelaySeconds(seconds: Int) {
        context.dataStore.edit {
            it[AUTO_RIDE_STOP_DELAY_SECONDS] = seconds.coerceIn(15, 600)
            markSyncedAt(it, AUTO_RIDE_STOP_DELAY_SECONDS.name)
        }
    }

    suspend fun saveRideOverviewItems(items: List<MetricType>) {
        val id = currentVehicleId.first()
        context.dataStore.edit {
            it[vKey(id, K_RIDE_OVERVIEW_ITEMS)] = items.joinToString(",") { item -> item.name }
            markSyncedAt(it, "v_${id}_$K_RIDE_OVERVIEW_ITEMS")
        }
    }

    suspend fun saveSpeedTestHistory(records: List<SpeedTestRecord>) {
        val id = currentVehicleId.first()
        context.dataStore.edit {
            it[vKey(id, K_SPEEDTEST_HISTORY)] = SpeedTestRecord.listToJson(records)
            markSyncedAt(it, "v_${id}_$K_SPEEDTEST_HISTORY")
        }
    }

    suspend fun saveRideHistory(records: List<RideHistoryRecord>) {
        val id = currentVehicleId.first()
        context.dataStore.edit {
            it[vKey(id, K_RIDE_HISTORY)] = RideHistoryRecord.listToJson(records)
            markSyncedAt(it, "v_${id}_$K_RIDE_HISTORY")
        }
    }

    suspend fun saveRideHistoryForVehicle(vehicleId: String, records: List<RideHistoryRecord>) {
        context.dataStore.edit {
            it[vKey(vehicleId, K_RIDE_HISTORY)] = RideHistoryRecord.listToJson(records)
            markSyncedAt(it, "v_${vehicleId}_$K_RIDE_HISTORY")
        }
    }

    suspend fun saveSpeedTestHistoryForVehicle(vehicleId: String, records: List<SpeedTestRecord>) {
        context.dataStore.edit {
            it[vKey(vehicleId, K_SPEEDTEST_HISTORY)] = SpeedTestRecord.listToJson(records)
            markSyncedAt(it, "v_${vehicleId}_$K_SPEEDTEST_HISTORY")
        }
    }

    suspend fun saveLogLevel(level: AppLogLevel) {
        if ((preferencesFlow.first().safeGet(LOG_LEVEL) ?: AppLogLevel.DEBUG.name) == level.name) {
            return
        }
        context.dataStore.edit {
            it[LOG_LEVEL] = level.name
            markSyncedAt(it, LOG_LEVEL.name)
        }
    }

    suspend fun saveOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[OVERLAY_ENABLED] = enabled
            markSyncedAt(it, OVERLAY_ENABLED.name)
        }
    }

    suspend fun saveRideHistoryNormalizationVersion(version: Int) {
        context.dataStore.edit {
            it[RIDE_HISTORY_NORMALIZATION_VERSION] = version
        }
    }

    suspend fun getRideHistoryStorageMigrationVersion(): Int =
        preferencesFlow.first().safeGet(RIDE_HISTORY_STORAGE_MIGRATION_VERSION) ?: 0

    suspend fun saveRideHistoryStorageMigrationVersion(version: Int) {
        context.dataStore.edit {
            it[RIDE_HISTORY_STORAGE_MIGRATION_VERSION] = version
        }
    }

    suspend fun listLegacyRideHistoryPayloads(): List<LegacyRideHistoryPayload> {
        val pref = preferencesFlow.first()
        return loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
            .map { profile ->
                LegacyRideHistoryPayload(
                    vehicleId = profile.id,
                    records = RideHistoryRecord.listFromJson(pref.safeGet(vKey(profile.id, K_RIDE_HISTORY)))
                )
            }
            .filter { it.records.isNotEmpty() }
    }

    suspend fun clearLegacyRideHistoryForVehicle(vehicleId: String) {
        context.dataStore.edit {
            it.remove(vKey(vehicleId, K_RIDE_HISTORY))
            markSyncedAt(it, "v_${vehicleId}_$K_RIDE_HISTORY")
        }
    }

    suspend fun savePosterSettings(settings: PosterSettings) {
        context.dataStore.edit {
            it[stringPreferencesKey(K_POSTER_TEMPLATE)] = PosterTemplates.byId(settings.defaultTemplate).id
            it[stringPreferencesKey(K_POSTER_ASPECT_RATIO)] = settings.defaultAspectRatio.name
            it[booleanPreferencesKey(K_POSTER_SHOW_TRACK)] = settings.showTrack
            it[booleanPreferencesKey(K_POSTER_SHOW_WATERMARK)] = settings.showWatermark
            markSyncedAt(
                it,
                K_POSTER_TEMPLATE,
                K_POSTER_ASPECT_RATIO,
                K_POSTER_SHOW_TRACK,
                K_POSTER_SHOW_WATERMARK
            )
        }
    }

    suspend fun buildSyncVehicleSettingsSnapshots(deviceId: String): List<SyncVehicleSettingsSnapshot> {
        val pref = preferencesFlow.first()
        return loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
            .distinctBy { it.id }
            .map { profile ->
                val vehicleId = profile.id
                val updatedAt = listOf(
                    readLocalModifiedAt(pref, "v_${vehicleId}_$K_SPEED_SRC"),
                    readLocalModifiedAt(pref, "v_${vehicleId}_$K_BATT_SRC"),
                    readLocalModifiedAt(pref, "v_${vehicleId}_$K_WHEEL"),
                    readLocalModifiedAt(pref, "v_${vehicleId}_$K_POLE"),
                    readLocalModifiedAt(pref, "v_${vehicleId}_$K_BRAND"),
                    readLocalModifiedAt(pref, "v_${vehicleId}_$K_LAST_CONTROLLER_DEVICE_ADDRESS"),
                    readLocalModifiedAt(pref, "v_${vehicleId}_$K_LAST_CONTROLLER_DEVICE_NAME"),
                    readLocalModifiedAt(pref, "v_${vehicleId}_$K_LAST_CONTROLLER_PROTOCOL_ID"),
                    readLocalModifiedAt(pref, "v_${vehicleId}_$K_DASH_ITEMS"),
                    readLocalModifiedAt(pref, "v_${vehicleId}_$K_RIDE_OVERVIEW_ITEMS")
                ).maxOrNull() ?: 0L
                SyncVehicleSettingsSnapshot(
                    vehicleProfileId = vehicleId,
                    speedSource = (pref.safeGet(vKey(vehicleId, K_SPEED_SRC)) ?: SpeedSource.CONTROLLER.name),
                    battDataSource = (pref.safeGet(vKey(vehicleId, K_BATT_SRC)) ?: DataSource.CONTROLLER.name),
                    wheelCircumferenceMm = (pref.safeGet(vKeyF(vehicleId, K_WHEEL))
                        ?: profile.wheelCircumferenceMm.takeIf { it > 0f }
                        ?: 1800f),
                    polePairs = (pref.safeGet(vKeyI(vehicleId, K_POLE))
                        ?: profile.polePairs.takeIf { it > 0 }
                        ?: 50),
                    controllerBrand = pref.safeGet(vKey(vehicleId, K_BRAND)) ?: "auto",
                    lastControllerDeviceAddress = pref.safeGet(vKey(vehicleId, K_LAST_CONTROLLER_DEVICE_ADDRESS)).orEmpty(),
                    lastControllerDeviceName = pref.safeGet(vKey(vehicleId, K_LAST_CONTROLLER_DEVICE_NAME)).orEmpty(),
                    lastControllerProtocolId = pref.safeGet(vKey(vehicleId, K_LAST_CONTROLLER_PROTOCOL_ID)).orEmpty(),
                    dashboardItems = parseMetricNames(pref.safeGet(vKey(vehicleId, K_DASH_ITEMS))),
                    rideOverviewItems = parseMetricNames(pref.safeGet(vKey(vehicleId, K_RIDE_OVERVIEW_ITEMS))),
                    updatedAt = updatedAt,
                    updatedByDeviceId = deviceId
                )
            }
    }

    suspend fun applyDriveSyncState(state: DriveCurrentState) {
        applyDriveSyncState(state, RideHistoryRepository(context))
    }

    suspend fun applyDriveSyncState(
        state: DriveCurrentState,
        rideHistoryRepository: RideHistoryRepository
    ) {
        val sanitizedProfiles = state.vehicleProfiles
            .map { it.toVehicleProfile() }
            .distinctBy { it.id }
            .ifEmpty { listOf(VehicleProfile.default()) }
        val ridesByVehicle = state.rides.groupBy { it.vehicleProfileId.ifBlank { state.activeVehicleProfileId } }
        val speedTestsByVehicle = state.speedTests.groupBy { it.vehicleProfileId.ifBlank { state.activeVehicleProfileId } }
        val resolvedCurrentVehicleId = state.activeVehicleProfileId
            .takeIf { candidate -> sanitizedProfiles.any { it.id == candidate } }
            ?: sanitizedProfiles.first().id

        context.dataStore.edit { pref ->
            pref[VEHICLE_LIST] = VehicleProfile.listToJson(sanitizedProfiles)
            pref[CURRENT_VEHICLE_ID] = resolvedCurrentVehicleId
            markSyncedAt(pref, VEHICLE_LIST.name, CURRENT_VEHICLE_ID.name)

            if (state.vehicleSettings.isNotEmpty()) {
                val validVehicleSettings = state.vehicleSettings
                    .filter { snapshot -> sanitizedProfiles.any { it.id == snapshot.vehicleProfileId } }
                validVehicleSettings.forEach { snapshot ->
                    applyVehicleSyncSettingsLocked(pref, snapshot)
                }
                validVehicleSettings
                    .firstOrNull { it.vehicleProfileId == resolvedCurrentVehicleId }
                    ?.let { snapshot ->
                        applyCurrentVehicleControllerSyncLocked(pref, snapshot)
                    }
                applyGlobalSyncSettingsLocked(pref, state.settings)
            } else {
                applySyncSettingsLocked(pref, resolvedCurrentVehicleId, state.settings)
            }

            sanitizedProfiles.forEach { profile ->
                val vehicleId = profile.id
                val localSpeedRecords = SpeedTestRecord.listFromJson(pref.safeGet(vKey(vehicleId, K_SPEEDTEST_HISTORY)))
                val speedRecords = mergeSyncedSpeedTestRecords(
                    local = localSpeedRecords,
                    remote = speedTestsByVehicle[vehicleId]
                    .orEmpty()
                    .map { it.toSpeedTestRecord() }
                )
                    .sortedByDescending { it.timestampMs }
                pref[vKey(vehicleId, K_SPEEDTEST_HISTORY)] = SpeedTestRecord.listToJson(speedRecords)
                markSyncedAt(pref, "v_${vehicleId}_$K_SPEEDTEST_HISTORY")
            }

            ensureVehiclePreferences(pref)
        }

        sanitizedProfiles.forEach { profile ->
            val vehicleId = profile.id
            val localRideRecords = rideHistoryRepository.listRideHistoryRecords(vehicleId)
            val rideRecords = mergeSyncedRideHistoryRecords(
                local = localRideRecords,
                remote = ridesByVehicle[vehicleId]
                    .orEmpty()
                    .map { it.toRideHistoryRecord() }
            ).sortedByDescending { it.startedAtMs }
            rideHistoryRepository.replaceRidesForVehicle(vehicleId, rideRecords)
        }
    }

    suspend fun isOverlayEnabled(): Boolean {
        return preferencesFlow.first().safeGet(OVERLAY_ENABLED) ?: false
    }

    suspend fun saveLastControllerDevice(address: String, name: String?) {
        saveLastControllerProfile(address = address, name = name, protocolId = null)
    }

    suspend fun saveLastControllerProfile(address: String, name: String?, protocolId: String?) {
        val id = currentVehicleId.first()
        context.dataStore.edit {
            it[vKey(id, K_LAST_CONTROLLER_DEVICE_ADDRESS)] = address
            it[LAST_CONTROLLER_DEVICE_ADDRESS] = address
            updateVehicleMacAddressLocked(it, id, address)
            val touchedKeys = mutableListOf(
                "v_${id}_$K_LAST_CONTROLLER_DEVICE_ADDRESS",
                LAST_CONTROLLER_DEVICE_ADDRESS.name
            )
            if (!name.isNullOrBlank()) {
                it[vKey(id, K_LAST_CONTROLLER_DEVICE_NAME)] = name
                it[LAST_CONTROLLER_DEVICE_NAME] = name
                touchedKeys += "v_${id}_$K_LAST_CONTROLLER_DEVICE_NAME"
                touchedKeys += LAST_CONTROLLER_DEVICE_NAME.name
            }
            if (!protocolId.isNullOrBlank()) {
                it[vKey(id, K_LAST_CONTROLLER_PROTOCOL_ID)] = protocolId
                it[LAST_CONTROLLER_PROTOCOL_ID] = protocolId
                touchedKeys += "v_${id}_$K_LAST_CONTROLLER_PROTOCOL_ID"
                touchedKeys += LAST_CONTROLLER_PROTOCOL_ID.name
            }
            markSyncedAt(it, *touchedKeys.toTypedArray())
        }
    }

    suspend fun clearLastControllerDevice() {
        val id = currentVehicleId.first()
        context.dataStore.edit {
            it.remove(vKey(id, K_LAST_CONTROLLER_DEVICE_ADDRESS))
            it.remove(vKey(id, K_LAST_CONTROLLER_DEVICE_NAME))
            it.remove(vKey(id, K_LAST_CONTROLLER_PROTOCOL_ID))
            it.remove(LAST_CONTROLLER_DEVICE_ADDRESS)
            it.remove(LAST_CONTROLLER_DEVICE_NAME)
            it.remove(LAST_CONTROLLER_PROTOCOL_ID)
            markSyncedAt(
                it,
                "v_${id}_$K_LAST_CONTROLLER_DEVICE_ADDRESS",
                "v_${id}_$K_LAST_CONTROLLER_DEVICE_NAME",
                "v_${id}_$K_LAST_CONTROLLER_PROTOCOL_ID",
                LAST_CONTROLLER_DEVICE_ADDRESS.name,
                LAST_CONTROLLER_DEVICE_NAME.name,
                LAST_CONTROLLER_PROTOCOL_ID.name
            )
        }
    }

    suspend fun getLastControllerDeviceAddress(): String? {
        val currentId = currentVehicleId.first()
        val pref = preferencesFlow.first()
        return pref.safeGet(vKey(currentId, K_LAST_CONTROLLER_DEVICE_ADDRESS))?.takeIf { it.isNotBlank() }
            ?: pref.safeGet(LAST_CONTROLLER_DEVICE_ADDRESS)?.takeIf { it.isNotBlank() }
    }

    private fun updateVehicleMacAddressLocked(
        pref: androidx.datastore.preferences.core.MutablePreferences,
        vehicleId: String,
        address: String
    ) {
        val profiles = loadVehicleProfiles(pref.safeGet(VEHICLE_LIST)).toMutableList()
        val index = profiles.indexOfFirst { it.id == vehicleId }
        if (index < 0) return
        val existing = profiles[index]
        if (existing.macAddress == address) return
        profiles[index] = normalizedProfile(
            existing.copy(macAddress = address),
            updatedAt = System.currentTimeMillis()
        )
        pref[VEHICLE_LIST] = VehicleProfile.listToJson(profiles)
        markSyncedAt(pref, VEHICLE_LIST.name)
    }

    private fun applySyncSettingsLocked(
        pref: androidx.datastore.preferences.core.MutablePreferences,
        vehicleId: String,
        snapshot: SyncSettingsSnapshot
    ) {
        applyVehicleSyncSettingsLocked(
            pref = pref,
            snapshot = SyncVehicleSettingsSnapshot(
                vehicleProfileId = vehicleId,
                speedSource = snapshot.speedSource,
                battDataSource = snapshot.battDataSource,
                wheelCircumferenceMm = snapshot.wheelCircumferenceMm,
                polePairs = snapshot.polePairs,
                controllerBrand = snapshot.controllerBrand,
                dashboardItems = snapshot.dashboardItems,
                rideOverviewItems = snapshot.rideOverviewItems,
                updatedAt = snapshot.updatedAt,
                updatedByDeviceId = snapshot.updatedByDeviceId
            )
        )
        applyGlobalSyncSettingsLocked(pref, snapshot)
    }

    private fun applyVehicleSyncSettingsLocked(
        pref: androidx.datastore.preferences.core.MutablePreferences,
        snapshot: SyncVehicleSettingsSnapshot
    ) {
        val vehicleId = snapshot.vehicleProfileId
        pref[vKey(vehicleId, K_SPEED_SRC)] = snapshot.speedSource
        pref[vKey(vehicleId, K_BATT_SRC)] = snapshot.battDataSource
        pref[vKeyF(vehicleId, K_WHEEL)] = snapshot.wheelCircumferenceMm
        pref[vKeyI(vehicleId, K_POLE)] = snapshot.polePairs
        pref[vKey(vehicleId, K_BRAND)] = snapshot.controllerBrand
        if (snapshot.lastControllerDeviceAddress.isNotBlank()) {
            pref[vKey(vehicleId, K_LAST_CONTROLLER_DEVICE_ADDRESS)] = snapshot.lastControllerDeviceAddress
        } else {
            pref.remove(vKey(vehicleId, K_LAST_CONTROLLER_DEVICE_ADDRESS))
        }
        if (snapshot.lastControllerDeviceName.isNotBlank()) {
            pref[vKey(vehicleId, K_LAST_CONTROLLER_DEVICE_NAME)] = snapshot.lastControllerDeviceName
        } else {
            pref.remove(vKey(vehicleId, K_LAST_CONTROLLER_DEVICE_NAME))
        }
        if (snapshot.lastControllerProtocolId.isNotBlank()) {
            pref[vKey(vehicleId, K_LAST_CONTROLLER_PROTOCOL_ID)] = snapshot.lastControllerProtocolId
        } else {
            pref.remove(vKey(vehicleId, K_LAST_CONTROLLER_PROTOCOL_ID))
        }
        pref[vKey(vehicleId, K_DASH_ITEMS)] = snapshot.dashboardItems.joinToString(",")
        pref[vKey(vehicleId, K_RIDE_OVERVIEW_ITEMS)] = snapshot.rideOverviewItems.joinToString(",")
        markSyncedAt(
            pref,
            "v_${vehicleId}_$K_SPEED_SRC",
            "v_${vehicleId}_$K_BATT_SRC",
            "v_${vehicleId}_$K_WHEEL",
            "v_${vehicleId}_$K_POLE",
            "v_${vehicleId}_$K_BRAND",
            "v_${vehicleId}_$K_LAST_CONTROLLER_DEVICE_ADDRESS",
            "v_${vehicleId}_$K_LAST_CONTROLLER_DEVICE_NAME",
            "v_${vehicleId}_$K_LAST_CONTROLLER_PROTOCOL_ID",
            "v_${vehicleId}_$K_DASH_ITEMS",
            "v_${vehicleId}_$K_RIDE_OVERVIEW_ITEMS"
        )
    }

    private fun applyGlobalSyncSettingsLocked(
        pref: androidx.datastore.preferences.core.MutablePreferences,
        snapshot: SyncSettingsSnapshot
    ) {
        pref[LOG_LEVEL] = AppLogLevel.fromName(snapshot.logLevel).name
        pref[OVERLAY_ENABLED] = snapshot.overlayEnabled
        pref[DRIVE_BACKUP_RETENTION] = snapshot.driveBackupRetention
        pref[AUTO_RIDE_STOP_ENABLED] = snapshot.autoRideStopEnabled
        pref[AUTO_RIDE_STOP_DELAY_SECONDS] = snapshot.autoRideStopDelaySeconds.coerceIn(15, 600)
        pref[stringPreferencesKey(K_POSTER_TEMPLATE)] = PosterTemplates.byId(snapshot.posterTemplateId).id
        pref[stringPreferencesKey(K_POSTER_ASPECT_RATIO)] =
            runCatching { PosterAspectRatio.valueOf(snapshot.posterAspectRatio) }.getOrNull()?.name
                ?: PosterSettings().defaultAspectRatio.name
        pref[booleanPreferencesKey(K_POSTER_SHOW_TRACK)] = snapshot.posterShowTrack
        pref[booleanPreferencesKey(K_POSTER_SHOW_WATERMARK)] = snapshot.posterShowWatermark
        markSyncedAt(
            pref,
            LOG_LEVEL.name,
            OVERLAY_ENABLED.name,
            DRIVE_BACKUP_RETENTION.name,
            AUTO_RIDE_STOP_ENABLED.name,
            AUTO_RIDE_STOP_DELAY_SECONDS.name,
            K_POSTER_TEMPLATE,
            K_POSTER_ASPECT_RATIO,
            K_POSTER_SHOW_TRACK,
            K_POSTER_SHOW_WATERMARK
        )
    }

    private fun applyCurrentVehicleControllerSyncLocked(
        pref: androidx.datastore.preferences.core.MutablePreferences,
        snapshot: SyncVehicleSettingsSnapshot
    ) {
        if (snapshot.lastControllerDeviceAddress.isNotBlank()) {
            pref[LAST_CONTROLLER_DEVICE_ADDRESS] = snapshot.lastControllerDeviceAddress
        } else {
            pref.remove(LAST_CONTROLLER_DEVICE_ADDRESS)
        }
        if (snapshot.lastControllerDeviceName.isNotBlank()) {
            pref[LAST_CONTROLLER_DEVICE_NAME] = snapshot.lastControllerDeviceName
        } else {
            pref.remove(LAST_CONTROLLER_DEVICE_NAME)
        }
        if (snapshot.lastControllerProtocolId.isNotBlank()) {
            pref[LAST_CONTROLLER_PROTOCOL_ID] = snapshot.lastControllerProtocolId
        } else {
            pref.remove(LAST_CONTROLLER_PROTOCOL_ID)
        }
        markSyncedAt(
            pref,
            LAST_CONTROLLER_DEVICE_ADDRESS.name,
            LAST_CONTROLLER_DEVICE_NAME.name,
            LAST_CONTROLLER_PROTOCOL_ID.name
        )
    }

    private fun parseMetricNames(raw: String?): List<String> =
        raw.orEmpty()
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }

    private fun SyncVehicleProfileSnapshot.toVehicleProfile(): VehicleProfile = normalizedProfile(
        VehicleProfile(
            id = id,
            name = name,
            macAddress = macAddress,
            batterySeries = batterySeries,
            batteryCapacityAh = batteryCapacityAh,
            wheelCircumferenceMm = wheelCircumferenceMm,
            wheelRimSize = wheelRimSize,
            tireSpecLabel = tireSpecLabel,
            polePairs = polePairs,
            totalMileageKm = totalMileageKm,
            learnedEfficiencyWhKm = learnedEfficiencyWhKm,
            learnedUsableEnergyRatio = learnedUsableEnergyRatio,
            learnedInternalResistanceOhm = learnedInternalResistanceOhm,
            lastModified = updatedAt
        ),
        updatedAt = updatedAt
    )

    private fun SyncRideSnapshot.toRideHistoryRecord(): RideHistoryRecord = RideHistoryRecord(
        id = id,
        title = title,
        startedAtMs = startedAtMs,
        endedAtMs = endedAtMs,
        durationMs = durationMs,
        distanceMeters = distanceMeters,
        maxSpeedKmh = maxSpeedKmh,
        avgSpeedKmh = avgSpeedKmh,
        peakPowerKw = peakPowerKw,
        totalEnergyWh = totalEnergyWh,
        tractionEnergyWh = tractionEnergyWh,
        regenEnergyWh = regenEnergyWh,
        avgEfficiencyWhKm = avgEfficiencyWhKm,
        avgNetEfficiencyWhKm = avgNetEfficiencyWhKm,
        avgTractionEfficiencyWhKm = avgTractionEfficiencyWhKm,
        trackPoints = trackPoints.map { it.toRideTrackPoint() },
        samples = samples.map { it.toRideMetricSample() }
    )

    private fun SyncSpeedTestSnapshot.toSpeedTestRecord(): SpeedTestRecord = SpeedTestRecord(
        id = id,
        label = label,
        targetSpeedKmh = targetSpeedKmh,
        timeMs = timeToTargetMs,
        timestampMs = startedAtMs,
        maxSpeedKmh = achievedSpeedKmh,
        peakPowerKw = peakPowerKw,
        peakBusCurrentA = peakBusCurrentA,
        minVoltage = minVoltageV,
        distanceMeters = distanceMeters,
        trackPoints = trackPoints.map { it.toSpeedTestTrackPoint() }
    )

    private fun com.shawnrain.sdash.data.sync.SyncRideTrackPoint.toRideTrackPoint() = RideTrackPoint(
        latitude = latitude,
        longitude = longitude
    )

    private fun com.shawnrain.sdash.data.sync.SyncRideMetricSample.toRideMetricSample() =
        RideMetricSample.fromSyncSnapshot(this)

    private fun com.shawnrain.sdash.data.sync.SyncSpeedTestTrackPoint.toSpeedTestTrackPoint() = SpeedTestTrackPoint(
        latitude = latitude,
        longitude = longitude
    )

    private fun mergeSyncedRideHistoryRecords(
        local: List<RideHistoryRecord>,
        remote: List<RideHistoryRecord>
    ): List<RideHistoryRecord> {
        val merged = linkedMapOf<String, RideHistoryRecord>()
        local.forEach { record ->
            merged[rideHistoryMergeKey(record)] = record
        }
        remote.forEach { remoteRecord ->
            val key = rideHistoryMergeKey(remoteRecord)
            val localRecord = merged[key]
            val preferred = if (localRecord == null) {
                remoteRecord
            } else {
                preferRideHistoryRecord(localRecord, remoteRecord)
            }
            merged[key] = preserveRideHistoryDetail(localRecord, preferred)
        }
        return merged.values.toList()
    }

    private fun preserveRideHistoryDetail(
        local: RideHistoryRecord?,
        resolved: RideHistoryRecord
    ): RideHistoryRecord {
        if (local == null) return resolved
        return resolved.copy(
            trackPoints = if (resolved.trackPoints.isNotEmpty()) resolved.trackPoints else local.trackPoints,
            samples = mergeRideMetricSamples(local.samples, resolved.samples)
        )
    }

    private fun mergeRideMetricSamples(
        local: List<RideMetricSample>,
        remote: List<RideMetricSample>
    ): List<RideMetricSample> {
        if (remote.isEmpty()) return local
        if (local.isEmpty()) return remote
        if (local.size != remote.size) {
            return if (rideMetricSamplesCoverageScore(remote) >= rideMetricSamplesCoverageScore(local)) remote else local
        }

        return local.indices.map { index ->
            val localSample = local[index]
            val remoteSample = remote[index]
            if (remoteSample.timestampMs == localSample.timestampMs &&
                remoteSample.elapsedMs == localSample.elapsedMs &&
                RideMetricSampleSchema.populatedFieldCount(remoteSample) >= RideMetricSampleSchema.populatedFieldCount(localSample)
            ) {
                remoteSample
            } else if (RideMetricSampleSchema.populatedFieldCount(remoteSample) > RideMetricSampleSchema.populatedFieldCount(localSample)) {
                remoteSample
            } else {
                localSample
            }
        }
    }

    private fun mergeSyncedSpeedTestRecords(
        local: List<SpeedTestRecord>,
        remote: List<SpeedTestRecord>
    ): List<SpeedTestRecord> {
        val merged = linkedMapOf<String, SpeedTestRecord>()
        local.forEach { record ->
            merged[speedTestMergeKey(record)] = record
        }
        remote.forEach { remoteRecord ->
            val key = speedTestMergeKey(remoteRecord)
            val localRecord = merged[key]
            val preferred = if (localRecord == null) {
                remoteRecord
            } else {
                preferSpeedTestRecord(localRecord, remoteRecord)
            }
            merged[key] = preserveSpeedTestTrack(localRecord, preferred)
        }
        return merged.values.toList()
    }

    private fun preserveSpeedTestTrack(
        local: SpeedTestRecord?,
        resolved: SpeedTestRecord
    ): SpeedTestRecord {
        if (local == null) return resolved
        return resolved.copy(
            trackPoints = if (resolved.trackPoints.isNotEmpty()) resolved.trackPoints else local.trackPoints
        )
    }

    suspend fun exportBackupJson(): String {
        return exportBackupJson(RideHistoryRepository(context))
    }

    suspend fun exportBackupJson(rideHistoryRepository: RideHistoryRepository): String {
        val snapshot = preferencesFlow.first().asMap()
        val prefs = JSONObject()
        snapshot.forEach { (key, value) ->
            val encoded = when (value) {
                is String -> JSONObject().put("type", "string").put("value", value)
                is Int -> JSONObject().put("type", "int").put("value", value)
                is Float -> JSONObject().put("type", "float").put("value", value.toDouble())
                is Boolean -> JSONObject().put("type", "boolean").put("value", value)
                is Long -> JSONObject().put("type", "long").put("value", value)
                is Double -> JSONObject().put("type", "double").put("value", value)
                is Set<*> -> JSONObject()
                    .put("type", "string_set")
                    .put("value", JSONArray(value.mapNotNull { it?.toString() }))
                else -> null
            }
            if (encoded != null) {
                prefs.put(key.name, encoded)
            }
        }
        encodeRideHistoryBackupEntries(prefs, rideHistoryRepository)
        return JSONObject()
            .put("schema", 1)
            .put("exportedAt", System.currentTimeMillis())
            .put("prefs", prefs)
            .toString()
    }

    private enum class BackupValueKind {
        STRING, INT, FLOAT, BOOLEAN, LONG, DOUBLE, STRING_SET
    }

    private data class BackupEntry(
        val name: String,
        val kind: BackupValueKind,
        val value: Any
    )

    suspend fun importBackupJson(rawJson: String): Int {
        return importBackupJson(rawJson, RideHistoryRepository(context))
    }

    suspend fun importBackupJson(
        rawJson: String,
        rideHistoryRepository: RideHistoryRepository
    ): Int {
        val root = JSONObject(rawJson)
        val prefsJson = root.optJSONObject("prefs") ?: root
        val localSnapshot = preferencesFlow.first()
        val localRideGroups = loadRideHistoryByVehicle(rideHistoryRepository)
        val staged = mutableListOf<BackupEntry>()
        var hasAppOwnedKey = false
        val keys = prefsJson.keys()
        while (keys.hasNext()) {
            val name = keys.next()?.trim().orEmpty()
            if (name.isBlank()) continue
            val node = prefsJson.opt(name)
            parseBackupEntry(name, node)?.let { entry ->
                staged += entry
                if (isAppOwnedPreferenceKey(name)) {
                    hasAppOwnedKey = true
                }
            }
        }
        if (staged.isEmpty() || !hasAppOwnedKey) {
            throw IllegalArgumentException("备份内容为空或不是 SmartDash 备份格式")
        }
        val restoredRideGroups = parseRideHistoryEntries(staged)

        var imported = 0
        context.dataStore.edit { pref ->
            pref.asMap().keys.toList().forEach { key ->
                pref.remove(key)
            }
            staged.forEach { entry ->
                if (!isLegacyRideHistoryKey(entry.name)) {
                    writeBackupEntry(pref, entry)
                    imported += 1
                }
            }
            val localProfiles = loadVehicleProfiles(localSnapshot.safeGet(VEHICLE_LIST))
            val restoredProfiles = loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
            val mergedProfiles = mergeVehicleProfiles(localProfiles, restoredProfiles)
            pref[VEHICLE_LIST] = VehicleProfile.listToJson(mergedProfiles)
            markSyncedAt(pref, VEHICLE_LIST.name)

            localSnapshot.asMap().keys.forEach { key ->
                val name = key.name
                when {
                    name.endsWith("_$K_SPEEDTEST_HISTORY") -> {
                        val restoredJson = pref.safeGet(stringPreferencesKey(name))
                        val localJson = localSnapshot.safeGet(stringPreferencesKey(name))
                        pref[stringPreferencesKey(name)] = mergeSpeedTestHistoryJson(localJson, restoredJson)
                        markSyncedAt(pref, name)
                    }
                }
            }
            ensureVehiclePreferences(pref)
        }
        mergeRideHistoryGroupsIntoRepository(
            rideHistoryRepository = rideHistoryRepository,
            localRideGroups = localRideGroups,
            remoteRideGroups = restoredRideGroups
        )
        return imported
    }

    fun previewBackupJson(rawJson: String): BackupPreview {
        val root = JSONObject(rawJson)
        val prefsJson = root.optJSONObject("prefs") ?: root
        val exportedAt = root.optLong("exportedAt", 0L)
        val parsedEntries = parseBackupEntries(prefsJson)
        val entryMap = parsedEntries.associateBy { it.name }
        val vehicles = loadVehicleProfiles(entryMap[VEHICLE_LIST.name]?.value as? String)
        val rideGroups = parsedEntries
            .filter { it.kind == BackupValueKind.STRING && it.name.endsWith("_$K_RIDE_HISTORY") }
            .associate { entry ->
                vehicleIdFromScopedKey(entry.name, K_RIDE_HISTORY) to
                    RideHistoryRecord.listFromJson(entry.value as? String)
            }
        val speedTestGroups = parsedEntries
            .filter { it.kind == BackupValueKind.STRING && it.name.endsWith("_$K_SPEEDTEST_HISTORY") }
            .associate { entry ->
                vehicleIdFromScopedKey(entry.name, K_SPEEDTEST_HISTORY) to
                    SpeedTestRecord.listFromJson(entry.value as? String)
            }
        val allRides = rideGroups.values.flatten()
            .sortedByDescending { it.startedAtMs }
        val speedTests = speedTestGroups.values.flatten()
        val previewVehicles = buildList {
            val allVehicleIds = (vehicles.map { it.id } + rideGroups.keys + speedTestGroups.keys)
                .filter { it.isNotBlank() }
                .distinct()
            allVehicleIds.forEach { vehicleId ->
                val profile = vehicles.firstOrNull { it.id == vehicleId }
                add(
                    BackupPreviewVehicle(
                        id = vehicleId,
                        name = profile?.name ?: "车辆 ${vehicleId.takeLast(4)}",
                        totalMileageKm = profile?.totalMileageKm ?: 0f,
                        rideCount = rideGroups[vehicleId]?.size ?: 0
                    )
                )
            }
        }

        return BackupPreview(
            exportedAt = exportedAt,
            vehicleCount = previewVehicles.size,
            rideCount = allRides.size,
            speedTestCount = speedTests.size,
            vehicles = previewVehicles.sortedByDescending { it.rideCount },
            recentRides = allRides.take(8).map { record ->
                val ownerVehicleId = rideGroups.entries.firstOrNull { (_, rides) ->
                    rides.any { it.id == record.id }
                }?.key.orEmpty()
                val owner = previewVehicles.firstOrNull { it.id == ownerVehicleId }
                BackupPreviewRide(
                    id = record.id,
                    title = record.title,
                    startedAtMs = record.startedAtMs,
                    distanceKm = (record.distanceMeters / 1000f).coerceAtLeast(0f),
                    durationMinutes = (record.durationMs / 60_000L).toInt(),
                    vehicleId = owner?.id.orEmpty(),
                    vehicleName = owner?.name ?: "未知车辆"
                )
            }
        )
    }

    /**
     * Merges remote backup with local data (iCloud-style sync).
     * - Vehicle profiles: union merge (add new ones, update existing by ID)
     * - Settings: takes remote values (last-write-wins)
     * Does NOT wipe local data first.
     */
    suspend fun mergeBackupJson(remoteJson: String): Int {
        return mergeBackupJson(remoteJson, RideHistoryRepository(context))
    }

    suspend fun mergeBackupJson(
        remoteJson: String,
        rideHistoryRepository: RideHistoryRepository
    ): Int {
        val remoteRoot = JSONObject(remoteJson)
        val remotePrefs = remoteRoot.optJSONObject("prefs") ?: remoteRoot

        // Parse remote entries
        val remoteEntries = parseBackupEntries(remotePrefs)
        val remoteEntriesByName = remoteEntries.associateBy { it.name }
        val localRideGroups = loadRideHistoryByVehicle(rideHistoryRepository)
        val remoteRideGroups = parseRideHistoryEntries(remoteEntries)

        if (remoteEntries.isEmpty()) return 0

        var mergedCount = 0
        context.dataStore.edit { localPref ->
            // Merge vehicle profiles
            val localProfiles = loadVehicleProfiles(localPref.safeGet(VEHICLE_LIST))
            val remoteProfilesJson = remoteEntries.find { it.name == "vehicle_list_json" }?.value as? String
            var mergedProfiles = localProfiles
            if (remoteProfilesJson != null) {
                val remoteProfiles = loadVehicleProfiles(remoteProfilesJson)
                mergedProfiles = mergeVehicleProfiles(localProfiles, remoteProfiles)
                localPref[VEHICLE_LIST] = VehicleProfile.listToJson(mergedProfiles)
                val localMtime = readLocalModifiedAt(localPref, VEHICLE_LIST.name)
                val remoteMtime = remoteModifiedAt(remoteEntriesByName, VEHICLE_LIST.name)
                markSyncedAt(localPref, VEHICLE_LIST.name, updatedAt = maxOf(localMtime, remoteMtime, System.currentTimeMillis()))
                mergedCount++
            }

            // Merge other settings using per-field modification timestamps.
            remoteEntries.forEach { entry ->
                if (entry.name != "vehicle_list_json" && !isSyncMetadataKey(entry.name) && !isLegacyRideHistoryKey(entry.name)) {
                    when {
                        entry.kind == BackupValueKind.STRING && entry.name.endsWith("_$K_SPEEDTEST_HISTORY") -> {
                            val localJson = localPref.safeGet(stringPreferencesKey(entry.name))
                            val mergedJson = mergeSpeedTestHistoryJson(localJson, entry.value as String)
                            localPref[stringPreferencesKey(entry.name)] = mergedJson
                            val localMtime = readLocalModifiedAt(localPref, entry.name)
                            val remoteMtime = remoteModifiedAt(remoteEntriesByName, entry.name)
                            markSyncedAt(localPref, entry.name, updatedAt = maxOf(localMtime, remoteMtime, System.currentTimeMillis()))
                            mergedCount++
                        }
                        else -> {
                            if (mergeBackupEntryByTimestamp(localPref, remoteEntriesByName, entry)) {
                                mergedCount++
                            }
                        }
                    }
                }
            }

            val remoteCurrentVehicleId = (remoteEntriesByName[CURRENT_VEHICLE_ID.name]?.value as? String)
                ?.takeIf { remoteId -> mergedProfiles.any { it.id == remoteId } }
            val remoteCurrentVehicleMtime = remoteModifiedAt(remoteEntriesByName, CURRENT_VEHICLE_ID.name)
            val localCurrentVehicleMtime = readLocalModifiedAt(localPref, CURRENT_VEHICLE_ID.name)
            if (remoteCurrentVehicleId != null && remoteCurrentVehicleMtime >= localCurrentVehicleMtime) {
                localPref[CURRENT_VEHICLE_ID] = remoteCurrentVehicleId
                markSyncedAt(localPref, CURRENT_VEHICLE_ID.name, updatedAt = maxOf(remoteCurrentVehicleMtime, localCurrentVehicleMtime, System.currentTimeMillis()))
            }

            // Ensure current vehicle is valid
            ensureVehiclePreferences(localPref)
        }
        if (remoteRideGroups.isNotEmpty() || localRideGroups.isNotEmpty()) {
            mergeRideHistoryGroupsIntoRepository(
                rideHistoryRepository = rideHistoryRepository,
                localRideGroups = localRideGroups,
                remoteRideGroups = remoteRideGroups
            )
            mergedCount += remoteRideGroups.values.sumOf { it.size }
        }
        return mergedCount
    }

    suspend fun restoreRideFromBackupJson(rawJson: String, rideId: String): Boolean {
        return restoreRideFromBackupJson(rawJson, rideId, RideHistoryRepository(context))
    }

    suspend fun restoreRideFromBackupJson(
        rawJson: String,
        rideId: String,
        rideHistoryRepository: RideHistoryRepository
    ): Boolean {
        val root = JSONObject(rawJson)
        val prefsJson = root.optJSONObject("prefs") ?: root
        val parsedEntries = parseBackupEntries(prefsJson)
        val entryMap = parsedEntries.associateBy { it.name }
        val remoteProfiles = loadVehicleProfiles(entryMap[VEHICLE_LIST.name]?.value as? String)
        val matchedEntry = parsedEntries.firstOrNull { entry ->
            entry.kind == BackupValueKind.STRING &&
                entry.name.endsWith("_$K_RIDE_HISTORY") &&
                RideHistoryRecord.listFromJson(entry.value as? String).any { it.id == rideId }
        } ?: return false
        val matchedVehicleId = vehicleIdFromScopedKey(matchedEntry.name, K_RIDE_HISTORY)
        val matchedProfile = remoteProfiles.firstOrNull { it.id == matchedVehicleId }
            ?: VehicleProfile(
                id = matchedVehicleId.ifBlank { VehicleProfile.DEFAULT_ID },
                name = "恢复的车辆"
            )
        val targetRide = RideHistoryRecord.listFromJson(
            matchedEntry.value as? String
        ).firstOrNull { it.id == rideId } ?: return false

        context.dataStore.edit { localPref ->
            val localProfiles = loadVehicleProfiles(localPref.safeGet(VEHICLE_LIST))
            val mergedProfiles = mergeVehicleProfiles(localProfiles, listOf(matchedProfile))
            localPref[VEHICLE_LIST] = VehicleProfile.listToJson(mergedProfiles)
            markSyncedAt(localPref, VEHICLE_LIST.name)
            ensureVehiclePreferences(localPref)
        }
        val localRideRecords = rideHistoryRepository.listRideHistoryRecords(matchedProfile.id)
        val mergedRideRecords = mergeSyncedRideHistoryRecords(
            local = localRideRecords,
            remote = listOf(targetRide)
        ).sortedByDescending { it.startedAtMs }
        rideHistoryRepository.replaceRidesForVehicle(matchedProfile.id, mergedRideRecords)
        return true
    }

    private fun parseBackupEntries(prefsJson: JSONObject): List<BackupEntry> {
        val parsed = mutableListOf<BackupEntry>()
        val keys = prefsJson.keys()
        while (keys.hasNext()) {
            val name = keys.next()?.trim().orEmpty()
            if (name.isBlank()) continue
            parseBackupEntry(name, prefsJson.opt(name))?.let { parsed += it }
        }
        return parsed
    }

    private fun vehicleIdFromScopedKey(name: String, suffix: String): String {
        val prefix = "v_"
        val suffixToken = "_$suffix"
        if (!name.startsWith(prefix) || !name.endsWith(suffixToken)) return ""
        return name.removePrefix(prefix).removeSuffix(suffixToken)
    }

    private suspend fun encodeRideHistoryBackupEntries(
        prefs: JSONObject,
        rideHistoryRepository: RideHistoryRepository
    ) {
        val grouped = loadRideHistoryByVehicle(rideHistoryRepository)
        grouped.forEach { (vehicleId, records) ->
            prefs.put(
                "v_${vehicleId}_$K_RIDE_HISTORY",
                JSONObject()
                    .put("type", "string")
                    .put("value", RideHistoryRecord.listToJson(records))
            )
        }
    }

    private suspend fun loadRideHistoryByVehicle(
        rideHistoryRepository: RideHistoryRepository
    ): Map<String, List<RideHistoryRecord>> {
        val grouped = linkedMapOf<String, MutableList<RideHistoryRecord>>()
        rideHistoryRepository.listRideHistorySummaries().forEach { summary ->
            val record = rideHistoryRepository.loadRideRecord(summary.id) ?: return@forEach
            grouped.getOrPut(summary.vehicleId) { mutableListOf() }.add(record)
        }
        return grouped.mapValues { (_, records) ->
            records.sortedByDescending { it.startedAtMs }
        }
    }

    private fun parseRideHistoryEntries(entries: List<BackupEntry>): Map<String, List<RideHistoryRecord>> =
        entries
            .filter { it.kind == BackupValueKind.STRING && it.name.endsWith("_$K_RIDE_HISTORY") }
            .mapNotNull { entry ->
                val vehicleId = vehicleIdFromScopedKey(entry.name, K_RIDE_HISTORY)
                    .takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                vehicleId to RideHistoryRecord.listFromJson(entry.value as? String)
            }
            .toMap()

    private suspend fun mergeRideHistoryGroupsIntoRepository(
        rideHistoryRepository: RideHistoryRepository,
        localRideGroups: Map<String, List<RideHistoryRecord>>,
        remoteRideGroups: Map<String, List<RideHistoryRecord>>
    ) {
        val allVehicleIds = (localRideGroups.keys + remoteRideGroups.keys)
            .filter { it.isNotBlank() }
            .distinct()
        allVehicleIds.forEach { vehicleId ->
            val mergedRideRecords = mergeSyncedRideHistoryRecords(
                local = localRideGroups[vehicleId].orEmpty(),
                remote = remoteRideGroups[vehicleId].orEmpty()
            ).sortedByDescending { it.startedAtMs }
            rideHistoryRepository.replaceRidesForVehicle(vehicleId, mergedRideRecords)
        }
    }

    private fun mergeRideHistoryJson(localJson: String?, remoteJson: String?): String {
        val merged = linkedMapOf<String, RideHistoryRecord>()

        RideHistoryRecord.listFromJson(localJson).forEach { record ->
            merged[rideHistoryMergeKey(record)] = record
        }
        RideHistoryRecord.listFromJson(remoteJson).forEach { record ->
            val key = rideHistoryMergeKey(record)
            val existing = merged[key]
            merged[key] = if (existing == null) {
                record
            } else {
                preferRideHistoryRecord(existing, record)
            }
        }

        return RideHistoryRecord.listToJson(
            merged.values
                .sortedByDescending { it.startedAtMs }
        )
    }

    private fun rideHistoryMergeKey(record: RideHistoryRecord): String {
        return record.id.takeIf { it.isNotBlank() }
            ?: "${record.startedAtMs}_${record.endedAtMs}_${record.title}"
    }

    private fun preferRideHistoryRecord(
        local: RideHistoryRecord,
        remote: RideHistoryRecord
    ): RideHistoryRecord {
        val localScore = rideHistoryCompletenessScore(local)
        val remoteScore = rideHistoryCompletenessScore(remote)
        return when {
            remoteScore > localScore -> remote
            remoteScore < localScore -> local
            rideMetricSamplesCoverageScore(remote.samples) > rideMetricSamplesCoverageScore(local.samples) -> remote
            rideMetricSamplesCoverageScore(remote.samples) < rideMetricSamplesCoverageScore(local.samples) -> local
            remote.endedAtMs > local.endedAtMs -> remote
            remote.startedAtMs > local.startedAtMs -> remote
            else -> local
        }
    }

    private fun rideHistoryCompletenessScore(record: RideHistoryRecord): Long {
        return (record.samples.size.toLong() * 1_000_000L) +
            (record.trackPoints.size.toLong() * 10_000L) +
            record.durationMs +
            record.distanceMeters.toLong()
    }

    private fun rideMetricSamplesCoverageScore(samples: List<RideMetricSample>): Long =
        samples.sumOf { RideMetricSampleSchema.populatedFieldCount(it).toLong() }

    private fun mergeSpeedTestHistoryJson(localJson: String?, remoteJson: String?): String {
        val merged = linkedMapOf<String, SpeedTestRecord>()

        SpeedTestRecord.listFromJson(localJson).forEach { record ->
            merged[speedTestMergeKey(record)] = record
        }
        SpeedTestRecord.listFromJson(remoteJson).forEach { record ->
            val key = speedTestMergeKey(record)
            val existing = merged[key]
            merged[key] = if (existing == null) {
                record
            } else {
                preferSpeedTestRecord(existing, record)
            }
        }

        return SpeedTestRecord.listToJson(
            merged.values
                .sortedByDescending { it.timestampMs }
        )
    }

    private fun speedTestMergeKey(record: SpeedTestRecord): String {
        return record.id.takeIf { it.isNotBlank() }
            ?: "${record.label}_${record.targetSpeedKmh}_${record.timestampMs}"
    }

    private fun preferSpeedTestRecord(
        local: SpeedTestRecord,
        remote: SpeedTestRecord
    ): SpeedTestRecord {
        val localScore = (local.trackPoints.size * 1_000) + local.distanceMeters.toInt()
        val remoteScore = (remote.trackPoints.size * 1_000) + remote.distanceMeters.toInt()
        return when {
            remoteScore > localScore -> remote
            remoteScore < localScore -> local
            remote.timestampMs > local.timestampMs -> remote
            else -> local
        }
    }

    /**
     * Merges local and remote vehicle profiles with field-level intelligence.
     * - Adds remote profiles not in local
     * - For existing profiles, merges individual fields:
     *   - learned* fields: always take the higher (more riding = more accurate)
     *   - totalMileageKm: take the higher
     *   - lastModified: take the higher
     *   - Other fields: prefer remote if remote is newer by lastModified
     */
    private fun mergeVehicleProfiles(
        local: List<VehicleProfile>,
        remote: List<VehicleProfile>
    ): List<VehicleProfile> {
        val merged = local.associateBy { it.id }.toMutableMap()
        remote.forEach { remoteProfile ->
            val localProfile = merged[remoteProfile.id]
            if (localProfile == null) {
                // New profile from remote
                merged[remoteProfile.id] = remoteProfile
            } else {
                // Existing profile on both sides - merge intelligently
                merged[remoteProfile.id] = localProfile.copy(
                    // Learned fields: higher value wins (more riding = more accurate)
                    learnedInternalResistanceOhm = maxOf(
                        localProfile.learnedInternalResistanceOhm,
                        remoteProfile.learnedInternalResistanceOhm
                    ).takeIf { it > 0f } ?: localProfile.learnedInternalResistanceOhm.takeIf { it > 0f }
                        ?: remoteProfile.learnedInternalResistanceOhm,
                    learnedEfficiencyWhKm = maxOf(
                        localProfile.learnedEfficiencyWhKm,
                        remoteProfile.learnedEfficiencyWhKm
                    ).takeIf { it > 0f } ?: localProfile.learnedEfficiencyWhKm.takeIf { it > 0f }
                        ?: remoteProfile.learnedEfficiencyWhKm,
                    learnedUsableEnergyRatio = maxOf(
                        localProfile.learnedUsableEnergyRatio,
                        remoteProfile.learnedUsableEnergyRatio
                    ).takeIf { it in 0.72f..0.98f }
                        ?: localProfile.learnedUsableEnergyRatio.takeIf { it in 0.72f..0.98f }
                        ?: remoteProfile.learnedUsableEnergyRatio,
                    // Total mileage: higher wins
                    totalMileageKm = maxOf(localProfile.totalMileageKm, remoteProfile.totalMileageKm),
                    // lastModified: take higher
                    lastModified = maxOf(localProfile.lastModified, remoteProfile.lastModified),
                    // Other fields: use remote if it's newer
                    name = if (remoteProfile.lastModified >= localProfile.lastModified) remoteProfile.name else localProfile.name,
                    macAddress = if (remoteProfile.lastModified >= localProfile.lastModified) remoteProfile.macAddress else localProfile.macAddress,
                    batterySeries = if (remoteProfile.lastModified >= localProfile.lastModified) remoteProfile.batterySeries else localProfile.batterySeries,
                    batteryCapacityAh = if (remoteProfile.lastModified >= localProfile.lastModified) remoteProfile.batteryCapacityAh else localProfile.batteryCapacityAh,
                    wheelCircumferenceMm = if (remoteProfile.lastModified >= localProfile.lastModified) remoteProfile.wheelCircumferenceMm else localProfile.wheelCircumferenceMm,
                    wheelRimSize = if (remoteProfile.lastModified >= localProfile.lastModified) remoteProfile.wheelRimSize else localProfile.wheelRimSize,
                    tireSpecLabel = if (remoteProfile.lastModified >= localProfile.lastModified) remoteProfile.tireSpecLabel else localProfile.tireSpecLabel,
                    polePairs = if (remoteProfile.lastModified >= localProfile.lastModified) remoteProfile.polePairs else localProfile.polePairs
                )
            }
        }
        return merged.values.toList()
    }

    private fun parseBackupEntry(name: String, node: Any?): BackupEntry? {
        if (node is JSONObject) {
            val declaredType = node.optString("type")
            val rawValue = node.opt("value")
            parseByKind(name, kindFromType(name, declaredType), rawValue)?.let { return it }
        }
        val inferred = expectedKindForKey(name) ?: inferKind(node) ?: return null
        return parseByKind(name, inferred, node)
    }

    private fun writeBackupEntry(
        pref: androidx.datastore.preferences.core.MutablePreferences,
        entry: BackupEntry
    ) {
        when (entry.kind) {
            BackupValueKind.STRING -> pref[stringPreferencesKey(entry.name)] = entry.value as String
            BackupValueKind.INT -> pref[intPreferencesKey(entry.name)] = entry.value as Int
            BackupValueKind.FLOAT -> pref[floatPreferencesKey(entry.name)] = entry.value as Float
            BackupValueKind.BOOLEAN -> pref[booleanPreferencesKey(entry.name)] = entry.value as Boolean
            BackupValueKind.LONG -> pref[longPreferencesKey(entry.name)] = entry.value as Long
            BackupValueKind.DOUBLE -> pref[doublePreferencesKey(entry.name)] = entry.value as Double
            BackupValueKind.STRING_SET -> pref[stringSetPreferencesKey(entry.name)] =
                (entry.value as Set<*>).mapNotNull { it?.toString() }.toSet()
        }
    }

    private fun mergeBackupEntryByTimestamp(
        pref: androidx.datastore.preferences.core.MutablePreferences,
        remoteEntriesByName: Map<String, BackupEntry>,
        entry: BackupEntry
    ): Boolean {
        val localModifiedAt = readLocalModifiedAt(pref, entry.name)
        val remoteModifiedAt = remoteModifiedAt(remoteEntriesByName, entry.name)
        val shouldApplyRemote = remoteModifiedAt > localModifiedAt
        return when (entry.kind) {
            BackupValueKind.STRING -> {
                val key = stringPreferencesKey(entry.name)
                val localValue = pref.safeGet(key)
                val remoteValue = entry.value as String
                if (localValue == null || shouldApplyRemote) {
                    pref[key] = remoteValue
                    markSyncedAt(pref, entry.name, updatedAt = maxOf(remoteModifiedAt, localModifiedAt, System.currentTimeMillis()))
                    true
                } else {
                    false
                }
            }
            BackupValueKind.INT -> {
                val key = intPreferencesKey(entry.name)
                val localValue = pref.safeGet(key)
                if (localValue == null || shouldApplyRemote) {
                    pref[key] = entry.value as Int
                    markSyncedAt(pref, entry.name, updatedAt = maxOf(remoteModifiedAt, localModifiedAt, System.currentTimeMillis()))
                    true
                } else {
                    false
                }
            }
            BackupValueKind.FLOAT -> {
                val key = floatPreferencesKey(entry.name)
                val localValue = pref.safeGet(key)
                if (localValue == null || shouldApplyRemote) {
                    pref[key] = entry.value as Float
                    markSyncedAt(pref, entry.name, updatedAt = maxOf(remoteModifiedAt, localModifiedAt, System.currentTimeMillis()))
                    true
                } else {
                    false
                }
            }
            BackupValueKind.BOOLEAN -> {
                val key = booleanPreferencesKey(entry.name)
                val localValue = pref.safeGet(key)
                if (localValue == null || shouldApplyRemote) {
                    pref[key] = entry.value as Boolean
                    markSyncedAt(pref, entry.name, updatedAt = maxOf(remoteModifiedAt, localModifiedAt, System.currentTimeMillis()))
                    true
                } else {
                    false
                }
            }
            BackupValueKind.LONG -> {
                val key = longPreferencesKey(entry.name)
                val localValue = pref.safeGet(key)
                if (localValue == null || shouldApplyRemote) {
                    pref[key] = entry.value as Long
                    markSyncedAt(pref, entry.name, updatedAt = maxOf(remoteModifiedAt, localModifiedAt, System.currentTimeMillis()))
                    true
                } else {
                    false
                }
            }
            BackupValueKind.DOUBLE -> {
                val key = doublePreferencesKey(entry.name)
                val localValue = pref.safeGet(key)
                if (localValue == null || shouldApplyRemote) {
                    pref[key] = entry.value as Double
                    markSyncedAt(pref, entry.name, updatedAt = maxOf(remoteModifiedAt, localModifiedAt, System.currentTimeMillis()))
                    true
                } else {
                    false
                }
            }
            BackupValueKind.STRING_SET -> {
                val key = stringSetPreferencesKey(entry.name)
                val localValue = pref.safeGet(key).orEmpty()
                val remoteValue = (entry.value as Set<*>).mapNotNull { it?.toString() }.toSet()
                val merged = if (shouldApplyRemote) {
                    remoteValue.filter { it.isNotBlank() }.toSet()
                } else {
                    (localValue + remoteValue).filter { it.isNotBlank() }.toSet()
                }
                if (merged != localValue) {
                    pref[key] = merged
                    markSyncedAt(pref, entry.name, updatedAt = maxOf(remoteModifiedAt, localModifiedAt, System.currentTimeMillis()))
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun ensureVehiclePreferences(pref: androidx.datastore.preferences.core.MutablePreferences) {
        val profiles = loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
        val normalizedProfilesJson = VehicleProfile.listToJson(profiles)
        val previousProfilesJson = pref.safeGet(VEHICLE_LIST)
        pref[VEHICLE_LIST] = normalizedProfilesJson
        val currentId = pref.safeGet(CURRENT_VEHICLE_ID)
        val resolvedCurrentId = profiles.firstOrNull { it.id == currentId }?.id ?: profiles.first().id
        pref[CURRENT_VEHICLE_ID] = resolvedCurrentId
        val touched = buildList {
            if (previousProfilesJson != normalizedProfilesJson) add(VEHICLE_LIST.name)
            if (currentId != resolvedCurrentId) add(CURRENT_VEHICLE_ID.name)
        }
        if (touched.isNotEmpty()) {
            markSyncedAt(pref, *touched.toTypedArray())
        }
    }

    private fun kindFromType(name: String, declaredType: String): BackupValueKind? {
        expectedKindForKey(name)?.let { return it }
        return when (declaredType.lowercase()) {
            "string" -> BackupValueKind.STRING
            "int" -> BackupValueKind.INT
            "float" -> BackupValueKind.FLOAT
            "boolean" -> BackupValueKind.BOOLEAN
            "long" -> BackupValueKind.LONG
            "double" -> BackupValueKind.DOUBLE
            "string_set", "stringset" -> BackupValueKind.STRING_SET
            else -> null
        }
    }

    private fun inferKind(rawValue: Any?): BackupValueKind? {
        return when (rawValue) {
            is Boolean -> BackupValueKind.BOOLEAN
            is Int -> BackupValueKind.INT
            is Long -> BackupValueKind.LONG
            is Float -> BackupValueKind.FLOAT
            is Double -> BackupValueKind.DOUBLE
            is Number -> BackupValueKind.FLOAT
            is String -> BackupValueKind.STRING
            is JSONArray -> BackupValueKind.STRING_SET
            is Collection<*> -> BackupValueKind.STRING_SET
            else -> null
        }
    }

    private fun parseByKind(name: String, kind: BackupValueKind?, rawValue: Any?): BackupEntry? {
        return when (kind) {
            BackupValueKind.STRING -> BackupEntry(name, BackupValueKind.STRING, rawValue?.toString().orEmpty())
            BackupValueKind.INT -> toIntValue(rawValue)?.let { BackupEntry(name, BackupValueKind.INT, it) }
            BackupValueKind.FLOAT -> toFloatValue(rawValue)?.let { BackupEntry(name, BackupValueKind.FLOAT, it) }
            BackupValueKind.BOOLEAN -> toBooleanValue(rawValue)?.let { BackupEntry(name, BackupValueKind.BOOLEAN, it) }
            BackupValueKind.LONG -> toLongValue(rawValue)?.let { BackupEntry(name, BackupValueKind.LONG, it) }
            BackupValueKind.DOUBLE -> toDoubleValue(rawValue)?.let { BackupEntry(name, BackupValueKind.DOUBLE, it) }
            BackupValueKind.STRING_SET -> toStringSetValue(rawValue)?.let { BackupEntry(name, BackupValueKind.STRING_SET, it) }
            null -> null
        }
    }

    private fun toIntValue(rawValue: Any?): Int? {
        return when (rawValue) {
            is Number -> rawValue.toInt()
            is String -> rawValue.toIntOrNull()
            else -> null
        }
    }

    private fun toFloatValue(rawValue: Any?): Float? {
        return when (rawValue) {
            is Number -> rawValue.toFloat()
            is String -> rawValue.toFloatOrNull()
            else -> null
        }
    }

    private fun toLongValue(rawValue: Any?): Long? {
        return when (rawValue) {
            is Number -> rawValue.toLong()
            is String -> rawValue.toLongOrNull()
            else -> null
        }
    }

    private fun toDoubleValue(rawValue: Any?): Double? {
        return when (rawValue) {
            is Number -> rawValue.toDouble()
            is String -> rawValue.toDoubleOrNull()
            else -> null
        }
    }

    private fun toBooleanValue(rawValue: Any?): Boolean? {
        return when (rawValue) {
            is Boolean -> rawValue
            is Number -> rawValue.toInt() != 0
            is String -> when (rawValue.trim().lowercase()) {
                "1", "true", "yes", "on" -> true
                "0", "false", "no", "off" -> false
                else -> null
            }
            else -> null
        }
    }

    private fun toStringSetValue(rawValue: Any?): Set<String>? {
        return when (rawValue) {
            is JSONArray -> buildSet {
                for (i in 0 until rawValue.length()) {
                    add(rawValue.opt(i)?.toString().orEmpty())
                }
            }
            is Collection<*> -> rawValue.mapNotNull { it?.toString() }.toSet()
            else -> null
        }?.filter { it.isNotBlank() }?.toSet()
    }

    private fun isAppOwnedPreferenceKey(name: String): Boolean {
        if (isSyncMetadataKey(name)) return true
        if (name == CURRENT_VEHICLE_ID.name) return true
        if (name == VEHICLE_LIST.name) return true
        if (name == LAST_CONTROLLER_DEVICE_ADDRESS.name) return true
        if (name == LAST_CONTROLLER_DEVICE_NAME.name) return true
        if (name == LAST_CONTROLLER_PROTOCOL_ID.name) return true
        if (name == LOG_LEVEL.name) return true
        if (name == OVERLAY_ENABLED.name) return true
        if (name == DRIVE_BACKUP_RETENTION.name) return true
        if (name == RIDE_HISTORY_NORMALIZATION_VERSION.name) return true
        if (name == RIDE_HISTORY_STORAGE_MIGRATION_VERSION.name) return true
        return name.startsWith("v_")
    }

    private fun isSyncMetadataKey(name: String): Boolean = name.startsWith(SYNC_META_PREFIX)

    private fun isLegacyRideHistoryKey(name: String): Boolean =
        name.startsWith("v_") && name.endsWith("_$K_RIDE_HISTORY")

    private fun expectedKindForKey(name: String): BackupValueKind? {
        if (isSyncMetadataKey(name)) return BackupValueKind.LONG
        if (name == CURRENT_VEHICLE_ID.name) return BackupValueKind.STRING
        if (name == VEHICLE_LIST.name) return BackupValueKind.STRING
        if (name == LAST_CONTROLLER_DEVICE_ADDRESS.name) return BackupValueKind.STRING
        if (name == LAST_CONTROLLER_DEVICE_NAME.name) return BackupValueKind.STRING
        if (name == LAST_CONTROLLER_PROTOCOL_ID.name) return BackupValueKind.STRING
        if (name == LOG_LEVEL.name) return BackupValueKind.STRING
        if (name == OVERLAY_ENABLED.name) return BackupValueKind.BOOLEAN
        if (name == DRIVE_BACKUP_RETENTION.name) return BackupValueKind.STRING
        if (name == RIDE_HISTORY_NORMALIZATION_VERSION.name) return BackupValueKind.INT
        if (name == RIDE_HISTORY_STORAGE_MIGRATION_VERSION.name) return BackupValueKind.INT
        if (!name.startsWith("v_")) return null
        if (name.endsWith("_$K_WHEEL")) return BackupValueKind.FLOAT
        if (name.endsWith("_$K_POLE")) return BackupValueKind.INT
        return when {
            name.endsWith("_$K_BRAND") -> BackupValueKind.STRING
            name.endsWith("_$K_SPEED_SRC") -> BackupValueKind.STRING
            name.endsWith("_$K_BATT_SRC") -> BackupValueKind.STRING
            name.endsWith("_$K_DASH_ITEMS") -> BackupValueKind.STRING
            name.endsWith("_$K_RIDE_OVERVIEW_ITEMS") -> BackupValueKind.STRING
            name.endsWith("_$K_SPEEDTEST_HISTORY") -> BackupValueKind.STRING
            name.endsWith("_$K_RIDE_HISTORY") -> BackupValueKind.STRING
            name.endsWith("_$K_LAST_CONTROLLER_DEVICE_ADDRESS") -> BackupValueKind.STRING
            name.endsWith("_$K_LAST_CONTROLLER_DEVICE_NAME") -> BackupValueKind.STRING
            name.endsWith("_$K_LAST_CONTROLLER_PROTOCOL_ID") -> BackupValueKind.STRING
            else -> null
        }
    }
}
