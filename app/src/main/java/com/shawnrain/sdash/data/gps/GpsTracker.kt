package com.shawnrain.sdash.data.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.shawnrain.sdash.debug.AppLogger

class GpsTracker(context: Context) {
    companion object {
        private const val TAG = "GpsTracker"
    }

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    @Volatile
    private var isTracking = false
    
    private val _gpsSpeed = MutableStateFlow(0f)
    val gpsSpeed: StateFlow<Float> = _gpsSpeed.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _location.value = location
            // convert m/s to km/h
            if (location.hasSpeed()) {
                val speedKmh = location.speed * 3.6f
                _gpsSpeed.value = speedKmh
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (isTracking) return
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                500L,
                0f,
                locationListener
            )
            isTracking = true
            AppLogger.i(TAG, "GPS tracking started (500ms high accuracy)")
        } catch (e: Exception) {
            isTracking = false
            AppLogger.e(TAG, "Failed to start GPS tracking", e)
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        try {
            locationManager.removeUpdates(locationListener)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to stop GPS tracking cleanly", e)
        } finally {
            isTracking = false
            _gpsSpeed.value = 0f
            AppLogger.i(TAG, "GPS tracking stopped")
        }
    }
}
