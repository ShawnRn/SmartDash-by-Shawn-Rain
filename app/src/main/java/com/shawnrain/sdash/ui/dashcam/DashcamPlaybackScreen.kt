package com.shawnrain.sdash.ui.dashcam

import android.content.Context
import android.content.Intent
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import android.content.ContentValues
import android.provider.MediaStore
import android.widget.Toast
import java.io.FileInputStream
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.SliderDefaults
import androidx.core.view.WindowCompat
import androidx.core.content.FileProvider
import com.shawnrain.sdash.data.dashcam.DashcamManager
import com.shawnrain.sdash.data.dashcam.DashcamSegment
import com.shawnrain.sdash.data.dashcam.DashcamTelemetrySample
import com.shawnrain.sdash.ui.navigation.P2PageHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashcamPlaybackScreen(
    segmentId: String,
    dashcamManager: DashcamManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val segment = remember(segmentId, dashcamManager.repository.segments.collectAsState().value) {
        dashcamManager.repository.segments.value.firstOrNull { it.id == segmentId }
    }

    val overlayConfig by dashcamManager.dashcamOverlayConfig.collectAsState(
        initial = com.shawnrain.sdash.data.dashcam.DashcamOverlayConfig()
    )

    if (segment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到录制片段")
        }
        return
    }

    var samples by remember { mutableStateOf<List<DashcamTelemetrySample>>(emptyList()) }
    LaunchedEffect(segment) {
        samples = dashcamManager.repository.getTelemetrySamples(segment)
    }

    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableStateOf(0) }
    var videoDurationMs by remember { mutableStateOf(0) }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }
    var showFullscreenOverlay by remember { mutableStateOf(true) }

    val activity = remember(context) { context.findActivity() }
    LaunchedEffect(showFullscreenOverlay, isPlaying) {
        if (showFullscreenOverlay && isPlaying) {
            delay(3000)
            showFullscreenOverlay = false
        }
    }

    DisposableEffect(isFullScreen, activity) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        
        if (isFullScreen) {
            controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
        
        onDispose {
            controller.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
    }

    BackHandler(enabled = isFullScreen) {
        isFullScreen = false
    }

    val hudBottomPadding = if (isFullScreen) {
        if (showFullscreenOverlay) 96.dp else 24.dp
    } else {
        8.dp
    }

    var isPortraitVideo by remember { mutableStateOf(false) }
    LaunchedEffect(segment) {
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            val retriever = android.media.MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, segment.videoUri)
                val widthStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val rotationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                val width = widthStr?.toIntOrNull() ?: 1920
                val height = heightStr?.toIntOrNull() ?: 1080
                val rotation = rotationStr?.toIntOrNull() ?: 0
                val actualW = if (rotation == 90 || rotation == 270) height else width
                val actualH = if (rotation == 90 || rotation == 270) width else height
                isPortraitVideo = actualH > actualW
            } catch (e: Exception) {
                com.shawnrain.sdash.debug.AppLogger.e("DashcamPlaybackScreen", "Error reading video dimensions for UI", e)
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    var isTranscoding by remember { mutableStateOf(false) }
    var transcodeProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("watermarked_") && file.name.endsWith(".mp4")) {
                    file.delete()
                }
            }
        }
    }

    fun processVideoWithWatermarkIfRequired(
        onProcessed: (File) -> Unit
    ) {
        val originalFile = segment.videoUri.path?.let { File(it) }
        if (originalFile == null || !originalFile.exists()) {
            Toast.makeText(context, "视频文件不存在", Toast.LENGTH_SHORT).show()
            return
        }

        val hasWatermark = overlayConfig.showTime || overlayConfig.showSpeed || overlayConfig.showPower ||
                overlayConfig.showDirection || overlayConfig.showVoltage || overlayConfig.showSoc || overlayConfig.showEfficiency

        if (!hasWatermark) {
            onProcessed(originalFile)
            return
        }

        val outputFilename = "watermarked_${segment.id}_${System.currentTimeMillis()}.mp4"
        val outputFile = File(context.cacheDir, outputFilename)

        isTranscoding = true
        transcodeProgress = 0f

        com.shawnrain.sdash.data.dashcam.VideoWatermarkProcessor.applyWatermark(
            context = context,
            inputVideoFile = originalFile,
            outputVideoFile = outputFile,
            startedAtMs = segment.startedAtMs,
            samples = samples,
            overlayConfig = overlayConfig,
            onProgress = { progress ->
                transcodeProgress = progress
            },
            onSuccess = {
                isTranscoding = false
                onProcessed(outputFile)
            },
            onFailure = { err ->
                isTranscoding = false
                Toast.makeText(context, "水印生成失败: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }



    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoViewInstance?.let {
                currentPositionMs = it.currentPosition
                videoDurationMs = it.duration
            }
            delay(100)
        }
    }

    val activeSample = remember(currentPositionMs, samples) {
        if (samples.isEmpty()) null
        else samples.minByOrNull { kotlin.math.abs(it.offsetMs - currentPositionMs) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    showFullscreenOverlay = !showFullscreenOverlay
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(segment.videoUri)
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            videoDurationMs = duration
                            start()
                            isPlaying = true
                        }
                        videoViewInstance = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { view ->
                    runCatching {
                        view.pause()
                    }
                    view.post {
                        runCatching {
                            view.stopPlayback()
                        }
                    }
                    videoViewInstance = null
                }
            )

            activeSample?.let { sample ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isPortraitVideo) 24.dp else 32.dp)
                ) {
                    val playbackTimeStr = remember(currentPositionMs, segment.startedAtMs) {
                        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        formatter.format(Date(segment.startedAtMs + currentPositionMs))
                    }

                    val contentTop = @Composable {
                        if (overlayConfig.showTime) {
                            Text(
                                text = "时间: $playbackTimeStr",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        if (overlayConfig.showSpeed && sample.speedKmH != null) {
                            Text(
                                text = "速度: ${String.format("%.1f", sample.speedKmH)} km/h",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        if (overlayConfig.showPower && sample.powerKw != null) {
                            Text(
                                text = "功率: ${String.format("%.2f", sample.powerKw)} kW",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        if (overlayConfig.showDirection && !sample.direction.isNullOrEmpty()) {
                            Text(
                                text = "方向: ${sample.direction}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    val contentBottom = @Composable {
                        if (overlayConfig.showVoltage && sample.voltage != null) {
                            Text(
                                text = "电压: ${String.format("%.1f", sample.voltage)} V",
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        if (overlayConfig.showSoc && sample.soc != null) {
                            Text(
                                text = "电量: ${sample.soc.toInt()}%",
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        if (overlayConfig.showEfficiency && sample.efficiency != null) {
                            Text(
                                text = "能耗: ${String.format("%.1f", sample.efficiency)} Wh/km",
                                color = Color.White,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    if (isPortraitVideo) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            contentTop()
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(bottom = if (showFullscreenOverlay) 100.dp else 24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            contentBottom()
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            contentTop()
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(bottom = if (showFullscreenOverlay) 100.dp else 24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            contentBottom()
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showFullscreenOverlay,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top Bar Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                                )
                            )
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.IconButton(
                                    onClick = onBack
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "返回",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "录像回放",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    val date = Date(segment.startedAtMs)
                                    val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
                                    Text(
                                        text = timeStr,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        processVideoWithWatermarkIfRequired { file ->
                                            shareVideoFile(context, file)
                                        }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = "分享", tint = Color.White)
                                }
                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        processVideoWithWatermarkIfRequired { file ->
                                            saveVideoToGallery(context, file)
                                        }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = "保存", tint = Color.White)
                                }
                                androidx.compose.material3.IconButton(
                                    onClick = { showDeleteConfirmation = true }
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    // Bottom Control Bar Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val curSec = currentPositionMs / 1000
                                val curMin = curSec / 60
                                val curRemSec = curSec % 60

                                val durSec = videoDurationMs / 1000
                                val durMin = durSec / 60
                                val durRemSec = durSec % 60

                                Text(
                                    text = String.format("%02d:%02d", curMin, curRemSec),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = String.format("%02d:%02d", durMin, durRemSec),
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        videoViewInstance?.let {
                                            if (it.isPlaying) {
                                                it.pause()
                                                isPlaying = false
                                            } else {
                                                it.start()
                                                isPlaying = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = "播放暂停",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Slider(
                                    value = if (videoDurationMs > 0) currentPositionMs.toFloat() / videoDurationMs else 0f,
                                    onValueChange = { frac ->
                                        videoViewInstance?.let {
                                            val newPos = (frac * videoDurationMs).toInt()
                                            it.seekTo(newPos)
                                            currentPositionMs = newPos
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.White,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isTranscoding) {
            com.shawnrain.sdash.ui.navigation.BlurredAlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = "正在压制视频水印...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { transcodeProgress },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${(transcodeProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                confirmButton = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            )
        }

        if (showDeleteConfirmation) {
            com.shawnrain.sdash.ui.navigation.BlurredAlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = {
                    Text(
                        text = "删除录像",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("确认要删除这段行车记录仪录像吗？删除后将无法找回。")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmation = false
                            scope.launch {
                                dashcamManager.repository.deleteSegment(segment.id)
                                delay(150)
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("确认删除", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

private fun shareVideoFile(context: Context, file: File) {
    if (!file.exists()) return

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "分享视频"))
}

private fun saveVideoToGallery(context: Context, file: File) {
    if (!file.exists()) return
    try {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "SmartDash_${System.currentTimeMillis()}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/Camera")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val uri = resolver.insert(collection, values)
        if (uri != null) {
            resolver.openOutputStream(uri).use { outputStream ->
                if (outputStream != null) {
                    FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            Toast.makeText(context, "已保存到相册", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "保存失败：无法创建媒体记录", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        com.shawnrain.sdash.debug.AppLogger.e("DashcamPlaybackScreen", "Save to gallery failed", e)
        Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private tailrec fun android.content.Context.findActivity(): android.app.Activity? {
    return when (this) {
        is android.app.Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
