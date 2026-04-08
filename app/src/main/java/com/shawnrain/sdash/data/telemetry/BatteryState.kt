package com.shawnrain.sdash.data.telemetry

import com.shawnrain.sdash.data.history.RideMetricSample

/**
 * 电池状态估计结果
 */
data class BatteryState(
    val socPercent: Float = 0.0f,
    val socByAhPercent: Float = 0.0f,
    val socByOcvPercent: Float = 0.0f,
    val filteredVoltage: Float = 0.0f,
    val filteredCurrent: Float = 0.0f,
    val learnedInternalResistanceOhm: Float = 0.0f,
    val isStationary: Boolean = true,
    val confidence: Float = 0.5f, // 0.0 - 1.0
    /**
     * 内阻学习上下文 (占位，用于后期分速度/分SoC区间分析)
     */
    val irContext: String = "default"
)
