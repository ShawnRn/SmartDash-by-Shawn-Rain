package com.shawnrain.sdash.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

/**
 * Edge-to-edge dynamic blur for page headers.
 *
 * This deliberately has no container shape, border or shadow. The lower mask fades the sampled
 * content back into the page so the material reads as part of the screen rather than a component.
 */
@Composable
internal fun DynamicBlurTopBar(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithCache {
                    val fadeMask = Brush.verticalGradient(
                        0.00f to Color.Black,
                        0.76f to Color.Black,
                        1.00f to Color.Transparent
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(fadeMask, blendMode = BlendMode.DstIn)
                    }
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    // lens() requires a CornerBasedShape. A zero-radius shape keeps the
                    // edge-to-edge rectangular appearance without introducing a visible frame.
                    shape = { RoundedCornerShape(0.dp) },
                    effects = {
                        vibrancy()
                        // Same optical recipe as LiquidGlassBottomBar. Each page source is opaque,
                        // so the blurred sample replaces the sharp content instead of overlaying it.
                        blur(8.dp.toPx())
                        lens(24.dp.toPx(), 24.dp.toPx())
                    },
                    onDrawSurface = {
                        drawRect(
                            if (darkTheme) {
                                Color(0xFF121212).copy(alpha = 0.40f)
                            } else {
                                Color(0xFFFAFAFA).copy(alpha = 0.40f)
                            }
                        )
                    }
                )
        )
        content()
    }
}
