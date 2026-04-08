package com.shawnrain.sdash.data.sync.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PendingMutationEntity::class, SyncMetadataEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SyncDatabase : RoomDatabase() {
    abstract fun pendingMutationDao(): PendingMutationDao
    abstract fun syncMetadataDao(): SyncMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: SyncDatabase? = null

        fun getInstance(context: Context): SyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SyncDatabase::class.java,
                    "smartdash_sync"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
