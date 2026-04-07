@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.shawnrain.habe.ui.speedtest

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.data.MetricType
import com.shawnrain.habe.data.history.RideHistoryRecord
import com.shawnrain.habe.data.history.RideMetricSample
import com.shawnrain.habe.data.history.RideTrackPoint
import com.shawnrain.habe.data.speedtest.SpeedTestRecord
import com.shawnrain.habe.data.sync.SyncState
import com.shawnrain.habe.ui.dashboard.BaselineMetricValue
import com.shawnrain.habe.ui.navigation.ApplyDialogWindowBlurEffect
import com.shawnrain.habe.ui.navigation.BlurredAlertDialog
import com.shawnrain.habe.ui.navigation.BlurredModalBottomSheet
import com.shawnrain.habe.ui.navigation.P2PageHeader
import com.shawnrain.habe.ui.navigation.PredictiveBackPopupTransform
import com.shawnrain.habe.ui.navigation.PopupBackdropBlurLayer
import com.shawnrain.habe.ui.navigation.rememberPredictiveBackMotion
import com.shawnrain.habe.ui.text.withDisplaySpacing
import com.shawnrain.habe.ui.theme.bezierPillShape
import com.shawnrain.habe.ui.theme.bezierRoundedShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

private val speedPageContentPadding = PaddingValues(
    start = 20.dp,
    top = 20.dp,
    end = 20.dp,
    bottom = 28.dp
)

private data class SpeedTestOption(
    val label: String,
    val targetSpeedKmh: Float
)

private data class DisplayMetric(
    val value: String,
    val unit: String = ""
)

private data class MetricCardData(
    val title: String,
    val metric: DisplayMetric
)

private enum class SpeedPageTab(val title: String) {
    TEST("性能测试"),
    RIDE_HISTORY("行程记录")
}

private enum class RideChartMetric(val title: String, val unit: String, val color: Color) {
    SPEED("速度", "km/h", Color(0xFF8DB4FF)),
    POWER("功率", "kW", Color(0xFFB7A1FF)),
    VOLTAGE("电压", "V", Color(0xFF8DE0B5)),
    VOLTAGE_SAG("压降", "V", Color(0xFFFFD166)),
    BUS_CURRENT("母线电流", "A", Color(0xFFFFB682)),
    PHASE_CURRENT("相电流", "A", Color(0xFFFF8FA3)),
    CONTROLLER_TEMP("控制器温度", "°C", Color(0xFFFFA86B)),
    EFFICIENCY("能耗", "Wh/km", Color(0xFF8CE0FF)),
    AVG_EFFICIENCY("平均能耗", "Wh/km", Color(0xFF6EE7F2)),
    RANGE("续航", "km", Color(0xFFD8F28F)),
    TOTAL_ENERGY("总能耗", "Wh", Color(0xFFFFB4A2)),
    RECOVERED_ENERGY("回收能量", "Wh", Color(0xFF86EFAC)),
    MAX_CONTROLLER_TEMP("控制器最高温度", "°C", Color(0xFFF59E8B)),
    SOC("电量", "%", Color(0xFF98E57A)),
    RPM("转速", "rpm", Color(0xFFC2C7D0)),
    DISTANCE("里程", "km", Color(0xFFA7F3D0)),
    REGEN_POWER("回收功率", "W", Color(0xFF86EFAC))
}

