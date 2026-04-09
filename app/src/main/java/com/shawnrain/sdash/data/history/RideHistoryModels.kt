package com.shawnrain.sdash.data.history

import com.shawnrain.sdash.data.sync.SyncRideMetricSample
import com.shawnrain.sdash.data.telemetry.EnergyWh
import com.shawnrain.sdash.data.telemetry.EfficiencyWhKm
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class RideTrackPoint(
    val latitude: Double,
    val longitude: Double
) {
    fun toJson(): JSONObject = JSONObject()
        .put("lat", latitude)
        .put("lon", longitude)

    companion object {
        fun fromJson(json: JSONObject): RideTrackPoint {
            return RideTrackPoint(
                latitude = json.optDouble("lat", 0.0),
                longitude = json.optDouble("lon", 0.0)
            )
        }
    }
}

data class RideMetricSample(
    val elapsedMs: Long,
    val timestampMs: Long,
    val speedKmH: Float,
    val powerKw: Float,
    val voltage: Float,
    val voltageSag: Float,
    val busCurrent: Float,
    val phaseCurrent: Float,
    val controllerTemp: Float,
    val soc: Float,
    val estimatedRangeKm: Float = 0.0f,
    val rpm: Float,
    val efficiencyWhKm: EfficiencyWhKm,
    val avgEfficiencyWhKm: EfficiencyWhKm = 0.0f,
    val avgNetEfficiencyWhKm: EfficiencyWhKm = 0.0f,
    val avgTractionEfficiencyWhKm: EfficiencyWhKm = 0.0f,
    val distanceMeters: Float,
    val totalEnergyWh: EnergyWh = 0.0f,
    val tractionEnergyWh: EnergyWh = 0.0f,
    val regenEnergyWh: EnergyWh = 0.0f,
    val recoveredEnergyWh: EnergyWh = 0.0f,
    val maxControllerTemp: Float = 0.0f,
    val gradePercent: Float? = null,
    val altitudeMeters: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun toJson(): JSONObject = RideMetricSampleSchema.toJson(this)

    fun toSyncSnapshot(): SyncRideMetricSample = RideMetricSampleSchema.toSyncSnapshot(this)

    companion object {
        fun fromJson(json: JSONObject): RideMetricSample = RideMetricSampleSchema.fromJson(json)

        fun fromSyncSnapshot(snapshot: SyncRideMetricSample): RideMetricSample =
            RideMetricSampleSchema.fromSyncSnapshot(snapshot)
    }
}

data class RideMetricSampleCsvColumn(
    val header: String,
    val value: (RideMetricSample, (Number?) -> String) -> String
)

object RideMetricSampleSchema {
    private const val K_ELAPSED_MS = "elapsedMs"
    private const val K_TIMESTAMP_MS = "timestampMs"
    private const val K_SPEED_KMH = "speedKmH"
    private const val K_POWER_KW = "powerKw"
    private const val K_VOLTAGE = "voltage"
    private const val K_VOLTAGE_SAG = "voltageSag"
    private const val K_BUS_CURRENT = "busCurrent"
    private const val K_PHASE_CURRENT = "phaseCurrent"
    private const val K_CONTROLLER_TEMP = "controllerTemp"
    private const val K_SOC = "soc"
    private const val K_ESTIMATED_RANGE_KM = "estimatedRangeKm"
    private const val K_RPM = "rpm"
    private const val K_EFFICIENCY_WH_KM = "efficiencyWhKm"
    private const val K_AVG_EFFICIENCY_WH_KM = "avgEfficiencyWhKm"
    private const val K_AVG_NET_EFFICIENCY_WH_KM = "avgNetEfficiencyWhKm"
    private const val K_AVG_TRACTION_EFFICIENCY_WH_KM = "avgTractionEfficiencyWhKm"
    private const val K_DISTANCE_METERS = "distanceMeters"
    private const val K_TOTAL_ENERGY_WH = "totalEnergyWh"
    private const val K_TRACTION_ENERGY_WH = "tractionEnergyWh"
    private const val K_REGEN_ENERGY_WH = "regenEnergyWh"
    private const val K_RECOVERED_ENERGY_WH = "recoveredEnergyWh"
    private const val K_MAX_CONTROLLER_TEMP = "maxControllerTemp"
    private const val K_GRADE_PERCENT = "gradePercent"
    private const val K_ALTITUDE_METERS = "altitudeMeters"
    private const val K_LATITUDE = "latitude"
    private const val K_LONGITUDE = "longitude"

