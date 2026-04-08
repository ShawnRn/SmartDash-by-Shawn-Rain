package com.shawnrain.sdash.ui.poster

import android.content.Context
import android.graphics.Bitmap
import com.shawnrain.sdash.data.RideSession
import com.shawnrain.sdash.data.history.RideHistoryRecord
import com.shawnrain.sdash.data.speedtest.SpeedTestRecord

class PosterRenderer(private val context: Context) {
    private val factory = PosterFactory()
    private val rendererV2 = PosterRendererV2(context)

    fun render(session: RideSession): Bitmap =
        rendererV2.render(factory.buildRidePosterSpec(session))

    fun renderSpeedTest(record: SpeedTestRecord): Bitmap =
        rendererV2.render(factory.buildSpeedTestPosterSpec(record))

    fun renderRideHistory(record: RideHistoryRecord): Bitmap =
        rendererV2.render(factory.buildRidePosterSpec(record))
}
