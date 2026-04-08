package com.shawnrain.sdash.data.speedtest

import org.json.JSONArray
import org.json.JSONObject

data class SpeedTestTrackPoint(
    val latitude: Double,
    val longitude: Double
) {
    fun toJson(): JSONObject = JSONObject()
        .put("lat", latitude)
        .put("lon", longitude)

    companion object {
        fun fromJson(json: JSONObject): SpeedTestTrackPoint {
            return SpeedTestTrackPoint(
                latitude = json.optDouble("lat", 0.0),
                longitude = json.optDouble("lon", 0.0)
            )
        }
    }
}

data class SpeedTestRecord(
    val id: String,
    val label: String,
    val targetSpeedKmh: Float,
    val timeMs: Long,
    val timestampMs: Long,
    val maxSpeedKmh: Float,
    val peakPowerKw: Float,
    val peakBusCurrentA: Float,
    val minVoltage: Float,
    val distanceMeters: Float,
    val trackPoints: List<SpeedTestTrackPoint>
) {
    fun toJson(): JSONObject {
        val points = JSONArray()
        trackPoints.forEach { points.put(it.toJson()) }
        return JSONObject()
            .put("id", id)
            .put("label", label)
            .put("targetSpeedKmh", targetSpeedKmh.toDouble())
            .put("timeMs", timeMs)
            .put("timestampMs", timestampMs)
            .put("maxSpeedKmh", maxSpeedKmh.toDouble())
            .put("peakPowerKw", peakPowerKw.toDouble())
            .put("peakBusCurrentA", peakBusCurrentA.toDouble())
            .put("minVoltage", minVoltage.toDouble())
            .put("distanceMeters", distanceMeters.toDouble())
            .put("trackPoints", points)
    }

    companion object {
        fun fromJson(json: JSONObject): SpeedTestRecord {
            val pointsJson = json.optJSONArray("trackPoints") ?: JSONArray()
            val points = buildList {
                for (index in 0 until pointsJson.length()) {
                    add(SpeedTestTrackPoint.fromJson(pointsJson.getJSONObject(index)))
                }
            }
            return SpeedTestRecord(
                id = json.optString("id"),
                label = json.optString("label"),
                targetSpeedKmh = json.optDouble("targetSpeedKmh", 0.0).toFloat(),
                timeMs = json.optLong("timeMs", 0L),
                timestampMs = json.optLong("timestampMs", System.currentTimeMillis()),
                maxSpeedKmh = json.optDouble("maxSpeedKmh", 0.0).toFloat(),
                peakPowerKw = json.optDouble("peakPowerKw", 0.0).toFloat(),
                peakBusCurrentA = json.optDouble("peakBusCurrentA", 0.0).toFloat(),
                minVoltage = json.optDouble("minVoltage", 0.0).toFloat(),
                distanceMeters = json.optDouble("distanceMeters", 0.0).toFloat(),
                trackPoints = points
            )
        }

        fun listToJson(records: List<SpeedTestRecord>): String {
            val array = JSONArray()
            records.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun listFromJson(raw: String?): List<SpeedTestRecord> {
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

data class SpeedTestSessionUiState(
    val isActive: Boolean = false,
    val isStandby: Boolean = false,
    val targetLabel: String = "",
    val targetSpeedKmh: Float = 0.0f,
    val elapsedMs: Long = 0L,
    val currentSpeedKmh: Float = 0.0f,
    val maxSpeedKmh: Float = 0.0f,
    val peakPowerKw: Float = 0.0f,
    val peakBusCurrentA: Float = 0.0f,
    val minVoltage: Float = 0.0f,
    val distanceMeters: Float = 0.0f,
    val statusText: String = "等待开始"
)
