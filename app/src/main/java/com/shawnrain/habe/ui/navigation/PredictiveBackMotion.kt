package com.shawnrain.habe.ui.navigation

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import java.util.concurrent.CancellationException

data class PredictiveBackMotionState(
    val progress: Float,
    val translationX: Float,
    val alpha: Float,
    val insetHorizontal: Dp,
    val insetVertical: Dp,
    val corner: Dp,
    val swipeEdge: Int
)

@Composable
fun rememberPredictiveBackMotion(
    width: Dp,
    onBack: () -> Unit,
    maxHorizontalInset: Dp = 18.dp,
    maxVerticalInset: Dp = 10.dp,
    maxCorner: Dp = 28.dp,
    maxScaleTravelFraction: Float = 0.16f
): PredictiveBackMotionState {
    val progress = remember { Animatable(0f) }
    var swipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }
    val density = LocalDensity.current
    val widthPx = remember(width, density) { with(density) { width.toPx() } }

    PredictiveBackHandler(enabled = true) { backEvents ->
        try {
            progress.snapTo(0f)
            backEvents.collect { event ->
                swipeEdge = event.swipeEdge
                progress.snapTo(event.progress)
            }
            onBack()
        } catch (_: CancellationException) {
            progress.animateTo(0f, animationSpec = tween(180))
        }
    }

    val animatedProgress = progress.value
    val direction = if (swipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
    return PredictiveBackMotionState(
        progress = animatedProgress,
        translationX = direction * widthPx * maxScaleTravelFraction * animatedProgress,
        alpha = 1f - (0.08f * animatedProgress),
        insetHorizontal = maxHorizontalInset * animatedProgress,
        insetVertical = maxVerticalInset * animatedProgress,
        corner = maxCorner * animatedProgress,
        swipeEdge = swipeEdge
    )
}

@Composable
fun PredictiveBackPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val motion = rememberPredictiveBackMotion(width = maxWidth, onBack = onBack)
        val direction = if (motion.swipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
        val scale = 1f - (0.1f * motion.progress)
        val transformOrigin = if (direction > 0f) {
            TransformOrigin(0f, 0.5f)
        } else {
            TransformOrigin(1f, 0.5f)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = motion.insetHorizontal, vertical = motion.insetVertical)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = motion.translationX
                        alpha = motion.alpha
                        this.transformOrigin = transformOrigin
                    },
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(motion.corner),
                shadowElevation = 20.dp * motion.progress,
                tonalElevation = 0.dp
            ) {
                content()
            }
        }
    }
}

@Composable
fun PredictiveBackPopupTransform(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    maxHorizontalInset: Dp = 8.dp,
    maxVerticalInset: Dp = 6.dp,
    maxCorner: Dp = 20.dp,
    maxScaleTravelFraction: Float = 0.08f,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val motion = rememberPredictiveBackMotion(
            width = maxWidth,
            onBack = onBack,
            maxHorizontalInset = maxHorizontalInset,
            maxVerticalInset = maxVerticalInset,
            maxCorner = maxCorner,
            maxScaleTravelFraction = maxScaleTravelFraction
        )
        val scale = 1f - (0.05f * motion.progress)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = motion.insetHorizontal, vertical = motion.insetVertical)
                .graphicsLayer {
                    translationX = motion.translationX
                    scaleX = scale
                    scaleY = scale
                    alpha = motion.alpha
                }
        ) {
            content()
        }
    }
}
