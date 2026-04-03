package com.shawnrain.habe.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.shawnrain.habe.MainActivity
import com.shawnrain.habe.R
import com.shawnrain.habe.debug.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    private const val PREFS_NAME = "overlay_hud"
    private const val KEY_HAS_POSITION = "has_position"
    private const val KEY_POSITION_X = "position_x"
    private const val KEY_POSITION_Y = "position_y"

    data class SavedPosition(
        val x: Int,
        val y: Int
    )

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

    fun loadSavedPosition(context: Context): SavedPosition? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_HAS_POSITION, false)) return null
        return SavedPosition(
            x = prefs.getInt(KEY_POSITION_X, 0),
            y = prefs.getInt(KEY_POSITION_Y, 0)
        )
    }

    fun savePosition(context: Context, x: Int, y: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_POSITION, true)
            .putInt(KEY_POSITION_X, x)
            .putInt(KEY_POSITION_Y, y)
            .apply()
    }
}

class HudOverlayService : Service() {
    companion object {
        private const val TAG = "HudOverlayService"
        private const val CHANNEL_ID = "hud_overlay"
        private const val NOTIFICATION_ID = 2001
        private const val TOUCH_GUARD_MS = 450L
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var windowManager: WindowManager
    private var overlayView: LinearLayout? = null
    private var overlayLayoutParams: WindowManager.LayoutParams? = null
    private var overlayShownAtMs: Long = 0L
    private var speedText: TextView? = null
    private var speedUnitText: TextView? = null
    private var powerText: TextView? = null
    private var avgEffText: TextView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        ensureNotificationChannel()
        startHudForeground()
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
            setPadding(dp(14), dp(12), dp(14), dp(12))
            isClickable = true
            isFocusable = true
            background = GradientDrawable().apply {
                setColor(0xDD14141BL.toInt())
                cornerRadius = dp(18).toFloat()
                setStroke(dp(1), 0x33FFFFFF)
            }
            setOnClickListener {
                startActivity(MainActivity.createLaunchIntent(this@HudOverlayService))
            }
        }

        val speedRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_HORIZONTAL
            isBaselineAligned = true
        }

        speedText = TextView(this).apply {
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setTextColor(0xFFE0E7FF.toInt())
            gravity = Gravity.CENTER
            text = "0"
            includeFontPadding = false
        }

        speedUnitText = TextView(this).apply {
            textSize = 12f
            setTextColor(0xFF9CA3AF.toInt())
            text = "km/h"
            typeface = Typeface.MONOSPACE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(4)
            }
            includeFontPadding = false
            gravity = Gravity.CENTER
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

        speedRow.addView(speedText)
        speedRow.addView(speedUnitText)
        container.addView(speedRow)
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
            y = dp(32)
        }

        OverlayHudController.loadSavedPosition(this)?.let { saved ->
            params.gravity = Gravity.TOP or Gravity.START
            params.x = saved.x
            params.y = saved.y.coerceAtLeast(dp(8))
        }

        container.setOnTouchListener(createDragTouchListener(container, params))
        windowManager.addView(container, params)
        overlayView = container
        overlayLayoutParams = params
        overlayShownAtMs = SystemClock.uptimeMillis()
        AppLogger.i(TAG, "悬浮窗已显示")
    }

    private fun hideOverlay() {
        overlayView?.let { view ->
            runCatching { windowManager.removeView(view) }
            AppLogger.i(TAG, "悬浮窗已移除")
        }
        overlayView = null
        overlayLayoutParams = null
        overlayShownAtMs = 0L
    }

    private fun render(state: OverlayHudState) {
        speedText?.text = state.speedKmh.roundToInt().toString()
        powerText?.text = String.format("%.2f kW", state.powerKw)
        avgEffText?.text = String.format("%.1f Wh/km", state.avgEfficiencyWhKm)
    }

    private fun createStatText(initialText: String): TextView {
        return TextView(this).apply {
            text = initialText
            textSize = 13f
            setTypeface(Typeface.DEFAULT_BOLD)
            setTextColor(0xFFF3F4F6.toInt())
        }
    }

    private fun createSpacer(): TextView {
        return TextView(this).apply {
            text = "  "
        }
    }

    private fun createDragTouchListener(
        view: View,
        params: WindowManager.LayoutParams
    ): View.OnTouchListener {
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        var downRawX = 0f
        var downRawY = 0f
        var startX = 0
        var startY = 0
        var dragging = false

        return View.OnTouchListener { _, event ->
            if (SystemClock.uptimeMillis() - overlayShownAtMs < TOUCH_GUARD_MS) {
                return@OnTouchListener true
            }
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val location = IntArray(2)
                    view.getLocationOnScreen(location)
                    downRawX = event.rawX
                    downRawY = event.rawY
                    startX = location[0]
                    startY = location[1]
                    dragging = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - downRawX).toInt()
                    val deltaY = (event.rawY - downRawY).toInt()
                    if (!dragging && (kotlin.math.abs(deltaX) > touchSlop || kotlin.math.abs(deltaY) > touchSlop)) {
                        dragging = true
                    }
                    if (dragging) {
                        params.gravity = Gravity.TOP or Gravity.START
                        params.x = startX + deltaX
                        params.y = (startY + deltaY).coerceAtLeast(dp(8))
                        overlayLayoutParams = params
                        runCatching { windowManager.updateViewLayout(view, params) }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (dragging) {
                        OverlayHudController.savePosition(this, params.x, params.y)
                    }
                    if (!dragging) {
                        view.performClick()
                    }
                    true
                }

                MotionEvent.ACTION_CANCEL -> true
                else -> false
            }
        }
    }

    private fun createNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            MainActivity.createLaunchIntent(this),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Habe 悬浮仪表运行中")
            .setContentText("后台显示速度、功率和平均能耗")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun startHudForeground() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
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
