package com.shawnrain.sdash.data.dashcam

import android.net.Uri

data class DashcamSegment(
    val id: String,                    // UUID
    val videoUri: Uri,                 // 视频文件 URI
    val sidecarUri: Uri?,              // 遥测 sidecar JSON URI
    val startedAtMs: Long,             // 开始时间戳
    val endedAtMs: Long,               // 结束时间戳
    val durationMs: Long,              // 时长
    val fileSizeBytes: Long,           // 文件大小
    val rideId: String?                // 关联的行程 ID
)