    val csvColumns: List<RideMetricSampleCsvColumn> = listOf(
        RideMetricSampleCsvColumn("elapsed_ms") { sample, _ -> sample.elapsedMs.toString() },
        RideMetricSampleCsvColumn("timestamp_ms") { sample, _ -> sample.timestampMs.toString() },
        RideMetricSampleCsvColumn("speed_kmh") { sample, format -> format(sample.speedKmH) },
        RideMetricSampleCsvColumn("power_kw") { sample, format -> format(sample.powerKw) },
        RideMetricSampleCsvColumn("voltage_v") { sample, format -> format(sample.voltage) },
        RideMetricSampleCsvColumn("voltage_sag_v") { sample, format -> format(sample.voltageSag) },
        RideMetricSampleCsvColumn("bus_current_a") { sample, format -> format(sample.busCurrent) },
        RideMetricSampleCsvColumn("phase_current_a") { sample, format -> format(sample.phaseCurrent) },
        RideMetricSampleCsvColumn("controller_temp_c") { sample, format -> format(sample.controllerTemp) },
        RideMetricSampleCsvColumn("soc_percent") { sample, format -> format(sample.soc) },
        RideMetricSampleCsvColumn("estimated_range_km") { sample, format -> format(sample.estimatedRangeKm) },
        RideMetricSampleCsvColumn("rpm") { sample, format -> format(sample.rpm) },
        RideMetricSampleCsvColumn("efficiency_wh_km") { sample, format -> format(sample.efficiencyWhKm) },
        RideMetricSampleCsvColumn("avg_efficiency_wh_km") { sample, format -> format(sample.avgEfficiencyWhKm) },
        RideMetricSampleCsvColumn("distance_m") { sample, format -> format(sample.distanceMeters) },
        RideMetricSampleCsvColumn("total_energy_wh") { sample, format -> format(sample.totalEnergyWh) },
        RideMetricSampleCsvColumn("traction_energy_wh") { sample, format -> format(sample.tractionEnergyWh) },
        RideMetricSampleCsvColumn("regen_energy_wh") { sample, format -> format(sample.regenEnergyWh) },
        RideMetricSampleCsvColumn("recovered_energy_wh") { sample, format -> format(sample.recoveredEnergyWh) },
        RideMetricSampleCsvColumn("max_controller_temp_c") { sample, format -> format(sample.maxControllerTemp) },
        RideMetricSampleCsvColumn("grade_percent") { sample, format -> format(sample.gradePercent) },
        RideMetricSampleCsvColumn("altitude_m") { sample, format -> format(sample.altitudeMeters) },
        RideMetricSampleCsvColumn("latitude") { sample, _ -> sample.latitude?.toString().orEmpty() },
        RideMetricSampleCsvColumn("longitude") { sample, _ -> sample.longitude?.toString().orEmpty() }
    )

    fun populatedFieldCount(sample: RideMetricSample): Int =
        toValueMap(sample).values.count { it != null }

    fun populatedFieldCount(snapshot: SyncRideMetricSample): Int =
        populatedFieldCount(fromSyncSnapshot(snapshot))

