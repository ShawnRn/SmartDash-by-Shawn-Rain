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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import com.shawnrain.sdash.R

data class SmartDashColors(
    val speedAccent: Color = Color(0xFFD0BCFF),
    val regenGreen: Color = Color(0xFF66BB6A),
    val warningAmber: Color = Color(0xFFFFCA28),
    val criticalRed: Color = Color(0xFFFF5252)
)

val LocalSmartDashColors = staticCompositionLocalOf { SmartDashColors() }
val LocalUiScale = staticCompositionLocalOf { 1.0f }

val MiSansFontFamily = FontFamily(
    Font(R.font.misans_light, FontWeight.Light),
    Font(R.font.misans_regular, FontWeight.Normal),
    Font(R.font.misans_medium, FontWeight.Medium),
    Font(R.font.misans_demibold, FontWeight.SemiBold),
    Font(R.font.misans_bold, FontWeight.Bold),
    Font(R.font.misans_heavy, FontWeight.ExtraBold),
    Font(R.font.misans_heavy, FontWeight.Black)
)

val LocalUseMiSansFont = staticCompositionLocalOf { true }

fun getAppTypography(useMiSans: Boolean): Typography {
    val fontFamily = if (useMiSans) MiSansFontFamily else FontFamily.Default
    val fontFeatureSettings = if (useMiSans) "tnum" else null
    val default = Typography()
    return Typography(
        displayLarge = default.displayLarge.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        displayMedium = default.displayMedium.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        displaySmall = default.displaySmall.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        headlineLarge = default.headlineLarge.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        headlineMedium = default.headlineMedium.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        headlineSmall = default.headlineSmall.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        titleLarge = default.titleLarge.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        titleMedium = default.titleMedium.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        titleSmall = default.titleSmall.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        bodyLarge = default.bodyLarge.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        bodyMedium = default.bodyMedium.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        bodySmall = default.bodySmall.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        labelLarge = default.labelLarge.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        labelMedium = default.labelMedium.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings),
        labelSmall = default.labelSmall.copy(fontFamily = fontFamily, fontFeatureSettings = fontFeatureSettings)
    )
}

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
    useMiSans: Boolean = true,
    uiScale: Float = 1.0f,
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
    val typography = getAppTypography(useMiSans)
    val systemDensity = LocalDensity.current
    val effectiveScale = uiScale.coerceIn(0.70f, 1.40f)
    val scaledDensity = Density(
        density = systemDensity.density * effectiveScale,
        fontScale = systemDensity.fontScale
    )

    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
        LocalSmartDashColors provides customColors,
        LocalUseMiSansFont provides useMiSans,
        LocalUiScale provides effectiveScale
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = HabeShapes,
            typography = typography,
            content = content
        )
    }
}
