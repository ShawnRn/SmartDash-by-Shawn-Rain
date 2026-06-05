package com.shawnrain.sdash.ui.dashcam

import android.content.Context
import android.content.Intent
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.FileProvider
import com.shawnrain.sdash.data.dashcam.DashcamManager
import com.shawnrain.sdash.data.dashcam.DashcamSegment
import com.shawnrain.sdash.data.dashcam.DashcamTelemetrySample
import com.shawnrain.sdash.ui.navigation.P2PageHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val date = Date(segment.startedAtMs)
        val subtitle = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
        P2PageHeader(
            title = "录像回放",
            subtitle = subtitle,
            onBack = onBack
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
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
                modifier = Modifier.fillMaxSize()
            )

            activeSample?.let { sample ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (sample.speedKmH != null) {
                            Text(
                                text = "速度: ${String.format("%.1f", sample.speedKmH)} km/h",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        if (sample.powerKw != null) {
                            Text(
                                text = "功率: ${String.format("%.2f", sample.powerKw)} kW",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        if (!sample.direction.isNullOrEmpty()) {
                            Text(
                                text = "方向: ${sample.direction}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (sample.voltage != null) {
                            Text(
                                text = "电压: ${String.format("%.1f", sample.voltage)} V",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        if (sample.soc != null) {
                            Text(
                                text = "电量: ${sample.soc.toInt()}%",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
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
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = String.format("%02d:%02d", durMin, durRemSec),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Slider(
                value = if (videoDurationMs > 0) currentPositionMs.toFloat() / videoDurationMs else 0f,
                onValueChange = { frac ->
                    videoViewInstance?.let {
                        val newPos = (frac * videoDurationMs).toInt()
                        it.seekTo(newPos)
                        currentPositionMs = newPos
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
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
                    }
                ) {
                    Text(if (isPlaying) "暂停" else "播放")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = {
                        shareVideoFile(context, segment)
                    }
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("分享视频")
                }

                TextButton(
                    onClick = {
                        scope.launch {
                            dashcamManager.repository.deleteSegment(segment.id)
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("删除片段")
                }
            }
        }
    }
}

private fun shareVideoFile(context: Context, segment: DashcamSegment) {
    val file = segment.videoUri.path?.let { File(it) } ?: return
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
