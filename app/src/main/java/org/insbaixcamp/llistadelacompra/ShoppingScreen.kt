package org.insbaixcamp.llistadelacompra

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
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
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// 1. MODELO DE DATOS
data class Producte(
    val id: String = UUID.randomUUID().toString(),
    val nom: String = "",
    val categoria: String = "",
    val completat: Boolean = false
)

// Categoria creada a mà dins d'una llista; els productes la referencien pel nom
data class Categoria(
    val id: String = UUID.randomUUID().toString(),
    val nom: String = ""
)

// Categoria per defecte on van els productes sense categoria coneguda; no es pot editar ni eliminar
const val CATEGORIA_PER_DEFECTE = "Altres"

// 2. DICCIONARIO PARA AUTOCATEGORIZACIÓN (Bilingüe y Masivo)
val autoCategories = mapOf(
    // 🥛 Lácteos y Derivados (Làctics)
    "leche" to "Lácteos", "llet" to "Lácteos",
    "queso" to "Lácteos", "formatge" to "Lácteos",
    "yogurt" to "Lácteos", "iogurt" to "Lácteos", "yogur" to "Lácteos",
    "mantequilla" to "Lácteos", "mantega" to "Lácteos",
    "nata" to "Lácteos", "kéfir" to "Lácteos", "kefir" to "Lácteos",
    "cuajada" to "Lácteos", "mató" to "Lácteos", "requesón" to "Lácteos",
    "batido" to "Lácteos", "batut" to "Lácteos",

    // 🍎 Fruta (Fruita)
    "manzana" to "Fruta", "poma" to "Fruta", "manzanas" to "Fruta", "pomes" to "Fruta",
    "plátano" to "Fruta", "plàtan" to "Fruta", "plátanos" to "Fruta", "plàtans" to "Fruta",
    "naranja" to "Fruta", "taronja" to "Fruta", "naranjas" to "Fruta", "taronges" to "Fruta",
    "limón" to "Fruta", "llimona" to "Fruta", "limones" to "Fruta", "llimones" to "Fruta",
    "fresa" to "Fruta", "maduixa" to "Fruta", "fresas" to "Fruta", "maduixes" to "Fruta",
    "uva" to "Fruta", "raïm" to "Fruta", "uvas" to "Fruta",
    "pera" to "Fruta", "peres" to "Fruta", "peras" to "Fruta",
    "melón" to "Fruta", "meló" to "Fruta",
    "sandía" to "Fruta", "síndria" to "Fruta",
    "melocotón" to "Fruta", "préssec" to "Fruta", "melocotones" to "Fruta",
    "cereza" to "Fruta", "cirera" to "Fruta", "cerezas" to "Fruta", "cireres" to "Fruta",
    "kiwi" to "Fruta", "kiwis" to "Fruta",
    "piña" to "Fruta", "pinya" to "Fruta",
    "mango" to "Fruta", "aguacate" to "Fruta", "alvocat" to "Fruta",
    "ciruela" to "Fruta", "pruna" to "Fruta",

    // 🥦 Verdura y Hortalizas (Verdura)
    "lechuga" to "Verdura", "enciam" to "Verdura",
    "tomate" to "Verdura", "tomàquet" to "Verdura", "tomates" to "Verdura", "tomàquets" to "Verdura",
    "cebolla" to "Verdura", "ceba" to "Verdura", "cebollas" to "Verdura", "cebes" to "Verdura",
    "patata" to "Verdura", "patatas" to "Verdura", "patates" to "Verdura",
    "zanahoria" to "Verdura", "pastanaga" to "Verdura", "zanahorias" to "Verdura", "pastanagues" to "Verdura",
    "pimiento" to "Verdura", "pebrot" to "Verdura", "pimientos" to "Verdura", "pebrots" to "Verdura",
    "ajo" to "Verdura", "all" to "Verdura", "ajos" to "Verdura", "alls" to "Verdura",
    "brócoli" to "Verdura", "bròquil" to "Verdura",
    "coliflor" to "Verdura",
    "espinacas" to "Verdura", "espinacs" to "Verdura",
    "calabacín" to "Verdura", "carbassó" to "Verdura",
    "berenjena" to "Verdura", "albergínia" to "Verdura",
    "pepino" to "Verdura", "cogombre" to "Verdura",
    "puerro" to "Verdura", "porro" to "Verdura",
    "champiñones" to "Verdura", "xampinyons" to "Verdura", "setas" to "Verdura", "bolets" to "Verdura",

    // 🥩 Carnicería y Embutidos (Carnisseria)
    "carne" to "Carnicería", "carn" to "Carnicería",
    "pollo" to "Carnicería", "pollastre" to "Carnicería", "pechugas" to "Carnicería", "pits de pollastre" to "Carnicería",
    "ternera" to "Carnicería", "vedella" to "Carnicería",
    "cerdo" to "Carnicería", "porc" to "Carnicería",
    "salchichas" to "Carnicería", "salsitxes" to "Carnicería", "frankfurt" to "Carnicería",
    "jamón" to "Carnicería", "pernil" to "Carnicería", "jamón dulce" to "Carnicería", "pernil dolç" to "Carnicería",
    "embutido" to "Carnicería", "embotit" to "Carnicería",
    "hamburguesa" to "Carnicería", "hamburguesas" to "Carnicería", "hamburgueses" to "Carnicería",
    "lomo" to "Carnicería", "llom" to "Carnicería",
    "chorizo" to "Carnicería", "xoriço" to "Carnicería",
    "salchichón" to "Carnicería", "llonganissa" to "Carnicería",
    "fuet" to "Carnicería",
    "bacon" to "Carnicería", "panceta" to "Carnicería", "cansalada" to "Carnicería",

    // 🐟 Pescadería (Peixateria)
    "pescado" to "Pescadería", "peix" to "Pescadería",
    "salmón" to "Pescadería", "salmó" to "Pescadería",
    "atún" to "Pescadería", "tonyina" to "Pescadería",
    "merluza" to "Pescadería", "lluç" to "Pescadería",
    "gambas" to "Pescadería", "gambes" to "Pescadería",
    "bacalao" to "Pescadería", "bacallà" to "Pescadería",
    "pulpo" to "Pescadería", "pop" to "Pescadería",
    "calamar" to "Pescadería", "calamares" to "Pescadería", "calamars" to "Pescadería",
    "sepia" to "Pescadería", "sípia" to "Pescadería",
    "mejillones" to "Pescadería", "musclos" to "Pescadería",
    "langostinos" to "Pescadería", "llagostins" to "Pescadería",
    "dorada" to "Pescadería", "lubina" to "Pescadería", "llobarro" to "Pescadería",
    "sardinas" to "Pescadería", "sardines" to "Pescadería",

    // 🥖 Panadería y Pastelería (Forn)
    "pan" to "Panadería", "pa" to "Panadería", "barra de pan" to "Panadería", "barra de pa" to "Panadería",
    "croissant" to "Panadería", "croissants" to "Panadería",
    "magdalenas" to "Panadería", "magdalenes" to "Panadería",
    "galletas" to "Panadería", "galetes" to "Panadería",
    "tostadas" to "Panadería", "torrades" to "Panadería",
    "pastel" to "Panadería", "pastís" to "Panadería", "tarta" to "Panadería",
    "ensaimada" to "Panadería",
    "pan de molde" to "Panadería", "pa de motlle" to "Panadería",

    // 🥫 Despensa y Básicos (Rebost)
    "arroz" to "Despensa", "arròs" to "Despensa",
    "pasta" to "Despensa", "macarrones" to "Despensa", "macarrons" to "Despensa", "espaguetis" to "Despensa",
    "aceite" to "Despensa", "oli" to "Despensa", "aceite de oliva" to "Despensa", "oli d'oliva" to "Despensa",
    "sal" to "Despensa",
    "azúcar" to "Despensa", "sucre" to "Despensa",
    "harina" to "Despensa", "farina" to "Despensa",
    "huevos" to "Despensa", "ous" to "Despensa", "huevo" to "Despensa", "ou" to "Despensa",
    "garbanzos" to "Despensa", "cigrons" to "Despensa",
    "lentejas" to "Despensa", "llenties" to "Despensa",
    "alubias" to "Despensa", "mongetes" to "Despensa",
    "vinagre" to "Despensa",
    "mayonesa" to "Despensa", "maionesa" to "Despensa",
    "ketchup" to "Despensa", "mostaza" to "Despensa", "mostassa" to "Despensa",
    "mermelada" to "Despensa", "confitura" to "Despensa",
    "miel" to "Despensa", "mel" to "Despensa",
    "cereales" to "Despensa", "cereals" to "Despensa", "avena" to "Despensa", "civada" to "Despensa",
    "frutos secos" to "Despensa", "fruits secs" to "Despensa", "nueces" to "Despensa", "nous" to "Despensa", "almendras" to "Despensa", "ametlles" to "Despensa",
    "conservas" to "Despensa", "conserves" to "Despensa", "olivas" to "Despensa", "olives" to "Despensa", "aceitunas" to "Despensa",

    // 🧃 Bebidas (Begudes)
    "agua" to "Bebidas", "aigua" to "Bebidas",
    "cerveza" to "Bebidas", "cervesa" to "Bebidas", "cervezas" to "Bebidas", "cerveses" to "Bebidas",
    "vino" to "Bebidas", "vi" to "Bebidas",
    "zumo" to "Bebidas", "suc" to "Bebidas",
    "refresco" to "Bebidas", "refresc" to "Bebidas", "coca-cola" to "Bebidas", "fanta" to "Bebidas",
    "café" to "Bebidas", "cafè" to "Bebidas", "cápsulas de café" to "Bebidas", "càpsules de cafè" to "Bebidas",
    "té" to "Bebidas", "infusión" to "Bebidas", "infusió" to "Bebidas",
    "leche de avena" to "Bebidas", "llet de civada" to "Bebidas", "leche de soja" to "Bebidas", "llet de soja" to "Bebidas",
    "licor" to "Bebidas", "cava" to "Bebidas",

    // 🧼 Limpieza y Hogar (Neteja)
    "jabón" to "Limpieza", "sabó" to "Limpieza",
    "lejía" to "Limpieza", "lleixiu" to "Limpieza",
    "detergente" to "Limpieza", "detergent" to "Limpieza",
    "suavizante" to "Limpieza", "suavitzant" to "Limpieza",
    "escoba" to "Limpieza", "escombra" to "Limpieza",
    "fregona" to "Limpieza",
    "bayeta" to "Limpieza", "baeta" to "Limpieza",
    "estropajo" to "Limpieza", "fregall" to "Limpieza",
    "limpiacristales" to "Limpieza", "netejavidres" to "Limpieza",
    "bolsas de basura" to "Limpieza", "bosses de brossa" to "Limpieza", "bosses d'escombraries" to "Limpieza",

    // 🧊 Congelados (Congelats)
    "helado" to "Congelados", "gelat" to "Congelados", "helados" to "Congelados", "gelats" to "Congelados",
    "guisantes" to "Congelados", "pèsols" to "Congelados",
    "croquetas" to "Congelados", "croquetes" to "Congelados",
    "pizza" to "Congelados", "pizzas" to "Congelados", "pizzes" to "Congelados",
    "hielo" to "Congelados", "gel" to "Congelados", "cubitos" to "Congelados", "glaçons" to "Congelados",
    "varitas de merluza" to "Congelados", "barretes de lluç" to "Congelados",

    // 🧴 Higiene Personal (Higiene Personal)
    "champú" to "Higiene", "xampú" to "Higiene",
    "gel de baño" to "Higiene", "gel de bany" to "Higiene",
    "pasta de dientes" to "Higiene", "pasta de dents" to "Higiene",
    "cepillo de dientes" to "Higiene", "raspall de dents" to "Higiene",
    "desodorante" to "Higiene", "desodorant" to "Higiene",
    "colonia" to "Higiene", "colònia" to "Higiene",
    "crema" to "Higiene",
    "espuma de afeitar" to "Higiene", "escuma d'afaitar" to "Higiene", "cuchillas" to "Higiene", "fulles d'afaitar" to "Higiene",
    "compresas" to "Higiene", "compreses" to "Higiene",
    "tampones" to "Higiene", "tampons" to "Higiene",
    "papel higiénico" to "Higiene", "paper higiènic" to "Higiene", "paper de wàter" to "Higiene",

    // 🐾 Mascotas (Mascotes)
    "comida para gatos" to "Mascotas", "menjar per a gats" to "Mascotas",
    "arena para gatos" to "Mascotas", "sorra per a gats" to "Mascotas",
    "pienso" to "Mascotas", "pinso" to "Mascotas",
    "comida para perros" to "Mascotas", "menjar per a gossos" to "Mascotas"
)

