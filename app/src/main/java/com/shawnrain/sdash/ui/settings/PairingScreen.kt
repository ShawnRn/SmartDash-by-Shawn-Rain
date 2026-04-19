package com.shawnrain.sdash.ui.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shawnrain.sdash.MainViewModel
import com.shawnrain.sdash.ui.navigation.P2PageHeader
import com.shawnrain.sdash.ui.theme.bezierPillShape
import com.shawnrain.sdash.ui.theme.bezierRoundedShape

@Composable
fun PairingScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val isHogpActive by viewModel.isHidPairingActive.collectAsState()
    val isHidConnected by viewModel.isHidConnected.collectAsState()
    val isHidSubscribed by viewModel.isHidSubscribed.collectAsState()
    val isHidSupported = viewModel.isHidSupported
    
    val infiniteTransition = rememberInfiniteTransition(label = "PairingPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            P2PageHeader(
                title = "iPhone 配对",
                subtitle = "模拟遥控逻辑",
                onBack = null, 
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Visualization Area
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isHogpActive && !isHidConnected) {
                        // Pulsing Rings (Only if advertising but not yet connected)
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .scale(pulseScale)
                                .alpha(pulseAlpha)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    shape = bezierRoundedShape(90.dp)
                                )
                        )
                    }

                    Surface(
                        modifier = Modifier.size(140.dp),
                        shape = bezierRoundedShape(40.dp),
                        color = when {
                            isHidSubscribed -> Color(0xFF4CAF50) // Green when ready
                            isHidConnected -> MaterialTheme.colorScheme.secondary // Yellow-ish/Secondary when connected
                            isHogpActive -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        tonalElevation = 4.dp,
                        shadowElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when {
                                    isHidSubscribed -> Icons.Default.BluetoothConnected
                                    isHidConnected -> Icons.Default.BluetoothSearching
                                    isHogpActive -> Icons.Default.BluetoothSearching
                                    else -> Icons.Default.Bluetooth
                                },
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = if (isHogpActive || isHidConnected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                val statusText = when {
                    isHidSubscribed -> "控制已就绪"
                    isHidConnected -> "连接中..."
                    isHogpActive -> "等待 iPhone 连接..."
                    else -> "配对模式未开启"
                }
                
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isHidSubscribed -> Color(0xFF4CAF50)
                        isHidConnected || isHogpActive -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                val descriptionText = when {
                    isHidSubscribed -> "已通过 HID 协议与 iPhone 建立连接。\n现在你可以使用仪表盘上的媒体卡片控制音乐了。"
                    isHidConnected -> "设备已连接，正在进行 HID 握手与配对。\n如果 iPhone 弹出配对请求，务必点击「确认」。"
                    isHogpActive -> "正在广播 SmartDash 信号...\n请在 iPhone 的「设置 - 蓝牙」中点击「SmartDash」。"
                    else -> "点击下方按钮开启广播，使 iPhone 可以发现此设备进行模拟遥控配对。"
                }

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { viewModel.toggleBluetoothDiscoverable() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = bezierPillShape(),
                    colors = if (isHogpActive) 
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                    else 
                        ButtonDefaults.buttonColors()
                ) {
                    Text(
                        text = if (isHogpActive) "停止配对广播" else "开始配对广播",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Guidance for Interconnect Users
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.shapes.large
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "已有「苹果互联」？",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "如果您已开启 vivo/小米等厂商自带的苹果互联功能，无需在此进行配对。直接在仪表仪表盘媒体卡片切换至「系统级」模式即可控制 iPhone。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Feature Highlights
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            MaterialTheme.shapes.large
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PairingFeatureItem(
                        icon = Icons.Default.Smartphone,
                        title = "模拟遥控模式 (HOGP)",
                        description = "针对没有系统级互联功能的设备，通过此模式将仪表模拟为蓝牙遥控器。"
                    )
                    PairingFeatureItem(
                        icon = Icons.Default.Bluetooth,
                        title = "解决搜索不到的问题",
                        description = "部分 iPhone 无法主动发现手机蓝牙，通过此模式可由仪表主动发起广播。"
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PairingFeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = bezierRoundedShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
