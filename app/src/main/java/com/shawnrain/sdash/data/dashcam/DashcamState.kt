package com.shawnrain.sdash.data.dashcam

enum class DashcamState {
    IDLE,        // 未初始化 / 已停止
    PREVIEWING,  // 预览中，未录制
    RECORDING,   // 录制中
    SEGMENT_GAP, // 分段切换间隙（~200ms）
    ERROR        // 初始化或录制出错
}
