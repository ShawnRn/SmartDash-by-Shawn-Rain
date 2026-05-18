package com.shawnrain.sdash.ui.dashboard

import android.app.Activity
import android.content.res.Configuration
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.core.view.WindowCompat
import com.shawnrain.sdash.MainViewModel
import com.shawnrain.sdash.ble.AutoConnectState
import com.shawnrain.sdash.ble.ConnectionState
import com.shawnrain.sdash.ble.displayName
import com.shawnrain.sdash.ble.VehicleMetrics
import com.shawnrain.sdash.data.MetricType
import com.shawnrain.sdash.debug.AppLogger
import com.shawnrain.sdash.ui.connect.ConnectionQuickSheet
import com.shawnrain.sdash.ui.navigation.BlurredModalBottomSheet
import com.shawnrain.sdash.ui.navigation.PredictiveBackPopupTransform
import com.shawnrain.sdash.ui.theme.bezierPillShape
import com.shawnrain.sdash.ui.theme.bezierRoundedShape
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

private const val DRAG_LOG_TAG = "DashboardDrag"

// ── Dashboard Design Tokens ──
private val CardCornerRadius = 20.dp
private val CardHeight = 110.dp
private val CardInnerPadding = 16.dp
private val CardSpacing = 12.dp
private val PageHorizontalPadding = 20.dp
private val SpeedAreaHeight = 152.dp
private val SpeedAreaCornerRadius = 22.dp
private val SectionSpacing = 12.dp

private fun Offset.toLogString(): String = "(%.1f, %.1f)".format(x, y)

private fun Rect.toLogString(): String = "[l=%.1f t=%.1f r=%.1f b=%.1f]".format(left, top, right, bottom)

private fun shouldSwapWithTarget(
    draggedCenter: Offset,
    currentBounds: Rect,
    targetBounds: Rect
): Boolean {
    val deltaX = targetBounds.center.x - currentBounds.center.x
    val deltaY = targetBounds.center.y - currentBounds.center.y
    return if (abs(deltaX) >= abs(deltaY)) {
        val midpointX = (currentBounds.center.x + targetBounds.center.x) / 2f
        if (deltaX > 0f) draggedCenter.x >= midpointX else draggedCenter.x <= midpointX
    } else {
        val midpointY = (currentBounds.center.y + targetBounds.center.y) / 2f
        if (deltaY > 0f) draggedCenter.y >= midpointY else draggedCenter.y <= midpointY
    }
}

fun formatMetricValue(type: MetricType, metrics: VehicleMetrics, rideElapsedMs: Long = 0L): String {
    return when (type) {
        MetricType.SPEED -> String.format("%.1f", metrics.speedKmH)
        MetricType.GRADE -> String.format("%+.1f", metrics.gradePercent)
        MetricType.ALTITUDE -> String.format("%.0f", metrics.altitudeMeters)
        MetricType.VOLTAGE -> String.format("%.1f", metrics.voltage)
        MetricType.VOLTAGE_SAG -> String.format("%.1f", metrics.voltageSag)
        MetricType.BUS_CURRENT -> String.format("%.1f", metrics.busCurrent)
        MetricType.PHASE_CURRENT -> String.format("%.1f", metrics.phaseCurrent)
        MetricType.POWER -> String.format("%.1f", metrics.totalPowerW / 1000)
        MetricType.TEMP -> String.format("%.1f", metrics.controllerTemp)
        MetricType.MAX_CONTROLLER_TEMP -> String.format("%.1f", metrics.maxControllerTemp)
        MetricType.SOC -> String.format("%.0f", metrics.soc)
        MetricType.RANGE -> String.format("%.1f", metrics.estimatedRangeKm)
        MetricType.RPM -> String.format("%.0f", metrics.rpm)
        MetricType.EFFICIENCY -> String.format("%.1f", metrics.avgEfficiencyWhKm)
        MetricType.TRIP_DISTANCE -> String.format("%.2f", metrics.tripDistance)
        MetricType.ELAPSED_TIME -> formatElapsedTime(rideElapsedMs)
        MetricType.TOTAL_ENERGY -> String.format("%.1f", metrics.totalEnergyWh)
        MetricType.PEAK_REGEN_POWER -> String.format("%.0f", metrics.peakRegenPowerKw * 1000f)
        MetricType.RECOVERED_ENERGY -> String.format("%.1f", metrics.recoveredEnergyWh)
        MetricType.MEDIA_CONTROL -> ""
    }
}

