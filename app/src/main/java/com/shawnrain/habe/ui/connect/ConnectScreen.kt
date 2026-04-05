package com.shawnrain.habe.ui.connect

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.shawnrain.habe.ble.ConnectionState
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.ui.navigation.BlurredAlertDialog
import com.shawnrain.habe.ui.navigation.BlurredModalBottomSheet
import com.shawnrain.habe.ui.navigation.PredictiveBackPopupTransform
import com.shawnrain.habe.ui.theme.bezierPillShape
import com.shawnrain.habe.ui.theme.bezierRoundedShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class ConnectListItem(
    val name: String,
    val address: String,
    val device: BluetoothDevice?,
    val isRemembered: Boolean,
    val isConnected: Boolean
)

@SuppressLint("MissingPermission")
private fun BluetoothDevice.safeNameOrNull(): String? {
    return runCatching { name }.getOrNull()
}

private fun buildConnectItems(
    scannedDevices: List<BluetoothDevice>,
    rememberedAddress: String?,
    rememberedName: String?,
    connectedDevice: BluetoothDevice?
): List<ConnectListItem> {
    return buildList {
        val merged = linkedMapOf<String, ConnectListItem>()

        connectedDevice?.let { device ->
            merged[device.address] = ConnectListItem(
                name = device.safeNameOrNull() ?: rememberedName ?: "已连接设备",
                address = device.address,
                device = device,
                isRemembered = device.address == rememberedAddress,
                isConnected = true
            )
        }

        rememberedAddress?.let { address ->
            val existing = merged[address]
            merged[address] = ConnectListItem(
                name = rememberedName ?: existing?.name ?: "已保存设备",
                address = address,
                device = existing?.device ?: scannedDevices.firstOrNull { it.address == address },
                isRemembered = true,
                isConnected = existing?.isConnected == true
            )
        }

        scannedDevices.forEach { device ->
            val existing = merged[device.address]
            merged[device.address] = ConnectListItem(
                name = device.safeNameOrNull() ?: existing?.name ?: "未知设备",
                address = device.address,
                device = device,
                isRemembered = existing?.isRemembered == true,
                isConnected = existing?.isConnected == true
            )
        }

        addAll(merged.values)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ConnectScreen(viewModel: MainViewModel, onNavigateToDashboard: () -> Unit, modifier: Modifier = Modifier) {
    val scannedDevices by viewModel.filteredDevices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val hasSearched by viewModel.hasSearched.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val rememberedAddress by viewModel.lastControllerDeviceAddress.collectAsState()
    val rememberedName by viewModel.lastControllerDeviceName.collectAsState()
    var deviceToConnect by remember { mutableStateOf<BluetoothDevice?>(null) }
    val connectedDevice = (connectionState as? ConnectionState.Connected)?.device
    val deviceItems = remember(scannedDevices, rememberedAddress, rememberedName, connectedDevice) {
        buildConnectItems(scannedDevices, rememberedAddress, rememberedName, connectedDevice)
    }
    val shouldShowDeviceList = hasSearched || deviceItems.isNotEmpty()

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    val topPadding by animateDpAsState(
        targetValue = if (shouldShowDeviceList) 24.dp else 200.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
    )

    if (deviceToConnect != null) {
        BlurredAlertDialog(
            onDismissRequest = { deviceToConnect = null },
            title = { Text("选择连接方式") },
            text = { Text("将这个设备连接为主控制器，还是作为独立 BMS 使用？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.stopScan()
                    viewModel.connect(deviceToConnect!!)
                    deviceToConnect = null
                    onNavigateToDashboard()
                }) {
                    Text("主控制器")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.stopScan()
                    viewModel.connectBms(deviceToConnect!!)
                    deviceToConnect = null
                    onNavigateToDashboard()
                }) {
                    Text("独立 BMS")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = topPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "连接设备",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (permissionState.allPermissionsGranted) {
                    if (isScanning) {
                        viewModel.stopScan()
                    } else {
                        viewModel.startScan()
                    }
                } else {
                    permissionState.launchMultiplePermissionRequest()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = bezierPillShape()
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("搜索附近设备", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(visible = shouldShowDeviceList, enter = fadeIn()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp)
                    .border(BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant), bezierRoundedShape(24.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (deviceItems.isEmpty() && isScanning) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在扫描附近设备...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else if (deviceItems.isEmpty() && !isScanning) {
                    Text("未发现可连接设备", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(deviceItems, key = { it.address }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(bezierRoundedShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        when {
                                            item.isConnected -> viewModel.disconnect()
                                            item.device != null -> deviceToConnect = item.device
                                            item.isRemembered -> {
                                                viewModel.stopScan()
                                                viewModel.connectRememberedController()
                                                onNavigateToDashboard()
                                            }
                                        }
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (item.isRemembered) {
                                        Text(
                                            text = if (item.isConnected) "已连接设备" else "已保存设备",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = item.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (item.isRemembered) {
                                        OutlinedButton(
                                            onClick = { viewModel.forgetRememberedController() },
                                            shape = bezierPillShape(),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.55f))
                                        ) {
                                            Text("忘记")
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            when {
                                                item.isConnected -> viewModel.disconnect()
                                                item.device != null -> deviceToConnect = item.device
                                                item.isRemembered -> {
                                                    viewModel.stopScan()
                                                    viewModel.connectRememberedController()
                                                    onNavigateToDashboard()
                                                }
                                            }
                                        },
                                        shape = bezierPillShape(),
                                        colors = if (item.isConnected) {
                                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        } else {
                                            ButtonDefaults.buttonColors()
                                        }
                                    ) {
                                        Text(if (item.isConnected) "断开" else "连接")
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

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConnectionQuickSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val scannedDevices by viewModel.filteredDevices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val rememberedAddress by viewModel.lastControllerDeviceAddress.collectAsState()
    val rememberedName by viewModel.lastControllerDeviceName.collectAsState()
    val connectedDevice = (connectionState as? ConnectionState.Connected)?.device
    val deviceItems = remember(scannedDevices, rememberedAddress, rememberedName, connectedDevice) {
        buildConnectItems(scannedDevices, rememberedAddress, rememberedName, connectedDevice)
    }
    val nearbyDeviceItems = remember(deviceItems, rememberedAddress) {
        deviceItems.filterNot { item ->
            rememberedAddress != null && item.address == rememberedAddress
        }
    }
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)
    val enterDurationMs = 300
    val exitDurationMs = 220
    var isSheetVisible by remember { mutableStateOf(false) }
    var dismissInFlight by remember { mutableStateOf(false) }
    val entryProgress by animateFloatAsState(
        targetValue = if (isSheetVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isSheetVisible) enterDurationMs else exitDurationMs,
            easing = if (isSheetVisible) FastOutSlowInEasing else FastOutLinearInEasing
        ),
        label = "ConnectionQuickSheetEntry"
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

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopScan()
        }
    }

    LaunchedEffect(Unit) {
        isSheetVisible = true
    }

    BlurredModalBottomSheet(onDismissRequest = ::requestDismiss) {
        PredictiveBackPopupTransform(
            onBack = ::requestDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            val entryTravelPx = with(density) { (1f - entryProgress) * 56.dp.toPx() }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = entryProgress
                        translationY = entryTravelPx
                        scaleX = 0.98f + (0.02f * entryProgress)
                        scaleY = 0.98f + (0.02f * entryProgress)
                    }
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("连接控制器", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Button(
                    onClick = {
                        if (permissionState.allPermissionsGranted) {
                            if (isScanning) viewModel.stopScan() else viewModel.startScan()
                        } else {
                            permissionState.launchMultiplePermissionRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = bezierPillShape()
                ) {
                    Text(if (isScanning) "停止扫描" else "扫描附近设备")
                }
                rememberedAddress?.let { address ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = bezierRoundedShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    rememberedName ?: "上次连接的控制器",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (connectedDevice?.address == address) {
                                OutlinedButton(onClick = { viewModel.disconnect() }, shape = bezierPillShape()) {
                                    Text("断开")
                                }
                            } else {
                                FilledTonalButton(onClick = { viewModel.connectRememberedController() }, shape = bezierPillShape()) {
                                    Text("连接")
                                }
                            }
                        }
                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 420.dp),
                    shape = bezierRoundedShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                ) {
                    if (nearbyDeviceItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isScanning) "正在扫描附近设备..." else "暂无可连接设备",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(nearbyDeviceItems, key = { it.address }) { item ->
                                DeviceCard(
                                    item = item,
                                    onClick = {
                                        item.device?.let { device ->
                                            viewModel.stopScan()
                                            viewModel.connect(device)
                                            requestDismiss()
                                        }
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
private fun DeviceCard(
    item: ConnectListItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = bezierRoundedShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                if (item.isRemembered) {
                    Text(
                        text = if (item.isConnected) "已连接设备" else "已保存设备",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = item.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(
                onClick = onClick,
                shape = bezierPillShape()
            ) {
                Text(if (item.isConnected) "已连接" else "连接")
            }
        }
    }
}
