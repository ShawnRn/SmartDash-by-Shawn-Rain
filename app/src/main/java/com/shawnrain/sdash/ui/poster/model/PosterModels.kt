package com.shawnrain.sdash.ui.poster.model

import com.shawnrain.sdash.data.history.RideTrackPoint

enum class PosterAspectRatio(
    val width: Int,
    val height: Int
) {
    STORY_9_16(1080, 1920),
    PORTRAIT_4_5(1080, 1350),
    SQUARE_1_1(1080, 1080)
}

enum class PosterTheme(
    val backgroundTop: Int,
    val backgroundBottom: Int,
    val accent: Int,
    val accentSecondary: Int,
    val cardFill: Int,
    val cardStroke: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val routeGrid: Int
) {
    PERFORMANCE_DARK(
        backgroundTop = 0xFF08101CL.toInt(),
        backgroundBottom = 0xFF151C2CL.toInt(),
        accent = 0xFF9FD4FF.toInt(),
        accentSecondary = 0xFF7EE787.toInt(),
        cardFill = 0xE61F283A.toInt(),
        cardStroke = 0xFF43506A.toInt(),
        textPrimary = 0xFFFFFFFF.toInt(),
        textSecondary = 0xFFB5C0D8.toInt(),
        routeGrid = 0x1FFFFFFF.toInt()
    ),
    RIDE_MINIMAL(
        backgroundTop = 0xFFF3EEDC.toInt(),
        backgroundBottom = 0xFFD7E3D0.toInt(),
        accent = 0xFF1E5A5A.toInt(),
        accentSecondary = 0xFFE58B5B.toInt(),
        cardFill = 0xF0FFF9F1.toInt(),
        cardStroke = 0x33203A3A,
        textPrimary = 0xFF14261F.toInt(),
        textSecondary = 0xFF4E625B.toInt(),
        routeGrid = 0x1A204040
    ),
    TRACK_FOCUS(
        backgroundTop = 0xFF10233B.toInt(),
        backgroundBottom = 0xFF0B121C.toInt(),
        accent = 0xFF5AD1E6.toInt(),
        accentSecondary = 0xFFFFCC66.toInt(),
        cardFill = 0xE6142235.toInt(),
        cardStroke = 0xFF38516B.toInt(),
        textPrimary = 0xFFFFFFFF.toInt(),
        textSecondary = 0xFFB4C7D8.toInt(),
        routeGrid = 0x26FFFFFF
    )
}

data class PosterMetric(
    val key: String,
    val label: String,
    val value: String,
    val unit: String? = null,
    val priority: Int = 0
)

data class PosterTrackData(
    val title: String,
    val subtitle: String,
    val points: List<RideTrackPoint>
)

data class PosterFooter(
    val line: String = "Made with SmartDash by Shawn Rain",
    val watermark: String? = null
)

data class PosterRenderOptions(
    val showTrack: Boolean = true,
    val showWatermark: Boolean = true,
    val emphasizeTrack: Boolean = false,
    val compactMetrics: Boolean = false
)

data class PosterSpec(
    val aspectRatio: PosterAspectRatio,
    val theme: PosterTheme,
    val title: String,
    val subtitle: String?,
    val heroMetric: PosterMetric,
    val metrics: List<PosterMetric>,
    val track: PosterTrackData?,
    val footer: PosterFooter = PosterFooter(),
    val options: PosterRenderOptions = PosterRenderOptions()
)
