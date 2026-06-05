package com.shawnrain.sdash.data.dashcam

import kotlinx.serialization.Serializable

@Serializable
data class DashcamTelemetrySample(
    val offsetMs: Long,       // 相对视频起始的时间偏移
    val speedKmH: Float? = null,
    val powerKw: Float? = null,
    val direction: String? = null,
    val voltage: Float? = null,
    val soc: Float? = null,
    val efficiency: Float? = null
)
