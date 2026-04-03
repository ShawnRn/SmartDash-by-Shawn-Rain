package com.shawnrain.habe.data

data class RideSession(
    val date: String = "",
    val distanceKm: Double = 0.0,
    val durationMin: Int = 0,
    val maxSpeed: Float = 0f,
    val avgSpeed: Float = 0f,
    val totalWh: Float = 0f,
    val avgEfficiency: Float = 0f
)
