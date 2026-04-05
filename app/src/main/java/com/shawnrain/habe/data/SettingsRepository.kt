package com.shawnrain.habe.data

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
import com.shawnrain.habe.data.history.RideHistoryRecord
import com.shawnrain.habe.data.speedtest.SpeedTestRecord
import com.shawnrain.habe.debug.AppLogLevel
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "habe_settings")

enum class MetricType(val title: String, val unit: String) {
    SPEED("速度", "km/h"),
    VOLTAGE("电压", "V"),
    VOLTAGE_SAG("压降", "V"),
    BUS_CURRENT("母线电流", "A"),
    PHASE_CURRENT("相电流", "A"),
    MOTOR_TEMP("电机温度", "°C"),
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
    RECOVERED_ENERGY("总回收能量", "Wh")
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
    companion object {
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
        val LOG_LEVEL = stringPreferencesKey("log_level")
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
    }

    private fun loadVehicleProfiles(raw: String?): List<VehicleProfile> {
        return VehicleProfile.listFromJson(raw)
            .distinctBy { it.id }
            .ifEmpty { listOf(VehicleProfile.default()) }
    }

    private fun <T> Preferences.safeGet(key: Preferences.Key<T>): T? =
        runCatching { this[key] }.getOrNull()

    private val preferencesFlow: Flow<Preferences> = context.dataStore.data
        .catch { emit(emptyPreferences()) }

    val vehicleProfiles: Flow<List<VehicleProfile>> = preferencesFlow.map { pref ->
        loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
    }

    val currentVehicleId: Flow<String> = preferencesFlow.map { pref ->
        val profiles = loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
        val storedId = pref.safeGet(CURRENT_VEHICLE_ID)
        profiles.firstOrNull { it.id == storedId }?.id ?: profiles.first().id
    }

