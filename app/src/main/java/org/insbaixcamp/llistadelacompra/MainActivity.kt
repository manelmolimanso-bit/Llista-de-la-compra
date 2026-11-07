package org.insbaixcamp.llistadelacompra

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import org.insbaixcamp.llistadelacompra.ui.theme.LlistaDeLaCompraTheme

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
fun MainApp() {
    // Corrutina per poder tancar la sessió (signOut) des del botó
    val scope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // Evita que el login apliqui dues vegades els marges de la barra del sistema
                .consumeWindowInsets(innerPadding)
        ) {
            // AuthGate mostra el login si no hi ha usuari, o HomeScreen si ja ha iniciat sessió
            AuthGate { user ->
                HomeScreen(
                    user = user,
                    onSignOut = { context -> scope.launch { signOut(context) } }
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

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra superior discreta: nom de l'usuari a l'esquerra i botó de tancar sessió a la dreta
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hola, $displayName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Botón para cerrar sesión (signOut + clearCredentialState)
            TextButton(onClick = { onSignOut(context) }) {
                Text(text = "Tancar Sessió")
            }
        }

        ShoppingScreen(modifier = Modifier.weight(1f), userEmail = user.email ?: "")
    }
}
