package com.shawnrain.habe.data.telemetry

import com.shawnrain.habe.ble.VehicleMetrics
import kotlin.math.abs

/**
 * 负责将原始控制器遥测流转换为合法的 TelemetrySample 流。
 * 处理去重、异常值过滤、以及采样时间间隔 (dt) 的合法性检查。
 */
class TelemetryStreamProcessor {
    private var lastRawVoltage = 0f
    private var lastRawBusCurrent = 0f
    private var lastRawPhaseCurrent = 0f
    private var lastRawRpm = 0f
    private var lastTimestampMs = 0L
    private var frameSequence = 0L

    fun process(rawMetrics: VehicleMetrics, nowMs: Long = System.currentTimeMillis()): TelemetrySample {
        val dtMs = if (lastTimestampMs > 0L) (nowMs - lastTimestampMs).coerceAtLeast(0L) else 0L

        // 识别重复或过密采样 (Android BLE 下窗口扩大至 50ms)
        val isDuplicate = dtMs < 50L && 
                abs(rawMetrics.voltage - lastRawVoltage) < 0.01f &&
                abs(rawMetrics.busCurrent - lastRawBusCurrent) < 0.01f &&
                abs(rawMetrics.phaseCurrent - lastRawPhaseCurrent) < 0.01f &&
                abs(rawMetrics.rpm - lastRawRpm) < 0.1f

        val quality = when {
            isDuplicate -> SampleQuality.DUPLICATE
            dtMs < 45L -> SampleQuality.TOO_DENSE
            dtMs > 3000L -> SampleQuality.GAP_RESET
            rawMetrics.voltage !in 15f..120f -> SampleQuality.OUTLIER
            abs(rawMetrics.busCurrent) > 550f -> SampleQuality.OUTLIER
            abs(rawMetrics.rpm) > 20000f -> SampleQuality.OUTLIER
            rawMetrics.controllerSpeedKmH > 300f -> SampleQuality.OUTLIER
            // RPM 与 速度逻辑矛盾 (例如转速 1000+ 但速度为 0)
            abs(rawMetrics.rpm) > 1000f && rawMetrics.controllerSpeedKmH < 1f -> SampleQuality.OUTLIER
            else -> SampleQuality.GOOD
        }

        // 积分规则：GOOD 或断流后的第一帧 (GAP_RESET) 时可考虑显示更新，
        // 但积分量必须建立在有效的 dt 之上。
        val allowIntegration = (quality == SampleQuality.GOOD) && (dtMs in 46L..2500L)
        val allowLearning = quality == SampleQuality.GOOD && dtMs in 100L..1500L

        if (!isDuplicate) frameSequence++

        val sample = TelemetrySample(
            timestampMs = nowMs,
            sourceFrameId = frameSequence,
            voltageV = rawMetrics.voltage,
            busCurrentA = rawMetrics.busCurrent,
            phaseCurrentA = rawMetrics.phaseCurrent,
            rpm = rawMetrics.rpm,
            controllerSpeedKmH = rawMetrics.controllerSpeedKmH,
            motorTempC = rawMetrics.mosfetTemp,
            controllerTempC = rawMetrics.controllerTemp,
            braking = rawMetrics.isBraking,
            cruise = rawMetrics.isCruise,
            quality = quality,
            dtMs = dtMs,
            allowIntegration = allowIntegration,
            allowLearning = allowLearning
        )

        // 更新状态，用于下一帧比对
        lastRawVoltage = rawMetrics.voltage
        lastRawBusCurrent = rawMetrics.busCurrent
        lastRawPhaseCurrent = rawMetrics.phaseCurrent
        lastRawRpm = rawMetrics.rpm
        lastTimestampMs = nowMs

        return sample
    }

    fun reset() {
        lastRawVoltage = 0f
        lastRawBusCurrent = 0f
        lastRawPhaseCurrent = 0f
        lastRawRpm = 0f
        lastTimestampMs = 0L
        frameSequence = 0L
    }
}
