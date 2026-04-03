package com.shawnrain.habe.ui.speedtest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shawnrain.habe.MainViewModel

@Composable
fun SpeedtestScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val metrics by viewModel.metrics.collectAsState()
    var isTesting by remember { mutableStateOf(false) }
    var currentRecord by remember { mutableStateOf("0.0 s") }
    var bestRecord by remember { mutableStateOf("未测") }
    var speedStart by remember { mutableStateOf(0f) }
    var timeStart by remember { mutableStateOf(0L) }

    LaunchedEffect(metrics.speedKmH) {
        val speed = metrics.speedKmH
        if (isTesting) {
            if (speed >= 100f) {
                isTesting = false
                val timeS = (System.currentTimeMillis() - timeStart) / 1000f
                currentRecord = String.format("%.2f s", timeS)
                bestRecord = currentRecord // simplistic mock update
            }
        } else {
            if (speed < 5f) {
                // allow trigger
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.RocketLaunch,
            contentDescription = "Rocket",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "0-100 加速测试",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("当前成绩", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(currentRecord, fontSize = 48.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("历史最佳成绩: $bestRecord", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                isTesting = !isTesting
                if (isTesting) {
                    timeStart = System.currentTimeMillis()
                    currentRecord = "测试中..."
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(if (isTesting) "停止测试" else "准备起步")
        }
    }
}
