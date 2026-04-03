package com.shawnrain.habe.ble.bms

import java.nio.ByteBuffer
import java.nio.ByteOrder

class AntBmsProtocol : BmsProtocol {
    private var buffer = ByteArray(0)

    override fun parse(data: ByteArray, emit: (BmsMetrics) -> Unit): Boolean {
        buffer += data
        
        var parsed = false
        // Ant sync: 0x7E 0xA1 ... 0xAA 0x55
        while (buffer.size >= 140) {
            val h1 = buffer[0].toInt() and 0xFF
            val h2 = buffer[1].toInt() and 0xFF
            if (h1 == 0x7E && h2 == 0xA1) {
                // Potential 140-byte frame
                try {
                    val view = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                    
                    // Specific offsets from reverse engineering
                    // Voltage offset 157-4 = 153? No, wait. 
                    // Let's use common Ant offsets for BLE (140-byte frame)
                    val voltRaw = view.getShort(4).toUShort().toInt()
                    val currRaw = view.getInt(70) 
                    val socRaw = view.get(74).toInt() and 0xFF
                    
                    emit(BmsMetrics(
                        totalVoltage = voltRaw / 10f,
                        current = currRaw / 10f,
                        soc = socRaw.toFloat()
                    ))
                    parsed = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                buffer = buffer.sliceArray(140 until buffer.size)
            } else {
                buffer = buffer.sliceArray(1 until buffer.size)
            }
        }
        
        if (buffer.size > 1024) buffer = ByteArray(0)
        return parsed
    }

    override fun getHandshakeCommand(): ByteArray? {
        // Ant usually requires a 0x7E A1 01 00 00 checksum AA 55 to start
        return byteArrayOf(0x7E.toByte(), 0xA1.toByte(), 0x01, 0x00, 0x00, 0x00, 0x00, 0xAA.toByte(), 0x55.toByte())
    }
}
