package com.shawnrain.habe.ui.navigation

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Window
import android.view.WindowManager
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

@Composable
fun ApplyDialogWindowBlurEffect(
    blurRadius: Dp = 28.dp,
    fullscreen: Boolean = false
) {
    val dialogView = LocalView.current
    val window = (dialogView.parent as? DialogWindowProvider)?.window
    val blurRadiusPx = with(LocalDensity.current) { blurRadius.roundToPx() }
    DisposableEffect(window, blurRadiusPx, fullscreen) {
        window?.let { configurePopupWindow(it, blurRadiusPx, fullscreen) }
        onDispose { }
    }
}

fun configurePopupWindow(
    window: Window,
    blurRadiusPx: Int,
    fullscreen: Boolean = false
) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT
    )
    window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        window.attributes = window.attributes.apply {
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
    }
    if (fullscreen) {
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }
    window.statusBarColor = android.graphics.Color.TRANSPARENT
    window.navigationBarColor = android.graphics.Color.TRANSPARENT
    window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    window.setDimAmount(0f)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        val attrs = window.attributes
        attrs.blurBehindRadius = (blurRadiusPx * 0.42f).roundToInt()
        window.attributes = attrs
        window.setBackgroundBlurRadius((blurRadiusPx * 0.82f).roundToInt())
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
        window.isStatusBarContrastEnforced = false
    }
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
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        title = title?.let {
            {
                Column {
                    ApplyDialogWindowBlurEffect(blurRadius = blurRadius)
                    it()
                }
            }
        },
        text = text?.let {
            {
                Column {
                    if (title == null) {
                        ApplyDialogWindowBlurEffect(blurRadius = blurRadius)
                    }
                    it()
                }
            }
        },
        confirmButton = {
            Column {
                if (title == null && text == null) {
                    ApplyDialogWindowBlurEffect(blurRadius = blurRadius)
                }
                confirmButton()
            }
        },
        dismissButton = dismissButton
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlurredModalBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    blurRadius: Dp = 28.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        ApplyDialogWindowBlurEffect(blurRadius = blurRadius)
        content()
    }
}