private fun formatElapsedTime(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

private fun dashboardSecondaryLine(type: MetricType, metrics: VehicleMetrics, rideElapsedMs: Long): String {
    return when (type) {
        MetricType.TEMP -> "最高 ${String.format("%.1f", metrics.maxControllerTemp)} °C"
        MetricType.VOLTAGE_SAG -> "最高 ${String.format("%.1f", metrics.maxVoltageSag)} V"
        MetricType.BUS_CURRENT -> "平均 ${String.format("%.1f", metrics.avgBusCurrent)} A"
        MetricType.POWER -> "峰值 ${String.format("%.1f", metrics.peakRegenPowerKw * 1000f)} W 回收"
        MetricType.SOC -> "续航 ${String.format("%.1f", metrics.estimatedRangeKm)} km"
        MetricType.TRIP_DISTANCE -> "用时 ${formatElapsedTime(rideElapsedMs)}"
        MetricType.TOTAL_ENERGY -> "回收 ${String.format("%.1f", metrics.recoveredEnergyWh)} Wh"
        MetricType.RECOVERED_ENERGY -> "峰值 ${String.format("%.0f", metrics.peakRegenPowerKw * 1000f)} W"
        else -> ""
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToZhikeSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = viewModel.metrics.collectAsStateWithLifecycle().value
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    val dashboardItems by viewModel.dashboardItems.collectAsStateWithLifecycle()
    val activeProtocol by viewModel.activeProtocolLabel.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val autoConnectState by viewModel.autoConnectState.collectAsStateWithLifecycle()
    val isRideActive by viewModel.isRideActive.collectAsStateWithLifecycle()
    val rideElapsedMs by viewModel.rideElapsedMs.collectAsStateWithLifecycle()
    val rideDirectionLabel by viewModel.rideDirectionLabel.collectAsStateWithLifecycle()
    
    val mediaTarget by viewModel.mediaTarget.collectAsStateWithLifecycle()
    val isHidConnected by viewModel.isHidConnected.collectAsStateWithLifecycle()
    val isHidSubscribed by viewModel.isHidSubscribed.collectAsStateWithLifecycle()
    val isHidSupported by viewModel.isHidSupported.collectAsStateWithLifecycle()
    val isHogpActive by viewModel.isHogpActive.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var isEditMode by remember { mutableStateOf(false) }
    var showAddPicker by remember { mutableStateOf(false) }
    var showConnectionSheet by remember { mutableStateOf(false) }
    var draggingItem by remember { mutableStateOf<MetricType?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var dragStartBounds by remember { mutableStateOf<Rect?>(null) }
    var lastReorderHapticAt by remember { mutableLongStateOf(0L) }
    val itemBounds = remember { mutableStateMapOf<MetricType, Rect>() }
    val displayedDashboardItems = remember { mutableStateListOf<MetricType>() }
    val gridItems = if (isEditMode || draggingItem != null) displayedDashboardItems else dashboardItems

    fun replaceDashboardDraft(items: List<MetricType>) {
        displayedDashboardItems.clear()
        displayedDashboardItems.addAll(items.distinct())
    }

    fun enterDashboardEditMode() {
        replaceDashboardDraft(dashboardItems)
        isEditMode = true
    }

    fun saveDashboardDraft() {
        val draft = displayedDashboardItems.distinct()
        if (draft.isNotEmpty()) {
            replaceDashboardDraft(draft)
            viewModel.saveDashboardItems(draft)
        } else {
            replaceDashboardDraft(dashboardItems)
        }
        draggingItem = null
        dragOffset = Offset.Zero
        dragStartBounds = null
        itemBounds.clear()
        isEditMode = false
    }

    fun discardDashboardDraft() {
        replaceDashboardDraft(dashboardItems)
        draggingItem = null
        dragOffset = Offset.Zero
        dragStartBounds = null
        itemBounds.clear()
        isEditMode = false
    }

    LaunchedEffect(dashboardItems, isEditMode) {
        if (!isEditMode) {
            replaceDashboardDraft(dashboardItems)
        }
    }

    val isRegen = metrics.busCurrent < 0
    val primaryColor = MaterialTheme.colorScheme.primary
    val ringColor = if (isRegen) Color(0xFF10B981) else primaryColor
    val isControllerConnected = connectionState is ConnectionState.Connected

    // Use auto-connect state for natural status messages
    val connectionStatusLabel by remember {
        derivedStateOf {
            when {
                connectionState is ConnectionState.Connected -> activeProtocol
                autoConnectState is AutoConnectState.Connecting -> "正在连接 ${autoConnectState.displayName}…"
                autoConnectState is AutoConnectState.Searching -> "搜索设备中…"
                autoConnectState is AutoConnectState.NotFound -> "未找到已知设备"
                autoConnectState is AutoConnectState.Error -> "连接失败"
                connectionState is ConnectionState.Connecting -> "连接中"
                connectionState is ConnectionState.Error -> "连接错误"
                else -> "未连接"
            }
        }
    }

    val angle = if (isEditMode) {
        val infiniteTransition = rememberInfiniteTransition(label = "shake")
        infiniteTransition.animateFloat(
            initialValue = -1.5f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(120, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shake_angle"
        ).value
    } else {
        0f
    }

    if (isEditMode) {
        BackHandler {
            discardDashboardDraft()
        }
    }

    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    }

    DisposableEffect(isLandscape, activity, view) {
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        if (controller != null) {
            controller.systemBarsBehavior =
                androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (isLandscape) {
                controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            controller?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!isLandscape) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PageHorizontalPadding)
            ) {
                DashboardTopSection(
                    isEditMode = isEditMode,
                    isRideActive = isRideActive,
                    rideDirectionLabel = rideDirectionLabel,
                    onEditModeToggle = { editing ->
                        if (editing) {
                            enterDashboardEditMode()
                        } else {
                            saveDashboardDraft()
                        }
                    },
                    onAddClick = { showAddPicker = true },
                    onRideToggle = { viewModel.toggleRideTracking() }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = SectionSpacing),
                    verticalArrangement = Arrangement.spacedBy(SectionSpacing)
                ) {
                    DashboardStatusStrip(
                        metrics = metrics,
                        connectionStatusLabel = connectionStatusLabel,
                        isControllerConnected = isControllerConnected,
                        onConnectionClick = { showConnectionSheet = true },
                        onTuningClick = if (activeProtocol == "zhike") {
                            onNavigateToZhikeSettings
                        } else null
                    )
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        SquareSpeedIndicator(
                            metrics = metrics,
                            color = ringColor
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(CardSpacing),
                    verticalArrangement = Arrangement.spacedBy(CardSpacing),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp)
                ) {
                    dashboardItemsGridContent(
                        items = gridItems,
                        metrics = metrics,
                        rideElapsedMs = rideElapsedMs,
                        isEditMode = isEditMode,
                        draggingItem = draggingItem,
                        shakeAngle = angle,
                        onMediaAction = { viewModel.onMediaAction(it) },
                        onSetMediaTarget = { viewModel.setMediaTarget(it) },
                        onToggleDiscoverable = { viewModel.toggleBluetoothDiscoverable() },
                        mediaTarget = mediaTarget,
                        isHidConnected = isHidConnected,
                        isHidSubscribed = isHidSubscribed,
                        isHidSupported = isHidSupported,
                        isHogpActive = isHogpActive,
                        onMove = { from, to ->
                            if (from in displayedDashboardItems.indices && to in displayedDashboardItems.indices && from != to) {
                                val moved = displayedDashboardItems.removeAt(from)
                                displayedDashboardItems.add(to, moved)
                            }
                            val now = SystemClock.elapsedRealtime()
                            if (now - lastReorderHapticAt >= 110L) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastReorderHapticAt = now
                            }
                        },
                        onRemove = { idx ->
                            if (idx in displayedDashboardItems.indices && displayedDashboardItems.size > 1) {
                                displayedDashboardItems.removeAt(idx)
                            }
                        },
                        onEnterEditMode = {
                            if (!isEditMode) {
                                enterDashboardEditMode()
                            }
                        },
                        onDragStart = { type ->
                            draggingItem = type
                            dragOffset = Offset.Zero
                            dragStartBounds = itemBounds[type]
                            lastReorderHapticAt = 0L
                        },
                        onDrag = { offset -> dragOffset += offset },
                        onDragEnd = {
                            draggingItem = null
                            dragOffset = Offset.Zero
                            dragStartBounds = null
                        },
                        itemBounds = itemBounds
                    )
                }
            }
        } else {
            LandscapeDashboardFocus(
                metrics = metrics,
                color = ringColor,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (showConnectionSheet) {
            ConnectionQuickSheet(
                viewModel = viewModel,
                onDismiss = { showConnectionSheet = false }
            )
        }

        if (showAddPicker) {
            BlurredModalBottomSheet(onDismissRequest = { showAddPicker = false }) {
                PredictiveBackPopupTransform(
                    onBack = { showAddPicker = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("添加数据模块", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                        val available = MetricType.entries.filter { it !in displayedDashboardItems }
                        if (available.isEmpty()) {
                            Text("所有支持的数据模块均已添加", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                        } else {
                            available.forEach { type ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    shape = bezierRoundedShape(CardCornerRadius),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    tonalElevation = 0.5.dp,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (type !in displayedDashboardItems) {
                                            displayedDashboardItems.add(type)
                                        }
                                        showAddPicker = false
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(type.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
        val draggedType = draggingItem
        val startBounds = dragStartBounds
        if (draggedType != null && startBounds != null) {
            StatCardWrap(
                metrics = metrics,
                rideElapsedMs = rideElapsedMs,
                type = draggedType,
                isEditMode = false,
                shakeAngle = 0f,
                onRemove = {},
                mediaTarget = mediaTarget,
                isHidConnected = isHidConnected,
                isHidSubscribed = isHidSubscribed,
                isHidSupported = isHidSupported,
                isHogpActive = isHogpActive,
                onMediaAction = { viewModel.onMediaAction(it) },
                onSetMediaTarget = { viewModel.setMediaTarget(it) },
                onToggleDiscoverable = { viewModel.toggleBluetoothDiscoverable() },
                modifier = Modifier
                    .zIndex(10f)
                    .offset {
                        IntOffset(
                            (startBounds.left + dragOffset.x).roundToInt(),
                            (startBounds.top + dragOffset.y).roundToInt()
                        )
                    }
                    .width(with(LocalDensity.current) { startBounds.width.toDp() })
                    .height(with(LocalDensity.current) { startBounds.height.toDp() })
                    .graphicsLayer {
                        scaleX = 1f
                        scaleY = 1f
                        alpha = 1f
                        shadowElevation = 16f
                    }
            )
        }
    }
}

@Composable
private fun LandscapeDashboardFocus(
    metrics: VehicleMetrics,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        FocusSpeedIndicator(
            metrics = metrics,
            color = color,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun DashboardTopSection(
    isEditMode: Boolean,
    isRideActive: Boolean,
    rideDirectionLabel: String,
    onEditModeToggle: (Boolean) -> Unit,
    onAddClick: () -> Unit,
    onRideToggle: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        DashboardHeader(
            isEditMode = isEditMode,
            isRideActive = isRideActive,
            rideDirectionLabel = rideDirectionLabel,
            onEditModeToggle = onEditModeToggle,
            onAddClick = onAddClick,
            onRideToggle = onRideToggle
        )
    }
}

private fun androidx.compose.foundation.lazy.grid.LazyGridScope.dashboardItemsGridContent(
    items: List<MetricType>,
    metrics: VehicleMetrics,
    rideElapsedMs: Long,
    isEditMode: Boolean,
    draggingItem: MetricType?,
    shakeAngle: Float,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onEnterEditMode: () -> Unit,
    onDragStart: (MetricType) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    mediaTarget: com.shawnrain.sdash.MediaTarget,
    isHidConnected: Boolean,
    isHidSubscribed: Boolean,
    isHidSupported: Boolean,
    isHogpActive: Boolean,
    onMediaAction: (com.shawnrain.sdash.MediaAction) -> Unit,
    onSetMediaTarget: (com.shawnrain.sdash.MediaTarget) -> Unit,
    onToggleDiscoverable: () -> Unit,
    itemBounds: MutableMap<MetricType, Rect>
) {
    itemsIndexed(items, key = { _, type -> type.name }) { index, type ->
        val isDragging = draggingItem == type
        val shake = if (isEditMode && !isDragging) shakeAngle else 0f
        
        StatCardWrap(
            metrics = metrics,
            rideElapsedMs = rideElapsedMs,
            type = type,
            isEditMode = isEditMode,
            shakeAngle = shake,
            onRemove = { onRemove(index) },
            mediaTarget = mediaTarget,
            isHidConnected = isHidConnected,
            isHidSubscribed = isHidSubscribed,
            isHidSupported = isHidSupported,
            isHogpActive = isHogpActive,
            onMediaAction = onMediaAction,
            onSetMediaTarget = onSetMediaTarget,
            onToggleDiscoverable = onToggleDiscoverable,
            modifier = Modifier
                .then(if (isDragging) Modifier else Modifier.animateItem())
                .graphicsLayer {
                    if (isDragging) {
                        alpha = 0f
                    }
                }
                .onGloballyPositioned { coordinates ->
                    itemBounds[type] = coordinates.boundsInRoot()
                }
                .pointerInput(type) {
                    var localDragOffset = Offset.Zero
                    var dragAnchorBounds: Rect? = null
                    var lastMoveLogAt = 0L
                    var lastLoggedTargetIndex = -1
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            localDragOffset = Offset.Zero
                            dragAnchorBounds = itemBounds[type]
                            lastMoveLogAt = 0L
                            lastLoggedTargetIndex = -1
                            AppLogger.i(
                                DRAG_LOG_TAG,
                                "drag_start type=${type.name} startIndex=$index anchor=${dragAnchorBounds?.toLogString() ?: "null"}"
                            )
                            onDragStart(type)
                            onEnterEditMode()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            localDragOffset += dragAmount
                            onDrag(dragAmount)

                            val currentItems = items
                            val currentIndex = currentItems.indexOf(type)
                            if (currentIndex == -1) return@detectDragGesturesAfterLongPress
                            val sourceBounds = dragAnchorBounds ?: return@detectDragGesturesAfterLongPress
                            val currentBounds = itemBounds[type] ?: sourceBounds
                            val floatingRect = Rect(
                                offset = Offset(
                                    x = sourceBounds.left + localDragOffset.x,
                                    y = sourceBounds.top + localDragOffset.y
                                ),
                                size = sourceBounds.size
                            )
                            val draggedCenter = Offset(
                                x = sourceBounds.center.x + localDragOffset.x,
                                y = sourceBounds.center.y + localDragOffset.y
                            )
                            val targetType = currentItems.firstOrNull { candidate ->
                                candidate != type && itemBounds[candidate]?.contains(draggedCenter) == true
                            }
                            val targetIndex = targetType?.let(currentItems::indexOf) ?: -1
                            val now = SystemClock.elapsedRealtime()
                            if (now - lastMoveLogAt >= 120L) {
                                AppLogger.i(
                                    DRAG_LOG_TAG,
                                    "drag_move type=${type.name} offset=${localDragOffset.toLogString()} floating=${floatingRect.toLogString()} center=${draggedCenter.toLogString()} current=$currentIndex target=$targetIndex"
                                )
                                lastMoveLogAt = now
                            }
                            if (targetIndex != lastLoggedTargetIndex) {
                                AppLogger.i(
                                    DRAG_LOG_TAG,
                                    "drag_target type=${type.name} offset=${localDragOffset.toLogString()} floating=${floatingRect.toLogString()} center=${draggedCenter.toLogString()} current=$currentIndex target=$targetIndex"
                                )
                                lastLoggedTargetIndex = targetIndex
                            }
                            val targetBounds = targetType?.let(itemBounds::get)
                            val canSwap = targetBounds != null &&
                                targetIndex != -1 &&
                                targetIndex != currentIndex &&
                                shouldSwapWithTarget(
                                    draggedCenter = draggedCenter,
                                    currentBounds = currentBounds,
                                    targetBounds = targetBounds
                                )
                            if (canSwap) {
                                AppLogger.i(
                                    DRAG_LOG_TAG,
                                    "drag_reorder type=${type.name} offset=${localDragOffset.toLogString()} floating=${floatingRect.toLogString()} center=${draggedCenter.toLogString()} currentBounds=${currentBounds.toLogString()} targetBounds=${targetBounds.toLogString()} from=$currentIndex to=$targetIndex"
                                )
                                onMove(currentIndex, targetIndex)
                            }
                        },
                        onDragEnd = {
                            AppLogger.i(
                                DRAG_LOG_TAG,
                                "drag_end type=${type.name} finalOffset=${localDragOffset.toLogString()} finalAnchor=${dragAnchorBounds?.toLogString() ?: "null"}"
                            )
                            onDragEnd()
                        },
                        onDragCancel = {
                            AppLogger.i(
                                DRAG_LOG_TAG,
                                "drag_cancel type=${type.name} finalOffset=${localDragOffset.toLogString()} finalAnchor=${dragAnchorBounds?.toLogString() ?: "null"}"
                            )
                            onDragEnd()
                        }
                    )
                }
        )
    }
}

@Composable
fun DashboardHeader(
    isEditMode: Boolean,
    isRideActive: Boolean,
    rideDirectionLabel: String,
    onEditModeToggle: (Boolean) -> Unit,
    onAddClick: () -> Unit,
    onRideToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditMode) {
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                }
            } else {
                Surface(
                    onClick = onRideToggle,
                    shape = bezierPillShape(),
                    color = if (isRideActive) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = if (isRideActive) "结束" else "开始",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        color = if (isRideActive) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(1.dp))

            if (isEditMode) {
                TextButton(onClick = { onEditModeToggle(false) }) {
                    Text("完成", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = rideDirectionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }

        Text(
            "SmartDash",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun StatCardWrap(
    metrics: VehicleMetrics,
    rideElapsedMs: Long = 0L,
    type: MetricType,
    isEditMode: Boolean,
    shakeAngle: Float,
    onRemove: () -> Unit,
    mediaTarget: com.shawnrain.sdash.MediaTarget,
    isHidConnected: Boolean,
    isHidSubscribed: Boolean,
    isHidSupported: Boolean,
    isHogpActive: Boolean,
    onMediaAction: (com.shawnrain.sdash.MediaAction) -> Unit,
    onSetMediaTarget: (com.shawnrain.sdash.MediaTarget) -> Unit,
    onToggleDiscoverable: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (type == MetricType.MEDIA_CONTROL) {
        MediaControlCard(
            mediaTarget = mediaTarget,
            isHidConnected = isHidConnected,
            isHidSubscribed = isHidSubscribed,
            isHidSupported = isHidSupported,
            isHogpActive = isHogpActive,
            onMediaAction = onMediaAction,
            onSetMediaTarget = onSetMediaTarget,
            onToggleDiscoverable = onToggleDiscoverable,
            shakeAngle = shakeAngle,
            isEditMode = isEditMode,
            onRemove = onRemove,
            modifier = modifier
        )
        return
    }

    val valueStr = remember(metrics, type, rideElapsedMs) {
        formatMetricValue(type, metrics, rideElapsedMs)
    }
    val secondaryText = remember(metrics, type, rideElapsedMs) {
        dashboardSecondaryLine(type, metrics, rideElapsedMs)
    }

    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = shakeAngle }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(CardHeight),
            shape = bezierRoundedShape(CardCornerRadius),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.5.dp
        ) {
            Column(
                modifier = Modifier.padding(CardInnerPadding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = type.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                BaselineMetricValue(
                    value = valueStr,
                    unit = type.unit,
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    unitColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    valueFontSize = 24.sp,
                    unitFontSize = 24.sp,
                    valueFontWeight = FontWeight.Bold,
                    unitFontWeight = FontWeight.Bold,
                    singleLine = true,
                    unitSpacing = 3.dp
                )
                Text(
                    text = secondaryText.ifBlank { " " },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(bezierRoundedShape(7.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardStatusStrip(
    metrics: VehicleMetrics,
    connectionStatusLabel: String,
    isControllerConnected: Boolean,
    onConnectionClick: () -> Unit,
    onTuningClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            CompactTelemetryBadge(
                label = "SOC",
                value = if (isControllerConnected) {
                    String.format("%.0f%%", metrics.soc)
                } else {
                    "--"
                }
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            ConnectionStatusBadge(
                label = if (isControllerConnected) connectionStatusLabel else "点击连接",
                isConnected = isControllerConnected,
                onClick = onConnectionClick,
                onTuningClick = onTuningClick
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            CompactTelemetryBadge(
                label = "续航",
                value = if (isControllerConnected && metrics.estimatedRangeKm > 0.1f) {
                    String.format("%.1f km", metrics.estimatedRangeKm)
                } else {
                    "--"
                }
            )
        }
    }
}

@Composable
fun SquareSpeedIndicator(
    metrics: VehicleMetrics,
    color: Color
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = metrics.speedKmH,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "speed_anim"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(SpeedAreaHeight),
        shape = bezierRoundedShape(SpeedAreaCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.5.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = PageHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Real-time Power
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%.2f", metrics.totalPowerW / 1000f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "功率 kW",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Center: Speed
                Column(
                    modifier = Modifier.weight(1.6f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BaselineMetricValue(
                        value = String.format("%.0f", animatedSpeed),
                        unit = "",
                        valueColor = color,
                        unitColor = Color.Transparent,
                        valueFontSize = 64.sp,
                        unitFontSize = 14.sp,
                        valueFontWeight = FontWeight.Black,
                        valueLineHeight = 64.sp,
                        horizontalArrangement = Arrangement.Center,
                        textAlign = TextAlign.Center,
                        singleLine = true
                    )
                }

                // Right: Avg Efficiency
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%.1f", metrics.avgEfficiencyWhKm),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "平均 Wh/km",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            PowerBalanceBar(
                powerKw = metrics.totalPowerW / 1000f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CardInnerPadding, vertical = 8.dp)
                    .height(6.dp)
            )
        }
    }
}

@Composable
private fun CompactTelemetryBadge(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
        shape = bezierPillShape()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ConnectionStatusBadge(
    label: String, 
    isConnected: Boolean, 
    onClick: () -> Unit,
    onTuningClick: (() -> Unit)? = null
) {
    Surface(
        color = if (isConnected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        shape = bezierPillShape(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isConnected) Color(0xFF16A34A) else MaterialTheme.colorScheme.outline
                    )
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isConnected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (isConnected && onTuningClick != null) {
                VerticalDivider(
                    modifier = Modifier.height(12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Tuning",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onTuningClick() }
                )
            }
        }
    }
}

@Composable
private fun PowerBalanceBar(
    powerKw: Float,
    modifier: Modifier = Modifier
) {
    var positivePeakKw by remember { mutableFloatStateOf(3.5f) }
    var regenPeakKw by remember { mutableFloatStateOf(0.8f) }

    LaunchedEffect(powerKw) {
        positivePeakKw = when {
            powerKw > 0f -> maxOf(positivePeakKw * 0.985f, 3.5f, powerKw * 1.12f)
            else -> maxOf(positivePeakKw * 0.992f, 3.5f)
        }
        regenPeakKw = when {
            powerKw < 0f -> maxOf(regenPeakKw * 0.978f, 0.8f, abs(powerKw) * 1.12f)
            else -> maxOf(regenPeakKw * 0.99f, 0.8f)
        }
    }

    val targetFraction = when {
        powerKw > 0f -> (powerKw / positivePeakKw).coerceIn(0f, 1f)
        powerKw < 0f -> (abs(powerKw) / regenPeakKw).coerceIn(0f, 1f)
        else -> 0f
    }
    val visualTargetFraction = remember(targetFraction, powerKw) {
        visualPowerFraction(
            normalizedFraction = targetFraction,
            isOutput = powerKw >= 0f,
            hasActivePower = abs(powerKw) >= 0.08f
        )
    }
    val animatedFraction by animateFloatAsState(
        targetValue = visualTargetFraction,
        animationSpec = tween(durationMillis = 180),
        label = "power_balance_fraction"
    )
    val isOutput = powerKw >= 0f
    val hasActivePower = abs(powerKw) >= 0.08f

    Box(
        modifier = modifier
            .clip(bezierRoundedShape(999.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight()
                .width(1.5.dp)
                .background(Color.White.copy(alpha = 0.38f))
        )
        if (hasActivePower) {
            val activeColor = powerBarColor(
                normalizedFraction = targetFraction,
                isOutput = isOutput
            )
            Box(
                modifier = Modifier
                    .align(if (isOutput) Alignment.CenterEnd else Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f * animatedFraction)
                    .clip(bezierRoundedShape(999.dp))
                    .background(activeColor)
            )
        }
    }
}

private fun visualPowerFraction(
    normalizedFraction: Float,
    isOutput: Boolean,
    hasActivePower: Boolean
): Float {
    if (!hasActivePower) return 0f
    val raw = normalizedFraction.coerceIn(0f, 1f)
    return if (isOutput) {
        maxOf(0.06f, raw.pow(0.82f))
    } else {
        maxOf(0.16f, raw.pow(0.56f))
    }.coerceIn(0f, 1f)
}

@Composable
private fun FocusSpeedIndicator(
    metrics: VehicleMetrics,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(bezierRoundedShape(34.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp)
            .clip(bezierRoundedShape(30.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FocusMetricColumn(
                value = String.format("%.2f", metrics.totalPowerW / 1000f),
                label = "功率 kW",
                modifier = Modifier.weight(0.95f)
            )

            Column(
                modifier = Modifier.weight(1.8f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BaselineMetricValue(
                    value = String.format("%.0f", metrics.speedKmH),
                    unit = "",
                    valueColor = color,
                    unitColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    valueFontSize = 120.sp, 
                    unitFontSize = 24.sp,
                    valueFontWeight = FontWeight.Black,
                    unitFontWeight = FontWeight.Bold,
                    valueLineHeight = 120.sp,
                    horizontalArrangement = Arrangement.Center,
                    textAlign = TextAlign.Center,
                    singleLine = true
                )
            }

            FocusMetricColumn(
                value = String.format("%.1f", metrics.avgEfficiencyWhKm),
                label = "平均 Wh/km",
                modifier = Modifier.weight(0.95f)
            )
        }

        PowerBalanceBar(
            powerKw = metrics.totalPowerW / 1000f,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 3.dp, vertical = 0.dp)
                .height(8.dp)
        )
    }
}

@Composable
fun CompactMediaControls(
    mediaTarget: com.shawnrain.sdash.MediaTarget,
    isHidConnected: Boolean,
    isHidSupported: Boolean,
    isHogpActive: Boolean,
    onMediaAction: (com.shawnrain.sdash.MediaAction) -> Unit,
    onSetMediaTarget: (com.shawnrain.sdash.MediaTarget) -> Unit,
    onToggleDiscoverable: () -> Unit,
    isCompactLandscape: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = if (isCompactLandscape) 0.dp else 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { onMediaAction(com.shawnrain.sdash.MediaAction.PREVIOUS) },
                modifier = Modifier.size(if (isCompactLandscape) 32.dp else 36.dp)
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
            
            Surface(
                onClick = { onMediaAction(com.shawnrain.sdash.MediaAction.PLAY_PAUSE) },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(if (isCompactLandscape) 42.dp else 48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
            }

            IconButton(
                onClick = { onMediaAction(com.shawnrain.sdash.MediaAction.NEXT) },
                modifier = Modifier.size(if (isCompactLandscape) 32.dp else 36.dp)
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
        }
        
        // Compact Status & Switcher
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable {
                    onSetMediaTarget(
                        if (mediaTarget == com.shawnrain.sdash.MediaTarget.SYSTEM)
                            com.shawnrain.sdash.MediaTarget.REMOTE
                        else com.shawnrain.sdash.MediaTarget.SYSTEM
                    )
                }
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = if (mediaTarget == com.shawnrain.sdash.MediaTarget.SYSTEM)
                    Icons.Default.SettingsInputAntenna else Icons.Default.BluetoothConnected,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (mediaTarget == com.shawnrain.sdash.MediaTarget.SYSTEM) "系统级" else "专属遥控",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (mediaTarget == com.shawnrain.sdash.MediaTarget.REMOTE) {
                val statusText = when {
                    isHidConnected -> " · 已连接"
                    isHogpActive -> " · 配对中"
                    else -> " · 未连接"
                }
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = if (isHidConnected) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.clickable(enabled = !isHidConnected) { onToggleDiscoverable() }
                )
            }
        }
    }
}

private fun powerBarColor(normalizedFraction: Float, isOutput: Boolean): Color {
    val fraction = normalizedFraction.coerceIn(0f, 1f)
    return if (isOutput) {
        when {
            fraction < 0.33f -> blendColor(Color(0xFFD4BF7A), Color(0xFFD6A86A), fraction / 0.33f)
            fraction < 0.66f -> blendColor(Color(0xFFD6A86A), Color(0xFFCC8968), (fraction - 0.33f) / 0.33f)
            else -> blendColor(Color(0xFFCC8968), Color(0xFFBA6F68), (fraction - 0.66f) / 0.34f)
        }
    } else {
        when {
            fraction < 0.25f -> blendColor(Color(0xFFD1A56A), Color(0xFFC7BD78), fraction / 0.25f)
            fraction < 0.6f -> blendColor(Color(0xFFC7BD78), Color(0xFF8BD39A), (fraction - 0.25f) / 0.35f)
            else -> blendColor(Color(0xFF8BD39A), Color(0xFF16C784), (fraction - 0.6f) / 0.4f)
        }
    }
}

private fun blendColor(start: Color, end: Color, fraction: Float): Color {
    val t = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * t,
        green = start.green + (end.green - start.green) * t,
        blue = start.blue + (end.blue - start.blue) * t,
        alpha = start.alpha + (end.alpha - start.alpha) * t
    )
}

@Composable
private fun FocusMetricColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ImmersiveDrivingView(
    metrics: VehicleMetrics,
    items: List<MetricType>,
    onManualExit: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val detailItems = remember(items) {
        val preferredOrder = listOf(
            MetricType.VOLTAGE,
            MetricType.BUS_CURRENT,
            MetricType.PHASE_CURRENT,
            MetricType.TEMP,
            MetricType.GRADE,
            MetricType.ALTITUDE,
            MetricType.TRIP_DISTANCE,
            MetricType.SOC,
            MetricType.RANGE,
            MetricType.EFFICIENCY,
            MetricType.RPM
        )
        (preferredOrder.filter { it in items } + items.filterNot { it in preferredOrder })
            .distinct()
            .take(6)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BaselineMetricValue(
                    value = String.format("%.0f", metrics.speedKmH),
                    unit = "",
                    valueColor = Color.White,
                    unitColor = Color.White.copy(alpha = 0.55f),
                    valueFontSize = 148.sp,
                    unitFontSize = 22.sp,
                    valueFontWeight = FontWeight.Black,
                    unitFontWeight = FontWeight.Bold,
                    valueLineHeight = 136.sp,
                    horizontalArrangement = Arrangement.Center,
                    textAlign = TextAlign.Center,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ImmersiveHighlightCard(
                        label = "功率",
                        value = String.format("%.2f", metrics.totalPowerW / 1000f),
                        unit = "kW",
                        modifier = Modifier.weight(1f)
                    )
                    ImmersiveHighlightCard(
                        label = "平均能耗",
                        value = String.format("%.1f", metrics.avgEfficiencyWhKm),
                        unit = "Wh/km",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(detailItems.size) { index ->
                    ImmersiveMetricTile(
                        type = detailItems[index],
                        metrics = metrics
                    )
                }
            }

            Text(
                text = "驾驶模式 · 静止约 2.5 秒自动退出",
                color = Color.White.copy(alpha = 0.38f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.22f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(22.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onManualExit()
                        }
                    )
                }
        )
    }
}

@Composable
private fun ImmersiveHighlightCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.06f),
        shape = bezierRoundedShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.56f))
            Spacer(modifier = Modifier.height(6.dp))
            BaselineMetricValue(
                value = value,
                unit = unit,
                valueColor = Color.White,
                unitColor = Color.White.copy(alpha = 0.62f),
                valueFontSize = 28.sp,
                unitFontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ImmersiveMetricTile(
    type: MetricType,
    metrics: VehicleMetrics
) {
    val value = formatMetricValue(type, metrics)
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = bezierRoundedShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BaselineMetricValue(
                value = value,
                unit = type.unit,
                valueColor = Color.White,
                unitColor = Color.White.copy(alpha = 0.62f),
                valueFontSize = 22.sp,
                unitFontSize = 11.sp,
                horizontalArrangement = Arrangement.Center,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = type.title,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BaselineMetricValue(
    value: String,
    unit: String,
    valueColor: Color,
    unitColor: Color,
    valueFontSize: androidx.compose.ui.unit.TextUnit,
    unitFontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
    valueFontWeight: FontWeight = FontWeight.Bold,
    unitFontWeight: FontWeight = FontWeight.Normal,
    valueLineHeight: androidx.compose.ui.unit.TextUnit = valueFontSize,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    textAlign: TextAlign? = null,
    singleLine: Boolean = false,
    unitSpacing: androidx.compose.ui.unit.Dp = 4.dp,
    unitMatchesValueStyle: Boolean = false
) {
    val resolvedUnitFontSize = if (unitMatchesValueStyle) valueFontSize else unitFontSize
    val resolvedUnitFontWeight = if (unitMatchesValueStyle) valueFontWeight else unitFontWeight
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = horizontalArrangement
    ) {
        Text(
            text = value,
            color = valueColor,
            fontSize = valueFontSize,
            fontWeight = valueFontWeight,
            lineHeight = valueLineHeight,
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = !singleLine,
            modifier = Modifier.alignByBaseline()
        )
        if (unit.isNotEmpty()) {
            Spacer(modifier = Modifier.width(unitSpacing))
            Text(
                text = unit,
                color = unitColor,
                fontSize = resolvedUnitFontSize,
                fontWeight = resolvedUnitFontWeight,
                textAlign = textAlign,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}

private tailrec fun android.content.Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

@Composable
fun MediaControlCard(
    mediaTarget: com.shawnrain.sdash.MediaTarget,
    isHidConnected: Boolean,
    isHidSubscribed: Boolean,
    isHidSupported: Boolean,
    isHogpActive: Boolean,
    onMediaAction: (com.shawnrain.sdash.MediaAction) -> Unit,
    onSetMediaTarget: (com.shawnrain.sdash.MediaTarget) -> Unit,
    onToggleDiscoverable: () -> Unit,
    shakeAngle: Float,
    isEditMode: Boolean = false,
    onRemove: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = shakeAngle }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(CardHeight),
            shape = bezierRoundedShape(CardCornerRadius),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.5.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AnimatedMediaButton(
                        onClick = { onMediaAction(com.shawnrain.sdash.MediaAction.PREVIOUS) },
                        icon = Icons.Default.SkipPrevious,
                        contentDescription = "上一个",
                        size = 30.dp
                    )
                    
                    AnimatedMediaButton(
                        onClick = { onMediaAction(com.shawnrain.sdash.MediaAction.PLAY_PAUSE) },
                        icon = Icons.Default.PlayArrow,
                        contentDescription = "播放暂停",
                        size = 40.dp,
                        isPrimary = true
                    )

                    AnimatedMediaButton(
                        onClick = { onMediaAction(com.shawnrain.sdash.MediaAction.NEXT) },
                        icon = Icons.Default.SkipNext,
                        contentDescription = "下一个",
                        size = 30.dp
                    )
                }

                // Device Switcher
                Surface(
                    onClick = {
                        onSetMediaTarget(
                            if (mediaTarget == com.shawnrain.sdash.MediaTarget.SYSTEM)
                                com.shawnrain.sdash.MediaTarget.REMOTE
                            else com.shawnrain.sdash.MediaTarget.SYSTEM
                        )
                    },
                    shape = bezierPillShape(),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (mediaTarget == com.shawnrain.sdash.MediaTarget.SYSTEM)
                                Icons.Default.SettingsInputAntenna else Icons.Default.BluetoothConnected,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = if (mediaTarget == com.shawnrain.sdash.MediaTarget.REMOTE && isHidSubscribed)
                                Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when {
                                mediaTarget == com.shawnrain.sdash.MediaTarget.SYSTEM -> "控制: 手机转发"
                                isHidSubscribed -> "控制: 模拟遥控"
                                isHidConnected -> "控制: 配对中..."
                                else -> "控制: 等待..."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (mediaTarget == com.shawnrain.sdash.MediaTarget.REMOTE && isHidSubscribed)
                                Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(bezierRoundedShape(7.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedMediaButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    size: androidx.compose.ui.unit.Dp,
    isPrimary: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1f,
        label = "alpha"
    )

    Surface(
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick() 
        },
        shape = CircleShape,
        color = if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        modifier = Modifier
            .size(size)
            .scale(scale)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(size * 0.65f)
            )
        }
    }
}
