package com.shawnrain.sdash.ui.poster.template

import com.shawnrain.sdash.ui.poster.model.PosterTheme

interface PosterTemplate {
    val id: String
    val theme: PosterTheme
    val showTrackFirst: Boolean
    val compactMetrics: Boolean
}

object PerformanceDarkTemplate : PosterTemplate {
    override val id: String = "performance_dark"
    override val theme: PosterTheme = PosterTheme.PERFORMANCE_DARK
    override val showTrackFirst: Boolean = false
    override val compactMetrics: Boolean = false
}

object RideMinimalTemplate : PosterTemplate {
    override val id: String = "ride_minimal"
    override val theme: PosterTheme = PosterTheme.RIDE_MINIMAL
    override val showTrackFirst: Boolean = false
    override val compactMetrics: Boolean = true
}

object TrackFocusTemplate : PosterTemplate {
    override val id: String = "track_focus"
    override val theme: PosterTheme = PosterTheme.TRACK_FOCUS
    override val showTrackFirst: Boolean = true
    override val compactMetrics: Boolean = false
}
