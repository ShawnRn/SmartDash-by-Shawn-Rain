package com.shawnrain.habe.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.data.DataSource
import com.shawnrain.habe.data.SpeedSource
import com.shawnrain.habe.data.VehicleProfile
import com.shawnrain.habe.data.WheelPresets
import com.shawnrain.habe.data.migration.LanBackupQrPayload
import com.shawnrain.habe.data.sync.BackupMetadata
import com.shawnrain.habe.data.sync.BackupPreview
import com.shawnrain.habe.data.sync.GoogleDriveAuth
import com.shawnrain.habe.data.sync.SyncState
import com.shawnrain.habe.debug.AppLogLevel
import com.shawnrain.habe.ui.navigation.ApplyDialogWindowBlurEffect
import com.shawnrain.habe.ui.navigation.BlurredAlertDialog
import com.shawnrain.habe.ui.navigation.BlurredModalBottomSheet
import com.shawnrain.habe.ui.navigation.P2PageHeader
import com.shawnrain.habe.ui.navigation.PredictiveBackPopupTransform
import com.shawnrain.habe.ui.navigation.PopupBackdropBlurLayer
import com.shawnrain.habe.ui.navigation.rememberPredictiveBackMotion
import com.shawnrain.habe.ui.theme.bezierPillShape
import com.shawnrain.habe.ui.theme.bezierRoundedShape
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
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
    val lanBackupShare by viewModel.lanBackupShare.collectAsState()
    val lanBackupRestoring by viewModel.lanBackupRestoring.collectAsState()
    val driveSyncState by viewModel.driveSyncState.collectAsState()
    val driveBackupPreview by viewModel.driveBackupPreview.collectAsState()
    val bottomContentPadding = 28.dp
    var stickyDriveSignedInState by remember { mutableStateOf<SyncState.SignedIn?>(null) }
    LaunchedEffect(driveSyncState) {
        val signedIn = driveSyncState as? SyncState.SignedIn
        if (signedIn != null) {
            stickyDriveSignedInState = signedIn
        }
    }

    var expandSpeedSource by remember { mutableStateOf(false) }
    var expandBattSource by remember { mutableStateOf(false) }
    var expandLogLevel by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<VehicleProfile?>(null) }
    var creatingVehicle by remember { mutableStateOf(false) }
    var deletingVehicle by remember { mutableStateOf<VehicleProfile?>(null) }
    var lanRestoreHost by remember { mutableStateOf("") }
    var lanRestorePort by remember { mutableStateOf("") }
    var lanRestoreCode by remember { mutableStateOf("") }
    var showLanQrDialog by remember { mutableStateOf(false) }
    var showLanScannerDialog by remember { mutableStateOf(false) }
    var showDriveSignOutConfirm by remember { mutableStateOf(false) }
    var showDriveHistory by remember { mutableStateOf(false) }
    var selectedDriveHistoryFileId by rememberSaveable { mutableStateOf<String?>(null) }
    val qrPayload = remember(lanBackupShare) { viewModel.currentLanBackupQrPayload() }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showLanScannerDialog = true
        } else {
            viewModel.showLanBackupMessage("未授予相机权限，无法扫码迁移")
        }
    }

    // Google Drive sign-in launcher
    val appContext = LocalContext.current
    val driveSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val account = GoogleDriveAuth(appContext).getCurrentAccount()
            if (account != null) {
                viewModel.completeDriveSignIn()
            } else {
                viewModel.checkDriveSignInStatus()
            }
        } else {
            viewModel.checkDriveSignInStatus()
        }
    }

    // Direct sign-in handler (no LaunchedEffect needed)
    val handleDriveSignIn = {
        val intent = GoogleDriveAuth(appContext).getSignInIntent()
        driveSignInLauncher.launch(intent)
    }

    LaunchedEffect(lanBackupShare.isSharing) {
        if (!lanBackupShare.isSharing) {
            showLanQrDialog = false
        }
    }
    LaunchedEffect(Unit) {
        viewModel.checkDriveSignInStatus()
    }
    val availableBackups = ((driveSyncState as? SyncState.SignedIn) ?: stickyDriveSignedInState)
        ?.availableBackups
        .orEmpty()
    LaunchedEffect(showDriveHistory, availableBackups) {
        if (!showDriveHistory || availableBackups.isEmpty()) return@LaunchedEffect
        val selected = availableBackups.firstOrNull { it.fileId == selectedDriveHistoryFileId }
        val target = selected ?: availableBackups.first()
        if (target.fileId != selectedDriveHistoryFileId || driveBackupPreview.selectedBackup?.fileId != target.fileId) {
            selectedDriveHistoryFileId = target.fileId
            viewModel.previewDriveBackup(target)
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = driveSyncState is SyncState.Syncing,
            onRefresh = { viewModel.syncDriveNow() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                P2PageHeader(
                    title = "设置",
                    subtitle = "当前活跃车辆「${currentVehicle.name}」",
                    modifier = Modifier.fillMaxWidth()
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, end = 24.dp, bottom = bottomContentPadding)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

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
                        "保存后会直接应用到当前活跃车辆：${currentVehicle.name}",
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
                            Text(
                                text = if (gpsCalibrationState.isRunning) "停止校准" else "开始校准",
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Button(
                            onClick = { viewModel.applyGpsCalibrationSuggestion() },
                            enabled = gpsCalibrationState.suggestedCircumferenceMm != null,
                            modifier = Modifier.weight(1f),
                            shape = bezierPillShape()
                        ) {
                            Text(
                                text = "应用",
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
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
                        Text("电池与BMS", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
                            Text("智科调校", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
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

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("换机助手", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "支持同一局域网下设备直接迁移 App 资料。旧设备开启发送，新设备输入地址、端口和配对码后可立即恢复。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AnimatedContent(
                        targetState = lanBackupShare.isSharing,
                        transitionSpec = {
                            (
                                fadeIn(
                                    animationSpec = tween(durationMillis = 240, easing = LinearOutSlowInEasing)
                                ) + slideInVertically(
                                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                                    initialOffsetY = { fullHeight -> fullHeight / 5 }
                                )
                            ).togetherWith(
                                fadeOut(
                                    animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)
                                ) + slideOutVertically(
                                    animationSpec = tween(durationMillis = 220, easing = FastOutLinearInEasing),
                                    targetOffsetY = { fullHeight -> -fullHeight / 6 }
                                )
                            ).using(SizeTransform(clip = false))
                        },
                        label = "LanShareStateSwap"
                    ) { isSharing ->
                        if (isSharing) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            "发送中：${lanBackupShare.host}:${lanBackupShare.port}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            "配对码：${lanBackupShare.code}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showLanQrDialog = true },
                                        shape = bezierPillShape(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.QrCode2, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("显示二维码")
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.stopLanBackupShare() },
                                        shape = bezierPillShape(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("停止发送")
                                    }
                                }
                            }
                        } else {
                            FilledTonalButton(
                                onClick = { viewModel.startLanBackupShare() },
                                shape = bezierPillShape()
                            ) {
                                Text("开启局域网发送")
                            }
                        }
                    }

                    HorizontalDivider()

                    Text("在本设备恢复", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    AnimatedVisibility(
                        visible = lanBackupRestoring,
                        enter = fadeIn(
                            animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing)
                        ) + slideInVertically(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            initialOffsetY = { fullHeight -> fullHeight / 3 }
                        ),
                        exit = fadeOut(
                            animationSpec = tween(durationMillis = 160, easing = FastOutLinearInEasing)
                        ) + slideOutVertically(
                            animationSpec = tween(durationMillis = 220, easing = FastOutLinearInEasing),
                            targetOffsetY = { fullHeight -> -fullHeight / 4 }
                        )
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.2.dp
                                )
                                Text(
                                    text = "正在从旧设备恢复数据，请保持网络稳定…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                showLanScannerDialog = true
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        shape = bezierPillShape()
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("扫码迁移")
                    }
                    OutlinedTextField(
                        value = lanRestoreHost,
                        onValueChange = { lanRestoreHost = it.trim() },
                        label = { Text("旧设备地址") },
                        supportingText = { Text("示例：192.168.1.23") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lanRestorePort,
                        onValueChange = { lanRestorePort = it.filter(Char::isDigit) },
                        label = { Text("端口") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lanRestoreCode,
                        onValueChange = { lanRestoreCode = it.filter(Char::isDigit).take(6) },
                        label = { Text("配对码") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            viewModel.restoreFromLanBackup(
                                host = lanRestoreHost,
                                portText = lanRestorePort,
                                code = lanRestoreCode
                            )
                        },
                        enabled = !lanBackupRestoring,
                        shape = bezierPillShape()
                    ) {
                        Text("立即恢复")
                    }

                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google Drive Cloud Sync Section
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Google Drive 云同步", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "开启后自动双向同步并智能合并云端/本地数据。通常无需手动上传或恢复。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Auto-reset Synced/Error state back to SignedIn after delay
                    LaunchedEffect(driveSyncState) {
                        when (driveSyncState) {
                            is com.shawnrain.habe.data.sync.SyncState.Synced -> {
                                kotlinx.coroutines.delay(3000)
                                viewModel.checkDriveSignInStatus()
                            }
                            is com.shawnrain.habe.data.sync.SyncState.Error -> {
                                kotlinx.coroutines.delay(5000)
                                viewModel.checkDriveSignInStatus()
                            }
                            else -> {}
                        }
                    }

                    val resolvedDriveState = (driveSyncState as? com.shawnrain.habe.data.sync.SyncState.SignedIn)
                        ?: stickyDriveSignedInState
                    AnimatedContent(
                        targetState = Pair(driveSyncState, resolvedDriveState),
                        transitionSpec = {
                            (fadeIn(tween(250)) + slideInVertically { it / 4 }).togetherWith(
                                fadeOut(tween(200)) + slideOutVertically { -it / 4 }
                            ).using(SizeTransform(clip = false))
                        },
                        label = "DriveSyncStateTransition"
                    ) { (transientState, stickyState) ->
                        when (transientState) {
                            is com.shawnrain.habe.data.sync.SyncState.SignedOut -> {
                                Button(
                                    onClick = handleDriveSignIn,
                                    shape = bezierPillShape()
                                ) {
                                    Text("登录 Google 账号")
                                }
                            }
                            is com.shawnrain.habe.data.sync.SyncState.SigningIn -> {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text("正在跳转登录…", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            else -> {
                                val state = stickyState
                                if (state == null) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        Text("正在载入同步状态…", style = MaterialTheme.typography.bodySmall)
                                    }
                                    return@AnimatedContent
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = { viewModel.syncDriveNow() },
                                            shape = bezierPillShape(),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("同步")
                                        }
                                        Button(
                                            onClick = { showDriveSignOutConfirm = true },
                                            shape = bezierPillShape(),
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        ) {
                                            Text("退出登录")
                                        }
                                    }
                                    if (state.availableBackups.isNotEmpty()) {
                                        OutlinedButton(
                                            onClick = {
                                                showDriveHistory = true
                                                val target = state.availableBackups.first()
                                                selectedDriveHistoryFileId = target.fileId
                                                viewModel.previewDriveBackup(target)
                                            },
                                            shape = bezierPillShape(),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("历史版本")
                                        }
                                    }
                                    Text(
                                        "自动同步已开启。仅在异常情况下手动触发同步。",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    when (transientState) {
                                        is com.shawnrain.habe.data.sync.SyncState.Syncing -> {
                                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                            Text("同步中…", style = MaterialTheme.typography.bodySmall)
                                        }
                                        is com.shawnrain.habe.data.sync.SyncState.Synced -> {
                                            val msg = when {
                                                transientState.uploaded && transientState.downloaded -> "已上传并恢复"
                                                transientState.uploaded -> "上传成功"
                                                transientState.downloaded -> "恢复成功"
                                                else -> "同步完成"
                                            }
                                            Text(
                                                msg,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        is com.shawnrain.habe.data.sync.SyncState.Error -> {
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = MaterialTheme.shapes.medium,
                                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                            ) {
                                                Text(
                                                    transientState.message,
                                                    modifier = Modifier.padding(10.dp),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                        else -> Unit
                                    }
                                    Text(
                                        "已登录：${state.email}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val latest = state.availableBackups.firstOrNull()
                                    if (latest != null) {
                                        Text(
                                            latest.deviceName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    val syncTime = state.lastSyncTime ?: latest?.createdAt
                                    if (syncTime != null) {
                                        Text(
                                            "上次同步：${java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault()).format(syncTime)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (state.availableBackups.isNotEmpty()) {
                                        val displayCount = state.availableBackups.size
                                        Text(
                                            "可用备份：$displayCount 个",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
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
        }
    }

    if (showLanQrDialog && qrPayload != null) {
        LanBackupQrDialog(
            payload = qrPayload,
            onDismiss = { showLanQrDialog = false }
        )
    }
    if (showLanScannerDialog) {
        LanBackupScanDialog(
            onDismiss = { showLanScannerDialog = false },
            onScanResult = { raw ->
                val payload = LanBackupQrPayload.decode(raw)
                if (payload == null) {
                    viewModel.showLanBackupMessage("二维码无效：不是 Habe 换机码")
                } else {
                    showLanScannerDialog = false
                    lanRestoreHost = payload.host
                    lanRestorePort = payload.port.toString()
                    lanRestoreCode = payload.code
                    viewModel.restoreFromLanBackup(
                        host = payload.host,
                        portText = payload.port.toString(),
                        code = payload.code
                    )
                }
            }
        )
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

    if (showDriveSignOutConfirm) {
        BlurredAlertDialog(
            onDismissRequest = { showDriveSignOutConfirm = false },
            title = { Text("退出 Google Drive") },
            text = {
                Text("退出后将停止自动同步，本地数据会保留。需要重新登录后才会继续同步。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.signOutOfDrive()
                        showDriveSignOutConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("确认退出")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDriveSignOutConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
    if (showDriveHistory) {
        BlurredModalBottomSheet(
            onDismissRequest = {
                showDriveHistory = false
                viewModel.clearDriveBackupPreview()
            },
            blurRadius = 34.dp
        ) {
            PredictiveBackPopupTransform(
                onBack = {
                    showDriveHistory = false
                    viewModel.clearDriveBackupPreview()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                DriveHistoryReviewSheet(
                    backups = availableBackups,
                    selectedFileId = selectedDriveHistoryFileId,
                    preview = driveBackupPreview.preview,
                    selectedBackup = driveBackupPreview.selectedBackup,
                    isLoading = driveBackupPreview.isLoading,
                    errorMessage = driveBackupPreview.errorMessage,
                    onSelect = { metadata ->
                        selectedDriveHistoryFileId = metadata.fileId
                        viewModel.previewDriveBackup(metadata)
                    },
                    onMerge = { metadata ->
                        viewModel.downloadAndMergeFromDrive(metadata.fileId)
                        showDriveHistory = false
                        viewModel.clearDriveBackupPreview()
                    },
                    onRestoreRide = { metadata, rideId ->
                        viewModel.restoreSingleRideFromDriveBackup(metadata.fileId, rideId)
                        showDriveHistory = false
                        viewModel.clearDriveBackupPreview()
                    },
                    onDismiss = {
                        showDriveHistory = false
                        viewModel.clearDriveBackupPreview()
                    }
                )
            }
        }
    }
}

@Composable
private fun DriveHistoryReviewSheet(
    backups: List<BackupMetadata>,
    selectedFileId: String?,
    preview: BackupPreview?,
    selectedBackup: BackupMetadata?,
    isLoading: Boolean,
    errorMessage: String?,
    onSelect: (BackupMetadata) -> Unit,
    onMerge: (BackupMetadata) -> Unit,
    onRestoreRide: (BackupMetadata, String) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedIndex = backups.indexOfFirst { it.fileId == selectedFileId }.coerceAtLeast(0)
    val selectedMetadata = selectedBackup ?: backups.getOrNull(selectedIndex)
    val sheetScroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(sheetScroll)
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("历史版本", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "浏览备份快照并恢复需要的内容",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (backups.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = bezierRoundedShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
            ) {
                Text(
                    "暂时还没有可浏览的历史版本。",
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = bezierRoundedShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.14f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                            )
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedContent(
                    targetState = selectedMetadata?.fileId,
                    transitionSpec = {
                        (fadeIn(tween(170)) + slideInVertically { it / 12 }).togetherWith(
                            fadeOut(tween(120)) + slideOutVertically { -it / 14 }
                        ).using(SizeTransform(clip = false))
                    },
                    label = "selected_backup_card"
                ) { _ ->
                    selectedMetadata?.let { metadata ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.16f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            formatBackupTimestamp(metadata.createdAt),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            metadata.deviceName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = bezierPillShape(),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                    ) {
                                        Text(
                                            "当前预览",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Text(
                                    metadata.fileName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {
                            backups.getOrNull((selectedIndex - 1).coerceAtLeast(0))?.let(onSelect)
                        },
                        enabled = selectedIndex > 0,
                        label = { Text("更新") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.ArrowBackIosNew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                    Text(
                        "${selectedIndex + 1} / ${backups.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AssistChip(
                        onClick = {
                            backups.getOrNull((selectedIndex + 1).coerceAtMost(backups.lastIndex))?.let(onSelect)
                        },
                        enabled = selectedIndex < backups.lastIndex,
                        label = { Text("更早") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }

            }
        }

        AnimatedContent(
            targetState = Triple(selectedMetadata?.fileId, isLoading, errorMessage),
            transitionSpec = {
                (fadeIn(tween(170)) + slideInVertically { it / 10 }).togetherWith(
                    fadeOut(tween(120)) + slideOutVertically { -it / 12 }
                ).using(SizeTransform(clip = false))
            },
            label = "backup_preview_content"
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = bezierRoundedShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.16f)
                )
            ) {
                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text("正在读取这个版本的内容…", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("无法预览这个版本", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                            Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    preview != null && selectedMetadata != null -> {
                        BackupPreviewContent(
                            preview = preview,
                            metadata = selectedMetadata,
                            onMerge = { onMerge(selectedMetadata) },
                            onRestoreRide = { rideId -> onRestoreRide(selectedMetadata, rideId) }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { selectedMetadata?.let(onMerge) },
                enabled = selectedMetadata != null && !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = bezierPillShape()
            ) {
                Text("恢复这个版本")
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = bezierPillShape()
            ) {
                Text("关闭")
            }
        }
    }
}

@Composable
private fun BackupPreviewContent(
    preview: BackupPreview,
    metadata: BackupMetadata,
    onMerge: () -> Unit,
    onRestoreRide: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            formatBackupTimestamp(metadata.createdAt),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "设备 ${metadata.deviceName} · 导出于 ${formatBackupTimestamp(preview.exportedAt.takeIf { it > 0L } ?: metadata.createdAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BackupStatCard(
                modifier = Modifier.weight(1f),
                title = "车辆",
                value = preview.vehicleCount.toString(),
                icon = Icons.Default.Settings
            )
            BackupStatCard(
                modifier = Modifier.weight(1f),
                title = "行程",
                value = preview.rideCount.toString(),
                icon = Icons.Default.History
            )
            BackupStatCard(
                modifier = Modifier.weight(1f),
                title = "测速",
                value = preview.speedTestCount.toString(),
                icon = Icons.Default.Speed
            )
        }

        if (preview.vehicles.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("车辆快照", style = MaterialTheme.typography.titleMedium)
                preview.vehicles.take(4).forEach { vehicle ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(vehicle.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${vehicle.rideCount} 条行程",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                String.format(Locale.getDefault(), "%.1f km", vehicle.totalMileageKm),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        if (preview.recentRides.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("最近行程", style = MaterialTheme.typography.titleMedium)
                preview.recentRides.forEach { ride ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    ride.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${ride.vehicleName} · ${formatBackupTimestamp(ride.startedAtMs)} · ${ride.durationMinutes} 分钟",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    String.format(Locale.getDefault(), "%.2f km", ride.distanceKm),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                TextButton(
                                    onClick = { onRestoreRide(ride.id) },
                                    shape = bezierPillShape(),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text("仅恢复这条")
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun BackupStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatBackupTimestamp(timestampMs: Long): String {
    if (timestampMs <= 0L) return "未知时间"
    return java.text.SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(java.util.Date(timestampMs))
}

@Composable
@androidx.annotation.OptIn(markerClass = [ExperimentalGetImage::class])
private fun LanBackupScanDialog(
    onDismiss: () -> Unit,
    onScanResult: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanOptions = remember {
        com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }
    val barcodeScanner = remember(scanOptions) { BarcodeScanning.getClient(scanOptions) }
    val consumed = remember { AtomicBoolean(false) }
    val cameraController = remember(context) {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
        }
    }
    var bindError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val enterDurationMs = 320
    val exitDurationMs = 220
    var isDialogVisible by remember { mutableStateOf(false) }
    var dismissInFlight by remember { mutableStateOf(false) }
    val entryProgress by animateFloatAsState(
        targetValue = if (isDialogVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isDialogVisible) enterDurationMs else exitDurationMs,
            easing = if (isDialogVisible) FastOutSlowInEasing else FastOutLinearInEasing
        ),
        label = "LanBackupScanDialogEntry"
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

    LaunchedEffect(Unit) {
        dismissInFlight = false
        isDialogVisible = true
    }

    DisposableEffect(Unit) {
        runCatching {
            cameraController.bindToLifecycle(lifecycleOwner)
            cameraController.setImageAnalysisAnalyzer(scanExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage == null || consumed.get()) {
                    imageProxy.close()
                    return@setImageAnalysisAnalyzer
                }
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (consumed.get()) return@addOnSuccessListener
                        val raw = barcodes
                            .firstNotNullOfOrNull { it.rawValue?.trim() }
                            ?.takeIf { it.isNotBlank() }
                        if (raw != null && consumed.compareAndSet(false, true)) {
                            onScanResult(raw)
                        }
                    }
                    .addOnFailureListener {
                        // Keep scanning; occasional frame decode failures are expected.
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
            bindError = null
        }.onFailure {
            bindError = "相机绑定失败"
        }
        onDispose {
            runCatching { cameraController.clearImageAnalysisAnalyzer() }
            runCatching { cameraController.unbind() }
            runCatching { barcodeScanner.close() }
            scanExecutor.shutdown()
        }
    }

    Dialog(
        onDismissRequest = ::requestDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        ApplyDialogWindowBlurEffect(blurRadius = 28.dp)
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val motion = rememberPredictiveBackMotion(
                width = maxWidth,
                onBack = ::requestDismiss,
                maxHorizontalInset = 10.dp,
                maxVerticalInset = 8.dp,
                maxCorner = 16.dp,
                maxScaleTravelFraction = 0.1f
            )
            val scale = 1f - (0.045f * motion.progress)
            PopupBackdropBlurLayer(
                blurRadius = 28.dp,
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.24f * entryProgress),
                onDismissRequest = ::requestDismiss
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .padding(horizontal = 24.dp)
                    .padding(horizontal = motion.insetHorizontal, vertical = motion.insetVertical)
                    .graphicsLayer {
                        translationX = motion.translationX
                        translationY = with(density) { (1f - entryProgress) * 24.dp.toPx() } + motion.insetVertical.toPx()
                        scaleX = scale * (0.95f + 0.05f * entryProgress)
                        scaleY = scale * (0.95f + 0.05f * entryProgress)
                        alpha = motion.alpha * entryProgress
                    },
                shape = bezierRoundedShape(28.dp + motion.corner),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("扫码迁移", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "请将旧设备的换机二维码放入取景框内，支持连续自动对焦。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = Color.Black
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4f)
                                .clip(MaterialTheme.shapes.large)
                                .background(Color.Black)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    PreviewView(ctx).apply {
                                        scaleType = PreviewView.ScaleType.FILL_CENTER
                                        // Use TextureView path so preview participates in the same
                                        // transform stack as the dialog card animation.
                                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                        setBackgroundColor(AndroidColor.BLACK)
                                        controller = cameraController
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        // Force a slight crop to hide device-specific letterboxing strips.
                                        scaleX = 1.08f
                                        scaleY = 1.08f
                                    }
                            )
                        }
                    }
                    bindError?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = ::requestDismiss,
                        shape = bezierPillShape(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("取消")
                    }
                }
            }
        }
    }
}

@Composable
private fun LanBackupQrDialog(
    payload: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val enterDurationMs = 320
    val exitDurationMs = 220
    var isDialogVisible by remember(payload) { mutableStateOf(false) }
    var dismissInFlight by remember(payload) { mutableStateOf(false) }
    val entryProgress by animateFloatAsState(
        targetValue = if (isDialogVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isDialogVisible) enterDurationMs else exitDurationMs,
            easing = if (isDialogVisible) FastOutSlowInEasing else FastOutLinearInEasing
        ),
        label = "LanBackupQrDialogEntry"
    )
    val qrBitmap = remember(payload) {
        generateLanBackupQrBitmap(payload = payload, sizePx = 900)
    }

    fun requestDismiss() {
        if (dismissInFlight) return
        dismissInFlight = true
        isDialogVisible = false
        scope.launch {
            kotlinx.coroutines.delay(exitDurationMs.toLong())
            onDismiss()
        }
    }

    LaunchedEffect(payload) {
        dismissInFlight = false
        isDialogVisible = true
    }

    Dialog(
        onDismissRequest = ::requestDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        ApplyDialogWindowBlurEffect(blurRadius = 28.dp)
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val motion = rememberPredictiveBackMotion(
                width = maxWidth,
                onBack = ::requestDismiss,
                maxHorizontalInset = 10.dp,
                maxVerticalInset = 8.dp,
                maxCorner = 16.dp,
                maxScaleTravelFraction = 0.1f
            )
            val scale = 1f - (0.045f * motion.progress)
            PopupBackdropBlurLayer(
                blurRadius = 28.dp,
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.24f * entryProgress),
                onDismissRequest = ::requestDismiss
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .padding(horizontal = 24.dp)
                    .padding(horizontal = motion.insetHorizontal, vertical = motion.insetVertical)
                    .graphicsLayer {
                        translationX = motion.translationX
                        translationY = with(density) { (1f - entryProgress) * 26.dp.toPx() } + motion.insetVertical.toPx()
                        scaleX = scale * (0.94f + 0.06f * entryProgress)
                        scaleY = scale * (0.94f + 0.06f * entryProgress)
                        alpha = motion.alpha * entryProgress
                    },
                shape = bezierRoundedShape(28.dp + motion.corner),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("换机二维码", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "新设备在“换机助手”里点“扫码迁移”即可自动恢复。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (qrBitmap != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "换机二维码",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                            )
                        }
                    } else {
                        Text(
                            "二维码生成失败，请改用手动输入地址、端口、配对码。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(payload))
                            },
                            modifier = Modifier.weight(1f),
                            shape = bezierPillShape()
                        ) {
                            Text("复制字符串")
                        }
                        Button(
                            onClick = ::requestDismiss,
                            modifier = Modifier.weight(1f),
                            shape = bezierPillShape()
                        ) {
                            Text("完成")
                        }
                    }
                }
            }
        }
    }
}

private fun generateLanBackupQrBitmap(payload: String, sizePx: Int): Bitmap? {
    if (payload.isBlank() || sizePx < 120) return null
    return runCatching {
        val hints = mapOf(
            EncodeHintType.MARGIN to 0,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
        )
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bitmap.setPixel(x, y, if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bitmap
    }.getOrNull()
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
