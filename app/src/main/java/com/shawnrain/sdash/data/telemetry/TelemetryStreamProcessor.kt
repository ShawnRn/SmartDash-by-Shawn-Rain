package com.shawnrain.sdash.data.telemetry

import com.shawnrain.sdash.ble.VehicleMetrics
import kotlin.math.abs

/**
 * 负责将原始控制器遥测流转换为合法的 TelemetrySample 流。
 * 处理去重、异常值过滤、以及采样时间间隔 (dt) 的合法性检查。
 */
class TelemetryStreamProcessor {
    private var lastRawVoltage = 0.0f
    private var lastRawBusCurrent = 0.0f
    private var lastRawPhaseCurrent = 0.0f
    private var lastRawRpm = 0.0f
    private var lastTimestampMs = 0L
    private var frameSequence = 0L

    fun process(rawMetrics: VehicleMetrics, nowMs: Long = System.currentTimeMillis()): TelemetrySample {
        val dtMs = if (lastTimestampMs > 0L) (nowMs - lastTimestampMs).coerceAtLeast(0L) else 0L

        // --- P0: finite value checks first — non-finite inputs are immediately rejected ---
        val hasNonFinite = !rawMetrics.voltage.isFinite() ||
                !rawMetrics.busCurrent.isFinite() ||
                !rawMetrics.phaseCurrent.isFinite() ||
                !rawMetrics.rpm.isFinite() ||
                !rawMetrics.controllerSpeedKmH.isFinite()

        if (hasNonFinite) {
            // Emit an OUTLIER sample with zeroed payload so integrators skip it cleanly
            return TelemetrySample(
                timestampMs = nowMs,
                sourceFrameId = ++frameSequence,
                voltageV = 0f,
                busCurrentA = 0f,
                phaseCurrentA = 0f,
                rpm = 0f,
                controllerSpeedKmH = 0f,
                motorTempC = 0f,
                controllerTempC = 0f,
                braking = false,
                cruise = false,
                quality = SampleQuality.OUTLIER,
                dtMs = dtMs,
                allowIntegration = false,
                allowLearning = false
            ).also {
                // Do NOT update last-state tracking for non-finite frames
                lastTimestampMs = nowMs
            }
        }

        // --- Duplicate detection: only skip when dt is tiny AND all values are nearly identical ---
        // Reduced from 50ms → 20ms to avoid dropping valid fast-arrival BLE frames
        val isDuplicate = dtMs < 20L &&
                abs(rawMetrics.voltage - lastRawVoltage) < 0.01f &&
                abs(rawMetrics.busCurrent - lastRawBusCurrent) < 0.01f &&
                abs(rawMetrics.phaseCurrent - lastRawPhaseCurrent) < 0.01f &&
                abs(rawMetrics.rpm - lastRawRpm) < 0.1f

        val quality = when {
            isDuplicate -> SampleQuality.DUPLICATE
            dtMs < 15L -> SampleQuality.TOO_DENSE
            dtMs > 5000L -> SampleQuality.GAP_RESET
            rawMetrics.voltage !in 15.0f..120.0f -> SampleQuality.OUTLIER
            abs(rawMetrics.busCurrent) > 550.0f -> SampleQuality.OUTLIER
            abs(rawMetrics.rpm) > 20000.0f -> SampleQuality.OUTLIER
            rawMetrics.controllerSpeedKmH > 300.0f -> SampleQuality.OUTLIER
            // RPM 与 速度逻辑矛盾 (例如转速 1000+ 但速度为 0)
            abs(rawMetrics.rpm) > 1000.0f && rawMetrics.controllerSpeedKmH < 1.0f -> SampleQuality.OUTLIER
            else -> SampleQuality.GOOD
        }

        // --- Integration rules ---
        // Relaxed from 46..2500 → 20..5000 to accommodate real BLE jitter:
        // - dt ~20-40ms: fast callback bursts, still valid physics for that interval
        // - dt 2500-5000ms: BLE congestion / GC pause, but power was sustained during that gap
        // - dt > 5000ms: true gap, mark GAP_RESET, let next GOOD frame start new segment
        val allowIntegration = (quality == SampleQuality.GOOD) && (dtMs >= 20L)
        val allowLearning = quality == SampleQuality.GOOD && dtMs in 50L..2000L

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

        // 更新状态，用于下一帧比对 (already guaranteed finite here)
        lastRawVoltage = rawMetrics.voltage
        lastRawBusCurrent = rawMetrics.busCurrent
        lastRawPhaseCurrent = rawMetrics.phaseCurrent
        lastRawRpm = rawMetrics.rpm
        lastTimestampMs = nowMs

        return sample
    }

    fun reset() {
        lastRawVoltage = 0.0f
        lastRawBusCurrent = 0.0f
        lastRawPhaseCurrent = 0.0f
        lastRawRpm = 0.0f
        lastTimestampMs = 0L
        frameSequence = 0L
    }
}
