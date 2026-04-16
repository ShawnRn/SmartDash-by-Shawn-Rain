package com.shawnrain.sdash.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.shawnrain.sdash.debug.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class HidRemoteManager(private val context: Context) {
    companion object {
        private const val TAG = "HidRemoteManager"
        
        // Consumer Control Report Descriptor
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

    private var hidDevice: BluetoothHidDevice? = null
    private var connectedDevice: BluetoothDevice? = null
    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _isSupported = MutableStateFlow(true)
    val isSupported = _isSupported.asStateFlow()

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            AppLogger.d(TAG, "onAppStatusChanged: registered=$registered")
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            AppLogger.d(TAG, "onConnectionStateChanged: device=${device.address} state=$state")
            if (state == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = device
                _isConnected.value = true
            } else {
                connectedDevice = null
                _isConnected.value = false
            }
        }
    }

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = proxy as BluetoothHidDevice
                registerApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
            }
        }
    }

    init {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        var supported = false
        if (adapter != null && android.os.Build.VERSION.SDK_INT >= 28) {
            try {
                // Use reflection to check support to avoid compilation issues with some SDK stubs
                val method = adapter.javaClass.getMethod("isBluetoothHidDeviceRoleSupported")
                supported = method.invoke(adapter) as Boolean
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to check HID support via reflection: ${e.message}")
            }
        }
        
        if (adapter == null || !supported) {
            _isSupported.value = false
            AppLogger.e(TAG, "Bluetooth HID Device role not supported or check failed")
        } else {
            adapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
        }
    }

    private fun registerApp() {
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            "SmartDash Remote",
            "SmartDash Remote Control",
            "ShawnRain",
            BluetoothHidDevice.SUBCLASS1_COMBO,
            REPORT_DESCRIPTOR
        )
        hidDevice?.registerApp(sdpSettings, null, null, Executors.newSingleThreadExecutor(), callback)
    }

    fun sendMediaKey(usageCode: Int) {
        val device = connectedDevice
        val hid = hidDevice
        if (device == null || hid == null) {
            AppLogger.w(TAG, "Cannot send media key: not connected")
            return
        }

        // Send press
        val report = byteArrayOf(
            (usageCode and 0xFF).toByte(),
            ((usageCode shr 8) and 0xFF).toByte()
        )
        hid.sendReport(device, 1, report)
        
        // Send release after short delay
        Handler(Looper.getMainLooper()).postDelayed({
            hid.sendReport(device, 1, byteArrayOf(0, 0))
        }, 30)
    }

    fun unregister() {
        hidDevice?.unregisterApp()
        BluetoothAdapter.getDefaultAdapter()?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
    }
}
