package com.shawnrain.sdash.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.shawnrain.sdash.data.MetricType

/**
 * 获取 MetricType 对应的 ImageVector 图标
 */
val MetricType.icon: ImageVector
    get() = when (this) {
        MetricType.SPEED -> Icons.Default.Speed
        MetricType.GRADE -> Icons.AutoMirrored.Filled.TrendingUp
        MetricType.ALTITUDE -> Icons.Default.FilterHdr
        MetricType.VOLTAGE -> Icons.Default.FlashOn
        MetricType.VOLTAGE_SAG -> Icons.Default.ElectricBolt
        MetricType.BUS_CURRENT -> Icons.Default.PowerInput
        MetricType.PHASE_CURRENT -> Icons.Default.ElectricalServices
        MetricType.POWER -> Icons.Default.Bolt
        MetricType.TEMP -> Icons.Default.Thermostat
        MetricType.MAX_CONTROLLER_TEMP -> Icons.Default.LocalFireDepartment
        MetricType.SOC -> Icons.Default.BatteryChargingFull
        MetricType.RANGE -> Icons.Default.Route
        MetricType.RPM -> Icons.Default.Autorenew
        MetricType.EFFICIENCY -> Icons.Default.BarChart
        MetricType.TRIP_DISTANCE -> Icons.Default.Map
        MetricType.ELAPSED_TIME -> Icons.Default.Timer
        MetricType.TOTAL_ENERGY -> Icons.Default.BatteryFull
        MetricType.PEAK_REGEN_POWER -> Icons.Default.OfflineBolt
        MetricType.RECOVERED_ENERGY -> Icons.Default.Eco
        MetricType.MEDIA_CONTROL -> Icons.Default.MusicNote
    }

/**
 * 获取 MetricType 对应的分类色彩
 */
val MetricType.tintColor: androidx.compose.ui.graphics.Color
    get() = when (this.category) {
        com.shawnrain.sdash.data.MetricCategory.DISTANCE -> androidx.compose.ui.graphics.Color(0xFF26C6DA)    // 青色 Cyan
        com.shawnrain.sdash.data.MetricCategory.ELECTRICAL -> androidx.compose.ui.graphics.Color(0xFFFFCA28)  // 琥珀色 Amber
        com.shawnrain.sdash.data.MetricCategory.POWER -> androidx.compose.ui.graphics.Color(0xFFD0BCFF)       // 紫色 speedAccent
        com.shawnrain.sdash.data.MetricCategory.THERMAL -> androidx.compose.ui.graphics.Color(0xFFFF7043)     // 橙红色 Orange
        com.shawnrain.sdash.data.MetricCategory.BATTERY -> androidx.compose.ui.graphics.Color(0xFF66BB6A)     // 绿色 Green
        com.shawnrain.sdash.data.MetricCategory.TIME -> androidx.compose.ui.graphics.Color(0xFFAB47BC)        // 紫色
        com.shawnrain.sdash.data.MetricCategory.MEDIA -> androidx.compose.ui.graphics.Color(0xFF29B6F6)       // 浅蓝色
    }

