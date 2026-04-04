package com.shawnrain.habe.ui.settings

import android.content.Intent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.data.DataSource
import com.shawnrain.habe.data.SpeedSource
import com.shawnrain.habe.data.VehicleProfile
import com.shawnrain.habe.data.WheelPresets
import com.shawnrain.habe.debug.AppLogLevel
import com.shawnrain.habe.ui.navigation.ApplyDialogWindowBlurEffect
import com.shawnrain.habe.ui.navigation.BlurredAlertDialog
import com.shawnrain.habe.ui.navigation.PopupBackdropBlurLayer
import com.shawnrain.habe.ui.navigation.rememberPredictiveBackMotion
import com.shawnrain.habe.ui.theme.bezierPillShape
import com.shawnrain.habe.ui.theme.bezierRoundedShape
import androidx.compose.runtime.saveable.rememberSaveable
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel, 
    onNavigateToBms: () -> Unit, 
    onNavigateToZhikeSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val speedSource by viewModel.speedSource.collectAsState()
    val battDataSource by viewModel.battDataSource.collectAsState()
    val logLevel by viewModel.logLevel.collectAsState()
    val overlayEnabled by viewModel.overlayEnabled.collectAsState()
    val vehicleProfiles by viewModel.vehicleProfiles.collectAsState()
    val currentVehicle by viewModel.currentVehicle.collectAsState()
    val gpsCalibrationState by viewModel.gpsCalibrationState.collectAsState()

    var expandSpeedSource by remember { mutableStateOf(false) }
    var expandBattSource by remember { mutableStateOf(false) }
    var expandLogLevel by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<VehicleProfile?>(null) }
    var creatingVehicle by remember { mutableStateOf(false) }
    var deletingVehicle by remember { mutableStateOf<VehicleProfile?>(null) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(20.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("车辆档案", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "按车辆隔离行程记录和最近连接控制器，车辆参数统一放在档案详情里维护。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                currentVehicle.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "${formatBatterySpec(currentVehicle)} · 累计 ${formatMileage(currentVehicle.totalMileageKm)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                currentVehicle.macAddress.takeIf { it.isNotBlank() }?.let { "控制器 $it" }
                                    ?: "当前还未绑定最近连接控制器",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f)
                            )
                            if (currentVehicle.learnedInternalResistanceOhm > 0f) {
                                Text(
                                    "学习内阻 ${formatResistance(currentVehicle.learnedInternalResistanceOhm)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        vehicleProfiles.forEach { vehicle ->
                            FilterChip(
                                selected = vehicle.id == currentVehicle.id,
                                onClick = { viewModel.selectVehicle(vehicle.id) },
                                label = { Text(vehicle.name) }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilledTonalButton(
                            onClick = { creatingVehicle = true },
                            modifier = Modifier.weight(1f),
                            shape = bezierPillShape()
                        ) {
                            Text("新增车辆")
                        }
                        OutlinedButton(
                            onClick = { editingVehicle = currentVehicle },
                            modifier = Modifier.weight(1f),
                            shape = bezierPillShape()
                        ) {
                            Text("编辑当前")
                        }
                    }

                    TextButton(
                        onClick = { deletingVehicle = currentVehicle },
                        enabled = vehicleProfiles.size > 1,
                        shape = bezierPillShape()
                    ) {
                        Text(if (vehicleProfiles.size > 1) "删除当前车辆" else "至少保留 1 台车辆")
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("GPS 轮径校准", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "保留在设置一级界面。保存时会直接应用到当前活跃车辆：${currentVehicle.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        gpsCalibrationState.hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricSummaryChip(
                            title = "GPS",
                            value = "${formatWheelInput(gpsCalibrationState.gpsDistanceMeters)} m",
                            modifier = Modifier.weight(1f)
                        )
                        MetricSummaryChip(
                            title = "控制器",
                            value = "${formatWheelInput(gpsCalibrationState.controllerDistanceMeters)} m",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricSummaryChip(
                            title = "当前建议",
                            value = gpsCalibrationState.suggestedCircumferenceMm?.let { "${formatWheelInput(it)} mm" } ?: "--",
                            modifier = Modifier.weight(1f)
                        )
                        MetricSummaryChip(
                            title = "偏差",
                            value = gpsCalibrationState.deltaPercent?.let { String.format(Locale.getDefault(), "%.1f%%", it) } ?: "--",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilledTonalButton(
                            onClick = {
                                if (gpsCalibrationState.isRunning) viewModel.stopGpsCalibration() else viewModel.startGpsCalibration()
                            },
                            modifier = Modifier.weight(1f),
                            shape = bezierPillShape()
                        ) {
                            Text(if (gpsCalibrationState.isRunning) "停止校准" else "开始校准")
                        }
                        Button(
                            onClick = { viewModel.applyGpsCalibrationSuggestion() },
                            enabled = gpsCalibrationState.suggestedCircumferenceMm != null,
                            modifier = Modifier.weight(1f),
                            shape = bezierPillShape()
                        ) {
                            Text("应用到当前车辆")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToBms() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = "BMS", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("电池信息与 BMS", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "Enter", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            val activeProtocolId by viewModel.activeProtocolId.collectAsState()

            if (activeProtocolId == "zhike") {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToZhikeSettings() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = "Zhike", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("智科控制器参数设置", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Enter", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("数据源优先级", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expandSpeedSource,
                onExpandedChange = { expandSpeedSource = it }
            ) {
                OutlinedTextField(
                    value = speedSource.title,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("速度数据源") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandSpeedSource) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = expandSpeedSource, onDismissRequest = { expandSpeedSource = false }) {
                    SpeedSource.entries.forEach { src ->
                        DropdownMenuItem(
                            text = { Text(src.title) },
                            onClick = {
                                viewModel.saveSpeedSource(src)
                                expandSpeedSource = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expandBattSource,
                onExpandedChange = { expandBattSource = it }
            ) {
                OutlinedTextField(
                    value = battDataSource.title,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("电池数据来源") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandBattSource) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = expandBattSource, onDismissRequest = { expandBattSource = false }) {
                    DataSource.entries.forEach { src ->
                        DropdownMenuItem(
                            text = { Text(src.title) },
                            onClick = {
                                viewModel.saveBattDataSource(src)
                                expandBattSource = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("悬浮仪表", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "切到后台后自动进入系统画中画，显示速度、功率、平均能耗三个核心指标。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("后台自动进入画中画", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "无需额外悬浮权限，点击小窗即可回到应用。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = overlayEnabled,
                            onCheckedChange = { enabled -> viewModel.saveOverlayEnabled(enabled) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("调试日志", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "可调日志级别，并导出或分享 BLE 连接、协议识别、指令发送与通知回包日志。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandLogLevel,
                        onExpandedChange = { expandLogLevel = it }
                    ) {
                        OutlinedTextField(
                            value = "${logLevel.name} · ${logLevel.label}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("日志级别") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandLogLevel) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = expandLogLevel, onDismissRequest = { expandLogLevel = false }) {
                            AppLogLevel.entries.forEach { level ->
                                DropdownMenuItem(
                                    text = { Text("${level.name} · ${level.label}") },
                                    onClick = {
                                        viewModel.saveLogLevel(level)
                                        expandLogLevel = false
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.exportLogs() },
                            modifier = Modifier.weight(1f),
                            shape = bezierPillShape()
                        ) {
                            Text("导出日志")
                        }
                        Button(
                            onClick = {
                                context.startActivity(viewModel.createLogShareIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            },
                            modifier = Modifier.weight(1f),
                            shape = bezierPillShape()
                        ) {
                            Text("分享日志")
                        }
                    }

                    TextButton(
                        onClick = { viewModel.clearLogs() },
                        shape = bezierPillShape()
                    ) {
                        Text("清空当前日志")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("协议识别", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "连接后会自动匹配智科、远驱、凌博、APT 等解析器。导出的日志中也会包含协议识别、写特征选择和智科指令收发记录。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (creatingVehicle || editingVehicle != null) {
        VehicleEditorDialog(
            initialVehicle = editingVehicle,
            fallbackVehicle = currentVehicle,
            onDismiss = {
                creatingVehicle = false
                editingVehicle = null
            },
            onConfirm = { profile ->
                viewModel.saveVehicleProfile(profile)
                creatingVehicle = false
                editingVehicle = null
            }
        )
    }

    deletingVehicle?.let { vehicle ->
        BlurredAlertDialog(
            onDismissRequest = { deletingVehicle = null },
            title = { Text("删除车辆档案") },
            text = {
                Text(
                    "将删除“${vehicle.name}”对应的车辆档案，并切换到其他车辆。该车辆下的本地设置与历史记录会继续保留在本地存储中，后续可再补专门清理能力。"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVehicleProfile(vehicle.id)
                        deletingVehicle = null
                    }
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingVehicle = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun MetricSummaryChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleEditorDialog(
    initialVehicle: VehicleProfile?,
    fallbackVehicle: VehicleProfile,
    onDismiss: () -> Unit,
    onConfirm: (VehicleProfile) -> Unit
) {
    val vehicleKey = initialVehicle?.id ?: "new"
    val seedVehicle = initialVehicle ?: fallbackVehicle
    var name by rememberSaveable(vehicleKey) { mutableStateOf(initialVehicle?.name.orEmpty()) }
    var seriesInput by rememberSaveable(vehicleKey) { mutableStateOf(seedVehicle.batterySeries.toString()) }
    var capacityInput by rememberSaveable(vehicleKey) { mutableStateOf(formatCapacityInput(seedVehicle.batteryCapacityAh)) }
    var wheelInput by rememberSaveable(vehicleKey) { mutableStateOf(formatWheelInput(seedVehicle.wheelCircumferenceMm)) }
    var rimSizeInput by rememberSaveable(vehicleKey) { mutableStateOf(seedVehicle.wheelRimSize) }
    var tireSpecInput by rememberSaveable(vehicleKey) { mutableStateOf(seedVehicle.tireSpecLabel) }
    var polePairsInput by rememberSaveable(vehicleKey) { mutableStateOf(seedVehicle.polePairs.toString()) }
    var macAddressInput by rememberSaveable(vehicleKey) { mutableStateOf(initialVehicle?.macAddress.orEmpty()) }
    var expandRimMenu by rememberSaveable(vehicleKey) { mutableStateOf(false) }
    var expandTireMenu by rememberSaveable(vehicleKey) { mutableStateOf(false) }

    val parsedSeries = seriesInput.toIntOrNull()?.coerceIn(1, 32)
    val parsedCapacity = capacityInput.toFloatOrNull()?.coerceAtLeast(1f)
    val parsedWheel = wheelInput.toFloatOrNull()?.takeIf { it in 500f..5000f }
    val parsedPolePairs = polePairsInput.toIntOrNull()?.coerceAtLeast(1)
    val selectedRimSize = remember(rimSizeInput) {
        WheelPresets.canonicalRimSize(rimSizeInput)
    }
    val tirePresets = remember(selectedRimSize) {
        WheelPresets.presetsForRim(selectedRimSize)
    }
    val selectedTirePreset = remember(selectedRimSize, tireSpecInput, parsedWheel) {
        WheelPresets.findPreset(
            rimSize = selectedRimSize,
            label = tireSpecInput,
            circumference = parsedWheel
        )
    }
    val canSave = name.isNotBlank() &&
        parsedSeries != null &&
        parsedCapacity != null &&
        parsedWheel != null &&
        parsedPolePairs != null &&
        rimSizeInput.isNotBlank()
    val formScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val enterDurationMs = 300
    val exitDurationMs = 220
    var isDialogVisible by remember(vehicleKey) { mutableStateOf(false) }
    var dismissInFlight by remember(vehicleKey) { mutableStateOf(false) }
    val entryProgress by animateFloatAsState(
        targetValue = if (isDialogVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isDialogVisible) enterDurationMs else exitDurationMs,
            easing = if (isDialogVisible) FastOutSlowInEasing else FastOutLinearInEasing
        ),
        label = "VehicleEditorEntry"
    )

    fun requestDismiss() {
        if (dismissInFlight) return
        dismissInFlight = true
        isDialogVisible = false
        scope.launch {
            kotlinx.coroutines.delay(exitDurationMs.toLong())
            onDismiss()
        }
    }

    fun requestConfirm(profile: VehicleProfile) {
        if (dismissInFlight) return
        dismissInFlight = true
        isDialogVisible = false
        scope.launch {
            kotlinx.coroutines.delay(exitDurationMs.toLong())
            onConfirm(profile)
        }
    }

    LaunchedEffect(vehicleKey) {
        dismissInFlight = false
        isDialogVisible = true
    }

    Dialog(
        onDismissRequest = ::requestDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ApplyDialogWindowBlurEffect(blurRadius = 28.dp)
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PopupBackdropBlurLayer(
                blurRadius = 28.dp,
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.24f * entryProgress),
                onDismissRequest = ::requestDismiss
            )
            val density = LocalDensity.current
            val motion = rememberPredictiveBackMotion(
                width = maxWidth,
                onBack = ::requestDismiss,
                maxHorizontalInset = 10.dp,
                maxVerticalInset = 8.dp,
                maxCorner = 16.dp,
                maxScaleTravelFraction = 0.1f
            )
            val scale = 1f - (0.05f * motion.progress)
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .widthIn(max = 420.dp)
                    .heightIn(max = 560.dp)
                    .padding(horizontal = motion.insetHorizontal, vertical = motion.insetVertical)
                    .graphicsLayer {
                        translationX = motion.translationX
                        translationY = with(density) { (1f - entryProgress) * 28.dp.toPx() } + motion.insetVertical.toPx()
                        scaleX = scale * (0.95f + 0.05f * entryProgress)
                        scaleY = scale * (0.95f + 0.05f * entryProgress)
                        alpha = motion.alpha * entryProgress
                    },
                shape = bezierRoundedShape(28.dp + motion.corner),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (initialVehicle == null) "新增车辆档案" else "编辑车辆档案",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(formScrollState),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("车辆名称") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = seriesInput,
                            onValueChange = { input ->
                                seriesInput = input.filter { it.isDigit() }
                            },
                            label = { Text("电池串数 (S)") },
                            supportingText = { Text("例如 13S、20S") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = capacityInput,
                            onValueChange = { input ->
                                capacityInput = sanitizeDecimalInput(input)
                            },
                            label = { Text("电池容量 (Ah)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = wheelInput,
                            onValueChange = { input ->
                                wheelInput = sanitizeDecimalInput(input)
                            },
                            label = { Text("轮径周长 (mm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ExposedDropdownMenuBox(
                            expanded = expandRimMenu,
                            onExpandedChange = { expandRimMenu = it }
                        ) {
                            OutlinedTextField(
                                value = selectedRimSize,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("轮圈大小") },
                                supportingText = { Text("从预设轮圈中选择") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandRimMenu) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = expandRimMenu,
                                onDismissRequest = { expandRimMenu = false }
                            ) {
                                WheelPresets.rimOptions.forEach { rimSize ->
                                    DropdownMenuItem(
                                        text = { Text(rimSize) },
                                        onClick = {
                                            rimSizeInput = rimSize
                                            val currentPreset = WheelPresets.findPreset(
                                                rimSize = rimSize,
                                                label = tireSpecInput,
                                                circumference = parsedWheel
                                            )
                                            if (currentPreset == null) {
                                                tireSpecInput = ""
                                            }
                                            expandRimMenu = false
                                            expandTireMenu = false
                                        }
                                    )
                                }
                            }
                        }
                        ExposedDropdownMenuBox(
                            expanded = expandTireMenu,
                            onExpandedChange = { expandTireMenu = it }
                        ) {
                            OutlinedTextField(
                                value = selectedTirePreset?.let { preset ->
                                    "${preset.label.removePrefix("${preset.rimSize} ")} · ${formatWheelInput(preset.circumference)} mm"
                                } ?: "选择 $selectedRimSize 轮胎规格",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("轮胎规格") },
                                supportingText = { Text("从对应轮圈的预设规格中选择") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandTireMenu) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                modifier = Modifier.heightIn(max = 320.dp),
                                expanded = expandTireMenu,
                                onDismissRequest = { expandTireMenu = false }
                            ) {
                                tirePresets.forEach { preset ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(preset.label.removePrefix("${preset.rimSize} "))
                                                Text(
                                                    "${formatWheelInput(preset.circumference)} mm · ${preset.group}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        },
                                        onClick = {
                                            tireSpecInput = preset.label
                                            wheelInput = formatWheelInput(preset.circumference)
                                            expandTireMenu = false
                                        }
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = polePairsInput,
                            onValueChange = { input ->
                                polePairsInput = input.filter { it.isDigit() }
                            },
                            label = { Text("电机极对数") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = macAddressInput,
                            onValueChange = { macAddressInput = it.trim().uppercase(Locale.getDefault()) },
                            label = { Text("控制器蓝牙地址") },
                            supportingText = { Text("可先留空；连接控制器后会自动回填最近地址") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = ::requestDismiss) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = {
                                val profile = initialVehicle?.copy(
                                    name = name.trim(),
                                    macAddress = macAddressInput.trim(),
                                    batterySeries = parsedSeries ?: 13,
                                    batteryCapacityAh = parsedCapacity ?: 50f,
                                    wheelCircumferenceMm = parsedWheel ?: 1800f,
                                    wheelRimSize = selectedRimSize,
                                    tireSpecLabel = selectedTirePreset?.label ?: tireSpecInput.trim(),
                                    polePairs = parsedPolePairs ?: 50
                                ) ?: VehicleProfile.create(
                                    name = name.trim(),
                                    macAddress = macAddressInput.trim(),
                                    batterySeries = parsedSeries ?: 13,
                                    batteryCapacityAh = parsedCapacity ?: 50f,
                                    wheelCircumferenceMm = parsedWheel ?: 1800f,
                                    wheelRimSize = selectedRimSize,
                                    tireSpecLabel = selectedTirePreset?.label ?: tireSpecInput.trim(),
                                    polePairs = parsedPolePairs ?: 50
                                )
                                requestConfirm(profile)
                            },
                            enabled = canSave
                        ) {
                            Text("保存")
                        }
                    }
                }
            }
        }
    }
}

private fun sanitizeDecimalInput(raw: String): String {
    val filtered = buildString {
        var dotSeen = false
        raw.forEach { char ->
            when {
                char.isDigit() -> append(char)
                char == '.' && !dotSeen -> {
                    append(char)
                    dotSeen = true
                }
            }
        }
    }
    return filtered
}

private fun formatBatterySpec(vehicle: VehicleProfile): String {
    return "${vehicle.batterySeries}S · ${formatCapacityInput(vehicle.batteryCapacityAh)}Ah"
}

private fun formatMileage(valueKm: Float): String {
    return String.format(Locale.getDefault(), "%.1f km", valueKm)
}

private fun formatResistance(valueOhm: Float): String {
    return String.format(Locale.getDefault(), "%.4f Ω", valueOhm)
}

private fun formatCapacityInput(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", value)
    }
}

private fun formatWheelInput(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format("%.1f", value)
    }
}
