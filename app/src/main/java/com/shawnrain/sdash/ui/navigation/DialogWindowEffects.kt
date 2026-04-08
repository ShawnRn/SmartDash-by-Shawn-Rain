package com.shawnrain.sdash.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.compose.foundation.isSystemInDarkTheme
import com.shawnrain.sdash.ui.theme.bezierRoundedShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private fun findWindowFromView(view: android.view.View): Window? {
    var currentView: android.view.View? = view
    while (currentView != null) {
        if (currentView is DialogWindowProvider) {
            return currentView.window
        }
        val parent = currentView.parent
        if (parent is android.view.View) {
            currentView = parent
        } else if (parent is DialogWindowProvider) {
            return parent.window
        } else {
            currentView = null
        }
    }
    return (view.context as? Activity)?.window
}

@Composable
fun ApplyDialogWindowBlurEffect(blurRadius: Dp, fullscreen: Boolean = false) {
    val view = LocalView.current
    val density = LocalDensity.current
    val darkTheme = isSystemInDarkTheme()
    val radiusPx = with(density) { blurRadius.toPx() }

    DisposableEffect(view, radiusPx, fullscreen, darkTheme) {
        val window = findWindowFromView(view)
        if (window == null) {
            onDispose { }
        } else {
            configurePopupWindow(window, radiusPx.roundToInt(), fullscreen)
            WindowCompat.getInsetsController(window, window.decorView)?.apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
            onDispose { }
        }
    }
}

@Suppress("DEPRECATION")
fun configurePopupWindow(
    window: Window,
    blurRadiusPx: Int,
    fullscreen: Boolean = false
) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.setWindowAnimations(0)
    window.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT
    )
    window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        window.attributes = window.attributes.apply {
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
    }
    window.statusBarColor = android.graphics.Color.TRANSPARENT
    window.navigationBarColor = android.graphics.Color.TRANSPARENT
    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    window.setDimAmount(0f)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
        window.isStatusBarContrastEnforced = false
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        val attrs = window.attributes
        attrs.blurBehindRadius = (blurRadiusPx * 0.45f).roundToInt()
        window.attributes = attrs
        window.setBackgroundBlurRadius((blurRadiusPx * 0.86f).roundToInt())
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    }
}

@Composable
fun PopupBackdropBlurLayer(
    blurRadius: Dp = 28.dp,
    scrimColor: Color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.30f),
    onDismissRequest: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (onDismissRequest != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onDismissRequest
                    )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor)
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun BlurredAlertDialog(
    onDismissRequest: () -> Unit,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    blurRadius: Dp = 28.dp,
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = false
    )
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val enterDurationMs = 260
    val exitDurationMs = 200
    var isDialogVisible by remember { mutableStateOf(false) }
    var dismissInFlight by remember { mutableStateOf(false) }
    val entryProgress by animateFloatAsState(
        targetValue = if (isDialogVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isDialogVisible) enterDurationMs else exitDurationMs,
            easing = if (isDialogVisible) FastOutSlowInEasing else FastOutLinearInEasing
        ),
        label = "BlurredAlertDialogEntry"
    )

    fun requestDismiss() {
        if (dismissInFlight) return
        dismissInFlight = true
        isDialogVisible = false
        scope.launch {
            delay(exitDurationMs.toLong())
            onDismissRequest()
        }
    }

    LaunchedEffect(Unit) {
        dismissInFlight = false
        isDialogVisible = true
    }

    Dialog(
        onDismissRequest = ::requestDismiss,
        properties = properties
    ) {
        ApplyDialogWindowBlurEffect(blurRadius = blurRadius, fullscreen = true)
        Box(modifier = Modifier.fillMaxSize()) {
            PopupBackdropBlurLayer(
                blurRadius = blurRadius,
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.30f * entryProgress),
                onDismissRequest = ::requestDismiss
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                contentAlignment = Alignment.Center
            ) {
                PredictiveBackPopupTransform(
                    onBack = ::requestDismiss,
                    modifier = Modifier.fillMaxSize(),
                    maxHorizontalInset = 10.dp,
                    maxVerticalInset = 8.dp,
                    maxCorner = 24.dp,
                    maxScaleTravelFraction = 0.08f
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .widthIn(max = 420.dp)
                                .padding(horizontal = 24.dp, vertical = 24.dp)
                                .graphicsLayer {
                                    alpha = entryProgress
                                    translationY = with(density) { (1f - entryProgress) * 42.dp.toPx() }
                                    val scale = 0.96f + (0.04f * entryProgress)
                                    scaleX = scale
                                    scaleY = scale
                            },
                            shape = bezierRoundedShape(28.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            shadowElevation = 0.dp,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp)
                            ) {
                                title?.invoke()
                                if (text != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    text()
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    dismissButton?.invoke()
                                    confirmButton()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlurredModalBottomSheet(
    onDismissRequest: () -> Unit,
    blurRadius: Dp = 28.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val enterDurationMs = 300
    val exitDurationMs = 220
    var isSheetVisible by remember { mutableStateOf(false) }
    var dismissInFlight by remember { mutableStateOf(false) }
    val entryProgress by animateFloatAsState(
        targetValue = if (isSheetVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isSheetVisible) enterDurationMs else exitDurationMs,
            easing = if (isSheetVisible) FastOutSlowInEasing else FastOutLinearInEasing
        ),
        label = "BlurredBottomSheetEntry"
    )

    fun requestDismiss() {
        if (dismissInFlight) return
        dismissInFlight = true
        isSheetVisible = false
        scope.launch {
            delay(exitDurationMs.toLong())
            onDismissRequest()
        }
    }

    LaunchedEffect(Unit) {
        dismissInFlight = false
        isSheetVisible = true
    }

    Dialog(
        onDismissRequest = ::requestDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        ApplyDialogWindowBlurEffect(blurRadius = blurRadius, fullscreen = true)
        Box(modifier = Modifier.fillMaxSize()) {
            PopupBackdropBlurLayer(
                blurRadius = blurRadius,
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.30f * entryProgress),
                onDismissRequest = ::requestDismiss
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .graphicsLayer {
                            alpha = entryProgress
                            translationY = with(density) { (1f - entryProgress) * 38.dp.toPx() }
                            val scale = 0.985f + (0.015f * entryProgress)
                            scaleX = scale
                            scaleY = scale
                        },
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 0.dp,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.12f)
                                    .widthIn(min = 40.dp, max = 64.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.34f),
                                        RoundedCornerShape(999.dp)
                                    )
                                    .padding(vertical = 2.dp)
                            )
                        }
                        content()
                    }
                }
            }
        }
    }
}
