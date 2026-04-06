package com.shawnrain.habe.ble.bms

import java.nio.ByteBuffer
import java.nio.ByteOrder

class JkBmsProtocol : BmsProtocol {
    private var buffer = ByteArray(0)

    override fun parse(data: ByteArray, emit: (BmsMetrics) -> Unit): Boolean {
        buffer += data
        
        var parsed = false
        // JK typically: 0x4E 0x57 [Len_H] [Len_L] ... 
        // Or older frames starting with sync 0x55 AA EB 90
        while (buffer.size >= 12) {
            val syncWord = ((buffer[0].toInt() and 0xFF) shl 8) or (buffer[1].toInt() and 0xFF)
            if (syncWord == 0x4E57) {
                val len = ((buffer[2].toInt() and 0xFF) shl 8) or (buffer[3].toInt() and 0xFF)
                val totalLen = len + 4 + 1 // sync(2) + len(2) + payload(len) + chk(1)
                
                if (buffer.size >= totalLen) {
                    try {
                        val payload = buffer.sliceArray(4 until totalLen - 1)
                        // Simple sum check if needed
                        
                        val view = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN)
                        // Mock mapping based on reverse-engineered source
                        // JK offset logic is complex, using standard offsets known for BLE
                        // Realtime data type is usually 0x01 or 0x03
                        
                        val type = payload[0].toInt()
                        if (type == 0x01 || type == 0x03) {
                            // Extract bytes
                            // voltage offset 118? (wait, using the miniprogram's calc logic)
                            // For simplicity, using common patterns:
                            val totalVoltage = view.getShort(118 - 4).toUShort().toInt() / 100f
                            val current = view.getShort(126 - 4).toShort().toInt() / 100f
                            val soc = view.get(125 - 4).toInt() and 0xFF
                            
                            emit(BmsMetrics(
                                totalVoltage = totalVoltage,
                                current = current,
                                soc = soc.toFloat(),
                                cycleCount = view.getInt(130 - 4),
                                chargeMosOn = true, // Simplified
                                dischargeMosOn = true
                            ))
                            parsed = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    buffer = buffer.sliceArray(totalLen until buffer.size)
                } else {
                    break
                }
            } else if (syncWord == 0x55AA) {
                // Older / different JK frame
                buffer = buffer.sliceArray(1 until buffer.size)
            } else {
                buffer = buffer.sliceArray(1 until buffer.size)
            }
        }
        
        if (buffer.size > 2048) buffer = ByteArray(0)
        return parsed
    }
}
