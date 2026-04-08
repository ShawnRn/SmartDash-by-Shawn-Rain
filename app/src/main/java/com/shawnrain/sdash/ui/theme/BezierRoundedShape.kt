package com.shawnrain.sdash.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

private const val SuperellipseExponent = 3.35f
private val PillCornerRadius = 999.dp

@Immutable
class BezierRoundedShape(
    topStart: CornerSize,
    topEnd: CornerSize,
    bottomEnd: CornerSize,
    bottomStart: CornerSize
) : CornerBasedShape(topStart, topEnd, bottomEnd, bottomStart) {

    constructor(cornerRadius: Dp) : this(
        topStart = CornerSize(cornerRadius),
        topEnd = CornerSize(cornerRadius),
        bottomEnd = CornerSize(cornerRadius),
        bottomStart = CornerSize(cornerRadius)
    )

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize
    ): BezierRoundedShape = BezierRoundedShape(topStart, topEnd, bottomEnd, bottomStart)

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection
    ): Outline {
        if (size.width <= 0f || size.height <= 0f) {
            return Outline.Generic(Path())
        }

        val maxRadius = min(size.width, size.height) / 2f
        val ts = topStart.coerceIn(0f, maxRadius)
        val te = topEnd.coerceIn(0f, maxRadius)
        val be = bottomEnd.coerceIn(0f, maxRadius)
        val bs = bottomStart.coerceIn(0f, maxRadius)
        val right = size.width
        val bottom = size.height

        val path = Path().apply {
            moveTo(ts, 0f)
            lineTo(right - te, 0f)
            addTopRightCorner(right, te)
            lineTo(right, bottom - be)
            addBottomRightCorner(right, bottom, be)
            lineTo(bs, bottom)
            addBottomLeftCorner(bottom, bs)
            lineTo(0f, ts)
            addTopLeftCorner(ts)
            close()
        }

        return Outline.Generic(path)
    }

    private fun Path.addTopRightCorner(right: Float, radius: Float) {
        if (radius <= 0f) {
            lineTo(right, 0f)
            return
        }

        val centerX = right - radius
        val centerY = radius
        val samples = cornerSamples(radius)
        for (step in 1..samples) {
            val angle = (PI.toFloat() / 2f) * (1f - step.toFloat() / samples.toFloat())
            val x = centerX + radius * superellipseCos(angle)
            val y = centerY - radius * superellipseSin(angle)
            lineTo(x, y)
        }
    }

    private fun Path.addBottomRightCorner(right: Float, bottom: Float, radius: Float) {
        if (radius <= 0f) {
            lineTo(right, bottom)
            return
        }

        val centerX = right - radius
        val centerY = bottom - radius
        val samples = cornerSamples(radius)
        for (step in 1..samples) {
            val angle = (PI.toFloat() / 2f) * (step.toFloat() / samples.toFloat())
            val x = centerX + radius * superellipseCos(angle)
            val y = centerY + radius * superellipseSin(angle)
            lineTo(x, y)
        }
    }

    private fun Path.addBottomLeftCorner(bottom: Float, radius: Float) {
        if (radius <= 0f) {
            lineTo(0f, bottom)
            return
        }

        val centerX = radius
        val centerY = bottom - radius
        val samples = cornerSamples(radius)
        for (step in 1..samples) {
            val angle = (PI.toFloat() / 2f) * (1f - step.toFloat() / samples.toFloat())
            val x = centerX - radius * superellipseCos(angle)
            val y = centerY + radius * superellipseSin(angle)
            lineTo(x, y)
        }
    }

    private fun Path.addTopLeftCorner(radius: Float) {
        if (radius <= 0f) {
            lineTo(0f, 0f)
            return
        }

        val centerX = radius
        val centerY = radius
        val samples = cornerSamples(radius)
        for (step in 1..samples) {
            val angle = (PI.toFloat() / 2f) * (step.toFloat() / samples.toFloat())
            val x = centerX - radius * superellipseCos(angle)
            val y = centerY - radius * superellipseSin(angle)
            lineTo(x, y)
        }
    }

    private fun cornerSamples(radius: Float): Int = max(8, ceil(radius / 3f).toInt())

    private fun superellipseCos(angle: Float): Float =
        cos(angle.toDouble()).coerceAtLeast(0.0).pow(2.0 / SuperellipseExponent).toFloat()

    private fun superellipseSin(angle: Float): Float =
        sin(angle.toDouble()).coerceAtLeast(0.0).pow(2.0 / SuperellipseExponent).toFloat()
}

fun bezierRoundedShape(cornerRadius: Dp): BezierRoundedShape = BezierRoundedShape(cornerRadius)

fun bezierPillShape(): BezierRoundedShape = BezierRoundedShape(PillCornerRadius)
