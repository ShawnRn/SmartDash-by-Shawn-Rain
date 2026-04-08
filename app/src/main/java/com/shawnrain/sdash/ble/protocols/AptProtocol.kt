package com.shawnrain.sdash.ble.protocols

import com.shawnrain.sdash.ble.VehicleMetrics
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AptProtocol : ControllerProtocol {
    override val id = "apt-ausi"
    override val label = "APT 安特控制器"

    private var buffer = ByteArray(0)

    override fun resetState() {
        buffer = ByteArray(0)
    }

    override fun score(deviceName: String, serviceIds: List<String>, charIds: List<String>): Float {
        var s = 0f
        val name = deviceName.uppercase()
        if (name.contains("AUSI") || name.contains("APT")) s += 0.75f
        if (serviceIds.any { it.contains("ffe0") || it.contains("ff00") }) s += 0.25f
        return s.coerceIn(0f, 1f)
    }

    override fun parse(
        data: ByteArray,
        emit: (VehicleMetrics) -> Unit,
        onConfigChange: (Pair<String, Int>) -> Unit
    ): Boolean {
        buffer += data
        
        var parsed = false
        // APT 64-byte frame parsing loop
        while (buffer.size >= 64) {
            val head = buffer[0].toUByte().toInt()
            if (head == 0xAA || head == 0x55) {
                try {
                    val byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                    
                    // Specific offsets from analysis (Generic High-Power Controller map)
                    val rpmRaw = byteBuffer.getShort(2) // offset 2
                    val busCrRaw = byteBuffer.getShort(4) // offset 4
                    val phCrRaw = byteBuffer.getShort(6) // offset 6
                    val voltRaw = byteBuffer.getShort(8) // offset 8
                    
                    // Simple checksum (often last byte is sum or similar)
                    // (Skipping complex CRC for now to avoid dropping frames on mismatch)
                    
                    emit(VehicleMetrics(
                        rpm = rpmRaw.toFloat().coerceAtLeast(0f),
                        busCurrent = (busCrRaw.toUShort().toFloat()) / 10f,
                        phaseCurrent = (phCrRaw.toUShort().toFloat()) / 10f,
                        voltage = (voltRaw.toUShort().toFloat()) / 10f,
                        totalPowerW = (voltRaw.toUShort().toFloat() / 10f) * (busCrRaw.toUShort().toFloat() / 10f)
                    ))
                    parsed = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                // Consume 64 bytes
                buffer = buffer.sliceArray(64 until buffer.size)
            } else {
                // Shift 1 byte if header doesn't match
                buffer = buffer.sliceArray(1 until buffer.size)
            }
        }
        
        if (buffer.size > 2048) buffer = ByteArray(0)
        return parsed
    }
}
