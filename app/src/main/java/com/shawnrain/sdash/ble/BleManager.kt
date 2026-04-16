package com.shawnrain.sdash.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import com.shawnrain.sdash.ble.ProtocolParser
import com.shawnrain.sdash.debug.AppLogger
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val device: BluetoothDevice) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

/**
 * Represents the result of a BLE write operation.
 */
sealed class WriteResult {
    object Success : WriteResult()
    data class Dropped(val reason: String) : WriteResult()
    data class Failed(val status: Int) : WriteResult()
}

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {
    companion object {
        private const val TAG = "BleManager"
        private const val CCC_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb"
        private const val SCAN_TIMEOUT_MS = 10_000L // 10 seconds
        private const val GATT_MTU = 247 // Request larger MTU for better throughput
        private const val TARGET_SCAN_TIMEOUT_MS = 5000L // 针对已知设备的快速扫描超时
    }

    private enum class WriteRoute {
        DEFAULT,
        ZHIKE_MAIN,
        ZHIKE_AUX
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDevice>> = _scannedDevices.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _rawData = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val rawData: SharedFlow<ByteArray> = _rawData.asSharedFlow()

    // Write result telemetry for observability
    private val _writeResults = MutableSharedFlow<WriteResult>(extraBufferCapacity = 16)
    val writeResults: SharedFlow<WriteResult> = _writeResults.asSharedFlow()

    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var zhikeMainWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var zhikeAuxWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var zhikeHandshakeCharacteristic: BluetoothGattCharacteristic? = null
    private var activeProtocolId: String? = null
    private var pendingDeviceNameHint: String? = null
    private var pendingProtocolIdHint: String? = null

    private val pollingHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val pollingRunnable = object : Runnable {
        override fun run() {
            if (activeProtocolId == "zhike") {
                sendZhikeMainCommand("AA13FF01AA130001")
            }
            pollingHandler.postDelayed(this, 100)
        }
    }

    private var targetDeviceAddress: String? = null
    private var onTargetDeviceFound: ((BluetoothDevice) -> Unit)? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            // Android 16 (SDK 36) Optimization: Prefer name from ScanRecord (advertisement) 
            // over BluetoothDevice.name which often returns null for unbonded devices.
            val name = result.scanRecord?.deviceName ?: safeDeviceName(device)
            val rssi = result.rssi

            AppLogger.v(TAG, "发现设备信号: ${name ?: "Unknown"} (${device.address}) rssi=$rssi")

            // 针对 AirPods 场景：如果发现目标设备，立即停止扫描并触发连接
            val targetAddress = targetDeviceAddress
            if (targetAddress != null && device.address == targetAddress) {
                AppLogger.i(TAG, "🎯 发现目标设备: ${name ?: "Unknown"} (${device.address}) rssi=$rssi，立即停止扫描")
                stopScan()
                onTargetDeviceFound?.invoke(device)
                onTargetDeviceFound = null
                targetDeviceAddress = null
                return
            }

            val currentDevices = _scannedDevices.value
            if (currentDevices.none { it.address == device.address }) {
                AppLogger.d(TAG, "扫描到新设备: ${name ?: "Unknown"} (${device.address}) rssi=$rssi")
                _scannedDevices.update { it + device }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            AppLogger.e(TAG, "BLE 扫描失败 errorCode=$errorCode")
            _scannedDevices.value = emptyList()
        }
    }

    private val scanTimeoutRunnable = Runnable {
        AppLogger.w(TAG, "BLE 扫描超时，自动停止扫描")
        stopScan()
    }

    /**
     * 普通扫描模式：用于用户在 App 内手动扫描设备
     */
    fun startScan() {
        startScanWithTarget(null)
    }

    /**
     * 针对已知设备的快速扫描：用于后台/前台快速发现并连接目标设备
     * 这是 AirPods 体验的核心：App 在后台时持续低功耗扫描，发现目标立即连接
     */
    fun startTargetedScan(
        targetAddress: String,
        onFound: (BluetoothDevice) -> Unit
    ) {
        startScanWithTarget(targetAddress, onFound)
    }

    private fun startScanWithTarget(
        targetAddress: String? = null,
        onFound: ((BluetoothDevice) -> Unit)? = null
    ) {
        if (!hasBluetoothScanPermission()) {
            AppLogger.w(TAG, "缺少 BLUETOOTH_SCAN 权限，无法开始扫描")
            return
        }

        // 如果已有扫描在运行，先停止
        stopScan()

        targetDeviceAddress = targetAddress
        onTargetDeviceFound = onFound

        _scannedDevices.value = emptyList()
        val logMsg = if (targetAddress != null) "针对目标设备 $targetAddress 快速扫描" else "开始 BLE 扫描"
        AppLogger.i(TAG, logMsg)

        pollingHandler.removeCallbacks(scanTimeoutRunnable)

        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner != null) {
            val filter = ScanFilter.Builder()
                .setServiceUuid(android.os.ParcelUuid(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")))
                .build()

            // AirPods 体验关键：有目标时用 LOW_LATENCY，无目标（手动扫描）时用 BALANCED
            // 在 Android 16 / SDK 36 下，LOW_POWER 可能导致前台扫描响应过慢
            val isTargeted = targetAddress != null
            val scanMode = if (isTargeted) {
                ScanSettings.SCAN_MODE_LOW_LATENCY
            } else {
                ScanSettings.SCAN_MODE_BALANCED
            }

            val settings = ScanSettings.Builder()
                .setScanMode(scanMode)
                .build()

            scanner.startScan(listOf(filter), settings, scanCallback)
        } else {
            bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
        }

        val timeout = if (targetAddress != null) TARGET_SCAN_TIMEOUT_MS else SCAN_TIMEOUT_MS
        pollingHandler.postDelayed(scanTimeoutRunnable, timeout)
    }

    fun stopScan() {
        pollingHandler.removeCallbacks(scanTimeoutRunnable)
        AppLogger.i(TAG, "停止 BLE 扫描")
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    fun connect(device: BluetoothDevice) {
        connectToDevice(device.address, safeDeviceName(device), pendingProtocolIdHint)
    }

    /**
     * Connects to a device by address. Returns true if connection initiated successfully.
     */
    fun connect(
        address: String,
        nameHint: String? = null,
        protocolIdHint: String? = null
    ): Boolean {
        return connectToDevice(address, nameHint, protocolIdHint)
    }

    private fun connectToDevice(
        address: String,
        nameHint: String?,
        protocolIdHint: String?
    ): Boolean {
        val adapter = bluetoothAdapter ?: run {
            AppLogger.w(TAG, "蓝牙适配器不可用，无法连接 $address")
            return false
        }
        if (!hasBluetoothConnectPermission()) {
            AppLogger.w(TAG, "缺少 BLUETOOTH_CONNECT 权限，无法连接 $address")
            return false
        }
        val device = try {
            adapter.getRemoteDevice(address)
        } catch (error: IllegalArgumentException) {
            AppLogger.e(TAG, "设备地址无效: $address", error)
            return false
        } catch (error: SecurityException) {
            AppLogger.e(TAG, "缺少蓝牙权限: $address", error)
            return false
        }
        pendingDeviceNameHint = nameHint?.takeIf { it.isNotBlank() }
        pendingProtocolIdHint = protocolIdHint?.takeIf { it.isNotBlank() }
        connectDeviceInternal(device)
        return true
    }

    private fun connectDeviceInternal(device: BluetoothDevice) {
        stopPolling()
        stopScan()
        ProtocolParser.reset()

        val existingGatt = bluetoothGatt
        if (existingGatt != null) {
            AppLogger.d(TAG, "断开现有连接后再重新连接")
            existingGatt.disconnect()
        }

        val resolvedName = safeDeviceName(device) ?: pendingDeviceNameHint
        if (pendingDeviceNameHint == null) {
            pendingDeviceNameHint = resolvedName
        }

        _connectionState.value = ConnectionState.Connecting
        AppLogger.i(TAG, "连接设备: ${resolvedName ?: "Unknown"} (${device.address})")
        try {
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        } catch (error: SecurityException) {
            AppLogger.e(TAG, "连接失败：缺少蓝牙权限 ${device.address}", error)
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    fun disconnect() {
        AppLogger.i(TAG, "主动断开 BLE 连接")
        stopPolling()
        
        // Disable notifications before disconnecting
        bluetoothGatt?.let { gatt ->
            writeCharacteristic?.let { char ->
                disableNotification(gatt, char)
            }
            zhikeMainWriteCharacteristic?.let { char ->
                disableNotification(gatt, char)
            }
        }
        
        bluetoothGatt?.disconnect()
        // Don't call close() here - let the callback handle it when STATE_DISCONNECTED fires
    }

    private fun disableNotification(gatt: BluetoothGatt, char: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(char, false)
        val descriptor = char.getDescriptor(UUID.fromString(CCC_DESCRIPTOR))
        if (descriptor != null) {
            writeDescriptorCompat(
                gatt = gatt,
                descriptor = descriptor,
                value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            )
        }
    }

    private fun writeDescriptorCompat(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        value: ByteArray
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, value)
        } else {
            @Suppress("DEPRECATION")
            run {
                descriptor.value = value
                gatt.writeDescriptor(descriptor)
            }
        }
    }

    private fun startPolling() {
        pollingHandler.removeCallbacks(pollingRunnable)
        AppLogger.d(TAG, "启动轮询定时器，2s 后进入实时请求")
        pollingHandler.postDelayed(pollingRunnable, 2000) // Start after initial discovery
    }

    private fun stopPolling() {
        pollingHandler.removeCallbacks(pollingRunnable)
        AppLogger.d(TAG, "停止轮询定时器")
    }

    fun sendCommand(hex: String) {
        val bytes = hexToBytes(hex)
        writeBytes(bytes, WriteRoute.DEFAULT)
    }

    fun sendZhikeMainCommand(hex: String) {
        writeBytes(hexToBytes(hex), WriteRoute.ZHIKE_MAIN)
    }

    fun sendZhikeAuxCommand(hex: String) {
        writeBytes(hexToBytes(hex), WriteRoute.ZHIKE_AUX)
    }

    private fun writeBytes(bytes: ByteArray, route: WriteRoute): WriteResult {
        val char = when (route) {
            WriteRoute.ZHIKE_MAIN -> zhikeMainWriteCharacteristic ?: writeCharacteristic
            WriteRoute.ZHIKE_AUX -> zhikeAuxWriteCharacteristic ?: writeCharacteristic
            WriteRoute.DEFAULT -> writeCharacteristic
        }

        if (char == null) {
            AppLogger.w(TAG, "写入失败：特征值为 null，route=$route，数据已丢弃")
            val result = WriteResult.Dropped("Characteristic is null for route $route")
            _writeResults.tryEmit(result)
            return result
        }

        val writeType = when {
            (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0 ->
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        }

        AppLogger.d(
            TAG,
            "发送 ${route.name} -> ${char.uuidString()} type=$writeType bytes=${bytes.size} hex=${bytes.toHexPreview()}"
        )

        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                bluetoothGatt?.writeCharacteristic(char, bytes, writeType)
            } else {
                @Suppress("DEPRECATION")
                char.writeType = writeType
                @Suppress("DEPRECATION")
                char.value = bytes
                @Suppress("DEPRECATION")
                bluetoothGatt?.writeCharacteristic(char)
            }
            // Note: Actual result will be reported via onCharacteristicWrite callback
            WriteResult.Success
        } catch (e: Exception) {
            AppLogger.e(TAG, "写入异常 route=$route", e)
            val result = WriteResult.Failed(-1)
            _writeResults.tryEmit(result)
            result
        }
    }

    /**
     * Sends data in 20-byte packets with a delay between each, as required by the ZhiKe protocol.
     */
    fun writeInPackets(data: ByteArray, delayMs: Long = 100) {
        val mtu = 20 // Standard BLE MTU for many controllers
        val packets = mutableListOf<ByteArray>()
        
        var start = 0
        while (start < data.size) {
            val end = (start + mtu).coerceAtMost(data.size)
            packets.add(data.sliceArray(start until end))
            start += mtu
        }

        sendNextPacket(packets, 0, delayMs)
    }

    private fun sendNextPacket(packets: List<ByteArray>, index: Int, delayMs: Long) {
        if (index >= packets.size) {
            AppLogger.d(TAG, "分包发送完成，共 ${packets.size} 包")
            return
        }

        writeBytes(
            packets[index],
            if (activeProtocolId == "zhike") WriteRoute.ZHIKE_MAIN else WriteRoute.DEFAULT
        )
        
        pollingHandler.postDelayed({
            sendNextPacket(packets, index + 1, delayMs)
        }, delayMs)
    }


    /**
     * Requests ZhiKe controller to enter write mode (AA210001).
     * This must be done before sending parameter write packets (AA12...).
     */
    fun requestZhikeWriteMode() {
        sendZhikeAuxCommand("AA210001")
    }

    private fun hexToBytes(hex: String): ByteArray {
        val s = hex.replace(" ", "")
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    private fun readCharacteristicCompat(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        gatt.readCharacteristic(characteristic)
    }

    private fun BluetoothGattCharacteristic.uuidString(): String = uuid.toString().lowercase()

    private fun ByteArray.toHexPreview(maxBytes: Int = 20): String {
        return joinToString(separator = " ", limit = maxBytes, truncated = " ...") {
            "%02X".format(it)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            AppLogger.i(TAG, "连接状态变化 status=$status newState=$newState device=${gatt.device.address}")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionState.value = ConnectionState.Connected(gatt.device)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stopPolling()
                ProtocolParser.reset()
                pendingDeviceNameHint = null
                pendingProtocolIdHint = null
                _connectionState.value = ConnectionState.Disconnected
                if (bluetoothGatt === gatt) {
                    bluetoothGatt = null
                }
                runCatching { gatt.close() }
                writeCharacteristic = null
                zhikeMainWriteCharacteristic = null
                zhikeAuxWriteCharacteristic = null
                zhikeHandshakeCharacteristic = null
                activeProtocolId = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val identifiedName = safeDeviceName(gatt.device) ?: pendingDeviceNameHint ?: "Unknown"
                val serviceIds = mutableListOf<String>()
                val charIds = mutableListOf<String>()
                val writableCharacteristics = mutableListOf<BluetoothGattCharacteristic>()
                val notifyCharacteristics = mutableListOf<BluetoothGattCharacteristic>()

                gatt.services.forEach { service ->
                    serviceIds.add(service.uuid.toString())
                    service.characteristics.forEach { char ->
                        charIds.add(char.uuid.toString())
                        if ((char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                            (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                            writableCharacteristics.add(char)
                        }
                        if ((char.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 ||
                            (char.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                            notifyCharacteristics.add(char)
                        }
                    }
                }

                AppLogger.i(
                    TAG,
                    "发现服务成功 services=${serviceIds.size} chars=${charIds.size} device=$identifiedName"
                )

                // Request MTU exchange for better throughput
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AppLogger.d(TAG, "请求 MTU $GATT_MTU")
                    gatt.requestMtu(GATT_MTU)
                }

                // Trigger Protocol Identification
                ProtocolParser.selectProtocol(
                    deviceName = identifiedName,
                    serviceIds = serviceIds,
                    charIds = charIds,
                    preferredProtocolId = pendingProtocolIdHint
                )

                activeProtocolId = ProtocolParser.getActiveProtocolId()
                AppLogger.i(TAG, "识别协议: ${activeProtocolId ?: "unknown"}")

                writeCharacteristic = selectWriteCharacteristic(
                    protocolId = activeProtocolId,
                    candidates = writableCharacteristics
                )
                zhikeMainWriteCharacteristic = writableCharacteristics.firstOrNull { it.uuidString().contains("0000ffe1") || it.uuidString().endsWith("ffe1-0000-1000-8000-00805f9b34fb") || it.uuidString().contains("ffe1") }
                zhikeAuxWriteCharacteristic = writableCharacteristics.firstOrNull { it.uuidString().contains("0000ffe2") || it.uuidString().endsWith("ffe2-0000-1000-8000-00805f9b34fb") || it.uuidString().contains("ffe2") }
                zhikeHandshakeCharacteristic = gatt.services
                    .flatMap { it.characteristics }
                    .firstOrNull {
                        val id = it.uuidString()
                        id.contains("0000fff1") || id.contains("0000fff2") || id.contains("fff1") || id.contains("fff2")
                    }

                AppLogger.d(
                    TAG,
                    "写特征 default=${writeCharacteristic?.uuidString()} zhikeMain=${zhikeMainWriteCharacteristic?.uuidString()} zhikeAux=${zhikeAuxWriteCharacteristic?.uuidString()} handshake=${zhikeHandshakeCharacteristic?.uuidString()}"
                )

                selectNotifyCharacteristics(
                    protocolId = activeProtocolId,
                    candidates = notifyCharacteristics
                ).forEach { char ->
                    enableNotifications(gatt, char)
                }

                // Start polling only after setup is complete
                startPolling()

                if (activeProtocolId == "zhike") {
                    // The original miniapp wakes the controller through FFE2 once before
                    // starting the regular FFE1 real-time polling loop.
                    pollingHandler.postDelayed({
                        sendZhikeAuxCommand("0102")
                    }, 400)
                    pollingHandler.postDelayed({
                        sendZhikeMainCommand("AA110001")
                    }, 1200)
                }
            } else {
                // Handle service discovery failure
                AppLogger.e(TAG, "服务发现失败 status=$status device=${gatt.device.address}")
                _connectionState.value = ConnectionState.Error("服务发现失败: status=$status")
                // Attempt to disconnect and allow retry
                gatt.disconnect()
            }
        }

        private fun selectWriteCharacteristic(
            protocolId: String?,
            candidates: List<BluetoothGattCharacteristic>
        ): BluetoothGattCharacteristic? {
            if (candidates.isEmpty()) return null

            val prioritized = when (protocolId) {
                "zhike" -> listOf("0000ffe1", "0000ffe2", "ffe1", "ffe2")
                "yuanqu", "apt-ausi" -> listOf("0000ffe1", "ffe1")
                else -> emptyList()
            }

            prioritized.forEach { token ->
                candidates.firstOrNull { it.uuidString().contains(token) }?.let { return it }
            }

            return candidates.firstOrNull()
        }

        private fun selectNotifyCharacteristics(
            protocolId: String?,
            candidates: List<BluetoothGattCharacteristic>
        ): List<BluetoothGattCharacteristic> {
            if (candidates.isEmpty()) return emptyList()

            if (protocolId == "zhike") {
                val preferred = candidates.filter {
                    val id = it.uuidString()
                    id.contains("0000ffe1") || id.contains("ffe1")
                }
                if (preferred.isNotEmpty()) return preferred
            }

            return candidates
        }

        private fun enableNotifications(gatt: BluetoothGatt, char: BluetoothGattCharacteristic) {
            gatt.setCharacteristicNotification(char, true)
            val descriptor = char.getDescriptor(UUID.fromString(CCC_DESCRIPTOR))
            if (descriptor != null) {
                val value = if ((char.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                } else {
                    BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                }
                AppLogger.d(TAG, "启用通知 ${char.uuidString()} desc=${descriptor.uuid}")
                writeDescriptorCompat(gatt = gatt, descriptor = descriptor, value = value)
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            AppLogger.d(
                TAG,
                "读取特征 status=$status char=${characteristic.uuidString()} hex=${characteristic.value?.toHexPreview() ?: ""}"
            )
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            AppLogger.d(
                TAG,
                "读取特征 status=$status char=${characteristic.uuidString()} hex=${value.toHexPreview()}"
            )
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                AppLogger.v(TAG, "写入成功 char=${characteristic.uuidString()}")
                _writeResults.tryEmit(WriteResult.Success)
            } else {
                AppLogger.w(TAG, "写入失败 status=$status char=${characteristic.uuidString()}")
                _writeResults.tryEmit(WriteResult.Failed(status))
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            AppLogger.d(TAG, "通知描述符写入 status=$status descriptor=${descriptor.uuid}")
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val value = characteristic.value
            AppLogger.v(TAG, "收到通知 ${characteristic.uuidString()} bytes=${value.size} hex=${value.toHexPreview()}")
            _rawData.tryEmit(value)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            AppLogger.v(TAG, "收到通知 ${characteristic.uuidString()} bytes=${value.size} hex=${value.toHexPreview()}")
            _rawData.tryEmit(value)
        }
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasBluetoothScanPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // For Android 11 and below, we need location permission
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
        // For Android 12+, we need BLUETOOTH_SCAN permission
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun safeDeviceName(device: BluetoothDevice): String? {
        return runCatching { device.name }.getOrNull()
    }
}
