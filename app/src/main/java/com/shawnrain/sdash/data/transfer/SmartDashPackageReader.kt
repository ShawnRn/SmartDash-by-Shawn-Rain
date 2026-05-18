package com.shawnrain.sdash.data.transfer

import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.history.RideHistoryRecord
import com.shawnrain.sdash.data.history.RideHistoryRepository
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.zip.ZipInputStream

data class SmartDashPackageImportResult(
    val settingsImported: Int,
    val ridesImported: Int
)

class SmartDashPackageReader(
    private val settingsRepository: SettingsRepository,
    private val rideHistoryRepository: RideHistoryRepository
) {
    suspend fun importPackageBytes(bytes: ByteArray): SmartDashPackageImportResult {
        var settingsImported = 0
        var ridesImported = 0
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.isDirectory) {
                    zip.closeEntry()
                    continue
                }
                val payload = zip.readBytes().toString(Charsets.UTF_8)
                when {
                    entry.name == "settings.json" -> {
                        settingsImported = settingsRepository.importBackupJson(payload, rideHistoryRepository)
                    }
                    entry.name.startsWith("rides/") && entry.name.endsWith(".json") -> {
                        val vehicleId = vehicleIdFromRideEntry(entry.name)
                        val record = RideHistoryRecord.fromJson(JSONObject(payload))
                        rideHistoryRepository.upsertRide(vehicleId, record)
                        ridesImported += 1
                    }
                }
                zip.closeEntry()
            }
        }
        return SmartDashPackageImportResult(
            settingsImported = settingsImported,
            ridesImported = ridesImported
        )
    }

    private fun vehicleIdFromRideEntry(entryName: String): String {
        val token = entryName.removePrefix("rides/").substringBefore('/')
        return runCatching {
            String(Base64.getUrlDecoder().decode(token), Charsets.UTF_8)
        }.getOrDefault("")
    }
}
