package com.shawnrain.sdash.data.sync

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.shawnrain.sdash.debug.AppLogger
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption

data class GoogleAccountSession(
    val email: String,
    val displayName: String? = null
)

class GoogleAccountAuth(private val context: Context) {

    companion object {
        private const val TAG = "GoogleAccountAuth"
        private const val PREFS_NAME = "google_drive_account_auth"
        private const val KEY_EMAIL = "email"
        private const val KEY_DISPLAY_NAME = "display_name"

        // SmartDash Sign in with Google must use the Web application client ID.
        const val CLIENT_ID = "8447150714-u2k6i812nojbahgk7tkm2n0vcqa5mhi2.apps.googleusercontent.com"
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val credentialManager by lazy {
        CredentialManager.create(context)
    }

    suspend fun signIn(activity: Activity): GoogleAccountSession {
        AppLogger.i(TAG, "Starting explicit Google sign-in")

        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val response = runCatching {
            credentialManager.getCredential(
                context = activity,
                request = request
            )
        }.onFailure { error ->
            AppLogger.e(TAG, "CredentialManager getCredential failed", error)
        }.getOrThrow()

        val credential = response.credential
        AppLogger.i(TAG, "Received credential type=${credential.type}")
        val googleCredential = when {
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ->
                GoogleIdTokenCredential.createFrom(credential.data)

            else -> throw IllegalStateException("未拿到可用的 Google 账号凭据")
        }

        val session = GoogleAccountSession(
            email = googleCredential.id,
            displayName = googleCredential.displayName
        )
        saveSession(session)
        AppLogger.i(TAG, "Google sign-in succeeded email=${session.email}")
        return session
    }

    fun isSignedIn(): Boolean = getCurrentAccount() != null

    fun getCurrentAccount(): GoogleAccountSession? {
        val email = prefs.getString(KEY_EMAIL, null)?.trim().orEmpty()
        if (email.isBlank()) return null
        return GoogleAccountSession(
            email = email,
            displayName = prefs.getString(KEY_DISPLAY_NAME, null)
        )
    }

    suspend fun signOut() {
        prefs.edit().clear().apply()
        AppLogger.i(TAG, "Signing out Google account")
        runCatching {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }.onFailure { error ->
            AppLogger.w(TAG, "Failed to clear CredentialManager state: ${error.message}")
        }
    }

    suspend fun silentSignIn(): Boolean {
        val signedIn = isSignedIn()
        AppLogger.d(TAG, "silentSignIn result=$signedIn")
        return signedIn
    }

    private fun saveSession(session: GoogleAccountSession) {
        prefs.edit()
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .apply()
    }
}