    fun toValueMap(sample: RideMetricSample): Map<String, Any?> = linkedMapOf(
        K_ELAPSED_MS to sample.elapsedMs,
        K_TIMESTAMP_MS to sample.timestampMs,
        K_SPEED_KMH to sample.speedKmH.toDouble(),
        K_POWER_KW to sample.powerKw.toDouble(),
        K_VOLTAGE to sample.voltage.toDouble(),
        K_VOLTAGE_SAG to sample.voltageSag.toDouble(),
        K_BUS_CURRENT to sample.busCurrent.toDouble(),
        K_PHASE_CURRENT to sample.phaseCurrent.toDouble(),
        K_CONTROLLER_TEMP to sample.controllerTemp.toDouble(),
        K_SOC to sample.soc.toDouble(),
        K_ESTIMATED_RANGE_KM to sample.estimatedRangeKm.toDouble(),
        K_RPM to sample.rpm.toDouble(),
        K_EFFICIENCY_WH_KM to sample.efficiencyWhKm.toDouble(),
        K_AVG_EFFICIENCY_WH_KM to sample.avgEfficiencyWhKm.toDouble(),
        K_AVG_NET_EFFICIENCY_WH_KM to sample.avgNetEfficiencyWhKm.toDouble(),
        K_AVG_TRACTION_EFFICIENCY_WH_KM to sample.avgTractionEfficiencyWhKm.toDouble(),
        K_DISTANCE_METERS to sample.distanceMeters.toDouble(),
        K_TOTAL_ENERGY_WH to sample.totalEnergyWh.toDouble(),
        K_TRACTION_ENERGY_WH to sample.tractionEnergyWh.toDouble(),
        K_REGEN_ENERGY_WH to sample.regenEnergyWh.toDouble(),
        K_RECOVERED_ENERGY_WH to sample.recoveredEnergyWh.toDouble(),
        K_MAX_CONTROLLER_TEMP to sample.maxControllerTemp.toDouble(),
        K_GRADE_PERCENT to sample.gradePercent?.takeIf { it.isFinite() }?.toDouble(),
        K_ALTITUDE_METERS to sample.altitudeMeters?.takeIf { it.isFinite() },
        K_LATITUDE to sample.latitude?.takeIf { it.isFinite() },
        K_LONGITUDE to sample.longitude?.takeIf { it.isFinite() }
    )

    fun toJson(sample: RideMetricSample): JSONObject = JSONObject().apply {
        toValueMap(sample).forEach { (key, value) -> put(key, value) }
    }

    fun fromJson(json: JSONObject): RideMetricSample = fromValueMap(
        mapOf(
            K_ELAPSED_MS to json.optValue(K_ELAPSED_MS),
            K_TIMESTAMP_MS to json.optValue(K_TIMESTAMP_MS),
            K_SPEED_KMH to json.optValue(K_SPEED_KMH),
            K_POWER_KW to json.optValue(K_POWER_KW),
            K_VOLTAGE to json.optValue(K_VOLTAGE),
            K_VOLTAGE_SAG to json.optValue(K_VOLTAGE_SAG),
            K_BUS_CURRENT to json.optValue(K_BUS_CURRENT),
            K_PHASE_CURRENT to json.optValue(K_PHASE_CURRENT),
            K_CONTROLLER_TEMP to json.optValue(K_CONTROLLER_TEMP),
            K_SOC to json.optValue(K_SOC),
            K_ESTIMATED_RANGE_KM to json.optValue(K_ESTIMATED_RANGE_KM),
            K_RPM to json.optValue(K_RPM),
            K_EFFICIENCY_WH_KM to json.optValue(K_EFFICIENCY_WH_KM),
            K_AVG_EFFICIENCY_WH_KM to json.optValue(K_AVG_EFFICIENCY_WH_KM),
            K_AVG_NET_EFFICIENCY_WH_KM to json.optValue(K_AVG_NET_EFFICIENCY_WH_KM),
            K_AVG_TRACTION_EFFICIENCY_WH_KM to json.optValue(K_AVG_TRACTION_EFFICIENCY_WH_KM),
            K_DISTANCE_METERS to json.optValue(K_DISTANCE_METERS),
            K_TOTAL_ENERGY_WH to json.optValue(K_TOTAL_ENERGY_WH),
            K_TRACTION_ENERGY_WH to json.optValue(K_TRACTION_ENERGY_WH),
            K_REGEN_ENERGY_WH to json.optValue(K_REGEN_ENERGY_WH),
            K_RECOVERED_ENERGY_WH to json.optValue(K_RECOVERED_ENERGY_WH),
            K_MAX_CONTROLLER_TEMP to json.optValue(K_MAX_CONTROLLER_TEMP),
            K_GRADE_PERCENT to json.optValue(K_GRADE_PERCENT),
            K_ALTITUDE_METERS to json.optValue(K_ALTITUDE_METERS),
            K_LATITUDE to json.optValue(K_LATITUDE),
            K_LONGITUDE to json.optValue(K_LONGITUDE)
        )
    )

