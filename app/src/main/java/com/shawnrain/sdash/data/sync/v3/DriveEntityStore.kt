package com.shawnrain.sdash.data.sync.v3

import com.shawnrain.sdash.data.sync.GoogleDriveSyncManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DriveEntityStore(
    private val driveSyncManager: GoogleDriveSyncManager
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun fetchManifest(): DriveV3Manifest? {
        val raw = driveSyncManager
            .downloadTextFileByName(MANIFEST_FILE_NAME)
            .getOrElse { error ->
                if (error.message?.startsWith("File not found:") == true) return null
                throw error
            }
        return json.decodeFromString(raw)
    }

    suspend fun uploadManifest(manifest: DriveV3Manifest) {
        driveSyncManager
            .uploadTextFile(MANIFEST_FILE_NAME, json.encodeToString(manifest))
            .getOrElse { throw it }
    }

    suspend fun uploadEntity(
        entryName: String,
        payload: ByteArray,
        existingFileId: String? = null,
        skipLookup: Boolean = false
    ): String =
        driveSyncManager.uploadRawFile(
            fileName = entryName,
            content = payload,
            existingFileIdHint = existingFileId,
            skipLookup = skipLookup
        ).getOrElse { throw it }

    suspend fun downloadEntity(entryName: String): ByteArray =
        driveSyncManager.downloadRawFile(entryName).getOrElse { throw it }

    companion object {
        const val MANIFEST_FILE_NAME = "manifest_v3.json"
    }
}
