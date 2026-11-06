package org.insbaixcamp.llistadelacompra

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.insbaixcamp.llistadelacompra.ui.theme.LlistaDeLaCompraTheme
import java.util.Date

class MainActivity : ComponentActivity() {

    // Declaramos la variable de la base de datos
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. TEST DE CONEXIÓN A FIREBASE
        db = Firebase.firestore
        val testData = hashMapOf("mensaje" to "Hola desde Android", "fecha" to Date())

        db.collection("test_conexion")
            .add(testData)
            .addOnSuccessListener { doc ->
                Log.d("FIREBASE_TEST", "¡CONEXIÓN EXITOSA! ID: ${doc.id}")
            }
            .addOnFailureListener { error ->
                Log.e("FIREBASE_TEST", "ERROR AL CONECTAR:", error)
            }
        // ==========================================

        // 2. INTERFAZ UNIFICADA
        setContent {
            LlistaDeLaCompraTheme {
                // AuthGate es el componente de Dani que gestiona el Login.
                // Lo que pongamos dentro de las llaves { } es lo que se verá cuando el usuario inicie sesión.
                AuthGate {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        ShoppingScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}