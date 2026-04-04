package com.shawnrain.habe

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Rational
import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shawnrain.habe.debug.AppLogger
import com.shawnrain.habe.ui.bms.BmsScreen
import com.shawnrain.habe.ui.connect.ConnectScreen
import com.shawnrain.habe.ui.dashboard.BaselineMetricValue
import com.shawnrain.habe.ui.dashboard.DashboardScreen
import com.shawnrain.habe.ui.navigation.PredictiveBackPage
import com.shawnrain.habe.ui.settings.SettingsScreen
import com.shawnrain.habe.ui.speedtest.SpeedtestScreen
import com.shawnrain.habe.ui.theme.HabeTheme
import kotlin.math.min
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainActivity : ComponentActivity() {
    private val isInPipModeState = mutableStateOf(false)
    private var currentRoute: String? = null
    private var pipEnabled = false
    private var pendingTelemetryPip = false

    companion object {
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
        dispatchLaunchIntent(intent)
        enableEdgeToEdge()
        setContent {
            HabeTheme {
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
                    }
                )
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
        requestTelemetryPictureInPicture()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipModeState.value = isInPictureInPictureMode
        if (!isInPictureInPictureMode) {
            pendingTelemetryPip = false
        }
    }

    private fun dispatchLaunchIntent(intent: Intent?) {
        intent
            ?.getStringExtra(EXTRA_TARGET_ROUTE)
            ?.takeIf { it.isNotBlank() }
            ?.let(MainActivityRouteRequests::emit)
    }

    private fun requestTelemetryPictureInPicture() {
        if (!pipEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.O || isInPictureInPictureMode) {
            return
        }
        if (currentRoute == Screen.Dashboard.route) {
            enterTelemetryPictureInPicture()
        } else {
            pendingTelemetryPip = false
            AppLogger.d("MainActivity", "Skip PiP because current route is $currentRoute")
        }
    }

    private fun enterTelemetryPictureInPicture() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || isInPictureInPictureMode) {
            return
        }
        val builder = PictureInPictureParams.Builder()
            .setAspectRatio(TELEMETRY_PIP_ASPECT_RATIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setSeamlessResizeEnabled(false)
            // Android 12+ supports system double-tap to toggle between normal and expanded PiP.
            // Keep the expanded state only slightly larger so it stays compact on bike use.
            builder.setExpandedAspectRatio(TELEMETRY_PIP_EXPANDED_ASPECT_RATIO)
        }
        val params = builder.build()
        runCatching {
            enterPictureInPictureMode(params)
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

    val items = listOf(
        Screen.Dashboard,
        Screen.Speedtest,
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
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
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
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel = viewModel) }
            composable(
                route = Screen.Bms.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                PredictiveBackPage(onBack = { navController.popBackStack() }) {
                    BmsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
            }
            composable(Screen.Speedtest.route) { SpeedtestScreen(viewModel = viewModel) }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToBms = { navController.navigate(Screen.Bms.route) },
                    onNavigateToZhikeSettings = { navController.navigate(Screen.ZhikeSettings.route) }
                )
            }
            composable(
                route = Screen.ZhikeSettings.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                PredictiveBackPage(onBack = { navController.popBackStack() }) {
                    com.shawnrain.habe.ui.settings.zhike.ZhikeSettingsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
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
    data object Bms : Screen("bms", "电池", Icons.Filled.BatteryChargingFull)
    data object ZhikeSettings : Screen("zhike_settings", "智科设置", Icons.Filled.Settings)
}
