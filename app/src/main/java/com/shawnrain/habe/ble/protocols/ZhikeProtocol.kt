package com.shawnrain.habe.ble.protocols

import com.shawnrain.habe.ble.VehicleMetrics
import android.util.Log

// Settings Data Model based on GEKOO Protocol
data class ZhikeSettings(
    var busCurrent: Int = 10,       // Word 0
    var phaseCurrent: Int = 10,     // Word 1
    var motorDirection: Boolean = false, // Word 2
    var sensorType: Int = 0,        // Word 30, bits 0-1
    var hallSequence: Boolean = false, // Word 31, bit 0
    var phaseShiftAngle: Int = 0,   // Word 32, scale 182.044
    var polePairs: Int = 10,        // Word 34
    var underVoltage: Int = 60,     // Word 9
    var overVoltage: Int = 84,      // Word 10
    var weakMagCurrent: Int = 0,    // Word 21
    var regenCurrent: Int = 10,     // Word 23
    var bluetoothPassword: Int = 8888, // Word 60
    var loadedFromController: Boolean = false,
    var rawWords: IntArray = IntArray(64) // Keep raw data for unmapped fields
)

class ZhikeProtocol : ControllerProtocol {
    override val id = "zhike"
    override val label = "智科控制器"

    private var buffer = ByteArray(0)
    private var pendingSettings: ZhikeSettings? = null
    
    companion object {
        private const val TAG = "ZhikeProtocol"
        private const val FRAME_HEADER = 0xAA.toByte()
        private const val CMD_REALTIME = 0x13.toByte()
        private const val FRAME_LENGTH = 67
    }

    override fun resetState() {
        buffer = ByteArray(0)
        pendingSettings = null
    }

    fun consumePendingSettings(): ZhikeSettings? {
        val settings = pendingSettings
        pendingSettings = null
        return settings
    }

    override fun score(deviceName: String, serviceIds: List<String>, charIds: List<String>): Float {
        var s = 0f
        val name = deviceName.uppercase()
        // Gekoo devices often start with "GEKOO" or "ZX"
        if (name.contains("GEKOO") || name.contains("ZX") || name.contains("ZK")) s += 0.8f
        // Service FFE0 is a strong indicator
        if (serviceIds.any { it.contains("ffe0", ignoreCase = true) }) s += 0.2f
        return s.coerceIn(0f, 1f)
    }

    override fun parse(
        data: ByteArray,
        emit: (VehicleMetrics) -> Unit,
        onConfigChange: (Pair<String, Int>) -> Unit
    ): Boolean {
        buffer += data
        
        while (buffer.size >= 2) {
            // Find header 0xAA
            val headerIndex = buffer.indexOf(FRAME_HEADER)
            if (headerIndex == -1) {
                buffer = ByteArray(0)
                return false
            }
            
            if (headerIndex > 0) {
                buffer = buffer.sliceArray(headerIndex until buffer.size)
            }
            
            if (buffer.size < 2) return false
            
            val cmd = buffer[1]
            val expectedLen = getFrameLength(cmd)
            
            if (expectedLen == null) {
                // Unknown command, skip header
                buffer = buffer.sliceArray(1 until buffer.size)
                continue
            }
            
            if (buffer.size < expectedLen) return false // Wait for more data
            
            val frame = buffer.sliceArray(0 until expectedLen)
            processFrame(cmd, frame, emit, onConfigChange)
            
            buffer = buffer.sliceArray(expectedLen until buffer.size)
        }
        
        return true
    }

    fun decodeSettings(frame: ByteArray): ZhikeSettings {
        val settings = ZhikeSettings()
        if (frame.size < 131) return settings
        
        val words = IntArray(64)
        for (i in 0 until 64) {
            val idx = 3 + i * 2
            if (idx + 1 < frame.size) {
                words[i] = ((frame[idx + 1].toInt() and 0xFF) shl 8) or (frame[idx].toInt() and 0xFF)
            }
        }
        settings.rawWords = words
        settings.busCurrent = words[0]
        settings.phaseCurrent = words[1]
        settings.motorDirection = (words[2] and 0x01) != 0
        settings.sensorType = words[30] and 0x03
        settings.hallSequence = (words[31] and 0x01) != 0
        settings.phaseShiftAngle = (words[32].toShort() / 182.0444).toInt()
        settings.polePairs = words[34]
        settings.underVoltage = words[9]
        settings.overVoltage = words[10]
        settings.weakMagCurrent = words[21]
        settings.regenCurrent = words[23]
        settings.bluetoothPassword = words[60]
        settings.loadedFromController = true
        
        return settings
    }

