package com.shawnrain.habe.ui.bms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shawnrain.habe.MainViewModel
import com.shawnrain.habe.ui.theme.bezierRoundedShape

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BmsScreen(viewModel: MainViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val bmsMetrics by viewModel.bmsMetrics.collectAsState()
    val bmsLabel by viewModel.bmsActiveProtocolLabel.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { 
                Column {
                    Text("电池与 BMS", style = MaterialTheme.typography.titleMedium)
                    Text(bmsLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )
        
        LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard(title = "总电压", value = String.format("%.2f V", bmsMetrics.totalVoltage), modifier = Modifier.weight(1f))
                    InfoCard(title = "剩余电量", value = String.format("%.1f %%", bmsMetrics.soc), modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard(title = "电流", value = String.format("%.2f A", bmsMetrics.current), modifier = Modifier.weight(1f))
                    InfoCard(title = "功率", value = String.format("%.2f kW", (bmsMetrics.totalVoltage * bmsMetrics.current) / 1000f), modifier = Modifier.weight(1f))
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("单体电压 (Cell Voltages)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 4
                ) {
                    bmsMetrics.cellVoltages.forEachIndexed { index, volt ->
                        CellCard(index + 1, volt)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("状态监控", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                StatusRow("充电 MOS", bmsMetrics.chargeMosOn)
                StatusRow("放电 MOS", bmsMetrics.dischargeMosOn)
                StatusRow("均衡状态", bmsMetrics.balanceOn)
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun CellCard(index: Int, voltage: Float) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = bezierRoundedShape(8.dp),
        modifier = Modifier.width(70.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "C$index", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = String.format("%.3f", voltage), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatusRow(label: String, isOn: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Surface(
            color = if (isOn) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
            shape = bezierRoundedShape(12.dp)
        ) {
            Text(
                text = if (isOn) "开启" else "关闭",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = if (isOn) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(bezierRoundedShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = MaterialTheme.colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
