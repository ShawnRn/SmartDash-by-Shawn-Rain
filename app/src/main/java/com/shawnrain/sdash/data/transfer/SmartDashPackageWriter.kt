package com.shawnrain.sdash.data.transfer

import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.history.RideHistoryRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SmartDashPackageWriter(
    private val settingsRepository: SettingsRepository,
    private val rideHistoryRepository: RideHistoryRepository
) {
    suspend fun buildPackageBytes(): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            val manifest = JSONObject()
                .put("schema", 3)
                .put("format", "smartdash-backup")
                .put("createdAt", System.currentTimeMillis())
                .put("entities", JSONObject())

            zip.writeTextEntry("manifest.json", manifest.toString())
            zip.writeTextEntry(
                "settings.json",
                settingsRepository.exportSettingsBackupJson(excludeLegacyRideHistory = true)
            )

            val rideEntries = JSONArray()
            rideHistoryRepository.listRideHistorySummaries().forEach { summary ->
                val record = rideHistoryRepository.loadRideRecord(summary.id) ?: return@forEach
                val entryName = "rides/${safeToken(summary.vehicleId)}/${safeToken(summary.id)}.json"
                zip.writeTextEntry(entryName, record.toJson().toString())
                rideEntries.put(
                    JSONObject()
                        .put("vehicleId", summary.vehicleId)
                        .put("rideId", summary.id)
                        .put("entry", entryName)
                        .put("updatedAt", summary.updatedAt)
                )
            }
            zip.writeTextEntry("rides_index.json", JSONObject().put("rides", rideEntries).toString())
        }
        return output.toByteArray()
    }

    private fun ZipOutputStream.writeTextEntry(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun safeToken(value: String): String =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.toByteArray(Charsets.UTF_8))
}
