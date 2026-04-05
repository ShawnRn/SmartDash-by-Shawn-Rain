package com.shawnrain.habe.data.migration

import android.net.Uri

data class LanBackupQrPayload(
    val host: String,
    val port: Int,
    val code: String
) {
    fun encodeToQrText(): String {
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(HOST)
            .appendQueryParameter(KEY_HOST, host)
            .appendQueryParameter(KEY_PORT, port.toString())
            .appendQueryParameter(KEY_CODE, code)
            .appendQueryParameter(KEY_VERSION, VERSION.toString())
            .build()
            .toString()
    }

    companion object {
        private const val SCHEME = "habe"
        private const val HOST = "lan-backup"
        private const val VERSION = 1
        private const val KEY_HOST = "host"
        private const val KEY_PORT = "port"
        private const val KEY_CODE = "code"
        private const val KEY_VERSION = "v"

        fun decode(raw: String): LanBackupQrPayload? {
            val uri = runCatching { Uri.parse(raw.trim()) }.getOrNull() ?: return null
            if (uri.scheme != SCHEME || uri.host != HOST) return null
            val host = uri.getQueryParameter(KEY_HOST)?.trim().orEmpty()
            val port = uri.getQueryParameter(KEY_PORT)?.toIntOrNull() ?: return null
            val code = uri.getQueryParameter(KEY_CODE)?.trim().orEmpty()
            if (host.isBlank() || port !in 1..65535) return null
            if (code.length !in 4..8 || code.any { !it.isDigit() }) return null
            return LanBackupQrPayload(host = host, port = port, code = code)
        }
    }
}