    fun fromValueMap(values: Map<String, Any?>): RideMetricSample = RideMetricSample(
        elapsedMs = values.longValue(K_ELAPSED_MS),
        timestampMs = values.longValue(K_TIMESTAMP_MS),
        speedKmH = values.floatValue(K_SPEED_KMH),
        powerKw = values.floatValue(K_POWER_KW),
        voltage = values.floatValue(K_VOLTAGE),
        voltageSag = values.floatValue(K_VOLTAGE_SAG),
        busCurrent = values.floatValue(K_BUS_CURRENT),
        phaseCurrent = values.floatValue(K_PHASE_CURRENT),
        controllerTemp = values.floatValue(K_CONTROLLER_TEMP),
        soc = values.floatValue(K_SOC),
        estimatedRangeKm = values.floatValue(K_ESTIMATED_RANGE_KM),
        rpm = values.floatValue(K_RPM),
        efficiencyWhKm = values.floatValue(K_EFFICIENCY_WH_KM),
        avgEfficiencyWhKm = values.floatValue(K_AVG_EFFICIENCY_WH_KM),
        avgNetEfficiencyWhKm = values.floatValue(K_AVG_NET_EFFICIENCY_WH_KM, values.floatValue(K_AVG_EFFICIENCY_WH_KM)),
        avgTractionEfficiencyWhKm = values.floatValue(K_AVG_TRACTION_EFFICIENCY_WH_KM),
        distanceMeters = values.floatValue(K_DISTANCE_METERS),
        totalEnergyWh = values.floatValue(K_TOTAL_ENERGY_WH),
        tractionEnergyWh = values.floatValue(K_TRACTION_ENERGY_WH, values.floatValue(K_TOTAL_ENERGY_WH)),
        regenEnergyWh = values.floatValue(K_REGEN_ENERGY_WH, values.floatValue(K_RECOVERED_ENERGY_WH)),
        recoveredEnergyWh = values.floatValue(K_RECOVERED_ENERGY_WH),
        maxControllerTemp = values.floatValue(K_MAX_CONTROLLER_TEMP),
        gradePercent = values.finiteFloat(K_GRADE_PERCENT),
        altitudeMeters = values.finiteDouble(K_ALTITUDE_METERS),
        latitude = values.finiteDouble(K_LATITUDE),
        longitude = values.finiteDouble(K_LONGITUDE)
    )

    fun toSyncSnapshot(sample: RideMetricSample): SyncRideMetricSample = SyncRideMetricSample(
        elapsedMs = sample.elapsedMs,
        timestampMs = sample.timestampMs,
        speedKmH = sample.speedKmH,
        powerKw = sample.powerKw,
        voltage = sample.voltage,
        voltageSag = sample.voltageSag,
        busCurrent = sample.busCurrent,
        phaseCurrent = sample.phaseCurrent,
        controllerTemp = sample.controllerTemp,
        soc = sample.soc,
        estimatedRangeKm = sample.estimatedRangeKm,
        rpm = sample.rpm,
        efficiencyWhKm = sample.efficiencyWhKm,
        avgEfficiencyWhKm = sample.avgEfficiencyWhKm,
        avgNetEfficiencyWhKm = sample.avgNetEfficiencyWhKm,
        avgTractionEfficiencyWhKm = sample.avgTractionEfficiencyWhKm,
        distanceMeters = sample.distanceMeters,
        totalEnergyWh = sample.totalEnergyWh,
        tractionEnergyWh = sample.tractionEnergyWh,
        regenEnergyWh = sample.regenEnergyWh,
        recoveredEnergyWh = sample.recoveredEnergyWh,
        maxControllerTemp = sample.maxControllerTemp,
        gradePercent = sample.gradePercent?.takeIf { it.isFinite() },
        altitudeMeters = sample.altitudeMeters?.takeIf { it.isFinite() },
        latitude = sample.latitude?.takeIf { it.isFinite() },
        longitude = sample.longitude?.takeIf { it.isFinite() }
    )

