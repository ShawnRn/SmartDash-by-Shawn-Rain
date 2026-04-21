package com.shawnrain.sdash.data.telemetry

enum class SampleDataMode {
    /**
     * 原始控制器帧，可信且可直接消费。
     */
    RAW,

    /**
     * 检测到短时毛刺后，暂时使用上一帧可信值顶住展示与估算。
     */
    HELD_LAST_GOOD,

    /**
     * 当前帧本身无效，且没有可安全复用的可信值。
     */
    INVALID,

    /**
     * 持续异常已被接受为真实掉电/休眠。
     */
    POWER_OFF
}

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
    val displaySpeedKmH: Float,
    val distanceSpeedKmH: Float,
    val controllerSpeedKmH: Float,
    val motorTempC: Float,
    val controllerTempC: Float,
    val braking: Boolean,
    val cruise: Boolean,
    val quality: SampleQuality,
    val dataMode: SampleDataMode = SampleDataMode.RAW,
    val dtMs: Long,
    /**
     * 是否允许参与 Wh/Ah 物理积分
     */
    val allowIntegration: Boolean,
    /**
     * 是否允许参与内阻/能耗学习
     */
    val allowLearning: Boolean
) {
    val shouldPersistToHistory: Boolean
        get() = quality == SampleQuality.GOOD && dataMode == SampleDataMode.RAW
}
