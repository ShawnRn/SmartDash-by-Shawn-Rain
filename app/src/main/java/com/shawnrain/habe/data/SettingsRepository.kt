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
    SOC("电量(预估)", "%"),
    RPM("转速", "rpm"),
    EFFICIENCY("实时能效", "Wh/km"),
    TRIP_DISTANCE("本次里程", "km")
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
        val LOG_LEVEL = stringPreferencesKey("log_level")
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
    }

    val currentVehicleId: Flow<String> = context.dataStore.data.map { it[CURRENT_VEHICLE_ID] ?: "default" }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val wheelCircumference: Flow<Float> = currentVehicleId.flatMapLatest { id ->
        context.dataStore.data.map { it[vKeyF(id, K_WHEEL)] ?: 1800f }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val polePairs: Flow<Int> = currentVehicleId.flatMapLatest { id ->
        context.dataStore.data.map { it[vKeyI(id, K_POLE)] ?: 50 }
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
                listOf(MetricType.VOLTAGE, MetricType.BUS_CURRENT, MetricType.POWER, MetricType.EFFICIENCY)
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
        pref[LAST_CONTROLLER_DEVICE_ADDRESS]?.takeIf { it.isNotBlank() }
    }

    val lastControllerDeviceName: Flow<String?> = context.dataStore.data.map { pref ->
        pref[LAST_CONTROLLER_DEVICE_NAME]?.takeIf { it.isNotBlank() }
    }

    val lastControllerProtocolId: Flow<String?> = context.dataStore.data.map { pref ->
        pref[LAST_CONTROLLER_PROTOCOL_ID]?.takeIf { it.isNotBlank() }
    }

    suspend fun saveCurrentVehicleId(id: String) = context.dataStore.edit { it[CURRENT_VEHICLE_ID] = id }

    suspend fun saveWheelCircumference(value: Float) {
        val id = currentVehicleId.first()
        context.dataStore.edit { it[vKeyF(id, K_WHEEL)] = value }
    }

    suspend fun savePolePairs(value: Int) {
        val id = currentVehicleId.first()
        context.dataStore.edit { it[vKeyI(id, K_POLE)] = value }
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
        context.dataStore.edit {
            it[LAST_CONTROLLER_DEVICE_ADDRESS] = address
            if (!name.isNullOrBlank()) {
                it[LAST_CONTROLLER_DEVICE_NAME] = name
            }
            if (!protocolId.isNullOrBlank()) {
                it[LAST_CONTROLLER_PROTOCOL_ID] = protocolId
            }
        }
    }

    suspend fun clearLastControllerDevice() {
        context.dataStore.edit {
            it.remove(LAST_CONTROLLER_DEVICE_ADDRESS)
            it.remove(LAST_CONTROLLER_DEVICE_NAME)
            it.remove(LAST_CONTROLLER_PROTOCOL_ID)
        }
    }

    suspend fun getLastControllerDeviceAddress(): String? {
        return context.dataStore.data.first()[LAST_CONTROLLER_DEVICE_ADDRESS]?.takeIf { it.isNotBlank() }
    }
}
