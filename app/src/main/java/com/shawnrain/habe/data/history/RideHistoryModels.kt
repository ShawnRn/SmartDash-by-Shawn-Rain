package com.shawnrain.habe.data.history

import org.json.JSONArray
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
    val motorTemp: Float,
    val controllerTemp: Float,
    val soc: Float,
    val rpm: Float,
    val efficiencyWhKm: Float,
    val distanceMeters: Float,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun toJson(): JSONObject = JSONObject()
        .put("elapsedMs", elapsedMs)
        .put("timestampMs", timestampMs)
        .put("speedKmH", speedKmH.toDouble())
        .put("powerKw", powerKw.toDouble())
        .put("voltage", voltage.toDouble())
        .put("voltageSag", voltageSag.toDouble())
        .put("busCurrent", busCurrent.toDouble())
        .put("phaseCurrent", phaseCurrent.toDouble())
        .put("motorTemp", motorTemp.toDouble())
        .put("controllerTemp", controllerTemp.toDouble())
        .put("soc", soc.toDouble())
        .put("rpm", rpm.toDouble())
        .put("efficiencyWhKm", efficiencyWhKm.toDouble())
        .put("distanceMeters", distanceMeters.toDouble())
        .put("latitude", latitude)
        .put("longitude", longitude)

    companion object {
        fun fromJson(json: JSONObject): RideMetricSample {
            return RideMetricSample(
                elapsedMs = json.optLong("elapsedMs", 0L),
                timestampMs = json.optLong("timestampMs", 0L),
                speedKmH = json.optDouble("speedKmH", 0.0).toFloat(),
                powerKw = json.optDouble("powerKw", 0.0).toFloat(),
                voltage = json.optDouble("voltage", 0.0).toFloat(),
                voltageSag = json.optDouble("voltageSag", 0.0).toFloat(),
                busCurrent = json.optDouble("busCurrent", 0.0).toFloat(),
                phaseCurrent = json.optDouble("phaseCurrent", 0.0).toFloat(),
                motorTemp = json.optDouble("motorTemp", 0.0).toFloat(),
                controllerTemp = json.optDouble("controllerTemp", 0.0).toFloat(),
                soc = json.optDouble("soc", 0.0).toFloat(),
                rpm = json.optDouble("rpm", 0.0).toFloat(),
                efficiencyWhKm = json.optDouble("efficiencyWhKm", 0.0).toFloat(),
                distanceMeters = json.optDouble("distanceMeters", 0.0).toFloat(),
                latitude = json.opt("latitude")?.toString()?.toDoubleOrNull(),
                longitude = json.opt("longitude")?.toString()?.toDoubleOrNull()
            )
        }
    }
}

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
    val totalEnergyWh: Float,
    val avgEfficiencyWhKm: Float,
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
            .put("avgEfficiencyWhKm", avgEfficiencyWhKm.toDouble())
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
                avgEfficiencyWhKm = json.optDouble("avgEfficiencyWhKm", 0.0).toFloat(),
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
