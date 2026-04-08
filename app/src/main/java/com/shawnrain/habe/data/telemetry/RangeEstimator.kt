package com.shawnrain.habe.data.telemetry

import kotlin.math.max

/**
 * 续航/效率估计器
 * 管理滑动窗口效率，并结合当前电池状态计算剩余里程
 */
class RangeEstimator {
    companion object {
        private const val RANGE_WINDOW_DISTANCE_KM = 2.0f
        private const val MIN_RANGE_WINDOW_DISTANCE_KM = 0.2f
        private const val NOMINAL_CELL_VOLTAGE = 3.7f
    }

    private data class RangeWindowSample(
        val distanceKm: Float,
        val energyWh: Float
    )

    private val rangeWindow = ArrayDeque<RangeWindowSample>()
    private var rangeWindowDistanceKm = 0.0f
    private var rangeWindowEnergyWh = 0.0f

    private var lastDistanceKm = 0.0f
    private var lastNetWh: EnergyWh = 0.0f

    // 数据质量追踪
    private val qualityWindow = ArrayDeque<SampleQuality>(20)

    fun reset() {
        rangeWindow.clear()
        rangeWindowDistanceKm = 0.0f
        rangeWindowEnergyWh = 0.0f
        lastDistanceKm = 0.0f
        lastNetWh = 0.0f
        qualityWindow.clear()
    }

    fun estimate(
        sample: TelemetrySample,
        batteryState: BatteryState,
        accumulator: RideAccumulatorState,
        batterySeries: Int,
        batteryCapacityAh: Float,
        usableEnergyRatio: Float
    ): RangeEstimate {
        // 1. 更新滑动窗口 (基于 Net Wh, 即 Traction - Regen, 更贴合电池真实的能量循环)
        val currentDistanceKm = accumulator.totalDistanceMeters / 1000.0f
        val currentNetWh = accumulator.netBatteryEnergyWh

        val deltaDistanceKm = (currentDistanceKm - lastDistanceKm).coerceAtLeast(0.0f)
        // 允许负值 (Regen 贡献)，从而让窗口平均值反映真实的 Net 能耗
        val deltaNetWh = currentNetWh - lastNetWh

        if (deltaDistanceKm > 0.001f) {
            rangeWindow.addLast(
                RangeWindowSample(
                    distanceKm = deltaDistanceKm,
                    energyWh = deltaNetWh
                )
            )
            rangeWindowDistanceKm += deltaDistanceKm
            rangeWindowEnergyWh += deltaNetWh
            trimRangeWindow()
        }

        // 1.5 追踪质量分布
        qualityWindow.addLast(sample.quality)
        if (qualityWindow.size > 20) qualityWindow.removeFirst()

        lastDistanceKm = currentDistanceKm
        lastNetWh = currentNetWh

        // 2. 计算窗口效率 (United Energy Standard: 强制使用 Net Wh)
        val avgEfficiencyWhKm: EfficiencyWhKm = if (rangeWindowDistanceKm >= MIN_RANGE_WINDOW_DISTANCE_KM) {
            rangeWindowEnergyWh / rangeWindowDistanceKm
        } else {
            0.0f
        }

        // 3. 计算置信度 (综合窗口距离、电池状态置信度、以及遥测物理质量)
        val goodSamples = qualityWindow.count { it == SampleQuality.GOOD }
        val recentGapResets = qualityWindow.count { it == SampleQuality.GAP_RESET }
        val dataHealthRatio = goodSamples.toFloat() / max(1, qualityWindow.size)

        val confidence = when {
            batteryState.confidence < 0.1f -> RangeConfidence.LOW // 电压极度不稳定
            dataHealthRatio < 0.5f -> RangeConfidence.LOW         // 采样抖动剧烈
            recentGapResets > 2 -> RangeConfidence.LOW           // 频繁断连
            rangeWindowDistanceKm < MIN_RANGE_WINDOW_DISTANCE_KM -> RangeConfidence.LOW
            rangeWindowDistanceKm < 1.0f || dataHealthRatio < 0.8f -> RangeConfidence.MEDIUM
            else -> RangeConfidence.HIGH
        }

        // 4. 计算剩余能量与里程
        val nominalPackVoltage = batterySeries * NOMINAL_CELL_VOLTAGE
        val totalNominalWh: EnergyWh = batteryCapacityAh * nominalPackVoltage
        val remainingWh: EnergyWh = (batteryState.socPercent / 100.0f) * totalNominalWh * usableEnergyRatio.coerceIn(0.7f, 1.0f)

        val rangeKm = if (avgEfficiencyWhKm > 0.1f) {
            (remainingWh / avgEfficiencyWhKm).coerceAtLeast(0.0f)
        } else {
            0.0f
        }

        return RangeEstimate(
            estimatedRangeKm = rangeKm,
            remainingEnergyWh = remainingWh,
            averageWindowEfficiencyWhKm = avgEfficiencyWhKm,
            isWindowFresh = rangeWindowDistanceKm >= MIN_RANGE_WINDOW_DISTANCE_KM,
            confidence = confidence
        )
    }

    private fun trimRangeWindow() {
        while (rangeWindowDistanceKm > RANGE_WINDOW_DISTANCE_KM && rangeWindow.isNotEmpty()) {
            val overflowKm = rangeWindowDistanceKm - RANGE_WINDOW_DISTANCE_KM
            val first = rangeWindow.removeFirst()
            if (first.distanceKm <= overflowKm + 1e-6f) {
                rangeWindowDistanceKm -= first.distanceKm
                rangeWindowEnergyWh -= first.energyWh
            } else {
                val keepDistanceKm = first.distanceKm - overflowKm
                val keepEnergyWh = if (first.distanceKm > 0.0f) {
                    first.energyWh * (keepDistanceKm / first.distanceKm)
                } else {
                    0.0f
                }
                rangeWindowDistanceKm -= overflowKm
                rangeWindowEnergyWh -= (first.energyWh - keepEnergyWh)
                rangeWindow.addFirst(
                    RangeWindowSample(
                        distanceKm = keepDistanceKm,
                        energyWh = keepEnergyWh
                    )
                )
            }
        }
    }
}
