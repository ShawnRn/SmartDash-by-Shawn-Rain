package com.shawnrain.habe.ble.protocols

import com.shawnrain.habe.ble.VehicleMetrics
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ZhikeProtocol : ControllerProtocol {
    override val id = "zhike"
    override val label = "智科控制器"

    private var buffer = ByteArray(0)

    override fun resetState() {
        buffer = ByteArray(0)
    }

    override fun score(deviceName: String, serviceIds: List<String>, charIds: List<String>): Float {
        var s = 0f
        val name = deviceName.uppercase()
        if (name.contains("ZHIKE") || name.contains("ZK")) s += 0.8f
        if (charIds.any { it.contains("fff1") || it.contains("fff2") }) s += 0.2f
        return s.coerceIn(0f, 1f)
    }

    override fun parse(
        data: ByteArray,
        emit: (VehicleMetrics) -> Unit,
        onConfigChange: (Pair<String, Int>) -> Unit
    ): Boolean {
        buffer += data
        if (buffer.size < 5) return false

        val hexString = buffer.joinToString("") { "%02X".format(it) }

        // Param Read Response (Pole Pairs)
        if (hexString.startsWith("010304")) {
            if (buffer.size >= 9) {
                val polePairs = ((buffer[3].toInt() and 0xFF) shl 8) or (buffer[4].toInt() and 0xFF)
                if (polePairs in 1..99) {
                    onConfigChange("polePairs" to polePairs)
                }
                buffer = buffer.sliceArray(9 until buffer.size)
                return true
            }
        }

        // Realtime Data (e.g. 02 01 payload...)
        // Standard Zhike frame: 02 01 [VoltHi] [VoltLo] [RpmHi] [RpmLo] [CurrHi] [CurrLo] ...
        if (buffer[0] == 0x02.toByte() && buffer[1] == 0x01.toByte()) {
            if (buffer.size >= 14) {
                val volt = (((buffer[2].toInt() and 0xFF) shl 8) or (buffer[3].toInt() and 0xFF)) / 10f
                val rpm = ((buffer[4].toInt() and 0xFF) shl 8) or (buffer[5].toInt() and 0xFF)
                val curr = (((buffer[6].toInt() and 0xFF) shl 8) or (buffer[7].toInt() and 0xFF)) / 10f
                
                emit(VehicleMetrics(
                    speedKmH = 0f, // Will be calculated by UI or recalibrated
                    voltage = volt,
                    busCurrent = curr,
                    motorTemp = buffer[10].toFloat(),
                    rpm = rpm.toFloat()
                ))
                buffer = buffer.sliceArray(14 until buffer.size)
                return true
            }
        }

        // Cleanup if buffer gets too large without matching
        if (buffer.size > 256) buffer = ByteArray(0)
        
        return false
    }
}
