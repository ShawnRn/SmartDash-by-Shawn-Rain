package com.shawnrain.habe.data.telemetry

/**
 * 续航估计结果
 */
data class RangeEstimate(
    val estimatedRangeKm: Float,
    val remainingEnergyWh: Float,
    val averageWindowEfficiencyWhKm: Float,
    val isWindowFresh: Boolean
)
