package com.shawnrain.sdash.data.sync.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RideHistoryDao {
    @Query(
        """
        SELECT * FROM ride_history_summary
        WHERE vehicleId = :vehicleId
        ORDER BY startedAtMs DESC
        """
    )
    fun observeSummaries(vehicleId: String): Flow<List<RideHistorySummaryEntity>>

    @Query("SELECT * FROM ride_history_summary ORDER BY startedAtMs DESC")
    suspend fun listAllSummaries(): List<RideHistorySummaryEntity>

    @Query(
        """
        SELECT * FROM ride_history_summary
        WHERE vehicleId = :vehicleId
        ORDER BY startedAtMs DESC
        """
    )
    suspend fun listSummariesForVehicle(vehicleId: String): List<RideHistorySummaryEntity>

    @Query("SELECT * FROM ride_history_detail WHERE rideId = :rideId LIMIT 1")
    suspend fun getDetail(rideId: String): RideHistoryDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSummary(entity: RideHistorySummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDetail(entity: RideHistoryDetailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummaries(entities: List<RideHistorySummaryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetails(entities: List<RideHistoryDetailEntity>)

    @Query("DELETE FROM ride_history_summary WHERE id = :rideId")
    suspend fun deleteRide(rideId: String)

    @Query("DELETE FROM ride_history_summary WHERE vehicleId = :vehicleId")
    suspend fun deleteRidesForVehicle(vehicleId: String)

    @Query("SELECT SUM(distanceMeters) FROM ride_history_summary WHERE vehicleId = :vehicleId")
    suspend fun getTotalDistanceMeters(vehicleId: String): Double?
}
