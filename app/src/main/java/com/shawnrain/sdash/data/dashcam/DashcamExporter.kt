package com.shawnrain.sdash.data.dashcam

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.shawnrain.sdash.debug.AppLogger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import androidx.media3.transformer.Effects
import androidx.media3.common.Effect

class DashcamExporter(private val context: Context, private val dashcamManager: DashcamManager) {

    sealed class ExportState {
        object Idle : ExportState()
        data class Exporting(val progress: Int) : ExportState()
        data class Success(val uri: Uri?) : ExportState()
        data class Error(val exception: Exception) : ExportState()
    }

    @OptIn(UnstableApi::class)
    fun exportSegments(segments: List<DashcamSegment>): Flow<ExportState> = callbackFlow {
        if (segments.isEmpty()) {
            trySend(ExportState.Error(IllegalArgumentException("No segments to export")))
            close()
            return@callbackFlow
        }

        // Sort by start time just to be safe
        val sortedSegments = segments.sortedBy { it.startedAtMs }
        val overlayConfig = dashcamManager.dashcamOverlayConfig.first()
        val hasWatermark = overlayConfig.showTime || overlayConfig.showSpeed || overlayConfig.showPower ||
                overlayConfig.showDirection || overlayConfig.showVoltage || overlayConfig.showSoc || overlayConfig.showEfficiency

        // Create EditedMediaItemSequence for the Composition
        val editedMediaItems = sortedSegments.map { segment ->
            val builder = EditedMediaItem.Builder(MediaItem.fromUri(segment.videoUri))
            val videoFile = segment.videoUri.path?.let { File(it) }
            if (hasWatermark && videoFile != null && videoFile.exists()) {
                val samples = dashcamManager.repository.getTelemetrySamples(segment)
                val (actualWidth, actualHeight) = VideoWatermarkProcessor.getVideoDimensions(videoFile)
                val effect = VideoWatermarkProcessor.createWatermarkEffect(
                    actualWidth = actualWidth,
                    actualHeight = actualHeight,
                    startedAtMs = segment.startedAtMs,
                    samples = samples,
                    overlayConfig = overlayConfig
                )
                builder.setEffects(Effects(listOf(), listOf(effect)))
            }
            builder.build()
        }
        val sequence = EditedMediaItemSequence(editedMediaItems)
        val composition = Composition.Builder(listOf(sequence)).build()

        // Setup temporary output file
        val tempDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val tempOutputFile = File(tempDir, "temp_export_${System.currentTimeMillis()}.mp4")

        val transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    try {
                        val finalUri = saveToMediaStore(tempOutputFile)
                        tempOutputFile.delete()
                        trySend(ExportState.Success(finalUri))
                        close()
                    } catch (e: Exception) {
                        AppLogger.e("DashcamExporter", "Failed to save to MediaStore: ${e.message}", e)
                        trySend(ExportState.Error(e))
                        close()
                    }
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    AppLogger.e("DashcamExporter", "Export failed: ${exportException.message}", exportException)
                    tempOutputFile.delete()
                    trySend(ExportState.Error(exportException))
                    close()
                }
            })
            .build()

        transformer.start(composition, tempOutputFile.absolutePath)

        // Poll for progress
        val progressHolder = androidx.media3.transformer.ProgressHolder()
        val updateJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            while (isActive) {
                val progressState = transformer.getProgress(progressHolder)
                if (progressState == Transformer.PROGRESS_STATE_AVAILABLE) {
                    trySend(ExportState.Exporting(progressHolder.progress))
                } else if (progressState == Transformer.PROGRESS_STATE_NOT_STARTED) {
                    trySend(ExportState.Exporting(0))
                }
                kotlinx.coroutines.delay(100)
            }
        }

        awaitClose {
            updateJob.cancel()
            transformer.cancel()
            if (tempOutputFile.exists()) {
                tempOutputFile.delete()
            }
        }
    }

    private fun saveToMediaStore(sourceFile: File): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "SmartDash_Merge_$timeStamp.mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/SmartDash")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IllegalStateException("Failed to insert into MediaStore")

        resolver.openOutputStream(uri)?.use { outputStream ->
            sourceFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        return uri
    }
}
