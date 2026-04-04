package com.shawnrain.habe.data

import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

data class VehicleProfile(
    val id: String,
    val name: String,
    val macAddress: String = "",
    val batterySeries: Int = 13,
    val batteryCapacityAh: Float = 50f,
    val wheelCircumferenceMm: Float = 1800f,
    val wheelRimSize: String = "10寸",
    val tireSpecLabel: String = "",
    val polePairs: Int = 50,
    val totalMileageKm: Float = 0f,
    val learnedInternalResistanceOhm: Float = 0f
) {
    fun toJson(): JSONObject {
        return JSONObject()
            .put("id", id)
            .put("name", name)
            .put("macAddress", macAddress)
            .put("batterySeries", batterySeries)
            .put("batteryCapacityAh", batteryCapacityAh.toDouble())
            .put("wheelCircumferenceMm", wheelCircumferenceMm.toDouble())
            .put("wheelRimSize", wheelRimSize)
            .put("tireSpecLabel", tireSpecLabel)
            .put("polePairs", polePairs)
            .put("totalMileageKm", totalMileageKm.toDouble())
            .put("learnedInternalResistanceOhm", learnedInternalResistanceOhm.toDouble())
    }

    companion object {
        const val DEFAULT_ID = "default"

        fun default(): VehicleProfile {
            return VehicleProfile(
                id = DEFAULT_ID,
                name = "默认车辆"
            )
        }

        fun create(
            name: String,
            macAddress: String = "",
            batterySeries: Int = 13,
            batteryCapacityAh: Float = 50f,
            wheelCircumferenceMm: Float = 1800f,
            wheelRimSize: String = "10寸",
            tireSpecLabel: String = "",
            polePairs: Int = 50
        ): VehicleProfile {
            return VehicleProfile(
                id = UUID.randomUUID().toString(),
                name = name.trim().ifBlank { "未命名车辆" },
                macAddress = macAddress.trim(),
                batterySeries = batterySeries.coerceAtLeast(1),
                batteryCapacityAh = batteryCapacityAh.coerceAtLeast(1f),
                wheelCircumferenceMm = wheelCircumferenceMm.coerceIn(500f, 5000f),
                wheelRimSize = wheelRimSize.trim().ifBlank { "10寸" },
                tireSpecLabel = tireSpecLabel.trim(),
                polePairs = polePairs.coerceAtLeast(1)
            )
        }

        fun fromJson(json: JSONObject): VehicleProfile {
            return VehicleProfile(
                id = json.optString("id").ifBlank { UUID.randomUUID().toString() },
                name = json.optString("name").ifBlank { "未命名车辆" },
                macAddress = json.optString("macAddress"),
                batterySeries = json.optInt("batterySeries", 13).coerceAtLeast(1),
                batteryCapacityAh = json.optDouble("batteryCapacityAh", 50.0).toFloat().coerceAtLeast(1f),
                wheelCircumferenceMm = json.optDouble("wheelCircumferenceMm", 1800.0).toFloat().coerceIn(500f, 5000f),
                wheelRimSize = json.optString("wheelRimSize").ifBlank { "10寸" },
                tireSpecLabel = json.optString("tireSpecLabel"),
                polePairs = json.optInt("polePairs", 50).coerceAtLeast(1),
                totalMileageKm = json.optDouble("totalMileageKm", 0.0).toFloat().coerceAtLeast(0f),
                learnedInternalResistanceOhm = json.optDouble("learnedInternalResistanceOhm", 0.0).toFloat().coerceAtLeast(0f)
            )
        }

        fun listToJson(profiles: List<VehicleProfile>): String {
            val array = JSONArray()
            profiles.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun listFromJson(raw: String?): List<VehicleProfile> {
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
