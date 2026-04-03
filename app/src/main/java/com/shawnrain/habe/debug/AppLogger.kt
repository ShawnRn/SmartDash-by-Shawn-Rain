package com.shawnrain.habe.debug

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

enum class AppLogLevel(val priority: Int, val label: String) {
    VERBOSE(Log.VERBOSE, "详细"),
    DEBUG(Log.DEBUG, "调试"),
    INFO(Log.INFO, "信息"),
    WARN(Log.WARN, "警告"),
    ERROR(Log.ERROR, "错误");

    companion object {
        fun fromName(name: String?): AppLogLevel {
            return entries.firstOrNull { it.name == name } ?: INFO
        }
    }
}

private data class AppLogEntry(
    val timestampMs: Long,
    val level: AppLogLevel,
    val tag: String,
    val message: String
)

object AppLogger {
    private const val MAX_ENTRIES = 4000
    private const val AUTHORITY_SUFFIX = ".fileprovider"
    private val timeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val logBuffer = ArrayDeque<AppLogEntry>(MAX_ENTRIES)

    @Volatile
    private var minLevel: AppLogLevel = AppLogLevel.INFO

    fun setMinLevel(level: AppLogLevel) {
        minLevel = level
        log(
            level = AppLogLevel.INFO,
            tag = "AppLogger",
            message = "日志级别已切换为 ${level.name}"
        )
    }

    fun getMinLevel(): AppLogLevel = minLevel

    fun v(tag: String, message: String) = log(AppLogLevel.VERBOSE, tag, message)
    fun d(tag: String, message: String) = log(AppLogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(AppLogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = log(AppLogLevel.WARN, tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) =
        log(AppLogLevel.ERROR, tag, message, throwable)

    fun clear() {
        synchronized(logBuffer) {
            logBuffer.clear()
        }
    }

    fun exportLogs(context: Context): Uri {
        val exportTime = System.currentTimeMillis()
        val exportDir = (
            context.getExternalFilesDir("exports")
                ?: File(context.filesDir, "exports")
            ).apply { mkdirs() }
        val file = File(exportDir, "habe-debug-${exportTime}.log")
        file.writeText(buildLogDump(exportTime))
        return FileProvider.getUriForFile(
            context,
            context.packageName + AUTHORITY_SUFFIX,
            file
        )
    }

    fun createShareIntent(context: Context): Intent {
        val uri = exportLogs(context)
        return Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Habe 调试日志")
            // Intentionally omit EXTRA_TEXT so target apps treat this exclusively as a file share
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun buildLogDump(exportTime: Long): String {
        val snapshot = synchronized(logBuffer) { logBuffer.toList() }
        return buildString {
            appendLine("Habe Debug Log")
            appendLine("ExportedAt=${timeFormatter.format(Date(exportTime))}")
            appendLine("MinLevel=${minLevel.name}")
            appendLine("Entries=${snapshot.size}")
            appendLine()
            snapshot.forEach { entry ->
                append(timeFormatter.format(Date(entry.timestampMs)))
                append(' ')
                append(entry.level.name.padEnd(7, ' '))
                append(' ')
                append(entry.tag)
                append(": ")
                appendLine(entry.message)
            }
        }
    }

    private fun log(
        level: AppLogLevel,
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {
        if (level.priority < minLevel.priority) return

        val fullMessage = if (throwable == null) {
            message
        } else {
            message + "\n" + Log.getStackTraceString(throwable)
        }

        synchronized(logBuffer) {
            while (logBuffer.size >= MAX_ENTRIES) {
                logBuffer.removeFirst()
            }
            logBuffer.addLast(
                AppLogEntry(
                    timestampMs = System.currentTimeMillis(),
                    level = level,
                    tag = tag,
                    message = fullMessage
                )
            )
        }

        Log.println(level.priority, tag, fullMessage)
    }
}
