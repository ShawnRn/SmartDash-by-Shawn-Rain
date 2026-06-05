package com.shawnrain.sdash.data.dashcam

import android.content.Context
import android.net.Uri
import com.shawnrain.sdash.debug.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

class DashcamRepository(private val context: Context) {
    private val TAG = "DashcamRepository"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val baseDir: File by lazy {
        val dir = context.getExternalFilesDir("dashcam") ?: File(context.filesDir, "dashcam")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    private val _segments = MutableStateFlow<List<DashcamSegment>>(emptyList())
    val segments: StateFlow<List<DashcamSegment>> = _segments.asStateFlow()

    init {
        loadSegmentsSync()
    }

    fun loadSegmentsSync() {
        try {
            val videoFiles = baseDir.listFiles { _, name -> name.endsWith(".mp4") } ?: emptyArray()
            val list = videoFiles.map { videoFile ->
                val id = videoFile.nameWithoutExtension
                val jsonFile = File(baseDir, "$id.json")
                
                val meta = readMetadata(id)
                val duration = meta.durationMs
                val startedAt = meta.startedAt ?: (videoFile.lastModified() - duration)
                val rideId = meta.rideId

                DashcamSegment(
                    id = id,
                    videoUri = Uri.fromFile(videoFile),
                    sidecarUri = if (jsonFile.exists()) Uri.fromFile(jsonFile) else null,
                    startedAtMs = startedAt,
                    endedAtMs = startedAt + duration,
                    durationMs = duration,
                    fileSizeBytes = videoFile.length(),
                    rideId = rideId
                )
            }.sortedByDescending { it.startedAtMs }
            _segments.value = list
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to load segments", e)
        }
    }

    suspend fun loadSegments() = withContext(Dispatchers.IO) {
        loadSegmentsSync()
    }

    @kotlinx.serialization.Serializable
    private data class SegmentMeta(
        val startedAt: Long? = null,
        val durationMs: Long = 0L,
        val rideId: String? = null
    )

    private fun readMetadata(id: String): SegmentMeta {
        val metaFile = File(baseDir, "$id.meta")
        if (metaFile.exists()) {
            return try {
                json.decodeFromString<SegmentMeta>(metaFile.readText())
            } catch (e: Exception) {
                SegmentMeta()
            }
        }
        return SegmentMeta()
    }

    fun saveMetadataSync(id: String, startedAt: Long, durationMs: Long, rideId: String?) {
        try {
            val metaFile = File(baseDir, "$id.meta")
            val meta = SegmentMeta(startedAt, durationMs, rideId)
            metaFile.writeText(json.encodeToString(meta))
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save metadata for $id", e)
        }
    }

    fun saveSidecarSync(id: String, samples: List<DashcamTelemetrySample>) {
        try {
            val sidecarFile = File(baseDir, "$id.json")
            sidecarFile.writeText(json.encodeToString(samples))
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save sidecar for $id", e)
        }
    }

    suspend fun getTelemetrySamples(segment: DashcamSegment): List<DashcamTelemetrySample> = withContext(Dispatchers.IO) {
        segment.sidecarUri?.path?.let { path ->
            val file = File(path)
            if (file.exists()) {
                return@withContext try {
                    json.decodeFromString<List<DashcamTelemetrySample>>(file.readText())
                } catch (e: Exception) {
                    AppLogger.e(TAG, "Failed to parse telemetry sidecar", e)
                    emptyList()
                }
            }
        }
        emptyList()
    }

    suspend fun deleteSegment(id: String) = withContext(Dispatchers.IO) {
        try {
            val videoFile = File(baseDir, "$id.mp4")
            val jsonFile = File(baseDir, "$id.json")
            val metaFile = File(baseDir, "$id.meta")
            if (videoFile.exists()) videoFile.delete()
            if (jsonFile.exists()) jsonFile.delete()
            if (metaFile.exists()) metaFile.delete()
            loadSegmentsSync()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to delete segment $id", e)
        }
    }

    suspend fun deleteAllSegments() = withContext(Dispatchers.IO) {
        try {
            baseDir.listFiles()?.forEach { it.delete() }
            loadSegmentsSync()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to delete all segments", e)
        }
    }

    fun getNewVideoFile(id: String): File {
        return File(baseDir, "$id.mp4")
    }

    suspend fun checkStorageLimitAndCleanup(limitMb: Int) = withContext(Dispatchers.IO) {
        val limitBytes = limitMb.toLong() * 1024 * 1024
        var totalSize = baseDir.listFiles()?.sumOf { it.length() } ?: 0L
        if (totalSize <= limitBytes) return@withContext

        AppLogger.i(TAG, "Storage size ($totalSize bytes) exceeds limit ($limitBytes bytes). Cleaning up old segments.")
        val segmentsToDelete = _segments.value.reversed() // oldest first
        for (seg in segmentsToDelete) {
            if (totalSize <= limitBytes) break
            AppLogger.i(TAG, "Deleting old segment: ${seg.id}")
            val videoFile = File(baseDir, "${seg.id}.mp4")
            val jsonFile = File(baseDir, "${seg.id}.json")
            val metaFile = File(baseDir, "${seg.id}.meta")
            val size = videoFile.length() + jsonFile.length() + metaFile.length()
            if (videoFile.exists()) videoFile.delete()
            if (jsonFile.exists()) jsonFile.delete()
            if (metaFile.exists()) metaFile.delete()
            totalSize -= size
        }
        loadSegmentsSync()
    }
}
