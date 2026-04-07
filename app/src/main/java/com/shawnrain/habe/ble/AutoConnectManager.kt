package com.shawnrain.habe.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.shawnrain.habe.debug.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * AirPods-like auto-connect experience for BLE controllers.
 *
 * Core behavior:
 * 1. App foreground → immediately try direct connect to last known device
 * 2. Direct connect fails → start targeted scan (5s timeout, LOW_LATENCY)
 * 3. App background → start low-power scan, keep listening for target device
 * 4. Target device found → immediately stop scan and connect
 * 5. User never needs to manually tap "connect" for known devices
 */
class AutoConnectManager(
    private val context: Context,
    private val bleManager: BleManager,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "AutoConnect"
        private const val RECONNECT_DELAY_MS = 1500L
    }

    data class KnownController(
        val address: String,
        val name: String? = null,
        val protocolId: String? = null,
        val lastConnectedAt: Long = 0L
    )

    private val _state = MutableStateFlow<AutoConnectState>(AutoConnectState.Idle)
    val state: StateFlow<AutoConnectState> = _state.asStateFlow()

    private val knownControllers = mutableMapOf<String, KnownController>()
    private var autoConnectJob: Job? = null
    private var isConnecting = false
    private var backgroundScanJob: Job? = null

    /**
     * Registers a known controller for auto-connect.
     */
    fun registerController(
        address: String,
        name: String? = null,
        protocolId: String? = null,
        lastConnectedAt: Long = System.currentTimeMillis()
    ) {
        val existing = knownControllers[address]
        knownControllers[address] = existing?.copy(
            name = name ?: existing.name,
            protocolId = protocolId ?: existing.protocolId,
            lastConnectedAt = maxOf(lastConnectedAt, existing.lastConnectedAt)
        ) ?: KnownController(
            address = address,
            name = name,
            protocolId = protocolId,
            lastConnectedAt = lastConnectedAt
        )
        AppLogger.d(TAG, "注册已知控制器: $address ($name)")
    }

    fun removeController(address: String) {
        knownControllers.remove(address)
        AppLogger.d(TAG, "移除已知控制器: $address")
    }

    /**
     * Start auto-connect: immediately try direct connect to last known device.
     * This is the fast path for AirPods experience.
     */
    fun start() {
        stop()
        AppLogger.i(TAG, "启动自动连接，已知设备数: ${knownControllers.size}")

        if (knownControllers.isEmpty()) {
            _state.value = AutoConnectState.Idle
            return
        }

        // Get the last connected device (highest priority)
        val lastDevice = knownControllers.values
            .filter { it.lastConnectedAt > 0 }
            .maxByOrNull { it.lastConnectedAt }

        if (lastDevice != null) {
            // Fast path: immediately try direct connect
            AppLogger.i(TAG, "🚀 快速直连最后设备: ${lastDevice.name} (${lastDevice.address})")
            scope.launch {
                attemptConnect(lastDevice)
            }
        } else {
            // No last connected device, start scanning
            startBackgroundScan()
        }
    }

    fun stop() {
        autoConnectJob?.cancel()
        autoConnectJob = null
        backgroundScanJob?.cancel()
        backgroundScanJob = null
        isConnecting = false
        AppLogger.i(TAG, "停止自动连接")
    }

    /**
     * Call when app goes to foreground: try direct connect immediately
     */
    fun onAppForeground() {
        val lastDevice = knownControllers.values
            .filter { it.lastConnectedAt > 0 }
            .maxByOrNull { it.lastConnectedAt }

        if (lastDevice != null) {
            AppLogger.i(TAG, "📱 App 前台，快速直连: ${lastDevice.address}")
            scope.launch {
                attemptConnect(lastDevice)
            }
        }
    }

    /**
     * Call when app goes to background: start low-power background scanning
     */
    fun onAppBackground() {
        AppLogger.i(TAG, "🌙 App 后台，启动低功耗扫描")
        startBackgroundScan()
    }

    fun onConnected(address: String, name: String? = null) {
        val controller = knownControllers[address]
        knownControllers[address] = controller?.copy(
            name = name ?: controller.name,
            lastConnectedAt = System.currentTimeMillis()
        ) ?: KnownController(
            address = address,
            name = name,
            lastConnectedAt = System.currentTimeMillis()
        )
        isConnecting = false
        backgroundScanJob?.cancel()
        backgroundScanJob = null
        _state.value = AutoConnectState.Connected(address, name)
        AppLogger.i(TAG, "✅ 控制器已连接: $address")
    }

    fun onDisconnected(address: String) {
        isConnecting = false
        _state.value = AutoConnectState.Searching
        AppLogger.w(TAG, "⚠️ 控制器断开: $address，准备自动重连")

        scope.launch {
            delay(RECONNECT_DELAY_MS)
            if (_state.value is AutoConnectState.Searching) {
                startBackgroundScan()
            }
        }
    }

    fun connectTo(address: String) {
        stop()
        val controller = knownControllers[address]
        if (controller != null) {
            scope.launch {
                attemptConnect(controller)
            }
        } else {
            _state.value = AutoConnectState.Error("未知设备: $address")
        }
    }

    // ======== Internal Implementation ========

    private suspend fun attemptConnect(controller: KnownController): Boolean {
        if (isConnecting) return false
        isConnecting = true

        knownControllers[controller.address] = controller.copy(
            lastConnectedAt = System.currentTimeMillis()
        )

        _state.value = AutoConnectState.Connecting(controller.address, controller.name)
        AppLogger.i(TAG, "🔗 尝试连接 ${controller.name ?: controller.address}")

        val success = bleManager.connect(
            address = controller.address,
            nameHint = controller.name,
            protocolIdHint = controller.protocolId
        )

        if (success) {
            onConnected(controller.address)
            return true
        } else {
            isConnecting = false
            // Direct connect failed, start targeted scan
            AppLogger.w(TAG, "直连失败，启动针对性扫描")
            startTargetedScanForController(controller)
            return false
        }
    }

    /**
     * Start targeted scan for a specific device - this is the AirPods magic.
     * Scans with LOW_LATENCY mode, immediately connects when device is found.
     */
    private fun startTargetedScanForController(controller: KnownController) {
        AppLogger.i(TAG, "🎯 启动针对 ${controller.address} 的快速扫描")

        bleManager.startTargetedScan(controller.address) { device ->
            // Device found! Immediately connect
            scope.launch {
                AppLogger.i(TAG, "🎯 发现目标，立即连接: ${controller.address}")
                bleManager.connect(
                    address = controller.address,
                    nameHint = controller.name,
                    protocolIdHint = controller.protocolId
                )
            }
        }
    }

    /**
     * Start low-power background scan for all known devices.
     * This runs when app is in background, listening for the target device.
     */
    private fun startBackgroundScan() {
        if (knownControllers.isEmpty()) return

        backgroundScanJob?.cancel()
        backgroundScanJob = scope.launch {
            // Get the last connected device to scan for
            val lastDevice = knownControllers.values
                .filter { it.lastConnectedAt > 0 }
                .maxByOrNull { it.lastConnectedAt }

            if (lastDevice != null) {
                AppLogger.i(TAG, "🔍 后台扫描最后连接设备: ${lastDevice.address}")
                bleManager.startTargetedScan(lastDevice.address) { device ->
                    // Found! Connect immediately
                    scope.launch {
                        bleManager.connect(
                            address = lastDevice.address,
                            nameHint = lastDevice.name,
                            protocolIdHint = lastDevice.protocolId
                        )
                    }
                }
            }
        }
    }
}

/**
 * States for auto-connect manager.
 */
sealed class AutoConnectState {
    object Idle : AutoConnectState()
    object Searching : AutoConnectState()
    data class Connecting(val address: String, val name: String?) : AutoConnectState()
    data class Connected(val address: String, val name: String? = null) : AutoConnectState()
    data class NotFound(
        val knownCount: Int,
        val lastAttemptedAt: Long
    ) : AutoConnectState()
    data class Error(val message: String) : AutoConnectState()
}

/** Display name for auto-connect state */
val AutoConnectState.displayName: String
    get() = when (val state = this) {
        is AutoConnectState.Connected -> state.name?.takeIf { it.isNotBlank() } ?: state.address
        is AutoConnectState.Connecting -> state.name?.takeIf { it.isNotBlank() } ?: state.address
        is AutoConnectState.NotFound -> "未找到设备"
        is AutoConnectState.Searching -> "搜索中..."
        is AutoConnectState.Error -> "连接失败"
        is AutoConnectState.Idle -> "未连接"
    }

val AutoConnectState.isConnecting: Boolean get() = this is AutoConnectState.Searching || this is AutoConnectState.Connecting
val AutoConnectState.isConnected: Boolean get() = this is AutoConnectState.Connected
