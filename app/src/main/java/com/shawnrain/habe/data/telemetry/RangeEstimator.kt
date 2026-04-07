package com.shawnrain.habe.data.telemetry

import kotlin.math.max

/**
 * 续航/效率估计器
 * 管理滑动窗口效率，并结合当前电池状态计算剩余里程
 */
class RangeEstimator {
    companion object {
        private const val RANGE_WINDOW_DISTANCE_KM = 2.0
        private const val MIN_RANGE_WINDOW_DISTANCE_KM = 0.2
        private const val NOMINAL_CELL_VOLTAGE = 3.7f
    }

    private data class RangeWindowSample(
        val distanceKm: Double,
        val energyWh: Double
    )

    private val rangeWindow = ArrayDeque<RangeWindowSample>()
    private var rangeWindowDistanceKm = 0.0
    private var rangeWindowEnergyWh = 0.0
    
    private var lastDistanceKm = 0.0
    private var lastTractionWh = 0.0

    fun reset() {
        rangeWindow.clear()
        rangeWindowDistanceKm = 0.0
        rangeWindowEnergyWh = 0.0
        lastDistanceKm = 0.0
        lastTractionWh = 0.0
    }

    fun estimate(
        batteryState: BatteryState,
        accumulator: RideAccumulatorState,
        batterySeries: Int,
        batteryCapacityAh: Float,
        usableEnergyRatio: Float
    ): RangeEstimate {
        // 1. 更新滑动窗口 (基于 Traction Wh, 不计入回充能量以降级到真实能耗口径)
        val currentDistanceKm = accumulator.totalDistanceMeters / 1000.0
        val currentTractionWh = accumulator.tractionEnergyWh.toDouble()
        
        val deltaDistanceKm = (currentDistanceKm - lastDistanceKm).coerceAtLeast(0.0)
        val deltaTractionWh = (currentTractionWh - lastTractionWh).coerceAtLeast(0.0)
        
        if (deltaDistanceKm > 0.001) {
            rangeWindow.addLast(
                RangeWindowSample(
                    distanceKm = deltaDistanceKm,
                    energyWh = deltaTractionWh
                )
            )
            rangeWindowDistanceKm += deltaDistanceKm
            rangeWindowEnergyWh += deltaTractionWh
            trimRangeWindow()
        }
        
        lastDistanceKm = currentDistanceKm
        lastTractionWh = currentTractionWh

        // 2. 计算窗口效率
        val avgEfficiencyWhKm = if (rangeWindowDistanceKm >= MIN_RANGE_WINDOW_DISTANCE_KM) {
            (rangeWindowEnergyWh / rangeWindowDistanceKm).toFloat()
        } else {
            0f
        }

        // 3. 计算剩余能量与里程
        val nominalPackVoltage = batterySeries * NOMINAL_CELL_VOLTAGE
        val totalNominalWh = batteryCapacityAh * nominalPackVoltage
        val remainingWh = (batteryState.socPercent / 100f) * totalNominalWh * usableEnergyRatio.coerceIn(0.7f, 1.0f)
        
        val rangeKm = if (avgEfficiencyWhKm > 0.1f) {
            (remainingWh / avgEfficiencyWhKm).coerceAtLeast(0f)
        } else {
            0f
        }

        return RangeEstimate(
            estimatedRangeKm = rangeKm,
            remainingEnergyWh = remainingWh,
            averageWindowEfficiencyWhKm = avgEfficiencyWhKm,
            isWindowFresh = rangeWindowDistanceKm >= MIN_RANGE_WINDOW_DISTANCE_KM
        )
    }

    private fun trimRangeWindow() {
        while (rangeWindowDistanceKm > RANGE_WINDOW_DISTANCE_KM && rangeWindow.isNotEmpty()) {
            val overflowKm = rangeWindowDistanceKm - RANGE_WINDOW_DISTANCE_KM
            val first = rangeWindow.removeFirst()
            if (first.distanceKm <= overflowKm + 1e-6) {
                rangeWindowDistanceKm -= first.distanceKm
                rangeWindowEnergyWh -= first.energyWh
            } else {
                val keepDistanceKm = first.distanceKm - overflowKm
                val keepEnergyWh = if (first.distanceKm > 0.0) {
                    first.energyWh * (keepDistanceKm / first.distanceKm)
                } else {
                    0.0
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
