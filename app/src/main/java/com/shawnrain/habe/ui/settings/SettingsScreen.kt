package com.shawnrain.habe.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.data.SpeedSource
import com.shawnrain.habe.data.DataSource
import com.shawnrain.habe.debug.AppLogLevel
import com.shawnrain.habe.overlay.OverlayHudController
import com.shawnrain.habe.ui.theme.bezierPillShape
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel, 
    onNavigateToBms: () -> Unit, 
    onNavigateToZhikeSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val wheelCircumference by viewModel.wheelCircumference.collectAsState()
    val polePairs by viewModel.polePairs.collectAsState()
    val speedSource by viewModel.speedSource.collectAsState()
    val battDataSource by viewModel.battDataSource.collectAsState()
    val gpsCalibrationState by viewModel.gpsCalibrationState.collectAsState()
    val logLevel by viewModel.logLevel.collectAsState()
    val overlayEnabled by viewModel.overlayEnabled.collectAsState()
    val syncMessage by viewModel.calibrationMessage.collectAsState()

    var wheelInput by remember(wheelCircumference) { mutableStateOf(wheelCircumference.toString()) }
    var poleInput by remember(polePairs) { mutableStateOf(polePairs.toString()) }

    var expandSpeedSource by remember { mutableStateOf(false) }
    var expandBattSource by remember { mutableStateOf(false) }
    var expandLogLevel by remember { mutableStateOf(false) }
    var canDrawOverlay by remember { mutableStateOf(OverlayHudController.canDrawOverlays(context)) }
    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canDrawOverlay = OverlayHudController.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearCalibrationMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            Spacer(modifier = Modifier.height(32.dp))

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
                        Text("电池详情与 BMS 面板", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
                            Text("智科控制器参数调校", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Enter", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("数据源优先仲裁", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
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
                    label = { Text("电池与系统数据源") },
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

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = wheelInput,
                onValueChange = {
                    wheelInput = it
                    it.toFloatOrNull()?.let { value -> viewModel.saveWheelCircumference(value) }
                },
                label = { Text("轮径周长 (mm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = poleInput,
                onValueChange = {
                    poleInput = it
                    it.toIntOrNull()?.let { value -> viewModel.savePolePairs(value) }
                },
                label = { Text("电机极对数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("GPS 速度校准", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "参考小程序逻辑，用 GPS 对照电机转速推算轮径周长",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            shape = bezierPillShape(),
                            label = { Text(if (gpsCalibrationState.isRunning) "校准中" else "未开始") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        gpsCalibrationState.hint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    CalibrationStatRow("GPS 距离", "${gpsCalibrationState.gpsDistanceMeters.toInt()} m")
                    CalibrationStatRow("控制器推算距离", "${gpsCalibrationState.controllerDistanceMeters.toInt()} m")
                    CalibrationStatRow("校准时长", formatCalibrationDuration(gpsCalibrationState.durationSec))
                    CalibrationStatRow("GPS 速度 / 控制器速度", "${"%.1f".format(gpsCalibrationState.gpsSpeedKmh)} / ${"%.1f".format(gpsCalibrationState.controllerSpeedKmh)} km/h")
                    CalibrationStatRow("稳定样本", "${gpsCalibrationState.stableSamples} 次")

                    gpsCalibrationState.suggestedCircumferenceMm?.let { suggestion ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("推荐轮径周长", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text(
                                    "${"%.0f".format(suggestion)} mm" + (
                                        gpsCalibrationState.deltaPercent?.let { delta ->
                                            "  (${if (delta >= 0) "+" else ""}${"%.1f".format(delta)}%)"
                                        } ?: ""
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                if (gpsCalibrationState.isRunning) viewModel.stopGpsCalibration() else viewModel.startGpsCalibration()
                            },
                            modifier = Modifier.weight(1f),
                            shape = bezierPillShape()
                        ) {
                            Icon(
                                imageVector = if (gpsCalibrationState.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (gpsCalibrationState.isRunning) "停止校准" else "开始校准")
                        }

                        OutlinedButton(
                            onClick = { viewModel.applyGpsCalibrationSuggestion() },
                            modifier = Modifier.weight(1f),
                            enabled = gpsCalibrationState.suggestedCircumferenceMm != null,
                            shape = bezierPillShape()
                        ) {
                            Text("应用推荐值")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("后台悬浮仪表", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "切到后台后自动显示速度、功率、平均能耗三个核心仪表。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("后台自动打开悬浮窗", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (canDrawOverlay) "悬浮窗权限已开启" else "需要先授权悬浮窗权限",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = overlayEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && !canDrawOverlay) {
                                    context.startActivity(OverlayHudController.createPermissionIntent(context))
                                } else {
                                    viewModel.saveOverlayEnabled(enabled)
                                }
                            }
                        )
                    }
                    if (!canDrawOverlay) {
                        OutlinedButton(
                            onClick = { context.startActivity(OverlayHudController.createPermissionIntent(context)) },
                            shape = bezierPillShape()
                        ) {
                            Text("前往开启悬浮窗权限")
                        }
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

            ListItem(
                headlineContent = { Text("协议识别 (内置解析库)") },
                supportingContent = { Text("连接后将自动分流至 智科/远驱/凌博/APT 等对应的解析器。日志导出已包含协议识别、写特征选择和智科指令收发记录。") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CalibrationStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun formatCalibrationDuration(durationSec: Int): String {
    val total = durationSec.coerceAtLeast(0)
    val minutes = total / 60
    val seconds = total % 60
    return "%d:%02d".format(minutes, seconds)
}
