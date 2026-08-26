package com.shawnrain.sdash

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import com.shawnrain.sdash.data.dashcam.DashcamManager
import com.shawnrain.sdash.data.dashcam.DashcamState
import com.shawnrain.sdash.debug.AppLogger
import com.shawnrain.sdash.service.DashcamForegroundService
import com.shawnrain.sdash.worker.DrivePullWorker
import com.shawnrain.sdash.worker.DrivePushWorker
import com.shawnrain.sdash.worker.DriveReconcileWorker
import com.shawnrain.sdash.worker.PeriodicDriveSyncWorker
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 统一「彻底退出」协调器。
 *
 * vivo 版包名 `com.vivo.bsptest` 容易被系统白名单/厂商策略保活：
 * 划掉后台卡片后 Activity 销毁了，但前台服务通知、WakeLock、
 * WorkManager 或未释放的相机/BLE 仍可能让进程挂着。
 *
 * 本类保证：
 * 1. 多入口（设置退出 / 划掉卡片 / Service.onTaskRemoved）只调度一次
 * 2. 先拆掉保活锚点（FGS 通知、录像、相机），再延迟杀进程
 * 3. 未在录像时用更短延迟，尽快结束进程
 */
object AppProcessExit {
    private const val TAG = "AppProcessExit"
    private const val DASHCAM_NOTIFICATION_ID = 20260605

    private val exitScheduled = AtomicBoolean(false)

    fun isExitScheduled(): Boolean = exitScheduled.get()

    /**
     * @param allowRecordingGrace 是否为录像收尾预留更长缓冲（muxer / 落盘）
     */
    fun schedule(
        context: Context,
        reason: String,
        allowRecordingGrace: Boolean = true
    ) {
        if (!exitScheduled.compareAndSet(false, true)) {
            AppLogger.i(TAG, "Exit already scheduled, skip reason=$reason")
            return
        }

        val appContext = context.applicationContext
        val isVivoProtectedPackage = appContext.packageName == "com.vivo.bsptest" ||
            Build.MANUFACTURER.equals("vivo", ignoreCase = true) ||
            Build.BRAND.equals("vivo", ignoreCase = true) ||
            Build.BRAND.equals("iqoo", ignoreCase = true)

        AppLogger.i(
            TAG,
            "Scheduling process exit reason=$reason vivoProtected=$isVivoProtectedPackage " +
                "pkg=${appContext.packageName} pid=${Process.myPid()}"
        )

        tearDownKeepAliveAnchors(appContext)

        val recordingOrMuxing = runCatching {
            val state = DashcamManager.getInstance(appContext).state.value
            state == DashcamState.RECORDING || state == DashcamState.ERROR
        }.getOrDefault(false)

        val delayMs = when {
            allowRecordingGrace && recordingOrMuxing -> 1_500L
            isVivoProtectedPackage -> 350L
            else -> 600L
        }

        Handler(Looper.getMainLooper()).postDelayed({
            AppLogger.i(TAG, "Killing process pid=${Process.myPid()} after ${delayMs}ms ($reason)")
            // 再清一次通知，防止厂商在 stopService 后残留 FGS 锚点
            clearDashcamNotification(appContext)
            runCatching {
                Process.killProcess(Process.myPid())
            }
            runCatching {
                System.exit(0)
            }
        }, delayMs)
    }

    /**
     * 拆除会让厂商认为「进程仍需保活」的锚点。
     * 可安全重复调用。
     */
    fun tearDownKeepAliveAnchors(context: Context) {
        val appContext = context.applicationContext

        runCatching {
            val dashcam = DashcamManager.getInstance(appContext)
            dashcam.stopRecording()
            dashcam.stopPreviewOnly()
        }.onFailure {
            AppLogger.e(TAG, "Failed to stop dashcam during exit teardown", it)
        }

        runCatching {
            DashcamForegroundService.stopService(appContext)
        }.onFailure {
            AppLogger.e(TAG, "Failed to stop DashcamForegroundService", it)
        }

        clearDashcamNotification(appContext)

        // 取消可能在退出后把进程重新拉起的同步任务；下次冷启动会由 SyncScheduler 重新调度
        runCatching {
            PeriodicDriveSyncWorker.cancelPeriodicSync(appContext)
        }
        runCatching {
            androidx.work.WorkManager.getInstance(appContext).apply {
                cancelUniqueWork(DrivePullWorker.WORK_NAME)
                cancelUniqueWork(DrivePushWorker.WORK_NAME)
                cancelUniqueWork(DriveReconcileWorker.WORK_NAME)
            }
        }.onFailure {
            AppLogger.e(TAG, "Failed to cancel WorkManager tasks on exit", it)
        }
    }

    fun clearDashcamNotification(context: Context) {
        runCatching {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(DASHCAM_NOTIFICATION_ID)
        }.onFailure {
            AppLogger.e(TAG, "Failed to cancel dashcam notification", it)
        }
    }
}
