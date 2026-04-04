package com.shawnrain.habe.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shawnrain.habe.data.history.RideHistoryRecord
import com.shawnrain.habe.data.speedtest.SpeedTestRecord
import com.shawnrain.habe.debug.AppLogLevel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "habe_settings")

enum class MetricType(val title: String, val unit: String) {
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
    EFFICIENCY("实时能效", "Wh/km"),
    TRIP_DISTANCE("本次里程", "km"),
    TOTAL_ENERGY("总能耗", "Wh"),
    PEAK_REGEN_POWER("最大回收功率", "kW"),
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

    val vehicleProfiles: Flow<List<VehicleProfile>> = context.dataStore.data.map { pref ->
        loadVehicleProfiles(pref[VEHICLE_LIST])
    }

    val currentVehicleId: Flow<String> = context.dataStore.data.map { pref ->
        val profiles = loadVehicleProfiles(pref[VEHICLE_LIST])
        val storedId = pref[CURRENT_VEHICLE_ID]
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
        context.dataStore.data.map { pref ->
            val currentProfile = loadVehicleProfiles(pref[VEHICLE_LIST])
                .firstOrNull { it.id == id }
                ?: VehicleProfile.default()
            currentProfile.wheelCircumferenceMm
                .takeIf { it in 500f..5000f }
                ?: pref[vKeyF(id, K_WHEEL)]
                ?: 1800f
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val polePairs: Flow<Int> = currentVehicleId.flatMapLatest { id ->
        context.dataStore.data.map { pref ->
            val currentProfile = loadVehicleProfiles(pref[VEHICLE_LIST])
                .firstOrNull { it.id == id }
                ?: VehicleProfile.default()
            currentProfile.polePairs.takeIf { it > 0 } ?: pref[vKeyI(id, K_POLE)] ?: 50
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val controllerBrand: Flow<String> = currentVehicleId.flatMapLatest { id ->
        context.dataStore.data.map { it[vKey(id, K_BRAND)] ?: "auto" }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val speedSource: Flow<SpeedSource> = currentVehicleId.flatMapLatest { id ->
        context.dataStore.data.map { pref ->
            val name = pref[vKey(id, K_SPEED_SRC)] ?: SpeedSource.CONTROLLER.name
            try { SpeedSource.valueOf(name) } catch(e: Exception) { SpeedSource.CONTROLLER }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val battDataSource: Flow<DataSource> = currentVehicleId.flatMapLatest { id ->
        context.dataStore.data.map { pref ->
            val name = pref[vKey(id, K_BATT_SRC)] ?: DataSource.CONTROLLER.name
            try { DataSource.valueOf(name) } catch(e: Exception) { DataSource.CONTROLLER }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val dashboardItems: Flow<List<MetricType>> = currentVehicleId.flatMapLatest { id ->
        context.dataStore.data.map { pref ->
            val raw = pref[vKey(id, K_DASH_ITEMS)]
            if (raw.isNullOrEmpty()) {
                listOf(MetricType.SOC, MetricType.RANGE, MetricType.POWER, MetricType.EFFICIENCY)
            } else {
                raw.split(",").mapNotNull { try { MetricType.valueOf(it) } catch (e: Exception) { null } }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val speedTestHistory: Flow<List<SpeedTestRecord>> = currentVehicleId.flatMapLatest { id ->
        context.dataStore.data.map { pref ->
            SpeedTestRecord.listFromJson(pref[vKey(id, K_SPEEDTEST_HISTORY)])
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val rideHistory: Flow<List<RideHistoryRecord>> = currentVehicleId.flatMapLatest { id ->
        context.dataStore.data.map { pref ->
            RideHistoryRecord.listFromJson(pref[vKey(id, K_RIDE_HISTORY)])
        }
    }

    val logLevel: Flow<AppLogLevel> = context.dataStore.data.map { pref ->
        AppLogLevel.fromName(pref[LOG_LEVEL] ?: AppLogLevel.DEBUG.name)
    }

    val overlayEnabled: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[OVERLAY_ENABLED] ?: false
    }

    val lastControllerDeviceAddress: Flow<String?> = context.dataStore.data.map { pref ->
        val profiles = loadVehicleProfiles(pref[VEHICLE_LIST])
        val currentId = profiles.firstOrNull { it.id == pref[CURRENT_VEHICLE_ID] }?.id ?: profiles.first().id
        pref[vKey(currentId, K_LAST_CONTROLLER_DEVICE_ADDRESS)]
            ?.takeIf { it.isNotBlank() }
            ?: pref[LAST_CONTROLLER_DEVICE_ADDRESS]?.takeIf { it.isNotBlank() }
    }

    val lastControllerDeviceName: Flow<String?> = context.dataStore.data.map { pref ->
        val profiles = loadVehicleProfiles(pref[VEHICLE_LIST])
        val currentId = profiles.firstOrNull { it.id == pref[CURRENT_VEHICLE_ID] }?.id ?: profiles.first().id
        pref[vKey(currentId, K_LAST_CONTROLLER_DEVICE_NAME)]
            ?.takeIf { it.isNotBlank() }
            ?: pref[LAST_CONTROLLER_DEVICE_NAME]?.takeIf { it.isNotBlank() }
    }

    val lastControllerProtocolId: Flow<String?> = context.dataStore.data.map { pref ->
        val profiles = loadVehicleProfiles(pref[VEHICLE_LIST])
        val currentId = profiles.firstOrNull { it.id == pref[CURRENT_VEHICLE_ID] }?.id ?: profiles.first().id
        pref[vKey(currentId, K_LAST_CONTROLLER_PROTOCOL_ID)]
            ?.takeIf { it.isNotBlank() }
            ?: pref[LAST_CONTROLLER_PROTOCOL_ID]?.takeIf { it.isNotBlank() }
    }

    suspend fun saveCurrentVehicleId(id: String) = context.dataStore.edit { pref ->
        val profiles = loadVehicleProfiles(pref[VEHICLE_LIST])
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
                    learnedInternalResistanceOhm = profile.learnedInternalResistanceOhm.coerceAtLeast(0f)
                )
            }
            .distinctBy { it.id }
            .ifEmpty { listOf(VehicleProfile.default()) }
        context.dataStore.edit { pref ->
            pref[VEHICLE_LIST] = VehicleProfile.listToJson(sanitized)
            val currentId = pref[CURRENT_VEHICLE_ID]
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
            learnedInternalResistanceOhm = profile.learnedInternalResistanceOhm.coerceAtLeast(0f)
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
        context.dataStore.edit { it[vKey(id, K_DASH_ITEMS)] = items.joinToString(",") { it.name } }
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
        return context.dataStore.data.first()[OVERLAY_ENABLED] ?: false
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
        val pref = context.dataStore.data.first()
        return pref[vKey(currentId, K_LAST_CONTROLLER_DEVICE_ADDRESS)]?.takeIf { it.isNotBlank() }
            ?: pref[LAST_CONTROLLER_DEVICE_ADDRESS]?.takeIf { it.isNotBlank() }
    }

    private fun updateVehicleMacAddressLocked(
        pref: androidx.datastore.preferences.core.MutablePreferences,
        vehicleId: String,
        address: String
    ) {
        val profiles = loadVehicleProfiles(pref[VEHICLE_LIST]).toMutableList()
        val index = profiles.indexOfFirst { it.id == vehicleId }
        if (index < 0) return
        val existing = profiles[index]
        if (existing.macAddress == address) return
        profiles[index] = existing.copy(macAddress = address)
        pref[VEHICLE_LIST] = VehicleProfile.listToJson(profiles)
    }
}
