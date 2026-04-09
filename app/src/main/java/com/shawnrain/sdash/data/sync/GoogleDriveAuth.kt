package com.shawnrain.sdash.data.sync

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.shawnrain.sdash.debug.AppLogger

/**
 * High-level Google Drive auth facade:
 * - GoogleAccountAuth handles account sign-in and current account persistence
 * - GoogleDriveAuthorization handles drive.appdata scope + access token acquisition
 */
class GoogleDriveAuth(context: Context) {
    companion object {
        private const val TAG = "GoogleDriveAuth"
    }

    private val accountAuth = GoogleAccountAuth(context)
    private val driveAuthorization = GoogleDriveAuthorization(context, accountAuth)

    suspend fun beginSignIn(activity: Activity): DriveAuthorizationStep {
        AppLogger.i(TAG, "beginSignIn invoked")
        val account = accountAuth.signIn(activity)
        return driveAuthorization.beginAuthorization(account)
    }

    fun completeAuthorization(data: Intent?): String {
        AppLogger.i(TAG, "completeAuthorization invoked")
        return driveAuthorization.completeAuthorization(data)
    }

    fun isSignedIn(): Boolean = accountAuth.isSignedIn()

    fun getCurrentAccount(): GoogleAccountSession? = accountAuth.getCurrentAccount()

    suspend fun signOut() {
        AppLogger.i(TAG, "signOut invoked")
        accountAuth.signOut()
    }

    suspend fun silentSignIn(): Boolean = accountAuth.silentSignIn()

    suspend fun getAccessToken(): String = driveAuthorization.getAccessToken()
}
