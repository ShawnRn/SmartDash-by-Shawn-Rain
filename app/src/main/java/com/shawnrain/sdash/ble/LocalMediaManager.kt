package com.shawnrain.sdash.ble

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import com.shawnrain.sdash.debug.AppLogger

class LocalMediaManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun sendMediaKey(keyCode: Int) {
        AppLogger.d("LocalMediaManager", "Sending local media key: $keyCode")
        
        // Simulating single key press (Down + Up)
        val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val eventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        
        // dispatchMediaKeyEvent handles the system-wide media key event routing
        audioManager.dispatchMediaKeyEvent(eventDown)
        audioManager.dispatchMediaKeyEvent(eventUp)
    }

    fun playPause() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    fun next() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
    fun previous() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
}
