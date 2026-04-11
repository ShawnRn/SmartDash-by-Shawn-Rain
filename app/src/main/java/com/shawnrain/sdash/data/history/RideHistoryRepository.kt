package com.shawnrain.sdash.data.history

import android.content.Context
import androidx.room.withTransaction
import com.shawnrain.sdash.data.sync.room.RideHistoryDao
import com.shawnrain.sdash.data.sync.room.RideHistoryDetailEntity
import com.shawnrain.sdash.data.sync.room.RideHistorySummaryEntity
import com.shawnrain.sdash.data.sync.room.SyncDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class RideHistoryRepository(context: Context) {
    companion object {
        private const val DETAIL_SCHEMA_REVISION = 1
    }

    private val database = SyncDatabase.getInstance(context)
    private val dao: RideHistoryDao = database.rideHistoryDao()
    private val normalizer = RideRecordNormalizer()

    fun observeRideHistorySummaries(vehicleId: String): Flow<List<RideHistorySummary>> =
        dao.observeSummaries(vehicleId).map { list -> list.map { it.toDomain() } }

    suspend fun listRideHistorySummaries(vehicleId: String? = null): List<RideHistorySummary> =
        (if (vehicleId.isNullOrBlank()) {
            dao.listAllSummaries()
        } else {
            dao.listSummariesForVehicle(vehicleId)
        }).map { it.toDomain() }

    suspend fun listRideHistoryRecords(vehicleId: String? = null): List<RideHistoryRecord> =
        listRideHistorySummaries(vehicleId).mapNotNull { summary ->
            loadRideRecord(summary.id)
        }

    suspend fun loadRideRecords(rideIds: Collection<String>): List<RideHistoryRecord> =
        rideIds.mapNotNull { rideId -> loadRideRecord(rideId) }

    suspend fun loadRideRecord(rideId: String): RideHistoryRecord? =
        dao.getDetail(rideId)?.toRideHistoryRecord()

    suspend fun upsertRide(vehicleId: String, record: RideHistoryRecord, updatedAt: Long = System.currentTimeMillis()) {
        val normalized = normalizer.normalize(record)
        val summary = normalized.toSummary(vehicleId = vehicleId, updatedAt = updatedAt)
        val detail = RideHistoryDetailEntity(
            rideId = normalized.id,
            compressedPayload = compressRideRecord(normalized)
        )
        database.withTransaction {
            dao.upsertSummary(summary)
            dao.upsertDetail(detail)
        }
    }

    suspend fun deleteRide(rideId: String) {
        dao.deleteRide(rideId)
    }

    suspend fun replaceRidesForVehicle(vehicleId: String, records: List<RideHistoryRecord>, updatedAt: Long = System.currentTimeMillis()) {
        val normalizedRecords = records.map(normalizer::normalize)
            .sortedByDescending { it.startedAtMs }
        val summaries = normalizedRecords.map { it.toSummary(vehicleId = vehicleId, updatedAt = updatedAt) }
        val details = normalizedRecords.map { record ->
            RideHistoryDetailEntity(
                rideId = record.id,
                compressedPayload = compressRideRecord(record)
            )
        }
        database.withTransaction {
            dao.deleteRidesForVehicle(vehicleId)
            if (summaries.isNotEmpty()) {
                dao.insertSummaries(summaries)
                dao.insertDetails(details)
            }
        }
    }

    private fun RideHistoryRecord.toSummary(vehicleId: String, updatedAt: Long): RideHistorySummaryEntity {
        val summaryStats = computeRideSummaryStats(this)
        val maxControllerTemp = samples.maxOfOrNull { maxOf(it.maxControllerTemp, it.controllerTemp) } ?: 0f
        return RideHistorySummaryEntity(
            id = id,
            vehicleId = vehicleId,
            title = title,
            startedAtMs = startedAtMs,
            endedAtMs = endedAtMs,
            durationMs = durationMs,
            distanceMeters = summaryStats.distanceMeters,
            maxSpeedKmh = summaryStats.maxSpeedKmh,
            avgSpeedKmh = summaryStats.avgSpeedKmh,
            peakPowerKw = peakPowerKw,
            totalEnergyWh = totalEnergyWh,
            tractionEnergyWh = tractionEnergyWh,
            regenEnergyWh = regenEnergyWh,
            avgEfficiencyWhKm = avgEfficiencyWhKm,
            avgNetEfficiencyWhKm = avgNetEfficiencyWhKm,
            avgTractionEfficiencyWhKm = avgTractionEfficiencyWhKm,
            maxControllerTemp = maxControllerTemp,
            sampleCount = samples.size,
            trackPointCount = trackPoints.size,
            hasGradeData = summaryStats.hasGradeData,
            maxUphillGradePercent = summaryStats.maxUphillGradePercent,
            maxDownhillGradePercent = summaryStats.maxDownhillGradePercent,
            avgSignedGradePercent = summaryStats.avgSignedGradePercent,
            hasAltitudeData = summaryStats.hasAltitudeData,
            maxAltitudeMeters = summaryStats.maxAltitudeMeters,
            minAltitudeMeters = summaryStats.minAltitudeMeters,
            avgAltitudeMeters = summaryStats.avgAltitudeMeters,
            detailSchemaRevision = DETAIL_SCHEMA_REVISION,
            detailFingerprint = computeRideDetailFingerprint(trackPoints, samples),
            updatedAt = updatedAt
        )
    }

    private fun RideHistorySummaryEntity.toDomain(): RideHistorySummary =
        RideHistorySummary(
            id = id,
            vehicleId = vehicleId,
            title = title,
            startedAtMs = startedAtMs,
            endedAtMs = endedAtMs,
            durationMs = durationMs,
            distanceMeters = distanceMeters,
            maxSpeedKmh = maxSpeedKmh,
            avgSpeedKmh = avgSpeedKmh,
            peakPowerKw = peakPowerKw,
            totalEnergyWh = totalEnergyWh,
            tractionEnergyWh = tractionEnergyWh,
            regenEnergyWh = regenEnergyWh,
            avgEfficiencyWhKm = avgEfficiencyWhKm,
            avgNetEfficiencyWhKm = avgNetEfficiencyWhKm,
            avgTractionEfficiencyWhKm = avgTractionEfficiencyWhKm,
            maxControllerTemp = maxControllerTemp,
            sampleCount = sampleCount,
            trackPointCount = trackPointCount,
            hasGradeData = hasGradeData,
            maxUphillGradePercent = maxUphillGradePercent,
            maxDownhillGradePercent = maxDownhillGradePercent,
            avgSignedGradePercent = avgSignedGradePercent,
            hasAltitudeData = hasAltitudeData,
            maxAltitudeMeters = maxAltitudeMeters,
            minAltitudeMeters = minAltitudeMeters,
            avgAltitudeMeters = avgAltitudeMeters,
            detailSchemaRevision = detailSchemaRevision,
            detailFingerprint = detailFingerprint,
            updatedAt = updatedAt
        )

    private fun RideHistoryDetailEntity.toRideHistoryRecord(): RideHistoryRecord =
        RideHistoryRecord.fromJson(JSONObject(decompressRideRecord(compressedPayload)))

    private fun compressRideRecord(record: RideHistoryRecord): ByteArray {
        val json = record.toJson().toString()
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(json)
        }
        return output.toByteArray()
    }

    private fun decompressRideRecord(payload: ByteArray): String =
        GZIPInputStream(ByteArrayInputStream(payload)).bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
}
