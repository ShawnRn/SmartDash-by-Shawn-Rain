package com.shawnrain.sdash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.toIntSize
import com.kyant.backdrop.Backdrop

@Composable
internal fun rememberFreezableLayerBackdrop(): FreezableLayerBackdrop {
    val graphicsLayer = rememberGraphicsLayer()
    return remember(graphicsLayer) { FreezableLayerBackdrop(graphicsLayer) }
}

@Stable
internal class FreezableLayerBackdrop(
    internal val graphicsLayer: GraphicsLayer
) : Backdrop {
    internal var layerCoordinates: LayoutCoordinates? = null

    override val isCoordinatesDependent: Boolean = true

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?
    ) {
        val targetCoordinates = coordinates ?: return
        val sourceCoordinates = layerCoordinates ?: return
        val offset = try {
            sourceCoordinates.localPositionOf(targetCoordinates)
        } catch (_: Exception) {
            targetCoordinates.positionInWindow() - sourceCoordinates.positionInWindow()
        }
        withTransform({ translate(-offset.x, -offset.y) }) {
            drawLayer(graphicsLayer)
        }
    }
}

internal fun Modifier.freezableLayerBackdrop(
    backdrop: FreezableLayerBackdrop,
    frozen: Boolean
): Modifier =
    onGloballyPositioned { coordinates ->
        if (coordinates.isAttached) {
            backdrop.layerCoordinates = coordinates
        }
    }.drawWithContent {
        drawContent()
        if (!frozen) {
            backdrop.graphicsLayer.record(size.toIntSize()) {
                this@drawWithContent.drawContent()
            }
        }
    }
