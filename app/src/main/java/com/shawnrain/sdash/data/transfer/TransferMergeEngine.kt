package com.shawnrain.sdash.data.transfer

import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.data.history.RideHistoryRepository

class TransferMergeEngine(
    settingsRepository: SettingsRepository,
    rideHistoryRepository: RideHistoryRepository
) {
    private val reader = SmartDashPackageReader(settingsRepository, rideHistoryRepository)

    suspend fun mergePackage(bytes: ByteArray): SmartDashPackageImportResult =
        reader.importPackageBytes(bytes)
}
