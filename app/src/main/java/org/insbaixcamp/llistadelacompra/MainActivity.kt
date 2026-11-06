package org.insbaixcamp.llistadelacompra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import org.insbaixcamp.llistadelacompra.ui.theme.LlistaDeLaCompraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
 Dani
        enableEdgeToEdge()

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

        setContent {
            LlistaDeLaCompraTheme {
                AuthGate { LlistaDeLaCompraApp() }
        setContent {
            LlistaDeLaCompraTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShoppingScreen(modifier = Modifier.padding(innerPadding))
                }
 master
            }
        }
    }
}