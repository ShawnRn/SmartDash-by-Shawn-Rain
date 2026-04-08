package com.shawnrain.sdash.data.telemetry

/**
 * 统一样本结构，将底层原始协议帧与上层估算逻辑隔离开。
 */
data class TelemetrySample(
    val timestampMs: Long,
    val sourceFrameId: Long? = null,
    val voltageV: Float,
    val busCurrentA: Float,
    val phaseCurrentA: Float,
    val rpm: Float,
    val controllerSpeedKmH: Float,
    val motorTempC: Float,
    val controllerTempC: Float,
    val braking: Boolean,
    val cruise: Boolean,
    val quality: SampleQuality,
    val dtMs: Long,
    /**
     * 是否允许参与 Wh/Ah 物理积分
     */
    val allowIntegration: Boolean,
    /**
     * 是否允许参与内阻/能耗学习
     */
    val allowLearning: Boolean
)