    fun encodeSettings(settings: ZhikeSettings): ByteArray {
        val words = settings.rawWords.copyOf()
        // Update mapped fields back into words
        words[0] = settings.busCurrent
        words[1] = settings.phaseCurrent
        words[2] = if (settings.motorDirection) 1 else 0
        words[30] = (words[30] and 0xFFFC.inv()) or (settings.sensorType and 0x03)
        words[31] = (words[31] and 0x01.inv()) or (if (settings.hallSequence) 1 else 0)
        words[32] = (settings.phaseShiftAngle * 182.0444).toInt() and 0xFFFF
        words[34] = settings.polePairs
        words[9] = settings.underVoltage
        words[10] = settings.overVoltage
        words[21] = settings.weakMagCurrent
        words[23] = settings.regenCurrent
        words[60] = settings.bluetoothPassword
        
        // Calculate checksum
        var sum = 0
        for (i in 0 until 63) {
            sum = (sum + words[i]) and 0xFFFF
        }
        words[63] = (0xFFFF - sum) and 0xFFFF
        
        // Build frame
        val result = ByteArray(131)
        result[0] = 0xAA.toByte()
        result[1] = 0x12.toByte() // Write command
        result[2] = 0x00.toByte()
        
        for (i in 0 until 64) {
            result[3 + i * 2] = (words[i] and 0xFF).toByte()
            result[3 + i * 2 + 1] = ((words[i] shr 8) and 0xFF).toByte()
        }
        return result
    }

    private fun getFrameLength(cmd: Byte): Int? {
        return when (cmd) {
            0x13.toByte() -> 67  // Real-time telemetry
            0x11.toByte() -> 131 // Parameter read response
            0x17.toByte() -> 5   // Key check response?
            0x12.toByte() -> 4   // Parameter write/reset ack
            0x21.toByte() -> 4   // Another ack
            else -> null
        }
    }

    private fun processFrame(
        cmd: Byte,
        frame: ByteArray,
        emit: (VehicleMetrics) -> Unit,
        onConfigChange: (Pair<String, Int>) -> Unit
    ) {
        when (cmd) {
            CMD_REALTIME -> {
                // Packet length 67
                // byte[0]=AA, byte[1]=13, byte[2]=??
                // byte[3..66] are 32 words (Little Endian)
                val words = IntArray(32)
                var sum = 0
                for (i in 0 until 32) {
                    val idx = 3 + i * 2
                    if (idx + 1 < frame.size) {
                        val word = ((frame[idx + 1].toInt() and 0xFF) shl 8) or (frame[idx].toInt() and 0xFF)
                        words[i] = word
                        sum = (sum + word) and 0xFFFF
                    }
                }
                
                // Official Checksum: Sum of all 16-bit words (from index 3) must be 0xFFFF
                if (sum != 0xFFFF) {
                    Log.w(TAG, "Zhike Checksum Failed: expected 0xFFFF, got 0x${"%04X".format(sum)}")
                    // return // Optional: Some users prefer seeing "noisy" data than nothing
                }
                
                // Using the word offsets from realtimedata.js
                // Note: JS uses o[X], where o is the words array
                val rpmRaw = words[6]
                val voltRaw = words[8]
                val currRaw = words[9]
                val phaseCurrRaw = words[10]
                val motorTempRaw = words[12]
                val controllerTempRaw = words[18]
                val ioStatus = words[23]
                val faultCode = words[22]

                emit(VehicleMetrics(
                    voltage = voltRaw / 273.0666667f,
                    busCurrent = currRaw.toFloat(), // JS: parseFloat(o[9])
                    phaseCurrent = phaseCurrRaw.toFloat(),
                    rpm = rpmRaw / 5.46f,
                    motorTemp = motorTempRaw / 100f,
                    controllerTemp = controllerTempRaw / 100f,
                    faultCode = faultCode,
                    isBraking = (ioStatus and 0x0C) != 0, 
                    isCruise = (ioStatus and 0x40) != 0,
                    isReverse = (ioStatus and 0x02) != 0
                ))
            }
            0x11.toByte() -> {
                val settings = decodeSettings(frame)
                pendingSettings = settings
                val polePairs = settings.polePairs
                if (polePairs in 1..99) {
                    onConfigChange("polePairs" to polePairs)
                }
            }
        }
    }
}
