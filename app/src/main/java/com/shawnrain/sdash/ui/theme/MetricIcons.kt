package com.shawnrain.sdash.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.shawnrain.sdash.data.MetricCategory
import com.shawnrain.sdash.data.MetricType

/**
 * 获取 MetricCategory 对应的分类强调色
 */
val MetricCategory.tintColor: Color
    get() = when (this) {
        MetricCategory.ELECTRICAL -> Color(0xFFFFCA28) // 琥珀色
        MetricCategory.POWER -> Color(0xFF7C4DFF)      // 蓝紫色
        MetricCategory.THERMAL -> Color(0xFFFF7043)    // 橙红色
        MetricCategory.DISTANCE -> Color(0xFF26C6DA)   // 青绿色
        MetricCategory.BATTERY -> Color(0xFF66BB6A)    // 绿色
        MetricCategory.TIME -> Color(0xFF42A5F5)       // 浅蓝色
        MetricCategory.MEDIA -> Color(0xFFD0BCFF)      // 主题 Primary 浅紫色
    }

/**
 * 获取 MetricType 对应的分类强调色（直接关联其 Category）
 */
val MetricType.tintColor: Color
    get() = this.category.tintColor

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
