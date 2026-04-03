@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.shawnrain.habe.ui.speedtest

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.lerp
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.data.history.RideHistoryRecord
import com.shawnrain.habe.data.history.RideMetricSample
import com.shawnrain.habe.data.history.RideTrackPoint
import com.shawnrain.habe.data.speedtest.SpeedTestRecord
import com.shawnrain.habe.ui.dashboard.BaselineMetricValue
import com.shawnrain.habe.ui.theme.bezierPillShape
import com.shawnrain.habe.ui.theme.bezierRoundedShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    BUS_CURRENT("母线电流", "A", Color(0xFFFFB682)),
    PHASE_CURRENT("相电流", "A", Color(0xFFFF8FA3)),
    CONTROLLER_TEMP("控制器温度", "°C", Color(0xFFFFA86B)),
    EFFICIENCY("能耗", "Wh/km", Color(0xFF8CE0FF)),
    SOC("电量", "%", Color(0xFF98E57A)),
    RPM("转速", "rpm", Color(0xFFC2C7D0))
}

@Composable
fun SpeedtestScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val metrics by viewModel.metrics.collectAsState()
    val speedTestSession by viewModel.speedTestSession.collectAsState()
    val speedTestHistory by viewModel.speedTestHistory.collectAsState()
    val rideHistory by viewModel.rideHistory.collectAsState()
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
    var selectedRideRecord by remember { mutableStateOf<RideHistoryRecord?>(null) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { SpeedPageTab.entries.size }
    )

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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                SpeedTestHero(
                                    option = selectedOption,
                                    speedTestSession = speedTestSession,
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
                                            context.startActivity(
                                                viewModel.createSpeedTestShareIntent(record).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            )
                                        },
                                        onDelete = { viewModel.deleteSpeedTestRecord(record.id) }
                                    )
                                }
                            }
                        }
                    }

                    SpeedPageTab.RIDE_HISTORY -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                SectionHeader(
                                    "自动行程记录",
                                    "骑行中会自动记录路线、速度、电压、电流、温度、功率和能耗走势"
                                )
                            }
                            if (rideHistory.isEmpty()) {
                                item { EmptyCard("还没有行程记录", "开始骑行后系统会自动创建记录，停车一段时间后自动归档") }
                            } else {
                                items(rideHistory, key = { it.id }) { record ->
                                    RideHistoryCard(
                                        record = record,
                                        onOpen = { selectedRideRecord = record },
                                        onShare = {
                                            context.startActivity(
                                                viewModel.createRideShareIntent(record).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            )
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }

    selectedRideRecord?.let { record ->
        RideHistoryDetailSheet(
            record = record,
            onDismiss = { selectedRideRecord = null },
            onShare = {
                context.startActivity(
                    viewModel.createRideShareIntent(record).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        )
    }
}

@Composable
private fun Header(currentSpeed: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Text("性能与行程", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "当前车速 ${formatFloat(currentSpeed)} km/h",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SpeedTestHero(
    option: SpeedTestOption,
    speedTestSession: com.shawnrain.habe.data.speedtest.SpeedTestSessionUiState,
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
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatChip("目标", metricOf(record.targetSpeedKmh.toInt().toString(), "km/h"))
                    StatChip("最高速度", metricOf(record.maxSpeedKmh, "km/h"))
                    StatChip("峰值功率", metricOf(record.peakPowerKw, "kW"))
                    StatChip("峰值母线电流", metricOf(record.peakBusCurrentA, "A"))
                    StatChip("最低电压", metricOf(record.minVoltage, "V"))
                    StatChip("轨迹点", metricOf(record.trackPoints.size.toString()))
                }
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
        AlertDialog(
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
    onOpen: () -> Unit,
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
            shape = bezierRoundedShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(record.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "${formatDate(record.startedAtMs)} · ${formatDuration(record.durationMs)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AssistChip(
                        onClick = onOpen,
                        label = { Text("查看曲线") },
                        leadingIcon = { Icon(Icons.Default.AutoGraph, contentDescription = null) }
                    )
                }
                StatMetricGrid(
                    metrics = listOf(
                        "里程" to metricOf(record.distanceMeters / 1000f, "km"),
                        "最高速度" to metricOf(record.maxSpeedKmh, "km/h"),
                        "平均速度" to metricOf(record.avgSpeedKmh, "km/h"),
                        "峰值功率" to metricOf(record.peakPowerKw, "kW"),
                        "能耗" to metricOf(record.avgEfficiencyWhKm, "Wh/km"),
                        "采样点" to metricOf(record.samples.size.toString())
                    ),
                    columns = 2
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AssistChip(
                        onClick = onShare,
                        label = { Text("分享图", maxLines = 1, softWrap = false) },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        modifier = Modifier.weight(1f)
                    )
                    AssistChip(
                        onClick = onOpen,
                        label = { Text("轨迹图表", maxLines = 1, softWrap = false) },
                        leadingIcon = { Icon(Icons.Default.Map, contentDescription = null) },
                        modifier = Modifier.weight(1f)
                    )
                    AssistChip(
                        onClick = { showDeleteConfirm = true },
                        label = { Text("删除", maxLines = 1, softWrap = false) },
                        leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RideHistoryDetailSheet(
    record: RideHistoryRecord,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    var selectedMetric by remember(record.id) { mutableStateOf(RideChartMetric.SPEED) }
    val samples = record.samples
    var selectedIndex by remember(record.id) { mutableIntStateOf((samples.lastIndex).coerceAtLeast(0)) }
    val selectedSample = samples.getOrNull(selectedIndex)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(record.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${formatDate(record.startedAtMs)} · ${formatDuration(record.durationMs)} · ${formatFloat(record.distanceMeters / 1000f)} km",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    RideChartMetric.entries.forEach { metric ->
                        FilterChip(
                            selected = metric == selectedMetric,
                            onClick = { selectedMetric = metric },
                            label = { Text(metric.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = metric.color.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
            item {
                RideMetricChart(
                    samples = samples,
                    metric = selectedMetric,
                    selectedIndex = selectedIndex,
                    onSelectIndex = { selectedIndex = it }
                )
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = bezierRoundedShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("时间点明细", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (selectedSample == null) {
                            Text("暂无可查看的采样数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(
                                "时间 ${formatDuration(selectedSample.elapsedMs)}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider()
                            val detailMetrics = remember(selectedSample) {
                                listOf(
                                    "速度" to metricOf(selectedSample.speedKmH, "km/h"),
                                    "功率" to metricOf(selectedSample.powerKw, "kW"),
                                    "电压" to metricOf(selectedSample.voltage, "V"),
                                    "母线电流" to metricOf(selectedSample.busCurrent, "A"),
                                    "相电流" to metricOf(selectedSample.phaseCurrent, "A"),
                                    "控制器温度" to metricOf(selectedSample.controllerTemp, "°C"),
                                    "电量" to metricOf(selectedSample.soc, "%"),
                                    "转速" to metricOf(selectedSample.rpm, "rpm"),
                                    "能耗" to metricOf(selectedSample.efficiencyWhKm, "Wh/km"),
                                    "里程" to metricOf(selectedSample.distanceMeters, "m")
                                )
                            }
                            StatMetricGrid(metrics = detailMetrics, columns = 3)
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
                        Text("轨迹路线", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        RoutePreview(
                            points = record.trackPoints,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = bezierPillShape()
                    ) {
                        Text("导出分享图")
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = bezierPillShape()
                    ) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}

@Composable
private fun RideMetricChart(
    samples: List<RideMetricSample>,
    metric: RideChartMetric,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    val values = remember(samples, metric) { samples.map { it.metricValue(metric) } }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = bezierRoundedShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
    ) {
        val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        Column(modifier = Modifier.padding(18.dp)) {
            Text(metric.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "拖动图表可查看任意时间点数据走势",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .pointerInput(values) {
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
                    val paddingLeft = 26.dp.toPx()
                    val paddingRight = 18.dp.toPx()
                    val paddingTop = 20.dp.toPx()
                    val paddingBottom = 28.dp.toPx()
                    val chartWidth = size.width - paddingLeft - paddingRight
                    val chartHeight = size.height - paddingTop - paddingBottom
                    val maxValue = values.maxOrNull()?.takeIf { it > 0f } ?: 1f
                    val minValue = values.minOrNull() ?: 0f
                    val span = (maxValue - minValue).takeIf { it > 0.001f } ?: 1f

                    repeat(4) { index ->
                        val y = paddingTop + chartHeight * index / 3f
                        drawLine(
                            color = gridColor,
                            start = Offset(paddingLeft, y),
                            end = Offset(size.width - paddingRight, y),
                            strokeWidth = 2f
                        )
                    }

                    val path = Path()
                    values.forEachIndexed { index, value ->
                        val x = paddingLeft + chartWidth * index / (values.lastIndex.coerceAtLeast(1))
                        val y = paddingTop + chartHeight - ((value - minValue) / span) * chartHeight
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = metric.color,
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )

                    val safeIndex = selectedIndex.coerceIn(0, values.lastIndex)
                    val selectedX = paddingLeft + chartWidth * safeIndex / (values.lastIndex.coerceAtLeast(1))
                    val selectedY = paddingTop + chartHeight - ((values[safeIndex] - minValue) / span) * chartHeight
                    drawLine(
                        color = Color.White.copy(alpha = 0.55f),
                        start = Offset(selectedX, paddingTop),
                        end = Offset(selectedX, size.height - paddingBottom),
                        strokeWidth = 3f
                    )
                    drawCircle(color = metric.color, radius = 12f, center = Offset(selectedX, selectedY))
                    drawCircle(color = Color.White, radius = 5f, center = Offset(selectedX, selectedY))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            val selectedValue = values.getOrNull(selectedIndex)
            if (selectedValue != null) {
                Text(
                    "${metric.title} ${formatFloat(selectedValue)} ${metric.unit}",
                    color = metric.color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RoutePreview(points: List<RideTrackPoint>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = bezierRoundedShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        if (points.size < 2) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("暂无足够轨迹数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val minLat = points.minOf { it.latitude }
                val maxLat = points.maxOf { it.latitude }
                val minLon = points.minOf { it.longitude }
                val maxLon = points.maxOf { it.longitude }
                val latSpan = (maxLat - minLat).takeIf { it > 0.000001 } ?: 0.000001
                val lonSpan = (maxLon - minLon).takeIf { it > 0.000001 } ?: 0.000001
                val path = Path()
                points.forEachIndexed { index, point ->
                    val x = ((point.longitude - minLon) / lonSpan).toFloat() * size.width
                    val y = size.height - ((point.latitude - minLat) / latSpan).toFloat() * size.height
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = Color(0xFF9FC0FF), style = Stroke(width = 8f, cap = StrokeCap.Round))
                val start = points.first()
                val end = points.last()
                val startX = ((start.longitude - minLon) / lonSpan).toFloat() * size.width
                val startY = size.height - ((start.latitude - minLat) / latSpan).toFloat() * size.height
                val endX = ((end.longitude - minLon) / lonSpan).toFloat() * size.width
                val endY = size.height - ((end.latitude - minLat) / latSpan).toFloat() * size.height
                drawCircle(color = Color(0xFF7DE38D), radius = 12f, center = Offset(startX, startY))
                drawCircle(color = Color(0xFFFF9E9E), radius = 12f, center = Offset(endX, endY))
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
            val compactUnit = metric.unit.length >= 4
            val compactValue = metric.value.length >= 5
            val compact = maxWidth < 112.dp || compactUnit || compactValue
            Column(modifier = Modifier.padding(horizontal = if (compact) 10.dp else 12.dp, vertical = 10.dp)) {
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                BaselineMetricValue(
                    value = metric.value,
                    unit = metric.unit,
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    unitColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    valueFontSize = if (compact) 20.sp else 22.sp,
                    unitFontSize = if (compact) 10.sp else 11.sp,
                    unitSpacing = if (compact) 2.dp else 4.dp,
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
            val compactUnit = metric.unit.length >= 4
            val compactValue = metric.value.length >= 5
            val compact = maxWidth < 144.dp || compactUnit || compactValue
            Column(modifier = Modifier.padding(if (compact) 14.dp else 16.dp)) {
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(10.dp))
                BaselineMetricValue(
                    value = metric.value,
                    unit = metric.unit,
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    unitColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    valueFontSize = if (compact) 22.sp else 24.sp,
                    unitFontSize = if (compact) 11.sp else 12.sp,
                    unitSpacing = if (compact) 2.dp else 4.dp,
                    singleLine = true
                )
            }
        }
    }
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
        RideChartMetric.BUS_CURRENT -> busCurrent
        RideChartMetric.PHASE_CURRENT -> phaseCurrent
        RideChartMetric.CONTROLLER_TEMP -> controllerTemp
        RideChartMetric.EFFICIENCY -> efficiencyWhKm
        RideChartMetric.SOC -> soc
        RideChartMetric.RPM -> rpm
    }
}

private fun metricOf(value: Float, unit: String): DisplayMetric =
    DisplayMetric(value = formatFloat(value), unit = unit)

private fun metricOf(value: String, unit: String = ""): DisplayMetric =
    DisplayMetric(value = value.trim(), unit = unit.trim())

private fun formatDate(timestampMs: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestampMs))

private fun formatDuration(durationMs: Long): String {
    val minutes = durationMs / 60000
    val seconds = (durationMs % 60000) / 1000
    return if (minutes > 0) "${minutes}分${seconds}秒" else "${seconds}秒"
}

private fun formatSeconds(durationMs: Long): String =
    String.format(Locale.getDefault(), "%.2f s", durationMs / 1000f)

private fun formatFloat(value: Float): String =
    String.format(Locale.getDefault(), "%.1f", value)

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
