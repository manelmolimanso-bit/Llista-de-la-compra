package org.insbaixcamp.sharedshoppinglist

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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun DetailScreen(
    list: ShoppingList,
    categories: List<Category>,
    products: List<Product>,
    onBack: () -> Unit,
    onAddCategory: (name: String) -> Unit,
    onDeleteCategory: (categoryId: String) -> Unit,
    onAddProduct: (name: String, categoryId: String, tags: List<String>) -> Unit,
    onToggleProduct: (productId: String, currentStatus: Boolean) -> Unit,
    onDeleteProduct: (productId: String) -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    var newProductName by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var newProductTags by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Capçalera amb botó de tornar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Tornar")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = list.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (validationError.isNotBlank()) {
            Text(
                text = validationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Formulari per afegir Categoria (Validació: nom obligatori)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Afegir Categoria", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = {
                            newCategoryName = it
                            validationError = ""
                        },
                        label = { Text("Nom de la Categoria *") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newCategoryName.isBlank()) {
                                validationError = "El nom de la categoria no pot estar buit!"
                            } else {
                                onAddCategory(newCategoryName.trim())
                                newCategoryName = ""
                                validationError = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("Afegir")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Formulari per afegir Producte (Validació: nom obligatori + Auto-classificació)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Afegir Producte (Auto-classificació de Categoria i Etiquetes)", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = newProductName,
                    onValueChange = {
                        newProductName = it
                        validationError = ""
                    },
                    label = { Text("Nom del Producte * (ex: llet, poma, pa)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newProductTags,
                    onValueChange = { newProductTags = it },
                    label = { Text("Etiquetes (ex: Bonpreu, Oferta, Urgent)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (newProductName.isBlank()) {
                            validationError = "El nom del producte no pot estar buit!"
                        } else {
                            val tagsList = newProductTags.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }

                            onAddProduct(newProductName.trim(), selectedCategoryId, tagsList)
                            newProductName = ""
                            newProductTags = ""
                            validationError = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Afegir Producte")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Llista de Categories i els seus Productes
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories, key = { it.id }) { category ->
                val categoryProducts = products.filter { it.categoryId == category.id }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📁 ${category.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedButton(onClick = { onDeleteCategory(category.id) }) {
                                Text("Esborrar Categoria")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (categoryProducts.isEmpty()) {
                            Text("Sense productes", style = MaterialTheme.typography.bodySmall)
                        } else {
                            categoryProducts.forEach { product ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = product.isCompleted,
                                            onCheckedChange = { onToggleProduct(product.id, product.isCompleted) }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = product.name,
                                                fontWeight = if (product.isCompleted) FontWeight.Normal else FontWeight.Bold,
                                                textDecoration = if (product.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                            )
                                            if (product.tags.isNotEmpty()) {
                                                Text(
                                                    text = "Tags: ${product.tags.joinToString(", ")}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        OutlinedButton(onClick = { onDeleteProduct(product.id) }) {
                                            Text("X")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