    val currentVehicleProfile: Flow<VehicleProfile> = combine(
        vehicleProfiles,
        currentVehicleId
    ) { profiles, currentId ->
        profiles.firstOrNull { it.id == currentId } ?: profiles.first()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val wheelCircumference: Flow<Float> = currentVehicleId.flatMapLatest { id ->
        preferencesFlow.map { pref ->
            val currentProfile = loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
                .firstOrNull { it.id == id }
                ?: VehicleProfile.default()
            currentProfile.wheelCircumferenceMm
                .takeIf { it in 500f..5000f }
                ?: pref.safeGet(vKeyF(id, K_WHEEL))
                ?: 1800f
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val polePairs: Flow<Int> = currentVehicleId.flatMapLatest { id ->
        preferencesFlow.map { pref ->
            val currentProfile = loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
                .firstOrNull { it.id == id }
                ?: VehicleProfile.default()
            currentProfile.polePairs.takeIf { it > 0 } ?: pref.safeGet(vKeyI(id, K_POLE)) ?: 50
        }
    }

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
                listOf(MetricType.SOC, MetricType.RANGE, MetricType.POWER, MetricType.EFFICIENCY)
            } else {
                raw.split(",").mapNotNull { try { MetricType.valueOf(it) } catch (e: Exception) { null } }
            }
            resolved
                .filterNot { it == MetricType.MOTOR_TEMP }
                .ifEmpty { listOf(MetricType.SOC, MetricType.RANGE, MetricType.POWER, MetricType.EFFICIENCY) }
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
    }

    val overlayEnabled: Flow<Boolean> = preferencesFlow.map { pref ->
        pref.safeGet(OVERLAY_ENABLED) ?: false
    }

    val lastControllerDeviceAddress: Flow<String?> = preferencesFlow.map { pref ->
        val profiles = loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
        val currentId = profiles.firstOrNull { it.id == pref.safeGet(CURRENT_VEHICLE_ID) }?.id ?: profiles.first().id
        pref.safeGet(vKey(currentId, K_LAST_CONTROLLER_DEVICE_ADDRESS))
            ?.takeIf { it.isNotBlank() }
            ?: pref.safeGet(LAST_CONTROLLER_DEVICE_ADDRESS)?.takeIf { it.isNotBlank() }
    }

    val lastControllerDeviceName: Flow<String?> = preferencesFlow.map { pref ->
        val profiles = loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
        val currentId = profiles.firstOrNull { it.id == pref.safeGet(CURRENT_VEHICLE_ID) }?.id ?: profiles.first().id
        pref.safeGet(vKey(currentId, K_LAST_CONTROLLER_DEVICE_NAME))
            ?.takeIf { it.isNotBlank() }
            ?: pref.safeGet(LAST_CONTROLLER_DEVICE_NAME)?.takeIf { it.isNotBlank() }
    }

    val lastControllerProtocolId: Flow<String?> = preferencesFlow.map { pref ->
        val profiles = loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
        val currentId = profiles.firstOrNull { it.id == pref.safeGet(CURRENT_VEHICLE_ID) }?.id ?: profiles.first().id
        pref.safeGet(vKey(currentId, K_LAST_CONTROLLER_PROTOCOL_ID))
            ?.takeIf { it.isNotBlank() }
            ?: pref.safeGet(LAST_CONTROLLER_PROTOCOL_ID)?.takeIf { it.isNotBlank() }
    }

    suspend fun saveCurrentVehicleId(id: String) = context.dataStore.edit { pref ->
        val profiles = loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
        pref[CURRENT_VEHICLE_ID] = profiles.firstOrNull { it.id == id }?.id ?: profiles.first().id
    }

    suspend fun saveVehicleProfiles(profiles: List<VehicleProfile>) {
        val sanitized = profiles
            .map { profile ->
                profile.copy(
                    name = profile.name.trim().ifBlank { "未命名车辆" },
                    macAddress = profile.macAddress.trim(),
                    batterySeries = profile.batterySeries.coerceAtLeast(1),
                    batteryCapacityAh = profile.batteryCapacityAh.coerceAtLeast(1f),
                    wheelCircumferenceMm = profile.wheelCircumferenceMm.coerceIn(500f, 5000f),
                    wheelRimSize = profile.wheelRimSize.trim().ifBlank { "10寸" },
                    tireSpecLabel = profile.tireSpecLabel.trim(),
                    polePairs = profile.polePairs.coerceAtLeast(1),
                    totalMileageKm = profile.totalMileageKm.coerceAtLeast(0f),
                    learnedInternalResistanceOhm = profile.learnedInternalResistanceOhm.coerceAtLeast(0f),
                    learnedEfficiencyWhKm = profile.learnedEfficiencyWhKm.coerceAtLeast(0f),
                    learnedUsableEnergyRatio = profile.learnedUsableEnergyRatio.coerceIn(0.72f, 0.98f)
                )
            }
            .distinctBy { it.id }
            .ifEmpty { listOf(VehicleProfile.default()) }
        context.dataStore.edit { pref ->
            pref[VEHICLE_LIST] = VehicleProfile.listToJson(sanitized)
            val currentId = pref.safeGet(CURRENT_VEHICLE_ID)
            pref[CURRENT_VEHICLE_ID] = sanitized.firstOrNull { it.id == currentId }?.id ?: sanitized.first().id
        }
    }

    suspend fun upsertVehicleProfile(profile: VehicleProfile) {
        val current = vehicleProfiles.first().toMutableList()
        val normalized = profile.copy(
            name = profile.name.trim().ifBlank { "未命名车辆" },
            macAddress = profile.macAddress.trim(),
            batterySeries = profile.batterySeries.coerceAtLeast(1),
            batteryCapacityAh = profile.batteryCapacityAh.coerceAtLeast(1f),
            wheelCircumferenceMm = profile.wheelCircumferenceMm.coerceIn(500f, 5000f),
            wheelRimSize = profile.wheelRimSize.trim().ifBlank { "10寸" },
            tireSpecLabel = profile.tireSpecLabel.trim(),
            polePairs = profile.polePairs.coerceAtLeast(1),
            totalMileageKm = profile.totalMileageKm.coerceAtLeast(0f),
            learnedInternalResistanceOhm = profile.learnedInternalResistanceOhm.coerceAtLeast(0f),
            learnedEfficiencyWhKm = profile.learnedEfficiencyWhKm.coerceAtLeast(0f),
            learnedUsableEnergyRatio = profile.learnedUsableEnergyRatio.coerceIn(0.72f, 0.98f)
        )
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
        profiles[currentIndex] = transform(profiles[currentIndex])
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
        context.dataStore.edit { it[vKeyF(id, K_WHEEL)] = normalized }
        updateCurrentVehicle { profile ->
            profile.copy(wheelCircumferenceMm = normalized)
        }
    }

    suspend fun savePolePairs(value: Int) {
        val id = currentVehicleId.first()
        val normalized = value.coerceAtLeast(1)
        context.dataStore.edit { it[vKeyI(id, K_POLE)] = normalized }
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
        context.dataStore.edit { it[vKey(id, K_BRAND)] = value }
    }

    suspend fun saveSpeedSource(source: SpeedSource) {
        val id = currentVehicleId.first()
        context.dataStore.edit { it[vKey(id, K_SPEED_SRC)] = source.name }
    }

    suspend fun saveBattDataSource(source: DataSource) {
        val id = currentVehicleId.first()
        context.dataStore.edit { it[vKey(id, K_BATT_SRC)] = source.name }
    }

    suspend fun saveDashboardItems(items: List<MetricType>) {
        val id = currentVehicleId.first()
        val sanitized = items.filterNot { it == MetricType.MOTOR_TEMP }
        context.dataStore.edit { it[vKey(id, K_DASH_ITEMS)] = sanitized.joinToString(",") { it.name } }
    }

    suspend fun saveRideOverviewItems(items: List<MetricType>) {
        val id = currentVehicleId.first()
        context.dataStore.edit {
            it[vKey(id, K_RIDE_OVERVIEW_ITEMS)] = items.joinToString(",") { item -> item.name }
        }
    }

    suspend fun saveSpeedTestHistory(records: List<SpeedTestRecord>) {
        val id = currentVehicleId.first()
        context.dataStore.edit { it[vKey(id, K_SPEEDTEST_HISTORY)] = SpeedTestRecord.listToJson(records) }
    }

    suspend fun saveRideHistory(records: List<RideHistoryRecord>) {
        val id = currentVehicleId.first()
        context.dataStore.edit { it[vKey(id, K_RIDE_HISTORY)] = RideHistoryRecord.listToJson(records) }
    }

    suspend fun saveLogLevel(level: AppLogLevel) {
        context.dataStore.edit { it[LOG_LEVEL] = level.name }
    }

    suspend fun saveOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { it[OVERLAY_ENABLED] = enabled }
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
            if (!name.isNullOrBlank()) {
                it[vKey(id, K_LAST_CONTROLLER_DEVICE_NAME)] = name
                it[LAST_CONTROLLER_DEVICE_NAME] = name
            }
            if (!protocolId.isNullOrBlank()) {
                it[vKey(id, K_LAST_CONTROLLER_PROTOCOL_ID)] = protocolId
                it[LAST_CONTROLLER_PROTOCOL_ID] = protocolId
            }
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
        profiles[index] = existing.copy(macAddress = address)
        pref[VEHICLE_LIST] = VehicleProfile.listToJson(profiles)
    }

