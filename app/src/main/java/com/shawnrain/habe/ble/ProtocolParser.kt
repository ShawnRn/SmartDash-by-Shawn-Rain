package com.shawnrain.habe.ble

import com.shawnrain.habe.ble.protocols.AptProtocol
import com.shawnrain.habe.ble.protocols.ControllerProtocol
import com.shawnrain.habe.ble.protocols.YuanquProtocol
import com.shawnrain.habe.ble.protocols.ZhikeProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global parsed metrics from BLE.
 */
data class VehicleMetrics(
    val speedKmH: Float = 0f,
    val busCurrent: Float = 0f,
    val phaseCurrent: Float = 0f,
    val voltage: Float = 0f,
    val totalPowerW: Float = 0f,
    val motorTemp: Float = 0f,
    val mosfetTemp: Float = 0f,
    val soc: Float = 0f,
    val rpm: Float = 0f,
    val tripDistance: Double = 0.0,
    val efficiencyWhKm: Float = 0f
)

object ProtocolParser {

    private val protocols = listOf(
        ZhikeProtocol(),
        AptProtocol(),
        YuanquProtocol()
    )

    private var activeProtocol: ControllerProtocol? = null

    private val _metrics = MutableStateFlow(VehicleMetrics())
    val metrics: StateFlow<VehicleMetrics> = _metrics.asStateFlow()

    private val _autoConfigUpdates = MutableSharedFlow<Pair<String, Int>>(extraBufferCapacity = 10)
    val autoConfigUpdates: SharedFlow<Pair<String, Int>> = _autoConfigUpdates.asSharedFlow()

    private val _activeProtocolLabel = MutableStateFlow("未识别")
    val activeProtocolLabel: StateFlow<String> = _activeProtocolLabel.asStateFlow()

    /**
     * Select the best protocol based on device metadata.
     */
    fun selectProtocol(deviceName: String, serviceIds: List<String>, charIds: List<String>) {
        val scored = protocols.map { it to it.score(deviceName, serviceIds, charIds) }
            .sortedByDescending { it.second }
        
        val best = scored.firstOrNull()
        if (best != null && best.second > 0.1f) {
            activeProtocol = best.first
            _activeProtocolLabel.value = best.first.label
            activeProtocol?.resetState()
        } else {
            activeProtocol = null
            _activeProtocolLabel.value = "通用协议"
        }
    }

    fun parse(data: ByteArray) {
        if (data.isEmpty()) return
        
        val protocol = activeProtocol
        if (protocol != null) {
            protocol.parse(
                data = data,
                emit = { _metrics.value = it },
                onConfigChange = { _autoConfigUpdates.tryEmit(it) }
            )
            // Note: We don't return here if we want to allow overlapping parsers, 
            // but usually a controller has only one protocol.
            return 
        }

        // --- Fallback / Legacy Heuristic Parsing ---
        val hexString = data.joinToString("") { "%02X".format(it) }
        try {
            when {
                data.size >= 9 && hexString.startsWith("010304") -> {
                    val polePairs = ((data[3].toInt() and 0xFF) shl 8) or (data[4].toInt() and 0xFF)
                    if (polePairs in 1..99) {
                        _autoConfigUpdates.tryEmit("polePairs" to polePairs)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
