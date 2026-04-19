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
import kotlin.math.min
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainActivity : ComponentActivity() {
    private val isInPipModeState = mutableStateOf(false)
    private var currentRoute: String? = null
    private var pipEnabled = false
    private var pendingTelemetryPip = false
    private var skipNextPipForSystemBack = false
    private var systemBackObserver: OnBackInvokedCallback? = null

    companion object {
        private const val BACK_CHAIN_TAG = "MainActivityBack"
        private const val EXTRA_TARGET_ROUTE = "target_route"
        private val TELEMETRY_PIP_ASPECT_RATIO = Rational(25, 14)
        private val TELEMETRY_PIP_EXPANDED_ASPECT_RATIO = Rational(8, 5)

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
            HabeTheme {
                PermissionBootstrapGate {
                    MainScreen(
                        isInPictureInPictureMode = isInPipModeState.value,
                        onRouteChanged = { route ->
                            currentRoute = route
                            updateOrientationForRoute(route)
                            if (pendingTelemetryPip && route == Screen.Dashboard.route) {
                                pendingTelemetryPip = false
                                enterTelemetryPictureInPicture()
                            }
                        },
                        onPipPreferenceChanged = { enabled ->
                            pipEnabled = enabled
                            AppLogger.i(BACK_CHAIN_TAG, "PiP preference changed enabled=$enabled")
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
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            AppLogger.i(BACK_CHAIN_TAG, "requestPiP skipped because activity is not resumed")
            return
        }
        if (!pipEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.O || isInPictureInPictureMode) {
            AppLogger.i(BACK_CHAIN_TAG, "requestPiP skipped by guard")
            return
        }
        if (currentRoute == Screen.Dashboard.route) {
            AppLogger.i(BACK_CHAIN_TAG, "requestPiP proceed to enterPiP")
            enterTelemetryPictureInPicture()
        } else {
            pendingTelemetryPip = false
            AppLogger.i(BACK_CHAIN_TAG, "requestPiP skipped because current route is $currentRoute")
        }
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
    onPipPreferenceChanged: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = remember(context) { context.applicationContext as Application }
    val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )
    val configuration = LocalConfiguration.current
    val pipEnabled by viewModel.overlayEnabled.collectAsState()
    val pendingRideStop by viewModel.pendingRideStop.collectAsState()
    val calibrationMessage by viewModel.calibrationMessage.collectAsState()
    val lanBackupMessage by viewModel.lanBackupMessage.collectAsState()
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

    LaunchedEffect(lanBackupMessage) {
        val message = lanBackupMessage ?: return@LaunchedEffect
        globalSnackbarHostState.currentSnackbarData?.dismiss()
        globalSnackbarHostState.showSnackbar(message)
        viewModel.clearLanBackupMessage()
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                hostState = globalSnackbarHostState,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
            )
        },
        bottomBar = {
            if (!isDashboardLandscape) {
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
                        onNavigateToZhikeSettings = { navController.navigate(Screen.ZhikeSettings.route) }
                    ) 
                }
                composable(
                    route = Screen.Pairing.route,
                    enterTransition = {
                        slideInVertically(
                            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                            initialOffsetY = { fullHeight -> fullHeight }
                        ) + fadeIn(animationSpec = tween(durationMillis = 300))
                    },
                    exitTransition = {
                        slideOutVertically(
                            animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing),
                            targetOffsetY = { fullHeight -> fullHeight }
                        ) + fadeOut(animationSpec = tween(durationMillis = 250))
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
                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> (fullWidth * 0.14f).toInt() }
                        ) + fadeIn(animationSpec = tween(durationMillis = 240, easing = LinearOutSlowInEasing))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(durationMillis = 160, easing = FastOutLinearInEasing))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 260, easing = FastOutLinearInEasing),
                            targetOffsetX = { fullWidth -> (fullWidth * 0.16f).toInt() }
                        ) + fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing))
                    }
                ) {
                    PredictiveBackPage(onBack = { navController.popBackStack() }) {
                        BmsScreen(viewModel = viewModel)
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
                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> (fullWidth * 0.14f).toInt() }
                        ) + fadeIn(animationSpec = tween(durationMillis = 240, easing = LinearOutSlowInEasing))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(durationMillis = 160, easing = FastOutLinearInEasing))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 260, easing = FastOutLinearInEasing),
                            targetOffsetX = { fullWidth -> (fullWidth * 0.16f).toInt() }
                        ) + fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing))
                    }
                ) {
                    PredictiveBackPage(onBack = { navController.popBackStack() }) {
                        com.shawnrain.sdash.ui.settings.zhike.ZhikeSettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
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
}

@Composable
private fun TelemetryPipScreen(
    viewModel: MainViewModel
) {
    val metrics by viewModel.metrics.collectAsState()
    val speedText = metrics.speedKmH.toInt().coerceAtLeast(0).toString()
    val baseWidth = 300.dp
    val baseHeight = 168.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)),
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
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    BaselineMetricValue(
                        value = speedText,
                        unit = "km/h",
                        valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unitColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                        valueFontSize = 64.sp,
                        unitFontSize = 26.sp,
                        valueLineHeight = 64.sp,
                        unitSpacing = 6.dp,
                        singleLine = true,
                        textAlign = TextAlign.Center
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BaselineMetricValue(
                        value = String.format("%.2f", metrics.totalPowerW / 1000f),
                        unit = "kW",
                        valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unitColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                        valueFontSize = 28.sp,
                        unitFontSize = 18.sp,
                        unitSpacing = 4.dp,
                        singleLine = true
                    )
                    BaselineMetricValue(
                        value = String.format("%.1f", metrics.avgEfficiencyWhKm),
                        unit = "Wh/km",
                        valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unitColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                        valueFontSize = 28.sp,
                        unitFontSize = 18.sp,
                        unitSpacing = 4.dp,
                        singleLine = true
                    )
                }
            }
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
}
