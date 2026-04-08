package com.shawnrain.habe.data.telemetry

import com.shawnrain.habe.data.history.RideMetricSample
import kotlin.math.abs

/**
 * 电池状态估计器 (SoC / 内阻 / 电压电流平滑)
 * 从原来的 RideEnergyEstimator 中剥离，专注电池本身
 */
class BatteryStateEstimator {
    companion object {
        private const val EMA_ALPHA = 0.12f
        private const val CURRENT_STEP_THRESHOLD_A = 10f
        private const val STATIONARY_CURRENT_THRESHOLD_A = 1f
        private const val OCV_RECALIBRATION_MS = 60_000L // 1分钟静置后大幅回归 OCV
        private const val OCV_SETTLED_MS = 180_000L      // 3分钟后几乎完全信任 OCV
    }

    private var filteredVoltage = Float.NaN
    private var filteredCurrent = Float.NaN
    private var previousRawVoltage = 0f
    private var previousRawCurrent = 0f
    private var learnedInternalResistanceOhm = 0f
    
    private var lastSampleAtMs = 0L
    private var stationarySinceMs = 0L
    private var baseSocPercent = Float.NaN
    private var currentSocAhPercent = Float.NaN
    private var lastRawSpeed = 0f
    private var lastRawRpm = 0f

    private var lastSampleId: Long? = null
    private var lastBatteryState: BatteryState? = null
    
    // 电压稳定性追踪 (用于判定是否处于回弹期)
    private val voltageWindow = FloatArray(8)
    private var voltageWindowPadding = 0
    private var voltageWindowIndex = 0

    fun reset(initialResistance: Float = 0f) {
        filteredVoltage = Float.NaN
        filteredCurrent = Float.NaN
        previousRawVoltage = 0f
        previousRawCurrent = 0f
        learnedInternalResistanceOhm = initialResistance.coerceAtLeast(0f)
        lastSampleAtMs = 0L
        stationarySinceMs = 0L
        baseSocPercent = Float.NaN
        currentSocAhPercent = Float.NaN
        lastSampleId = null
        lastBatteryState = null
        voltageWindowPadding = 0
        voltageWindowIndex = 0
        voltageWindow.fill(0f)
        lastRawSpeed = 0f
        lastRawRpm = 0f
    }

    fun estimate(
        sample: TelemetrySample,
        accumulator: RideAccumulatorState,
        batteryCapacityAh: Float,
        batterySeries: Int,
        fallbackSocPercent: Float? = null
    ): BatteryState {
        // 0. 幂等性保护：同一帧不重复消费 (防止 UI 刷新干扰 EMA 和静置时长)
        if (sample.sourceFrameId == lastSampleId && lastBatteryState != null) {
            return lastBatteryState!!
        }

        val now = sample.timestampMs
        val rawVoltage = sample.voltageV
        val rawCurrent = sample.busCurrentA

        // 1. 电压稳定性追踪 (用于判定是否处于回弹期)
        voltageWindow[voltageWindowIndex] = rawVoltage
        voltageWindowIndex = (voltageWindowIndex + 1) % voltageWindow.size
        if (voltageWindowPadding < voltageWindow.size) voltageWindowPadding++

        val isVoltageStable = if (voltageWindowPadding >= 4) {
            var minV = Float.MAX_VALUE
            var maxV = Float.MIN_VALUE
            for (i in 0 until voltageWindowPadding) {
                minV = minOf(minV, voltageWindow[i])
                maxV = maxOf(maxV, voltageWindow[i])
            }
            (maxV - minV) < 0.18f // 8 帧内波动小于 0.18V 则认为稳定 (适配采样抖动)
        } else false

        // 2. 电压电流平滑 (EMA)
        filteredVoltage = if (filteredVoltage.isNaN()) rawVoltage else ema(filteredVoltage, rawVoltage)
        filteredCurrent = if (filteredCurrent.isNaN()) rawCurrent else ema(filteredCurrent, rawCurrent)

        // 3. 内阻学习 (基于电流阶阶跃，增加物理稳定性校验)
        if (sample.allowLearning) {
            updateLearnedResistance(rawVoltage, rawCurrent, now, sample)
        }

        // 4. 计算补偿后的 OCV SoC
        val ocvCompensated = filteredVoltage + (filteredCurrent.coerceAtLeast(0f) * learnedInternalResistanceOhm)
        val socByOcv = socFromPackVoltage(ocvCompensated, batterySeries)

        // 5. 计算基于 Ah 积分的 SoC
        val capacityAh = batteryCapacityAh.coerceAtLeast(1f)
        val initialSoc = fallbackSocPercent?.takeIf { it in 1f..100f } ?: socByOcv
        
        if (baseSocPercent.isNaN()) {
            baseSocPercent = initialSoc
        }
        
        val netAh = accumulator.netBatteryAh
        val socByAh = (baseSocPercent - ((netAh / capacityAh) * 100.0)).toFloat().coerceIn(0f, 100f)

        // 6. 静置检测与融合 (除电流、速度、转速外，引入电压稳定性门控)
        val isStationary = abs(filteredCurrent) < STATIONARY_CURRENT_THRESHOLD_A &&
                sample.controllerSpeedKmH < 0.8f &&
                sample.rpm < 10f &&
                isVoltageStable
                
        if (isStationary) {
            if (stationarySinceMs == 0L) stationarySinceMs = now
        } else {
            stationarySinceMs = 0L
        }

        val stationaryDuration = if (stationarySinceMs > 0) now - stationarySinceMs else 0L
        val (wAh, wOcv) = getFusionWeights(abs(filteredCurrent), stationaryDuration)
        
        val fusedSoc = ((socByAh * wAh) + (socByOcv * wOcv)).coerceIn(0f, 100f)

        // 更新状态以供下次使用
        previousRawVoltage = rawVoltage
        previousRawCurrent = rawCurrent
        lastRawSpeed = sample.controllerSpeedKmH
        lastRawRpm = sample.rpm
        lastSampleAtMs = now
        lastSampleId = sample.sourceFrameId
        
        val result = BatteryState(
            socPercent = fusedSoc,
            socByAhPercent = socByAh,
            socByOcvPercent = socByOcv,
            filteredVoltage = filteredVoltage,
            filteredCurrent = filteredCurrent,
            learnedInternalResistanceOhm = learnedInternalResistanceOhm,
            isStationary = isStationary,
            confidence = calculateConfidence(isStationary, stationaryDuration)
        )
        lastBatteryState = result
        return result
    }

