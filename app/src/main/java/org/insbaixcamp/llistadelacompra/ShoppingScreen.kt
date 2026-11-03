package org.insbaixcamp.llistadelacompra

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import java.util.UUID

// IMPORTACIONES DE FIREBASE
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// 1. MODELO DE DATOS
data class Producte(
    val id: String = UUID.randomUUID().toString(),
    val nom: String = "",
    val categoria: String = "",
    val completat: Boolean = false
)

// 2. DICCIONARIO PARA AUTOCATEGORIZACIÓN
val autoCategories = mapOf(
    "llet" to "Làctics", "formatge" to "Làctics", "iogurt" to "Làctics", "mantega" to "Làctics",
    "pomes" to "Fruita", "plàtans" to "Fruita", "taronges" to "Fruita", "llimones" to "Fruita",
    "enciam" to "Verdura", "tomàquets" to "Verdura", "cebes" to "Verdura", "patates" to "Verdura",
    "carn" to "Carnisseria", "pollastre" to "Carnisseria", "vedella" to "Carnisseria", "porc" to "Carnisseria",
    "peix" to "Peixateria", "salmó" to "Peixateria", "tonyina" to "Peixateria",
    "pa" to "Forn", "croissants" to "Forn", "galetes" to "Forn",
    "arròs" to "Rebost", "pasta" to "Rebost", "oli" to "Rebost", "sal" to "Rebost", "ous" to "Rebost",
    "aigua" to "Begudes", "cervesa" to "Begudes", "vi" to "Begudes", "refresc" to "Begudes",
    "sabó" to "Neteja", "lleixiu" to "Neteja", "detergent" to "Neteja", "paper de wàter" to "Neteja",
    "gelat" to "Congelats", "pèsols" to "Congelats", "pizza" to "Congelats"
)

// 3. PANTALLA PRINCIPAL
@Composable
fun ShoppingScreen(modifier: Modifier = Modifier) {
    val db = Firebase.firestore
    val llistaProductes = remember { mutableStateListOf<Producte>() }
    var nouProducteText by remember { mutableStateOf("") }

    // DESCARGAR DATOS DE FIREBASE EN TIEMPO REAL
    LaunchedEffect(Unit) {
        db.collection("shopping_lists").document("lista_de_prueba").collection("productes")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    llistaProductes.clear()
                    for (document in snapshot.documents) {
                        val producte = document.toObject(Producte::class.java)
                        if (producte != null) llistaProductes.add(producte)
                    }
                }
            }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        Text(text = "La meva llista", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nouProducteText,
                onValueChange = { nouProducteText = it },
                label = { Text("Ex: Llet, Pomes...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (nouProducteText.isNotBlank()) {
                        val cat = autoCategories[nouProducteText.lowercase().trim()] ?: "Altres"
                        val nouProducte = Producte(nom = nouProducteText.trim(), categoria = cat)

                        // SUBIR NUEVO PRODUCTO A FIREBASE
                        db.collection("shopping_lists").document("lista_de_prueba").collection("productes")
                            .document(nouProducte.id).set(nouProducte)

                        nouProducteText = ""
                    }
                }
            ) {
                Text("+")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val productesAgrupats = llistaProductes.groupBy { it.categoria }

            productesAgrupats.forEach { (categoria, productes) ->
                item {
                    Text(
                        text = categoria,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(productes, key = { it.id }) { producte ->
                    ProducteItem(
                        producte = producte,
                        onCheckedChange = { isChecked ->
                            // ACTUALIZAR ESTADO DEL CHECKBOX EN FIREBASE
                            db.collection("shopping_lists").document("lista_de_prueba").collection("productes")
                                .document(producte.id).update("completat", isChecked)
                        },
                        onDelete = {
                            // BORRAR PRODUCTO DE FIREBASE
                            db.collection("shopping_lists").document("lista_de_prueba").collection("productes")
                                .document(producte.id).delete()
                        }
                    )
                }
            }
        }
    }
}

// 4. DISEÑO DE LA FILA DE PRODUCTO
@Composable
fun ProducteItem(
    producte: Producte,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = producte.completat, onCheckedChange = onCheckedChange)
            Text(
                text = producte.nom,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (producte.completat) TextDecoration.LineThrough else TextDecoration.None
                )
            )
            IconButton(onClick = onDelete) {
                Text("X", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}