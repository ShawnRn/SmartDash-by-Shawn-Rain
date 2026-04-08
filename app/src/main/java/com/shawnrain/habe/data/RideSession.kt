package com.shawnrain.habe.data

data class RideSession(
    val date: String = "",
    val distanceKm: Float = 0.0f,
    val durationMin: Int = 0,
    val maxSpeed: Float = 0.0f,
    val avgSpeed: Float = 0.0f,
    val totalWh: Float = 0.0f,
    val avgEfficiency: Float = 0.0f
)