// 3. PANTALLA PRINCIPAL
@Composable
fun ShoppingScreen(modifier: Modifier = Modifier, userEmail: String, listId: String, listName: String) {
    val db = Firebase.firestore
    // Referències a la llista oberta: els productes i les categories són subcol·leccions
    val productesRef = db.collection("shopping_lists").document(listId).collection("productes")
    val categoriesRef = db.collection("shopping_lists").document(listId).collection("categories")
    val llistaProductes = remember { mutableStateListOf<Producte>() }
    val llistaCategories = remember { mutableStateListOf<Categoria>() }
    var nouProducteText by remember { mutableStateOf("") }

    // Diàlegs oberts: nova categoria, categoria que s'edita (pel nom) i producte que s'edita
    var novaCategoria by remember { mutableStateOf(false) }
    var categoriaEditant by remember { mutableStateOf<String?>(null) }
    var producteEditant by remember { mutableStateOf<Producte?>(null) }

    // DESCARGAR DATOS DE FIREBASE EN TIEMPO REAL
    // Els listeners s'aturen en sortir de la llista per no acumular-ne
    DisposableEffect(listId) {
        val registreProductes = productesRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                llistaProductes.clear()
                for (document in snapshot.documents) {
                    val producte = document.toObject(Producte::class.java)
                    if (producte != null) llistaProductes.add(producte)
                }
            }
        }
        val registreCategories = categoriesRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                llistaCategories.clear()
                for (document in snapshot.documents) {
                    val categoria = document.toObject(Categoria::class.java)
                    if (categoria != null) llistaCategories.add(categoria)
                }
            }
        }
        onDispose {
            registreProductes.remove()
            registreCategories.remove()
        }
    }

    // Noms de categoria visibles: les creades a mà (encara que estiguin buides) i les que ja usen productes
    val nomsCategories = (llistaCategories.map { it.nom } + llistaProductes.map { it.categoria })
        .filter { it.isNotBlank() }
        .distinct()
        .sortedWith(compareBy({ it == CATEGORIA_PER_DEFECTE }, { it.lowercase() }))

    // Canvia el nom d'una categoria i el dels productes que hi pertanyen
    fun reanomenarCategoria(antic: String, nou: String) {
        val lot = db.batch()
        val existent = llistaCategories.firstOrNull { it.nom == antic }
        if (existent != null) {
            lot.update(categoriesRef.document(existent.id), "nom", nou)
        } else {
            val creada = Categoria(nom = nou)
            lot.set(categoriesRef.document(creada.id), creada)
        }
        llistaProductes.filter { it.categoria == antic }.forEach {
            lot.update(productesRef.document(it.id), "categoria", nou)
        }
        lot.commit()
    }

    // Elimina la categoria; els seus productes passen a la categoria per defecte
    fun eliminarCategoria(nom: String) {
        val lot = db.batch()
        llistaCategories.filter { it.nom == nom }.forEach { lot.delete(categoriesRef.document(it.id)) }
        llistaProductes.filter { it.categoria == nom }.forEach {
            lot.update(productesRef.document(it.id), "categoria", CATEGORIA_PER_DEFECTE)
        }
        lot.commit()
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = listName,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nouProducteText,
                onValueChange = { nouProducteText = it },
                label = { Text("Añade productos") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (nouProducteText.isNotBlank()) {
                        val cat = autoCategories[nouProducteText.lowercase().trim()] ?: CATEGORIA_PER_DEFECTE
                        val nouProducte = Producte(nom = nouProducteText.trim(), categoria = cat)

                        // SUBIR NUEVO PRODUCTO A FIREBASE
                        productesRef.document(nouProducte.id).set(nouProducte)

                        nouProducteText = ""
                    }
                }
            ) {
                Text("Afegir")
            }
        }

        TextButton(onClick = { novaCategoria = true }) {
            Text("+ Categoria")
        }

        Spacer(modifier = Modifier.height(8.dp))


        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val productesAgrupats = llistaProductes.groupBy { it.categoria }

            nomsCategories.forEach { categoria ->
                val productes = productesAgrupats[categoria].orEmpty()
                item(key = "categoria_$categoria") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).animateItem(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = categoria,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        // La categoria per defecte no es pot editar ni eliminar
                        if (categoria != CATEGORIA_PER_DEFECTE) {
                            TextButton(onClick = { categoriaEditant = categoria }) {
                                Text("Edita")
                            }
                            IconButton(onClick = { eliminarCategoria(categoria) }) {
                                Text("X", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                items(productes, key = { it.id }) { producte ->
                    ProducteItem(
                        // Les files s'animen suaument en afegir-se, eliminar-se o canviar de lloc
                        modifier = Modifier.animateItem(),
                        producte = producte,
                        onCheckedChange = { isChecked ->
                            // ACTUALIZAR ESTADO DEL CHECKBOX EN FIREBASE
                            productesRef.document(producte.id).update("completat", isChecked)
                        },
                        onDelete = {
                            // BORRAR PRODUCTO DE FIREBASE
                            productesRef.document(producte.id).delete()
                        },
                        onEdit = { producteEditant = producte }
                    )
                }
            }
        }
    }

    if (novaCategoria) {
        NomDialog(
            titol = "Nova categoria",
            valorInicial = "",
            validacio = { nom ->
                when {
                    nom.isBlank() -> "El nom és obligatori"
                    nomsCategories.any { it.equals(nom, ignoreCase = true) } -> "Aquesta categoria ja existeix"
                    else -> null
                }
            },
            onConfirm = { nom ->
                val creada = Categoria(nom = nom)
                categoriesRef.document(creada.id).set(creada)
                novaCategoria = false
            },
            onDismiss = { novaCategoria = false }
        )
    }

    categoriaEditant?.let { antic ->
        NomDialog(
            titol = "Edita la categoria",
            valorInicial = antic,
            validacio = { nom ->
                when {
                    nom.isBlank() -> "El nom és obligatori"
                    // El mateix nom (encara que canviï les majúscules) no compta com a duplicat
                    !nom.equals(antic, ignoreCase = true) &&
                        nomsCategories.any { it.equals(nom, ignoreCase = true) } -> "Aquesta categoria ja existeix"
                    else -> null
                }
            },
            onConfirm = { nom ->
                if (nom != antic) reanomenarCategoria(antic, nom)
                categoriaEditant = null
            },
            onDismiss = { categoriaEditant = null }
        )
    }

    producteEditant?.let { producte ->
        ProducteDialog(
            producte = producte,
            categories = (nomsCategories + CATEGORIA_PER_DEFECTE).distinct(),
            onConfirm = { nom, categoria ->
                productesRef.document(producte.id).update(mapOf("nom" to nom, "categoria" to categoria))
                producteEditant = null
            },
            onDismiss = { producteEditant = null }
        )
    }
}

