package com.shawnrain.sdash.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.shawnrain.sdash.debug.AppLogger
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

data class InstalledAppVersion(
    val versionName: String,
    val versionCode: Long
) {
    val displayName: String = "v$versionName ($versionCode)"
}

data class AppReleaseInfo(
    val tagName: String,
    val versionName: String,
    val buildCode: Long?,
    val title: String,
    val notes: String,
    val htmlUrl: String,
    val publishedAt: String?,
    val apkName: String,
    val apkUrl: String
)

data class AppUpdateUiState(
    val currentVersion: InstalledAppVersion,
    val isChecking: Boolean = false,
    val availableRelease: AppReleaseInfo? = null,
    val lastCheckedAt: Long? = null,
    val isDownloading: Boolean = false,
    val downloadProgress: Float? = null,
    val downloadedApkPath: String? = null,
    val errorMessage: String? = null
) {
    val isUpdateAvailable: Boolean get() = availableRelease != null
    val hasDownloadedApk: Boolean get() = !downloadedApkPath.isNullOrBlank()
}

class AppUpdateManager(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient()
) {
    companion object {
        private const val TAG = "AppUpdate"
        private const val RELEASES_LATEST_URL =
            "https://api.github.com/repos/ShawnRn/SmartDash-by-Shawn-Rain/releases/latest"
        private val versionRegex = Regex("""(\d+)\.(\d+)\.(\d+)""")
        private val buildRegex = Regex("""(?im)build(?:\s+code)?\s*[:=]?\s*(\d{6,})""")
    }

    fun getInstalledVersion(): InstalledAppVersion {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
        return InstalledAppVersion(
            versionName = packageInfo.versionName ?: "0.0.0",
            versionCode = versionCode
        )
    }

    suspend fun checkForUpdate(): Result<AppReleaseInfo?> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(RELEASES_LATEST_URL)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "${context.packageName}/android")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("检查更新失败：HTTP ${response.code}")
                }
                val body = response.body?.string().orEmpty()
                val release = json.decodeFromString<GithubReleaseResponse>(body)
                val apkAsset = release.assets.firstOrNull { asset ->
                    asset.name.endsWith(".apk", ignoreCase = true) &&
                        asset.downloadUrl.isNotBlank()
                } ?: return@use null
                val releaseInfo = AppReleaseInfo(
                    tagName = release.tagName,
                    versionName = normalizeTag(release.tagName),
                    buildCode = resolveBuildCode(release.body, apkAsset.name),
                    title = release.name.ifBlank { "SmartDash by Shawn Rain ${normalizeTag(release.tagName)}" },
                    notes = release.body.trim(),
                    htmlUrl = release.htmlUrl,
                    publishedAt = release.publishedAt,
                    apkName = apkAsset.name,
                    apkUrl = apkAsset.downloadUrl
                )
                if (isNewerThanInstalled(releaseInfo, getInstalledVersion())) {
                    releaseInfo
                } else {
                    null
                }
            }
        }
    }

    suspend fun downloadReleaseApk(
        release: AppReleaseInfo,
        onProgress: (Float?) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(release.apkUrl)
                .header("Accept", "application/octet-stream")
                .header("User-Agent", "${context.packageName}/android")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("下载更新失败：HTTP ${response.code}")
                }
                val body = response.body ?: error("下载更新失败：响应为空")
                val targetDir = File(context.cacheDir, "app-updates").apply { mkdirs() }
                targetDir.listFiles()?.forEach { existing ->
                    if (existing.name.endsWith(".apk", ignoreCase = true)) {
                        existing.delete()
                    }
                }
                val targetFile = File(targetDir, release.apkName)
                val total = body.contentLength().takeIf { it > 0L }
                body.byteStream().use { input ->
                    targetFile.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytesCopied = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            bytesCopied += read
                            onProgress(total?.let { bytesCopied.toFloat() / it.toFloat() })
                        }
                    }
                }
                onProgress(1f)
                AppLogger.i(TAG, "Downloaded update apk=${targetFile.absolutePath}")
                targetFile
            }
        }
    }

    fun canRequestPackageInstalls(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()
    }

    fun createManageUnknownSourcesIntent(): Intent {
        return Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun createInstallIntent(apkFile: File): Intent {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun isNewerThanInstalled(
        release: AppReleaseInfo,
        installed: InstalledAppVersion
    ): Boolean {
        val releaseBuildCode = release.buildCode
        return when {
            releaseBuildCode != null && releaseBuildCode > installed.versionCode -> true
            releaseBuildCode != null && releaseBuildCode < installed.versionCode -> false
            else -> compareSemanticVersion(release.versionName, installed.versionName) > 0
        }
    }

    private fun normalizeTag(tagName: String): String = tagName.removePrefix("v").trim()

    private fun resolveBuildCode(body: String, assetName: String): Long? {
        return buildRegex.find(body)?.groupValues?.getOrNull(1)?.toLongOrNull()
            ?: buildRegex.find(assetName)?.groupValues?.getOrNull(1)?.toLongOrNull()
    }

    private fun compareSemanticVersion(left: String, right: String): Int {
        val leftParts = versionRegex.find(left)?.groupValues?.drop(1)?.map { it.toInt() }.orEmpty()
        val rightParts = versionRegex.find(right)?.groupValues?.drop(1)?.map { it.toInt() }.orEmpty()
        val max = maxOf(leftParts.size, rightParts.size)
        for (index in 0 until max) {
            val leftValue = leftParts.getOrElse(index) { 0 }
            val rightValue = rightParts.getOrElse(index) { 0 }
            if (leftValue != rightValue) return leftValue.compareTo(rightValue)
        }
        return 0
    }
}

@Serializable
private data class GithubReleaseResponse(
    @SerialName("tag_name") val tagName: String,
    val name: String = "",
    val body: String = "",
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("published_at") val publishedAt: String? = null,
    val assets: List<GithubReleaseAsset> = emptyList()
)

@Serializable
private data class GithubReleaseAsset(
    val name: String,
    @SerialName("browser_download_url") val downloadUrl: String
)

private val json = Json { ignoreUnknownKeys = true }
