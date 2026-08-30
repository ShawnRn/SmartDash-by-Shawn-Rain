package com.shawnrain.sdash.ui.navigation

/*
 * Optical styling is adapted from AndroidLiquidGlass 1.0.3 LiquidBottomTabs.
 * Copyright 2025 Kyant. Licensed under the Apache License, Version 2.0.
 * SmartDash keeps the moving glass on one stable GPU layer for high-refresh devices.
 */

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.capsule.ContinuousCapsule
import kotlin.math.abs
import kotlin.math.roundToInt

data class LiquidGlassBottomBarItem(
    val title: String,
    val icon: ImageVector
)

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
    val haptic = LocalHapticFeedback.current
    val latestOnItemSelected by rememberUpdatedState(onItemSelected)
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val animationScope = rememberCoroutineScope()
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

        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    }
                )
        )

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
                    translationX = itemWidthPx * visualPosition
                    translationY = 0f
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousCapsule },
                    effects = {
                        vibrancy()
                        blur(5.dp.toPx())
                        lens(
                            refractionHeight = 10.dp.toPx(),
                            refractionAmount = 14.dp.toPx()
                        )
                    },
                    highlight = {
                        Highlight.Default.copy(
                            width = 0.55.dp,
                            blurRadius = 0.4.dp,
                            alpha = if (darkTheme) 0.42f else 0.55f
                        )
                    },
                    shadow = {
                        Shadow(
                            radius = 7.dp,
                            offset = DpOffset.Zero,
                            color = Color.Black.copy(alpha = if (darkTheme) 0.14f else 0.075f)
                        )
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = 5.dp,
                            offset = DpOffset.Zero,
                            color = Color.Black.copy(alpha = if (darkTheme) 0.10f else 0.045f)
                        )
                    },
                    onDrawSurface = {
                        drawRect(
                            if (darkTheme) {
                                Color.White.copy(alpha = 0.065f)
                            } else {
                                Color.Black.copy(alpha = 0.085f)
                            }
                        )
                    }
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == visualIndex
                LiquidGlassBottomTab(
                    selected = selected,
                    onAccessibilityClick = { commitSelection(index, 0f) }
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                        },
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = item.title,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                        },
                        fontSize = 10.5.sp,
                        lineHeight = 12.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }

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
                        val velocityTracker = VelocityTracker()
                        velocityTracker.addPosition(down.uptimeMillis, down.position)
                        visualIndex = downSlot
                        motion.beginInteraction(downSlot.toFloat())

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
                                motion.dragBy(delta.x * direction / itemWidthPx)
                                val nearest = motion.position.roundToInt()
                                    .coerceIn(0, items.lastIndex)
                                if (nearest != visualIndex) visualIndex = nearest
                                if (didDrag) change.consume()
                            }
                        }

                        val targetIndex = when {
                            canceled -> committedIndex
                            didDrag -> (motion.position + logicalVelocity * 0.045f)
                                .roundToInt()
                                .coerceIn(0, items.lastIndex)
                            else -> downSlot
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
    selected: Boolean,
    onAccessibilityClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
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
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}
