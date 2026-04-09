package com.shawnrain.sdash.data.update

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.updateDataStore by preferencesDataStore(name = "sdash_updates")

class AppUpdatePreferences(private val context: Context) {
    companion object {
        private val LAST_CHECKED_AT = longPreferencesKey("last_checked_at")
        private val LAST_SEEN_TAG = stringPreferencesKey("last_seen_tag")
        private val IGNORED_TAG = stringPreferencesKey("ignored_tag")
        private val DOWNLOADED_TAG = stringPreferencesKey("downloaded_tag")
        private val DOWNLOADED_APK_PATH = stringPreferencesKey("downloaded_apk_path")
        private val INSTALL_PENDING_TAG = stringPreferencesKey("install_pending_tag")
        private val ETAG = stringPreferencesKey("etag")
    }

    val lastCheckedAt: Flow<Long> = context.updateDataStore.data.map { it[LAST_CHECKED_AT] ?: 0L }
    val lastSeenTag: Flow<String?> = context.updateDataStore.data.map { it[LAST_SEEN_TAG] }
    val ignoredTag: Flow<String?> = context.updateDataStore.data.map { it[IGNORED_TAG] }
    val downloadedTag: Flow<String?> = context.updateDataStore.data.map { it[DOWNLOADED_TAG] }
    val downloadedApkPath: Flow<String?> = context.updateDataStore.data.map { it[DOWNLOADED_APK_PATH] }
    val installPendingTag: Flow<String?> = context.updateDataStore.data.map { it[INSTALL_PENDING_TAG] }
    val etag: Flow<String?> = context.updateDataStore.data.map { it[ETAG] }

    suspend fun saveLastChecked(atMs: Long, tag: String?, etag: String?) {
        context.updateDataStore.edit { prefs ->
            prefs[LAST_CHECKED_AT] = atMs
            if (tag != null) prefs[LAST_SEEN_TAG] = tag
            if (etag != null) prefs[ETAG] = etag
        }
    }

    suspend fun setIgnoredTag(tag: String) {
        context.updateDataStore.edit { it[IGNORED_TAG] = tag }
    }

    suspend fun clearIgnoredTag() {
        context.updateDataStore.edit { it.remove(IGNORED_TAG) }
    }

    suspend fun saveDownloadedApk(tag: String, path: String) {
        context.updateDataStore.edit { prefs ->
            prefs[DOWNLOADED_TAG] = tag
            prefs[DOWNLOADED_APK_PATH] = path
        }
    }

    suspend fun markInstallPending(tag: String) {
        context.updateDataStore.edit { prefs ->
            prefs[INSTALL_PENDING_TAG] = tag
        }
    }

    suspend fun clearInstallPending() {
        context.updateDataStore.edit { prefs ->
            prefs.remove(INSTALL_PENDING_TAG)
        }
    }

    suspend fun clearDownload() {
        context.updateDataStore.edit { prefs ->
            prefs.remove(DOWNLOADED_TAG)
            prefs.remove(DOWNLOADED_APK_PATH)
        }
    }
}
