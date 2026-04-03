package com.shawnrain.habe.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.data.SpeedSource
import com.shawnrain.habe.data.DataSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onNavigateToBms: () -> Unit, modifier: Modifier = Modifier) {
    val wheelCircumference by viewModel.wheelCircumference.collectAsState()
    val polePairs by viewModel.polePairs.collectAsState()
    val speedSource by viewModel.speedSource.collectAsState()
    val battDataSource by viewModel.battDataSource.collectAsState()

    var wheelInput by remember(wheelCircumference) { mutableStateOf(wheelCircumference.toString()) }
    var poleInput by remember(polePairs) { mutableStateOf(polePairs.toString()) }

    var expandSpeedSource by remember { mutableStateOf(false) }
    var expandBattSource by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))

        // BMS Entry
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
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("数据源优先仲裁", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        // Speed Source Selector
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

        // Battery Data Source Selector
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

        ListItem(
            headlineContent = { Text("协议识别 (内置解析库)") },
            supportingContent = { Text("连接后将自动分流至 智科/远驱/凌博/APT 等对应的解析器。所有控制器的全部字段与高级设置均免费下发。") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
