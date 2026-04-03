package com.shawnrain.habe.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.shawnrain.habe.R
import com.shawnrain.habe.debug.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OverlayHudState(
    val speedKmh: Float = 0f,
    val powerKw: Float = 0f,
    val avgEfficiencyWhKm: Float = 0f
)

object OverlayHudStore {
    private val _state = MutableStateFlow(OverlayHudState())
    val state = _state.asStateFlow()

    fun update(state: OverlayHudState) {
        _state.value = state
    }
}

object OverlayHudController {
    private const val TAG = "OverlayHud"

    fun canDrawOverlays(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }

    fun createPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun start(context: Context) {
        if (!canDrawOverlays(context)) {
            AppLogger.w(TAG, "悬浮窗权限未开启，跳过启动")
            return
        }
        AppLogger.i(TAG, "启动悬浮窗服务")
        val intent = Intent(context, HudOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stop(context: Context) {
        AppLogger.i(TAG, "停止悬浮窗服务")
        context.stopService(Intent(context, HudOverlayService::class.java))
    }
}

class HudOverlayService : Service() {
    companion object {
        private const val TAG = "HudOverlayService"
        private const val CHANNEL_ID = "hud_overlay"
        private const val NOTIFICATION_ID = 2001
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var windowManager: WindowManager
    private var overlayView: LinearLayout? = null
    private var speedText: TextView? = null
    private var powerText: TextView? = null
    private var avgEffText: TextView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        ensureNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        showOverlay()
        serviceScope.launch {
            OverlayHudStore.state.collect { render(it) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!OverlayHudController.canDrawOverlays(this)) {
            AppLogger.w(TAG, "启动时缺少悬浮窗权限，结束服务")
            stopSelf()
            return START_NOT_STICKY
        }
        if (overlayView == null) {
            showOverlay()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        hideOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlay() {
        if (overlayView != null || !OverlayHudController.canDrawOverlays(this)) return

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(16), dp(20), dp(16))
            background = GradientDrawable().apply {
                setColor(0xDD14141BL.toInt())
                cornerRadius = dp(24).toFloat()
                setStroke(dp(1), 0x33FFFFFF)
            }
        }

        speedText = TextView(this).apply {
            textSize = 34f
            setTypeface(Typeface.DEFAULT_BOLD)
            setTextColor(0xFFE0E7FF.toInt())
            gravity = Gravity.CENTER
            text = "0.0"
        }

        val speedUnit = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFF9CA3AF.toInt())
            gravity = Gravity.CENTER
            text = "km/h"
        }

        val statsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        powerText = createStatText("0.00 kW")
        avgEffText = createStatText("0.0 Wh/km")
        statsRow.addView(powerText)
        statsRow.addView(createSpacer())
        statsRow.addView(avgEffText)

        container.addView(speedText)
        container.addView(speedUnit)
        container.addView(statsRow)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(56)
        }

        windowManager.addView(container, params)
        overlayView = container
        AppLogger.i(TAG, "悬浮窗已显示")
    }

    private fun hideOverlay() {
        overlayView?.let { view ->
            runCatching { windowManager.removeView(view) }
            AppLogger.i(TAG, "悬浮窗已移除")
        }
        overlayView = null
    }

    private fun render(state: OverlayHudState) {
        speedText?.text = String.format("%.1f", state.speedKmh)
        powerText?.text = String.format("%.2f kW", state.powerKw)
        avgEffText?.text = String.format("%.1f Wh/km", state.avgEfficiencyWhKm)
    }

    private fun createStatText(initialText: String): TextView {
        return TextView(this).apply {
            text = initialText
            textSize = 15f
            setTypeface(Typeface.DEFAULT_BOLD)
            setTextColor(0xFFF3F4F6.toInt())
        }
    }

    private fun createSpacer(): TextView {
        return TextView(this).apply {
            text = "   "
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Habe 悬浮仪表运行中")
            .setContentText("后台显示速度、功率和平均能耗")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "悬浮仪表",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "后台悬浮仪表服务"
        }
        manager.createNotificationChannel(channel)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
