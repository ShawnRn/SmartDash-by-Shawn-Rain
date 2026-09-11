package com.shawnrain.sdash.ui.navigation

/*
 * Optical styling and layered tab sampling are adapted from AndroidLiquidGlass 1.0.3
 * LiquidBottomTabs and Mishka's LiquidGlassNavigationBar.
 * Copyright 2025 Kyant. Licensed under the Apache License, Version 2.0.
 * Copyright 2026 compose-miuix-ui contributors. Licensed under the Apache License, Version 2.0.
 * SmartDash keeps the moving glass on one stable GPU layer for high-refresh devices.
 */

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.capsule.ContinuousCapsule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

data class LiquidGlassBottomBarItem(
    val title: String,
    val icon: ImageVector
)

private fun stabilizedTabIndex(
    position: Float,
    currentIndex: Int,
    lastIndex: Int
): Int {
    var result = currentIndex.coerceIn(0, lastIndex)
    while (result < lastIndex && position >= result + 0.58f) result++
    while (result > 0 && position <= result - 0.58f) result--
    return result
}

@Composable
fun LiquidGlassBottomBar(
    items: List<LiquidGlassBottomBarItem>,
    selectedIndex: Int,
    backdrop: Backdrop,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val darkTheme = isSystemInDarkTheme()
    val accentColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
    val containerColor = if (darkTheme) {
        Color(0xFF121212).copy(alpha = 0.40f)
    } else {
        Color(0xFFFAFAFA).copy(alpha = 0.40f)
    }
    val haptic = LocalHapticFeedback.current
    val latestOnItemSelected by rememberUpdatedState(onItemSelected)
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val animationScope = rememberCoroutineScope()
    val tabsBackdrop = rememberLayerBackdrop()
    val combinedBackdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop)
    var panelDragPx by remember { mutableFloatStateOf(0f) }
    var panelReturnJob by remember { mutableStateOf<Job?>(null) }
    var committedIndex by remember(items.size) {
        mutableIntStateOf(selectedIndex.coerceIn(0, items.lastIndex))
    }
    var visualIndex by remember(items.size) { mutableIntStateOf(committedIndex) }
    val motion = remember(animationScope, items.size) {
        LiquidGlassTabMotionState(
            animationScope = animationScope,
            initialPosition = committedIndex.toFloat(),
            positionRange = 0f..items.lastIndex.toFloat()
        )
    }

    LaunchedEffect(selectedIndex) {
        val externalIndex = selectedIndex.coerceIn(0, items.lastIndex)
        if (externalIndex != committedIndex) {
            committedIndex = externalIndex
            visualIndex = externalIndex
            motion.syncTo(externalIndex.toFloat())
        }
    }

    BoxWithConstraints(
        modifier = modifier.height(64.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val itemWidth = (maxWidth - 8.dp) / items.size
        val itemWidthPx = with(density) { itemWidth.toPx() }.coerceAtLeast(1f)
        val contentStartPx = with(density) { 4.dp.toPx() }
        val totalWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
        val rubberBandPx = with(density) { 4.dp.toPx() }
        val panelOffsetPx by remember(totalWidthPx, rubberBandPx) {
            derivedStateOf {
                val fraction = (panelDragPx / totalWidthPx).coerceIn(-1f, 1f)
                rubberBandPx * fraction.sign * EaseOut.transform(abs(fraction))
            }
        }
        val interactiveHighlight = remember(animationScope, motion, itemWidthPx, totalWidthPx, isLtr) {
            LiquidGlassInteractiveHighlight(animationScope) { size ->
                Offset(
                    x = if (isLtr) {
                        (motion.position + 0.5f) * itemWidthPx + panelOffsetPx
                    } else {
                        totalWidthPx - (motion.position + 0.5f) * itemWidthPx + panelOffsetPx
                    },
                    y = size.height / 2f
                )
            }
        }

        val commitSelection: (Int, Float) -> Unit = { requestedIndex, velocity ->
            val targetIndex = requestedIndex.coerceIn(0, items.lastIndex)
            visualIndex = targetIndex
            motion.settleTo(targetIndex.toFloat(), velocity)
            if (targetIndex != committedIndex) {
                committedIndex = targetIndex
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                latestOnItemSelected(targetIndex)
            }
        }

        Row(
            modifier = Modifier
                .graphicsLayer { translationX = panelOffsetPx }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousCapsule },
                    effects = {
                        vibrancy()
                        blur(8.dp.toPx())
                        lens(24.dp.toPx(), 24.dp.toPx())
                    },
                    highlight = {
                        Highlight.Default.copy(
                            width = 0.65.dp,
                            blurRadius = 0.45.dp,
                            alpha = if (darkTheme) 0.52f else 0.68f
                        )
                    },
                    shadow = {
                        Shadow(
                            radius = 16.dp,
                            offset = DpOffset.Zero,
                            color = Color.Black.copy(alpha = if (darkTheme) 0.20f else 0.10f)
                        )
                    },
                    onDrawSurface = {
                        drawRect(
                            if (darkTheme) {
                                Color(0xFF121212).copy(alpha = 0.40f)
                            } else {
                                Color(0xFFFAFAFA).copy(alpha = 0.40f)
                            }
                        )
                    },
                    layerBlock = {
                        val progress = motion.pressProgress.coerceIn(0f, 1f)
                        val scale = lerp(
                            1f,
                            1f + 16.dp.toPx() / size.width.coerceAtLeast(1f),
                            progress
                        )
                        scaleX = scale
                        scaleY = scale
                    }
                )
                .then(interactiveHighlight.modifier)
                .fillMaxSize()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                LiquidGlassBottomTab(
                    item = item,
                    selected = index == visualIndex,
                    tint = contentColor,
                    contentScale = 1f,
                    onAccessibilityClick = { commitSelection(index, 0f) }
                )
            }
        }

        Row(
            modifier = Modifier
                .clearAndSetSemantics { }
                .alpha(0f)
                .layerBackdrop(tabsBackdrop)
                .graphicsLayer { translationX = panelOffsetPx }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousCapsule },
                    effects = {
                        val progress = motion.pressProgress.coerceIn(0f, 1f)
                        vibrancy()
                        blur(8.dp.toPx())
                        lens(
                            refractionHeight = 24.dp.toPx() * progress,
                            refractionAmount = 24.dp.toPx() * progress
                        )
                    },
                    highlight = {
                        Highlight.Default.copy(
                            width = 0.65.dp,
                            blurRadius = 0.45.dp,
                            alpha = motion.pressProgress.coerceIn(0f, 1f)
                        )
                    },
                    onDrawSurface = { drawRect(containerColor) }
                )
                .then(interactiveHighlight.modifier)
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selectedContentScale = lerp(
                1f,
                1.18f,
                motion.pressProgress.coerceIn(0f, 1f)
            )
            items.forEachIndexed { index, item ->
                LiquidGlassBottomTab(
                    item = item,
                    selected = index == visualIndex,
                    tint = accentColor,
                    contentScale = selectedContentScale,
                    onAccessibilityClick = { }
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(x = 4.dp)
                .width(itemWidth)
                .height(56.dp)
                .graphicsLayer {
                    val visualPosition = if (isLtr) {
                        motion.position
                    } else {
                        items.lastIndex - motion.position
                    }
                    translationX = itemWidthPx * visualPosition + panelOffsetPx
                    translationY = 0f
                }
                .drawBackdrop(
                    backdrop = combinedBackdrop,
                    shape = { ContinuousCapsule },
                    effects = {
                        val progress = motion.pressProgress.coerceIn(0f, 1f)
                        lens(
                            refractionHeight = 10.dp.toPx() * progress,
                            refractionAmount = 14.dp.toPx() * progress,
                            depthEffect = true,
                            chromaticAberration = true
                        )
                    },
                    highlight = {
                        Highlight.Default.copy(
                            width = 0.75.dp,
                            blurRadius = 0.55.dp,
                            alpha = motion.pressProgress.coerceIn(0f, 1f)
                        )
                    },
                    shadow = {
                        val progress = motion.pressProgress.coerceIn(0f, 1f)
                        Shadow(
                            radius = 10.dp,
                            offset = DpOffset.Zero,
                            color = Color.Black.copy(
                                alpha = (if (darkTheme) 0.20f else 0.10f) * progress
                            )
                        )
                    },
                    innerShadow = {
                        val progress = motion.pressProgress.coerceIn(0f, 1f)
                        InnerShadow(
                            radius = 8.dp * progress,
                            offset = DpOffset.Zero,
                            color = Color.Black.copy(alpha = 0.15f * progress)
                        )
                    },
                    layerBlock = {
                        val progress = motion.pressProgress.coerceIn(0f, 1f)
                        scaleX = lerp(1f, 1.30f, progress)
                        scaleY = lerp(1f, 1.30f, progress)
                        val normalizedVelocity = motion.velocity / 10f
                        scaleX /= 1f - (normalizedVelocity * 0.50f).coerceIn(-0.08f, 0.08f)
                        scaleY *= 1f - (normalizedVelocity * 0.18f).coerceIn(-0.04f, 0.04f)
                    },
                    onDrawSurface = {
                        val progress = motion.pressProgress.coerceIn(0f, 1f)
                        drawRect(
                            if (darkTheme) {
                                Color.White.copy(alpha = 0.10f)
                            } else {
                                Color.Black.copy(alpha = 0.10f)
                            },
                            alpha = 1f - progress
                        )
                        drawRect(Color.Black.copy(alpha = 0.03f * progress))
                    }
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(motion, itemWidthPx, isLtr, committedIndex) {
                    fun logicalSlotAt(x: Float): Int {
                        val visualSlot = ((x - contentStartPx) / itemWidthPx)
                            .toInt()
                            .coerceIn(0, items.lastIndex)
                        return if (isLtr) visualSlot else items.lastIndex - visualSlot
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downSlot = logicalSlotAt(down.position.x)
                        panelReturnJob?.cancel()
                        panelDragPx = 0f
                        interactiveHighlight.press()
                        val velocityTracker = VelocityTracker()
                        velocityTracker.addPosition(down.uptimeMillis, down.position)
                        visualIndex = downSlot
                        motion.beginInteraction(
                            if (downSlot == committedIndex) motion.position else downSlot.toFloat()
                        )

                        var totalDx = 0f
                        var totalDy = 0f
                        var didDrag = false
                        var canceled = false
                        var logicalVelocity = 0f

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null) {
                                canceled = true
                                break
                            }
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            if (change.changedToUpIgnoreConsumed()) break
                            if (!change.pressed) {
                                canceled = true
                                break
                            }

                            val delta = change.positionChange()
                            totalDx += delta.x
                            totalDy += delta.y
                            if (!didDrag &&
                                abs(totalDx) > viewConfiguration.touchSlop &&
                                abs(totalDx) > abs(totalDy)
                            ) {
                                didDrag = true
                            }
                            if (delta.x != 0f) {
                                val direction = if (isLtr) 1f else -1f
                                logicalVelocity = velocityTracker.calculateVelocity().x *
                                    direction / itemWidthPx
                                motion.dragBy(
                                    logicalDelta = delta.x * direction / itemWidthPx,
                                    logicalVelocity = logicalVelocity
                                )
                                panelDragPx += delta.x
                                visualIndex = stabilizedTabIndex(
                                    position = motion.position,
                                    currentIndex = visualIndex,
                                    lastIndex = items.lastIndex
                                )
                                if (didDrag) change.consume()
                            }
                        }

                        val targetIndex = when {
                            canceled -> committedIndex
                            didDrag -> (motion.position + logicalVelocity.coerceIn(-8f, 8f) * 0.025f)
                                .roundToInt()
                                .coerceIn(0, items.lastIndex)
                            else -> downSlot
                        }
                        interactiveHighlight.release()
                        panelReturnJob = animationScope.launch {
                            animate(
                                initialValue = panelDragPx,
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = 1f,
                                    stiffness = 300f,
                                    visibilityThreshold = 0.5f
                                )
                            ) { value, _ -> panelDragPx = value }
                            panelDragPx = 0f
                        }
                        if (canceled) {
                            visualIndex = committedIndex
                            motion.settleTo(committedIndex.toFloat(), 0f)
                        } else {
                            commitSelection(targetIndex, logicalVelocity)
                        }
                    }
                }
        )
    }
}

