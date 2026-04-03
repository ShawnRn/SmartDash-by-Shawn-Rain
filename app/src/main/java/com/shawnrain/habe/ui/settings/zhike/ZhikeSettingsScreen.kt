package com.shawnrain.habe.ui.settings.zhike

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.ble.protocols.ZhikeSettings
import com.shawnrain.habe.ui.theme.bezierPillShape
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZhikeSettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    var settings by remember { mutableStateOf(ZhikeSettings()) }
    var isDirty by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("8888") }
    var isSyncing by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val syncMessage by viewModel.calibrationMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.zhikeSettings.collect {
            settings = it
            isDirty = false
            isSyncing = false
        }
    }

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearCalibrationMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "智科控制器调校",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authError = null
                        viewModel.readZhikeSettings()
                        isSyncing = true
                    }) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isDirty) {
                Surface(tonalElevation = 8.dp) {
                    Button(
                        onClick = { showAuthDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = bezierPillShape()
                    ) {
                        Text("同步至控制器")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard()

            // --- Power Section ---
            SettingsSection("动力与电压") {
                NumberSettingItem("母线电流限制 (A)", settings.busCurrent) { 
                    settings = settings.copy(busCurrent = it); isDirty = true 
                }
                NumberSettingItem("相线电流限制 (A)", settings.phaseCurrent) { 
                    settings = settings.copy(phaseCurrent = it); isDirty = true 
                }
                NumberSettingItem("欠压保护 (V)", settings.underVoltage) { 
                    settings = settings.copy(underVoltage = it); isDirty = true 
                }
                NumberSettingItem("过压保护 (V)", settings.overVoltage) { 
                    settings = settings.copy(overVoltage = it); isDirty = true 
                }
            }

            // --- Control Section ---
            SettingsSection("控制参数") {
                SwitchSettingItem("电机反向", settings.motorDirection) {
                    settings = settings.copy(motorDirection = it); isDirty = true
                }
                NumberSettingItem("电机极对数", settings.polePairs) {
                    settings = settings.copy(polePairs = it); isDirty = true
                }
                NumberSettingItem("相位偏移角 (°)", settings.phaseShiftAngle) {
                    settings = settings.copy(phaseShiftAngle = it); isDirty = true
                }
            }

            // --- Advanced Section ---
            SettingsSection("高级优化") {
                NumberSettingItem("弱磁补偿电流", settings.weakMagCurrent) {
                    settings = settings.copy(weakMagCurrent = it); isDirty = true
                }
                NumberSettingItem("反峰制动电流", settings.regenCurrent) {
                    settings = settings.copy(regenCurrent = it); isDirty = true
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = { Text("安全身份验证") },
            text = {
                Column {
                    Text("写入参数需要智科蓝牙管理密码：", modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it.filter(Char::isDigit).take(4)
                            authError = null
                        },
                        label = { Text("4位蓝牙密码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = authError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        supportingText = {
                            authError?.let { Text(it) }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val error = viewModel.writeZhikeSettings(settings, password)
                        if (error != null) {
                            authError = error
                            return@Button
                        }

                        authError = null
                        showAuthDialog = false
                        isSyncing = true
                    },
                    shape = bezierPillShape()
                ) {
                    Text("确认并写入")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        authError = null
                        showAuthDialog = false
                    },
                    shape = bezierPillShape()
                ) { Text("取消") }
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
fun NumberSettingItem(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > 0) onValueChange(value - 1) }) {
                Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Text(value.toString(), modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
            IconButton(onClick = { onValueChange(value + 1) }) {
                Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun SwitchSettingItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun InfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "请在断开 P 挡且静止状态下修改，不正确的电流或相位角设置可能导致电机啸叫或损坏控制器。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
