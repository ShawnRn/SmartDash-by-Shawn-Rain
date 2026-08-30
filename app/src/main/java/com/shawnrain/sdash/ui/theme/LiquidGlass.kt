package com.shawnrain.sdash.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SmartDash 液态玻璃 (Liquid Glass / Glassmorphism) 设计系统
 *
 * 采用半透光底色、表面高光微光反射 (Specular Gloss) 与物理边缘折射边框 (Rim Lighting)，
 * 保证容器内文字与图标 100% 锐利清晰，同时呈现高端玻璃悬浮质感。
 */

/**
 * 液态玻璃修饰符
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape = bezierRoundedShape(20.dp),
    tintColor: Color = Color.Unspecified,
    alpha: Float = 0.13f,
    borderWidth: Dp = 0.65.dp,
    borderAlpha: Float = 0.14f,
    elevation: Dp = 0.dp
): Modifier {
    val isDark = isSystemInDarkTheme()

    // 自适应底色
    val baseTint = if (tintColor != Color.Unspecified) {
        tintColor
    } else if (isDark) {
        Color(0xFF17191E)
    } else {
        Color(0xFFFFFFFF)
    }

    val resolvedAlpha = (alpha * baseTint.alpha).coerceIn(0.06f, 0.72f)
    val topGlow = if (isDark) Color.White.copy(alpha = 0.035f) else Color.White.copy(alpha = 0.10f)
    val bottomShade = if (isDark) Color.Black.copy(alpha = 0.055f) else Color.Black.copy(alpha = 0.012f)

    // 物理高光折射边框（左上角光照高亮）
    val borderBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = (borderAlpha * 1.15f).coerceAtMost(0.24f)),
                Color.White.copy(alpha = borderAlpha * 0.55f),
                Color.White.copy(alpha = borderAlpha * 0.20f),
                Color.White.copy(alpha = borderAlpha * 0.08f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = (borderAlpha * 1.35f).coerceAtMost(0.72f)),
                Color.White.copy(alpha = borderAlpha * 0.55f),
                Color.Black.copy(alpha = borderAlpha * 0.06f),
                Color.White.copy(alpha = borderAlpha * 0.35f)
            )
        },
        start = Offset.Zero,
        end = Offset.Infinite
    )

    // 层次渐变底色
    val surfaceBrush = Brush.verticalGradient(
        colors = listOf(
            baseTint.copy(alpha = (resolvedAlpha + 0.045f).coerceAtMost(0.76f)),
            baseTint.copy(alpha = resolvedAlpha),
            baseTint.copy(alpha = (resolvedAlpha - 0.035f).coerceAtLeast(0.04f))
        )
    )

    return this
        .then(
            if (elevation > 0.dp) {
                Modifier.shadow(
                    elevation = elevation,
                    shape = shape,
                    ambientColor = if (isDark) Color.Black.copy(alpha = 0.45f) else Color(0x22000000),
                    spotColor = if (isDark) Color(0xFF6750A4).copy(alpha = 0.25f) else Color(0x18000000)
                )
            } else Modifier
        )
        .clip(shape)
        .background(surfaceBrush)
        .drawWithCache {
            val sheenBrush = Brush.linearGradient(
                colors = listOf(topGlow, Color.Transparent, bottomShade),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
            onDrawWithContent {
                drawRect(sheenBrush)
                drawContent()
            }
        }
        .border(BorderStroke(borderWidth, borderBrush), shape)
}

/**
 * 液态玻璃容器 (Liquid Glass Surface)
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = bezierRoundedShape(20.dp),
    tintColor: Color = Color.Unspecified,
    alpha: Float = 0.13f,
    borderWidth: Dp = 0.65.dp,
    borderAlpha: Float = 0.14f,
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .liquidGlass(
                shape = shape,
                tintColor = tintColor,
                alpha = alpha,
                borderWidth = borderWidth,
                borderAlpha = borderAlpha,
                elevation = elevation
            )
            .then(clickableModifier),
        content = content
    )
}

/**
 * 液态玻璃胶囊药丸 (Liquid Glass Pill)
 * 适用于状态栏 Badge、快捷药丸按钮等
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    tintColor: Color = Color.Unspecified,
    alpha: Float = 0.28f,
    borderWidth: Dp = 1.dp,
    borderAlpha: Float = 0.28f,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = bezierPillShape(),
        tintColor = tintColor,
        alpha = alpha,
        borderWidth = borderWidth,
        borderAlpha = borderAlpha,
        elevation = elevation,
        onClick = onClick,
        content = content
    )
}