@Composable
fun SpeedtestScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val metrics by viewModel.metrics.collectAsState()
    val speedTestSession by viewModel.speedTestSession.collectAsState()
    val speedTestHistory by viewModel.speedTestHistory.collectAsState()
    val rideHistory by viewModel.rideHistory.collectAsState()
    val driveSyncState by viewModel.driveSyncState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val options = remember {
        listOf(
            SpeedTestOption("0-25", 25f),
            SpeedTestOption("0-50", 50f),
            SpeedTestOption("0-60", 60f),
            SpeedTestOption("0-100", 100f)
        )
    }
    var selectedOption by remember { mutableStateOf(options[2]) }
    val bestSpeedTestRecord = remember(speedTestHistory, selectedOption) {
        speedTestHistory
            .filter { it.label == selectedOption.label }
            .minByOrNull { it.timeMs }
    }
    var selectedRideRecord by remember { mutableStateOf<RideHistoryRecord?>(null) }
    var selectedRideIds by remember { mutableStateOf(setOf<String>()) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { SpeedPageTab.entries.size }
    )
    val isSyncing = driveSyncState is SyncState.Syncing
    val rideSelectionMode = selectedRideIds.isNotEmpty()
    BackHandler(enabled = rideSelectionMode) {
        selectedRideIds = emptySet()
    }

    LaunchedEffect(speedTestSession.statusText) {
        if (!speedTestSession.isActive && speedTestSession.statusText.isNotBlank() && speedTestSession.statusText != "等待开始") {
            snackbarHostState.showSnackbar(speedTestSession.statusText)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
                    )
                )
            )
        ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(metrics.speedKmH)
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    val current = tabPositions.getOrNull(pagerState.currentPage) ?: return@TabRow
                    val offsetFraction = pagerState.currentPageOffsetFraction
                    val targetIndex = when {
                        offsetFraction > 0f && pagerState.currentPage < tabPositions.lastIndex -> pagerState.currentPage + 1
                        offsetFraction < 0f && pagerState.currentPage > 0 -> pagerState.currentPage - 1
                        else -> pagerState.currentPage
                    }
                    val target = tabPositions.getOrNull(targetIndex) ?: current
                    val fraction = abs(offsetFraction)
                    val indicatorStart = lerp(current.left, target.left, fraction)
                    val indicatorEnd = lerp(current.right, target.right, fraction)
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .wrapContentSize(Alignment.BottomStart)
                            .offset(x = indicatorStart)
                            .width(indicatorEnd - indicatorStart)
                    )
                }
            ) {
                SpeedPageTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(tab.title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (SpeedPageTab.entries[page]) {
                    SpeedPageTab.TEST -> {
                        PullToRefreshBox(
                            isRefreshing = isSyncing,
                            onRefresh = { viewModel.syncDriveNow() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = speedPageContentPadding,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    SpeedTestHero(
                                        option = selectedOption,
                                        speedTestSession = speedTestSession,
                                        bestRecord = bestSpeedTestRecord,
                                        onStart = {
                                            val error = viewModel.startSpeedTest(selectedOption.label, selectedOption.targetSpeedKmh)
                                            if (error != null) {
                                                scope.launch { snackbarHostState.showSnackbar(error) }
                                            }
                                        },
                                        onStop = { viewModel.stopSpeedTest() }
                                    )
                                }
                                item {
                                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                        options.forEachIndexed { index, option ->
                                            SegmentedButton(
                                                selected = selectedOption == option,
                                                onClick = { selectedOption = option },
                                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                                            ) {
                                                Text(option.label)
                                            }
                                        }
                                    }
                                }
                                item {
                                    LiveTelemetryGrid(metrics)
                                }
                                item {
                                    SectionHeader("加速历史", "每条记录都支持分享冲刺海报")
                                }
                                if (speedTestHistory.isEmpty()) {
                                    item { EmptyCard("还没有加速记录", "完成一次 0-25 / 0-50 / 0-60 / 0-100 测试后会出现在这里") }
                                } else {
                                    items(speedTestHistory, key = { it.id }) { record ->
                                        SpeedTestHistoryCard(
                                            record = record,
                                            onShare = {
                                                context.startActivity(viewModel.createSpeedTestShareIntent(record))
                                            },
                                            onDelete = { viewModel.deleteSpeedTestRecord(record.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    SpeedPageTab.RIDE_HISTORY -> {
                        PullToRefreshBox(
                            isRefreshing = isSyncing,
                            onRefresh = { viewModel.syncDriveNow() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = speedPageContentPadding,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    SectionHeader(
                                        "自动行程记录",
                                        "下滑可同步云端最新行程记录。骑行中会自动记录路线、速度、电压、电流、温度、功率和能耗走势"
                                    )
                                }
                                if (rideHistory.isEmpty()) {
                                    item { EmptyCard("还没有行程记录", "开始骑行后系统会自动创建记录，停车一段时间后自动归档") }
                                } else {
                                    items(rideHistory, key = { it.id }) { record ->
                                        RideHistoryCard(
                                            record = record,
                                            selectionMode = rideSelectionMode,
                                            selected = record.id in selectedRideIds,
                                            onOpen = {
                                                if (rideSelectionMode) {
                                                    selectedRideIds = if (record.id in selectedRideIds) {
                                                        selectedRideIds - record.id
                                                    } else {
                                                        selectedRideIds + record.id
                                                    }
                                                } else {
                                                    selectedRideRecord = record
                                                }
                                            },
                                            onLongPress = {
                                                selectedRideIds = selectedRideIds + record.id
                                            },
                                            onShare = {
                                                context.startActivity(viewModel.createRideShareIntent(record))
                                            },
                                            onSaveToAlbum = {
                                                runCatching { viewModel.saveRidePosterToGallery(record) }
                                                    .onSuccess { fileName ->
                                                        scope.launch { snackbarHostState.showSnackbar("已保存到相册: $fileName") }
                                                    }
                                                    .onFailure {
                                                        scope.launch { snackbarHostState.showSnackbar("保存到相册失败") }
                                                    }
                                            },
                                            onExportCsv = {
                                                context.startActivity(viewModel.createRideCsvShareIntent(record))
                                            },
                                            onDelete = { viewModel.deleteRideHistoryRecord(record.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )

        AnimatedVisibility(
            visible = rideSelectionMode,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 84.dp)
        ) {
            Surface(
                shape = bezierRoundedShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "已选 ${selectedRideIds.size} 条",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { selectedRideIds = emptySet() },
                        shape = bezierPillShape()
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            val merged = viewModel.mergeRideHistoryRecords(selectedRideIds)
                            if (merged != null) {
                                selectedRideIds = emptySet()
                                selectedRideRecord = merged
                                scope.launch { snackbarHostState.showSnackbar("已合并行程记录") }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("至少选择 2 条行程记录") }
                            }
                        },
                        enabled = selectedRideIds.size >= 2,
                        shape = bezierPillShape()
                    ) {
                        Text("合并")
                    }
                }
            }
        }
    }

    selectedRideRecord?.let { record ->
        RideHistoryDetailLayer(
            record = record,
            onDismiss = { selectedRideRecord = null },
            onShare = {
                context.startActivity(viewModel.createRideShareIntent(record))
            },
            onSaveToAlbum = {
                runCatching { viewModel.saveRidePosterToGallery(record) }
                    .onSuccess { fileName ->
                        scope.launch { snackbarHostState.showSnackbar("已保存到相册: $fileName") }
                    }
                    .onFailure {
                        scope.launch { snackbarHostState.showSnackbar("保存到相册失败") }
                    }
            },
            onExportCsv = {
                context.startActivity(viewModel.createRideCsvShareIntent(record))
            }
        )
    }
}

@Composable
private fun Header(currentSpeed: Float) {
    P2PageHeader(
        title = "性能与行程",
        subtitle = "当前车速 ${formatFloat(currentSpeed)} km/h"
    )
}

@Composable
private fun SpeedTestHero(
    option: SpeedTestOption,
    speedTestSession: com.shawnrain.habe.data.speedtest.SpeedTestSessionUiState,
    bestRecord: SpeedTestRecord?,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val active = speedTestSession.isActive
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = bezierRoundedShape(30.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
        tonalElevation = 4.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (active) "正在测试 ${speedTestSession.targetLabel}" else "${option.label} 加速测试",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        speedTestSession.statusText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    onClick = { if (active) onStop() else onStart() },
                    shape = bezierPillShape(),
                    color = if (active) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (active) Icons.Default.Stop else Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = if (active) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (active) "停止" else "开始",
                            fontWeight = FontWeight.Bold,
                            color = if (active) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (speedTestSession.elapsedMs > 0L) formatSeconds(speedTestSession.elapsedMs) else "--.--",
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatChip("当前速度", metricOf(speedTestSession.currentSpeedKmh, "km/h"), modifier = Modifier.weight(1f))
                    StatChip("峰值功率", metricOf(speedTestSession.peakPowerKw, "kW"), modifier = Modifier.weight(1f))
                    StatChip("最高速度", metricOf(speedTestSession.maxSpeedKmh, "km/h"), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatChip("母线电流", metricOf(speedTestSession.peakBusCurrentA, "A"), modifier = Modifier.weight(1f))
                    StatChip("最低电压", metricOf(speedTestSession.minVoltage, "V"), modifier = Modifier.weight(1f))
                    StatChip("冲刺距离", metricOf(speedTestSession.distanceMeters, "m"), modifier = Modifier.weight(1f))
                }
                bestRecord?.let { record ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatChip(
                            label = "最佳成绩",
                            metric = metricOf(formatSeconds(record.timeMs)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveTelemetryGrid(metrics: com.shawnrain.habe.ble.VehicleMetrics) {
    val cards = listOf(
        MetricCardData("实时功率", metricOf(metrics.totalPowerW / 1000f, "kW")),
        MetricCardData("平均能耗", metricOf(metrics.avgEfficiencyWhKm, "Wh/km")),
        MetricCardData("电压", metricOf(metrics.voltage, "V")),
        MetricCardData("压降", metricOf(metrics.voltageSag, "V")),
        MetricCardData("母线电流", metricOf(metrics.busCurrent, "A")),
        MetricCardData("相电流", metricOf(metrics.phaseCurrent, "A")),
        MetricCardData("控制器温度", metricOf(metrics.controllerTemp, "°C")),
        MetricCardData("转速", metricOf(metrics.rpm, "rpm"))
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cards.chunked(2).forEach { pair ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { card ->
                    MetricStatCard(
                        title = card.title,
                        metric = card.metric,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SpeedTestHistoryCard(
    record: SpeedTestRecord,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember(record.id) { mutableStateOf(false) }
    var isRemoving by remember(record.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = !isRemoving,
        exit = fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = bezierRoundedShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(record.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(formatDate(record.timestampMs), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        formatSeconds(record.timeMs),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                StatMetricGrid(
                    metrics = listOf(
                        "目标" to metricOf(record.targetSpeedKmh.toInt().toString(), "km/h"),
                        "最高速度" to metricOf(record.maxSpeedKmh, "km/h"),
                        "峰值功率" to metricOf(record.peakPowerKw, "kW"),
                        "峰值母线电流" to metricOf(record.peakBusCurrentA, "A"),
                        "最低电压" to metricOf(record.minVoltage, "V"),
                        "轨迹点" to metricOf(record.trackPoints.size.toString())
                    ),
                    columns = 3
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssistChip(
                        onClick = onShare,
                        label = { Text("分享图") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors()
                    )
                    AssistChip(
                        onClick = { showDeleteConfirm = true },
                        label = { Text("删除") },
                        leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors()
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        BlurredAlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除加速记录？") },
            text = { Text("删除后将无法恢复，这条加速成绩和轨迹会一起移除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        isRemoving = true
                        scope.launch {
                            delay(220)
                            onDelete()
                        }
                    }
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun RideHistoryCard(
    record: RideHistoryRecord,
    selectionMode: Boolean,
    selected: Boolean,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    onShare: () -> Unit,
    onSaveToAlbum: () -> Unit,
    onExportCsv: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember(record.id) { mutableStateOf(false) }
    var showShareActions by remember(record.id) { mutableStateOf(false) }
    var isRemoving by remember(record.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val displayDistanceMeters = remember(record.distanceMeters, record.samples) {
        max(
            record.distanceMeters,
            record.samples.maxOfOrNull { it.distanceMeters } ?: 0f
        )
    }
    val displayAvgSpeedKmh = remember(record.avgSpeedKmh, displayDistanceMeters, record.durationMs) {
        if (record.durationMs > 0L && displayDistanceMeters > 1f) {
            ((displayDistanceMeters / 1000f) / (record.durationMs / 3_600_000f)).coerceAtLeast(0f)
        } else {
            record.avgSpeedKmh
        }
    }
    val summaryMetrics = listOf(
        "里程" to metricOf(displayDistanceMeters / 1000f, "km"),
        "最高速度" to metricOf(record.maxSpeedKmh, "km/h"),
        "平均速度" to metricOf(displayAvgSpeedKmh, "km/h"),
        "峰值功率" to metricOf(record.peakPowerKw, "kW"),
        "能耗" to metricOf(record.avgEfficiencyWhKm, "Wh/km"),
        "采样点" to metricOf(record.samples.size.toString())
    )

    AnimatedVisibility(
        visible = !isRemoving,
        exit = fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onOpen,
                    onLongClick = onLongPress
                ),
            shape = bezierRoundedShape(26.dp),
            color = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
            },
            border = if (selected) {
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.72f))
            } else {
                null
            }
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(formatRideTitle(record.title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        if (selectionMode) {
                            Text(
                                text = if (selected) "已选择" else "点按选择",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "${formatDate(record.startedAtMs)} · ${formatDuration(record.durationMs)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatMetricGrid(
                    metrics = summaryMetrics,
                    columns = 2
                )
                if (!selectionMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HistoryActionChip(
                            onClick = onOpen,
                            text = "详情",
                            icon = { Icon(Icons.Default.AutoGraph, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )
                        HistoryActionChip(
                            onClick = { showShareActions = true },
                            text = "分享",
                            icon = { Icon(Icons.Default.Share, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )
                        HistoryActionChip(
                            onClick = { showDeleteConfirm = true },
                            text = "删除",
                            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            danger = true
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        BlurredAlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除行程记录？") },
            text = { Text("删除后将无法恢复，这条行程、轨迹和图表数据会一起移除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        isRemoving = true
                        scope.launch {
                            delay(220)
                            onDelete()
                        }
                    }
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showShareActions) {
        ShareActionsSheet(
            onDismiss = { showShareActions = false },
            onSharePoster = {
                showShareActions = false
                onShare()
            },
            onSaveToAlbum = {
                showShareActions = false
                onSaveToAlbum()
            },
            onExportCsv = {
                showShareActions = false
                onExportCsv()
            }
        )
    }
}

@Composable
private fun HistoryActionChip(
    onClick: () -> Unit,
    text: String,
    icon: @Composable (() -> Unit)?,
    modifier: Modifier = Modifier,
    danger: Boolean = false
) {
    val labelColor = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    val chipColors = if (danger) {
        AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.error,
            leadingIconContentColor = MaterialTheme.colorScheme.error
        )
    } else {
        AssistChipDefaults.assistChipColors()
    }
    val chipBorder = if (danger) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.55f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f))
    }
    AssistChip(
        onClick = onClick,
        label = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(text, maxLines = 1, softWrap = false, color = labelColor)
            }
        },
        leadingIcon = icon,
        modifier = modifier,
        colors = chipColors,
        border = chipBorder
    )
}

@Composable
private fun RideHistoryDetailLayer(
    record: RideHistoryRecord,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onSaveToAlbum: () -> Unit,
    onExportCsv: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var fullscreenMetric by remember(record.id) { mutableStateOf<RideChartMetric?>(null) }
    var fullscreenRoute by remember(record.id) { mutableStateOf(false) }
    var multiCompareMetrics by remember(record.id) { mutableStateOf<List<RideChartMetric>>(emptyList()) }
    var selectedCompareMetrics by remember(record.id) { mutableStateOf<Set<RideChartMetric>>(emptySet()) }
    var showShareActions by remember(record.id) { mutableStateOf(false) }
    var dismissDragOffset by remember(record.id) { mutableFloatStateOf(0f) }
    val samples = remember(record.id, record.samples) { normalizeRideDetailSamples(record) }
    var selectedIndex by remember(record.id) { mutableIntStateOf((samples.lastIndex).coerceAtLeast(0)) }
    val overviewCards = remember(record.id, samples) {
        buildRideOverviewCards(record, samples, MetricType.entries.toList())
    }
    val navigationBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val enterDurationMs = 300
    val exitDurationMs = 220
    var isSheetVisible by remember(record.id) { mutableStateOf(false) }
    var dismissInFlight by remember(record.id) { mutableStateOf(false) }
    var heavySectionsReady by remember(record.id) { mutableStateOf(false) }
    val entryProgress by animateFloatAsState(
        targetValue = if (isSheetVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isSheetVisible) enterDurationMs else exitDurationMs,
            easing = if (isSheetVisible) FastOutSlowInEasing else FastOutLinearInEasing
        ),
        label = "RideOverlayEntryAlpha"
    )

    fun requestDismiss() {
        if (dismissInFlight) return
        dismissInFlight = true
        isSheetVisible = false
        scope.launch {
            delay(exitDurationMs.toLong())
            onDismiss()
        }
    }
    fun clearCompareSelection() {
        selectedCompareMetrics = emptySet()
    }

    LaunchedEffect(record.id) {
        isSheetVisible = true
        withFrameNanos { }
        delay(120)
        heavySectionsReady = true
    }

    BackHandler(enabled = selectedCompareMetrics.isNotEmpty()) {
        clearCompareSelection()
    }

    Dialog(
        onDismissRequest = {
            if (selectedCompareMetrics.isNotEmpty()) {
                clearCompareSelection()
            } else {
                requestDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyDialogWindowBlurEffect(blurRadius = 28.dp, fullscreen = true)

        Box(modifier = Modifier.fillMaxSize()) {
            // Layer 0: Background Blur (Strictly fixed, only alpha animates)
            PopupBackdropBlurLayer(
                blurRadius = 28.dp,
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.26f * entryProgress),
                onDismissRequest = ::requestDismiss
            )

            // Layer 1: Foreground Content (Animated independently)
            val density = LocalDensity.current
            val config = LocalConfiguration.current
            val screenWidth = config.screenWidthDp.dp
            
            val motion = rememberPredictiveBackMotion(
                width = screenWidth,
                onBack = {
                    if (selectedCompareMetrics.isNotEmpty()) {
                        clearCompareSelection()
                    } else {
                        requestDismiss()
                    }
                },
                maxHorizontalInset = 10.dp,
                maxVerticalInset = 8.dp,
                maxCorner = 20.dp,
                maxScaleTravelFraction = 0.1f
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.90f) // keep more breathing room near system bars
                    .graphicsLayer {
                        val baseScale = 1f - (0.06f * motion.progress)
                        scaleX = baseScale
                        scaleY = baseScale
                        alpha = motion.alpha * entryProgress
                        
                        // Intuitive bottom-origin path: enter from bottom, exit back to bottom.
                        val entryTravelPx = with(density) { (1f - entryProgress) * 72.dp.toPx() }
                        translationY = entryTravelPx + dismissDragOffset + motion.insetVertical.toPx()
                        translationX = motion.translationX
                        
                        // Scale from bottom center to keep it pinned to bottom
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                    }
                    .pointerInput(record.id) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                dismissDragOffset = (dismissDragOffset + dragAmount).coerceAtLeast(0f)
                            },
                            onDragEnd = {
                                if (dismissDragOffset > 96.dp.toPx()) {
                                    requestDismiss()
                                }
                                dismissDragOffset = 0f
                            },
                            onDragCancel = {
                                dismissDragOffset = 0f
                            }
                        )
                    },
                shape = bezierRoundedShape(32.dp + motion.corner),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(5.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        shape = bezierPillShape()
                                    )
                            )
                        }
                        RideHistoryDetailBody(
                            record = record,
                            samples = samples,
                            overviewCards = overviewCards,
                            heavySectionsReady = heavySectionsReady,
                            selectedIndex = selectedIndex,
                            onSelectIndex = { selectedIndex = it },
                            navigationBottomInset = navigationBottomInset,
                            selectedCompareMetrics = selectedCompareMetrics,
                            onMetricClick = { metric ->
                                if (selectedCompareMetrics.isNotEmpty()) {
                                    selectedCompareMetrics = if (metric in selectedCompareMetrics) {
                                        selectedCompareMetrics - metric
                                    } else {
                                        selectedCompareMetrics + metric
                                    }
                                } else {
                                    fullscreenMetric = metric
                                }
                            },
                            onMetricLongPress = { metric ->
                                selectedCompareMetrics = if (metric in selectedCompareMetrics) {
                                    selectedCompareMetrics - metric
                                } else {
                                    selectedCompareMetrics + metric
                                }
                            },
                            onRouteClick = { fullscreenRoute = true },
                            onShowShare = { showShareActions = true },
                            onDismiss = ::requestDismiss
                        )
                    }

                    AnimatedVisibility(
                        visible = selectedCompareMetrics.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                            expandVertically(expandFrom = Alignment.Bottom, animationSpec = tween(220, easing = FastOutSlowInEasing)),
                        exit = fadeOut(animationSpec = tween(140, easing = FastOutLinearInEasing)) +
                            shrinkVertically(shrinkTowards = Alignment.Bottom, animationSpec = tween(180, easing = FastOutLinearInEasing)),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 20.dp, vertical = 16.dp + navigationBottomInset)
                    ) {
                        Surface(
                            shape = bezierRoundedShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            tonalElevation = 10.dp,
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "已选 ${selectedCompareMetrics.size} 项",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { multiCompareMetrics = selectedCompareMetrics.toList() },
                                    shape = bezierPillShape()
                                ) {
                                    Text("打开对照")
                                }
                                OutlinedButton(
                                    onClick = ::clearCompareSelection,
                                    shape = bezierPillShape()
                                ) {
                                    Text("清空")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShareActions) {
        ShareActionsSheet(
            onDismiss = { showShareActions = false },
            onSharePoster = {
                showShareActions = false
                onShare()
            },
            onSaveToAlbum = {
                showShareActions = false
                onSaveToAlbum()
            },
            onExportCsv = {
                showShareActions = false
                onExportCsv()
            }
        )
    }

    fullscreenMetric?.let { metric ->
        RideMetricFullscreenDialog(
            title = formatRideTitle(record.title),
            samples = samples,
            metric = metric,
            onDismiss = { fullscreenMetric = null }
        )
    }

    if (fullscreenRoute) {
        RouteFullscreenDialog(
            record = record,
            samples = samples,
            onDismiss = { fullscreenRoute = false }
        )
    }

    if (multiCompareMetrics.isNotEmpty()) {
        RideMultiMetricCompareDialog(
            title = formatRideTitle(record.title),
            samples = samples,
            metrics = multiCompareMetrics,
            onDismiss = { multiCompareMetrics = emptyList() }
        )
    }
}

@Composable
private fun RideHistoryDetailBody(
    record: RideHistoryRecord,
    samples: List<RideMetricSample>,
    overviewCards: List<RideOverviewCard>,
    heavySectionsReady: Boolean,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    navigationBottomInset: Dp,
    selectedCompareMetrics: Set<RideChartMetric>,
    onMetricClick: (RideChartMetric) -> Unit,
    onMetricLongPress: (RideChartMetric) -> Unit,
    onRouteClick: () -> Unit,
    onShowShare: () -> Unit,
    onDismiss: () -> Unit
) {
    val displayedOverviewCards = remember(record.id) { mutableStateListOf<RideOverviewCard>() }

    LaunchedEffect(overviewCards) {
        displayedOverviewCards.clear()
        displayedOverviewCards.addAll(overviewCards)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 10.dp,
            bottom = 44.dp + navigationBottomInset
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(formatRideTitle(record.title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${formatDate(record.startedAtMs)} · ${formatDuration(record.durationMs)} · ${formatFloat(record.distanceMeters / 1000f)} km",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = bezierRoundedShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .animateContentSize(
                            animationSpec = tween(
                                durationMillis = 220,
                                easing = FastOutSlowInEasing
                            )
                        ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("统计概览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    AnimatedContent(
                        targetState = selectedCompareMetrics.isNotEmpty(),
                        label = "RideCompareHint"
                    ) { selecting ->
                        Text(
                            text = if (!selecting) {
                                "默认展示全部参数卡片；点击任一参数卡片可打开横屏全屏图表，长按可多选对照。"
                            } else {
                                "已选 ${selectedCompareMetrics.size} 项参数；可继续勾选，底部悬浮条可随时打开对照或清空。"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (displayedOverviewCards.isEmpty()) {
                        Text("暂无可查看的采样数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            displayedOverviewCards.chunked(2).forEach { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    row.forEach { card ->
                                        RideOverviewMetricCard(
                                            card = card,
                                            selected = card.metric in selectedCompareMetrics,
                                            onClick = { onMetricClick(card.metric) },
                                            onLongClick = { onMetricLongPress(card.metric) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                        )
                                    }
                                    repeat(2 - row.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            val voltageSagSummary = remember(record.id, samples) { summarizeVoltageSag(samples) }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = bezierRoundedShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("压降分析", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = "平均压降",
                            metric = metricOf(voltageSagSummary.average, "V"),
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "最大压降",
                            metric = metricOf(voltageSagSummary.maximum, "V"),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        item {
            if (heavySectionsReady) {
                RideMetricChart(
                    samples = samples,
                    metric = RideChartMetric.SPEED,
                    selectedIndex = selectedIndex,
                    onSelectIndex = onSelectIndex
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    shape = bezierRoundedShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("图表加载中…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = bezierRoundedShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("GPS 轨迹路线", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (heavySectionsReady) {
                        RoutePreview(
                            record = record,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            onClick = onRouteClick
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("轨迹加载中…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        item {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onShowShare,
                    modifier = Modifier.fillMaxWidth().weight(1f, false).height(54.dp),
                    shape = bezierPillShape()
                ) {
                    Text("分享")
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().weight(1f, false).height(54.dp),
                    shape = bezierPillShape()
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RideHistoryDetailSheet(
    record: RideHistoryRecord,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onSaveToAlbum: () -> Unit,
    onExportCsv: () -> Unit
) {
    var fullscreenMetric by remember(record.id) { mutableStateOf<RideChartMetric?>(null) }
    var showShareActions by remember(record.id) { mutableStateOf(false) }
    val samples = remember(record.id, record.samples) { normalizeRideDetailSamples(record) }
    var selectedIndex by remember(record.id) { mutableIntStateOf((samples.lastIndex).coerceAtLeast(0)) }
    val overviewCards = remember(record.id, samples) {
        buildRideOverviewCards(record, samples, MetricType.entries.toList())
    }
    val navigationBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    BlurredModalBottomSheet(onDismissRequest = onDismiss) {
        PredictiveBackPopupTransform(
            onBack = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 0.dp,
                    bottom = 28.dp + navigationBottomInset
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            item {
                Column {
                    Text(formatRideTitle(record.title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${formatDate(record.startedAtMs)} · ${formatDuration(record.durationMs)} · ${formatFloat(record.distanceMeters / 1000f)} km",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = bezierRoundedShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("统计概览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "卡片展示的是整段行程统计值；底部固定显示速度图表，点击任一参数卡片可打开对应横屏全屏图表。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (overviewCards.isEmpty()) {
                            Text("暂无可查看的采样数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                overviewCards.chunked(2).forEach { row ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        row.forEach { card ->
                                            RideOverviewMetricCard(
                                                card = card,
                                                selected = false,
                                                onClick = { fullscreenMetric = card.metric },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                            )
                                        }
                                        repeat(2 - row.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                val voltageSagSummary = remember(record.id, samples) { summarizeVoltageSag(samples) }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = bezierRoundedShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("压降分析", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricStatCard(
                                title = "平均压降",
                                metric = metricOf(voltageSagSummary.average, "V"),
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "最大压降",
                                metric = metricOf(voltageSagSummary.maximum, "V"),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            item {
                RideMetricChart(
                    samples = samples,
                    metric = RideChartMetric.SPEED,
                    selectedIndex = selectedIndex,
                    onSelectIndex = { selectedIndex = it }
                )
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = bezierRoundedShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("GPS 轨迹路线", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        RoutePreview(
                            record = record,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showShareActions = true },
                        modifier = Modifier.fillMaxWidth().weight(1f, false).height(54.dp),
                        shape = bezierPillShape()
                    ) {
                        Text("分享")
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().weight(1f, false).height(54.dp),
                        shape = bezierPillShape()
                    ) {
                        Text("关闭")
                    }
                }
            }
        }
        }
    }

    if (showShareActions) {
        ShareActionsSheet(
            onDismiss = { showShareActions = false },
            onSharePoster = {
                showShareActions = false
                onShare()
            },
            onSaveToAlbum = {
                showShareActions = false
                onSaveToAlbum()
            },
            onExportCsv = {
                showShareActions = false
                onExportCsv()
            }
        )
    }

    fullscreenMetric?.let { metric ->
        RideMetricFullscreenDialog(
            title = formatRideTitle(record.title),
            samples = samples,
            metric = metric,
            onDismiss = { fullscreenMetric = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareActionsSheet(
    onDismiss: () -> Unit,
    onSharePoster: () -> Unit,
    onSaveToAlbum: () -> Unit,
    onExportCsv: () -> Unit
) {
    BlurredModalBottomSheet(onDismissRequest = onDismiss) {
        PredictiveBackPopupTransform(
            onBack = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("分享与导出", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                RoundedSheetAction(
                    title = "分享分享图",
                    subtitle = "生成海报后调用系统分享面板",
                    icon = { Icon(Icons.Default.Share, contentDescription = null) },
                    onClick = onSharePoster
                )
                RoundedSheetAction(
                    title = "保存到相册",
                    subtitle = "将当前行程分享图保存到系统相册",
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                    onClick = onSaveToAlbum
                )
                RoundedSheetAction(
                    title = "保存 CSV",
                    subtitle = "导出当前行程的原始采样数据",
                    icon = { Icon(Icons.Default.Download, contentDescription = null) },
                    onClick = onExportCsv
                )
            }
        }
    }
}

@Composable
private fun RoundedSheetAction(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = bezierRoundedShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(22.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RideMetricChart(
    samples: List<RideMetricSample>,
    metric: RideChartMetric,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 260.dp,
    landscapeMode: Boolean = false
) {
    val values = remember(samples, metric) { samples.map { it.metricValue(metric) } }
    val summaryValue = remember(samples, metric, values) { rideMetricSummaryValue(samples, metric, values) }
    val summaryLabel = remember(metric) { rideMetricSummaryLabel(metric) }
    val peakIndex = remember(values) { values.indices.maxByOrNull { values[it] } ?: 0 }
    val averageLabelBackground = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
    val density = LocalDensity.current
    val axisTextSizePx = with(density) { if (landscapeMode) 12.sp.toPx() else 11.sp.toPx() }
    val averageLabelSizePx = with(density) { if (landscapeMode) 12.sp.toPx() else 10.sp.toPx() }
    val axisPaint = remember(axisTextSizePx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.LEFT
            textSize = axisTextSizePx
        }
    }
    val averagePaint = remember(averageLabelSizePx) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.RIGHT
            textSize = averageLabelSizePx
        }
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = bezierRoundedShape(if (landscapeMode) 30.dp else 28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
    ) {
        val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        axisPaint.color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f).toArgb()
        averagePaint.color = metric.color.copy(alpha = 0.96f).toArgb()
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = if (landscapeMode) 16.dp else 18.dp)) {
            Text(metric.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "拖动图表查看采样点；带坐标轴，虚线显示当前指标的统一摘要口径。",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(if (landscapeMode) 12.dp else 16.dp))
            RideMetricPlot(
                samples = samples,
                metric = metric,
                selectedIndex = selectedIndex,
                onSelectIndex = onSelectIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight),
                landscapeMode = landscapeMode,
                gridColor = gridColor,
                axisPaint = axisPaint,
                averagePaint = averagePaint
            )
            Spacer(modifier = Modifier.height(12.dp))
            val selectedValue = values.getOrNull(selectedIndex)
            val selectedSample = samples.getOrNull(selectedIndex)
            if (selectedValue != null) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${metric.title} ${formatFloat(selectedValue)} ${metric.unit}",
                        color = metric.color,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$summaryLabel ${formatFloat(summaryValue)} ${metric.unit} · 最高 ${formatFloat(values[peakIndex])} ${metric.unit}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    selectedSample?.let { sample ->
                        Text(
                            "采样时间 ${formatDuration(sample.elapsedMs)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RideMetricPlot(
    samples: List<RideMetricSample>,
    metric: RideChartMetric,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier,
    landscapeMode: Boolean,
    gridColor: Color,
    axisPaint: Paint,
    averagePaint: Paint
) {
    val values = remember(samples, metric) { samples.map { it.metricValue(metric) } }
    val summaryValue = remember(samples, metric, values) { rideMetricSummaryValue(samples, metric, values) }
    val summaryLabel = remember(metric) { rideMetricSummaryLabel(metric) }
    val referenceLine = remember(samples, metric, values, summaryValue, summaryLabel) {
        rideMetricReferenceLine(metric, values, summaryLabel, summaryValue)
    }
    val peakIndex = remember(values) { values.indices.maxByOrNull { values[it] } ?: 0 }
    val averageLabelBackground = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)

    Box(
        modifier = modifier.pointerInput(values) {
            detectDragGestures(
                onDragStart = { offset ->
                    if (values.isNotEmpty()) {
                        val step = size.width / values.size.toFloat().coerceAtLeast(1f)
                        onSelectIndex(((offset.x / step).toInt()).coerceIn(0, values.lastIndex))
                    }
                }
            ) { change, _ ->
                if (values.isNotEmpty()) {
                    val step = size.width / values.size.toFloat().coerceAtLeast(1f)
                    onSelectIndex(((change.position.x / step).toInt()).coerceIn(0, values.lastIndex))
                }
            }
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (values.isEmpty()) return@Canvas
            val paddingLeft = if (landscapeMode) 42.dp.toPx() else 50.dp.toPx()
            val paddingRight = if (landscapeMode) 28.dp.toPx() else 42.dp.toPx()
            val paddingTop = if (landscapeMode) 28.dp.toPx() else 30.dp.toPx()
            val paddingBottom = if (landscapeMode) 30.dp.toPx() else 34.dp.toPx()
            val chartWidth = size.width - paddingLeft - paddingRight
            val chartHeight = size.height - paddingTop - paddingBottom
            val maxValue = values.maxOrNull() ?: 1f
            val minValue = values.minOrNull() ?: 0f
            val span = (maxValue - minValue).takeIf { it > 0.001f } ?: 1f
            val tickValues = listOf(maxValue, maxValue - span / 2f, minValue)
            val averageLabel = referenceLine.label
            val averageLabelWidth = averagePaint.measureText(averageLabel) + 18.dp.toPx()
            val averageLabelHeight = averagePaint.textSize + 12.dp.toPx()

            drawLine(
                color = gridColor.copy(alpha = 0.7f),
                start = Offset(paddingLeft, paddingTop),
                end = Offset(paddingLeft, size.height - paddingBottom),
                strokeWidth = 2f
            )
            drawLine(
                color = gridColor.copy(alpha = 0.7f),
                start = Offset(paddingLeft, size.height - paddingBottom),
                end = Offset(size.width - paddingRight, size.height - paddingBottom),
                strokeWidth = 2f
            )

            tickValues.forEachIndexed { index, tickValue ->
                val y = paddingTop + chartHeight * index / (tickValues.lastIndex.coerceAtLeast(1))
                drawLine(
                    color = gridColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(size.width - paddingRight, y),
                    strokeWidth = 2f
                )
                drawContext.canvas.nativeCanvas.drawText(
                    formatFloat(tickValue),
                    6.dp.toPx(),
                    y + axisPaint.textSize / 3f,
                    axisPaint
                )
            }

            val path = Path()
            values.forEachIndexed { index, value ->
                val x = paddingLeft + chartWidth * index / (values.lastIndex.coerceAtLeast(1))
                val y = paddingTop + chartHeight - ((value - minValue) / span) * chartHeight
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            val referenceStartY = paddingTop + chartHeight - ((referenceLine.startValue - minValue) / span) * chartHeight
            val referenceEndY = paddingTop + chartHeight - ((referenceLine.endValue - minValue) / span) * chartHeight
            val averageLabelTop = 6.dp.toPx()
            val averageLabelLeft = (size.width - paddingRight - averageLabelWidth).coerceAtLeast(paddingLeft + 12.dp.toPx())
            drawLine(
                color = metric.color.copy(alpha = 0.55f),
                start = Offset(paddingLeft, referenceStartY),
                end = Offset(size.width - paddingRight, referenceEndY),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f))
            )
            drawRoundRect(
                color = averageLabelBackground,
                topLeft = Offset(averageLabelLeft, averageLabelTop),
                size = androidx.compose.ui.geometry.Size(averageLabelWidth, averageLabelHeight),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )
            drawContext.canvas.nativeCanvas.drawText(
                averageLabel,
                averageLabelLeft + averageLabelWidth - 10.dp.toPx(),
                averageLabelTop + averageLabelHeight - 8.dp.toPx(),
                averagePaint
            )
            drawPath(
                path = path,
                color = metric.color,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )

            val safeIndex = selectedIndex.coerceIn(0, values.lastIndex)
            val selectedX = paddingLeft + chartWidth * safeIndex / (values.lastIndex.coerceAtLeast(1))
            val selectedY = paddingTop + chartHeight - ((values[safeIndex] - minValue) / span) * chartHeight
            val peakX = paddingLeft + chartWidth * peakIndex / (values.lastIndex.coerceAtLeast(1))
            val peakY = paddingTop + chartHeight - ((values[peakIndex] - minValue) / span) * chartHeight
            drawLine(
                color = Color.White.copy(alpha = 0.55f),
                start = Offset(selectedX, paddingTop),
                end = Offset(selectedX, size.height - paddingBottom),
                strokeWidth = 3f
            )
            drawCircle(color = metric.color.copy(alpha = 0.32f), radius = 16f, center = Offset(peakX, peakY))
            drawCircle(color = metric.color, radius = 10f, center = Offset(peakX, peakY))
            drawCircle(color = metric.color, radius = 12f, center = Offset(selectedX, selectedY))
            drawCircle(color = Color.White, radius = 5f, center = Offset(selectedX, selectedY))

            val midIndex = values.lastIndex / 2
            val xAxisLabels = listOf(
                paddingLeft to formatAxisDuration(samples.firstOrNull()?.elapsedMs ?: 0L),
                (paddingLeft + chartWidth * 0.5f) to formatAxisDuration(samples.getOrNull(midIndex)?.elapsedMs ?: 0L),
                (paddingLeft + chartWidth) to formatAxisDuration(samples.lastOrNull()?.elapsedMs ?: 0L)
            )
            xAxisLabels.forEach { (x, label) ->
                val width = axisPaint.measureText(label)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    (x - width / 2f).coerceIn(paddingLeft, size.width - paddingRight - width),
                    size.height - 8.dp.toPx(),
                    axisPaint
                )
            }
        }
    }
}

@Composable
private fun RideMultiMetricCompareDialog(
    title: String,
    samples: List<RideMetricSample>,
    metrics: List<RideChartMetric>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedIndex by remember(metrics, samples) { mutableIntStateOf((samples.lastIndex).coerceAtLeast(0)) }
    val dialogMetrics = remember(metrics) { metrics.distinct().take(6) }
    val overlaySeries = remember(samples, dialogMetrics) { buildOverlayMetricSeries(samples, dialogMetrics) }
    val mermaidPayload = remember(title, samples, dialogMetrics) {
        buildMultiMetricMermaid(title = title, samples = samples, metrics = dialogMetrics)
    }
    var highlightedMetric by remember(dialogMetrics) { mutableStateOf<RideChartMetric?>(null) }
    var dismissDrag by remember(dialogMetrics) { mutableFloatStateOf(0f) }
    val enterDurationMs = 300
    val exitDurationMs = 220
    var isDialogVisible by remember(dialogMetrics) { mutableStateOf(false) }
    var dismissInFlight by remember(dialogMetrics) { mutableStateOf(false) }
    val entryProgress by animateFloatAsState(
        targetValue = if (isDialogVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isDialogVisible) enterDurationMs else exitDurationMs,
            easing = if (isDialogVisible) FastOutSlowInEasing else FastOutLinearInEasing
        ),
        label = "RideMultiMetricDialogEntry"
    )
    val selectedSample = samples.getOrNull(selectedIndex)

    fun requestDismiss() {
        if (dismissInFlight) return
        dismissInFlight = true
        isDialogVisible = false
        scope.launch {
            delay(exitDurationMs.toLong())
            onDismiss()
        }
    }

    LaunchedEffect(dialogMetrics) {
        isDialogVisible = true
    }

    Dialog(
        onDismissRequest = ::requestDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyDialogWindowBlurEffect(blurRadius = 34.dp, fullscreen = true)
        val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navigationBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PopupBackdropBlurLayer(
                blurRadius = 34.dp,
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.24f * entryProgress),
                onDismissRequest = ::requestDismiss
            )
            val motion = rememberPredictiveBackMotion(
                width = maxWidth,
                onBack = ::requestDismiss,
                maxHorizontalInset = 8.dp,
                maxVerticalInset = 8.dp,
                maxCorner = 22.dp,
                maxScaleTravelFraction = 0.08f
            )
            val outerHorizontal = 4.dp + motion.insetHorizontal
            val outerTop = statusBarInset + 10.dp + motion.insetVertical
            val outerBottom = navigationBarInset + 10.dp + motion.insetVertical
            val availableWidth = (maxWidth - outerHorizontal * 2).coerceAtLeast(240.dp)
            val availableHeight = (maxHeight - outerTop - outerBottom).coerceAtLeast(360.dp)
            val density = LocalDensity.current
            val entryTravelPx = with(density) { (1f - entryProgress) * 56.dp.toPx() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = outerHorizontal, end = outerHorizontal, top = outerTop, bottom = outerBottom)
                    .graphicsLayer {
                        val scale = (1f - (0.06f * motion.progress)) * (0.94f + 0.06f * entryProgress)
                        scaleX = scale
                        scaleY = scale
                        alpha = motion.alpha * entryProgress
                        translationX = motion.translationX
                        translationY = dismissDrag + entryTravelPx
                    }
                    .pointerInput(dialogMetrics) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                dismissDrag = (dismissDrag + dragAmount).coerceAtLeast(0f)
                            },
                            onDragEnd = {
                                if (dismissDrag > 96.dp.toPx()) requestDismiss()
                                dismissDrag = 0f
                            },
                            onDragCancel = { dismissDrag = 0f }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .requiredWidth(availableHeight)
                        .requiredHeight(availableWidth)
                        .graphicsLayer { rotationZ = 90f },
                    shape = bezierRoundedShape(30.dp + motion.corner),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(122.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("参数对照图", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                Text(
                                    title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                                selectedSample?.let { sample ->
                                    Text(
                                        "联动 ${formatAxisDuration(sample.elapsedMs)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    "同图多线归一化",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { shareMermaidText(context, title, mermaidPayload) },
                                    enabled = mermaidPayload.isNotBlank(),
                                    shape = bezierPillShape(),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text("导出")
                                }
                                OutlinedButton(
                                    onClick = ::requestDismiss,
                                    shape = bezierPillShape(),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text("关闭")
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = bezierRoundedShape(26.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RideMultiMetricPlot(
                                    samples = samples,
                                    series = overlaySeries,
                                    highlightedMetric = highlightedMetric,
                                    selectedIndex = selectedIndex,
                                    onSelectIndex = { selectedIndex = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val safeIndex = selectedIndex.coerceIn(0, (samples.lastIndex).coerceAtLeast(0))
                                    overlaySeries.forEach { series ->
                                        val value = series.rawValues.getOrNull(safeIndex) ?: 0f
                                        Surface(
                                            shape = bezierRoundedShape(14.dp),
                                            color = if (series.metric == highlightedMetric) {
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.86f)
                                            } else if (highlightedMetric != null) {
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.58f)
                                            } else {
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
                                            },
                                            border = if (series.metric == highlightedMetric) {
                                                BorderStroke(1.dp, series.metric.color.copy(alpha = 0.82f))
                                            } else {
                                                null
                                            },
                                            onClick = {
                                                highlightedMetric = if (highlightedMetric == series.metric) {
                                                    null
                                                } else {
                                                    series.metric
                                                }
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(series.metric.color, shape = bezierPillShape())
                                                )
                                                Text(
                                                    "${series.metric.title} ${formatFloat(value)} ${series.metric.unit}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class OverlayMetricSeries(
    val metric: RideChartMetric,
    val rawValues: List<Float>,
    val normalizedValues: List<Float>
)

private fun buildOverlayMetricSeries(
    samples: List<RideMetricSample>,
    metrics: List<RideChartMetric>
): List<OverlayMetricSeries> {
    if (samples.isEmpty() || metrics.isEmpty()) return emptyList()
    return metrics.distinct().map { metric ->
        val raw = samples.map { sample -> sample.metricValue(metric) }
        val minValue = raw.minOrNull() ?: 0f
        val maxValue = raw.maxOrNull() ?: minValue
        val span = (maxValue - minValue).takeIf { it > 0.001f } ?: 1f
        val normalized = raw.map { value ->
            ((value - minValue) / span).coerceIn(0f, 1f)
        }
        OverlayMetricSeries(
            metric = metric,
            rawValues = raw,
            normalizedValues = normalized
        )
    }
}

@Composable
private fun RideMultiMetricPlot(
    samples: List<RideMetricSample>,
    series: List<OverlayMetricSeries>,
    highlightedMetric: RideChartMetric?,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val axisPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.LEFT
        }
    }
    val axisTextSizePx = with(density) { 11.sp.toPx() }
    axisPaint.textSize = axisTextSizePx
    axisPaint.color = Color(0xFFC8CFDD).toArgb()

    Box(
        modifier = modifier.pointerInput(samples, series) {
            detectDragGestures(
                onDragStart = { offset ->
                    if (samples.isNotEmpty()) {
                        val step = size.width / samples.size.toFloat().coerceAtLeast(1f)
                        onSelectIndex(((offset.x / step).toInt()).coerceIn(0, samples.lastIndex))
                    }
                }
            ) { change, _ ->
                if (samples.isNotEmpty()) {
                    val step = size.width / samples.size.toFloat().coerceAtLeast(1f)
                    onSelectIndex(((change.position.x / step).toInt()).coerceIn(0, samples.lastIndex))
                }
            }
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (samples.isEmpty() || series.isEmpty()) return@Canvas
            val paddingLeft = 44.dp.toPx()
            val paddingRight = 18.dp.toPx()
            val paddingTop = 20.dp.toPx()
            val paddingBottom = 30.dp.toPx()
            val chartWidth = size.width - paddingLeft - paddingRight
            val chartHeight = size.height - paddingTop - paddingBottom

            drawLine(
                color = Color.White.copy(alpha = 0.44f),
                start = Offset(paddingLeft, paddingTop),
                end = Offset(paddingLeft, size.height - paddingBottom),
                strokeWidth = 1.8f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.44f),
                start = Offset(paddingLeft, size.height - paddingBottom),
                end = Offset(size.width - paddingRight, size.height - paddingBottom),
                strokeWidth = 1.8f
            )

            val ticks = listOf(1f, 0.5f, 0f)
            ticks.forEach { tick ->
                val y = paddingTop + (1f - tick) * chartHeight
                drawLine(
                    color = Color.White.copy(alpha = 0.18f),
                    start = Offset(paddingLeft, y),
                    end = Offset(size.width - paddingRight, y),
                    strokeWidth = 1.5f
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "${(tick * 100).toInt()}%",
                    8.dp.toPx(),
                    y + axisPaint.textSize / 3f,
                    axisPaint
                )
            }

            series.forEach { one ->
                val isHighlighted = highlightedMetric == null || one.metric == highlightedMetric
                val lineColor = if (isHighlighted) {
                    one.metric.color.copy(alpha = if (highlightedMetric == null) 0.95f else 1f)
                } else {
                    Color(0xFF8A8F9A).copy(alpha = 0.42f)
                }
                val path = Path()
                one.normalizedValues.forEachIndexed { index, value ->
                    val x = paddingLeft + chartWidth * index / (one.normalizedValues.lastIndex.coerceAtLeast(1))
                    val y = paddingTop + chartHeight - (value * chartHeight)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = if (isHighlighted) 4.8f else 3.2f,
                        cap = StrokeCap.Round
                    )
                )
            }

            val safeIndex = selectedIndex.coerceIn(0, samples.lastIndex)
            val selectedX = paddingLeft + chartWidth * safeIndex / (samples.lastIndex.coerceAtLeast(1))
            drawLine(
                color = Color.White.copy(alpha = 0.55f),
                start = Offset(selectedX, paddingTop),
                end = Offset(selectedX, size.height - paddingBottom),
                strokeWidth = 2.8f
            )
            series.forEach { one ->
                val value = one.normalizedValues.getOrNull(safeIndex) ?: return@forEach
                val y = paddingTop + chartHeight - (value * chartHeight)
                val isHighlighted = highlightedMetric == null || one.metric == highlightedMetric
                val pointColor = if (isHighlighted) one.metric.color else Color(0xFF8A8F9A)
                drawCircle(
                    color = pointColor.copy(alpha = if (isHighlighted) 0.30f else 0.18f),
                    radius = if (isHighlighted) 10f else 7f,
                    center = Offset(selectedX, y)
                )
                drawCircle(
                    color = pointColor.copy(alpha = if (isHighlighted) 1f else 0.58f),
                    radius = if (isHighlighted) 6f else 4f,
                    center = Offset(selectedX, y)
                )
            }

            val midIndex = samples.lastIndex / 2
            val labels = listOf(
                paddingLeft to formatAxisDuration(samples.firstOrNull()?.elapsedMs ?: 0L),
                (paddingLeft + chartWidth * 0.5f) to formatAxisDuration(samples.getOrNull(midIndex)?.elapsedMs ?: 0L),
                (paddingLeft + chartWidth) to formatAxisDuration(samples.lastOrNull()?.elapsedMs ?: 0L)
            )
            labels.forEach { (x, label) ->
                val textWidth = axisPaint.measureText(label)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    (x - textWidth / 2f).coerceIn(paddingLeft, size.width - paddingRight - textWidth),
                    size.height - 8.dp.toPx(),
                    axisPaint
                )
            }
        }
    }
}

@Composable
private fun RideMetricFullscreenDialog(
    title: String,
    samples: List<RideMetricSample>,
    metric: RideChartMetric,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedIndex by remember(metric, samples) { mutableIntStateOf((samples.lastIndex).coerceAtLeast(0)) }
    var dismissDrag by remember(metric) { mutableFloatStateOf(0f) }
    val enterDurationMs = 320
    val exitDurationMs = 220
    var isDialogVisible by remember(metric) { mutableStateOf(false) }
    var dismissInFlight by remember(metric) { mutableStateOf(false) }
    val values = remember(samples, metric) { samples.map { it.metricValue(metric) } }
    val mermaidPayload = remember(title, samples, metric) {
        buildSingleMetricMermaid(title = title, samples = samples, metric = metric)
    }
    val summaryValue = remember(samples, metric, values) { rideMetricSummaryValue(samples, metric, values) }
    val summaryLabel = remember(metric) { rideMetricSummaryLabel(metric) }
    val peakValue = remember(values) { values.maxOrNull() ?: 0f }
    val selectedValue = values.getOrNull(selectedIndex) ?: 0f
    val selectedSample = samples.getOrNull(selectedIndex)
    val entryProgress by animateFloatAsState(
        targetValue = if (isDialogVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isDialogVisible) enterDurationMs else exitDurationMs,
            easing = if (isDialogVisible) FastOutSlowInEasing else FastOutLinearInEasing
        ),
        label = "RideMetricDialogEntry"
    )

    fun requestDismiss() {
        if (dismissInFlight) return
        dismissInFlight = true
        isDialogVisible = false
        scope.launch {
            delay(exitDurationMs.toLong())
            onDismiss()
        }
    }

    LaunchedEffect(metric) {
        isDialogVisible = true
    }

    Dialog(
        onDismissRequest = ::requestDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyDialogWindowBlurEffect(blurRadius = 34.dp, fullscreen = true)
        val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navigationBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PopupBackdropBlurLayer(
                blurRadius = 34.dp,
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.24f * entryProgress),
                onDismissRequest = ::requestDismiss
            )
            val motion = rememberPredictiveBackMotion(
                width = maxWidth,
                onBack = ::requestDismiss,
                maxHorizontalInset = 8.dp,
                maxVerticalInset = 6.dp,
                maxCorner = 20.dp,
                maxScaleTravelFraction = 0.1f
            )
            val outerHorizontal = 4.dp + motion.insetHorizontal
            val outerTop = statusBarInset + 10.dp + motion.insetVertical
            val outerBottom = navigationBarInset + 10.dp + motion.insetVertical
            val availableWidth = (maxWidth - outerHorizontal * 2).coerceAtLeast(240.dp)
            val availableHeight = (maxHeight - outerTop - outerBottom).coerceAtLeast(360.dp)
            val overlayDensity = LocalDensity.current
            val entryTravelPx = with(overlayDensity) { (1f - entryProgress) * 64.dp.toPx() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = outerHorizontal, end = outerHorizontal, top = outerTop, bottom = outerBottom)
                    .graphicsLayer {
                        val scale = (1f - (0.06f * motion.progress)) * (0.94f + 0.06f * entryProgress)
                        scaleX = scale
                        scaleY = scale
                        alpha = motion.alpha * entryProgress
                        translationX = motion.translationX
                        translationY = dismissDrag + entryTravelPx
                    }
                    .pointerInput(metric) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                dismissDrag = (dismissDrag + dragAmount).coerceAtLeast(0f)
                            },
                            onDragEnd = {
                                if (dismissDrag > 96.dp.toPx()) {
                                    requestDismiss()
                                }
                                dismissDrag = 0f
                            },
                            onDragCancel = {
                                dismissDrag = 0f
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .requiredWidth(availableHeight)
                        .requiredHeight(availableWidth)
                        .graphicsLayer {
                            rotationZ = 90f
                        },
                    shape = bezierRoundedShape(30.dp + motion.corner),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(112.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, maxLines = 2)
                            }
                            Surface(
                                shape = bezierRoundedShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("当前", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${formatFloat(selectedValue)} ${metric.unit}", style = MaterialTheme.typography.titleSmall, color = metric.color, fontWeight = FontWeight.Bold)
                                    Text("$summaryLabel ${formatFloat(summaryValue)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("最高 ${formatFloat(peakValue)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    selectedSample?.let { sample ->
                                        Text("时间 ${formatAxisDuration(sample.elapsedMs)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { shareMermaidText(context, title, mermaidPayload) },
                                    enabled = mermaidPayload.isNotBlank(),
                                    shape = bezierPillShape(),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text("导出")
                                }
                                OutlinedButton(
                                    onClick = ::requestDismiss,
                                    shape = bezierPillShape(),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Text("关闭")
                                }
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = bezierRoundedShape(26.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                RideMetricPlot(
                                    samples = samples,
                                    metric = metric,
                                    selectedIndex = selectedIndex,
                                    onSelectIndex = { selectedIndex = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    landscapeMode = true,
                                    gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                        textAlign = Paint.Align.LEFT
                                        textSize = with(overlayDensity) { 11.sp.toPx() }
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f).toArgb()
                                    },
                                    averagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                        textAlign = Paint.Align.RIGHT
                                        textSize = with(overlayDensity) { 11.sp.toPx() }
                                        color = metric.color.copy(alpha = 0.96f).toArgb()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteFullscreenDialog(
    record: RideHistoryRecord,
    samples: List<RideMetricSample>,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var dismissDrag by remember { mutableFloatStateOf(0f) }
    val enterDurationMs = 320
    val exitDurationMs = 220
    var isDialogVisible by remember { mutableStateOf(false) }
    var dismissInFlight by remember { mutableStateOf(false) }
    val routePoints = remember(record.id, record.samples, record.trackPoints) {
        buildRoutePreviewPoints(record)
    }

    val entryProgress by animateFloatAsState(
        targetValue = if (isDialogVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isDialogVisible) enterDurationMs else exitDurationMs,
            easing = if (isDialogVisible) FastOutSlowInEasing else FastOutLinearInEasing
        ),
        label = "RouteDialogEntry"
    )

    fun requestDismiss() {
        if (dismissInFlight) return
        dismissInFlight = true
        isDialogVisible = false
        scope.launch {
            delay(exitDurationMs.toLong())
            onDismiss()
        }
    }

    LaunchedEffect(record.id) {
        isDialogVisible = true
    }

    Dialog(
        onDismissRequest = ::requestDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyDialogWindowBlurEffect(blurRadius = 34.dp, fullscreen = true)
        val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navigationBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PopupBackdropBlurLayer(
                blurRadius = 34.dp,
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.24f * entryProgress),
                onDismissRequest = ::requestDismiss
            )
            val motion = rememberPredictiveBackMotion(
                width = maxWidth,
                onBack = ::requestDismiss,
                maxHorizontalInset = 8.dp,
                maxVerticalInset = 6.dp,
                maxCorner = 20.dp,
                maxScaleTravelFraction = 0.1f
            )
            val outerHorizontal = 4.dp + motion.insetHorizontal
            val outerTop = statusBarInset + 10.dp + motion.insetVertical
            val outerBottom = navigationBarInset + 10.dp + motion.insetVertical
            val availableWidth = (maxWidth - outerHorizontal * 2).coerceAtLeast(240.dp)
            val availableHeight = (maxHeight - outerTop - outerBottom).coerceAtLeast(360.dp)
            val overlayDensity = LocalDensity.current
            val entryTravelPx = with(overlayDensity) { (1f - entryProgress) * 64.dp.toPx() }
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = entryProgress
                        translationY = entryTravelPx
                    },
                shape = bezierRoundedShape(32.dp + motion.corner),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(5.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    shape = bezierPillShape()
                                )
                        )
                    }
                    // Title bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "轨迹路线",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = formatRideTitle(record.title),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = ::requestDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Route canvas
                    if (routePoints.size >= 2) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            val projection = projectRoute(routePoints, size.width, size.height)
                            if (projection == null) return@Canvas
                            // Draw route segments
                            for (index in 0 until projection.points.lastIndex) {
                                val start = projection.points[index]
                                val end = projection.points[index + 1]
                                val segmentSpeed = ((start.speedKmh + end.speedKmh) * 0.5f).coerceAtLeast(0f)
                                drawLine(
                                    color = routeSpeedColor(
                                        speedKmh = segmentSpeed,
                                        minSpeedKmh = projection.minSpeedKmh,
                                        maxSpeedKmh = projection.maxSpeedKmh
                                    ),
                                    start = start.offset,
                                    end = end.offset,
                                    strokeWidth = 10f,
                                    cap = StrokeCap.Round
                                )
                            }
                            // Start point
                            drawCircle(color = Color(0xFF7DE38D), radius = 16f, center = projection.start.offset)
                            drawCircle(
                                color = Color.White,
                                radius = 8f,
                                center = projection.start.offset
                            )
                            // End point
                            drawCircle(color = Color(0xFFFF9E9E), radius = 16f, center = projection.end.offset)
                            drawCircle(
                                color = Color.White,
                                radius = 8f,
                                center = projection.end.offset
                            )
                        }
                        // Legend
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(Color(0xFF7DE38D), CircleShape)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("起点", style = MaterialTheme.typography.labelMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(Color(0xFFFF9E9E), CircleShape)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("终点", style = MaterialTheme.typography.labelMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(routeSpeedColor(0f, 0f, 60f), CircleShape)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("低速", style = MaterialTheme.typography.labelMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(routeSpeedColor(60f, 0f, 60f), CircleShape)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("高速", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无足够轨迹数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    // Bottom stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "总距离: ${"%.2f".format(record.distanceMeters / 1000)} km",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "时长: ${record.durationMs / 60000} 分钟",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatAxisDuration(durationMs: Long): String {
    val minutes = durationMs / 60000
    val seconds = (durationMs % 60000) / 1000
    return if (minutes > 0) "${minutes}m${seconds}s" else "${seconds}s"
}

private fun shareMermaidText(
    context: Context,
    title: String,
    payload: String
) {
    if (payload.isBlank()) return
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "$title Mermaid")
        putExtra(Intent.EXTRA_TEXT, payload)
    }
    runCatching {
        context.startActivity(Intent.createChooser(shareIntent, "导出 Mermaid"))
    }
}

private fun buildSingleMetricMermaid(
    title: String,
    samples: List<RideMetricSample>,
    metric: RideChartMetric
): String {
    val sampled = downsampleForMermaid(samples)
    if (sampled.isEmpty()) return ""
    val xValues = sampled.indices.map { it.toFloat() }
    val yValues = smoothSeries(sampled.map { it.metricValue(metric) })
    val minValue = yValues.minOrNull() ?: 0f
    val maxValue = yValues.maxOrNull() ?: minValue
    val span = (maxValue - minValue).coerceAtLeast(0.001f)
    val padding = (span * 0.14f).coerceAtLeast(1f)
    val yMin = minValue - padding
    val yMax = maxValue + padding
    val xPadding = 2f
    val xMin = -xPadding
    val xMax = (xValues.lastOrNull() ?: 0f) + xPadding
    val metricLabel = "${metric.title} (${metric.unit})"
    return buildString {
        appendLine("%%{init: {'theme': 'dark'}}%%")
        appendLine("%% 导入 draw.io 时请直接粘贴本段纯 Mermaid 文本")
        appendLine("%% 时间范围: ${formatAxisDuration(sampled.first().elapsedMs)} -> ${formatAxisDuration(sampled.last().elapsedMs)}")
        appendLine("%% 导出点数: ${sampled.size}（已降采样）")
        appendLine("xychart-beta")
        appendLine("    title \"${escapeMermaidText(title)} - ${escapeMermaidText(metric.title)}\"")
        appendLine("    x-axis \"采样序列\" ${formatMermaidAxisNumber(xMin)} --> ${formatMermaidAxisNumber(xMax)}")
        appendLine("    y-axis \"${escapeMermaidText(metricLabel)}\" ${formatMermaidValueNumber(yMin)} --> ${formatMermaidValueNumber(yMax)}")
        appendLine("    line \"${escapeMermaidText(metric.title)}\" [${yValues.joinToString(", ") { formatMermaidValueNumber(it) }}]")
    }
}

private fun buildMultiMetricMermaid(
    title: String,
    samples: List<RideMetricSample>,
    metrics: List<RideChartMetric>
): String {
    val sampled = downsampleForMermaid(samples)
    val distinctMetrics = metrics.distinct().take(6)
    if (sampled.isEmpty() || distinctMetrics.isEmpty()) return ""
    val xValues = sampled.indices.map { it.toFloat() }
    val xPadding = 2f
    val xMin = -xPadding
    val xMax = (xValues.lastOrNull() ?: 0f) + xPadding
    return buildString {
        appendLine("%%{init: {'theme': 'dark'}}%%")
        appendLine("%% 导入 draw.io 时请直接粘贴本段纯 Mermaid 文本")
        appendLine("%% 多参数已归一化到 0-100%，便于同图对照")
        appendLine("%% 时间范围: ${formatAxisDuration(sampled.first().elapsedMs)} -> ${formatAxisDuration(sampled.last().elapsedMs)}")
        appendLine("%% 导出点数: ${sampled.size}（已降采样）")
        appendLine("xychart-beta")
        appendLine("    title \"${escapeMermaidText(title)} - 参数对照图\"")
        appendLine("    x-axis \"采样序列\" ${formatMermaidAxisNumber(xMin)} --> ${formatMermaidAxisNumber(xMax)}")
        appendLine("    y-axis \"归一化(%)\" -8 --> 108")
        distinctMetrics.forEach { metric ->
            val rawValues = sampled.map { it.metricValue(metric) }
            val lowerBound = percentile(rawValues, 0.05f)
            val upperBound = percentile(rawValues, 0.95f)
            val minValue = rawValues.minOrNull() ?: 0f
            val maxValue = rawValues.maxOrNull() ?: minValue
            val span = (upperBound - lowerBound).coerceAtLeast(0.001f)
            val normalized = if (rawValues.size < 2) {
                List(rawValues.size) { 50f }
            } else {
                smoothSeries(
                    rawValues.map { ((it - lowerBound) / span * 100f).coerceIn(2f, 98f) }
                )
            }
            appendLine("%% ${escapeMermaidText(metric.title)} (${metric.unit}) min=${formatMermaidValueNumber(minValue)} max=${formatMermaidValueNumber(maxValue)}")
            appendLine("    line \"${escapeMermaidText(metric.title)}\" [${normalized.joinToString(", ") { formatMermaidValueNumber(it) }}]")
        }
    }
}

private fun downsampleForMermaid(
    samples: List<RideMetricSample>,
    maxPoints: Int = 28
): List<RideMetricSample> {
    if (samples.size <= maxPoints) return samples
    val step = samples.lastIndex.toFloat() / (maxPoints - 1).coerceAtLeast(1)
    return (0 until maxPoints)
        .map { index ->
            val sampleIndex = (index * step).roundToInt().coerceIn(0, samples.lastIndex)
            samples[sampleIndex]
        }
        .distinctBy { it.elapsedMs to it.distanceMeters }
}

private fun escapeMermaidText(text: String): String =
    text.replace('"', '\'')

private fun formatMermaidAxisNumber(value: Float): String =
    String.format(Locale.US, "%.0f", value)

private fun formatMermaidValueNumber(value: Float): String =
    String.format(Locale.US, "%.1f", value)

private fun smoothSeries(values: List<Float>, radius: Int = 1): List<Float> {
    if (values.size <= 2 || radius <= 0) return values
    return values.indices.map { index ->
        val start = (index - radius).coerceAtLeast(0)
        val end = (index + radius).coerceAtMost(values.lastIndex)
        var sum = 0f
        var count = 0
        for (i in start..end) {
            sum += values[i]
            count += 1
        }
        sum / count.coerceAtLeast(1)
    }
}

private fun percentile(values: List<Float>, p: Float): Float {
    if (values.isEmpty()) return 0f
    val sorted = values.sorted()
    val clamped = p.coerceIn(0f, 1f)
    val position = clamped * (sorted.lastIndex)
    val lowerIndex = position.toInt().coerceIn(0, sorted.lastIndex)
    val upperIndex = (lowerIndex + 1).coerceAtMost(sorted.lastIndex)
    val fraction = position - lowerIndex
    return sorted[lowerIndex] * (1f - fraction) + sorted[upperIndex] * fraction
}

private fun rideMetricSummaryLabel(metric: RideChartMetric): String {
    return when (metric) {
        RideChartMetric.AVG_EFFICIENCY -> "整段"
        RideChartMetric.SOC, RideChartMetric.RANGE -> "终点"
        RideChartMetric.DISTANCE, RideChartMetric.TOTAL_ENERGY, RideChartMetric.RECOVERED_ENERGY -> "累计"
        RideChartMetric.MAX_CONTROLLER_TEMP -> "最高"
        else -> "均值"
    }
}

private fun rideMetricSummaryValue(
    samples: List<RideMetricSample>,
    metric: RideChartMetric,
    values: List<Float>
): Float {
    if (samples.isEmpty()) return 0f
    val fallbackAverage = robustRideMetricAverage(samples, metric, values)
    val last = samples.last()
    return when (metric) {
        RideChartMetric.AVG_EFFICIENCY -> when {
            last.avgEfficiencyWhKm > 0.01f -> last.avgEfficiencyWhKm
            last.distanceMeters > 10f && last.totalEnergyWh > 0.01f ->
                last.totalEnergyWh / (last.distanceMeters / 1000f)
            else -> fallbackAverage
        }
        RideChartMetric.SOC -> last.soc.takeIf { it in 1f..100f } ?: fallbackAverage
        RideChartMetric.RANGE -> last.estimatedRangeKm.takeIf { it > 0.01f } ?: fallbackAverage
        RideChartMetric.DISTANCE -> (last.distanceMeters / 1000f).coerceAtLeast(0f)
        RideChartMetric.TOTAL_ENERGY -> last.totalEnergyWh.coerceAtLeast(0f)
        RideChartMetric.RECOVERED_ENERGY -> last.recoveredEnergyWh.coerceAtLeast(0f)
        RideChartMetric.MAX_CONTROLLER_TEMP -> maxOf(last.maxControllerTemp, last.controllerTemp)
        else -> fallbackAverage
    }
}

private fun isMeaningfulAverageSample(sample: RideMetricSample): Boolean {
    return sample.speedKmH >= 2.5f ||
        abs(sample.busCurrent) >= 2f ||
        abs(sample.phaseCurrent) >= 4f ||
        abs(sample.powerKw) >= 0.12f ||
        sample.rpm >= 120f
}

private fun rideAverageSamples(samples: List<RideMetricSample>): List<RideMetricSample> {
    val active = samples.filter(::isMeaningfulAverageSample)
    if (active.isNotEmpty()) return active
    val moving = samples.filter {
        it.speedKmH >= 0.8f ||
            abs(it.busCurrent) >= 0.8f ||
            abs(it.powerKw) >= 0.04f ||
            it.rpm >= 30f
    }
    return moving.ifEmpty { samples }
}

private fun robustAverage(values: List<Float>, trimFraction: Float = 0.08f): Float {
    val clean = values.filter { it.isFinite() }
    if (clean.isEmpty()) return 0f
    if (clean.size < 5) return clean.average().toFloat()
    val sorted = clean.sorted()
    val trimCount = (sorted.size * trimFraction).toInt().coerceAtMost((sorted.size - 1) / 2)
    val trimmed = sorted.subList(trimCount, sorted.size - trimCount)
    return trimmed.average().toFloat()
}

private fun reliableEfficiencyValues(samples: List<RideMetricSample>): List<Float> {
    val primary = rideAverageSamples(samples).mapNotNull { sample ->
        val value = when {
            sample.avgEfficiencyWhKm > 0.01f -> sample.avgEfficiencyWhKm
            else -> sample.efficiencyWhKm
        }
        value.takeIf { it.isFinite() && it in 4f..140f }
    }
    if (primary.isNotEmpty()) return primary
    return samples.mapNotNull { sample ->
        val value = when {
            sample.avgEfficiencyWhKm > 0.01f -> sample.avgEfficiencyWhKm
            else -> sample.efficiencyWhKm
        }
        value.takeIf { it.isFinite() && it in 4f..140f }
    }
}

private fun robustRideMetricAverage(
    samples: List<RideMetricSample>,
    metric: RideChartMetric,
    values: List<Float>
): Float {
    if (values.isEmpty()) return 0f
    val activePairs = samples.zip(values).filter { (sample, _) -> isMeaningfulAverageSample(sample) }
    val candidateValues = when (metric) {
        RideChartMetric.EFFICIENCY -> reliableEfficiencyValues(samples)
        RideChartMetric.REGEN_POWER -> {
            val filtered = activePairs.map { it.second }.filter { it.isFinite() && it >= 5f }
            filtered.ifEmpty { values.filter { it.isFinite() && it >= 0f } }
        }
        else -> {
            val filtered = activePairs.map { it.second }
            filtered.ifEmpty { values.filter { it.isFinite() } }
        }
    }
    return robustAverage(candidateValues)
}

private data class RideMetricReferenceLine(
    val label: String,
    val startValue: Float,
    val endValue: Float
)

private fun rideMetricReferenceLine(
    metric: RideChartMetric,
    values: List<Float>,
    summaryLabel: String,
    summaryValue: Float
): RideMetricReferenceLine {
    if (metric == RideChartMetric.VOLTAGE && values.size >= 2) {
        val (startValue, endValue) = linearTrendEndpoints(values)
        val deltaValue = endValue - startValue
        return RideMetricReferenceLine(
            label = "趋势 ${formatSignedFloat(deltaValue)} ${metric.unit}",
            startValue = startValue,
            endValue = endValue
        )
    }
    return RideMetricReferenceLine(
        label = "$summaryLabel ${formatFloat(summaryValue)} ${metric.unit}",
        startValue = summaryValue,
        endValue = summaryValue
    )
}

private fun linearTrendEndpoints(values: List<Float>): Pair<Float, Float> {
    if (values.size < 2) {
        val fallback = values.firstOrNull() ?: 0f
        return fallback to fallback
    }
    val n = values.size.toFloat()
    val meanX = (values.lastIndex.toFloat()) / 2f
    val meanY = values.average().toFloat()
    var numerator = 0f
    var denominator = 0f
    values.forEachIndexed { index, value ->
        val dx = index - meanX
        numerator += dx * (value - meanY)
        denominator += dx * dx
    }
    val slope = if (denominator > 0.0001f) numerator / denominator else 0f
    val intercept = meanY - (slope * meanX)
    val startValue = intercept
    val endValue = intercept + (slope * values.lastIndex)
    return startValue to endValue
}

private fun formatMermaidNumber(value: Float): String =
    String.format(Locale.US, "%.2f", value)

private fun normalizeRideDetailSamples(record: RideHistoryRecord): List<RideMetricSample> {
    val rawSamples = record.samples
    if (rawSamples.isEmpty()) return rawSamples

    val hasCumulativeEnergy = rawSamples.any { it.totalEnergyWh > 0.01f }
    val hasAverageEfficiency = rawSamples.any { it.avgEfficiencyWhKm > 0.01f }
    val hasRecoveredEnergy = rawSamples.any { it.recoveredEnergyWh > 0.01f }
    val hasMaxControllerTemp = rawSamples.any { it.maxControllerTemp > 0.01f }
    val finalDistanceMeters = rawSamples.lastOrNull()?.distanceMeters?.takeIf { it > 1f }
        ?: record.distanceMeters.takeIf { it > 1f }
        ?: 0f
    val finalElapsedMs = rawSamples.lastOrNull()?.elapsedMs?.takeIf { it > 0L } ?: 0L
    var runningMaxControllerTemp = 0f
    var energyOffsetWh = 0f
    var recoveredOffsetWh = 0f
    var previousRawEnergyWh = 0f
    var previousRawRecoveredWh = 0f
    var runningTotalEnergyWh = 0f
    var runningTotalRecoveredWh = 0f

    return rawSamples.map { sample ->
        val progress = when {
            finalDistanceMeters > 1f && sample.distanceMeters > 0f ->
                (sample.distanceMeters / finalDistanceMeters).coerceIn(0f, 1f)
            finalElapsedMs > 0L ->
                (sample.elapsedMs.toFloat() / finalElapsedMs.toFloat()).coerceIn(0f, 1f)
            else -> 0f
        }
        val totalEnergyWh = when {
            hasCumulativeEnergy -> sample.totalEnergyWh
            record.totalEnergyWh > 0.01f -> record.totalEnergyWh * progress
            else -> sample.totalEnergyWh
        }
        if (hasCumulativeEnergy && totalEnergyWh + 0.05f < previousRawEnergyWh) {
            energyOffsetWh += previousRawEnergyWh
        }
        previousRawEnergyWh = totalEnergyWh
        val mergedTotalEnergyWh = max(
            runningTotalEnergyWh,
            (energyOffsetWh + totalEnergyWh).coerceAtLeast(0f)
        )
        runningTotalEnergyWh = mergedTotalEnergyWh
        // avgEfficiencyWhKm must stay consistent with normalized energy/distance.
        // A ride is a whole data entity — per-sample avg_eff should always equal
        // cumulative_energy / cumulative_distance to avoid mismatches in CSV export.
        val avgEfficiencyWhKm = when {
            sample.distanceMeters > 10f && mergedTotalEnergyWh > 0.01f ->
                mergedTotalEnergyWh / (sample.distanceMeters / 1000f)
            record.avgEfficiencyWhKm > 0.1f -> record.avgEfficiencyWhKm
            else -> sample.avgEfficiencyWhKm
        }
        val recoveredEnergyWh = when {
            hasRecoveredEnergy -> sample.recoveredEnergyWh
            else -> 0f
        }
        if (hasRecoveredEnergy && recoveredEnergyWh + 0.05f < previousRawRecoveredWh) {
            recoveredOffsetWh += previousRawRecoveredWh
        }
        previousRawRecoveredWh = recoveredEnergyWh
        val mergedRecoveredEnergyWh = max(
            runningTotalRecoveredWh,
            (recoveredOffsetWh + recoveredEnergyWh).coerceAtLeast(0f)
        )
        runningTotalRecoveredWh = mergedRecoveredEnergyWh
        runningMaxControllerTemp = max(
            runningMaxControllerTemp,
            max(sample.controllerTemp, sample.maxControllerTemp)
        )
        val maxControllerTemp = when {
            hasMaxControllerTemp && sample.maxControllerTemp > 0.01f -> sample.maxControllerTemp
            else -> runningMaxControllerTemp
        }
        sample.copy(
            totalEnergyWh = mergedTotalEnergyWh,
            avgEfficiencyWhKm = avgEfficiencyWhKm,
            recoveredEnergyWh = mergedRecoveredEnergyWh,
            maxControllerTemp = maxControllerTemp
        )
    }
}

private data class RideOverviewCard(
    val type: MetricType,
    val title: String,
    val value: DisplayMetric,
    val supporting: String,
    val metric: RideChartMetric
)

@Composable
private fun RideOverviewMetricCard(
    card: RideOverviewCard,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.68f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        },
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "RideCardColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "RideCardBorder"
    )
    val tonalElevation by animateDpAsState(
        targetValue = if (selected) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "RideCardTonalElevation"
    )
    Surface(
        modifier = modifier.heightIn(min = 104.dp),
        shape = bezierRoundedShape(22.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = tonalElevation,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .padding(end = 0.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
                Text(
                    card.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                BaselineMetricValue(
                    value = card.value.value,
                    unit = card.value.unit,
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    unitColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    valueFontSize = 24.sp,
                    unitFontSize = 11.sp,
                    singleLine = true
                )
            Text(
                card.supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
        }
    }
}

private fun buildRideOverviewCards(
    record: RideHistoryRecord,
    samples: List<RideMetricSample>,
    orderedTypes: List<MetricType>
): List<RideOverviewCard> {
    if (samples.isEmpty()) return emptyList()
    val first = samples.first()
    val last = samples.last()
    val averageSamples = rideAverageSamples(samples)
    val startSoc = samples.firstOrNull { it.soc > 1f }?.soc ?: first.soc.takeIf { it > 0.01f } ?: last.soc
    val startRangeKm = samples.firstOrNull { it.estimatedRangeKm > 0.1f }?.estimatedRangeKm
        ?: first.estimatedRangeKm.takeIf { it > 0.01f }
        ?: last.estimatedRangeKm
    val finalDistanceKm = ((last.distanceMeters.takeIf { it > 1f } ?: record.distanceMeters) / 1000f).coerceAtLeast(0f)
    val avgPowerKw = robustAverage(averageSamples.map { it.powerKw })
    val peakPowerKw = samples.maxOf { it.powerKw }
    val peakRegenPowerKw = samples.maxOf { (-it.powerKw).coerceAtLeast(0f) }
    val avgVoltage = robustAverage(averageSamples.map { it.voltage })
    val minVoltage = samples.minOf { it.voltage }
    val avgVoltageSag = robustAverage(averageSamples.map { it.voltageSag.coerceAtLeast(0f) })
    val peakVoltageSag = samples.maxOf { it.voltageSag.coerceAtLeast(0f) }
    val avgBusCurrent = robustAverage(averageSamples.map { kotlin.math.abs(it.busCurrent) })
    val peakBusCurrent = samples.maxOf { kotlin.math.abs(it.busCurrent) }
    val avgPhaseCurrent = robustAverage(averageSamples.map { kotlin.math.abs(it.phaseCurrent) })
    val peakPhaseCurrent = samples.maxOf { kotlin.math.abs(it.phaseCurrent) }
    val avgControllerTemp = robustAverage(averageSamples.map { it.controllerTemp })
    val peakControllerTemp = samples.maxOf { it.controllerTemp }
    val avgSpeedKmh = record.avgSpeedKmh.takeIf { it > 0.01f }
        ?: robustAverage(averageSamples.map { it.speedKmH })
    val peakSpeedKmh = max(record.maxSpeedKmh, samples.maxOf { it.speedKmH })
    val avgRpm = robustAverage(averageSamples.map { it.rpm })
    val peakRpm = samples.maxOf { it.rpm }
    val avgInstantEfficiency = robustAverage(reliableEfficiencyValues(samples))
    val peakAverageEfficiency = reliableEfficiencyValues(samples).maxOrNull()
        ?: record.avgEfficiencyWhKm
    val recoveredEnergyWh = samples.maxOf { it.recoveredEnergyWh }.coerceAtLeast(last.recoveredEnergyWh)
    val cardsByType = linkedMapOf<MetricType, RideOverviewCard>(
        MetricType.SPEED to RideOverviewCard(
            type = MetricType.SPEED,
            title = "速度",
            value = metricOf(peakSpeedKmh, "km/h"),
            supporting = "平均 ${formatFloat(avgSpeedKmh)} km/h",
            metric = RideChartMetric.SPEED
        ),
        MetricType.POWER to RideOverviewCard(
            type = MetricType.POWER,
            title = "功率",
            value = metricOf(avgPowerKw, "kW"),
            supporting = "峰值 ${formatFloat(peakPowerKw)} kW",
            metric = RideChartMetric.POWER
        ),
        MetricType.VOLTAGE to RideOverviewCard(
            type = MetricType.VOLTAGE,
            title = "电压",
            value = metricOf(avgVoltage, "V"),
            supporting = "最低 ${formatFloat(minVoltage)} V",
            metric = RideChartMetric.VOLTAGE
        ),
        MetricType.VOLTAGE_SAG to RideOverviewCard(
            type = MetricType.VOLTAGE_SAG,
            title = "压降",
            value = metricOf(avgVoltageSag, "V"),
            supporting = "最大 ${formatFloat(peakVoltageSag)} V",
            metric = RideChartMetric.VOLTAGE_SAG
        ),
        MetricType.BUS_CURRENT to RideOverviewCard(
            type = MetricType.BUS_CURRENT,
            title = "母线电流",
            value = metricOf(avgBusCurrent, "A"),
            supporting = "峰值 ${formatFloat(peakBusCurrent)} A",
            metric = RideChartMetric.BUS_CURRENT
        ),
        MetricType.PHASE_CURRENT to RideOverviewCard(
            type = MetricType.PHASE_CURRENT,
            title = "相电流",
            value = metricOf(avgPhaseCurrent, "A"),
            supporting = "峰值 ${formatFloat(peakPhaseCurrent)} A",
            metric = RideChartMetric.PHASE_CURRENT
        ),
        MetricType.TEMP to RideOverviewCard(
            type = MetricType.TEMP,
            title = "控制器温度",
            value = metricOf(avgControllerTemp, "°C"),
            supporting = "最高 ${formatFloat(peakControllerTemp)} °C",
            metric = RideChartMetric.CONTROLLER_TEMP
        ),
        MetricType.MAX_CONTROLLER_TEMP to RideOverviewCard(
            type = MetricType.MAX_CONTROLLER_TEMP,
            title = "控制器最高温度",
            value = metricOf(peakControllerTemp, "°C"),
            supporting = "平均 ${formatFloat(avgControllerTemp)} °C",
            metric = RideChartMetric.MAX_CONTROLLER_TEMP
        ),
        MetricType.SOC to RideOverviewCard(
            type = MetricType.SOC,
            title = "电量",
            value = metricOf(last.soc, "%"),
            supporting = "起点 ${formatFloat(startSoc)} %",
            metric = RideChartMetric.SOC
        ),
        MetricType.RANGE to RideOverviewCard(
            type = MetricType.RANGE,
            title = "剩余续航",
            value = metricOf(last.estimatedRangeKm, "km"),
            supporting = "起点 ${formatFloat(startRangeKm)} km",
            metric = RideChartMetric.RANGE
        ),
        MetricType.RPM to RideOverviewCard(
            type = MetricType.RPM,
            title = "转速",
            value = metricOf(avgRpm, "rpm"),
            supporting = "峰值 ${formatFloat(peakRpm)} rpm",
            metric = RideChartMetric.RPM
        ),
        MetricType.EFFICIENCY to RideOverviewCard(
            type = MetricType.EFFICIENCY,
            title = "平均能耗",
            value = metricOf(record.avgEfficiencyWhKm.takeIf { it > 0.01f } ?: avgInstantEfficiency, "Wh/km"),
            supporting = "峰值 ${formatFloat(peakAverageEfficiency)} Wh/km",
            metric = RideChartMetric.AVG_EFFICIENCY
        ),
        MetricType.TRIP_DISTANCE to RideOverviewCard(
            type = MetricType.TRIP_DISTANCE,
            title = "里程",
            value = metricOf(finalDistanceKm, "km"),
            supporting = "平均 ${formatFloat(record.avgSpeedKmh)} km/h",
            metric = RideChartMetric.DISTANCE
        ),
        MetricType.TOTAL_ENERGY to RideOverviewCard(
            type = MetricType.TOTAL_ENERGY,
            title = "总能耗",
            value = metricOf(record.totalEnergyWh, "Wh"),
            supporting = "回收 ${formatFloat(recoveredEnergyWh)} Wh",
            metric = RideChartMetric.TOTAL_ENERGY
        ),
        MetricType.PEAK_REGEN_POWER to RideOverviewCard(
            type = MetricType.PEAK_REGEN_POWER,
            title = "最大回收功率",
            value = metricOf(peakRegenPowerKw * 1000f, "W"),
            supporting = "总回收 ${formatFloat(recoveredEnergyWh)} Wh",
            metric = RideChartMetric.REGEN_POWER
        ),
        MetricType.RECOVERED_ENERGY to RideOverviewCard(
            type = MetricType.RECOVERED_ENERGY,
            title = "总回收能量",
            value = metricOf(recoveredEnergyWh, "Wh"),
            supporting = "回收峰值 ${formatFloat(peakRegenPowerKw * 1000f)} W",
            metric = RideChartMetric.RECOVERED_ENERGY
        )
    )
    val ordered = orderedTypes
        .distinct()
        .mapNotNull { type -> cardsByType[type] }
    return if (ordered.isNotEmpty()) ordered else MetricType.entries.mapNotNull { cardsByType[it] }
}


@Composable
private fun RoutePreview(
    record: RideHistoryRecord,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val routePoints = remember(record.id, record.samples, record.trackPoints) {
        buildRoutePreviewPoints(record)
    }
    val clickableModifier = onClick?.let {
        Modifier
            .clickable(onClick = it)
            .then(modifier)
    } ?: modifier
    Surface(
        modifier = clickableModifier,
        shape = bezierRoundedShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        if (routePoints.size < 2) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("暂无足够轨迹数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val projection = projectRoute(routePoints, size.width, size.height)
                if (projection == null) return@Canvas
                for (index in 0 until projection.points.lastIndex) {
                    val start = projection.points[index]
                    val end = projection.points[index + 1]
                    val segmentSpeed = ((start.speedKmh + end.speedKmh) * 0.5f)
                        .coerceAtLeast(0f)
                    drawLine(
                        color = routeSpeedColor(
                            speedKmh = segmentSpeed,
                            minSpeedKmh = projection.minSpeedKmh,
                            maxSpeedKmh = projection.maxSpeedKmh
                        ),
                        start = start.offset,
                        end = end.offset,
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }
                drawCircle(color = Color(0xFF7DE38D), radius = 12f, center = projection.start.offset)
                drawCircle(color = Color(0xFFFF9E9E), radius = 12f, center = projection.end.offset)
            }
            // Hint overlay on hover/touch
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    shape = bezierPillShape(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ) {
                    Text(
                        "全屏查看",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, metric: DisplayMetric, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = bezierRoundedShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f)
    ) {
        BoxWithConstraints {
            val style = metricCompactStyle(maxWidth = maxWidth, metric = metric, statChip = true)
            Column(modifier = Modifier.padding(horizontal = style.horizontalPadding, vertical = style.verticalPadding)) {
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                BaselineMetricValue(
                    value = metric.value,
                    unit = metric.unit,
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    unitColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    valueFontSize = style.valueFontSize,
                    unitFontSize = style.unitFontSize,
                    unitSpacing = style.unitSpacing,
                    singleLine = true
                )
            }
        }
    }
}

@Composable
private fun StatMetricGrid(
    metrics: List<Pair<String, DisplayMetric>>,
    columns: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        metrics.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (label, metric) ->
                    StatChip(
                        label = label,
                        metric = metric,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    metric: DisplayMetric,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = bezierRoundedShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    ) {
        BoxWithConstraints {
            val style = metricCompactStyle(maxWidth = maxWidth, metric = metric, statChip = false)
            Column(modifier = Modifier.padding(horizontal = style.horizontalPadding, vertical = style.verticalPadding + 2.dp)) {
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(10.dp))
                BaselineMetricValue(
                    value = metric.value,
                    unit = metric.unit,
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    unitColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    valueFontSize = style.valueFontSize,
                    unitFontSize = style.unitFontSize,
                    unitSpacing = style.unitSpacing,
                    singleLine = true
                )
            }
        }
    }
}

private data class MetricCompactStyle(
    val valueFontSize: androidx.compose.ui.unit.TextUnit,
    val unitFontSize: androidx.compose.ui.unit.TextUnit,
    val unitSpacing: androidx.compose.ui.unit.Dp,
    val horizontalPadding: androidx.compose.ui.unit.Dp,
    val verticalPadding: androidx.compose.ui.unit.Dp
)

private fun metricCompactStyle(
    maxWidth: androidx.compose.ui.unit.Dp,
    metric: DisplayMetric,
    statChip: Boolean
): MetricCompactStyle {
    val longUnit = metric.unit.length >= 4
    val longValue = metric.value.length >= 5
    val veryTight = if (statChip) maxWidth < 108.dp else maxWidth < 132.dp
    val compact = if (statChip) maxWidth < 118.dp else maxWidth < 150.dp
    return when {
        veryTight || (longUnit && longValue) -> MetricCompactStyle(
            valueFontSize = if (statChip) 17.sp else 20.sp,
            unitFontSize = if (statChip) 8.sp else 9.sp,
            unitSpacing = 1.dp,
            horizontalPadding = if (statChip) 8.dp else 12.dp,
            verticalPadding = if (statChip) 9.dp else 12.dp
        )
        compact || longUnit || longValue -> MetricCompactStyle(
            valueFontSize = if (statChip) 19.sp else 22.sp,
            unitFontSize = if (statChip) 9.sp else 10.sp,
            unitSpacing = 2.dp,
            horizontalPadding = if (statChip) 10.dp else 14.dp,
            verticalPadding = if (statChip) 10.dp else 14.dp
        )
        else -> MetricCompactStyle(
            valueFontSize = if (statChip) 22.sp else 24.sp,
            unitFontSize = if (statChip) 11.sp else 12.sp,
            unitSpacing = 4.dp,
            horizontalPadding = if (statChip) 12.dp else 16.dp,
            verticalPadding = if (statChip) 10.dp else 16.dp
        )
    }
}

private data class RouteProjection(
    val rect: androidx.compose.ui.geometry.Rect,
    val points: List<ProjectedRoutePoint>,
    val start: ProjectedRoutePoint,
    val end: ProjectedRoutePoint,
    val minSpeedKmh: Float,
    val maxSpeedKmh: Float
)

private data class RoutePreviewPoint(
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Float
)

private data class ProjectedRoutePoint(
    val offset: Offset,
    val speedKmh: Float
)

private fun projectRoute(
    points: List<RoutePreviewPoint>,
    width: Float,
    height: Float
): RouteProjection? {
    if (points.size < 2 || width <= 0f || height <= 0f) return null
    val minLat = points.minOf { it.latitude }
    val maxLat = points.maxOf { it.latitude }
    val minLon = points.minOf { it.longitude }
    val maxLon = points.maxOf { it.longitude }
    val latSpan = (maxLat - minLat).takeIf { it > 0.000001 } ?: 0.000001
    val lonSpan = (maxLon - minLon).takeIf { it > 0.000001 } ?: 0.000001
    val plotSize = minOf(width, height)
    val left = (width - plotSize) / 2f
    val top = (height - plotSize) / 2f
    val inset = plotSize * 0.08f
    val plotRect = androidx.compose.ui.geometry.Rect(
        left + inset,
        top + inset,
        left + plotSize - inset,
        top + plotSize - inset
    )
    val scale = minOf(plotRect.width / lonSpan.toFloat(), plotRect.height / latSpan.toFloat())
    val xOffset = plotRect.left + (plotRect.width - lonSpan.toFloat() * scale) / 2f
    val yOffset = plotRect.top + (plotRect.height - latSpan.toFloat() * scale) / 2f
    val projected = points.map { point ->
        ProjectedRoutePoint(
            offset = Offset(
                x = xOffset + ((point.longitude - minLon).toFloat() * scale),
                y = yOffset + (latSpan.toFloat() * scale) - ((point.latitude - minLat).toFloat() * scale)
            ),
            speedKmh = point.speedKmh
        )
    }
    val minSpeed = points.minOf { it.speedKmh }
    val maxSpeed = points.maxOf { it.speedKmh }
    return RouteProjection(
        rect = plotRect,
        points = projected,
        start = projected.first(),
        end = projected.last(),
        minSpeedKmh = minSpeed,
        maxSpeedKmh = maxSpeed
    )
}

private fun buildRoutePreviewPoints(record: RideHistoryRecord): List<RoutePreviewPoint> {
    val sampled = buildRoutePreviewPointsFromSamples(record.samples)
    if (sampled.size >= 2) return sampled
    return record.trackPoints.map { point ->
        RoutePreviewPoint(
            latitude = point.latitude,
            longitude = point.longitude,
            speedKmh = 0f
        )
    }
}

private fun buildRoutePreviewPointsFromSamples(samples: List<RideMetricSample>): List<RoutePreviewPoint> {
    val points = mutableListOf<RoutePreviewPoint>()
    var previousValid: RideMetricSample? = null
    var missingBetween = 0
    samples.forEach { sample ->
        val latitude = sample.latitude
        val longitude = sample.longitude
        if (latitude == null || longitude == null) {
            if (previousValid != null) missingBetween += 1
            return@forEach
        }
        if (previousValid == null) {
            appendRoutePreviewPoint(
                points,
                RoutePreviewPoint(latitude, longitude, sample.speedKmH.coerceAtLeast(0f))
            )
            previousValid = sample
            missingBetween = 0
            return@forEach
        }
        appendInterpolatedRouteSegment(
            target = points,
            previous = previousValid!!,
            next = sample,
            missingBetween = missingBetween
        )
        previousValid = sample
        missingBetween = 0
    }
    return points
}

private fun appendInterpolatedRouteSegment(
    target: MutableList<RoutePreviewPoint>,
    previous: RideMetricSample,
    next: RideMetricSample,
    missingBetween: Int
) {
    val previousLat = previous.latitude ?: return
    val previousLon = previous.longitude ?: return
    val nextLat = next.latitude ?: return
    val nextLon = next.longitude ?: return
    val directDistanceMeters = haversineMeters(previousLat, previousLon, nextLat, nextLon)
    val sampledDistanceMeters = (next.distanceMeters - previous.distanceMeters).coerceAtLeast(0f)
    val insertedPointCount = when {
        missingBetween > 0 -> maxOf(
            missingBetween,
            (sampledDistanceMeters / 24f).roundToInt()
        )
        directDistanceMeters > 30.0 -> (directDistanceMeters / 24.0).roundToInt()
        else -> 0
    }.coerceIn(0, 24)
    repeat(insertedPointCount) { index ->
        val fraction = (index + 1f) / (insertedPointCount + 1f)
        appendRoutePreviewPoint(
            target,
            RoutePreviewPoint(
                latitude = lerpDouble(previousLat, nextLat, fraction),
                longitude = lerpDouble(previousLon, nextLon, fraction),
                speedKmh = lerpFloat(previous.speedKmH, next.speedKmH, fraction).coerceAtLeast(0f)
            )
        )
    }
    appendRoutePreviewPoint(
        target,
        RoutePreviewPoint(
            latitude = nextLat,
            longitude = nextLon,
            speedKmh = next.speedKmH.coerceAtLeast(0f)
        )
    )
}

private fun appendRoutePreviewPoint(
    target: MutableList<RoutePreviewPoint>,
    point: RoutePreviewPoint
) {
    val previous = target.lastOrNull()
    if (previous != null && haversineMeters(previous.latitude, previous.longitude, point.latitude, point.longitude) < 0.6) {
        target[target.lastIndex] = point
        return
    }
    target += point
}

private fun routeSpeedColor(
    speedKmh: Float,
    minSpeedKmh: Float,
    maxSpeedKmh: Float
): Color {
    val normalized = if (maxSpeedKmh - minSpeedKmh < 1f) {
        0.5f
    } else {
        ((speedKmh - minSpeedKmh) / (maxSpeedKmh - minSpeedKmh)).coerceIn(0f, 1f)
    }
    val hue = 120f * (1f - normalized)
    return Color.hsv(hue = hue, saturation = 0.82f, value = 0.92f)
}

private fun haversineMeters(
    startLat: Double,
    startLon: Double,
    endLat: Double,
    endLon: Double
): Double {
    val earthRadiusMeters = 6_371_000.0
    val dLat = Math.toRadians(endLat - startLat)
    val dLon = Math.toRadians(endLon - startLon)
    val startLatRad = Math.toRadians(startLat)
    val endLatRad = Math.toRadians(endLat)
    val a = kotlin.math.sin(dLat / 2.0) * kotlin.math.sin(dLat / 2.0) +
        kotlin.math.cos(startLatRad) * kotlin.math.cos(endLatRad) *
        kotlin.math.sin(dLon / 2.0) * kotlin.math.sin(dLon / 2.0)
    val c = 2.0 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1.0 - a))
    return earthRadiusMeters * c
}

private fun lerpDouble(start: Double, end: Double, fraction: Float): Double {
    return start + ((end - start) * fraction.toDouble())
}

private fun lerpFloat(start: Float, end: Float, fraction: Float): Float {
    return start + ((end - start) * fraction)
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(6.dp))
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = bezierRoundedShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun RideMetricSample.metricValue(metric: RideChartMetric): Float {
    return when (metric) {
        RideChartMetric.SPEED -> speedKmH
        RideChartMetric.POWER -> powerKw
        RideChartMetric.VOLTAGE -> voltage
        RideChartMetric.VOLTAGE_SAG -> voltageSag
        RideChartMetric.BUS_CURRENT -> busCurrent
        RideChartMetric.PHASE_CURRENT -> phaseCurrent
        RideChartMetric.CONTROLLER_TEMP -> controllerTemp
        RideChartMetric.EFFICIENCY -> efficiencyWhKm
        RideChartMetric.AVG_EFFICIENCY -> avgEfficiencyWhKm
        RideChartMetric.RANGE -> estimatedRangeKm
        RideChartMetric.TOTAL_ENERGY -> totalEnergyWh
        RideChartMetric.RECOVERED_ENERGY -> recoveredEnergyWh
        RideChartMetric.MAX_CONTROLLER_TEMP -> maxControllerTemp
        RideChartMetric.SOC -> soc
        RideChartMetric.RPM -> rpm
        RideChartMetric.DISTANCE -> distanceMeters / 1000f
        RideChartMetric.REGEN_POWER -> (-powerKw).coerceAtLeast(0f) * 1000f
    }
}

private fun metricOf(value: Float, unit: String): DisplayMetric =
    DisplayMetric(value = formatFloat(value), unit = unit)

private fun metricOf(value: String, unit: String = ""): DisplayMetric =
    DisplayMetric(value = value.trim(), unit = unit.trim())

private fun formatDate(timestampMs: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestampMs)).withDisplaySpacing()

private fun formatRideTitle(title: String): String = title.withDisplaySpacing()

private fun formatDuration(durationMs: Long): String {
    val minutes = durationMs / 60000
    val seconds = (durationMs % 60000) / 1000
    return (if (minutes > 0) "${minutes}分${seconds}秒" else "${seconds}秒").withDisplaySpacing()
}

private fun formatSeconds(durationMs: Long): String =
    String.format(Locale.getDefault(), "%.2f s", durationMs / 1000f)

private fun formatFloat(value: Float): String =
    String.format(Locale.getDefault(), "%.1f", value)

private fun formatSignedFloat(value: Float): String =
    String.format(Locale.getDefault(), "%+.1f", value)

private data class VoltageSagSummary(
    val average: Float,
    val maximum: Float
)

private fun summarizeVoltageSag(samples: List<RideMetricSample>): VoltageSagSummary {
    if (samples.isEmpty()) return VoltageSagSummary(0f, 0f)
    val hasRecordedSag = samples.any { it.voltageSag > 0.01f }
    val referenceVoltage = samples.maxOfOrNull { it.voltage } ?: 0f
    val sagValues = samples.map { sample ->
        if (hasRecordedSag) {
            sample.voltageSag.coerceAtLeast(0f)
        } else {
            (referenceVoltage - sample.voltage).coerceAtLeast(0f)
        }
    }
    val average = sagValues.average().toFloat()
    val maximum = sagValues.maxOrNull() ?: 0f
    return VoltageSagSummary(average = average, maximum = maximum)
}
