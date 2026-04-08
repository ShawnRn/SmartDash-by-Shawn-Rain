package com.shawnrain.sdash.data.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GpsTracker(context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
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
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                500L,
                0f,
                locationListener
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopTracking() {
        locationManager.removeUpdates(locationListener)
        _gpsSpeed.value = 0f
    }
}
