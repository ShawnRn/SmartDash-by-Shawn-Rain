package com.shawnrain.habe.data.telemetry

import com.shawnrain.habe.data.history.RideMetricSample

/**
 * 电池状态估计结果
 */
data class BatteryState(
    val socPercent: Float,
    val socByAhPercent: Float,
    val socByOcvPercent: Float,
    val filteredVoltage: Float,
    val filteredCurrent: Float,
    val learnedInternalResistanceOhm: Float,
    val isStationary: Boolean,
    val confidence: Float // 0.0 - 1.0
)