@Composable
private fun RowScope.LiquidGlassBottomTab(
    item: LiquidGlassBottomBarItem,
    selected: Boolean,
    tint: Color,
    contentScale: Float,
    onAccessibilityClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(ContinuousCapsule)
            .semantics {
                this.selected = selected
                role = Role.Tab
                onClick {
                    onAccessibilityClick()
                    true
                }
            }
            .fillMaxHeight()
            .weight(1f)
            .graphicsLayer {
                scaleX = contentScale
                scaleY = contentScale
            },
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = item.title,
            color = tint,
            fontSize = 10.5.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private class LiquidGlassInteractiveHighlight(
    private val animationScope: CoroutineScope,
    private val position: (Size) -> Offset
) {
    private val progress = Animatable(0f, 0.001f)
    private val shader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        RuntimeShader(
            """
            uniform float2 size;
            layout(color) uniform half4 color;
            uniform float radius;
            uniform float2 position;

            half4 main(float2 coord) {
                float dist = distance(coord, position);
                float intensity = smoothstep(radius, radius * 0.5, dist);
                return color * intensity;
            }
            """.trimIndent()
        )
    } else {
        null
    }

    val modifier: Modifier = Modifier.drawWithContent {
        val pressProgress = progress.value
        if (pressProgress > 0f) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && shader != null) {
                drawRect(
                    color = Color.White.copy(alpha = 0.08f * pressProgress),
                    blendMode = BlendMode.Plus
                )
                val highlightPosition = position(size)
                shader.apply {
                    setFloatUniform("size", size.width, size.height)
                    setColorUniform(
                        "color",
                        Color.White.copy(alpha = 0.15f * pressProgress).toArgb()
                    )
                    setFloatUniform("radius", size.minDimension * 1.5f)
                    setFloatUniform(
                        "position",
                        highlightPosition.x.coerceIn(0f, size.width),
                        highlightPosition.y.coerceIn(0f, size.height)
                    )
                }
                drawRect(
                    brush = ShaderBrush(shader),
                    blendMode = BlendMode.Plus
                )
            } else {
                drawRect(
                    color = Color.White.copy(alpha = 0.20f * pressProgress),
                    blendMode = BlendMode.Plus
                )
            }
        }
        drawContent()
    }

    fun press() {
        animationScope.launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.5f,
                    stiffness = 300f,
                    visibilityThreshold = 0.001f
                )
            )
        }
    }

    fun release() {
        animationScope.launch {
            progress.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.5f,
                    stiffness = 300f,
                    visibilityThreshold = 0.001f
                )
            )
        }
    }
}
