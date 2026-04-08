package com.shawnrain.habe.data.telemetry

/**
 * 骑行累加器的状态快照
 */
data class RideAccumulatorState(
    val tractionEnergyWh: EnergyWh = 0.0f,
    val regenEnergyWh: EnergyWh = 0.0f,
    val netBatteryEnergyWh: EnergyWh = 0.0f,
    val tractionAh: EnergyAh = 0.0f,
    val netBatteryAh: EnergyAh = 0.0f,
    val peakDrivePowerKw: Float = 0.0f,
    val peakRegenPowerKw: Float = 0.0f,
    val maxControllerTempC: Float = 0.0f,
    val maxMotorTempC: Float = 0.0f,
    val totalDistanceMeters: Float = 0.0f,
    val maxSpeedKmh: Float = 0.0f,
    val totalSpeedSum: Float = 0.0f,
    val movingTimeMs: Long = 0L,
    val sampleCount: Int = 0,
    val integrationCount: Int = 0
)
