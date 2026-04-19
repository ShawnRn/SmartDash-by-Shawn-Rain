package com.shawnrain.sdash.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import com.shawnrain.sdash.debug.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import java.util.concurrent.Executors

/**
 * HidRemoteManager - 媒体遥控器管理器
 * 
 * 核心逻辑：
 * 1. 优先尝试系统级 BluetoothHidDevice (Classic Bluetooth)，兼容性好但受硬件厂商限制。
 * 2. 如果不支持 (isHidDeviceRoleSupported == false)，则启动 HOGP (HID Over GATT) 模拟器。
 * 3. HOGP 是基于 BLE 的通用协议，几乎支持所有现代 Android 手机。
 */
@SuppressLint("MissingPermission")
class HidRemoteManager(private val context: Context) {
    companion object {
        private const val TAG = "HidRemoteManager"

        // UUIDs for HOGP (HID Over GATT Profile)
        private val UUID_HID_SERVICE = UUID.fromString("00001812-0000-1000-8000-00805f9b34fb")
        private val UUID_REPORT_MAP = UUID.fromString("00002a4b-0000-1000-8000-00805f9b34fb")
        private val UUID_REPORT = UUID.fromString("00002a4d-0000-1000-8000-00805f9b34fb")
        private val UUID_HID_INFO = UUID.fromString("00002a4a-0000-1000-8000-00805f9b34fb")
        private val UUID_CONTROL_POINT = UUID.fromString("00002a4c-0000-1000-8000-00805f9b34fb")
        private val UUID_PROTOCOL_MODE = UUID.fromString("00002a4e-0000-1000-8000-00805f9b34fb")
        private val UUID_CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        private val UUID_REPORT_REF = UUID.fromString("00002908-0000-1000-8000-00805f9b34fb")
        private val UUID_BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
        private val UUID_BATTERY_LEVEL = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
        private val UUID_APPEARANCE = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb")
        
        // Appearance for Remote Control (0x0180)
        private val APPEARANCE_REMOTE_CONTROL = byteArrayOf(0x80.toByte(), 0x01.toByte())

        // Consumer Control Report Descriptor (16-bit Usage)
        private val REPORT_DESCRIPTOR = byteArrayOf(
            0x05.toByte(), 0x0c.toByte(),                    // USAGE_PAGE (Consumer Devices)
            0x09.toByte(), 0x01.toByte(),                    // USAGE (Consumer Control)
            0xa1.toByte(), 0x01.toByte(),                    // COLLECTION (Application)
            0x85.toByte(), 0x01.toByte(),                    //   REPORT_ID (1)
            0x19.toByte(), 0x00.toByte(),                    //   USAGE_MINIMUM (0)
            0x2a.toByte(), 0x3c.toByte(), 0x02.toByte(),    //   USAGE_MAXIMUM (572)
            0x15.toByte(), 0x00.toByte(),                    //   LOGICAL_MINIMUM (0)
            0x26.toByte(), 0x3c.toByte(), 0x02.toByte(),    //   LOGICAL_MAXIMUM (572)
            0x95.toByte(), 0x01.toByte(),                    //   REPORT_COUNT (1)
            0x75.toByte(), 0x10.toByte(),                    //   REPORT_SIZE (16)
            0x81.toByte(), 0x00.toByte(),                    //   INPUT (Data,Ary,Abs)
            0xc0.toByte()                           // END_COLLECTION
        )

        const val KEY_PLAY_PAUSE = 0x00CD
        const val KEY_NEXT = 0x00B5
        const val KEY_PREV = 0x00B6
        const val KEY_VOLUME_UP = 0x00E9
        const val KEY_VOLUME_DOWN = 0x00EA
    }

    private var adapter: BluetoothAdapter? = context.getSystemService(BluetoothManager::class.java)?.adapter
    
    // --- Classic HID Profile State ---
    private var classicHidDevice: BluetoothHidDevice? = null
    private var classicConnectedDevice: BluetoothDevice? = null

    // --- HOGP (BLE HID) State ---
    private var gattServer: BluetoothGattServer? = null
    private var gattReportChar: BluetoothGattCharacteristic? = null
    private var lastHogpReport = byteArrayOf(0, 0)
    
    // Key: Device, Value: Is HID Subscribed (CCCD written)
    private val gattConnectedHosts = mutableMapOf<BluetoothDevice, Boolean>()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()
    
    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed = _isSubscribed.asStateFlow()

