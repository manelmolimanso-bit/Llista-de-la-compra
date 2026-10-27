package org.insbaixcamp.sharedshoppinglist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ListsScreen(
    lists: List<ShoppingList>,
    currentUserId: String,
    onCreateList: (title: String) -> Unit,
    onShareList: (listId: String, memberUid: String) -> Unit,
    onDeleteList: (listId: String) -> Unit,
    onSelectList: (ShoppingList) -> Unit,
    onSignOut: () -> Unit
) {
    var newTitle by remember { mutableStateOf("") }
    var shareInputMap by remember { mutableStateOf(mapOf<String, String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Capçalera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Les Meves Llistes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(onClick = onSignOut) {
                Text("Tancar sessió")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Formulari per crear nova llista
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Crear Nova Llista", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Títol de la llista") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank()) {
                                onCreateList(newTitle.trim())
                                newTitle = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("Crear")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Llista de llistes
        if (lists.isEmpty()) {
            Text("No tens cap llista compartida o creada encara.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(lists, key = { it.id }) { list ->
                    val shareInput = shareInputMap[list.id] ?: ""

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectList(list) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = list.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedButton(onClick = { onDeleteList(list.id) }) {
                                    Text("Eliminar")
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Membres (${list.members.size})",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Secció per compartir
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = shareInput,
                                    onValueChange = { shareInputMap = shareInputMap + (list.id to it) },
                                    label = { Text("Afegir membre (UID/Email)") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (shareInput.isNotBlank()) {
                                            onShareList(list.id, shareInput.trim())
                                            shareInputMap = shareInputMap + (list.id to "")
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Text("Compartir")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
