package com.shawnrain.sdash.ui.poster.template

import com.shawnrain.sdash.ui.poster.model.PosterTheme

interface PosterTemplate {
    val id: String
    val displayName: String
    val description: String
    val theme: PosterTheme
    val showTrackFirst: Boolean
    val compactMetrics: Boolean
}

object PerformanceDarkTemplate : PosterTemplate {
    override val id: String = "performance_dark"
    override val displayName: String = "性能海报"
    override val description: String = "强调速度、功率和科技感"
    override val theme: PosterTheme = PosterTheme.PERFORMANCE_DARK
    override val showTrackFirst: Boolean = false
    override val compactMetrics: Boolean = false
}

object RideMinimalTemplate : PosterTemplate {
    override val id: String = "ride_minimal"
    override val displayName: String = "简约行程"
    override val description: String = "更柔和，突出里程与平均表现"
    override val theme: PosterTheme = PosterTheme.RIDE_MINIMAL
    override val showTrackFirst: Boolean = false
    override val compactMetrics: Boolean = true
}

object TrackFocusTemplate : PosterTemplate {
    override val id: String = "track_focus"
    override val displayName: String = "轨迹优先"
    override val description: String = "放大路线轨迹与出行氛围"
    override val theme: PosterTheme = PosterTheme.TRACK_FOCUS
    override val showTrackFirst: Boolean = true
    override val compactMetrics: Boolean = false
}
