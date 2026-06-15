package com.shawnrain.sdash

import android.Manifest
import android.app.PictureInPictureParams
import android.app.Application
import android.content.pm.ActivityInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Rational
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.PlayArrow
import com.shawnrain.sdash.ui.dashcam.DashcamPlaybackScreen
import com.shawnrain.sdash.data.dashcam.DashcamManager
import com.shawnrain.sdash.ui.dashcam.DashcamRecordingsSheet
import com.shawnrain.sdash.data.dashcam.DashcamState
import androidx.compose.ui.draw.blur
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shawnrain.sdash.debug.AppLogger
import com.shawnrain.sdash.ui.bms.BmsScreen
import com.shawnrain.sdash.ui.connect.ConnectScreen
import com.shawnrain.sdash.ui.dashboard.BaselineMetricValue
import com.shawnrain.sdash.ui.dashboard.DashboardScreen
import com.shawnrain.sdash.ui.navigation.PredictiveBackPage
import com.shawnrain.sdash.ui.settings.SettingsScreen
import com.shawnrain.sdash.ui.speedtest.SpeedtestScreen
import com.shawnrain.sdash.ui.theme.HabeTheme
import com.shawnrain.sdash.ui.theme.bezierRoundedShape
import kotlin.math.min
import kotlin.math.max
import kotlin.math.abs
import kotlin.math.pow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.mutableFloatStateOf
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainActivity : ComponentActivity() {
    private lateinit var mainViewModel: MainViewModel
    private val isInPipModeState = mutableStateOf(false)
    private var currentRoute: String? = null
    private var pipEnabled = false
    private var pendingTelemetryPip = false
    private var skipNextPipForSystemBack = false
    private var systemBackObserver: OnBackInvokedCallback? = null

    companion object {
        private const val BACK_CHAIN_TAG = "MainActivityBack"
        private const val EXTRA_TARGET_ROUTE = "target_route"
        private val TELEMETRY_PIP_ASPECT_RATIO = Rational(18, 9)
        private val TELEMETRY_PIP_EXPANDED_ASPECT_RATIO = Rational(18, 9)

        fun createLaunchIntent(
            context: Context,
            route: String = Screen.Dashboard.route
        ): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_TARGET_ROUTE, route)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.e("FATAL", "Uncaught Exception on ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            systemBackObserver = OnBackInvokedCallback {
                // Observe committed system back and avoid stealing it with auto PiP.
                skipNextPipForSystemBack = true
                AppLogger.i(
                    BACK_CHAIN_TAG,
                    "OnBackInvoked(observer) currentRoute=$currentRoute pipEnabled=$pipEnabled inPip=$isInPictureInPictureMode"
                )
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(300)
                    if (skipNextPipForSystemBack) {
                        skipNextPipForSystemBack = false
                        AppLogger.i(BACK_CHAIN_TAG, "Auto-reset skipNextPipForSystemBack to false after timeout")
                    }
                }
            }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_SYSTEM_NAVIGATION_OBSERVER,
                systemBackObserver!!
            )
            AppLogger.i(BACK_CHAIN_TAG, "Registered OnBackInvoked observer (sdk=${Build.VERSION.SDK_INT})")
        }
        dispatchLaunchIntent(intent)
        enableEdgeToEdge()
        setContent {
            val viewModel = mainViewModel
            val useMiSans by viewModel.useMiSansFont.collectAsState()

            HabeTheme(useMiSans = useMiSans) {
                PermissionBootstrapGate {
                    MainScreen(
                        viewModel = viewModel,
                        isInPictureInPictureMode = isInPipModeState.value,
                        onRouteChanged = { route ->
                            currentRoute = route
                            updateOrientationForRoute(route)
                            if (pendingTelemetryPip && route == Screen.Dashboard.route) {
                                pendingTelemetryPip = false
                                enterTelemetryPictureInPicture()
                            }
                            updatePictureInPictureParams()
                        },
                        onPipPreferenceChanged = { enabled ->
                            pipEnabled = enabled
                            AppLogger.i(BACK_CHAIN_TAG, "PiP preference changed enabled=$enabled")
                            updatePictureInPictureParams()
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dispatchLaunchIntent(intent)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        AppLogger.i(
            BACK_CHAIN_TAG,
            "onUserLeaveHint currentRoute=$currentRoute pipEnabled=$pipEnabled inPip=$isInPictureInPictureMode finishing=$isFinishing"
        )
        requestTelemetryPictureInPicture()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        AppLogger.i(BACK_CHAIN_TAG, "onPictureInPictureModeChanged inPip=$isInPictureInPictureMode route=$currentRoute")
        isInPipModeState.value = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        } else {
            updateOrientationForRoute(currentRoute)
        }
        if (!isInPictureInPictureMode) {
            pendingTelemetryPip = false
        }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            systemBackObserver?.let { callback ->
                onBackInvokedDispatcher.unregisterOnBackInvokedCallback(callback)
            }
            systemBackObserver = null
        }
        
        if (isFinishing) {
            AppLogger.i("MainActivity", "Activity is finishing (swipe away), cleaning up resources...")
            try {
                if (::mainViewModel.isInitialized) {
                    mainViewModel.cleanupOnExit()
                }
            } catch (e: Exception) {
                AppLogger.e("MainActivity", "Failed to cleanup on exit", e)
            }
        }
        
        super.onDestroy()
    }

    private fun dispatchLaunchIntent(intent: Intent?) {
        intent
            ?.getStringExtra(EXTRA_TARGET_ROUTE)
            ?.takeIf { it.isNotBlank() }
            ?.let(MainActivityRouteRequests::emit)
    }

    private fun requestTelemetryPictureInPicture() {
        AppLogger.i(
            BACK_CHAIN_TAG,
            "requestPiP start currentRoute=$currentRoute pipEnabled=$pipEnabled inPip=$isInPictureInPictureMode skipByBack=$skipNextPipForSystemBack finishing=$isFinishing"
        )
        if (skipNextPipForSystemBack) {
            skipNextPipForSystemBack = false
            AppLogger.i(BACK_CHAIN_TAG, "requestPiP skipped by OnBackInvoked observer")
            return
        }
        if (isFinishing || isDestroyed) {
            AppLogger.i(BACK_CHAIN_TAG, "requestPiP skipped because activity is finishing/destroyed")
            return
        }
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            AppLogger.i(BACK_CHAIN_TAG, "requestPiP skipped because activity is not started")
            return
        }
        if (!pipEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.O || isInPictureInPictureMode) {
            AppLogger.i(BACK_CHAIN_TAG, "requestPiP skipped by guard")
            return
        }
        AppLogger.i(BACK_CHAIN_TAG, "requestPiP proceed to enterPiP")
        enterTelemetryPictureInPicture()
    }

    private fun enterTelemetryPictureInPicture() {
        AppLogger.i(
            BACK_CHAIN_TAG,
            "enterPiP start currentRoute=$currentRoute pipEnabled=$pipEnabled inPip=$isInPictureInPictureMode"
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || isInPictureInPictureMode) {
            AppLogger.i(BACK_CHAIN_TAG, "enterPiP skipped by guard")
            return
        }
        // Temporarily reset requestedOrientation to allow non-dashboard routes (which lock to portrait)
        // to safely enter landscape aspect ratio PiP.
        if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_FULL_USER) {
            AppLogger.i(BACK_CHAIN_TAG, "enterPiP: Temporarily setting orientation to FULL_USER to allow PiP entry")
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        }
        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(TELEMETRY_PIP_ASPECT_RATIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setSeamlessResizeEnabled(false)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 12+ supports system double-tap to toggle between normal and expanded PiP.
            // Keep the expanded state only slightly larger so it stays compact on bike use.
            builder.setExpandedAspectRatio(TELEMETRY_PIP_EXPANDED_ASPECT_RATIO)
        }
        val params = builder.build()
        runCatching {
            enterPictureInPictureMode(params)
            AppLogger.i(BACK_CHAIN_TAG, "enterPiP invoke enterPictureInPictureMode done")
        }.onFailure { error ->
            AppLogger.e(BACK_CHAIN_TAG, "enterPiP failed", error)
            // If failed, restore orientation
            updateOrientationForRoute(currentRoute)
        }
    }

    private fun updatePictureInPictureParams() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(TELEMETRY_PIP_ASPECT_RATIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setSeamlessResizeEnabled(false)
            // Only enable autoEnter for Dashboard route to avoid system-level portrait orientation conflicts on non-dashboard tabs.
            // For other portrait-locked routes, autoEnter is disabled and we rely on onUserLeaveHint fallback with manual orientation reset.
            val autoEnter = pipEnabled && (currentRoute == Screen.Dashboard.route)
            builder.setAutoEnterEnabled(autoEnter)
            AppLogger.d(BACK_CHAIN_TAG, "updatePiPParams setAutoEnterEnabled=$autoEnter currentRoute=$currentRoute")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            builder.setExpandedAspectRatio(TELEMETRY_PIP_EXPANDED_ASPECT_RATIO)
        }
        runCatching {
            setPictureInPictureParams(builder.build())
        }.onFailure { error ->
            AppLogger.e(BACK_CHAIN_TAG, "Failed to setPictureInPictureParams", error)
        }
    }

    private fun updateOrientationForRoute(route: String?) {
        val targetOrientation = if (route == Screen.Dashboard.route) {
            ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        if (requestedOrientation != targetOrientation) {
            requestedOrientation = targetOrientation
        }
    }
}

