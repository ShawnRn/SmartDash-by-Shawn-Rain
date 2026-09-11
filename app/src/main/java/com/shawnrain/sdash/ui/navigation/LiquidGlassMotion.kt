package com.shawnrain.sdash.ui.navigation

/*
 * Motion model derived from the AndroidLiquidGlass interaction architecture.
 * Copyright 2025 Kyant. Licensed under the Apache License, Version 2.0.
 * SmartDash replaces per-pointer coroutine creation with one persistent state machine.
 */

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class LiquidGlassTabMotionState(
    private val animationScope: CoroutineScope,
    initialPosition: Float,
    private val positionRange: ClosedFloatingPointRange<Float>
) {
    var position by mutableFloatStateOf(initialPosition.coerceIn(positionRange))
        private set

    var pressProgress by mutableFloatStateOf(0f)
        private set

    var velocity by mutableFloatStateOf(0f)
        private set

    var isInteracting: Boolean = false
        private set

    private var positionJob: Job? = null
    private var pressJob: Job? = null

    fun beginInteraction(targetPosition: Float) {
        isInteracting = true
        animatePressTo(1f)
        animatePositionTo(
            targetPosition = targetPosition,
            initialVelocity = 0f
        )
    }

    fun dragBy(logicalDelta: Float, logicalVelocity: Float) {
        positionJob?.cancel()
        val nextPosition = (position + logicalDelta).coerceIn(positionRange)
        val hitStartBoundary = nextPosition == positionRange.start && logicalVelocity < 0f
        val hitEndBoundary = nextPosition == positionRange.endInclusive && logicalVelocity > 0f
        position = nextPosition
        velocity = if (hitStartBoundary || hitEndBoundary) {
            0f
        } else {
            val boundedVelocity = logicalVelocity.coerceIn(-8f, 8f)
            velocity * 0.72f + boundedVelocity * 0.28f
        }
    }

    fun settleTo(targetPosition: Float, initialVelocity: Float = 0f) {
        isInteracting = false
        animatePressTo(0f)
        animatePositionTo(
            targetPosition = targetPosition,
            initialVelocity = initialVelocity.coerceIn(-8f, 8f)
        )
    }

    fun syncTo(targetPosition: Float) {
        if (!isInteracting && targetPosition != position) {
            animatePositionTo(
                targetPosition = targetPosition,
                initialVelocity = 0f
            )
        }
    }

    private fun animatePositionTo(
        targetPosition: Float,
        initialVelocity: Float
    ): Job {
        positionJob?.cancel()
        val boundedTarget = targetPosition.coerceIn(positionRange)
        return animationScope.launch {
            animate(
                initialValue = position,
                targetValue = boundedTarget,
                initialVelocity = initialVelocity,
                animationSpec = spring(
                    dampingRatio = 1f,
                    stiffness = 1000f,
                    visibilityThreshold = 0.001f
                )
            ) { value, currentVelocity ->
                position = value
                velocity = currentVelocity
            }
            position = boundedTarget
            velocity = 0f
        }.also { positionJob = it }
    }

    private fun animatePressTo(targetProgress: Float) {
        pressJob?.cancel()
        pressJob = animationScope.launch {
            val animationSpec: AnimationSpec<Float> = if (targetProgress < pressProgress) {
                tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            } else {
                spring(
                    dampingRatio = 0.72f,
                    stiffness = 700f,
                    visibilityThreshold = 0.001f
                )
            }
            animate(
                initialValue = pressProgress,
                targetValue = targetProgress,
                animationSpec = animationSpec
            ) { value, _ ->
                pressProgress = value.coerceIn(0f, 1f)
            }
            pressProgress = targetProgress
        }
    }
}
