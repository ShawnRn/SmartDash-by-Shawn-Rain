package com.shawnrain.sdash.data.sync

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

data class GoogleAccountSession(
    val email: String,
    val displayName: String? = null
)

class GoogleAccountAuth(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "google_drive_account_auth"
        private const val KEY_EMAIL = "email"
        private const val KEY_DISPLAY_NAME = "display_name"

        // Existing SmartDash OAuth client id.
        const val CLIENT_ID = "8447150714-s2l193jktl69tpc4ja7o9q0squijoj7r.apps.googleusercontent.com"
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val credentialManager by lazy {
        CredentialManager.create(context)
    }

    suspend fun signIn(activity: Activity): GoogleAccountSession {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val response = credentialManager.getCredential(
            context = activity,
            request = request
        )

        val credential = response.credential
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
        runCatching {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
    }

    suspend fun silentSignIn(): Boolean = isSignedIn()

    private fun saveSession(session: GoogleAccountSession) {
        prefs.edit()
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .apply()
    }
}