    fun fromSyncSnapshot(snapshot: SyncRideMetricSample): RideMetricSample = RideMetricSample(
        elapsedMs = snapshot.elapsedMs,
        timestampMs = snapshot.timestampMs,
        speedKmH = snapshot.speedKmH,
        powerKw = snapshot.powerKw,
        voltage = snapshot.voltage,
        voltageSag = snapshot.voltageSag,
        busCurrent = snapshot.busCurrent,
        phaseCurrent = snapshot.phaseCurrent,
        controllerTemp = snapshot.controllerTemp,
        soc = snapshot.soc,
        estimatedRangeKm = snapshot.estimatedRangeKm,
        rpm = snapshot.rpm,
        efficiencyWhKm = snapshot.efficiencyWhKm,
        avgEfficiencyWhKm = snapshot.avgEfficiencyWhKm,
        avgNetEfficiencyWhKm = snapshot.avgNetEfficiencyWhKm,
        avgTractionEfficiencyWhKm = snapshot.avgTractionEfficiencyWhKm,
        distanceMeters = snapshot.distanceMeters,
        totalEnergyWh = snapshot.totalEnergyWh,
        tractionEnergyWh = snapshot.tractionEnergyWh,
        regenEnergyWh = snapshot.regenEnergyWh,
        recoveredEnergyWh = snapshot.recoveredEnergyWh,
        maxControllerTemp = snapshot.maxControllerTemp,
        gradePercent = snapshot.gradePercent?.takeIf { it.isFinite() },
        altitudeMeters = snapshot.altitudeMeters?.takeIf { it.isFinite() },
        latitude = snapshot.latitude?.takeIf { it.isFinite() },
        longitude = snapshot.longitude?.takeIf { it.isFinite() }
    )
}

private fun JSONObject.optFiniteDouble(name: String): Double? {
    val rawValue = try {
        opt(name)
    } catch (_: JSONException) {
        null
    } ?: return null
    if (rawValue == JSONObject.NULL) return null
    return when (rawValue) {
        is Number -> rawValue.toDouble()
        is String -> rawValue.toDoubleOrNull()
        else -> rawValue.toString().toDoubleOrNull()
    }?.takeIf { it.isFinite() }
}

private fun JSONObject.optFiniteFloat(name: String): Float? =
    optFiniteDouble(name)?.toFloat()?.takeIf { it.isFinite() }

private fun JSONObject.optValue(name: String): Any? {
    val rawValue = try {
        opt(name)
    } catch (_: JSONException) {
        null
    }
    return rawValue?.takeUnless { it == JSONObject.NULL }
}

private fun Map<String, Any?>.longValue(name: String, default: Long = 0L): Long =
    when (val raw = this[name]) {
        is Number -> raw.toLong()
        is String -> raw.toLongOrNull() ?: default
        else -> default
    }

private fun Map<String, Any?>.floatValue(name: String, default: Float = 0f): Float =
    when (val raw = this[name]) {
        is Number -> raw.toFloat()
        is String -> raw.toFloatOrNull() ?: default
        else -> default
    }

private fun Map<String, Any?>.finiteDouble(name: String): Double? =
    when (val raw = this[name]) {
        is Number -> raw.toDouble()
        is String -> raw.toDoubleOrNull()
        else -> null
    }?.takeIf { it.isFinite() }

private fun Map<String, Any?>.finiteFloat(name: String): Float? =
    finiteDouble(name)?.toFloat()?.takeIf { it.isFinite() }

