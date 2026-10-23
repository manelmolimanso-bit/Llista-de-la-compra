package org.insbaixcamp.llistadelacompra

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import org.insbaixcamp.llistadelacompra.ui.theme.LlistaDeLaCompraTheme
import java.util.Date

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isChecked: Boolean = false
)

class MainActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        db = Firebase.firestore

        val testData = hashMapOf(
            "mensaje" to "Prueba de conexión"
            "fecha" to Date()
        )

        db.collection("test_conexion")
            .add(testData)
            .addOnSuccessListener { doc ->
                Log.d("FIREBASE_TEST", "¡CONEXIÓN EXITOSA! ID: ${doc.id}")
            }
            .addOnFailureListener { error ->
                Log.e("FIREBASE_TEST", "ERROR AL CONECTAR:", error)
            }
        // ==========================================

        setContent {
            LlistaDeLaCompraTheme {
                LlistaDeLaCompraApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun LlistaDeLaCompraApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting(
                name = "Android",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    FAVORITES("Favorites", R.drawable.ic_favorite),
    PROFILE("Profile", R.drawable.ic_account_box),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var texto by rememberSaveable {mutableStateOf("")}
    Column(modifier = modifier.fillMaxSize()){
        Text(text = "LISTA DE LA COMPRA BRUTAL")

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LlistaDeLaCompraTheme {
        Greeting("Android")
    }
}