    private val _isSupported = MutableStateFlow(true) 
    val isSupported = _isSupported.asStateFlow()

    private val _isClassicSupported = MutableStateFlow(false)
    val isClassicSupported = _isClassicSupported.asStateFlow()

    private val _isHogpActive = MutableStateFlow(false)
    val isHogpActive = _isHogpActive.asStateFlow()

    private var isStarting = false
    private var lastToggleTime = 0L
    private var shouldBeDiscoverable = false
    private val restartHandler = Handler(Looper.getMainLooper())

    // --- Callbacks ---

    private val classicCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            AppLogger.d(TAG, "Classic Hid: onAppStatusChanged registered=$registered")
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            AppLogger.d(TAG, "Classic Hid: onConnectionStateChanged state=$state")
            if (state == BluetoothProfile.STATE_CONNECTED) {
                classicConnectedDevice = device
                syncConnectionState()
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                classicConnectedDevice = null
                syncConnectionState()
            }
        }
    }

    private val gattCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            AppLogger.d(TAG, "HOGP: onConnectionStateChange device=${device.address} status=$status new=$newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Initial add, but not 'subscribed' yet
                gattConnectedHosts[device] = false
                restartHandler.removeCallbacksAndMessages(null)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gattConnectedHosts.remove(device)
                
                // --- Auto-Reconnect Guardianship ---
                if (shouldBeDiscoverable && gattConnectedHosts.isEmpty()) {
                    AppLogger.i(TAG, "HOGP: 检测到断开且处于活跃模式，将在 1.5s 后自动重启广播...")
                    restartHandler.removeCallbacksAndMessages(null)
                    restartHandler.postDelayed({
                        if (shouldBeDiscoverable && gattConnectedHosts.isEmpty()) {
                            togglePairingMode(true, isInternalRestart = true)
                        }
                    }, 1500)
                }
            }
            syncConnectionState()
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic) {
            AppLogger.d(TAG, "HOGP: onCharacteristicReadRequest ${characteristic.uuid} offset=$offset from ${device.address}")
            // Basic filtering: if a device reads HID info, it's definitely an HID host
            if (characteristic.uuid == UUID_HID_INFO || characteristic.uuid == UUID_REPORT_MAP) {
                if (gattConnectedHosts.containsKey(device) && gattConnectedHosts[device] == false) {
                    AppLogger.d(TAG, "HOGP: Device ${device.address} identified as HID Host")
                }
            }
            
            val fullValue = when (characteristic.uuid) {
                UUID_REPORT_MAP -> REPORT_DESCRIPTOR
                UUID_REPORT -> lastHogpReport
                UUID_HID_INFO -> byteArrayOf(0x11, 0x01, 0x00, 0x02) // v1.1, country 0, flags: remote-wake
                UUID_PROTOCOL_MODE -> byteArrayOf(0x01) // Report Mode
                UUID_BATTERY_LEVEL -> byteArrayOf(100) // 100%
                UUID_APPEARANCE -> APPEARANCE_REMOTE_CONTROL
                else -> byteArrayOf()
            }

            val responseValue = if (offset < fullValue.size) {
                fullValue.copyOfRange(offset, fullValue.size)
            } else {
                byteArrayOf()
            }
            
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, responseValue)
        }

        override fun onDescriptorReadRequest(device: BluetoothDevice, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor) {
            AppLogger.d(TAG, "HOGP: onDescriptorReadRequest ${descriptor.uuid} offset=$offset from ${device.address}")
            
            val fullValue = when (descriptor.uuid) {
                UUID_REPORT_REF -> byteArrayOf(0x01, 0x01) // ID 1, Type Input
                UUID_CCCD -> {
                    val isSubscribed = gattConnectedHosts[device] == true
                    if (isSubscribed) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                }
                else -> byteArrayOf()
            }

            val responseValue = if (offset < fullValue.size) {
                fullValue.copyOfRange(offset, fullValue.size)
            } else {
                byteArrayOf()
            }

            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, responseValue)
        }


        override fun onDescriptorWriteRequest(device: BluetoothDevice, requestId: Int, descriptor: BluetoothGattDescriptor, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray) {
            AppLogger.d(TAG, "HOGP: onDescriptorWriteRequest ${descriptor.uuid} value=${value.toHexString()}")
            if (descriptor.uuid == UUID_CCCD) {
                val subscribed = value.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                gattConnectedHosts[device] = subscribed
                AppLogger.i(TAG, "HOGP: Host ${device.address} HID subscription changed: $subscribed")
                syncConnectionState()
            }
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }
        }

        override fun onNotificationSent(device: BluetoothDevice, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                AppLogger.v(TAG, "HOGP: Notification delivered to ${device.address}")
            } else {
                AppLogger.w(TAG, "HOGP: Notification failed for ${device.address} status=$status")
            }
        }
    }

    private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

    init {
        checkSupport()
        setupHogpServer()
    }

    private fun checkSupport() {
        val adapter = this.adapter ?: return
        var classicSupported = false
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            try {
                val method = adapter.javaClass.getMethod("isBluetoothHidDeviceRoleSupported")
                classicSupported = method.invoke(adapter) as Boolean
            } catch (e: Exception) {
                AppLogger.w(TAG, "Classic HID check failed: ${e.message}")
            }
        }
        _isClassicSupported.value = classicSupported
        
        if (classicSupported) {
            adapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    classicHidDevice = proxy as BluetoothHidDevice
                    registerClassicApp()
                }
                override fun onServiceDisconnected(profile: Int) {
                    classicHidDevice = null
                }
            }, BluetoothProfile.HID_DEVICE)
        }
    }

    private fun registerClassicApp() {
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "SmartDash Remote",
            "SmartDash Remote Control",
            "ShawnRain",
            BluetoothHidDevice.SUBCLASS1_COMBO,
            REPORT_DESCRIPTOR
        )
        classicHidDevice?.registerApp(sdpSettings, null, null, Executors.newSingleThreadExecutor(), classicCallback)
    }

    private fun setupHogpServer() {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = manager.openGattServer(context, gattCallback) ?: return

        val hidService = BluetoothGattService(UUID_HID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        // Report Map Characteristic
        val reportMap = BluetoothGattCharacteristic(UUID_REPORT_MAP, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED)
        hidService.addCharacteristic(reportMap)

        // Report Characteristic (Notify)
        gattReportChar = BluetoothGattCharacteristic(UUID_REPORT, 
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED or BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED)
        
        val cccd = BluetoothGattDescriptor(UUID_CCCD, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)
        gattReportChar?.addDescriptor(cccd)
        
        val reportRef = BluetoothGattDescriptor(UUID_REPORT_REF, BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED)
        gattReportChar?.addDescriptor(reportRef)
        
        hidService.addCharacteristic(gattReportChar)

        // HID Information
        hidService.addCharacteristic(BluetoothGattCharacteristic(UUID_HID_INFO, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED))
        
        // Protocol Mode
        hidService.addCharacteristic(BluetoothGattCharacteristic(UUID_PROTOCOL_MODE, BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, 
            BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED or BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED))
        
        // Control Point
        hidService.addCharacteristic(BluetoothGattCharacteristic(UUID_CONTROL_POINT, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED))
        
        // Appearance (Standard requirement for some HID hosts)
        hidService.addCharacteristic(BluetoothGattCharacteristic(UUID_APPEARANCE, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED))

        val addedHid = gattServer?.addService(hidService) ?: false
        AppLogger.d(TAG, "HOGP: HID Service added: $addedHid")
        
        // Battery Service (Optional but recommended for HOGP)
        val batteryService = BluetoothGattService(UUID_BATTERY_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val batteryLevel = BluetoothGattCharacteristic(UUID_BATTERY_LEVEL, 
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED)
        batteryService.addCharacteristic(batteryLevel)
        val addedBat = gattServer?.addService(batteryService) ?: false
        AppLogger.d(TAG, "HOGP: Battery Service added: $addedBat")
    }

    fun togglePairingMode(active: Boolean, isInternalRestart: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!isInternalRestart && now - lastToggleTime < 1000) {
            return
        }
        lastToggleTime = now
        
        val advertiser = adapter?.bluetoothLeAdvertiser ?: return
        
        if (!isInternalRestart) {
            shouldBeDiscoverable = active
        }

        if (active) {
            if (isStarting) {
                AppLogger.d(TAG, "HOGP: 正在启动广播中，忽略重复请求")
                return
            }
            
            // Already connected? Don't start advertising unless forced
            if (!isInternalRestart && gattConnectedHosts.isNotEmpty()) {
                AppLogger.d(TAG, "HOGP: 已连接，忽略广播启动请求")
                return
            }
            
            isStarting = true

            // Advertise Settings: 
            // - Low Latency mode helps iOS find it faster
            // - Connectable of course
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build()

            // Balanced Fix for Visibility vs. Size:
            // - Primary: Identifiers (UUID + Appearance) -> Fast discovery
            // - Response: Identity (Name) -> Friendly display
            // This stays well below 31 bytes in each packet.
            
            if (adapter?.name != "SD") {
                try { adapter?.name = "SD" } catch (e: Exception) {}
            }

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false) 
                .addServiceUuid(ParcelUuid(UUID_HID_SERVICE))
                .setIncludeTxPowerLevel(false)
                .build()

            val scanResponse = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build()

            advertiser.stopAdvertising(advertiseCallback)
            advertiser.startAdvertising(settings, data, scanResponse, advertiseCallback)
            
            if (!isInternalRestart) {
                AppLogger.i(TAG, "已启动 HOGP 广播 (Appearance: Remote Control)，请在 iPhone 上查找设备名称并配对")
            }
        } else {
            isStarting = false
            advertiser.stopAdvertising(advertiseCallback)
            _isHogpActive.value = false
            restartHandler.removeCallbacksAndMessages(null)
            // Clear connections when explicitly stopped? 
            // Better to keep them if user just toggled something else, but here 'active' means 'should be on'
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            AppLogger.d(TAG, "HOGP Advertising started successfully")
            isStarting = false
            _isHogpActive.value = true
        }
        override fun onStartFailure(errorCode: Int) {
            AppLogger.e(TAG, "HOGP Advertising failed: $errorCode")
            isStarting = false
            _isHogpActive.value = false
            if (errorCode != AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED && shouldBeDiscoverable) {
                restartHandler.postDelayed({
                    if (shouldBeDiscoverable) togglePairingMode(true, isInternalRestart = true)
                }, 5000)
            }
        }
    }

    private fun syncConnectionState() {
        val anyConnected = classicConnectedDevice != null || gattConnectedHosts.isNotEmpty()
        val anySubscribed = gattConnectedHosts.values.any { it }
        
        if (_isConnected.value != anyConnected) {
            _isConnected.value = anyConnected
        }
        if (_isSubscribed.value != anySubscribed) {
            _isSubscribed.value = anySubscribed
            AppLogger.i(TAG, "遥控器订阅状态变更: $anySubscribed")
        }
    }

    fun sendMediaKey(usageCode: Int) {
        // --- Try Classic HID First ---
        classicConnectedDevice?.let { device ->
            classicHidDevice?.let { hid ->
                val report = byteArrayOf(
                    (usageCode and 0xFF).toByte(),
                    ((usageCode shr 8) and 0xFF).toByte()
                )
                hid.sendReport(device, 1, report)
                Handler(Looper.getMainLooper()).postDelayed({
                    hid.sendReport(device, 1, byteArrayOf(0, 0))
                }, 30)
                AppLogger.d(TAG, "Media key sent via Classic HID: 0x${Integer.toHexString(usageCode)}")
                return
            }
        }

        // --- Try HOGP Second ---
        val subscribedHosts = gattConnectedHosts.filterValues { it }.keys
        if (subscribedHosts.isNotEmpty()) {
            val char = gattReportChar ?: return
            val report = byteArrayOf(
                (usageCode and 0xFF).toByte(),
                ((usageCode shr 8) and 0xFF).toByte()
            )
            lastHogpReport = report
            subscribedHosts.forEach { device ->
                gattServer?.notifyCharacteristicChanged(device, char, false, report)
            }
            
            Handler(Looper.getMainLooper()).postDelayed({
                val emptyReport = byteArrayOf(0, 0)
                lastHogpReport = emptyReport
                subscribedHosts.forEach { device ->
                    gattServer?.notifyCharacteristicChanged(device, char, false, emptyReport)
                }
            }, 30)
            AppLogger.d(TAG, "Media key sent via HOGP (Subscribed Hosts: ${subscribedHosts.size}): 0x${Integer.toHexString(usageCode)}")
            return
        }

        if (gattConnectedHosts.isNotEmpty()) {
            AppLogger.w(TAG, "无法发送媒体键: 虽然有连接，但主机未开启 HID 通知 (未订阅)")
        } else {
            AppLogger.w(TAG, "无法发送媒体键: 没有任何 iPhone 连接")
        }
    }

    fun unregister() {
        togglePairingMode(false)
        classicHidDevice?.unregisterApp()
        adapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, classicHidDevice)
        gattServer?.close()
    }
}
