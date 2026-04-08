package com.shawnrain.habe.data.telemetry

import kotlin.math.abs

/**
 * 负责基于 TelemetrySample 进行物理积分累计。
 * 严格遵循：只在 allowIntegration 为 true 时更新累计量。
 */
class RideAccumulator {
    private var _state = RideAccumulatorState()
    val state: RideAccumulatorState get() = _state

    fun accumulate(sample: TelemetrySample) {
        val nextMaxSpeed = maxOf(_state.maxSpeedKmh, sample.controllerSpeedKmH)
        val nextTotalSpeedSum = _state.totalSpeedSum + sample.controllerSpeedKmH
        
        _state = _state.copy(
            sampleCount = _state.sampleCount + 1,
            maxSpeedKmh = nextMaxSpeed,
            totalSpeedSum = nextTotalSpeedSum,
            maxControllerTempC = maxOf(_state.maxControllerTempC, sample.controllerTempC),
            maxMotorTempC = maxOf(_state.maxMotorTempC, sample.motorTempC)
        )

        if (!sample.allowIntegration) return

        val powerW = sample.voltageV * sample.busCurrentA
        val dtHours = sample.dtMs / 3600000.0
        
        var tractionWh = _state.tractionEnergyWh
        var regenWh = _state.regenEnergyWh
        var tractionAh = _state.tractionAh
        var netAh = _state.netBatteryAh
        var peakDriveKw = _state.peakDrivePowerKw
        var peakRegenKw = _state.peakRegenPowerKw

        // 净 Ah 始终累计 (放电为正，回收为负)
        netAh += (sample.busCurrentA * dtHours).toFloat()

        if (powerW >= 0f) {
            tractionWh += (powerW * dtHours).toFloat()
            tractionAh += (sample.busCurrentA * dtHours).toFloat()
            peakDriveKw = maxOf(peakDriveKw, powerW / 1000f)
        } else {
            regenWh += (abs(powerW) * dtHours).toFloat()
            peakRegenKw = maxOf(peakRegenKw, abs(powerW) / 1000f)
        }

        val netWh = tractionWh - regenWh
        
        // 增加速度门槛防止静止噪音漂移
        val deltaDistance = if (sample.controllerSpeedKmH > 0.8f) {
            (sample.controllerSpeedKmH * dtHours * 1000.0)
        } else {
            0.0
        }
        val distanceMeters = _state.totalDistanceMeters + deltaDistance
        
        val movingTimeMs = if (sample.controllerSpeedKmH > 1.0f) {
            _state.movingTimeMs + sample.dtMs
        } else {
            _state.movingTimeMs
        }

        _state = _state.copy(
            tractionEnergyWh = tractionWh,
            regenEnergyWh = regenWh,
            netBatteryEnergyWh = netWh,
            tractionAh = tractionAh,
            netBatteryAh = netAh,
            peakDrivePowerKw = peakDriveKw,
            peakRegenPowerKw = peakRegenKw,
            totalDistanceMeters = distanceMeters,
            movingTimeMs = movingTimeMs,
            integrationCount = _state.integrationCount + 1
        )
    }

    fun reset() {
        _state = RideAccumulatorState()
    }
}
