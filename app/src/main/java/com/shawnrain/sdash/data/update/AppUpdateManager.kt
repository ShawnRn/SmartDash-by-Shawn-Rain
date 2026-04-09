package com.shawnrain.sdash.data.update

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.shawnrain.sdash.debug.AppLogger
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class AppUpdateManager(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient()
) {
    companion object {
        private const val TAG = "AppUpdateManager"
        private const val REPO_BASE_URL =
            "https://github.com/ShawnRn/SmartDash-by-Shawn-Rain"
        private const val RELEASES_LATEST_DOWNLOAD_BASE_URL =
            "$REPO_BASE_URL/releases/latest/download"
        private const val RELEASES_LATEST_MANIFEST_URL =
            "$RELEASES_LATEST_DOWNLOAD_BASE_URL/release-manifest.json"
        private const val RELEASES_LATEST_URL =
            "https://api.github.com/repos/ShawnRn/SmartDash-by-Shawn-Rain/releases/latest"
        private val versionRegex = Regex("""(\d+)\.(\d+)\.(\d+)""")
        private val buildRegex = Regex("""(?im)build(?:\s+code)?\s*[:=]?\s*(\d{6,})""")
        private val json = Json { ignoreUnknownKeys = true }
    }

    private val prefs = AppUpdatePreferences(context)

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

    /**
     * Checks for updates by fetching the latest release from GitHub.
     * Uses release-manifest.json if available, otherwise falls back to parsing the release notes and assets.
     */
    suspend fun checkForUpdate(honorIgnoredTag: Boolean = true): Result<AppUpdatePackage?> = withContext(Dispatchers.IO) {
        runCatching {
            val installedVersion = getInstalledVersion()
            reconcileInstalledUpdate(installedVersion)
            val cachedEtag = prefs.etag.first()
            val checkedAt = System.currentTimeMillis()
            val ignoredTag = prefs.ignoredTag.first()
            val conditionalResult = fetchLatestReleasePackage(cachedEtag)

            if (conditionalResult.notModified) {
                val lastSeenTag = prefs.lastSeenTag.first()
                prefs.saveLastChecked(
                    atMs = checkedAt,
                    tag = lastSeenTag,
                    etag = conditionalResult.etag ?: cachedEtag
                )
                val shouldBypass304 = !lastSeenTag.isNullOrBlank() &&
                    compareSemanticVersion(normalizeTag(lastSeenTag), installedVersion.versionName) > 0 &&
                    (!honorIgnoredTag || ignoredTag != lastSeenTag)
                if (!shouldBypass304) {
                    return@runCatching null
                }
            }

            val pkg = if (conditionalResult.notModified) {
                fetchLatestReleasePackage(etag = null).pkg
            } else {
                conditionalResult.pkg
            }

            prefs.saveLastChecked(checkedAt, pkg?.tag, conditionalResult.etag ?: cachedEtag)

            if (honorIgnoredTag && pkg != null && ignoredTag == pkg.tag) {
                AppLogger.i(TAG, "Skip ignored release tag=${pkg.tag}")
                return@runCatching null
            }

            if (pkg != null && isNewerThanInstalled(pkg, installedVersion)) pkg else null
        }
    }

    private suspend fun fetchLatestReleasePackage(etag: String?): LatestReleaseFetchResult {
        fetchStaticManifestPackage(etag)?.let { return it }
        AppLogger.w(TAG, "Static release manifest unavailable, falling back to GitHub API")

        val request = Request.Builder()
            .url(RELEASES_LATEST_URL)
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "${context.packageName}/android")
            .apply {
                if (!etag.isNullOrBlank()) {
                    header("If-None-Match", etag)
                }
            }
            .build()

        client.newCall(request).execute().use { response ->
            val responseEtag = response.header("ETag")
            if (response.code == 304) {
                return LatestReleaseFetchResult(
                    pkg = null,
                    etag = responseEtag ?: etag,
                    notModified = true
                )
            }
            if (!response.isSuccessful) {
                error("检查更新失败：HTTP ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            val githubRelease = json.decodeFromString<GithubReleaseResponse>(body)
            val manifestAsset = githubRelease.assets.firstOrNull {
                it.name.equals("release-manifest.json", ignoreCase = true) ||
                    it.name.endsWith("-manifest.json", ignoreCase = true)
            }
            val pkg = if (manifestAsset != null) {
                fetchManifest(manifestAsset.downloadUrl, githubRelease.assets)
            } else {
                parseReleaseFromAssets(githubRelease)
            }
            return LatestReleaseFetchResult(pkg = pkg, etag = responseEtag, notModified = false)
        }
    }

    private suspend fun fetchStaticManifestPackage(etag: String?): LatestReleaseFetchResult? {
        val request = Request.Builder()
            .url(RELEASES_LATEST_MANIFEST_URL)
            .header("Accept", "application/json")
            .header("User-Agent", "${context.packageName}/android")
            .apply {
                if (!etag.isNullOrBlank()) {
                    header("If-None-Match", etag)
                }
            }
            .build()

        return runCatching {
            client.newCall(request).execute().use { response ->
                val responseEtag = response.header("ETag")
                when {
                    response.code == 304 -> {
                        AppLogger.i(TAG, "Static release manifest not modified")
                        LatestReleaseFetchResult(
                            pkg = null,
                            etag = responseEtag ?: etag,
                            notModified = true
                        )
                    }

                    response.isSuccessful -> {
                        val body = response.body?.string().orEmpty()
                        val manifest = json.decodeFromString<ReleaseManifestJson>(body)
                        val pkg = manifest.toAppUpdatePackage()
                        if (pkg != null) {
                            AppLogger.i(TAG, "Loaded latest release from static manifest tag=${pkg.tag}")
                        }
                        LatestReleaseFetchResult(
                            pkg = pkg,
                            etag = responseEtag,
                            notModified = false
                        )
                    }

                    response.code == 404 -> {
                        AppLogger.w(TAG, "Static release manifest not found yet")
                        null
                    }

                    else -> {
                        AppLogger.w(TAG, "Static release manifest failed: HTTP ${response.code}")
                        null
                    }
                }
            }
        }.getOrElse { error ->
            AppLogger.w(TAG, "Static release manifest fetch failed: ${error.message}")
            null
        }
    }

    private suspend fun fetchManifest(url: String, assets: List<GithubReleaseAsset>): AppUpdatePackage? {
        return runCatching {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val manifest = json.decodeFromString<ReleaseManifestJson>(body)
                
                val apkAsset = assets.firstOrNull { it.name == manifest.apkName } ?: return null
                
                AppUpdatePackage(
                    tag = manifest.tag,
                    versionName = manifest.versionName,
                    versionCode = manifest.versionCode,
                    channel = ReleaseChannel.valueOf(manifest.channel.uppercase()),
                    apkName = manifest.apkName,
                    apkUrl = apkAsset.downloadUrl,
                    htmlUrl = manifest.htmlUrl,
                    sha256 = manifest.apkSha256,
                    publishedAt = manifest.publishedAt,
                    releaseNotes = manifest.releaseNotes
                )
            }
        }.getOrNull()
    }

    private fun parseReleaseFromAssets(release: GithubReleaseResponse): AppUpdatePackage? {
        val apkAsset = selectBestApkAsset(release) ?: return null
        
        return AppUpdatePackage(
            tag = release.tagName,
            versionName = normalizeTag(release.tagName),
            versionCode = resolveBuildCode(release.body, apkAsset.name) ?: 0L,
            channel = ReleaseChannel.fromTag(release.tagName),
            apkName = apkAsset.name,
            apkUrl = apkAsset.downloadUrl,
            htmlUrl = release.htmlUrl,
            publishedAt = release.publishedAt,
            releaseNotes = release.body.trim()
        )
    }

    /**
     * Downloads the APK with SHA256 verification.
     * Stores in filesDir/app-updates/
     */
    suspend fun downloadReleaseApk(
        pkg: AppUpdatePackage,
        onProgress: (Float?) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(pkg.apkUrl)
                .header("Accept", "application/octet-stream")
                .header("User-Agent", "${context.packageName}/android")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("下载更新失败：HTTP ${response.code}")
                }
                val body = response.body ?: error("下载更新失败：响应为空")
                
                // Use filesDir instead of cacheDir for persistence and reliability
                val targetDir = File(context.filesDir, "app-updates").apply { mkdirs() }
                
                // Clean up old update files
                targetDir.listFiles()?.forEach { if (it.name.endsWith(".apk")) it.delete() }
                
                val targetFile = File(targetDir, pkg.apkName)
                val totalLength = body.contentLength().takeIf { it > 0L }
                
                body.byteStream().use { input ->
                    targetFile.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytesRead = 0L
                        var lastProgressUpdate = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            bytesRead += read
                            
                            // Throttle progress updates
                            val now = System.currentTimeMillis()
                            if (now - lastProgressUpdate > 100) {
                                onProgress(totalLength?.let { bytesRead.toFloat() / it.toFloat() })
                                lastProgressUpdate = now
                            }
                        }
                    }
                }
                
                onProgress(1f)
                
                // SHA256 Verification
                if (!pkg.sha256.isNullOrBlank()) {
                    val actualSha = calculateSha256(targetFile)
                    if (!actualSha.equals(pkg.sha256, ignoreCase = true)) {
                        targetFile.delete()
                        prefs.clearDownload()
                        error("校验失败：SHA256 不匹配")
                    }
                    AppLogger.i(TAG, "SHA256 verified for ${pkg.apkName}")
                } else {
                    AppLogger.w(TAG, "No SHA256 provided for ${pkg.apkName}, skipping verification")
                }

                prefs.clearIgnoredTag()
                prefs.saveDownloadedApk(pkg.tag, targetFile.absolutePath)
                AppLogger.i(TAG, "Downloaded update apk=${targetFile.absolutePath}")
                targetFile
            }
        }
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead = input.read(buffer)
            while (bytesRead != -1) {
                digest.update(buffer, 0, bytesRead)
                bytesRead = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
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

    @Suppress("DEPRECATION")
    fun createInstallIntent(apkFile: File): Intent {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        return Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            data = apkUri
            clipData = ClipData.newRawUri(apkFile.name, apkUri)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        }
    }

    suspend fun findDownloadedApkPath(tag: String): String? = withContext(Dispatchers.IO) {
        val downloadedTag = prefs.downloadedTag.first()
        val downloadedPath = prefs.downloadedApkPath.first()
        if (downloadedTag != tag || downloadedPath.isNullOrBlank()) return@withContext null
        val file = File(downloadedPath)
        if (!file.exists()) {
            prefs.clearDownload()
            return@withContext null
        }
        downloadedPath
    }

    suspend fun ignoreRelease(tag: String) {
        prefs.setIgnoredTag(tag)
    }

    suspend fun markInstallLaunched(tag: String) {
        prefs.markInstallPending(tag)
    }

    suspend fun reconcileInstalledUpdate(installed: InstalledAppVersion): Boolean = withContext(Dispatchers.IO) {
        val pendingTag = prefs.installPendingTag.first()
        val downloadedTag = prefs.downloadedTag.first()
        val candidateTag = pendingTag ?: downloadedTag ?: return@withContext false
        val candidateVersion = normalizeTag(candidateTag)
        val installedOrNewer = compareSemanticVersion(installed.versionName, candidateVersion) >= 0
        if (!installedOrNewer) {
            if (pendingTag != null) prefs.clearInstallPending()
            return@withContext false
        }

        prefs.clearInstallPending()
        prefs.clearDownload()
        AppLogger.i(TAG, "Detected installed app update tag=$candidateTag version=${installed.versionName}")
        true
    }

    private fun isNewerThanInstalled(
        pkg: AppUpdatePackage,
        installed: InstalledAppVersion
    ): Boolean {
        return when {
            pkg.versionCode > 0 && pkg.versionCode > installed.versionCode -> true
            pkg.versionCode > 0 && pkg.versionCode < installed.versionCode -> false
            else -> compareSemanticVersion(pkg.versionName, installed.versionName) > 0
        }
    }

    private fun normalizeTag(tagName: String): String = tagName.removePrefix("v").trim()

    private data class LatestReleaseFetchResult(
        val pkg: AppUpdatePackage?,
        val etag: String?,
        val notModified: Boolean
    )

    private fun ReleaseManifestJson.toAppUpdatePackage(): AppUpdatePackage? {
        val resolvedChannel = runCatching { ReleaseChannel.valueOf(channel.uppercase()) }
            .getOrElse { ReleaseChannel.fromTag(tag) }
        val resolvedApkUrl = "$RELEASES_LATEST_DOWNLOAD_BASE_URL/$apkName"
        return AppUpdatePackage(
            tag = tag,
            versionName = versionName,
            versionCode = versionCode,
            channel = resolvedChannel,
            apkName = apkName,
            apkUrl = resolvedApkUrl,
            htmlUrl = htmlUrl ?: "$REPO_BASE_URL/releases/tag/$tag",
            sha256 = apkSha256,
            publishedAt = publishedAt,
            releaseNotes = releaseNotes
        )
    }

    private fun selectBestApkAsset(release: GithubReleaseResponse): GithubReleaseAsset? {
        val channel = ReleaseChannel.fromTag(release.tagName)
        return release.assets
            .asSequence()
            .filter { it.name.endsWith(".apk", ignoreCase = true) && it.downloadUrl.isNotBlank() }
            .map { asset -> asset to apkAssetScore(asset.name, channel) }
            .sortedByDescending { it.second }
            .firstOrNull { it.second > Int.MIN_VALUE }?.first
    }

    private fun apkAssetScore(name: String, channel: ReleaseChannel): Int {
        val lower = name.lowercase()
        if (!lower.endsWith(".apk")) return Int.MIN_VALUE
        if ("sha256" in lower || "mapping" in lower || "symbols" in lower) return Int.MIN_VALUE

        var score = 0
        if ("release" in lower) score += 80
        if ("smartdash" in lower || "sdash" in lower) score += 30
        if ("universal" in lower) score += 12
        if ("arm64" in lower) score += 6
        if ("signed" in lower) score += 4
        if ("debug" in lower) score -= 200
        if ("devrelease" in lower || "fastdevrelease" in lower || "fast-dev-release" in lower) score -= 220
        if ("test" in lower) score -= 160

        when (channel) {
            ReleaseChannel.STABLE -> if ("beta" in lower || "nightly" in lower) score -= 120
            ReleaseChannel.BETA -> if ("beta" in lower) score += 20
            ReleaseChannel.NIGHTLY -> if ("nightly" in lower) score += 20
        }
        return score
    }

    private fun resolveBuildCode(body: String, assetName: String): Long? {
        return buildRegex.find(body)?.groupValues?.getOrNull(1)?.toLongOrNull()
            ?: buildRegex.find(assetName)?.groupValues?.getOrNull(1)?.toLongOrNull()
    }

    private fun compareSemanticVersion(left: String, right: String): Int {
        val leftParts = versionRegex.find(left)?.groupValues?.drop(1)?.map { it.toIntOrNull() ?: 0 }.orEmpty()
        val rightParts = versionRegex.find(right)?.groupValues?.drop(1)?.map { it.toIntOrNull() ?: 0 }.orEmpty()
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

@Serializable
private data class ReleaseManifestJson(
    val tag: String,
    val versionName: String,
    val versionCode: Long,
    val channel: String,
    val apkName: String,
    val apkSha256: String,
    val publishedAt: String,
    val htmlUrl: String? = null,
    val releaseNotes: String? = null
)
