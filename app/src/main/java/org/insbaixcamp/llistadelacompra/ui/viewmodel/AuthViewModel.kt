package org.insbaixcamp.llistadelacompra.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.insbaixcamp.llistadelacompra.R

// ViewModel para gestionar el inicio de sesión con Google y Firebase Auth
class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    // Usuario actual autenticado en Firebase
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // Estado de carga durante el proceso de login o logout
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mensajes de error para la interfaz de usuario
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Iniciar sesión con Google mediante Credential Manager
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Obtener el ID de cliente Web del google-services.json (generado en strings.xml)
                val webClientId = context.getString(R.string.default_web_client_id)

                // Crear opción de Google ID
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // Muestra todas las cuentas de Google del dispositivo
                    .setServerClientId(webClientId)
                    .build()

                // Crear la petición de credenciales
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // Solicitar credenciales al usuario con CredentialManager
                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context = context, request = request)

                // Extraer el token de ID de Google de la respuesta
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                // Autenticar en Firebase Auth con el token de Google
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()

                // Actualizar el estado con el usuario logueado
                _currentUser.value = authResult.user

            } catch (e: GetCredentialException) {
                Log.e("AuthViewModel", "Error al obtener credenciales de Google", e)
                _errorMessage.value = "Inicio de sesión cancelado o no disponible."
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al autenticar con Firebase", e)
                _errorMessage.value = "Error al iniciar sesión: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Cerrar sesión en Firebase Auth y limpiar credenciales en el dispositivo
    fun signOut(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cerrar sesión en Firebase
                auth.signOut()

                // Limpiar credenciales guardadas en el CredentialManager
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())

                // Actualizar el estado local
                _currentUser.value = null
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error al cerrar sesión", e)
                _errorMessage.value = "Error al cerrar sesión: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
