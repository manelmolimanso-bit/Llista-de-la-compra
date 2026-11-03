package org.insbaixcamp.llistadelacompra

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import java.util.UUID


data class Producte(
    val id: String = UUID.randomUUID().toString(),
    val nom: String,
    val categoria: String,
    val completat: Boolean = false
)


val autoCategories = mapOf(
    // Làctics i derivats
    "llet" to "Làctics",
    "formatge" to "Làctics",
    "iogurt" to "Làctics",
    "mantega" to "Làctics",
    "nata" to "Làctics",

    // Fruita
    "pomes" to "Fruita",
    "plàtans" to "Fruita",
    "peres" to "Fruita",
    "taronges" to "Fruita",
    "maduixes" to "Fruita",
    "raïm" to "Fruita",
    "llimones" to "Fruita",

    // Verdura i Hortalisses
    "enciam" to "Verdura",
    "tomàquets" to "Verdura",
    "cebes" to "Verdura",
    "patates" to "Verdura",
    "pastanagues" to "Verdura",
    "pebrots" to "Verdura",
    "alls" to "Verdura",

    // Carnisseria i Embotits
    "carn" to "Carnisseria",
    "pollastre" to "Carnisseria",
    "vedella" to "Carnisseria",
    "porc" to "Carnisseria",
    "salsitxes" to "Carnisseria",
    "pernil" to "Carnisseria",
    "embotit" to "Carnisseria",
    "hamburgueses" to "Carnisseria",

    // Peixateria
    "peix" to "Peixateria",
    "salmó" to "Peixateria",
    "lluç" to "Peixateria",
    "tonyina" to "Peixateria",
    "gambes" to "Peixateria",
    "bacallà" to "Peixateria",

    // Forn i Pastisseria
    "pa" to "Forn",
    "croissants" to "Forn",
    "magdalenes" to "Forn",
    "galetes" to "Forn",
    "torrades" to "Forn",

    // Rebost i Bàsics
    "arròs" to "Rebost",
    "pasta" to "Rebost",
    "macarrons" to "Rebost",
    "espaguetis" to "Rebost",
    "oli" to "Rebost",
    "sal" to "Rebost",
    "sucre" to "Rebost",
    "farina" to "Rebost",
    "ous" to "Rebost",
    "cigrons" to "Rebost",
    "llenties" to "Rebost",

    // Begudes
    "aigua" to "Begudes",
    "cervesa" to "Begudes",
    "vi" to "Begudes",
    "suc" to "Begudes",
    "refresc" to "Begudes",
    "cafè" to "Begudes",

    // Neteja i Llar
    "sabó" to "Neteja",
    "lleixiu" to "Neteja",
    "detergent" to "Neteja",
    "suavitzant" to "Neteja",
    "paper de wàter" to "Neteja",
    "escombres" to "Neteja",

    // Congelats
    "gelat" to "Congelats",
    "pèsols" to "Congelats",
    "croquetes" to "Congelats",
    "pizza" to "Congelats"
)


@Composable
fun ShoppingScreen(modifier: Modifier = Modifier) {
    val llistaProductes = remember { mutableStateListOf<Producte>() }
    var nouProducteText by remember { mutableStateOf("") }

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
                        llistaProductes.add(Producte(nom = nouProducteText.trim(), categoria = cat))
                        nouProducteText = ""
                    }
                }
            ) {
                // Icono cambiado a texto para evitar error de librería
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
                            val index = llistaProductes.indexOf(producte)
                            if (index != -1) llistaProductes[index] = producte.copy(completat = isChecked)
                        },
                        onDelete = { llistaProductes.remove(producte) }
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
                // Icono cambiado a texto rojo para evitar error de librería
                Text("X", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}