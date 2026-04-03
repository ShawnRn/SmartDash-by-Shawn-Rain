package com.shawnrain.habe

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.shawnrain.habe.ui.bms.BmsScreen
import com.shawnrain.habe.ui.connect.ConnectScreen
import com.shawnrain.habe.ui.dashboard.DashboardScreen
import com.shawnrain.habe.ui.settings.SettingsScreen
import com.shawnrain.habe.ui.speedtest.SpeedtestScreen
import com.shawnrain.habe.ui.theme.HabeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabeTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(viewModel, lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
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

    val isDrivingMode by viewModel.isDrivingMode.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isDashboardLandscape =
        currentDestination?.hierarchy?.any { it.route == Screen.Dashboard.route } == true &&
            configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!isDrivingMode && !isDashboardLandscape) {
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
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = null) },
                                label = { Text(screen.title) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
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
                androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        150, easing = androidx.compose.animation.core.FastOutLinearInEasing
                    )
                ) + androidx.compose.animation.scaleIn(
                    initialScale = 0.92f,
                    animationSpec = androidx.compose.animation.core.tween(
                        150, easing = androidx.compose.animation.core.FastOutLinearInEasing
                    )
                )
            },
            exitTransition = {
                androidx.compose.animation.fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(
                        100, easing = androidx.compose.animation.core.FastOutLinearInEasing
                    )
                )
            }
        ) {
            composable(Screen.Connect.route) { 
                ConnectScreen(viewModel = viewModel, onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel = viewModel) }
            composable(Screen.Bms.route) { BmsScreen(viewModel = viewModel, onBack = { navController.popBackStack() }) }
            composable(Screen.Speedtest.route) { SpeedtestScreen(viewModel = viewModel) }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    viewModel = viewModel, 
                    onNavigateToBms = { navController.navigate(Screen.Bms.route) },
                    onNavigateToZhikeSettings = { navController.navigate(Screen.ZhikeSettings.route) }
                ) 
            }
            composable(Screen.ZhikeSettings.route) {
                com.shawnrain.habe.ui.settings.zhike.ZhikeSettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Connect : Screen("connect", "连接", Icons.Filled.Bluetooth)
    object Dashboard : Screen("dashboard", "仪表", Icons.Filled.Speed)
    object Speedtest : Screen("speedtest", "百公里", Icons.Filled.RocketLaunch)
    object Settings : Screen("settings", "设置", Icons.Filled.Settings)
    object Bms : Screen("bms", "电池", Icons.Filled.BatteryChargingFull)
    object ZhikeSettings : Screen("zhike_settings", "智科调校", Icons.Filled.Settings)
}
