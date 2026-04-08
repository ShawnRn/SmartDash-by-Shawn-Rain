package com.shawnrain.sdash.data.gps

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.location.Location
import android.view.Display
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HeadingTracker(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // --- Raw source flows (for DirectionStabilizer / external fusion) ---
    private val _sensorHeadingDegrees = MutableStateFlow<Float?>(null)
    val sensorHeadingDegrees: StateFlow<Float?> = _sensorHeadingDegrees.asStateFlow()

    private val _locationCourseDegrees = MutableStateFlow<Float?>(null)
    val locationCourseDegrees: StateFlow<Float?> = _locationCourseDegrees.asStateFlow()

    private val _locationCourseAgeMs = MutableStateFlow<Long?>(null)
    val locationCourseAgeMs: StateFlow<Long?> = _locationCourseAgeMs.asStateFlow()

    private val _sensorHeadingAgeMs = MutableStateFlow<Long?>(null)
    val sensorHeadingAgeMs: StateFlow<Long?> = _sensorHeadingAgeMs.asStateFlow()

    private val _locationAccuracyMeters = MutableStateFlow<Float?>(null)
    val locationAccuracyMeters: StateFlow<Float?> = _locationAccuracyMeters.asStateFlow()

    private val _locationStepDistanceMeters = MutableStateFlow<Float?>(null)
    val locationStepDistanceMeters: StateFlow<Float?> = _locationStepDistanceMeters.asStateFlow()

    // --- Legacy: retained for backward compatibility ---
    private val _headingDegrees = MutableStateFlow(0f)
    val headingDegrees: StateFlow<Float> = _headingDegrees.asStateFlow()

    private var isRegistered = false
    private var lastSensorHeading = 0f
    private var lastSensorTimestampMs = 0L
    private var lastLocationHeading: Float? = null
    private var lastLocationTimestampMs = 0L

    private var lastBearingLocation: android.location.Location? = null

    companion object {
        private const val LOCATION_HEADING_MIN_SPEED_MPS = 2.0f
        private const val LOCATION_HEADING_FRESH_MS = 4_000L
        private const val SENSOR_HEADING_FRESH_MS = 2_500L
        private const val SENSOR_SMOOTHING_ALPHA = 0.18f
        private const val LOCATION_SMOOTHING_ALPHA = 0.4f
    }

    fun start() {
        if (isRegistered || rotationSensor == null) return
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        isRegistered = true
    }

    fun stop() {
        if (!isRegistered) return
        sensorManager.unregisterListener(this)
        isRegistered = false
    }

    fun updateFromLocation(location: Location) {
        val previous = lastBearingLocation
        val stepDistance = if (previous != null) previous.distanceTo(location) else null
        lastBearingLocation = location

        _locationAccuracyMeters.value = location.accuracy.takeIf { it.isFinite() }
        _locationStepDistanceMeters.value = stepDistance?.takeIf { it.isFinite() }

        if (location.hasBearing() && location.speed >= LOCATION_HEADING_MIN_SPEED_MPS) {
            lastLocationHeading = normalize(location.bearing)
            lastLocationTimestampMs = location.time.takeIf { it > 0L } ?: System.currentTimeMillis()
            _locationCourseDegrees.value = normalize(location.bearing)
            _locationCourseAgeMs.value = 0L
            publishBestHeading()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
        val rotationMatrix = FloatArray(9)
        val remappedRotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

        val (axisX, axisY) = displayAxes()
        SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, remappedRotationMatrix)
        SensorManager.getOrientation(remappedRotationMatrix, orientationAngles)
        val azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        lastSensorHeading = normalize(azimuthDegrees)
        lastSensorTimestampMs = System.currentTimeMillis()

        // Update raw sensor flow for DirectionStabilizer
        _sensorHeadingDegrees.value = lastSensorHeading
        _sensorHeadingAgeMs.value = 0L

        publishBestHeading()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun publishBestHeading(now: Long = System.currentTimeMillis()) {
        val target = when {
            isLocationHeadingFresh(now) -> lastLocationHeading ?: lastSensorHeading
            isSensorHeadingFresh(now) -> lastSensorHeading
            else -> _headingDegrees.value
        }
        val alpha = if (isLocationHeadingFresh(now)) LOCATION_SMOOTHING_ALPHA else SENSOR_SMOOTHING_ALPHA
        _headingDegrees.value = lerpAngle(_headingDegrees.value, target, alpha)
    }

    private fun isLocationHeadingFresh(now: Long): Boolean {
        val heading = lastLocationHeading ?: return false
        if (heading.isNaN()) return false
        return now - lastLocationTimestampMs in 0..LOCATION_HEADING_FRESH_MS
    }

    private fun isSensorHeadingFresh(now: Long): Boolean {
        return now - lastSensorTimestampMs in 0..SENSOR_HEADING_FRESH_MS
    }

    private fun displayAxes(): Pair<Int, Int> {
        val rotation = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)?.rotation ?: Surface.ROTATION_0
        return when (rotation) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
    }

    private fun lerpAngle(current: Float, target: Float, alpha: Float): Float {
        val delta = shortestAngleDelta(current, target)
        return normalize(current + delta * alpha.coerceIn(0f, 1f))
    }

    private fun shortestAngleDelta(from: Float, to: Float): Float {
        val normalized = ((to - from + 540f) % 360f) - 180f
        return if (normalized == -180f) 180f else normalized
    }

    private fun normalize(value: Float): Float {
        return ((value % 360f) + 360f) % 360f
    }
}
