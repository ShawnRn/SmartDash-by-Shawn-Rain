package com.shawnrain.habe.ui.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.data.SpeedSource
import com.shawnrain.habe.data.DataSource
import com.shawnrain.habe.data.WheelPreset
import com.shawnrain.habe.data.WheelPresets
import com.shawnrain.habe.debug.AppLogLevel
import com.shawnrain.habe.overlay.OverlayHudController
import com.shawnrain.habe.ui.theme.bezierPillShape
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel, 
    onNavigateToBms: () -> Unit, 
    onNavigateToZhikeSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val wheelCircumference by viewModel.wheelCircumference.collectAsState()
    val polePairs by viewModel.polePairs.collectAsState()
    val speedSource by viewModel.speedSource.collectAsState()
    val battDataSource by viewModel.battDataSource.collectAsState()
    val gpsCalibrationState by viewModel.gpsCalibrationState.collectAsState()
    val logLevel by viewModel.logLevel.collectAsState()
    val overlayEnabled by viewModel.overlayEnabled.collectAsState()
    val syncMessage by viewModel.calibrationMessage.collectAsState()

    var wheelInput by remember(wheelCircumference) { mutableStateOf(formatWheelInput(wheelCircumference)) }
    var poleInput by remember(polePairs) { mutableStateOf(polePairs.toString()) }

    var expandSpeedSource by remember { mutableStateOf(false) }
    var expandBattSource by remember { mutableStateOf(false) }
    var expandLogLevel by remember { mutableStateOf(false) }
    var expandWheelRim by remember { mutableStateOf(false) }
    var expandWheelPreset by remember { mutableStateOf(false) }
    var canDrawOverlay by remember { mutableStateOf(OverlayHudController.canDrawOverlays(context)) }
    val snackbarHostState = remember { SnackbarHostState() }
    val inferredWheelPreset = remember(wheelCircumference) {
        WheelPresets.common.firstOrNull { kotlin.math.abs(it.circumference - wheelCircumference) < 2f }
    }
    var selectedWheelPresetLabel by rememberSaveable { mutableStateOf(inferredWheelPreset?.label) }
    val selectedWheelPreset = remember(wheelCircumference, selectedWheelPresetLabel) {
        val explicit = selectedWheelPresetLabel?.let { label ->
            WheelPresets.common.firstOrNull { it.label == label }
        }
        when {
            explicit != null && kotlin.math.abs(explicit.circumference - wheelCircumference) < 2f -> explicit
            else -> inferredWheelPreset
        }
    }
    var selectedWheelRim by rememberSaveable { mutableStateOf(selectedWheelPreset?.rimSize ?: "10寸") }
    LaunchedEffect(selectedWheelPreset?.rimSize, wheelCircumference) {
        selectedWheelPreset?.rimSize?.let { selectedWheelRim = it }
        if (selectedWheelPreset == null) {
            selectedWheelPresetLabel = null
        }
    }
    val filteredWheelPresets = remember(selectedWheelRim) {
        WheelPresets.presetsForRim(selectedWheelRim)
    }
    val featuredPresets = remember(selectedWheelRim) {
        val scopedRecommended = filteredWheelPresets.filter { it.recommended }
        if (scopedRecommended.isNotEmpty()) scopedRecommended else filteredWheelPresets.take(3)
    }

    fun commitWheelInput() {
        val parsed = wheelInput.toFloatOrNull()
        if (parsed != null && parsed in 500f..5000f) {
            viewModel.saveWheelCircumference(parsed)
            wheelInput = formatWheelInput(parsed)
        } else {
            wheelInput = formatWheelInput(wheelCircumference)
        }
    }

    fun applyWheelPreset(preset: WheelPreset) {
        wheelInput = formatWheelInput(preset.circumference)
        selectedWheelRim = preset.rimSize
        selectedWheelPresetLabel = preset.label
        viewModel.saveWheelCircumference(preset.circumference)
        expandWheelPreset = false
        focusManager.clearFocus()
    }

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

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = wheelInput,
                onValueChange = { input ->
                    wheelInput = sanitizeDecimalInput(input)
                    if (input.contains('\n')) {
                        commitWheelInput()
                    }
                },
                label = { Text("轮径周长 (mm)") },
                supportingText = {
                    Text(
                        selectedWheelPreset?.let { "当前选择: ${it.label}" } ?: "推荐先选预设，再用 GPS 校准细调"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        commitWheelInput()
                        focusManager.clearFocus()
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (!it.isFocused) {
                            commitWheelInput()
                        }
                    }
            )
            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expandWheelRim,
                onExpandedChange = { expandWheelRim = it }
            ) {
                OutlinedTextField(
                    value = selectedWheelRim,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("轮圈大小") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandWheelRim) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expandWheelRim,
                    onDismissRequest = { expandWheelRim = false }
                ) {
                    WheelPresets.rimOptions.forEach { rimSize ->
                        DropdownMenuItem(
                            text = { Text(rimSize) },
                            onClick = {
                                selectedWheelRim = rimSize
                                expandWheelRim = false
                                expandWheelPreset = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expandWheelPreset,
                onExpandedChange = { expandWheelPreset = it }
            ) {
                OutlinedTextField(
                    value = selectedWheelPreset
                        ?.takeIf { it.rimSize == selectedWheelRim }
                        ?.let { "${it.label} · ${formatWheelInput(it.circumference)} mm" }
                        ?: "选择 $selectedWheelRim 轮胎规格",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("轮胎规格") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandWheelPreset) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    modifier = Modifier.heightIn(max = 320.dp),
                    expanded = expandWheelPreset,
                    onDismissRequest = { expandWheelPreset = false }
                ) {
                    filteredWheelPresets.forEach { preset ->
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
                            onClick = { applyWheelPreset(preset) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WheelPresets.rimOptions.forEach { rimSize ->
                    FilterChip(
                        selected = rimSize == selectedWheelRim,
                        onClick = {
                            selectedWheelRim = rimSize
                            expandWheelPreset = false
                            focusManager.clearFocus()
                        },
                        label = { Text(rimSize) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                featuredPresets.forEach { preset ->
                    AssistChip(
                        onClick = { applyWheelPreset(preset) },
                        label = { Text(preset.label.removePrefix("${preset.rimSize} ")) }
                    )
                }
            }
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
                                Text("GPS 轮径校准", style = MaterialTheme.typography.titleMedium)
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
                            Text("应用推荐周长")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("悬浮仪表", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
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
                headlineContent = { Text("协议识别") },
                supportingContent = { Text("连接后会自动匹配智科、远驱、凌博、APT 等解析器。导出的日志中也会包含协议识别、写特征选择和智科指令收发记录。") },
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

private fun formatWheelInput(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format("%.1f", value)
    }
}
