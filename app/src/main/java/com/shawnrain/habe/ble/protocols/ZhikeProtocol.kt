package com.shawnrain.habe.ble.protocols

import android.os.SystemClock
import com.shawnrain.habe.ble.VehicleMetrics
import com.shawnrain.habe.debug.AppLogger
import java.util.Locale
import kotlin.math.abs

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
    var originalBluetoothPassword: Int = 8888,
    var speedCoefficient: Int = 0,
    var loadedFromController: Boolean = false,
    var rawWords: IntArray = IntArray(64) // Keep raw data for unmapped fields
)

class ZhikeProtocol : ControllerProtocol {
    override val id = "zhike"
    override val label = "智科控制器"

    private var buffer = ByteArray(0)
    private var pendingSettings: ZhikeSettings? = null
    private var lastRealtimeMetrics: VehicleMetrics? = null
    private var lastRealtimeAtMs: Long = 0L
    
    companion object {
        private const val TAG = "ZhikeProtocol"
        private const val FRAME_HEADER = 0xAA.toByte()
        private const val CMD_REALTIME = 0x13.toByte()
        private const val MAX_REASONABLE_CURRENT_A = 5_000f
    }

    override fun resetState() {
        buffer = ByteArray(0)
        pendingSettings = null
        lastRealtimeMetrics = null
        lastRealtimeAtMs = 0L
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
        // FFE0 + FFE1/FFE2 + FFF1/FFF2 is the characteristic Zhike/GEKOO layout.
        if (serviceIds.any { it.contains("ffe0", ignoreCase = true) }) s += 0.2f
        if (charIds.any { it.contains("ffe1", ignoreCase = true) }) s += 0.2f
        if (charIds.any { it.contains("ffe2", ignoreCase = true) }) s += 0.15f
        if (charIds.any { it.contains("fff1", ignoreCase = true) || it.contains("fff2", ignoreCase = true) }) s += 0.15f
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
            
            if (buffer.size < 4) return false // Need at least 4 bytes to determine frame length reliably for some commands
            
            val cmd = buffer[1]
            val expectedLen = getFrameLength(cmd, buffer)
            
            if (expectedLen == null) {
                // Unknown command, skip header
                buffer = buffer.sliceArray(1 until buffer.size)
                continue
            }
            
            if (buffer.size < expectedLen) return false // Wait for more data

            if (cmd == CMD_REALTIME && expectedLen == 67 && !hasValidRealtimeChecksum(buffer, expectedLen)) {
                buffer = buffer.sliceArray(1 until buffer.size)
                continue
            }
            
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
        settings.syncLegacyFieldsFromWords()
        settings.originalBluetoothPassword = settings.bluetoothPassword
        settings.speedCoefficient = extractSpeedCoefficient(frame)
        settings.loadedFromController = true

        return settings
    }

