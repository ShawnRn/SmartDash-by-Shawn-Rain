package com.shawnrain.sdash.ble.protocols

import com.shawnrain.sdash.ble.VehicleMetrics

/**
 * Interface representing a generic controller protocol handler.
 */
interface ControllerProtocol {
    /** Unique ID for the protocol (e.g., "zhike", "apt-ausi"). */
    val id: String
    
    /** Displayable name. */
    val label: String
    
    /** Reset internal state (buffer, etc.). */
    fun resetState()
    
    /** 
     * Score how likely a BLE device is this protocol based on its metadata.
     * @return 0 to 1 score.
     */
    fun score(deviceName: String, serviceIds: List<String>, charIds: List<String>): Float
    
    /** 
     * Main parse function. 
     * @return true if data was consumed, false if incomplete or mismatch.
     */
    fun parse(data: ByteArray, emit: (VehicleMetrics) -> Unit, onConfigChange: (Pair<String, Int>) -> Unit): Boolean
}
