package com.shawnrain.habe.ui.settings.zhike

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.ble.protocols.ZhikeParamDefinition
import com.shawnrain.habe.ble.protocols.ZhikeParamGroup
import com.shawnrain.habe.ble.protocols.ZhikeParamKind
import com.shawnrain.habe.ble.protocols.ZhikeParameterCatalog
import com.shawnrain.habe.ble.protocols.ZhikeSettings
import com.shawnrain.habe.ui.navigation.BlurredAlertDialog
import com.shawnrain.habe.ui.theme.bezierPillShape
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ZhikeSettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    var settings by remember { mutableStateOf(ZhikeSettings()) }
    var isDirty by remember { mutableStateOf(false) }
    var showWriteDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("8888") }
    var isSyncing by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val syncMessage by viewModel.calibrationMessage.collectAsState()

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

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearCalibrationMessage()
        }
    }

    val visibleGroups = remember(settings.loadedFromController) {
        ZhikeParameterCatalog.groups
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("智科控制器调参")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
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
            if (isDirty) {
                Surface(tonalElevation = 8.dp) {
                    Button(
                        onClick = { showWriteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = bezierPillShape()
                    ) {
                        Text("写入控制器")
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

    if (showWriteDialog) {
        BlurredAlertDialog(
            onDismissRequest = { showWriteDialog = false },
            title = { Text("写入确认") },
            text = {
                Text("将使用当前蓝牙密码校验后写入全部调参项。请确保车辆静止、断开 P 挡，并确认参数无误。")
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
                when (definition.kind) {
                    ZhikeParamKind.BOOL -> {
                        SelectionParameterRow(
                            label = definition.label,
                            valueLabel = definition.optionLabels.getOrElse(
                                if (ZhikeParameterCatalog.readBool(settings, definition)) 1 else 0
                            ) { "" },
                            options = definition.optionLabels,
                            onSelect = { selectedIndex ->
                                onUpdateSetting(
                                    ZhikeParameterCatalog.updateBool(
                                        settings,
                                        definition,
                                        selectedIndex == 1
                                    )
                                )
                            }
                        )
                    }
                    ZhikeParamKind.LIST -> {
                        SelectionParameterRow(
                            label = definition.label,
                            valueLabel = ZhikeParameterCatalog.optionLabel(settings, definition),
                            options = definition.optionLabels,
                            onSelect = { selectedIndex ->
                                onUpdateSetting(
                                    ZhikeParameterCatalog.updateList(settings, definition, selectedIndex)
                                )
                            }
                        )
                    }
                    ZhikeParamKind.TEXT -> {
                        ReadOnlyParameterRow(
                            label = definition.label,
                            value = ZhikeParameterCatalog.readText(settings, definition)
                        )
                    }
                    else -> {
                        NumericParameterRow(
                            definition = definition,
                            value = ZhikeParameterCatalog.readNumeric(settings, definition),
                            onCommit = { parsedValue ->
                                onUpdateSetting(
                                    ZhikeParameterCatalog.updateNumeric(settings, definition, parsedValue)
                                )
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
        Text(
            text = definition.label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
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
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = valueLabel,
                onValueChange = {},
                readOnly = true,
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
private fun ReadOnlyParameterRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
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
