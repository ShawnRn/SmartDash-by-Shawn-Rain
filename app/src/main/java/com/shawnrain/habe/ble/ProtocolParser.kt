package com.shawnrain.habe.ble

import com.shawnrain.habe.ble.protocols.AptProtocol
import com.shawnrain.habe.ble.protocols.ControllerProtocol
import com.shawnrain.habe.ble.protocols.YuanquProtocol
import com.shawnrain.habe.ble.protocols.ZhikeProtocol
import com.shawnrain.habe.ble.protocols.ZhikeSettings
import com.shawnrain.habe.debug.AppLogger
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
    val voltageSag: Float = 0f,
    val totalPowerW: Float = 0f,
    val motorTemp: Float = 0f,
    val mosfetTemp: Float = 0f,
    val controllerTemp: Float = 0f,
    val soc: Float = 0f,
    val rpm: Float = 0f,
    val tripDistance: Double = 0.0,
    val efficiencyWhKm: Float = 0f,
    val avgEfficiencyWhKm: Float = 0f,
    val faultCode: Int = 0,
    val isBraking: Boolean = false,
    val isCruise: Boolean = false,
    val isReverse: Boolean = false
)

object ProtocolParser {
    private const val TAG = "ProtocolParser"

    private val protocols = listOf(
        ZhikeProtocol(),
        AptProtocol(),
        YuanquProtocol()
    )

    private val _activeProtocolId = MutableStateFlow<String?>(null)
    val activeProtocolId: StateFlow<String?> = _activeProtocolId.asStateFlow()

    private var activeProtocol: ControllerProtocol? = null
    fun getActiveProtocolId(): String? = activeProtocolId.value

    private val _metrics = MutableStateFlow(VehicleMetrics())
    val metrics: StateFlow<VehicleMetrics> = _metrics.asStateFlow()

    private val _autoConfigUpdates = MutableSharedFlow<Pair<String, Int>>(extraBufferCapacity = 10)
    val autoConfigUpdates: SharedFlow<Pair<String, Int>> = _autoConfigUpdates.asSharedFlow()

    private val _zhikeSettings = MutableSharedFlow<ZhikeSettings>(extraBufferCapacity = 1)
    val zhikeSettings: SharedFlow<ZhikeSettings> = _zhikeSettings.asSharedFlow()

    private val _activeProtocolLabel = MutableStateFlow("未识别")
    val activeProtocolLabel: StateFlow<String> = _activeProtocolLabel.asStateFlow()

    /**
     * Select the best protocol based on device metadata.
     */
    fun selectProtocol(
        deviceName: String,
        serviceIds: List<String>,
        charIds: List<String>,
        preferredProtocolId: String? = null
    ) {
        val scored = protocols.map { it to it.score(deviceName, serviceIds, charIds) }
            .sortedByDescending { it.second }
        AppLogger.i(
            TAG,
            "协议候选 ${scored.joinToString { "${it.first.id}=${"%.2f".format(it.second)}" }} device=$deviceName"
        )

        val preferred = preferredProtocolId?.let { hint ->
            protocols.firstOrNull { it.id == hint }
        }
        if (preferred != null) {
            activeProtocol = preferred
            _activeProtocolId.value = preferred.id
            _activeProtocolLabel.value = preferred.label
            activeProtocol?.resetState()
            AppLogger.i(TAG, "协议命中记忆画像 ${preferred.id} device=$deviceName")
            return
        }

        val best = scored.firstOrNull()
        if (best != null && best.second > 0.1f) {
            activeProtocol = best.first
            _activeProtocolId.value = best.first.id
            _activeProtocolLabel.value = best.first.label
            activeProtocol?.resetState()
            AppLogger.i(TAG, "协议匹配 ${best.first.id} score=${"%.2f".format(best.second)} device=$deviceName")
        } else {
            activeProtocol = null
            _activeProtocolId.value = null
            _activeProtocolLabel.value = "通用协议"
            AppLogger.w(TAG, "未识别具体协议，回退到通用解析 device=$deviceName")
        }
    }

    fun reset() {
        activeProtocol?.resetState()
        activeProtocol = null
        _activeProtocolId.value = null
        _activeProtocolLabel.value = "未识别"
        _metrics.value = VehicleMetrics()
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

            if (protocol is ZhikeProtocol) {
                protocol.consumePendingSettings()?.let { settings ->
                    AppLogger.i(TAG, "收到智科参数快照 polePairs=${settings.polePairs} busCurrent=${settings.busCurrent}")
                    _zhikeSettings.tryEmit(settings)
                }
            }
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