@Composable
private fun PermissionBootstrapGate(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val firstLaunchPrefs = remember(context) {
        context.getSharedPreferences("habe_bootstrap", Context.MODE_PRIVATE)
    }
    val shouldSuggestMigration = remember {
        !firstLaunchPrefs.getBoolean("migration_assistant_suggested", false)
    }
    val requiredPermissions = remember { requiredRuntimePermissions() }
    val requiresBackgroundLocation = remember { Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q }
    var hasAllPermissions by remember {
        mutableStateOf(hasAllRuntimePermissions(context, requiredPermissions))
    }
    var hasBackgroundLocation by remember {
        mutableStateOf(hasBackgroundLocationPermission(context))
    }
    var launchRequested by remember { mutableStateOf(false) }
    var backgroundLaunchRequested by remember { mutableStateOf(false) }
    var showMigrationSuggestion by remember { mutableStateOf(false) }
    var suggestionEntranceScheduled by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasAllPermissions = hasAllRuntimePermissions(context, requiredPermissions)
        hasBackgroundLocation = hasBackgroundLocationPermission(context)
    }
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        hasBackgroundLocation = hasBackgroundLocationPermission(context)
    }

    LaunchedEffect(hasAllPermissions, launchRequested, requiredPermissions) {
        if (requiredPermissions.isNotEmpty() && !hasAllPermissions && !launchRequested) {
            launchRequested = true
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        }
    }
    LaunchedEffect(hasAllPermissions, hasBackgroundLocation, requiresBackgroundLocation, backgroundLaunchRequested) {
        if (
            hasAllPermissions &&
            requiresBackgroundLocation &&
            !hasBackgroundLocation &&
            !backgroundLaunchRequested
        ) {
            backgroundLaunchRequested = true
            backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }
    LaunchedEffect(hasAllPermissions, shouldSuggestMigration) {
        if (hasAllPermissions && shouldSuggestMigration && !suggestionEntranceScheduled) {
            suggestionEntranceScheduled = true
            delay(160)
            showMigrationSuggestion = true
        }
    }

    val readyForMainContent =
        (hasAllPermissions || requiredPermissions.isEmpty()) &&
            (!requiresBackgroundLocation || hasBackgroundLocation)
    if (readyForMainContent) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            AnimatedVisibility(
                visible = showMigrationSuggestion,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 22.dp),
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 280, easing = LinearOutSlowInEasing)
                ) + slideInVertically(
                    animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
                    initialOffsetY = { fullHeight -> fullHeight / 3 }
                ) + scaleIn(
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                    initialScale = 0.92f,
                    transformOrigin = TransformOrigin(0.5f, 1f)
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)
                ) + slideOutVertically(
                    animationSpec = tween(durationMillis = 220, easing = FastOutLinearInEasing),
                    targetOffsetY = { fullHeight -> fullHeight / 3 }
                ) + scaleOut(
                    animationSpec = tween(durationMillis = 220, easing = FastOutLinearInEasing),
                    targetScale = 0.96f,
                    transformOrigin = TransformOrigin(0.5f, 1f)
                )
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 6.dp,
                    shadowElevation = 10.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "是否从旧设备迁移数据？",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "建议先用“换机助手”迁移车辆、行程和配置，避免重复设置。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    firstLaunchPrefs.edit()
                                        .putBoolean("migration_assistant_suggested", true)
                                        .apply()
                                    showMigrationSuggestion = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "稍后再说",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = {
                                    firstLaunchPrefs.edit()
                                        .putBoolean("migration_assistant_suggested", true)
                                        .apply()
                                    showMigrationSuggestion = false
                                    MainActivityRouteRequests.emit(Screen.Settings.route)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "去换机助手",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "授权后继续",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "应用需要蓝牙、相机、定位与后台定位权限，以支持后台/熄屏继续记录行程。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (requiredPermissions.isNotEmpty() && !hasAllPermissions) {
                Button(
                    onClick = { permissionLauncher.launch(requiredPermissions.toTypedArray()) },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text("重新请求基础权限")
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            if (requiresBackgroundLocation && !hasBackgroundLocation) {
                Button(
                    onClick = { backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION) },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text("请求“始终允许位置”")
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            OutlinedButton(
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text("前往系统设置授权（含后台定位）")
            }
        }
    }
}

