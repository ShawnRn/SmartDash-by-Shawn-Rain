package com.shawnrain.sdash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shawnrain.sdash.ui.theme.LocalUseMiSansFont
import com.shawnrain.sdash.ui.theme.MiSansFontFamily
import kotlin.math.abs
import kotlin.math.min

/** The original compact 2x2 PiP HUD, restored after the visual experiment. */
@Composable
internal fun InitialTelemetryPipScreen(viewModel: MainViewModel) {
    val metrics by viewModel.metrics.collectAsState()
    val currentVehicle by viewModel.currentVehicle.collectAsState()
    val speedText = metrics.speedKmH.toInt().coerceAtLeast(0).toString()
    val powerKw = metrics.totalPowerW / 1000f
    val baseWidth = 350.dp
    val baseHeight = 175.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        val scale = min(maxWidth / baseWidth, maxHeight / baseHeight)
        Box(
            modifier = Modifier
                .width(baseWidth * scale)
                .height(baseHeight * scale),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .requiredSize(baseWidth, baseHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        val font = pipFontFamily()
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = speedText,
                                fontSize = 52.sp,
                                lineHeight = 52.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = font,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "km/h",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = font,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            InitialPipGridItem(
                                value = metrics.soc.toInt().coerceIn(0, 100).toString(),
                                unit = "%",
                                label = "SOC",
                                valueColor = when {
                                    metrics.soc < 20f -> Color(0xFFFF5252)
                                    metrics.soc < 40f -> Color(0xFFFF9800)
                                    else -> MaterialTheme.colorScheme.onBackground
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            val underVoltage = currentVehicle.batterySeries.coerceAtLeast(1) * 3.2f
                            InitialPipGridItem(
                                value = String.format("%.1f", metrics.voltage),
                                unit = "V",
                                label = "电压",
                                valueColor = if (metrics.voltage < 45f || metrics.voltage < underVoltage) {
                                    Color(0xFFFF9800)
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            InitialPipGridItem(
                                value = metrics.estimatedRangeKm.toInt().coerceAtLeast(0).toString(),
                                unit = "km",
                                label = "续航"
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            InitialPipGridItem(
                                value = metrics.controllerTemp.toInt().toString(),
                                unit = "°C",
                                label = "温控",
                                valueColor = if (metrics.controllerTemp > 65f) {
                                    Color(0xFFFF5252)
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            )
                        }
                    }
                }
                InitialPipPowerBar(
                    powerKw = powerKw,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
        }
    }
}

@Composable
private fun InitialPipGridItem(
    value: String,
    unit: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground
) {
    val font = pipFontFamily()
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray.copy(alpha = 0.9f),
            fontFamily = font
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 30.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = font,
                color = valueColor,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = unit,
                fontSize = 12.sp,
                fontFamily = font,
                color = Color.Gray,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}

@Composable
private fun InitialPipPowerBar(powerKw: Float, modifier: Modifier = Modifier) {
    val fraction by animateFloatAsState(
        targetValue = (abs(powerKw) / if (powerKw < 0f) 0.8f else 3.5f).coerceIn(0f, 1f),
        animationSpec = tween(180),
        label = "initial_pip_power"
    )
    val active = abs(powerKw) >= 0.08f
    val color = if (powerKw < 0f) Color(0xFF10B981) else Color(0xFFF59E0B)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight()
                .width(1.5.dp)
                .background(Color.White.copy(alpha = 0.38f))
        )
        if (active) {
            Box(
                modifier = Modifier
                    .align(if (powerKw >= 0f) Alignment.CenterEnd else Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f * fraction)
                    .background(color.copy(alpha = 0.86f))
            )
        }
    }
}

@Composable
private fun pipFontFamily(): FontFamily =
    if (LocalUseMiSansFont.current) MiSansFontFamily else FontFamily.Monospace