    suspend fun exportBackupJson(): String {
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
        val root = JSONObject(rawJson)
        val prefsJson = root.optJSONObject("prefs") ?: root
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
            throw IllegalArgumentException("备份内容为空或不是 Habe 备份格式")
        }

        var imported = 0
        context.dataStore.edit { pref ->
            pref.asMap().keys.toList().forEach { key ->
                pref.remove(key)
            }
            staged.forEach { entry ->
                writeBackupEntry(pref, entry)
                imported += 1
            }
            ensureVehiclePreferences(pref)
        }
        return imported
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

    private fun ensureVehiclePreferences(pref: androidx.datastore.preferences.core.MutablePreferences) {
        val profiles = loadVehicleProfiles(pref.safeGet(VEHICLE_LIST))
        pref[VEHICLE_LIST] = VehicleProfile.listToJson(profiles)
        val currentId = pref.safeGet(CURRENT_VEHICLE_ID)
        pref[CURRENT_VEHICLE_ID] = profiles.firstOrNull { it.id == currentId }?.id ?: profiles.first().id
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
        if (name == CURRENT_VEHICLE_ID.name) return true
        if (name == VEHICLE_LIST.name) return true
        if (name == LAST_CONTROLLER_DEVICE_ADDRESS.name) return true
        if (name == LAST_CONTROLLER_DEVICE_NAME.name) return true
        if (name == LAST_CONTROLLER_PROTOCOL_ID.name) return true
        if (name == LOG_LEVEL.name) return true
        if (name == OVERLAY_ENABLED.name) return true
        return name.startsWith("v_")
    }

    private fun expectedKindForKey(name: String): BackupValueKind? {
        if (name == CURRENT_VEHICLE_ID.name) return BackupValueKind.STRING
        if (name == VEHICLE_LIST.name) return BackupValueKind.STRING
        if (name == LAST_CONTROLLER_DEVICE_ADDRESS.name) return BackupValueKind.STRING
        if (name == LAST_CONTROLLER_DEVICE_NAME.name) return BackupValueKind.STRING
        if (name == LAST_CONTROLLER_PROTOCOL_ID.name) return BackupValueKind.STRING
        if (name == LOG_LEVEL.name) return BackupValueKind.STRING
        if (name == OVERLAY_ENABLED.name) return BackupValueKind.BOOLEAN
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
