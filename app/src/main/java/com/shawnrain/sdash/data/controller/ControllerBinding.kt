package com.shawnrain.sdash.data.controller

import org.json.JSONObject

data class ControllerBinding(
    val vehicleId: String,
    val macAddress: String,
    val deviceName: String = "",
    val protocolId: String = "",
    val verifiedAt: Long = 0L,
    val lastConnectedAt: Long = 0L,
    val autoConnectEnabled: Boolean = true,
    val userConfirmed: Boolean = true
) {
    val canAutoConnect: Boolean
        get() = macAddress.isNotBlank() && autoConnectEnabled && userConfirmed

    fun toJson(): JSONObject =
        JSONObject()
            .put("vehicleId", vehicleId)
            .put("macAddress", macAddress)
            .put("deviceName", deviceName)
            .put("protocolId", protocolId)
            .put("verifiedAt", verifiedAt)
            .put("lastConnectedAt", lastConnectedAt)
            .put("autoConnectEnabled", autoConnectEnabled)
            .put("userConfirmed", userConfirmed)

    companion object {
        fun fromJson(raw: String?): ControllerBinding? {
            if (raw.isNullOrBlank()) return null
            return runCatching { fromJson(JSONObject(raw)) }.getOrNull()
        }

        fun fromJson(json: JSONObject): ControllerBinding? {
            val vehicleId = json.optString("vehicleId").trim()
            val macAddress = json.optString("macAddress").trim()
            if (vehicleId.isBlank() || macAddress.isBlank()) return null
            return ControllerBinding(
                vehicleId = vehicleId,
                macAddress = macAddress,
                deviceName = json.optString("deviceName").trim(),
                protocolId = json.optString("protocolId").trim(),
                verifiedAt = json.optLong("verifiedAt", 0L),
                lastConnectedAt = json.optLong("lastConnectedAt", 0L),
                autoConnectEnabled = json.optBoolean("autoConnectEnabled", true),
                userConfirmed = json.optBoolean("userConfirmed", true)
            )
        }

        fun fromLegacy(
            vehicleId: String,
            macAddress: String?,
            deviceName: String?,
            protocolId: String?,
            now: Long = System.currentTimeMillis()
        ): ControllerBinding? {
            val address = macAddress?.trim().orEmpty()
            if (vehicleId.isBlank() || address.isBlank()) return null
            return ControllerBinding(
                vehicleId = vehicleId,
                macAddress = address,
                deviceName = deviceName?.trim().orEmpty(),
                protocolId = protocolId?.trim().orEmpty(),
                verifiedAt = now,
                lastConnectedAt = now,
                autoConnectEnabled = true,
                userConfirmed = true
            )
        }
    }
}
