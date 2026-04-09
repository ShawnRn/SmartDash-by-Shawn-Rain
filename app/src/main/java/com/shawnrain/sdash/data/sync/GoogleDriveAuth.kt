package com.shawnrain.sdash.data.sync

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * High-level Google Drive auth facade:
 * - GoogleAccountAuth handles account sign-in and current account persistence
 * - GoogleDriveAuthorization handles drive.appdata scope + access token acquisition
 */
class GoogleDriveAuth(context: Context) {

    private val accountAuth = GoogleAccountAuth(context)
    private val driveAuthorization = GoogleDriveAuthorization(context, accountAuth)

    suspend fun beginSignIn(activity: Activity): DriveAuthorizationStep {
        val account = accountAuth.signIn(activity)
        return driveAuthorization.beginAuthorization(account)
    }

    fun completeAuthorization(data: Intent?): String {
        return driveAuthorization.completeAuthorization(data)
    }

    fun isSignedIn(): Boolean = accountAuth.isSignedIn()

    fun getCurrentAccount(): GoogleAccountSession? = accountAuth.getCurrentAccount()

    suspend fun signOut() = accountAuth.signOut()

    suspend fun silentSignIn(): Boolean = accountAuth.silentSignIn()

    suspend fun getAccessToken(): String = driveAuthorization.getAccessToken()
}
