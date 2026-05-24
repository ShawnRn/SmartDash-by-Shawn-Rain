package com.shawnrain.sdash.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

data class SmartDashColors(
    val speedAccent: Color = Color(0xFFD0BCFF),
    val regenGreen: Color = Color(0xFF66BB6A),
    val warningAmber: Color = Color(0xFFFFCA28),
    val criticalRed: Color = Color(0xFFFF5252)
)

val LocalSmartDashColors = staticCompositionLocalOf { SmartDashColors() }

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6650a4),
    secondary = Color(0xFF625b71),
    tertiary = Color(0xFF7D5260)
)

private val HabeShapes = Shapes(
    extraSmall = BezierRoundedShape(14.dp),
    small = BezierRoundedShape(20.dp),
    medium = BezierRoundedShape(26.dp),
    large = BezierRoundedShape(34.dp),
    extraLarge = BezierRoundedShape(42.dp)
)

@Composable
fun HabeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val customColors = SmartDashColors()

    CompositionLocalProvider(
        LocalSmartDashColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = HabeShapes,
            content = content
        )
    }
}
