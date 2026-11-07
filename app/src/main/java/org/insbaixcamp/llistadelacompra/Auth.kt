package org.insbaixcamp.llistadelacompra

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun AuthScreen() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()
    // Identificador del client web de Firebase (client_type 3 de google-services.json)
    val webClientId = "445154804528-b2m35u886cecdr3f9i4p0n705g92jvvf.apps.googleusercontent.com"

    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    // Fa que la pantalla d'inici de sessió aparegui amb un fos suau en lloc d'aparèixer de cop.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Quan el login va be, FirebaseAuth avisa a AuthGate i canvia de pantalla sol.
    fun handle(task: Task<AuthResult>) {
        task.addOnCompleteListener {
            loading = false
            error = task.exception?.localizedMessage
        }
    }

    fun googleSignIn() {
        loading = true
        error = null
        scope.launch {
            try {
                val option = GetSignInWithGoogleOption.Builder(webClientId).build()
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
            } catch (e: GetCredentialCancellationException) {
                // Si l'usuari tanca el diàleg de Google no es mostra cap error.
                loading = false
            } catch (e: Exception) {
                loading = false
                error = e.localizedMessage ?: "Error en iniciar sessió amb Google"
            }
        }
    }

    AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(600))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Llista de la compra", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Inicia sessió amb Google per continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(32.dp))
            if (loading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = { googleSignIn() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Continua amb Google")
                }
            }
        }
    }
}

/**
 * Envolta l'app: si no hi ha sessio iniciada mostra el login
 * quan l'usuari entra mostra el contingut tal com estava
 * El contingut rep l'usuari que ha iniciat sessió
 */
@Composable
fun AuthGate(content: @Composable (FirebaseUser) -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    var user by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { user = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val currentUser = user
    if (currentUser == null) AuthScreen() else content(currentUser)
}

/**
 * Tanca la sessió de Firebase i esborra l'estat de credencials de Google,
 * de manera que en tornar a entrar es pugui triar el compte de nou.
 * En tancar-la, AuthGate torna a mostrar la pantalla d'inici de sessió.
 */
suspend fun signOut(context: Context) {
    FirebaseAuth.getInstance().signOut()
    CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
}