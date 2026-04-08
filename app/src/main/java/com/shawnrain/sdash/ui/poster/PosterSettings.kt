package com.shawnrain.sdash.ui.poster

import com.shawnrain.sdash.ui.poster.model.PosterAspectRatio
import com.shawnrain.sdash.ui.poster.template.PerformanceDarkTemplate
import com.shawnrain.sdash.ui.poster.template.PosterTemplate

data class PosterSettings(
    val defaultAspectRatio: PosterAspectRatio = PosterAspectRatio.STORY_9_16,
    val defaultTemplate: String = PerformanceDarkTemplate.id,
    val showTrack: Boolean = true,
    val showWatermark: Boolean = true
)

object PosterTemplates {
    fun all(): List<PosterTemplate> = listOf(
        com.shawnrain.sdash.ui.poster.template.PerformanceDarkTemplate,
        com.shawnrain.sdash.ui.poster.template.RideMinimalTemplate,
        com.shawnrain.sdash.ui.poster.template.TrackFocusTemplate
    )

    fun byId(id: String): PosterTemplate =
        all().firstOrNull { it.id == id } ?: PerformanceDarkTemplate
}
