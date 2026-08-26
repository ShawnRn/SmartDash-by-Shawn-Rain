package com.shawnrain.sdash.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.shawnrain.sdash.AppProcessExit
import com.shawnrain.sdash.MainActivity
import com.shawnrain.sdash.data.dashcam.DashcamManager
import com.shawnrain.sdash.data.dashcam.DashcamState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DashcamForegroundService : Service() {
    private val TAG = "DashcamForegroundService"
    private val NOTIFICATION_ID = 20260605
    private val CHANNEL_ID = "dashcam_service_channel"

    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        private const val EXTRA_USE_AUDIO = "extra_use_audio"

        fun startService(context: Context, useAudio: Boolean = false) {
            if (AppProcessExit.isExitScheduled()) {
                Log.w("DashcamForegroundService", "Skip startService: process exit already scheduled")
                return
            }
            val intent = Intent(context, DashcamForegroundService::class.java).apply {
                putExtra(EXTRA_USE_AUDIO, useAudio)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, DashcamForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Foreground Service onCreate")
        createNotificationChannel()
        acquireWakeLock()

        val dashcamManager = DashcamManager.getInstance(this)
        dashcamManager.recordingDurationMs.onEach { durationMs ->
            if (dashcamManager.state.value == DashcamState.RECORDING) {
                updateNotification(durationMs)
            }
        }.launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Foreground Service onStartCommand")

        if (AppProcessExit.isExitScheduled()) {
            Log.w(TAG, "Exit already scheduled, refusing to keep FGS alive")
            dismantleForegroundAndSelf()
            return START_NOT_STICKY
        }

        val useAudio = intent?.getBooleanExtra(EXTRA_USE_AUDIO, false) ?: false
        val notification = buildNotification(0L)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val type = if (useAudio) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                } else {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                }
                startForeground(NOTIFICATION_ID, notification, type)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to start foreground service due to SecurityException: ${e.message}", e)
            dismantleForegroundAndSelf()
            return START_NOT_STICKY
        }

        // Never sticky: 划掉任务后不要被系统自动重启，尤其 vivo 白名单场景
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "Foreground Service onDestroy")
        dismantleForegroundAndSelf(stop = false)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.i(TAG, "onTaskRemoved: user swiped task, tearing down FGS keep-alive anchors")
        runCatching {
            DashcamManager.getInstance(this).stopRecording()
        }
        dismantleForegroundAndSelf()
        // 有 FGS 时划掉卡片往往只到这里；统一走退出协调器，避免 vivo 白名单下进程悬挂
        AppProcessExit.schedule(
            context = this,
            reason = "service_onTaskRemoved",
            allowRecordingGrace = true
        )
    }

    private fun dismantleForegroundAndSelf(stop: Boolean = true) {
        releaseWakeLock()
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        }
        AppProcessExit.clearDashcamNotification(this)
        if (stop) {
            stopSelf()
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmartDash::DashcamWakeLock").apply {
                acquire(10 * 60 * 60 * 1000L)
            }
            Log.i(TAG, "WakeLock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                Log.i(TAG, "WakeLock released")
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "SmartDash 行车记录仪服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun buildNotification(durationMs: Long): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val durationStr = String.format("%02d:%02d", minutes, seconds)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🔴 SmartDash 行车记录仪录制中")
            .setContentText("已录制时间：$durationStr")
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification(durationMs: Long) {
        if (AppProcessExit.isExitScheduled()) return
        val notification = buildNotification(durationMs)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
