package com.shawnrain.habe.data.telemetry

import com.shawnrain.habe.ble.VehicleMetrics
import com.shawnrain.habe.data.history.RideHistoryRecord
import com.shawnrain.habe.data.history.RideMetricSample

/**
 * 遥测回放引擎：用于从历史记录或 CSV 样本中重构估算链路。
 * 允许在不发生真实骑行的情况下，验证算法变更对 SoC、续航和能效的影响。
 */
class TelemetryReplayRunner() {
    private val processor = TelemetryStreamProcessor()
    private val accumulator = RideAccumulator()
    private val batteryEstimator = BatteryStateEstimator()
    private val rangeEstimator = RangeEstimator()

    /**
     * 回放报告：包含重构后的终点指标
     */
    data class ReplayReport(
        val totalDistanceMeters: Float,
        val netEnergyWh: Float,
        val avgEfficiencyWhKm: Float,
        val startSoc: Float,
        val endSoc: Float,
        val socDrop: Float,
        val sampleCount: Int,
        val processedSamples: List<TelemetrySample>
    )

    fun replay(record: RideHistoryRecord, profile: com.shawnrain.habe.data.VehicleProfile): ReplayReport {
        reset()
        val processed = mutableListOf<TelemetrySample>()
        
        // 按照时间戳排序，确保回放顺序正确
        val sortedSamples = record.samples.sortedBy { it.timestampMs }
        
        var firstSoc = 0f
        var lastSoc = 0f

        val batteryCapacityAh = profile.batteryCapacityAh
        val batterySeries = profile.batterySeries
        val usableEnergyRatio = profile.learnedUsableEnergyRatio

        sortedSamples.forEachIndexed { index, raw ->
            // 将历史样本还原为 VehicleMetrics 喂入处理器
            val metrics = toVehicleMetrics(raw)
            val sample = processor.process(metrics, raw.timestampMs)
            processed.add(sample)
            
            // 物理累计
            accumulator.accumulate(sample)
            
            // 估算
            val batteryState = batteryEstimator.estimate(
                sample = sample,
                accumulator = accumulator.state,
                batteryCapacityAh = batteryCapacityAh,
                batterySeries = batterySeries,
                fallbackSocPercent = null
            )
            
            rangeEstimator.estimate(
                sample = sample,
                batteryState = batteryState,
                accumulator = accumulator.state,
                batterySeries = batterySeries,
                batteryCapacityAh = batteryCapacityAh,
                usableEnergyRatio = usableEnergyRatio
            )

            if (index == 0) firstSoc = batteryState.socPercent
            lastSoc = batteryState.socPercent
        }

        val accState = accumulator.state
        return ReplayReport(
            totalDistanceMeters = accState.totalDistanceMeters,
            netEnergyWh = accState.netBatteryEnergyWh,
            avgEfficiencyWhKm = if (accState.totalDistanceMeters > 50f) {
                accState.netBatteryEnergyWh / (accState.totalDistanceMeters / 1000f)
            } else 0f,
            startSoc = firstSoc,
            endSoc = lastSoc,
            socDrop = firstSoc - lastSoc,
            sampleCount = sortedSamples.size,
            processedSamples = processed
        )
    }

    private fun reset() {
        processor.reset()
        accumulator.reset()
        batteryEstimator.reset()
        rangeEstimator.reset()
    }

    private fun toVehicleMetrics(raw: RideMetricSample): VehicleMetrics {
        return VehicleMetrics(
            speedKmH = raw.speedKmH ?: 0f,
            voltage = raw.voltage,
            busCurrent = raw.busCurrent ?: 0f,
            phaseCurrent = raw.phaseCurrent ?: 0f,
            rpm = raw.rpm ?: 0f,
            controllerTemp = raw.controllerTemp ?: 0f,
            mosfetTemp = raw.controllerTemp ?: 0f,
            isBraking = false,
            isCruise = false,
            voltageSag = raw.voltageSag ?: 0f,
            soc = raw.soc ?: 0f,
            estimatedRangeKm = raw.estimatedRangeKm ?: 0f,
            efficiencyWhKm = raw.avgNetEfficiencyWhKm,
            avgEfficiencyWhKm = raw.avgNetEfficiencyWhKm,
            totalEnergyWh = raw.totalEnergyWh,
            recoveredEnergyWh = raw.recoveredEnergyWh
        )
    }
}
