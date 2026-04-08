package com.shawnrain.sdash.ble.bms

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BmsMetrics(
    val totalVoltage: Float = 0f,
    val current: Float = 0f,
    val soc: Float = 0f,
    val temp: Float = 0f,
    val cellVoltages: List<Float> = emptyList(),
    val chargeMosOn: Boolean = false,
    val dischargeMosOn: Boolean = false,
    val balanceOn: Boolean = false,
    val cycleCount: Int = 0,
    val remainingCapacityAh: Float = 0f,
    val fullCapacityAh: Float = 0f,
    val faultMessage: String? = null
)

interface BmsProtocol {
    fun parse(data: ByteArray, emit: (BmsMetrics) -> Unit): Boolean
    fun getHandshakeCommand(): ByteArray? = null
}

object BmsParser {
    private val _metrics = MutableStateFlow(BmsMetrics())
    val metrics: StateFlow<BmsMetrics> = _metrics.asStateFlow()

    private val _activeProtocolLabel = MutableStateFlow("Unconnected")
    val activeProtocolLabel: StateFlow<String> = _activeProtocolLabel.asStateFlow()

    private var activeProtocol: BmsProtocol? = null

    fun selectProtocol(name: String?, serviceUuids: List<String>) {
        val deviceName = name ?: ""
        when {
            deviceName.contains("JK", ignoreCase = true) || deviceName.contains("极空") -> {
                activeProtocol = JkBmsProtocol()
                _activeProtocolLabel.value = "极空 BMS"
            }
            deviceName.contains("ANT", ignoreCase = true) || deviceName.contains("蚂蚁") -> {
                activeProtocol = AntBmsProtocol()
                _activeProtocolLabel.value = "蚂蚁 BMS"
            }
            else -> {
                // Default to JK if unidentified but connected to BMS service?
                // For now, keep as is
            }
        }
    }

    fun parse(data: ByteArray) {
        activeProtocol?.parse(data) {
            _metrics.value = it
        }
    }

    fun reset() {
        activeProtocol = null
        _activeProtocolLabel.value = "Disconnected"
    }
}