data class RideHistoryRecord(
    val id: String,
    val title: String,
    val startedAtMs: Long,
    val endedAtMs: Long,
    val durationMs: Long,
    val distanceMeters: Float,
    val maxSpeedKmh: Float,
    val avgSpeedKmh: Float,
    val peakPowerKw: Float,
    val totalEnergyWh: EnergyWh,
    val tractionEnergyWh: EnergyWh = 0.0f,
    val regenEnergyWh: EnergyWh = 0.0f,
    val avgEfficiencyWhKm: EfficiencyWhKm,
    val avgNetEfficiencyWhKm: EfficiencyWhKm = 0.0f,
    val avgTractionEfficiencyWhKm: EfficiencyWhKm = 0.0f,
    val trackPoints: List<RideTrackPoint>,
    val samples: List<RideMetricSample>
) {
    fun toJson(): JSONObject {
        val tracks = JSONArray()
        trackPoints.forEach { tracks.put(it.toJson()) }
        val sampleArray = JSONArray()
        samples.forEach { sampleArray.put(it.toJson()) }
        return JSONObject()
            .put("id", id)
            .put("title", title)
            .put("startedAtMs", startedAtMs)
            .put("endedAtMs", endedAtMs)
            .put("durationMs", durationMs)
            .put("distanceMeters", distanceMeters.toDouble())
            .put("maxSpeedKmh", maxSpeedKmh.toDouble())
            .put("avgSpeedKmh", avgSpeedKmh.toDouble())
            .put("peakPowerKw", peakPowerKw.toDouble())
            .put("totalEnergyWh", totalEnergyWh.toDouble())
            .put("tractionEnergyWh", tractionEnergyWh.toDouble())
            .put("regenEnergyWh", regenEnergyWh.toDouble())
            .put("avgEfficiencyWhKm", avgEfficiencyWhKm.toDouble())
            .put("avgNetEfficiencyWhKm", avgNetEfficiencyWhKm.toDouble())
            .put("avgTractionEfficiencyWhKm", avgTractionEfficiencyWhKm.toDouble())
            .put("trackPoints", tracks)
            .put("samples", sampleArray)
    }

    companion object {
        fun fromJson(json: JSONObject): RideHistoryRecord {
            val tracksJson = json.optJSONArray("trackPoints") ?: JSONArray()
            val samplesJson = json.optJSONArray("samples") ?: JSONArray()
            val tracks = buildList {
                for (index in 0 until tracksJson.length()) {
                    add(RideTrackPoint.fromJson(tracksJson.getJSONObject(index)))
                }
            }
            val samples = buildList {
                for (index in 0 until samplesJson.length()) {
                    add(RideMetricSample.fromJson(samplesJson.getJSONObject(index)))
                }
            }
            return RideHistoryRecord(
                id = json.optString("id"),
                title = json.optString("title"),
                startedAtMs = json.optLong("startedAtMs", 0L),
                endedAtMs = json.optLong("endedAtMs", 0L),
                durationMs = json.optLong("durationMs", 0L),
                distanceMeters = json.optDouble("distanceMeters", 0.0).toFloat(),
                maxSpeedKmh = json.optDouble("maxSpeedKmh", 0.0).toFloat(),
                avgSpeedKmh = json.optDouble("avgSpeedKmh", 0.0).toFloat(),
                peakPowerKw = json.optDouble("peakPowerKw", 0.0).toFloat(),
                totalEnergyWh = json.optDouble("totalEnergyWh", 0.0).toFloat(),
                tractionEnergyWh = json.optDouble("tractionEnergyWh", json.optDouble("totalEnergyWh", 0.0)).toFloat(),
                regenEnergyWh = json.optDouble("regenEnergyWh", 0.0).toFloat(),
                avgEfficiencyWhKm = json.optDouble("avgEfficiencyWhKm", 0.0).toFloat(),
                avgNetEfficiencyWhKm = json.optDouble("avgNetEfficiencyWhKm", json.optDouble("avgEfficiencyWhKm", 0.0)).toFloat(),
                avgTractionEfficiencyWhKm = json.optDouble("avgTractionEfficiencyWhKm", 0.0).toFloat(),
                trackPoints = tracks,
                samples = samples
            )
        }

        fun listToJson(records: List<RideHistoryRecord>): String {
            val array = JSONArray()
            records.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun listFromJson(raw: String?): List<RideHistoryRecord> {
            if (raw.isNullOrBlank()) return emptyList()
            return runCatching {
                val array = JSONArray(raw)
                buildList {
                    for (index in 0 until array.length()) {
                        add(fromJson(array.getJSONObject(index)))
                    }
                }
            }.getOrElse { emptyList() }
        }
    }
}
