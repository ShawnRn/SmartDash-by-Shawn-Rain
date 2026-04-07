package com.shawnrain.habe.data.telemetry

/**
 * 骑行累加器的状态快照
 */
data class RideAccumulatorState(
    val tractionEnergyWh: Float = 0f,
    val regenEnergyWh: Float = 0f,
    val netBatteryEnergyWh: Float = 0f,
    val tractionAh: Float = 0f,
    val netBatteryAh: Float = 0f,
    val peakDrivePowerKw: Float = 0f,
    val peakRegenPowerKw: Float = 0f,
    val maxControllerTempC: Float = 0f,
    val maxMotorTempC: Float = 0f,
    val totalDistanceMeters: Double = 0.0,
    val maxSpeedKmh: Float = 0f,
    val totalSpeedSum: Float = 0f,
    val movingTimeMs: Long = 0L,
    val sampleCount: Int = 0,
    val integrationCount: Int = 0
)