private fun requiredRuntimePermissions(): List<String> {
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions += Manifest.permission.BLUETOOTH_SCAN
        permissions += Manifest.permission.BLUETOOTH_CONNECT
    }
    return permissions
}

private fun hasAllRuntimePermissions(context: Context, permissions: List<String>): Boolean {
    return permissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

private fun hasBackgroundLocationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private data class RouteRequest(
    val id: Long,
    val route: String
)

private object MainActivityRouteRequests {
    private val _current = MutableSharedFlow<RouteRequest>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val current = _current.asSharedFlow()

    fun emit(route: String) {
        _current.tryEmit(
            RouteRequest(
                id = SystemClock.elapsedRealtimeNanos(),
                route = route
            )
        )
    }
}

@Composable
fun MainScreen(
    isInPictureInPictureMode: Boolean,
    onRouteChanged: (String?) -> Unit,
    onPipPreferenceChanged: (Boolean) -> Unit,
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val pipEnabled by viewModel.overlayEnabled.collectAsState()
    val pendingRideStop by viewModel.pendingRideStop.collectAsState()
    val calibrationMessage by viewModel.calibrationMessage.collectAsState()
    val driveSyncMessage by viewModel.driveSyncMessage.collectAsState()
    val globalSnackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(viewModel) {
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.handleAppVisibilityChanged(true)
                Lifecycle.Event.ON_STOP -> viewModel.handleAppVisibilityChanged(false)
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(pipEnabled) {
        onPipPreferenceChanged(pipEnabled)
    }

    LaunchedEffect(calibrationMessage) {
        val message = calibrationMessage ?: return@LaunchedEffect
        globalSnackbarHostState.currentSnackbarData?.dismiss()
        globalSnackbarHostState.showSnackbar(message)
        viewModel.clearCalibrationMessage()
    }


    LaunchedEffect(driveSyncMessage) {
        val message = driveSyncMessage ?: return@LaunchedEffect
        globalSnackbarHostState.currentSnackbarData?.dismiss()
        globalSnackbarHostState.showSnackbar(message)
        viewModel.clearDriveSyncMessage()
    }

    val items = listOf(
        Screen.Speedtest,
        Screen.Dashboard,
        Screen.Settings
    )
    val topLevelRoutes = remember(items) { items.mapTo(linkedSetOf()) { it.route } }
    val topLevelRouteOrder = remember(items) { items.mapIndexed { index, screen -> screen.route to index }.toMap() }
    val haptic = LocalHapticFeedback.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    LaunchedEffect(currentDestination?.route) {
        onRouteChanged(currentDestination?.route)
    }
    val isDashboardLandscape =
        currentDestination?.hierarchy?.any { it.route == Screen.Dashboard.route } == true &&
            configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val showDashcamRecordingsSheet by viewModel.showDashcamRecordingsSheet.collectAsState()

    LaunchedEffect(navController) {
        MainActivityRouteRequests.current.collect { request ->
            navController.navigate(request.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    if (isInPictureInPictureMode) {
        TelemetryPipScreen(viewModel = viewModel)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val dashcamManager = remember { DashcamManager.getInstance(context) }
        val dashcamState by dashcamManager.state.collectAsState()

        Scaffold(
            modifier = Modifier.fillMaxSize().blur(if (showDashcamRecordingsSheet) 20.dp else 0.dp),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = {
                SnackbarHost(
                    hostState = globalSnackbarHostState,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                    snackbar = { data ->
                        Surface(
                            color = MaterialTheme.colorScheme.inverseSurface,
                            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                            shape = bezierRoundedShape(16.dp),
                            tonalElevation = 6.dp,
                            shadowElevation = 8.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = data.visuals.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.inverseOnSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                data.visuals.actionLabel?.let { action ->
                                    TextButton(
                                        onClick = { data.performAction() },
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text(action, color = MaterialTheme.colorScheme.inversePrimary)
                                    }
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                val isSettingsSubActive by viewModel.isSettingsSubPageActive.collectAsState()
                val showBottomBar = currentDestination?.route in topLevelRoutes && !isSettingsSubActive
                if (showBottomBar && !isDashboardLandscape) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
                        tonalElevation = 3.dp
                    ) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        items.forEach { screen ->
                            val isSelected =
                                currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = null) },
                                label = { Text(screen.title) },
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) return@NavigationBarItem
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.fillMaxSize(),
            enterTransition = {
                if (isTopLevelTabTransition(initialState.destination.route, targetState.destination.route, topLevelRoutes)) {
                    fadeIn(animationSpec = tween(120)) + slideInHorizontally(
                        animationSpec = tween(120),
                        initialOffsetX = { fullWidth ->
                            topLevelOffset(
                                fromRoute = initialState.destination.route,
                                toRoute = targetState.destination.route,
                                topLevelRouteOrder = topLevelRouteOrder,
                                fullWidth = fullWidth
                            )
                        }
                    )
                } else {
                    fadeIn(animationSpec = tween(90))
                }
            },
            exitTransition = {
                if (isTopLevelTabTransition(initialState.destination.route, targetState.destination.route, topLevelRoutes)) {
                    fadeOut(animationSpec = tween(90)) + slideOutHorizontally(
                        animationSpec = tween(90),
                        targetOffsetX = { fullWidth ->
                            -topLevelOffset(
                                fromRoute = initialState.destination.route,
                                toRoute = targetState.destination.route,
                                topLevelRouteOrder = topLevelRouteOrder,
                                fullWidth = fullWidth
                            ) / 2
                        }
                    )
                } else {
                    fadeOut(animationSpec = tween(90))
                }
            },
            popEnterTransition = { fadeIn(animationSpec = tween(120)) },
            popExitTransition = {
                fadeOut(animationSpec = tween(120))
            }
            ) {
                composable(Screen.Connect.route) {
                    ConnectScreen(
                        viewModel = viewModel,
                        onNavigateToDashboard = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    )
                }
                composable(Screen.Dashboard.route) { 
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToZhikeSettings = { navController.navigate(Screen.ZhikeSettings.route) },
                        onNavigateToPlayback = { segmentId ->
                            viewModel.selectPlaybackSegment(segmentId)
                            navController.navigate(Screen.DashcamPlayback.route)
                        }
                    ) 
                }
                composable(
                    route = Screen.Pairing.route,
                    enterTransition = {
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> fullWidth }
                        ) + fadeIn(animationSpec = tween(durationMillis = 200))
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> -fullWidth / 3 }
                        ) + fadeOut(animationSpec = tween(durationMillis = 200))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> -fullWidth / 3 }
                        ) + fadeIn(animationSpec = tween(durationMillis = 200))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> fullWidth }
                        ) + fadeOut(animationSpec = tween(durationMillis = 200))
                    }
                ) {
                    com.shawnrain.sdash.ui.navigation.PredictiveBackPage(onBack = { navController.popBackStack() }) { 
                        com.shawnrain.sdash.ui.settings.PairingScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(
                    route = Screen.Bms.route,
                    enterTransition = {
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> fullWidth }
                        ) + fadeIn(animationSpec = tween(durationMillis = 200))
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> -fullWidth / 3 }
                        ) + fadeOut(animationSpec = tween(durationMillis = 200))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> -fullWidth / 3 }
                        ) + fadeIn(animationSpec = tween(durationMillis = 200))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> fullWidth }
                        ) + fadeOut(animationSpec = tween(durationMillis = 200))
                    }
                ) {
                    PredictiveBackPage(onBack = { navController.popBackStack() }) {
                        BmsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(Screen.Speedtest.route) { SpeedtestScreen(viewModel = viewModel) }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToBms = { navController.navigate(Screen.Bms.route) },
                        onNavigateToZhikeSettings = { navController.navigate(Screen.ZhikeSettings.route) },
                        onNavigateToPairing = { navController.navigate(Screen.Pairing.route) }
                    )
                }
                composable(
                    route = Screen.ZhikeSettings.route,
                    enterTransition = {
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> fullWidth }
                        ) + fadeIn(animationSpec = tween(durationMillis = 200))
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> -fullWidth / 3 }
                        ) + fadeOut(animationSpec = tween(durationMillis = 200))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> -fullWidth / 3 }
                        ) + fadeIn(animationSpec = tween(durationMillis = 200))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> fullWidth }
                        ) + fadeOut(animationSpec = tween(durationMillis = 200))
                    }
                ) {
                    PredictiveBackPage(onBack = { navController.popBackStack() }) {
                        com.shawnrain.sdash.ui.settings.zhike.ZhikeSettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable(
                    route = Screen.DashcamPlayback.route,
                    enterTransition = {
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> fullWidth }
                        ) + fadeIn(animationSpec = tween(durationMillis = 200))
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> -fullWidth / 3 }
                        ) + fadeOut(animationSpec = tween(durationMillis = 200))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> -fullWidth / 3 }
                        ) + fadeIn(animationSpec = tween(durationMillis = 200))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                            targetOffsetX = { fullWidth -> fullWidth }
                        ) + fadeOut(animationSpec = tween(durationMillis = 200))
                    }
                ) {
                    DisposableEffect(Unit) {
                        onDispose {
                            viewModel.setShowDashcamRecordingsSheet(true)
                        }
                    }
                    PredictiveBackPage(onBack = {
                        navController.popBackStack()
                    }) {
                        DashcamPlaybackScreen(
                            segmentId = viewModel.selectedPlaybackSegmentId.collectAsState().value ?: "",
                            dashcamManager = DashcamManager.getInstance(LocalContext.current),
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }

            pendingRideStop?.let { pending ->
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text(pending.title) },
                    text = { Text(pending.message) },
                    confirmButton = {
                        Button(onClick = { viewModel.confirmRideStopCountdownNow() }) {
                            Text(
                                text = "立即结束",
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.cancelRideStopCountdown() }) {
                            Text(
                                text = "继续记录",
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                )
            }
        }
    }

    val segments by dashcamManager.repository.segments.collectAsState()
    val scope = rememberCoroutineScope()
    DashcamRecordingsSheet(
        isVisible = showDashcamRecordingsSheet,
        segments = segments,
        dashcamManager = dashcamManager,
        onDismissRequest = {
            viewModel.setShowDashcamRecordingsSheet(false)
            val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasCameraPermission && dashcamManager.state.value == DashcamState.IDLE) {
                dashcamManager.startPreviewOnly()
            }
        },
        onPlaySegment = { segment ->
            viewModel.setShowDashcamRecordingsSheet(false)
            viewModel.selectPlaybackSegment(segment.id)
            navController.navigate(Screen.DashcamPlayback.route)
        },
        onDeleteSegment = { segmentId ->
            scope.launch {
                dashcamManager.repository.deleteSegment(segmentId)
            }
        },
        onDeleteAll = {
            scope.launch {
                dashcamManager.repository.deleteAllSegments()
            }
        }
    )
    }
}

