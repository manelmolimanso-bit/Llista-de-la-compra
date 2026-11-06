package org.insbaixcamp.llistadelacompra

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseUser
import org.insbaixcamp.llistadelacompra.ui.screens.LoginScreen
import org.insbaixcamp.llistadelacompra.ui.theme.LlistaDeLaCompraTheme
import org.insbaixcamp.llistadelacompra.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LlistaDeLaCompraTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(
    authViewModel: AuthViewModel = viewModel()
) {
    // Comprobar el usuario actual autenticado en Firebase
    val currentUser by authViewModel.currentUser.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mostrar LoginScreen si no hay usuario, o HomeScreen si ya inició sesión
            if (currentUser == null) {
                LoginScreen(authViewModel = authViewModel)
            } else {
                HomeScreen(
                    user = currentUser!!,
                    onSignOut = { context -> authViewModel.signOut(context) }
                )
            }
        }
    }
}

// Pantalla provisional al iniciar sesión con éxito
@Composable
fun HomeScreen(
    user: FirebaseUser,
    onSignOut: (Context) -> Unit
) {
    val context = LocalContext.current
    val displayName = user.displayName ?: user.email ?: "Usuario"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hola, $displayName",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user.email ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para cerrar sesión (signOut + clearCredentialState)
        Button(
            onClick = { onSignOut(context) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "Cerrar sesión")
        }
    }
}
