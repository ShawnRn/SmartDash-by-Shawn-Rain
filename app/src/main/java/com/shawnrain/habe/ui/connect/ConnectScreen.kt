package com.shawnrain.habe.ui.connect

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.shawnrain.habe.MainViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ConnectScreen(viewModel: MainViewModel, onNavigateToDashboard: () -> Unit, modifier: Modifier = Modifier) {
    val scannedDevices by viewModel.filteredDevices.collectAsState()
    var isScanning by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var deviceToConnect by remember { mutableStateOf<BluetoothDevice?>(null) }

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
        targetValue = if (hasSearched) 24.dp else 200.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
    )

    if (deviceToConnect != null) {
        AlertDialog(
            onDismissRequest = { deviceToConnect = null },
            title = { Text("选择设备角色") },
            text = { Text("您希望将此设备作为主驱动控制器还是独立的 BMS 板连接？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.stopScan()
                    isScanning = false
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
                    isScanning = false
                    viewModel.connectBms(deviceToConnect!!)
                    deviceToConnect = null
                    onNavigateToDashboard()
                }) {
                    Text("辅 BMS")
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
                    hasSearched = true
                    if (isScanning) {
                        viewModel.stopScan()
                        isScanning = false
                    } else {
                        viewModel.startScan()
                        isScanning = true
                    }
                } else {
                    permissionState.launchMultiplePermissionRequest()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (isScanning) "停止搜索" else "搜索附近的控制器", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(visible = hasSearched, enter = fadeIn()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp)
                    .border(BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (scannedDevices.isEmpty() && isScanning) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在雷达扫描...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else if (scannedDevices.isEmpty() && !isScanning) {
                    Text("未发现可用控制器阵列", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(scannedDevices) { device ->
                            @SuppressLint("MissingPermission")
                            val deviceName = device.name ?: "Unknown Device"
                            val deviceAddress = device.address

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        deviceToConnect = device
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = deviceName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = deviceAddress,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(onClick = { 
                                    deviceToConnect = device
                                }, shape = RoundedCornerShape(8.dp)) {
                                    Text("连接")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
