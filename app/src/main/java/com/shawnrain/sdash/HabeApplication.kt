package com.shawnrain.sdash

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.shawnrain.sdash.debug.AppLogger
import java.util.concurrent.atomic.AtomicInteger

class HabeApplication : Application(), ViewModelStoreOwner {
    private val appViewModelStore = ViewModelStore()
    private val startedActivityCount = AtomicInteger(0)

    val appViewModelFactory: ViewModelProvider.Factory by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
    }

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

            override fun onActivityStarted(activity: Activity) {
                startedActivityCount.incrementAndGet()
            }

            override fun onActivityResumed(activity: Activity) = Unit

            override fun onActivityPaused(activity: Activity) = Unit

            override fun onActivityStopped(activity: Activity) {
                startedActivityCount.decrementAndGet()
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

            override fun onActivityDestroyed(activity: Activity) {
                // 无 FGS 时划掉卡片不会走 Service.onTaskRemoved，只能靠 Activity 销毁兜底。
                // vivo 白名单包尤其容易出现「卡片没了、进程还在」。
                if (!activity.isFinishing || AppProcessExit.isExitScheduled()) return
                val remainingStarted = startedActivityCount.get()
                if (remainingStarted > 0) return
                AppLogger.i(
                    "HabeApplication",
                    "Last finishing activity destroyed (${activity.javaClass.simpleName}), scheduling process exit"
                )
                AppProcessExit.schedule(
                    context = this@HabeApplication,
                    reason = "last_activity_destroyed",
                    allowRecordingGrace = true
                )
            }
        })
    }
}
