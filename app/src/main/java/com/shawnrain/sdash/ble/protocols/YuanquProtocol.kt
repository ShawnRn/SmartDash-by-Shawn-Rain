package com.shawnrain.sdash.ble.protocols

import com.shawnrain.sdash.ble.VehicleMetrics
import java.nio.ByteBuffer
import java.nio.ByteOrder

class YuanquProtocol : ControllerProtocol {
    override val id = "yuanqu"
    override val label = "远驱控制器"

    private var buffer = ByteArray(0)

    override fun resetState() {
        buffer = ByteArray(0)
    }

    override fun score(deviceName: String, serviceIds: List<String>, charIds: List<String>): Float {
        var s = 0f
        val name = deviceName.uppercase()
        if (name.contains("YUANQU") || name.contains("YQ")) s += 0.7f
        // FFE1 alone is too common and causes GEKOO/Zhike devices to be misdetected.
        if (serviceIds.any { it.contains("fff0", ignoreCase = true) || it.contains("ffe5", ignoreCase = true) }) s += 0.2f
        if (charIds.any { it.contains("ffe4", ignoreCase = true) || it.contains("ffe5", ignoreCase = true) }) s += 0.2f
        return s.coerceIn(0f, 1f)
    }

    override fun parse(
        data: ByteArray,
        emit: (VehicleMetrics) -> Unit,
        onConfigChange: (Pair<String, Int>) -> Unit
    ): Boolean {
        buffer += data
        
        var parsed = false
        // Yuanqu typically: 01 03 [Len] [Payload...] [CRC_L] [CRC_H]
        while (buffer.size >= 8) {
            if (buffer[0] == 0x01.toByte() && (buffer[1] == 0x03.toByte() || buffer[1] == 0x04.toByte())) {
                val len = buffer[2].toInt() and 0xFF
                val totalFrameSize = len + 5 // Address(1) + Func(1) + Len(1) + Data(len) + CRC(2)
                
                if (buffer.size >= totalFrameSize) {
                    try {
                        val byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                        // Mock mapping based on typical Far-Drive telemetry
                        // Usually offset 3 is Voltage, offset 5 is Current...
                        if (len >= 10) {
                            val voltRaw = byteBuffer.getShort(3).toUShort().toInt()
                            val curRaw = byteBuffer.getShort(5).toShort().toInt()
                            val rpmRaw = byteBuffer.getShort(7).toShort().toInt()
                            
                            emit(VehicleMetrics(
                                voltage = voltRaw / 10f,
                                busCurrent = curRaw / 10f,
                                rpm = rpmRaw.toFloat().coerceAtLeast(0f),
                                totalPowerW = (voltRaw / 10f) * (curRaw / 10f)
                            ))
                            parsed = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    buffer = buffer.sliceArray(totalFrameSize until buffer.size)
                } else {
                    // Wait for more data
                    break 
                }
            } else {
                buffer = buffer.sliceArray(1 until buffer.size)
            }
        }
        
        if (buffer.size > 512) buffer = ByteArray(0)
        return parsed
    }
}
