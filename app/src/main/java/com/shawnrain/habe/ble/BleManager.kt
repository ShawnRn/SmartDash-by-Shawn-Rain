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

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name != null && !_scannedDevices.value.any { it.address == device.address }) {
                _scannedDevices.update { it + device }
            }
        }
    }

    fun startScan() {
        _scannedDevices.value = emptyList()
        bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
    }

    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    fun connect(device: BluetoothDevice) {
        _connectionState.value = ConnectionState.Connecting
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    fun sendCommand(hex: String) {
        val bytes = hexToBytes(hex)
        writeCharacteristic?.let { char ->
            char.value = bytes
            bluetoothGatt?.writeCharacteristic(char)
        }
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

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionState.value = ConnectionState.Connected(gatt.device)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = ConnectionState.Disconnected
                writeCharacteristic = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val serviceIds = mutableListOf<String>()
                val charIds = mutableListOf<String>()

                gatt.services.forEach { service ->
                    serviceIds.add(service.uuid.toString())
                    service.characteristics.forEach { char ->
                        charIds.add(char.uuid.toString())
                        // Identify Write Characteristic (Common for most protocols)
                        if ((char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                            (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                            writeCharacteristic = char
                        }
                    }
                }

                // Trigger Protocol Identification
                ProtocolParser.selectProtocol(
                    deviceName = gatt.device.name ?: "Unknown",
                    serviceIds = serviceIds,
                    charIds = charIds
                )
                
                // Auto request Zhike Params (just in case it's a Zhike controller)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    sendCommand("010303EB00027AB5")
                }, 1000)
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val value = characteristic.value
            _rawData.tryEmit(value)
        }
    }
}
