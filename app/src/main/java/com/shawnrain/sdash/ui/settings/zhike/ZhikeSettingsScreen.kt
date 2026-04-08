package com.shawnrain.sdash.ui.settings.zhike

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shawnrain.sdash.MainViewModel
import com.shawnrain.sdash.ble.protocols.WriteFailurePhase
import com.shawnrain.sdash.ble.protocols.ZhikeParamDefinition
import com.shawnrain.sdash.ble.protocols.ZhikeParamGroup
import com.shawnrain.sdash.ble.protocols.ZhikeParamKind
import com.shawnrain.sdash.ble.protocols.ZhikeParameterCatalog
import com.shawnrain.sdash.ble.protocols.ZhikeSettings
import com.shawnrain.sdash.ble.protocols.ZhikeWriteState
import com.shawnrain.sdash.ble.protocols.syncLegacyFieldsFromWords
import com.shawnrain.sdash.ui.navigation.BlurredAlertDialog
import com.shawnrain.sdash.ui.navigation.SecondaryScreenTopBar
import com.shawnrain.sdash.ui.theme.bezierPillShape
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ZhikeSettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    var settings by remember { mutableStateOf(ZhikeSettings()) }
    var isDirty by remember { mutableStateOf(false) }
    var showWriteDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("8888") }
    var isSyncing by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Write state observation
    val writeState by viewModel.zhikeWriteState.collectAsState()
    val validationResult by viewModel.zhikeValidationResult.collectAsState()

    // Dialogs state
    var showValidationDialog by remember { mutableStateOf(false) }
    var showFactoryResetDialog by remember { mutableStateOf(false) }
    var showSafePresetsDialog by remember { mutableStateOf(false) }
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var showExportSuccessDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonContent by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }

    // Password change state
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var passwordChangeError by remember { mutableStateOf<String?>(null) }
    var passwordChangeSuccess by remember { mutableStateOf(false) }

    // Menu state
    var showBottomMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // File picker launcher for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                        reader.readText()
                    }
                    if (content != null) {
                        importJsonContent = content
                        importError = null
                        showImportDialog = true
                    } else {
                        importError = "无法读取文件内容"
                    }
                } catch (e: Exception) {
                    importError = "导入失败: ${e.message}"
                }
            }
        }
    }

    // File saver launcher for export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(it)?.bufferedWriter()?.use { writer ->
                        writer.write(settingsToJson(settings))
                    }
                    showExportSuccessDialog = true
                } catch (e: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar("导出失败: ${e.message}")
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.zhikeSettings.collect {
            settings = it
            isDirty = false
            isSyncing = false
            if (it.loadedFromController) {
                currentPassword = String.format(Locale.getDefault(), "%04d", it.originalBluetoothPassword)
            }
        }
    }

    // Handle write state transitions
    LaunchedEffect(writeState) {
        when (writeState) {
            is ZhikeWriteState.Succeeded -> {
                snackbarHostState.showSnackbar("写入成功")
                delay(2000)
                isDirty = false
            }
            is ZhikeWriteState.Failed -> {
                val failed = writeState as ZhikeWriteState.Failed
                snackbarHostState.showSnackbar("写入失败: ${failed.reason}")
            }
            else -> {}
        }
    }

    val visibleGroups = remember(settings.loadedFromController) {
        ZhikeParameterCatalog.groups
    }

    Scaffold(
        topBar = {
            SecondaryScreenTopBar(
                title = "智科调校",
                subtitle = "控制器参数",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        authError = null
                        isSyncing = true
                        viewModel.readZhikeSettings()
                    }) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.width(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isDirty || true) {
                Surface(tonalElevation = 8.dp) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        // Write step progress indicator
                        if (writeState !is ZhikeWriteState.Idle && writeState !is ZhikeWriteState.Succeeded && writeState !is ZhikeWriteState.Failed) {
                            val steps = listOf(
                                ZhikeWriteState.Handshaking to "1. 握手",
                                ZhikeWriteState.EnteringWriteMode to "2. 写入模式",
                                ZhikeWriteState.ReadyToWrite to "3. 准备",
                                ZhikeWriteState.WritingPackets to "4. 写入",
                                ZhikeWriteState.WaitingWriteAck to "5. 确认",
                                ZhikeWriteState.Verifying to "6. 核对"
                            )
                            val currentStateIndex = steps.indexOfFirst { writeState is ZhikeWriteState && it.first::class == writeState::class }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    steps.forEachIndexed { index, step ->
                                        val isCompleted = currentStateIndex > index
                                        val isCurrent = currentStateIndex == index
                                        val color = when {
                                            isCompleted -> MaterialTheme.colorScheme.primary
                                            isCurrent -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .width(8.dp)
                                                    .height(8.dp)
                                                    .background(
                                                        color = color,
                                                        shape = androidx.compose.foundation.shape.CircleShape
                                                    )
                                            )
                                            Text(
                                                text = step.second,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = color,
                                                fontSize = androidx.compose.ui.unit.TextUnit(6f, androidx.compose.ui.unit.TextUnitType.Sp)
                                            )
                                        }
                                        if (index < steps.lastIndex) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when (writeState) {
                                        is ZhikeWriteState.Handshaking -> "正在握手..."
                                        is ZhikeWriteState.EnteringWriteMode -> "正在进入写入模式..."
                                        is ZhikeWriteState.ReadyToWrite -> "准备写入..."
                                        is ZhikeWriteState.WritingPackets -> "正在写入参数..."
                                        is ZhikeWriteState.WaitingWriteAck -> "等待控制器确认..."
                                        is ZhikeWriteState.Verifying -> "正在回读核对..."
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CircularProgressIndicator(modifier = Modifier.width(24.dp), strokeWidth = 2.dp)
                            }
                        }

                        // Failed state with phase-specific hint
                        if (writeState is ZhikeWriteState.Failed) {
                            val failed = writeState as ZhikeWriteState.Failed
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "❌ 写入失败",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = failed.phase.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = failed.phase.userHint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                if (failed.reason.isNotEmpty()) {
                                    Text(
                                        text = failed.reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isDirty) {
                                Button(
                                    onClick = {
                                        // Check validation result first
                                        val result = validationResult
                                        if (result != null && result.hasBlockingIssues) {
                                            showValidationDialog = true
                                        } else if (result != null && result.hasWarnings) {
                                            showValidationDialog = true
                                        } else {
                                            showWriteDialog = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = bezierPillShape(),
                                    enabled = writeState is ZhikeWriteState.Idle || writeState is ZhikeWriteState.Succeeded
                                ) {
                                    Text("写入控制器")
                                }
                            }

                            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                                IconButton(
                                    onClick = { showBottomMenu = true }
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                                }
                                DropdownMenu(
                                    expanded = showBottomMenu,
                                    onDismissRequest = { showBottomMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("恢复安全参数") },
                                        onClick = {
                                            showBottomMenu = false
                                            showSafePresetsDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("恢复出厂设置") },
                                        onClick = {
                                            showBottomMenu = false
                                            showFactoryResetDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("修改蓝牙密码") },
                                        onClick = {
                                            showBottomMenu = false
                                            oldPassword = currentPassword
                                            newPassword = ""
                                            confirmNewPassword = ""
                                            passwordChangeError = null
                                            passwordChangeSuccess = false
                                            showPasswordChangeDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("导出参数") },
                                        onClick = {
                                            showBottomMenu = false
                                            exportLauncher.launch("zhike_settings_${System.currentTimeMillis()}.json")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("导入参数") },
                                        onClick = {
                                            showBottomMenu = false
                                            importError = null
                                            importJsonContent = ""
                                            importLauncher.launch("application/json")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item {
                InfoCard()
            }

            items(visibleGroups, key = { it.id }) { group ->
                ZhikeGroupCard(
                    group = group,
                    settings = settings,
                    onApplyPreset = { presetId ->
                        settings = ZhikeParameterCatalog.applyPreset(settings, presetId)
                        isDirty = true
                    },
                    onUpdateSetting = { updated ->
                        settings = updated
                        isDirty = true
                    }
                )
            }

            item {
                SecurityCard(
                    currentPassword = currentPassword,
                    onPasswordChange = {
                        currentPassword = it.filter(Char::isDigit).take(4)
                        authError = null
                    },
                    authError = authError
                )
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }

    SnackbarHost(hostState = snackbarHostState)

    // Validation dialog (shown before write dialog if there are issues)
    if (showValidationDialog && validationResult != null) {
        val result = validationResult!!
        BlurredAlertDialog(
            onDismissRequest = {
                showValidationDialog = false
                if (!result.hasBlockingIssues) {
                    showWriteDialog = true
                }
            },
            title = {
                Text(if (result.hasBlockingIssues) "参数校验错误" else "参数风险提示")
            },
            text = {
                Column {
                    if (result.errors.isNotEmpty()) {
                        Text(
                            text = "以下错误阻止写入:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        result.errors.forEach { issue ->
                            Text(
                                text = "• ${issue.title}: ${issue.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }
                    if (result.warnings.isNotEmpty()) {
                        if (result.errors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = "以下风险请确认:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        result.warnings.forEach { issue ->
                            Text(
                                text = "• ${issue.title}: ${issue.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (result.hasBlockingIssues) {
                    TextButton(
                        onClick = { showValidationDialog = false },
                        shape = bezierPillShape()
                    ) {
                        Text("关闭")
                    }
                } else {
                    Button(
                        onClick = {
                            showValidationDialog = false
                            showWriteDialog = true
                        },
                        shape = bezierPillShape()
                    ) {
                        Text("确认并继续")
                    }
                }
            },
            dismissButton = {
                if (!result.hasBlockingIssues) {
                    TextButton(
                        onClick = { showValidationDialog = false },
                        shape = bezierPillShape()
                    ) {
                        Text("取消")
                    }
                }
            }
        )
    }

    // Write confirmation dialog
    if (showWriteDialog) {
        BlurredAlertDialog(
            onDismissRequest = { showWriteDialog = false },
            title = { Text("写入确认") },
            text = {
                Column {
                    Text("将使用当前蓝牙密码校验后写入全部调参项。请确保车辆静止、断开 P 挡，并确认参数无误。")
                    if (validationResult?.hasWarnings == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "注意: 存在 ${validationResult!!.warnings.size} 项风险提示，已在上一步确认。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val error = viewModel.writeZhikeSettings(settings, currentPassword)
                        if (error != null) {
                            authError = error
                            return@Button
                        }
                        authError = null
                        isSyncing = true
                        showWriteDialog = false
                    },
                    shape = bezierPillShape()
                ) {
                    Text("确认写入")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        authError = null
                        showWriteDialog = false
                    },
                    shape = bezierPillShape()
                ) {
                    Text("取消")
                }
            }
        )
    }

    // Factory reset dialog
    if (showFactoryResetDialog) {
        BlurredAlertDialog(
            onDismissRequest = { showFactoryResetDialog = false },
            title = { Text("恢复出厂设置") },
            text = {
                Text("将重置控制器所有参数为出厂默认值。此操作不可撤销，请确保车辆静止并断开 P 挡。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetZhikeFactorySettings()
                        showFactoryResetDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("已发送恢复出厂设置指令")
                        }
                    },
                    shape = bezierPillShape()
                ) {
                    Text("确认重置")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFactoryResetDialog = false },
                    shape = bezierPillShape()
                ) {
                    Text("取消")
                }
            }
        )
    }

    // Password change dialog
    if (showPasswordChangeDialog) {
        BlurredAlertDialog(
            onDismissRequest = { showPasswordChangeDialog = false },
            title = { Text("修改蓝牙密码") },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it.filter(Char::isDigit).take(4) },
                        label = { Text("当前密码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = passwordChangeError != null
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it.filter(Char::isDigit).take(4) },
                        label = { Text("新密码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { confirmNewPassword = it.filter(Char::isDigit).take(4) },
                        label = { Text("确认新密码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = passwordChangeError != null
                    )
                    if (passwordChangeError != null) {
                        Text(
                            text = passwordChangeError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (passwordChangeSuccess) {
                        Text(
                            text = "密码修改成功",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val old = oldPassword.toIntOrNull()
                        val new = newPassword.toIntOrNull()
                        val confirm = confirmNewPassword.toIntOrNull()

                        if (old == null || old !in 0..9999) {
                            passwordChangeError = "当前密码格式错误"
                            return@Button
                        }
                        if (new == null || new !in 0..9999) {
                            passwordChangeError = "新密码格式错误"
                            return@Button
                        }
                        if (new != confirm) {
                            passwordChangeError = "两次输入的新密码不一致"
                            return@Button
                        }
                        if (old == new) {
                            passwordChangeError = "新密码与当前密码相同"
                            return@Button
                        }

                        val error = viewModel.changeZhikePassword(old, new)
                        if (error != null) {
                            passwordChangeError = error
                        } else {
                            passwordChangeSuccess = true
                            currentPassword = String.format(Locale.getDefault(), "%04d", new)
                        }
                    },
                    shape = bezierPillShape(),
                    enabled = !passwordChangeSuccess
                ) {
                    Text("确认修改")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPasswordChangeDialog = false },
                    shape = bezierPillShape()
                ) {
                    Text("取消")
                }
            }
        )
    }

    // Safe presets dialog
    if (showSafePresetsDialog) {
        BlurredAlertDialog(
            onDismissRequest = { showSafePresetsDialog = false },
            title = { Text("恢复安全参数") },
            text = {
                Column {
                    Text("将以下参数恢复为保守安全值：")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("• 母线电流：25A", style = MaterialTheme.typography.bodySmall)
                        Text("• 相线电流：80A", style = MaterialTheme.typography.bodySmall)
                        Text("• 弱磁电流：15A", style = MaterialTheme.typography.bodySmall)
                        Text("• 欠压点：39V", style = MaterialTheme.typography.bodySmall)
                        Text("• 过压点：84V", style = MaterialTheme.typography.bodySmall)
                        Text("• 回充电压上限：80V", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "此操作仅修改当前编辑模型，不会立即写入控制器。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val safeSettings = viewModel.applySafeZhikePresets()
                        settings = safeSettings
                        isDirty = true
                        showSafePresetsDialog = false
                    },
                    shape = bezierPillShape()
                ) {
                    Text("应用安全参数")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSafePresetsDialog = false },
                    shape = bezierPillShape()
                ) {
                    Text("取消")
                }
            }
        )
    }

    // Export success dialog
    if (showExportSuccessDialog) {
        BlurredAlertDialog(
            onDismissRequest = { showExportSuccessDialog = false },
            title = { Text("导出成功") },
            text = { Text("参数已导出为 JSON 文件。") },
            confirmButton = {
                TextButton(
                    onClick = { showExportSuccessDialog = false },
                    shape = bezierPillShape()
                ) {
                    Text("确定")
                }
            }
        )
    }

    // Import dialog
    if (showImportDialog) {
        BlurredAlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("导入参数") },
            text = {
                Column {
                    if (importError != null) {
                        Text(
                            text = importError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("将从 JSON 文件导入参数。导入后将覆盖当前设置。")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val imported = jsonToSettings(importJsonContent)
                            settings = imported
                            isDirty = true
                            showImportDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("导入成功")
                            }
                        } catch (e: Exception) {
                            importError = "解析失败: ${e.message}"
                        }
                    },
                    shape = bezierPillShape(),
                    enabled = importError == null
                ) {
                    Text("确认导入")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImportDialog = false },
                    shape = bezierPillShape()
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ZhikeGroupCard(
    group: ZhikeParamGroup,
    settings: ZhikeSettings,
    onApplyPreset: (String) -> Unit,
    onUpdateSetting: (ZhikeSettings) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.55f))

            group.parameters.forEach { definition ->
                val isDangerous = definition.isDangerous
                val isVersionLocked = definition.isVersionLocked(null) // TODO: get from controller

                when (definition.kind) {
                    ZhikeParamKind.BOOL -> {
                        SelectionParameterRow(
                            label = definition.label,
                            valueLabel = definition.optionLabels.getOrElse(
                                if (ZhikeParameterCatalog.readBool(settings, definition)) 1 else 0
                            ) { "" },
                            options = definition.optionLabels,
                            isDangerous = isDangerous,
                            isVersionLocked = isVersionLocked,
                            versionHint = definition.versionHint,
                            onSelect = { selectedIndex ->
                                if (!isVersionLocked) {
                                    onUpdateSetting(
                                        ZhikeParameterCatalog.updateBool(
                                            settings,
                                            definition,
                                            selectedIndex == 1
                                        )
                                    )
                                }
                            }
                        )
                    }
                    ZhikeParamKind.LIST -> {
                        SelectionParameterRow(
                            label = definition.label,
                            valueLabel = ZhikeParameterCatalog.optionLabel(settings, definition),
                            options = definition.optionLabels,
                            isDangerous = isDangerous,
                            isVersionLocked = isVersionLocked,
                            versionHint = definition.versionHint,
                            onSelect = { selectedIndex ->
                                if (!isVersionLocked) {
                                    onUpdateSetting(
                                        ZhikeParameterCatalog.updateList(settings, definition, selectedIndex)
                                    )
                                }
                            }
                        )
                    }
                    ZhikeParamKind.TEXT -> {
                        ReadOnlyParameterRow(
                            label = definition.label,
                            value = ZhikeParameterCatalog.readText(settings, definition),
                            isDangerous = isDangerous,
                            isVersionLocked = isVersionLocked,
                            versionHint = definition.versionHint
                        )
                    }
                    else -> {
                        NumericParameterRow(
                            definition = definition,
                            value = ZhikeParameterCatalog.readNumeric(settings, definition),
                            isDangerous = isDangerous,
                            isVersionLocked = isVersionLocked,
                            versionHint = definition.versionHint,
                            onCommit = { parsedValue ->
                                if (!isVersionLocked) {
                                    onUpdateSetting(
                                        ZhikeParameterCatalog.updateNumeric(settings, definition, parsedValue)
                                    )
                                }
                            }
                        )
                    }
                }
            }

            if (group.presets.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    group.presets.forEach { preset ->
                        AssistChip(
                            onClick = { onApplyPreset(preset.id) },
                            label = { Text(preset.label) },
                            colors = AssistChipDefaults.assistChipColors()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumericParameterRow(
    definition: ZhikeParamDefinition,
    value: Double,
    isDangerous: Boolean,
    isVersionLocked: Boolean,
    versionHint: String?,
    onCommit: (Double) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var text by remember(definition.key, value) {
        mutableStateOf(ZhikeParameterCatalog.formatNumericValue(value, definition))
    }

    fun commitValue() {
        val parsed = text.toDoubleOrNull() ?: value
        val clamped = parsed.coerceIn(definition.min, definition.max)
        text = ZhikeParameterCatalog.formatNumericValue(clamped, definition)
        onCommit(clamped)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = definition.label,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isDangerous) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "⚠️ 风险",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (isVersionLocked && versionHint != null) {
                Text(
                    text = versionHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                text = input
            },
            modifier = Modifier
                .width(164.dp)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        commitValue()
                    }
                },
            enabled = !isVersionLocked,
            suffix = {
                if (definition.unit.isNotBlank()) {
                    Text(definition.unit)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (definition.kind == ZhikeParamKind.FLOAT || definition.kind == ZhikeParamKind.INT) {
                    KeyboardType.Decimal
                } else {
                    KeyboardType.Number
                },
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    commitValue()
                    focusManager.clearFocus()
                }
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionParameterRow(
    label: String,
    valueLabel: String,
    options: List<String>,
    isDangerous: Boolean,
    isVersionLocked: Boolean,
    versionHint: String?,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isDangerous) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "⚠️ 风险",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (isVersionLocked && versionHint != null) {
                Text(
                    text = versionHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (!isVersionLocked) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = valueLabel,
                onValueChange = {},
                readOnly = true,
                enabled = !isVersionLocked,
                modifier = Modifier
                    .width(164.dp)
                    .menuAnchor(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyParameterRow(
    label: String,
    value: String,
    isDangerous: Boolean,
    isVersionLocked: Boolean,
    versionHint: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isDangerous) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "⚠️ 风险",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (isVersionLocked && versionHint != null) {
                Text(
                    text = versionHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isVersionLocked)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isVersionLocked)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SecurityCard(
    currentPassword: String,
    onPasswordChange: (String) -> Unit,
    authError: String?
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "写入校验",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.55f))
            OutlinedTextField(
                value = currentPassword,
                onValueChange = onPasswordChange,
                label = { Text("当前蓝牙密码") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = authError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                supportingText = {
                    Text(authError ?: "读取参数后，写入时会先用这里的密码做校验。")
                }
            )
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "已按 GEKOO 原版参数表补齐分组和位域。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "请在车辆静止、断开 P 挡时修改。电流、相位角、EBS 和 TCS 设置不当可能导致异响、误触发或驱动异常。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

// --- Extension properties and helpers ---

private val DANGEROUS_PARAM_KEYS = setOf(
    "bus_current",
    "phase_current",
    "weak_magnet_current",
    "under_voltage",
    "over_voltage",
    "regen_voltage_limit",
    "full_throttle_voltage",
    "idle_throttle_voltage",
    "tcs_sensitivity",
    "phase_shift_angle"
)

private val ZhikeParamDefinition.isDangerous: Boolean
    get() = key in DANGEROUS_PARAM_KEYS

private fun ZhikeParamDefinition.isVersionLocked(currentFirmwareVersion: Int?): Boolean {
    return minFirmwareVersion != null && currentFirmwareVersion != null && minFirmwareVersion > currentFirmwareVersion
}

private val ZhikeParamDefinition.versionHint: String?
    get() = if (minFirmwareVersion != null) "需要固件版本 >= $minFirmwareVersion" else null

private fun settingsToJson(settings: ZhikeSettings): String {
    val json = JSONObject()
    json.put("loadedFromController", settings.loadedFromController)
    json.put("originalBluetoothPassword", settings.originalBluetoothPassword)
    json.put("bluetoothPassword", settings.bluetoothPassword)
    json.put("busCurrent", settings.busCurrent)
    json.put("phaseCurrent", settings.phaseCurrent)
    json.put("motorDirection", settings.motorDirection)
    json.put("sensorType", settings.sensorType)
    json.put("hallSequence", settings.hallSequence)
    json.put("phaseShiftAngle", settings.phaseShiftAngle)
    json.put("polePairs", settings.polePairs)
    json.put("underVoltage", settings.underVoltage)
    json.put("overVoltage", settings.overVoltage)
    json.put("weakMagCurrent", settings.weakMagCurrent)
    json.put("regenCurrent", settings.regenCurrent)
    val rawArray = JSONArray()
    settings.rawWords.forEach { rawArray.put(it) }
    json.put("rawWords", rawArray)
    return json.toString(2)
}

private fun jsonToSettings(jsonString: String): ZhikeSettings {
    val json = JSONObject(jsonString)
    val settings = ZhikeSettings()
    settings.loadedFromController = json.optBoolean("loadedFromController", false)
    settings.originalBluetoothPassword = json.optInt("originalBluetoothPassword", 8888)
    settings.bluetoothPassword = json.optInt("bluetoothPassword", settings.originalBluetoothPassword)
    settings.busCurrent = json.optInt("busCurrent", 10)
    settings.phaseCurrent = json.optInt("phaseCurrent", 10)
    settings.motorDirection = json.optBoolean("motorDirection", false)
    settings.sensorType = json.optInt("sensorType", 0)
    settings.hallSequence = json.optBoolean("hallSequence", false)
    settings.phaseShiftAngle = json.optInt("phaseShiftAngle", 0)
    settings.polePairs = json.optInt("polePairs", 10)
    settings.underVoltage = json.optInt("underVoltage", 60)
    settings.overVoltage = json.optInt("overVoltage", 84)
    settings.weakMagCurrent = json.optInt("weakMagCurrent", 0)
    settings.regenCurrent = json.optInt("regenCurrent", 10)

    val rawArray = json.optJSONArray("rawWords")
    if (rawArray != null && rawArray.length() >= 64) {
        for (i in 0 until 64) {
            settings.rawWords[i] = rawArray.optInt(i, 0)
        }
        settings.syncLegacyFieldsFromWords()
    }

    return settings
}
