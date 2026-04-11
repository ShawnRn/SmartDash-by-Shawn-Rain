package com.shawnrain.sdash.data.sync.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ride_history_summary",
    indices = [
        Index(value = ["vehicleId", "startedAtMs"]),
        Index(value = ["startedAtMs"])
    ]
)
data class RideHistorySummaryEntity(
    @PrimaryKey
    val id: String,
    val vehicleId: String,
    val title: String,
    val startedAtMs: Long,
    val endedAtMs: Long,
    val durationMs: Long,
    val distanceMeters: Float,
    val maxSpeedKmh: Float,
    val avgSpeedKmh: Float,
    val peakPowerKw: Float,
    val totalEnergyWh: Float,
    val tractionEnergyWh: Float,
    val regenEnergyWh: Float,
    val avgEfficiencyWhKm: Float,
    val avgNetEfficiencyWhKm: Float,
    val avgTractionEfficiencyWhKm: Float,
    val maxControllerTemp: Float,
    val sampleCount: Int,
    val trackPointCount: Int,
    val hasGradeData: Boolean,
    val maxUphillGradePercent: Float,
    val maxDownhillGradePercent: Float,
    val avgSignedGradePercent: Float,
    val hasAltitudeData: Boolean,
    val maxAltitudeMeters: Float,
    val minAltitudeMeters: Float,
    val avgAltitudeMeters: Float,
    val detailSchemaRevision: Int,
    val detailFingerprint: String,
    val updatedAt: Long
)

@Entity(
    tableName = "ride_history_detail",
    foreignKeys = [
        ForeignKey(
            entity = RideHistorySummaryEntity::class,
            parentColumns = ["id"],
            childColumns = ["rideId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["rideId"], unique = true)]
)
data class RideHistoryDetailEntity(
    @PrimaryKey
    val rideId: String,
    val compressedPayload: ByteArray
)
