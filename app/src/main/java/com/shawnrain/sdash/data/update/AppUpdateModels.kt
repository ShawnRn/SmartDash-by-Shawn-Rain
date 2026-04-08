package com.shawnrain.sdash.data.update

import kotlinx.serialization.Serializable

@Serializable
enum class ReleaseChannel {
    STABLE, BETA, NIGHTLY;

    companion object {
        fun fromTag(tag: String): ReleaseChannel {
            val lower = tag.lowercase()
            return when {
                lower.contains("beta") -> BETA
                lower.contains("nightly") -> NIGHTLY
                else -> STABLE
            }
        }
    }
}

@Serializable
data class AppUpdatePackage(
    val tag: String,
    val versionName: String,
    val versionCode: Long,
    val channel: ReleaseChannel,
    val apkName: String,
    val apkUrl: String,
    val htmlUrl: String? = null,
    val sha256: String? = null,
    val publishedAt: String? = null,
    val releaseNotes: String? = null
)

data class InstalledAppVersion(
    val versionName: String,
    val versionCode: Long
) {
    val displayName: String = "v$versionName ($versionCode)"
}

sealed class AppUpdateState {
    abstract val currentVersion: InstalledAppVersion

    data class Idle(override val currentVersion: InstalledAppVersion) : AppUpdateState()
    data class Checking(override val currentVersion: InstalledAppVersion) : AppUpdateState()
    data class UpToDate(override val currentVersion: InstalledAppVersion, val checkedAt: Long) : AppUpdateState()
    data class Available(override val currentVersion: InstalledAppVersion, val pkg: AppUpdatePackage) : AppUpdateState()
    data class Downloading(
        override val currentVersion: InstalledAppVersion, 
        val pkg: AppUpdatePackage, 
        val progress: Float?
    ) : AppUpdateState()
    data class Downloaded(
        override val currentVersion: InstalledAppVersion, 
        val pkg: AppUpdatePackage, 
        val apkPath: String
    ) : AppUpdateState()
    data class InstallPermissionRequired(
        override val currentVersion: InstalledAppVersion, 
        val pkg: AppUpdatePackage, 
        val apkPath: String
    ) : AppUpdateState()
    data class Error(
        override val currentVersion: InstalledAppVersion, 
        val message: String, 
        val stage: String
    ) : AppUpdateState()

    // UI Helpers
    val isChecking: Boolean get() = this is Checking
    val isDownloading: Boolean get() = this is Downloading
    val hasDownloadedApk: Boolean get() = this is Downloaded || this is InstallPermissionRequired
    val isUpdateAvailable: Boolean get() = this is Available
    val errorMessage: String? get() = (this as? Error)?.message
    val downloadProgress: Float? get() = (this as? Downloading)?.progress
}
