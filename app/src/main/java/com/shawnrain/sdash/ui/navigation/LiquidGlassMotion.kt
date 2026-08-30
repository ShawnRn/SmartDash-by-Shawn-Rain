package com.shawnrain.sdash.ui.navigation

/*
 * Motion model derived from the AndroidLiquidGlass interaction architecture.
 * Copyright 2025 Kyant. Licensed under the Apache License, Version 2.0.
 * SmartDash replaces per-pointer coroutine creation with one persistent state machine.
 */

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
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

    var isInteracting: Boolean = false
        private set

    private var positionJob: Job? = null

    fun beginInteraction(targetPosition: Float) {
        isInteracting = true
        animatePositionTo(
            targetPosition = targetPosition,
            initialVelocity = 0f
        )
    }

    fun dragBy(logicalDelta: Float) {
        positionJob?.cancel()
        position = (position + logicalDelta).coerceIn(positionRange)
    }

    fun settleTo(targetPosition: Float, initialVelocity: Float = 0f) {
        isInteracting = false
        animatePositionTo(
            targetPosition = targetPosition,
            initialVelocity = initialVelocity
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
    ) {
        positionJob?.cancel()
        val boundedTarget = targetPosition.coerceIn(positionRange)
        positionJob = animationScope.launch {
            animate(
                initialValue = position,
                targetValue = boundedTarget,
                initialVelocity = initialVelocity,
                animationSpec = spring(
                    dampingRatio = 1f,
                    stiffness = 1000f,
                    visibilityThreshold = 0.001f
                )
            ) { value, _ ->
                position = value
            }
            position = boundedTarget
        }
    }
}
