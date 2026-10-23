package org.insbaixcamp.llistadelacompra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.insbaixcamp.llistadelacompra.data.repository.AuthRepository

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUserFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.currentUser
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Si us plau, omple l'email i la contrasenya."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepository.signInWithEmail(email.trim(), pass.trim())
                .onFailure { e ->
                    _errorMessage.value = e.localizedMessage ?: "Error en iniciar sessió"
                }
            _isLoading.value = false
        }
    }

    fun signUpWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Si us plau, omple l'email i la contrasenya."
            return
        }
        if (pass.length < 6) {
            _errorMessage.value = "La contrasenya ha de tenir almenys 6 caràcters."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepository.signUpWithEmail(email.trim(), pass.trim())
                .onFailure { e ->
                    _errorMessage.value = e.localizedMessage ?: "Error en registrar-se"
                }
            _isLoading.value = false
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepository.signInAnonymously()
                .onFailure { e ->
                    _errorMessage.value = e.localizedMessage ?: "Error en mode convidat"
                }
            _isLoading.value = false
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
