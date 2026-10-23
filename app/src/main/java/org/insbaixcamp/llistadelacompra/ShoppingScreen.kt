package org.insbaixcamp.llistadelacompra

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import java.util.UUID

// 1. MODEL DE DADES: Defineix l'estructura d'un producte
data class Producte(
    val id: String = UUID.randomUUID().toString(), // Genera un codi únic automàticament
    val nom: String,
    val categoria: String,
    val completat: Boolean = false // Per defecte, cap producte està comprat (completat)
)

// DICCIONARI INTEL·LIGENT (Característica extra): Relaciona paraules clau amb categories
val autoCategories = mapOf(
    "llet" to "Làctics",
    "formatge" to "Làctics",
    "pomes" to "Fruita",
    "carn" to "Carnisseria",
    "pa" to "Forn"
)

// 2. PANTALLA PRINCIPAL: On s'ajunta tota la interfície i la lògica
@Composable
fun ShoppingScreen(modifier: Modifier = Modifier) {
    // 'llistaProductes' és la memòria de l'app. Quan s'hi afegeix alguna cosa, la pantalla s'actualitza sola.
    // Més endavant, el teu company connectarà això a Firebase Firestore.
    val llistaProductes = remember { mutableStateListOf<Producte>() }

    // 'nouProducteText' guarda el que l'usuari està teclejant en temps real
    var nouProducteText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        Text(text = "La meva llista", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // CAMP DE TEXT I BOTÓ PER AFEGIR PRODUCTES
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nouProducteText,
                onValueChange = { nouProducteText = it }, // Actualitza la variable cada cop que tecleges
                label = { Text("Ex: Llet, Pomes...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (nouProducteText.isNotBlank()) { // Comprova que no estiguis afegint un espai en blanc
                        // AUTOCATEGORITZACIÓ: Busca la paraula en minúscules al diccionari. Si no hi és, posa "Altres".
                        val cat = autoCategories[nouProducteText.lowercase().trim()] ?: "Altres"

                        // Crea el producte i l'afegeix a la llista visual
                        llistaProductes.add(Producte(nom = nouProducteText.trim(), categoria = cat))

                        // Buida el camp de text per poder escriure el següent producte
                        nouProducteText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Afegir")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LLISTA AGRUPADA (LazyColumn només gasta memòria per dibuixar el que es veu a la pantalla)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Reorganitzem la llista base agrupant els productes per la seva categoria
            val productesAgrupats = llistaProductes.groupBy { it.categoria }

            // Recorrem cada grup (ex: primer "Làctics", després "Fruita"...)
            productesAgrupats.forEach { (categoria, productes) ->
                // Dibuixem el títol de la categoria
                item {
                    Text(
                        text = categoria,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                // Dibuixem cada producte que pertany a aquesta categoria cridant al disseny individual
                items(productes, key = { it.id }) { producte ->
                    ProducteItem(
                        producte = producte,
                        onCheckedChange = { isChecked ->
                            // Busca el producte a la llista i canvia el seu estat (completat o no)
                            val index = llistaProductes.indexOf(producte)
                            if (index != -1) llistaProductes[index] = producte.copy(completat = isChecked)
                        },
                        onDelete = {
                            // Esborra el producte de la llista
                            llistaProductes.remove(producte)
                        }
                    )
                }
            }
        }
    }
}

// 3. DISSENY INDIVIDUAL: Com es veu cada targeta de producte
@Composable
fun ProducteItem(
    producte: Producte,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        // Fila per posar el Checkbox, el nom i la paperera un al costat de l'altre
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = producte.completat, onCheckedChange = onCheckedChange)
            Text(
                text = producte.nom,
                modifier = Modifier.weight(1f),
                // Si el producte està completat, ratlla el text per complir el requisit de no esborrar-lo.
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (producte.completat) TextDecoration.LineThrough else TextDecoration.None
                )
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Esborrar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}