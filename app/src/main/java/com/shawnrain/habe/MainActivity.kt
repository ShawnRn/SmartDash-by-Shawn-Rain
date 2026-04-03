package com.shawnrain.habe

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.SystemClock
import android.os.Bundle
import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.shawnrain.habe.ui.bms.BmsScreen
import com.shawnrain.habe.ui.connect.ConnectScreen
import com.shawnrain.habe.ui.dashboard.DashboardScreen
import com.shawnrain.habe.ui.settings.SettingsScreen
import com.shawnrain.habe.ui.speedtest.SpeedtestScreen
import com.shawnrain.habe.ui.theme.HabeTheme

import com.shawnrain.habe.debug.AppLogger
import java.util.concurrent.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_TARGET_ROUTE = "target_route"

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

    private val appViewModel: MainViewModel by lazy(LazyThreadSafetyMode.NONE) {
        val application = application as HabeApplication
        ViewModelProvider(application, application.appViewModelFactory)[MainViewModel::class.java]
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
                MainScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dispatchLaunchIntent(intent)
    }

    private fun dispatchLaunchIntent(intent: Intent?) {
        intent
            ?.getStringExtra(EXTRA_TARGET_ROUTE)
            ?.takeIf { it.isNotBlank() }
            ?.let(MainActivityRouteRequests::emit)
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
        _current.tryEmit(RouteRequest(
            id = SystemClock.elapsedRealtimeNanos(),
            route = route
        ))
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = remember(context) { context.applicationContext as HabeApplication }
    val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        viewModelStoreOwner = application,
        factory = application.appViewModelFactory
    )
    val configuration = LocalConfiguration.current

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
    
    val items = listOf(
        Screen.Connect,
        Screen.Dashboard,
        Screen.Speedtest,
        Screen.Settings
    )
    val topLevelRoutes = remember(items) { items.mapTo(linkedSetOf()) { it.route } }
    val topLevelRouteOrder = remember(items) { items.mapIndexed { index, screen -> screen.route to index }.toMap() }
    val haptic = LocalHapticFeedback.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!isDashboardLandscape) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    tonalElevation = 3.dp
                ) {
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                ConnectScreen(viewModel = viewModel, onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                        launchSingleTop = true
                        restoreState = false
                    }
                })
            }
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel = viewModel) }
            composable(Screen.Bms.route) {
                PredictiveBackRoute(onBack = { navController.popBackStack() }) {
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
            composable(Screen.ZhikeSettings.route) {
                PredictiveBackRoute(onBack = { navController.popBackStack() }) {
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
private fun PredictiveBackRoute(
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val progress = remember { Animatable(0f) }
    var swipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = remember(maxWidth, density) { with(density) { maxWidth.toPx() } }

        PredictiveBackHandler(enabled = true) { backEvents ->
            try {
                progress.snapTo(0f)
                backEvents.collect { event ->
                    swipeEdge = event.swipeEdge
                    progress.snapTo(event.progress)
                }
                onBack()
            } catch (_: CancellationException) {
                progress.animateTo(0f, animationSpec = tween(180))
            }
        }

        val direction = if (swipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
        val animatedProgress = progress.value
        val scale = 1f - (0.06f * animatedProgress)
        val translationX = direction * widthPx * 0.1f * animatedProgress
        val alpha = 1f - (0.06f * animatedProgress)
        val transformOrigin = if (direction > 0f) {
            TransformOrigin(0f, 0.5f)
        } else {
            TransformOrigin(1f, 0.5f)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.translationX = translationX
                    this.alpha = alpha
                    this.transformOrigin = transformOrigin
                },
            contentAlignment = Alignment.Center
        ) {
            content()
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

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Connect : Screen("connect", "连接", Icons.Filled.Bluetooth)
    object Dashboard : Screen("dashboard", "仪表", Icons.Filled.Speed)
    object Speedtest : Screen("speedtest", "加速", Icons.Filled.RocketLaunch)
    object Settings : Screen("settings", "设置", Icons.Filled.Settings)
    object Bms : Screen("bms", "电池", Icons.Filled.BatteryChargingFull)
    object ZhikeSettings : Screen("zhike_settings", "智科设置", Icons.Filled.Settings)
}
