package com.shawnrain.habe.data.sync

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Handles Google Sign-In authentication for Drive access.
 * Uses the simplified Google Sign-In API with appDataFolder scope.
 */
class GoogleDriveAuth(private val context: Context) {

    companion object {
        // Scope for accessing only the app's private data folder on Drive
        private const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        const val SIGN_IN_REQUEST_CODE = 9001
        // OAuth 2.0 Client ID for Android (from Google Cloud Console)
        private const val CLIENT_ID = "8447150714-6g8ef28e8n2k7n01ek1816kqulpngu45.apps.googleusercontent.com"
    }

    private val googleSignInClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestId()
            .requestScopes(Scope(DRIVE_APPDATA_SCOPE))
            .build()
        GoogleSignIn.getClient(context, options)
    }

    /**
     * Returns the sign-in intent to launch via startActivityForResult.
     */
    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    /**
     * Checks if the user is already signed in with the required Drive scope.
     */
    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && GoogleSignIn.hasPermissions(
            account,
            Scope(DRIVE_APPDATA_SCOPE)
        )
    }

    /**
     * Gets the currently signed-in account, or null.
     */
    fun getCurrentAccount() = GoogleSignIn.getLastSignedInAccount(context)

    /**
     * Signs the user out.
     */
    suspend fun signOut() = suspendCancellableCoroutine { cont ->
        googleSignInClient.signOut()
            .addOnCompleteListener { cont.resume(Unit) }
    }

    /**
     * Silently signs in if credentials are cached.
     * Returns true if successful.
     */
    suspend fun silentSignIn(): Boolean = suspendCancellableCoroutine { cont ->
        googleSignInClient.silentSignIn()
            .addOnCompleteListener { task ->
                cont.resume(task.isSuccessful)
            }
    }
}
