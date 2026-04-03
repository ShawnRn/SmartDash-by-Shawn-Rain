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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.ble.VehicleMetrics
import com.shawnrain.habe.data.MetricType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val metrics = viewModel.metrics.collectAsState().value
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    val dashboardItems by viewModel.dashboardItems.collectAsState()
    val haptic = LocalHapticFeedback.current

    var isEditMode by remember { mutableStateOf(false) }
    var showAddPicker by remember { mutableStateOf(false) }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { 
                        isEditMode = true 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onTap = { if (isEditMode) isEditMode = false }
                )
            }
    ) {
        val uniqueDashboardItems = dashboardItems.distinct()

        if (!isLandscape) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 48.dp)
            ) {
                // Header Span
                item(span = { GridItemSpan(2) }) {
                    val activeProtocol by viewModel.activeProtocolLabel.collectAsState()
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        DashboardHeader(isEditMode = isEditMode, onEditModeToggle = { isEditMode = it }, onAddClick = { showAddPicker = true })
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
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
                
                // Ring Span
                item(span = { GridItemSpan(2) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 32.dp), contentAlignment = Alignment.Center) {
                        SpeedIndicator(speed = metrics.speedKmH, color = ringColor)
                    }
                }

                itemsIndexed(uniqueDashboardItems, key = { _, type -> type.name }) { index, type ->
                    val shake = if (isEditMode) angle else 0f
                    StatCardWrap(
                        metrics = metrics,
                        type = type,
                        isEditMode = isEditMode,
                        shakeAngle = shake,
                        onEditStart = { 
                            isEditMode = true 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onRemove = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.removeDashboardItem(dashboardItems.indexOf(type)) 
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        } else {
            // Landscape
            Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    SpeedIndicator(speed = metrics.speedKmH, color = ringColor)
                }
                Spacer(modifier = Modifier.width(24.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        DashboardHeader(isEditMode = isEditMode, onEditModeToggle = { isEditMode = it }, onAddClick = { showAddPicker = true })
                    }
                    
                    itemsIndexed(uniqueDashboardItems, key = { _, type -> type.name }) { index, type ->
                        val shake = if (isEditMode) angle else 0f
                        StatCardWrap(
                            metrics = metrics,
                            type = type,
                            isEditMode = isEditMode,
                            shakeAngle = shake,
                            onEditStart = { 
                                isEditMode = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onRemove = { 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.removeDashboardItem(dashboardItems.indexOf(type)) 
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatCardWrap(
    metrics: VehicleMetrics,
    type: MetricType,
    isEditMode: Boolean,
    shakeAngle: Float,
    onEditStart: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val valueStr = when (type) {
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

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = shakeAngle
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(
                    onClick = { if (!isEditMode) { /* normal click */ } },
                    onLongClick = onEditStart
                )
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = type.title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = valueStr, color = MaterialTheme.colorScheme.onSurface, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = type.unit, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }

        if (isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(28.dp)
                    .clip(CircleShape)
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
fun SpeedIndicator(speed: Float, color: Color) {
    Box(
        modifier = Modifier
            .size(260.dp)
            .clip(RoundedCornerShape(130.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant) 
            .padding(16.dp)
            .clip(RoundedCornerShape(114.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = color.copy(alpha = 0.5f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            val mappedSweep = (speed / 120f).coerceIn(0f, 1f) * 270f
            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = mappedSweep,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.1f", speed),
                fontSize = 84.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = "km/h",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
