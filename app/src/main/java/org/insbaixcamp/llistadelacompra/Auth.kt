package org.insbaixcamp.llistadelacompra

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun AuthScreen() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    // Quan el login va be, FirebaseAuth avisa a LlistaDeLaCompraApp i canvia de pantalla sol.
    fun handle(task: Task<AuthResult>) {
        task.addOnCompleteListener {
            loading = false
            error = task.exception?.localizedMessage
        }
    }

    fun emailAction(create: Boolean) {
        val e = email.trim()
        if (e.isEmpty() || password.isEmpty()) {
            error = "Escriu el correu i la contrasenya"
            return
        }
        loading = true
        error = null
        handle(
            if (create) auth.createUserWithEmailAndPassword(e, password)
            else auth.signInWithEmailAndPassword(e, password)
        )
    }

    fun googleSignIn() {
        loading = true
        error = null
        scope.launch {
            try {
                val option = GetSignInWithGoogleOption.Builder(
                    context.getString(R.string.default_web_client_id)
                ).build()
                val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                val credential = CredentialManager.create(context).getCredential(context, request).credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    handle(auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)))
                } else {
                    loading = false
                    error = "No s'ha pogut obtenir el compte de Google"
                }
            } catch (e: Exception) {
                loading = false
                error = e.localizedMessage ?: "Error en iniciar sessió amb Google"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Llista de la compra", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correu") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contrasenya") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))
        if (loading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { emailAction(create = false) }, modifier = Modifier.fillMaxWidth()) {
                Text("Entra")
            }
            TextButton(onClick = { emailAction(create = true) }) { Text("Crea un compte nou") }
            OutlinedButton(onClick = { googleSignIn() }, modifier = Modifier.fillMaxWidth()) {
                Text("Continua amb Google")
            }
        }
    }
}

/**
 * Envolta l'app: si no hi ha sessio iniciada mostra el login,
 * i quan l'usuari entra mostra el contingut tal com estava.
 */
@Composable
fun AuthGate(content: @Composable () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    var user by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { user = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (user == null) AuthScreen() else content()
}
