package com.shawnrain.sdash.data.sync

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GoogleDriveAuthorization(
    private val context: Context,
    private val accountAuth: GoogleAccountAuth
) {

    companion object {
        const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
    }

    private val authorizationClient by lazy {
        Identity.getAuthorizationClient(context)
    }

    suspend fun beginAuthorization(account: GoogleAccountSession): DriveAuthorizationStep {
        val result = authorize(buildRequest(account))
        val accessToken = result.getAccessToken()
        return when {
            result.hasResolution() -> {
                val pendingIntent = result.pendingIntent
                    ?: throw IllegalStateException("Google Drive 授权缺少待处理交互")
                DriveAuthorizationStep.RequiresAuthorization(pendingIntent.intentSender)
            }

            !accessToken.isNullOrBlank() -> DriveAuthorizationStep.Authorized
            else -> throw IllegalStateException("Google Drive 授权未返回 access token")
        }
    }

    suspend fun getAccessToken(): String {
        val account = accountAuth.getCurrentAccount()
            ?: throw IllegalStateException("尚未登录 Google 账号")
        val result = authorize(buildRequest(account))
        if (result.hasResolution()) {
            throw IllegalStateException("Google Drive 授权需要重新确认，请在设置页重新登录")
        }
        return result.getAccessToken()
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Google Drive 未返回 access token")
    }

    fun completeAuthorization(data: Intent?): String {
        val intent = data ?: throw IllegalStateException("Google Drive 授权结果为空")
        val result = try {
            authorizationClient.getAuthorizationResultFromIntent(intent)
        } catch (error: ApiException) {
            throw IllegalStateException(error.localizedMessage ?: "Google Drive 授权失败", error)
        }
        return result.getAccessToken()
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Google Drive 授权成功，但未返回 access token")
    }

    private fun buildRequest(account: GoogleAccountSession): AuthorizationRequest {
        return AuthorizationRequest.builder()
            .setAccount(Account(account.email, "com.google"))
            .setRequestedScopes(listOf(Scope(DRIVE_APPDATA_SCOPE)))
            .build()
    }

    private suspend fun authorize(request: AuthorizationRequest): AuthorizationResult {
        return suspendCancellableCoroutine { continuation ->
            authorizationClient.authorize(request)
                .addOnSuccessListener { result -> continuation.resume(result) }
                .addOnFailureListener { error -> continuation.resumeWithException(error) }
        }
    }
}

sealed interface DriveAuthorizationStep {
    data object Authorized : DriveAuthorizationStep
    data class RequiresAuthorization(val intentSender: IntentSender) : DriveAuthorizationStep
}