    private fun ema(prev: Float, curr: Float) = (EMA_ALPHA * curr) + ((1f - EMA_ALPHA) * prev)

    private fun updateLearnedResistance(rawV: Float, rawI: Float, now: Long, sample: TelemetrySample) {
        if (lastSampleAtMs == 0L) return
        val dt = now - lastSampleAtMs
        if (dt !in 50L..2000L) return

        // 内阻学习稳定窗校验 (防止急加速/急减速/机械制动瞬间的干扰)
        if (sample.braking) return
        if (abs(sample.controllerSpeedKmH - lastRawSpeed) > 1.5f) return
        if (abs(sample.rpm - lastRawRpm) > 50f) return

        val deltaI = rawI - previousRawCurrent
        if (abs(deltaI) < CURRENT_STEP_THRESHOLD_A) return

        val candidate = abs((previousRawVoltage - rawV) / deltaI)
        if (candidate !in 0.001f..1.0f) return

        learnedInternalResistanceOhm = if (learnedInternalResistanceOhm <= 0f) {
            candidate
        } else {
            (learnedInternalResistanceOhm * 0.85f + candidate * 0.15f)
        }
    }

    private fun getFusionWeights(absI: Float, stationaryMs: Long): Pair<Float, Float> {
        return when {
            absI < STATIONARY_CURRENT_THRESHOLD_A && stationaryMs >= OCV_SETTLED_MS -> 0.1f to 0.9f
            absI < STATIONARY_CURRENT_THRESHOLD_A && stationaryMs >= OCV_RECALIBRATION_MS -> 0.4f to 0.6f
            absI <= 2f -> 0.6f to 0.4f
            absI <= 8f -> 0.85f to 0.15f
            else -> 0.98f to 0.02f
        }
    }

    private fun calculateConfidence(isStationary: Boolean, stationaryMs: Long): Float {
        if (!isStationary) return 0.5f
        return (0.5f + (stationaryMs.toFloat() / OCV_SETTLED_MS) * 0.5f).coerceAtMost(1.0f)
    }

    private fun socFromPackVoltage(v: Float, series: Int): Float {
        val cellV = if (series > 0) v / series else 0f
        val table = listOf(
            4.20f to 100f, 4.15f to 96f, 4.10f to 91f, 4.05f to 85f, 4.00f to 78f,
            3.95f to 71f, 3.90f to 63f, 3.85f to 55f, 3.80f to 47f, 3.75f to 39f,
            3.70f to 31f, 3.65f to 24f, 3.60f to 17f, 3.55f to 12f, 3.50f to 8f,
            3.45f to 5f, 3.40f to 3f, 3.30f to 1f, 3.20f to 0f
        )
        if (cellV >= table.first().first) return table.first().second
        if (cellV <= table.last().first) return table.last().second
        table.zipWithNext().forEach { (hi, lo) ->
            if (cellV <= hi.first && cellV >= lo.first) {
                val p = (cellV - lo.first) / (hi.first - lo.first)
                return lo.second + (hi.second - lo.second) * p
            }
        }
        return 0f
    }
}
