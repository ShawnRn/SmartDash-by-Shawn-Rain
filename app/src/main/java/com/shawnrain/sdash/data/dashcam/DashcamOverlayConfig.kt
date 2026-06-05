package com.shawnrain.sdash.data.dashcam

import kotlinx.serialization.Serializable

@Serializable
data class DashcamOverlayConfig(
    val showSpeed: Boolean = true,
    val showTime: Boolean = true,
    val showDirection: Boolean = true,
    val showPower: Boolean = false,
    val showEfficiency: Boolean = false,
    val showVoltage: Boolean = false,
    val showSoc: Boolean = false
)
