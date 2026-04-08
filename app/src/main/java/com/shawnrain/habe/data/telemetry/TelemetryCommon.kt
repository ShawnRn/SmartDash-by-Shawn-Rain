package com.shawnrain.habe.data.telemetry

/**
 * 能量口径单位与语义化类型定义
 */

/**
 * 能量值 (Watt-hour)
 */
typealias EnergyWh = Float

/**
 * 电量值 (Ampere-hour)
 */
typealias EnergyAh = Float

/**
 * 能效/能耗比 (Wh/km)
 */
typealias EfficiencyWhKm = Float

/**
 * 遥测口径常量定义 (United Energy Standard)
 */
object TelemetryConstants {
    /**
     * 用户界面与续航估算默认采用的口径：Net (Traction - Regen)
     * 包含动能回收贡献，是最能反映续航能力的基准。
     */
    const val PREFERRED_EFFICIENCY_NAME = "Net"
    
    /**
     * 仅用于分析或特定详情展示的口径：Traction (毛能耗)
     */
    const val ANALYSIS_EFFICIENCY_NAME = "Traction"
}
