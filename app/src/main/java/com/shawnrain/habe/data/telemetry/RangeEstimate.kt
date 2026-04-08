package com.shawnrain.habe.data.telemetry

/**
 * 续航置信度
 */
enum class RangeConfidence {
    LOW,    // 窗口数据不足或波动极大
    MEDIUM, // 数据基本可靠
    HIGH    // 稳态高置信度
}

/**
 * 续航估计结果
 */
data class RangeEstimate(
    val estimatedRangeKm: Float = 0.0f,
    val remainingEnergyWh: EnergyWh = 0.0f,
    val averageWindowEfficiencyWhKm: EfficiencyWhKm = 0.0f,
    val isWindowFresh: Boolean = false,
    val confidence: RangeConfidence = RangeConfidence.LOW
)
