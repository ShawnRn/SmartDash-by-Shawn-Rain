package com.shawnrain.sdash.ui.dashcam

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shawnrain.sdash.data.dashcam.DashcamSegment
import com.shawnrain.sdash.debug.AppLogger
import com.shawnrain.sdash.ui.navigation.BlurredAlertDialog
import com.shawnrain.sdash.ui.navigation.BlurredInlineBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import com.shawnrain.sdash.data.dashcam.DashcamExporter
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.border
import kotlinx.coroutines.launch

import com.shawnrain.sdash.data.dashcam.DashcamManager

@Composable
fun DashcamRecordingsSheet(
    isVisible: Boolean,
    segments: List<DashcamSegment>,
    dashcamManager: DashcamManager,
    onDismissRequest: () -> Unit,
    onPlaySegment: (DashcamSegment) -> Unit,
    onDeleteSegment: (String) -> Unit,
    onDeleteAll: () -> Unit
) {
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    var segmentToDelete by remember { mutableStateOf<String?>(null) }
    val selectedSegmentIds = remember { mutableStateListOf<String>() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exporter = remember { DashcamExporter(context, dashcamManager) }
    var exportState by remember { mutableStateOf<DashcamExporter.ExportState>(DashcamExporter.ExportState.Idle) }

    LaunchedEffect(isVisible, segments.size) {
        if (!isVisible || segments.isEmpty()) {
            isEditMode = false
            selectedSegmentIds.clear()
        }
    }

    BlurredInlineBottomSheet(isVisible = isVisible, immersive = true, onDismissRequest = onDismissRequest) { requestDismiss ->
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f)) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "录像回放",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (segments.isNotEmpty()) {
                        TextButton(
                            onClick = { isEditMode = !isEditMode },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isEditMode) "完成" else "选择",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (segments.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无录像",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 2.dp,
                            end = 2.dp,
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 100.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(segments, key = { it.id }) { segment ->
                            RecordingGridItem(
                                segment = segment,
                                isEditMode = isEditMode,
                                isSelected = selectedSegmentIds.contains(segment.id),
                                onClick = {
                                    if (isEditMode) {
                                        if (selectedSegmentIds.contains(segment.id)) {
                                            selectedSegmentIds.remove(segment.id)
                                        } else {
                                            selectedSegmentIds.add(segment.id)
                                        }
                                    } else {
                                        onPlaySegment(segment)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Bar for Edit Mode
            androidx.compose.animation.AnimatedVisibility(
                visible = isEditMode && segments.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isAllSelected = selectedSegmentIds.size == segments.size && segments.isNotEmpty()
                        TextButton(onClick = {
                            if (isAllSelected) {
                                selectedSegmentIds.clear()
                            } else {
                                selectedSegmentIds.clear()
                                selectedSegmentIds.addAll(segments.map { it.id })
                            }
                        }) {
                            Text(if (isAllSelected) "取消全选" else "全选")
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = {
                                    if (selectedSegmentIds.isNotEmpty()) {
                                        scope.launch {
                                            val selected = segments.filter { selectedSegmentIds.contains(it.id) }
                                            exporter.exportSegments(selected).collect { state ->
                                                exportState = state
                                                if (state is DashcamExporter.ExportState.Success) {
                                                    Toast.makeText(context, "导出成功！已保存到相册", Toast.LENGTH_LONG).show()
                                                    isEditMode = false
                                                } else if (state is DashcamExporter.ExportState.Error) {
                                                    Toast.makeText(context, "导出失败: ${state.exception.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    }
                                },
                                enabled = selectedSegmentIds.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("导出 (${selectedSegmentIds.size})", fontWeight = FontWeight.SemiBold)
                            }
                            
                            TextButton(
                                onClick = { 
                                    if (selectedSegmentIds.isNotEmpty()) {
                                        showDeleteAllConfirm = true 
                                    }
                                },
                                enabled = selectedSegmentIds.isNotEmpty()
                            ) {
                                val tint = if (selectedSegmentIds.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                Icon(Icons.Default.Delete, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("删除 (${selectedSegmentIds.size})", color = tint, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteAllConfirm) {
            BlurredAlertDialog(
                onDismissRequest = { showDeleteAllConfirm = false },
                title = { Text("确认删除") },
                text = { Text("此操作将永久删除选中的 ${selectedSegmentIds.size} 个录像片段，是否继续？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteAllConfirm = false
                            val toDelete = selectedSegmentIds.toList()
                            selectedSegmentIds.clear()
                            isEditMode = false
                            toDelete.forEach { onDeleteSegment(it) }
                        }
                    ) {
                        Text("确认删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllConfirm = false }) {
                        Text("取消")
                    }
                }
            )
        }

        if (exportState is DashcamExporter.ExportState.Exporting) {
            val progress = (exportState as DashcamExporter.ExportState.Exporting).progress
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { /* Prevent dismiss */ },
                title = { Text("正在合并导出") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(if (progress >= 0) "进度: $progress%" else "正在处理中...")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { exportState = DashcamExporter.ExportState.Idle }) {
                        Text("后台运行")
                    }
                }
            )
        }

        if (segmentToDelete != null) {
            BlurredAlertDialog(
                onDismissRequest = { segmentToDelete = null },
                title = { Text("删除录像") },
                text = { Text("确认要删除这段录像吗？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteSegment(segmentToDelete!!)
                            segmentToDelete = null
                        }
                    ) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { segmentToDelete = null }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
private fun RecordingGridItem(
    segment: DashcamSegment,
    isEditMode: Boolean,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
            .background(Color.Black)
    ) {
        VideoThumbnail(
            videoUri = segment.videoUri,
            modifier = Modifier.fillMaxSize()
        )

        val date = Date(segment.startedAtMs)
        val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
        val timeStr = timeFormat.format(date)
        
        Text(
            text = timeStr,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )

        val durSec = segment.durationMs / 1000
        val durMin = durSec / 60
        val durRemSec = durSec % 60
        Text(
            text = String.format("%02d:%02d", durMin, durRemSec),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )

        AnimatedVisibility(
            visible = isEditMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoThumbnail(
    videoUri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember(videoUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(videoUri) {
        withContext(Dispatchers.IO) {
            val cacheKey = videoUri.toString()
            val cached = ThumbnailCache.get(cacheKey)
            if (cached != null) {
                bitmap = cached
            } else {
                val retriever = android.media.MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, videoUri)
                    val frame = retriever.frameAtTime
                    if (frame != null) {
                        val scaled = Bitmap.createScaledBitmap(frame, 240, 240, false)
                        ThumbnailCache.put(cacheKey, scaled)
                        bitmap = scaled
                    }
                } catch (e: Exception) {
                    AppLogger.e("VideoThumbnail", "Failed to retrieve thumbnail", e)
                } finally {
                    try {
                        retriever.release()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private object ThumbnailCache {
    private val cache = android.util.LruCache<String, Bitmap>(50)
    
    fun get(key: String): Bitmap? {
        return cache.get(key)
    }
    
    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }
}
