package com.shawnrain.habe.data.telemetry

import com.shawnrain.habe.data.history.RideMetricSample

/**
 * 电池状态估计结果
 */
data class BatteryState(
    val socPercent: Float = 0f,
    val socByAhPercent: Float = 0f,
    val socByOcvPercent: Float = 0f,
    val filteredVoltage: Float = 0f,
    val filteredCurrent: Float = 0f,
    val learnedInternalResistanceOhm: Float = 0f,
    val isStationary: Boolean = true,
    val confidence: Float = 0.5f // 0.0 - 1.0
)