    fun encodeSettings(settings: ZhikeSettings): ByteArray {
        val words = settings.rawWords.copyOf()
        ZhikeParameterCatalog.findDefinition("bus_current")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(settings.copy(rawWords = words), def, settings.busCurrent.toDouble()).rawWords.copyInto(words)
        }
        ZhikeParameterCatalog.findDefinition("phase_current")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(settings.copy(rawWords = words), def, settings.phaseCurrent.toDouble()).rawWords.copyInto(words)
        }
        ZhikeParameterCatalog.findDefinition("motor_direction")?.let { def ->
            ZhikeParameterCatalog.updateBool(settings.copy(rawWords = words), def, settings.motorDirection).rawWords.copyInto(words)
        }
        ZhikeParameterCatalog.findDefinition("sensor_type")?.let { def ->
            ZhikeParameterCatalog.updateList(settings.copy(rawWords = words), def, settings.sensorType).rawWords.copyInto(words)
        }
        ZhikeParameterCatalog.findDefinition("hall_sequence")?.let { def ->
            ZhikeParameterCatalog.updateBool(settings.copy(rawWords = words), def, settings.hallSequence).rawWords.copyInto(words)
        }
        ZhikeParameterCatalog.findDefinition("phase_shift_angle")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(settings.copy(rawWords = words), def, settings.phaseShiftAngle.toDouble()).rawWords.copyInto(words)
        }
        ZhikeParameterCatalog.findDefinition("pole_pairs")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(settings.copy(rawWords = words), def, settings.polePairs.toDouble()).rawWords.copyInto(words)
        }
        ZhikeParameterCatalog.findDefinition("under_voltage")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(settings.copy(rawWords = words), def, settings.underVoltage.toDouble()).rawWords.copyInto(words)
        }
        ZhikeParameterCatalog.findDefinition("over_voltage")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(settings.copy(rawWords = words), def, settings.overVoltage.toDouble()).rawWords.copyInto(words)
        }
        ZhikeParameterCatalog.findDefinition("weak_magnet_current")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(settings.copy(rawWords = words), def, settings.weakMagCurrent.toDouble()).rawWords.copyInto(words)
        }
        ZhikeParameterCatalog.findDefinition("regen_current")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(settings.copy(rawWords = words), def, settings.regenCurrent.toDouble()).rawWords.copyInto(words)
        }
        ZhikeParameterCatalog.findDefinition("bluetooth_password")?.let { def ->
            ZhikeParameterCatalog.updateNumeric(settings.copy(rawWords = words), def, settings.bluetoothPassword.toDouble()).rawWords.copyInto(words)
        }

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

    private fun getFrameLength(cmd: Byte, buffer: ByteArray): Int? {
        return when (cmd) {
            0x13.toByte() -> {
                // Check if this is a 4-byte ACK echo from realtime start/stop command
                if ((buffer[2] == 0x00.toByte() || buffer[2] == 0xFF.toByte()) && buffer[3] == 0x01.toByte()) {
                    4
                } else {
                    67  // Real-time telemetry
                }
            }
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
                if (frame.size == 4) return // Skip 4-byte command ACK

                val words = extractWords(frame)
                val realtimeWords = words.dropLast(1)
                if (realtimeWords.size < 26) return

                val voltage = decodeWord(realtimeWords, 8, scale = 273.0666667f, digits = 1)
                val busCurrent = decodeWord(realtimeWords, 9, signed = true, digits = 1)
                // 智科实时帧的相电流是 0.1A 标度，直接按整数显示会出现 2000A 级别假值。
                val phaseCurrentRaw = decodeWord(realtimeWords, 10, scale = 10f, digits = 1)
                val rpmSigned = decodeWord(realtimeWords, 6, scale = 5.46f, signed = true, digits = 1)
                val speedRaw = decodeWord(realtimeWords, 6, scale = 10f, signed = true, digits = 1)
                val controllerTemp = decodeWord(realtimeWords, 18, scale = 100f, signed = true, digits = 1)
                val ioStatus = realtimeWords.getOrElse(21) { 0 }
                val faultCode = realtimeWords.getOrElse(22) { 0 }

                if (
                    voltage !in 5f..200f ||
                    abs(busCurrent) > MAX_REASONABLE_CURRENT_A ||
                    phaseCurrentRaw !in 0f..MAX_REASONABLE_CURRENT_A ||
                    controllerTemp !in -60f..220f
                ) {
                    AppLogger.w(
                        TAG,
                        "丢弃异常智科数据 voltage=${"%.2f".format(voltage)} busCurrent=${"%.1f".format(busCurrent)} phaseCurrent=${"%.1f".format(phaseCurrentRaw)} rpm=${"%.1f".format(rpmSigned)} speed=${"%.1f".format(speedRaw)}"
                    )
                    return
                }

                val isReverse = (ioStatus and 0x04) != 0
                val sanitizedSpeed = sanitizeSpeedCandidate(
                    candidateSpeedKmh = abs(speedRaw),
                    busCurrent = busCurrent,
                    rpm = abs(rpmSigned)
                )
                val normalizedPhaseCurrent = sanitizePhaseCurrent(
                    candidatePhaseCurrent = phaseCurrentRaw,
                    busCurrent = busCurrent,
                    rpm = abs(rpmSigned)
                )
                val realtimeMetrics = VehicleMetrics(
                    voltage = voltage,
                    busCurrent = busCurrent,
                    phaseCurrent = normalizedPhaseCurrent,
                    speedKmH = sanitizedSpeed,
                    rpm = abs(rpmSigned),
                    mosfetTemp = controllerTemp,
                    controllerTemp = controllerTemp,
                    faultCode = faultCode,
                    isBraking = (ioStatus and 0x08) != 0,
                    isCruise = (ioStatus and 0x40) != 0,
                    isReverse = isReverse
                )
                lastRealtimeMetrics = realtimeMetrics
                lastRealtimeAtMs = SystemClock.elapsedRealtime()
                emit(realtimeMetrics)
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

    private fun signedWord(value: Int): Float {
        return if (value > 0x7FFF) (value - 0x10000).toFloat() else value.toFloat()
    }

    private fun decodeWord(
        words: List<Int>,
        index: Int,
        scale: Float = 1f,
        signed: Boolean = false,
        digits: Int = 2
    ): Float {
        if (index !in words.indices) return 0f
        var value = words[index]
        if (signed && value > 0x7FFF) {
            value -= 0x10000
        }
        val scaled = value / scale
        return String.format(Locale.US, "%.${digits}f", scaled).toFloatOrNull() ?: scaled
    }

    private fun extractWords(frame: ByteArray): List<Int> {
        val words = mutableListOf<Int>()
        var checksum = 0
        var index = 3
        while (index + 1 < frame.size) {
            val word = (frame[index].toInt() and 0xFF) or ((frame[index + 1].toInt() and 0xFF) shl 8)
            words += word
            checksum = (checksum + word) and 0xFFFF
            index += 2
        }
        return if (checksum == 0xFFFF) words else emptyList()
    }

    private fun extractSpeedCoefficient(frame: ByteArray): Int {
        if (frame.size < 24) return 0
        return ((frame[23].toInt() and 0xFF) shl 8) or (frame[22].toInt() and 0xFF)
    }

    private fun sanitizeSpeedCandidate(
        candidateSpeedKmh: Float,
        busCurrent: Float,
        rpm: Float
    ): Float {
        // 不限制极速（适配不同车型），仅通过变化率过滤异常跳变
        val speed = candidateSpeedKmh.coerceAtLeast(0f)
        val previous = lastRealtimeMetrics ?: return speed
        val now = SystemClock.elapsedRealtime()
        val deltaMs = (now - lastRealtimeAtMs).coerceAtLeast(1L)
        val speedDelta = abs(speed - previous.speedKmH)
        val lowLoad = abs(busCurrent) < 3f && abs(previous.busCurrent) < 3f

        if (speed < 0.6f) return 0f
        // 极低负载下速度突增视为异常
        if (lowLoad && previous.speedKmH < 5f && speed > 20f) return 0f
        // 低速且无负载时，异常高值视为传感器噪声
        if (speed > 80f && lowLoad && rpm < 200f) return previous.speedKmH.coerceAtMost(5f)
        // RPM 与速度不匹配：有速度但 RPM 接近 0（控制器下电异常）
        if (speed > 10f && rpm < 20f && lowLoad) {
            return previous.speedKmH
        }
        // 变化率限制：400ms 内速度变化不应超过物理极限（~20km/h/s 加速极限）
        if (deltaMs < 400L) {
            val maxAllowedDelta = if (lowLoad) 8f else 25f
            if (speedDelta > maxAllowedDelta) {
                return previous.speedKmH
            }
        }
        return speed
    }

    private fun sanitizePhaseCurrent(
        candidatePhaseCurrent: Float,
        busCurrent: Float,
        rpm: Float
    ): Float {
        val absBusCurrent = abs(busCurrent)
        if (candidatePhaseCurrent < 0.5f) return 0f
        if (absBusCurrent < 5f && candidatePhaseCurrent < 60f) return 0f
        if (absBusCurrent < 1.5f && rpm < 30f && candidatePhaseCurrent in 0f..20f) return 0f
        return candidatePhaseCurrent
    }

    private fun hasValidRealtimeChecksum(data: ByteArray, expectedLen: Int): Boolean {
        if (data.size < expectedLen || expectedLen < 67) return false

        var sum = 0
        for (i in 0 until 32) {
            val idx = 3 + i * 2
            val word = ((data[idx + 1].toInt() and 0xFF) shl 8) or (data[idx].toInt() and 0xFF)
            sum = (sum + word) and 0xFFFF
        }
        return sum == 0xFFFF
    }

    private fun ByteArray.toHexPreview(maxBytes: Int = 20): String {
        return joinToString(separator = " ", limit = maxBytes, truncated = " ...") {
            "%02X".format(it)
        }
    }
}
