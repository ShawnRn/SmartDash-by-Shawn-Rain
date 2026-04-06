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
 * Manages AirPods-like auto-connect experience for BLE controllers.
 *
 * Behavior:
 * 1. On app start, immediately tries to connect to last known controller
 * 2. If not found, scans for all known controllers in priority order
 * 3. Background scanning continues until a known device is found
 * 4. When disconnected, automatically attempts to reconnect
 * 5. User never needs to manually tap "connect" for known devices
 */
class AutoConnectManager(
    private val context: Context,
    private val bleManager: BleManager,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "AutoConnect"
        private const val INITIAL_RETRY_DELAY_MS = 1000L
        private const val MAX_RETRY_DELAY_MS = 10_000L
        private const val SCAN_INTERVAL_MS = 5000L
        private const val MAX_SCAN_ATTEMPTS = 6 // 30 seconds total
    }

    /**
     * Priority-ordered list of known controllers to try connecting to.
     * Last connected device has highest priority.
     */
    data class KnownController(
        val address: String,
        val name: String? = null,
        val protocolId: String? = null,
        val lastConnectedAt: Long = 0L,
        val connectAttempts: Int = 0,
        val lastAttemptAt: Long = 0L
    ) {
        val priority: Int get() = when {
            lastConnectedAt > 0 -> 0 // Last connected = highest priority
            connectAttempts == 0 -> 1 // Never tried = medium priority
            else -> 2 // Previously failed = lowest priority
        }
    }

    private val _state = MutableStateFlow<AutoConnectState>(AutoConnectState.Idle)
    val state: StateFlow<AutoConnectState> = _state.asStateFlow()

    private val knownControllers = mutableMapOf<String, KnownController>()
    private var autoConnectJob: Job? = null
    private var isConnecting = false

    /**
     * Registers a known controller for auto-connect.
     * Call this for every controller the user has ever connected to.
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

    /**
     * Removes a controller from known list.
     */
    fun removeController(address: String) {
        knownControllers.remove(address)
        AppLogger.d(TAG, "移除已知控制器: $address")
    }

    /**
     * Starts the auto-connect process.
     * Should be called when app starts or user enables auto-connect.
     */
    fun start() {
        stop()
        AppLogger.i(TAG, "启动自动连接管理，已知设备数: ${knownControllers.size}")

        if (knownControllers.isEmpty()) {
            _state.value = AutoConnectState.Idle
            return
        }

        autoConnectJob = scope.launch {
            performAutoConnect()
        }
    }

    /**
     * Stops auto-connect process.
     * Call when user manually disconnects or disables auto-connect.
     */
    fun stop() {
        autoConnectJob?.cancel()
        autoConnectJob = null
        isConnecting = false
        AppLogger.i(TAG, "停止自动连接管理")
    }

    /**
     * Marks a controller as successfully connected.
     * Updates priority and resets failure counters.
     */
    fun onConnected(address: String, name: String? = null) {
        val controller = knownControllers[address]
        knownControllers[address] = controller?.copy(
            name = name ?: controller.name,
            lastConnectedAt = System.currentTimeMillis(),
            connectAttempts = 0,
            lastAttemptAt = 0L
        ) ?: KnownController(
            address = address,
            name = name,
            lastConnectedAt = System.currentTimeMillis()
        )
        isConnecting = false
        _state.value = AutoConnectState.Connected(address, name)
        AppLogger.i(TAG, "控制器已连接: $address")
    }

    /**
     * Marks a controller as disconnected.
     * Triggers auto-reconnect attempt.
     */
    fun onDisconnected(address: String) {
        isConnecting = false
        _state.value = AutoConnectState.Searching
        AppLogger.w(TAG, "控制器断开: $address，准备自动重连")

        // Short delay before attempting reconnect (avoid rapid reconnect loops)
        scope.launch {
            delay(2000)
            if (_state.value is AutoConnectState.Searching) {
                start()
            }
        }
    }

    /**
     * Force connection to a specific controller.
     * Used when user explicitly selects a device.
     */
    fun connectTo(address: String) {
        stop()
        scope.launch {
            val controller = knownControllers[address]
            if (controller != null) {
                attemptConnect(controller)
            } else {
                _state.value = AutoConnectState.Error("未知设备: $address")
            }
        }
    }

    // ======== Internal Implementation ========

    private suspend fun performAutoConnect() {
        // Sort by priority: last connected first
        val sortedDevices = knownControllers.values.sortedWith(
            compareBy({ it.priority }, { -it.lastConnectedAt })
        )

        if (sortedDevices.isEmpty()) {
            _state.value = AutoConnectState.Idle
            return
        }

        _state.value = AutoConnectState.Searching
        AppLogger.i(TAG, "开始自动连接流程，尝试 ${sortedDevices.size} 个设备")

        // Phase 1: Try each known device once (fast path)
        for (controller in sortedDevices) {
            if (isConnecting) return
            if (attemptConnect(controller)) return
        }

        // Phase 2: Background scanning with retry
        var scanAttempts = 0
        while (scanAttempts < MAX_SCAN_ATTEMPTS && !isConnecting) {
            scanAttempts++
            AppLogger.d(TAG, "后台扫描第 $scanAttempts/$MAX_SCAN_ATTEMPTS 次")
            _state.value = AutoConnectState.Searching

            // Start scan and wait for known device
            val foundDevice = scanForKnownDevices(sortedDevices)
            if (foundDevice != null) {
                if (attemptConnect(foundDevice)) return
            }

            // Exponential backoff between scans
            val delayMs = (INITIAL_RETRY_DELAY_MS * (1L shl (scanAttempts - 1)))
                .coerceAtMost(MAX_RETRY_DELAY_MS)
            delay(delayMs)
        }

        // Phase 3: Give up with user-friendly message
        if (!isConnecting) {
            _state.value = AutoConnectState.NotFound(
                knownCount = knownControllers.size,
                lastAttemptedAt = sortedDevices.firstOrNull()?.lastAttemptAt ?: 0L
            )
        }
    }

    private suspend fun attemptConnect(controller: KnownController): Boolean {
        if (isConnecting) return false
        isConnecting = true

        val now = System.currentTimeMillis()
        val updated = controller.copy(
            connectAttempts = controller.connectAttempts + 1,
            lastAttemptAt = now
        )
        knownControllers[controller.address] = updated

        _state.value = AutoConnectState.Connecting(controller.address, controller.name)
        AppLogger.i(
            TAG,
            "尝试连接 ${controller.name ?: controller.address} " +
                    "(第 ${updated.connectAttempts} 次)"
        )

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
            return false
        }
    }

    private suspend fun scanForKnownDevices(devices: List<KnownController>): KnownController? {
        // Start BLE scan
        bleManager.startScan()

        // Wait for scan results and check if any known device appears
        val scanTimeout = 10_000L
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < scanTimeout) {
            delay(500)

            val scannedDevices = bleManager.scannedDevices.value
            for (scanned in scannedDevices) {
                val known = devices.find { it.address == scanned.address }
                if (known != null) {
                    AppLogger.i(TAG, "发现已知设备: ${known.name} (${known.address})")
                    bleManager.stopScan()
                    return known
                }
            }
        }

        bleManager.stopScan()
        return null
    }
}

/**
 * States for auto-connect manager.
 */
sealed class AutoConnectState {
    /** No known controllers registered */
    object Idle : AutoConnectState()

    /** Scanning for known devices */
    object Searching : AutoConnectState()

    /** Attempting to connect to specific device */
    data class Connecting(val address: String, val name: String?) : AutoConnectState()

    /** Successfully connected */
    data class Connected(val address: String, val name: String? = null) : AutoConnectState()

    /** No known devices found after multiple attempts */
    data class NotFound(
        val knownCount: Int,
        val lastAttemptedAt: Long
    ) : AutoConnectState()

    /** Error during connection process */
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