// Diàleg amb un camp de text de nom obligatori; validacio retorna el motiu de l'error o null si és vàlid
@Composable
private fun NomDialog(
    titol: String,
    valorInicial: String,
    validacio: (String) -> String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(valorInicial) }
    val nom = text.trim()
    val error = validacio(nom)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titol) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nom") },
                singleLine = true,
                // No es mostra l'error mentre el camp està buit
                supportingText = { if (error != null && nom.isNotEmpty()) Text(error) }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nom) }, enabled = error == null) { Text("Desa") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel·la") }
        }
    )
}

// Diàleg per editar el nom (obligatori) i la categoria d'un producte
@Composable
private fun ProducteDialog(
    producte: Producte,
    categories: List<String>,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(producte.nom) }
    var categoria by remember { mutableStateOf(producte.categoria) }
    var menuObert by remember { mutableStateOf(false) }
    val nom = text.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edita el producte") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Nom") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box {
                    OutlinedButton(onClick = { menuObert = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(categoria)
                    }
                    DropdownMenu(expanded = menuObert, onDismissRequest = { menuObert = false }) {
                        categories.forEach { opcio ->
                            DropdownMenuItem(
                                text = { Text(opcio) },
                                onClick = {
                                    categoria = opcio
                                    menuObert = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nom, categoria) }, enabled = nom.isNotEmpty()) { Text("Desa") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel·la") }
        }
    )
}

// 4. DISEÑO DE LA FILA DE PRODUCTO
@Composable
fun ProducteItem(
    producte: Producte,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    // El text d'un producte completat es va atenuant de manera progressiva
    val colorText by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.onSurface.copy(
            alpha = if (producte.completat) 0.5f else 1f
        ),
        label = "colorProducte"
    )

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = producte.completat, onCheckedChange = onCheckedChange)
            Text(
                text = producte.nom,
                // En tocar el nom s'obre l'edició del producte
                modifier = Modifier.weight(1f).clickable(onClick = onEdit),
                color = colorText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (producte.completat) TextDecoration.LineThrough else TextDecoration.None,

                )
            )
            IconButton(onClick = onDelete) {
                Text("X", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}