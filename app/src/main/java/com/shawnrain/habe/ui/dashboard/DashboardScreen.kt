package com.shawnrain.habe.ui.dashboard

import android.content.res.Configuration
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
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.windowInsetsPadding
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.ble.VehicleMetrics
import com.shawnrain.habe.data.MetricType
import com.shawnrain.habe.ui.theme.bezierRoundedShape
import androidx.compose.ui.zIndex

fun formatMetricValue(type: MetricType, metrics: VehicleMetrics): String {
    return when (type) {
        MetricType.VOLTAGE -> String.format("%.1f", metrics.voltage)
        MetricType.BUS_CURRENT -> String.format("%.1f", metrics.busCurrent)
        MetricType.PHASE_CURRENT -> String.format("%.1f", metrics.phaseCurrent)
        MetricType.POWER -> String.format("%.1f", metrics.totalPowerW / 1000)
        MetricType.TEMP -> String.format("%.1f", metrics.motorTemp)
        MetricType.SOC -> String.format("%.0f", metrics.soc)
        MetricType.RPM -> String.format("%.0f", metrics.rpm)
        MetricType.EFFICIENCY -> String.format("%.1f", metrics.efficiencyWhKm)
        MetricType.TRIP_DISTANCE -> String.format("%.2f", metrics.tripDistance)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val metrics = viewModel.metrics.collectAsState().value
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    val dashboardItems by viewModel.dashboardItems.collectAsState()
    val isDrivingMode by viewModel.isDrivingMode.collectAsState()
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    var isEditMode by remember { mutableStateOf(false) }
    var showAddPicker by remember { mutableStateOf(false) }
    var draggingItem by remember { mutableStateOf<MetricType?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    val isRegen = metrics.busCurrent < 0
    val ringColor = if (isRegen) Color(0xFF10B981) else MaterialTheme.colorScheme.primary

    // Auto-calibration / auto-config snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val calibrationMessage by viewModel.calibrationMessage.collectAsState()
    LaunchedEffect(calibrationMessage) {
        calibrationMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearCalibrationMessage()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val angle by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake_angle"
    )

    if (isEditMode) {
        BackHandler {
            isEditMode = false
        }
    }

    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose {
            view.keepScreenOn = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val uniqueDashboardItems = dashboardItems.distinct()

        if (!isLandscape) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .pointerInput(isEditMode) {
                        if (!isEditMode) {
                            detectTapGestures(onLongPress = {
                                isEditMode = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            })
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
            ) {
                // Header
                item(span = { GridItemSpan(2) }) {
                    val activeProtocol by viewModel.activeProtocolLabel.collectAsState()
                    DashboardTopSection(
                        isEditMode = isEditMode,
                        activeProtocol = activeProtocol,
                        onEditModeToggle = { isEditMode = it },
                        onAddClick = { showAddPicker = true }
                    )
                }
                
                // Speed Indicator
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        SquareSpeedIndicator(metrics = metrics, color = ringColor)
                    }
                }

                dashboardItemsGridContent(
                    items = uniqueDashboardItems,
                    metrics = metrics,
                    isEditMode = isEditMode,
                    draggingItem = draggingItem,
                    dragOffset = dragOffset,
                    shakeAngle = angle,
                    onMove = { from, to -> 
                        viewModel.moveDashboardItem(from, to)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onRemove = { idx -> viewModel.removeDashboardItem(idx) },
                    onDragStart = { type -> 
                        draggingItem = type
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { offset -> dragOffset += offset },
                    onAdjustOffset = { adjustment -> dragOffset += adjustment },
                    onDragEnd = {
                        draggingItem = null
                        dragOffset = Offset.Zero
                    }
                )
            }
        } else {
            LandscapeDashboardFocus(
                metrics = metrics,
                color = ringColor,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Immersive Driving Mode Overlay
        if (isDrivingMode) {
            ImmersiveDrivingView(metrics = metrics, items = uniqueDashboardItems)
        }

        if (showAddPicker) {
            ModalBottomSheet(onDismissRequest = { showAddPicker = false }) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("添加数据模块", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                    val available = MetricType.entries.filter { it !in uniqueDashboardItems }
                    if (available.isEmpty()) {
                        Text("所有支持的数据模块均已添加", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                    } else {
                        available.forEach { type ->
                            ListItem(
                                headlineContent = { Text(type.title) },
                                modifier = Modifier.clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.addDashboardItem(type)
                                    showAddPicker = false
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Calibration & auto-config notification
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
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
    activeProtocol: String,
    onEditModeToggle: (Boolean) -> Unit,
    onAddClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        DashboardHeader(isEditMode = isEditMode, onEditModeToggle = onEditModeToggle, onAddClick = onAddClick)
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            shape = bezierRoundedShape(12.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = activeProtocol,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.grid.LazyGridScope.dashboardItemsGridContent(
    items: List<MetricType>,
    metrics: VehicleMetrics,
    isEditMode: Boolean,
    draggingItem: MetricType?,
    dragOffset: Offset,
    shakeAngle: Float,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onDragStart: (MetricType) -> Unit,
    onDrag: (Offset) -> Unit,
    onAdjustOffset: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    itemsIndexed(items, key = { _, type -> type.name }) { index, type ->
        val isDragging = draggingItem == type
        val shake = if (isEditMode && !isDragging) shakeAngle else 0f
        val density = androidx.compose.ui.platform.LocalDensity.current
        
        StatCardWrap(
            metrics = metrics,
            type = type,
            isEditMode = isEditMode,
            shakeAngle = shake,
            onRemove = { onRemove(index) },
            modifier = Modifier
                .animateItem()
                .zIndex(if (isDragging) 1f else 0f)
                .graphicsLayer {
                    if (isDragging) {
                        scaleX = 1.05f
                        scaleY = 1.05f
                        alpha = 0.9f
                        translationX = dragOffset.x
                        translationY = dragOffset.y
                    }
                }
                .pointerInput(isEditMode, type) {
                    if (isEditMode) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart(type) },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount)
                                
                                // Precise Grid Metrics in Pixels
                                val spacingPx = with(density) { 8.dp.toPx() }
                                val fullW = size.width.toFloat() + spacingPx
                                val fullH = size.height.toFloat() + spacingPx
                                
                                // X Swap (Left/Right)
                                if (dragOffset.x > fullW * 0.5f && index % 2 == 0 && index + 1 < items.size) {
                                    onMove(index, index + 1)
                                    onAdjustOffset(Offset(-fullW, 0f))
                                } else if (dragOffset.x < -fullW * 0.5f && index % 2 == 1) {
                                    onMove(index, index - 1)
                                    onAdjustOffset(Offset(fullW, 0f))
                                }
                                
                                // Y Swap (Up/Down)
                                if (dragOffset.y > fullH * 0.5f && index + 2 < items.size) {
                                    onMove(index, index + 2)
                                    onAdjustOffset(Offset(0f, -fullH))
                                } else if (dragOffset.y < -fullH * 0.5f && index - 2 >= 0) {
                                    onMove(index, index - 2)
                                    onAdjustOffset(Offset(0f, fullH))
                                }
                            },
                            onDragEnd = onDragEnd
                        )
                    }
                }
        )
    }
}

@Composable
fun DashboardHeader(isEditMode: Boolean, onEditModeToggle: (Boolean) -> Unit, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isEditMode) {
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
        
        Text("Habe", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        
        if (isEditMode) {
            TextButton(onClick = { onEditModeToggle(false) }) {
                Text("完成", fontWeight = FontWeight.Bold)
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun StatCardWrap(
    metrics: VehicleMetrics,
    type: MetricType,
    isEditMode: Boolean,
    shakeAngle: Float,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val valueStr = remember(metrics, type) {
        formatMetricValue(type, metrics)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = shakeAngle
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
                .clip(bezierRoundedShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = type.title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = valueStr, color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = type.unit, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
                }
            }
        }

        if (isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(28.dp)
                    .clip(bezierRoundedShape(14.dp))
                    .background(Color.Red)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SquareSpeedIndicator(metrics: VehicleMetrics, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp) // Increased from 130
            .clip(bezierRoundedShape(20.dp)) // Rounder
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(2.dp)
            .clip(bezierRoundedShape(18.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Real-time Power
            Column(
                modifier = Modifier.weight(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%.2f", metrics.totalPowerW / 1000f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "功率 kW",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Center: Speed
            Column(
                modifier = Modifier.weight(1.5f), // Slightly more weight for speed
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%.1f", metrics.speedKmH),
                    fontSize = 68.sp, // Larger speed
                    fontWeight = FontWeight.Black,
                    color = color,
                    lineHeight = 68.sp
                )
                Text(
                    text = "km/h",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Right: Avg Efficiency
            Column(
                modifier = Modifier.weight(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%.1f", metrics.avgEfficiencyWhKm),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "平均 Wh/km",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        
        // Horizontal speed bar at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            val progress = (metrics.speedKmH / 120f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(color)
            )
        }
    }
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
                Text(
                    text = String.format("%.1f", metrics.speedKmH),
                    fontSize = 128.sp,
                    lineHeight = 128.sp,
                    fontWeight = FontWeight.Black,
                    color = color,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "km/h",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            FocusMetricColumn(
                value = String.format("%.1f", metrics.avgEfficiencyWhKm),
                label = "平均 Wh/km",
                modifier = Modifier.weight(0.95f)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(6.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
        ) {
            val progress = (metrics.speedKmH / 120f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(color)
            )
        }
    }
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
fun ImmersiveDrivingView(metrics: VehicleMetrics, items: List<MetricType>) {
    val haptic = LocalHapticFeedback.current

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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Massive Speed Display
            Text(
                text = String.format("%.0f", metrics.speedKmH),
                fontSize = 140.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = 140.sp
            )
            Text(
                text = "KM/H",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Dynamic grid for ALL items
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(items.size) { index ->
                    val type = items[index]
                    val valueStr = formatMetricValue(type, metrics)
                    val displayUnit = when (type) {
                        MetricType.VOLTAGE -> "V"
                        MetricType.BUS_CURRENT, MetricType.PHASE_CURRENT -> "A"
                        MetricType.TEMP -> "°C"
                        MetricType.SOC -> "%"
                        else -> ""
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (displayUnit.isNotEmpty()) valueStr + displayUnit else valueStr, 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (type == MetricType.POWER) "功率(kW)" else type.title, 
                            fontSize = 11.sp, 
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Lock Hint
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.align(Alignment.TopEnd).padding(24.dp).size(24.dp)
        )
        
        Text(
            text = "驾驶模式 · 停车自动退出",
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            fontSize = 10.sp
        )
    }
}
