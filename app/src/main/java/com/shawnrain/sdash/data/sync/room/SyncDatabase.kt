package com.shawnrain.sdash.data.sync.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PendingMutationEntity::class,
        SyncMetadataEntity::class,
        RideHistorySummaryEntity::class,
        RideHistoryDetailEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SyncDatabase : RoomDatabase() {
    abstract fun pendingMutationDao(): PendingMutationDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun rideHistoryDao(): RideHistoryDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ride_history_summary (
                        id TEXT NOT NULL PRIMARY KEY,
                        vehicleId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        startedAtMs INTEGER NOT NULL,
                        endedAtMs INTEGER NOT NULL,
                        durationMs INTEGER NOT NULL,
                        distanceMeters REAL NOT NULL,
                        maxSpeedKmh REAL NOT NULL,
                        avgSpeedKmh REAL NOT NULL,
                        peakPowerKw REAL NOT NULL,
                        totalEnergyWh REAL NOT NULL,
                        tractionEnergyWh REAL NOT NULL,
                        regenEnergyWh REAL NOT NULL,
                        avgEfficiencyWhKm REAL NOT NULL,
                        avgNetEfficiencyWhKm REAL NOT NULL,
                        avgTractionEfficiencyWhKm REAL NOT NULL,
                        maxControllerTemp REAL NOT NULL,
                        sampleCount INTEGER NOT NULL,
                        trackPointCount INTEGER NOT NULL,
                        hasGradeData INTEGER NOT NULL,
                        maxUphillGradePercent REAL NOT NULL,
                        maxDownhillGradePercent REAL NOT NULL,
                        avgSignedGradePercent REAL NOT NULL,
                        hasAltitudeData INTEGER NOT NULL,
                        maxAltitudeMeters REAL NOT NULL,
                        minAltitudeMeters REAL NOT NULL,
                        avgAltitudeMeters REAL NOT NULL,
                        detailSchemaRevision INTEGER NOT NULL,
                        detailFingerprint TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_ride_history_summary_vehicleId_startedAtMs ON ride_history_summary(vehicleId, startedAtMs)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_ride_history_summary_startedAtMs ON ride_history_summary(startedAtMs)"
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ride_history_detail (
                        rideId TEXT NOT NULL PRIMARY KEY,
                        compressedPayload BLOB NOT NULL,
                        FOREIGN KEY(rideId) REFERENCES ride_history_summary(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_ride_history_detail_rideId ON ride_history_detail(rideId)"
                )
            }
        }

        @Volatile
        private var INSTANCE: SyncDatabase? = null

        fun getInstance(context: Context): SyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SyncDatabase::class.java,
                    "smartdash_sync"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