@Composable
private fun TelemetryPipScreen(
    viewModel: MainViewModel
) {
    val metrics by viewModel.metrics.collectAsState()
    val currentVehicle by viewModel.currentVehicle.collectAsState()
    val speedText = metrics.speedKmH.toInt().coerceAtLeast(0).toString()
    val powerKw = metrics.totalPowerW / 1000f

    // 18:9 ratio base size
    val baseWidth = 350.dp
    val baseHeight = 175.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        val scale = min(maxWidth / baseWidth, maxHeight / baseHeight)
        val scaledWidth = baseWidth * scale
        val scaledHeight = baseHeight * scale

        Box(
            modifier = Modifier
                .width(scaledWidth)
                .height(scaledHeight),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .requiredSize(baseWidth, baseHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Area: Speed Box centered (Unified app style)
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .fillMaxHeight()
                            .clip(bezierRoundedShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        val useMiSans = com.shawnrain.sdash.ui.theme.LocalUseMiSansFont.current
                        val pipFontFamily = if (useMiSans) com.shawnrain.sdash.ui.theme.MiSansFontFamily else androidx.compose.ui.text.font.FontFamily.Monospace
                        val pipFontFeatureSettings = if (useMiSans) "tnum" else null

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = speedText,
                                fontSize = 52.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                                fontFamily = pipFontFamily,
                                style = androidx.compose.material3.LocalTextStyle.current.copy(fontFeatureSettings = pipFontFeatureSettings),
                                color = MaterialTheme.colorScheme.primary,
                                lineHeight = 52.sp
                            )
                            Text(
                                text = "km/h",
                                fontSize = 12.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                fontFamily = pipFontFamily,
                                style = androidx.compose.material3.LocalTextStyle.current.copy(fontFeatureSettings = pipFontFeatureSettings),
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Right Area: 2 columns to form a perfect 2x2 grid
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Column 1: SOC & Voltage
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            // SOC
                            PipGridItem(
                                value = metrics.soc.toInt().coerceIn(0, 100).toString(),
                                unit = "%",
                                label = "SOC",
                                valueColor = when {
                                    metrics.soc < 20f -> Color(0xFFFF5252)
                                    metrics.soc < 40f -> Color(0xFFFF9800)
                                    else -> MaterialTheme.colorScheme.onBackground
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Voltage
                            val series = currentVehicle.batterySeries.coerceAtLeast(1)
                            val underVoltageThreshold = series * 3.2f
                            val isUnderVoltage = metrics.voltage < 45.0f || metrics.voltage < underVoltageThreshold
                            PipGridItem(
                                value = String.format("%.1f", metrics.voltage),
                                unit = "V",
                                label = "电压",
                                valueColor = if (isUnderVoltage) Color(0xFFFF9800) else MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Column 2: Range & Temp
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Range
                            PipGridItem(
                                value = metrics.estimatedRangeKm.toInt().coerceAtLeast(0).toString(),
                                unit = "km",
                                label = "续航"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Temp
                            val isOverTemp = metrics.controllerTemp > 65f
                            PipGridItem(
                                value = metrics.controllerTemp.toInt().toString(),
                                unit = "°C",
                                label = "温控",
                                valueColor = if (isOverTemp) Color(0xFFFF5252) else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // PowerBalanceBar at the bottom spanning the full width
                PipPowerBalanceBar(
                    powerKw = powerKw,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
        }
    }
}

@Composable
private fun PipPowerBalanceBar(
    powerKw: Float,
    modifier: Modifier = Modifier
) {
    var positivePeakKw by remember { mutableFloatStateOf(3.5f) }
    var regenPeakKw by remember { mutableFloatStateOf(0.8f) }

    LaunchedEffect(powerKw) {
        positivePeakKw = when {
            powerKw > 0f -> max(positivePeakKw * 0.985f, max(3.5f, powerKw * 1.12f))
            else -> max(positivePeakKw * 0.992f, 3.5f)
        }
        regenPeakKw = when {
            powerKw < 0f -> max(regenPeakKw * 0.978f, max(0.8f, abs(powerKw) * 1.12f))
            else -> max(regenPeakKw * 0.99f, 0.8f)
        }
    }

    val targetFraction = when {
        powerKw > 0f -> (powerKw / positivePeakKw).coerceIn(0f, 1f)
        powerKw < 0f -> (abs(powerKw) / regenPeakKw).coerceIn(0f, 1f)
        else -> 0f
    }
    val visualTargetFraction = remember(targetFraction, powerKw) {
        val hasActivePower = abs(powerKw) >= 0.08f
        if (!hasActivePower) 0f else {
            val raw = targetFraction.coerceIn(0f, 1f)
            if (powerKw >= 0f) {
                max(0.06f, raw.pow(0.82f))
            } else {
                max(0.16f, raw.pow(0.56f))
            }.coerceIn(0f, 1f)
        }
    }
    val animatedFraction by animateFloatAsState(
        targetValue = visualTargetFraction,
        animationSpec = tween(durationMillis = 180),
        label = "pip_power_balance_fraction"
    )
    val isOutput = powerKw >= 0f
    val hasActivePower = abs(powerKw) >= 0.08f
    val barColor = remember(animatedFraction, isOutput) {
        if (isOutput) {
            if (animatedFraction < 0.5f) {
                androidx.compose.ui.graphics.lerp(
                    Color(0xFF10B981), // 绿
                    Color(0xFFF59E0B), // 黄
                    animatedFraction * 2f
                )
            } else {
                androidx.compose.ui.graphics.lerp(
                    Color(0xFFF59E0B), // 黄
                    Color(0xFFEF4444), // 红
                    ((animatedFraction - 0.5f) * 2f).coerceIn(0f, 1f)
                )
            }
        } else {
            if (animatedFraction < 0.5f) {
                androidx.compose.ui.graphics.lerp(
                    Color(0xFFF59E0B), // 黄/橙
                    Color(0xFF84CC16), // 黄绿
                    animatedFraction * 2f
                )
            } else {
                androidx.compose.ui.graphics.lerp(
                    Color(0xFF84CC16), // 黄绿
                    Color(0xFF10B981), // 强绿
                    ((animatedFraction - 0.5f) * 2f).coerceIn(0f, 1f)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .clip(bezierRoundedShape(999.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight()
                .width(1.5.dp)
                .background(Color.White.copy(alpha = 0.38f))
        )
        if (hasActivePower) {
            Box(
                modifier = Modifier
                    .align(if (isOutput) Alignment.CenterEnd else Alignment.CenterStart)
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f * animatedFraction)
                    .clip(bezierRoundedShape(999.dp))
                    .background(barColor.copy(alpha = 0.86f))
            )
        }
    }
}

@Composable
private fun PipGridItem(
    value: String,
    unit: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
    unitColor: Color = Color.Gray
) {
    val useMiSans = com.shawnrain.sdash.ui.theme.LocalUseMiSansFont.current
    val pipFontFamily = if (useMiSans) com.shawnrain.sdash.ui.theme.MiSansFontFamily else androidx.compose.ui.text.font.FontFamily.Monospace
    val pipFontFeatureSettings = if (useMiSans) "tnum" else null

    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
            color = Color.Gray.copy(alpha = 0.9f),
            fontFamily = pipFontFamily,
            style = androidx.compose.material3.LocalTextStyle.current.copy(fontFeatureSettings = pipFontFeatureSettings)
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = value,
                fontSize = 30.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                fontFamily = pipFontFamily,
                style = androidx.compose.material3.LocalTextStyle.current.copy(fontFeatureSettings = pipFontFeatureSettings),
                color = valueColor,
                modifier = Modifier.alignByBaseline()
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = unit,
                fontSize = 12.sp,
                fontFamily = pipFontFamily,
                style = androidx.compose.material3.LocalTextStyle.current.copy(fontFeatureSettings = pipFontFeatureSettings),
                color = unitColor,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}

private fun isTopLevelTabTransition(
    fromRoute: String?,
    toRoute: String?,
    topLevelRoutes: Set<String>
): Boolean {
    return fromRoute in topLevelRoutes && toRoute in topLevelRoutes
}

private fun topLevelOffset(
    fromRoute: String?,
    toRoute: String?,
    topLevelRouteOrder: Map<String, Int>,
    fullWidth: Int
): Int {
    val fromIndex = topLevelRouteOrder[fromRoute] ?: return fullWidth / 24
    val toIndex = topLevelRouteOrder[toRoute] ?: return fullWidth / 24
    val direction = if (toIndex >= fromIndex) 1 else -1
    return (fullWidth / 24) * direction
}

sealed class Screen(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Connect : Screen("connect", "连接", Icons.Filled.Bluetooth)
    data object Dashboard : Screen("dashboard", "仪表", Icons.Filled.Speed)
    data object Speedtest : Screen("speedtest", "加速", Icons.Filled.RocketLaunch)
    data object Settings : Screen("settings", "设置", Icons.Filled.Settings)
    data object Bms : Screen("bms", "电池与BMS", Icons.Filled.BatteryChargingFull)
    data object ZhikeSettings : Screen("zhike_settings", "智科调校", Icons.Filled.Settings)
    data object Pairing : Screen("pairing", "iPhone 配对", Icons.Filled.Bluetooth)
    data object DashcamPlayback : Screen("dashcam_playback", "录像回放", Icons.Filled.PlayArrow)
}
