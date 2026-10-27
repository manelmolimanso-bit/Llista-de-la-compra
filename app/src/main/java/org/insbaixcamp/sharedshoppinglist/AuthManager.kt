package org.insbaixcamp.sharedshoppinglist

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    companion object {
        private const val TAG = "AuthManager"
        const val WEB_CLIENT_ID = "445154804528-b2m35u886cecdr3f9i4p0n705g92jvvf.apps.googleusercontent.com"
    }

    // Iniciar sessió amb Google usant Credential Manager
    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "Iniciant procés de login amb Google...")

            val activity = context as? Activity
                ?: throw Exception("El context proporcionat no és una Activity vàlida")

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activity)
            Log.d(TAG, "Sol·licitant credencials a CredentialManager...")
            val result = credentialManager.getCredential(activity, request)

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

            Log.d(TAG, "Autenticant amb Firebase Auth...")
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user ?: throw Exception("Error al trobar l'usuari de Firebase")

            Log.d(TAG, "Login correcte per a l'usuari: ${user.email}")
            Result.success(user)

        } catch (_: GetCredentialCancellationException) {
            Log.w(TAG, "L'usuari ha cancel·lat el diàleg de selecció de compte de Google")
            Result.failure(Exception("S'ha cancel·lat l'inici de sessió."))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Error de CredentialManager: ${e.type} - ${e.message}", e)
            Result.failure(Exception("Error de credencials: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Error durant l'autenticació amb Google", e)
            Result.failure(e)
        }
    }

    // Tancar sessió
    suspend fun signOut(context: Context) {
        try {
            auth.signOut()
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Log.d(TAG, "Sessió tancada correctament")
        } catch (e: Exception) {
            Log.e(TAG, "Error en tancar sessió", e)
        }
    }
}
