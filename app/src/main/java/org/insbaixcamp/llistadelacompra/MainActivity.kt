package org.insbaixcamp.llistadelacompra

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
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
    // El correu en minúscules és l'identificador de l'usuari dins dels membres d'una llista
    val userEmail = (user.email ?: "").lowercase()

    // Llista oberta actualment; si és null es mostra el llistat de llistes de l'usuari
    var llistaOberta by remember { mutableStateOf<Llista?>(null) }

    // El botó enrere del sistema torna al llistat de llistes
    BackHandler(enabled = llistaOberta != null) { llistaOberta = null }

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

            // Només amb una llista oberta: torna al llistat de llistes
            if (llistaOberta != null) {
                TextButton(onClick = { llistaOberta = null }) {
                    Text(text = "Llistes")
                }
            }

            // Botón para cerrar sesión (signOut + clearCredentialState)
            TextButton(onClick = { onSignOut(context) }) {
                Text(text = "Tancar Sessió")
            }
        }

        val llista = llistaOberta
        if (llista == null) {
            ListsScreen(
                modifier = Modifier.weight(1f),
                userEmail = userEmail,
                onOpen = { llistaOberta = it }
            )
        } else {
            ShoppingScreen(
                modifier = Modifier.weight(1f),
                userEmail = userEmail,
                listId = llista.id,
                listName = llista.nom
            )
        }
    }
}

// Llista de compra: el propietari és qui l'ha creada i els membres (inclòs el propietari) hi poden editar
data class Llista(
    val id: String = "",
    val nom: String = "",
    val propietari: String = "",
    val membres: List<String> = emptyList()
)

// Llistat de les llistes de l'usuari (pròpies i compartides), amb opció de crear, compartir i esborrar
@Composable
fun ListsScreen(
    modifier: Modifier = Modifier,
    userEmail: String,
    onOpen: (Llista) -> Unit
) {
    val db = Firebase.firestore
    val llistes = remember { mutableStateListOf<Llista>() }
    var error by remember { mutableStateOf<String?>(null) }
    var nomNovaLlista by remember { mutableStateOf("") }
    var llistaPerCompartir by remember { mutableStateOf<Llista?>(null) }
    var llistaPerEsborrar by remember { mutableStateOf<Llista?>(null) }

    // Escolta en temps real les llistes on l'usuari figura com a membre
    DisposableEffect(userEmail) {
        val registre = db.collection("shopping_lists")
            .whereArrayContains("membres", userEmail)
            .addSnapshotListener { snapshot, e ->
                error = e?.localizedMessage
                if (snapshot != null) {
                    llistes.clear()
                    snapshot.documents.mapNotNullTo(llistes) { d ->
                        d.toObject(Llista::class.java)?.copy(id = d.id)
                    }
                }
            }
        onDispose { registre.remove() }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "LES MEVES LLISTES", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nomNovaLlista,
                onValueChange = { nomNovaLlista = it },
                label = { Text("Nom de la nova llista") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    // El nom és obligatori
                    if (nomNovaLlista.isNotBlank()) {
                        db.collection("shopping_lists").add(
                            hashMapOf(
                                "nom" to nomNovaLlista.trim(),
                                "propietari" to userEmail,
                                "membres" to listOf(userEmail)
                            )
                        )
                        nomNovaLlista = ""
                    }
                }
            ) {
                Text("Crea")
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (llistes.isEmpty()) {
            Text(
                text = "Encara no tens cap llista",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(llistes.sortedBy { it.nom.lowercase() }, key = { it.id }) { llista ->
                val esPropietari = llista.propietari == userEmail
                Card(modifier = Modifier.fillMaxWidth().animateItem()) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f).clickable { onOpen(llista) }
                        ) {
                            Text(text = llista.nom, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (esPropietari) "Pròpia" else "Compartida per ${llista.propietari}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Qualsevol membre pot compartir la llista amb altres persones
                        TextButton(onClick = { llistaPerCompartir = llista }) {
                            Text("Comparteix")
                        }
                        // Només el propietari pot eliminar la llista
                        if (esPropietari) {
                            IconButton(onClick = { llistaPerEsborrar = llista }) {
                                Text("X", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    llistaPerCompartir?.let { llista ->
        CompartirDialog(
            llista = llista,
            onConfirm = { correu ->
                // arrayUnion evita duplicats si el correu ja és membre
                db.collection("shopping_lists").document(llista.id)
                    .update("membres", FieldValue.arrayUnion(correu))
                llistaPerCompartir = null
            },
            onDismiss = { llistaPerCompartir = null }
        )
    }

    llistaPerEsborrar?.let { llista ->
        AlertDialog(
            onDismissRequest = { llistaPerEsborrar = null },
            title = { Text("Eliminar llista") },
            text = { Text("S'eliminarà «${llista.nom}» amb tots els seus productes i categories per a tots els membres.") },
            confirmButton = {
                TextButton(onClick = {
                    esborrarLlista(llista)
                    llistaPerEsborrar = null
                }) { Text("Elimina", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { llistaPerEsborrar = null }) { Text("Cancel·la") }
            }
        )
    }
}

// Diàleg per afegir un membre a la llista a partir del seu correu
@Composable
private fun CompartirDialog(
    llista: Llista,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var correu by remember { mutableStateOf("") }
    val correuNet = correu.trim().lowercase()
    val valid = correuNet.contains("@") && correuNet !in llista.membres

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comparteix «${llista.nom}»") },
        text = {
            Column {
                OutlinedTextField(
                    value = correu,
                    onValueChange = { correu = it },
                    label = { Text("Correu de la persona") },
                    singleLine = true,
                    supportingText = {
                        if (correuNet in llista.membres) Text("Ja té accés a la llista")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Amb accés: " + llista.membres.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(correuNet) }, enabled = valid) { Text("Comparteix") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel·la") }
        }
    )
}

// Firestore no elimina les subcol·leccions en esborrar el document: s'esborren totes en un sol lot
private fun esborrarLlista(llista: Llista) {
    val db = Firebase.firestore
    val ref = db.collection("shopping_lists").document(llista.id)
    ref.collection("productes").get().addOnSuccessListener { productes ->
        ref.collection("categories").get().addOnSuccessListener { categories ->
            val lot = db.batch()
            (productes.documents + categories.documents).forEach { lot.delete(it.reference) }
            lot.delete(ref)
            lot.commit()
        }
    }
}
