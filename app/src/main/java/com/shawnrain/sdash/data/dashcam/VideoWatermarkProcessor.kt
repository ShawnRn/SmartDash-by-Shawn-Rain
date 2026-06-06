package com.shawnrain.sdash.data.dashcam

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.TextureOverlay
import androidx.media3.common.Effect
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.google.common.collect.ImmutableList
import com.shawnrain.sdash.debug.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VideoWatermarkProcessor {
    private const val TAG = "VideoWatermarkProcessor"

    fun getVideoDimensions(videoFile: File): Pair<Int, Int> {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(videoFile.absolutePath)
            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val width = widthStr?.toIntOrNull() ?: 1920
            val height = heightStr?.toIntOrNull() ?: 1080
            val rotation = rotationStr?.toIntOrNull() ?: 0
            if (rotation == 90 || rotation == 270) {
                return Pair(height, width)
            }
            return Pair(width, height)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error getting video dimensions", e)
            return Pair(1920, 1080)
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun createWatermarkEffect(
        actualWidth: Int,
        actualHeight: Int,
        startedAtMs: Long,
        samples: List<DashcamTelemetrySample>,
        overlayConfig: DashcamOverlayConfig
    ): Effect {
        val watermarkOverlay = object : BitmapOverlay() {
            private var bitmap: Bitmap? = null
            private var canvas: Canvas? = null
            private val scale = maxOf(actualWidth, actualHeight) / 1920f
            private val baseTextSize = 40f * scale
            private val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = baseTextSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
                setShadowLayer(4f * scale, 2f * scale, 2f * scale, Color.BLACK)
            }
            private val margin = 60f * scale
            private val bottomY = actualHeight - 60f * scale
            private val topY = 90f * scale
            private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            override fun getBitmap(presentationTimeUs: Long): Bitmap {
                if (bitmap == null) {
                    bitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
                    canvas = Canvas(bitmap!!)
                }
                val currentBitmap = bitmap!!
                val currentCanvas = canvas!!

                currentBitmap.eraseColor(Color.TRANSPARENT)
                val offsetMs = presentationTimeUs / 1000
                val sample = if (samples.isEmpty()) null
                else samples.minByOrNull { Math.abs(it.offsetMs - offsetMs) }

                // 左下角：绘制电量、电压、能耗
                val sbLeft = StringBuilder()
                if (overlayConfig.showSoc && sample?.soc != null) {
                    sbLeft.append(String.format("电量: %.0f%%  ", sample.soc))
                }
                if (overlayConfig.showVoltage && sample?.voltage != null) {
                    sbLeft.append(String.format("电压: %.1f V  ", sample.voltage))
                }
                if (overlayConfig.showEfficiency && sample?.efficiency != null) {
                    sbLeft.append(String.format("能耗: %.1f Wh/km", sample.efficiency))
                }
                val leftText = sbLeft.toString().trim()
                if (leftText.isNotEmpty()) {
                    currentCanvas.drawText(leftText, margin, bottomY, textPaint)
                }

                // 右下角：绘制车速、功率、方向
                val sbRight = StringBuilder()
                if (overlayConfig.showSpeed && sample?.speedKmH != null) {
                    sbRight.append(String.format("速度: %.1f km/h  ", sample.speedKmH))
                }
                if (overlayConfig.showPower && sample?.powerKw != null) {
                    sbRight.append(String.format("功率: %.2f kW  ", sample.powerKw))
                }
                if (overlayConfig.showDirection && !sample?.direction.isNullOrEmpty()) {
                    sbRight.append(String.format("方向: %s", sample.direction))
                }
                val rightText = sbRight.toString().trim()
                if (rightText.isNotEmpty()) {
                    currentCanvas.drawText(rightText, actualWidth.toFloat() - textPaint.measureText(rightText) - margin, bottomY, textPaint)
                }

                // 右上角（或左上角）：绘制绝对时间戳
                if (overlayConfig.showTime) {
                    val dateStr = timeFormat.format(Date(startedAtMs + offsetMs))
                    currentCanvas.drawText(dateStr, actualWidth.toFloat() - textPaint.measureText(dateStr) - margin, topY, textPaint)
                }

                return currentBitmap
            }
        }
        return OverlayEffect(ImmutableList.of(watermarkOverlay as TextureOverlay)) as Effect
    }

    fun applyWatermark(
        context: Context,
        inputVideoFile: File,
        outputVideoFile: File,
        startedAtMs: Long,
        samples: List<DashcamTelemetrySample>,
        overlayConfig: DashcamOverlayConfig,
        onProgress: (Float) -> Unit,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        AppLogger.i(TAG, "Starting media3 transcoding watermark for ${inputVideoFile.name} -> ${outputVideoFile.name}")

        try {
            val (actualWidth, actualHeight) = getVideoDimensions(inputVideoFile)
            AppLogger.i(TAG, "Video dimensions: w=$actualWidth, h=$actualHeight")

            val transformer = Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .build()

            val overlayEffect = createWatermarkEffect(actualWidth, actualHeight, startedAtMs, samples, overlayConfig)
            val editedMediaItem = EditedMediaItem.Builder(MediaItem.fromUri(Uri.fromFile(inputVideoFile)))
                .setEffects(Effects(listOf(), listOf(overlayEffect)))
                .build()

            var progressJob: Job? = null

            transformer.addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    AppLogger.i(TAG, "Media3 Transcoding completed successfully")
                    progressJob?.cancel()
                    onSuccess()
                }

                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    AppLogger.e(TAG, "Media3 Transcoding failed", exportException)
                    progressJob?.cancel()
                    onFailure(exportException)
                }
            })

            transformer.start(editedMediaItem, outputVideoFile.absolutePath)

            // 启动轮询进度的协程
            val scope = CoroutineScope(Dispatchers.Main)
            progressJob = scope.launch {
                val progressHolder = ProgressHolder()
                while (true) {
                    val progressState = transformer.getProgress(progressHolder)
                    if (progressState == Transformer.PROGRESS_STATE_AVAILABLE) {
                        onProgress(progressHolder.progress / 100f)
                    }
                    delay(250)
                }
            }

        } catch (e: Exception) {
            AppLogger.e(TAG, "Exception initializing Media3 transformer", e)
            onFailure(e)
        }
    }
}
