package com.shawnrain.habe.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import com.shawnrain.habe.ble.ProtocolParser
import com.shawnrain.habe.debug.AppLogger
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
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
}

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {
    companion object {
        private const val TAG = "BleManager"
        private const val CCC_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb"
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

    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var zhikeMainWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var zhikeAuxWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var zhikeHandshakeCharacteristic: BluetoothGattCharacteristic? = null
    private var activeProtocolId: String? = null
    
    private val pollingHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val pollingRunnable = object : Runnable {
        override fun run() {
            if (activeProtocolId == "zhike") {
                sendZhikeMainCommand("AA13FF01AA130001")
            }
            pollingHandler.postDelayed(this, 100)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name != null && !_scannedDevices.value.any { it.address == device.address }) {
                AppLogger.d(TAG, "扫描到设备: ${device.name} (${device.address}) rssi=${result.rssi}")
                _scannedDevices.update { it + device }
            }
        }
    }

    fun startScan() {
        _scannedDevices.value = emptyList()
        AppLogger.i(TAG, "开始 BLE 扫描")
        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
    }

    fun stopScan() {
        AppLogger.i(TAG, "停止 BLE 扫描")
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    fun connect(device: BluetoothDevice) {
        _connectionState.value = ConnectionState.Connecting
        AppLogger.i(TAG, "连接设备: ${device.name ?: "Unknown"} (${device.address})")
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        startPolling()
    }

    fun disconnect() {
        AppLogger.i(TAG, "主动断开 BLE 连接")
        stopPolling()
        bluetoothGatt?.disconnect()
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

    private fun writeBytes(bytes: ByteArray, route: WriteRoute) {
        val char = when (route) {
            WriteRoute.ZHIKE_MAIN -> zhikeMainWriteCharacteristic ?: writeCharacteristic
            WriteRoute.ZHIKE_AUX -> zhikeAuxWriteCharacteristic ?: writeCharacteristic
            WriteRoute.DEFAULT -> writeCharacteristic
        } ?: return

        val writeType = when {
            (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0 ->
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        }

        AppLogger.d(
            TAG,
            "发送 ${route.name} -> ${char.uuidString()} type=$writeType bytes=${bytes.size} hex=${bytes.toHexPreview()}"
        )

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
     * Triggers the ZhiKe parameter unlock handshake.
     */
    fun handshakeZhike() {
        sendZhikeMainCommand("AA16004C58B3A7")
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
                _connectionState.value = ConnectionState.Disconnected
                writeCharacteristic = null
                zhikeMainWriteCharacteristic = null
                zhikeAuxWriteCharacteristic = null
                zhikeHandshakeCharacteristic = null
                activeProtocolId = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
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
                    "发现服务成功 services=${serviceIds.size} chars=${charIds.size} device=${gatt.device.name ?: "Unknown"}"
                )

                // Trigger Protocol Identification
                ProtocolParser.selectProtocol(
                    deviceName = gatt.device.name ?: "Unknown",
                    serviceIds = serviceIds,
                    charIds = charIds
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
                
                if (activeProtocolId == "zhike") {
                    zhikeHandshakeCharacteristic?.let {
                        AppLogger.d(TAG, "尝试读取智科握手特征 ${it.uuidString()}")
                        readCharacteristicCompat(gatt, it)
                    }
                    // The original miniapp wakes the controller through FFE2 once before
                    // starting the regular FFE1 real-time polling loop.
                    pollingHandler.postDelayed({
                        sendZhikeAuxCommand("0102")
                    }, 250)
                    pollingHandler.postDelayed({
                        sendZhikeMainCommand("AA110001")
                    }, 900)
                }
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
                descriptor.value = value
                AppLogger.d(TAG, "启用通知 ${char.uuidString()} desc=${descriptor.uuid}")
                gatt.writeDescriptor(descriptor)
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
}
