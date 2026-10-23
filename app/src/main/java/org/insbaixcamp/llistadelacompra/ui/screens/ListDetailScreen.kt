package org.insbaixcamp.llistadelacompra.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.insbaixcamp.llistadelacompra.data.model.Category
import org.insbaixcamp.llistadelacompra.data.model.Product
import org.insbaixcamp.llistadelacompra.data.model.ShoppingList
import org.insbaixcamp.llistadelacompra.ui.components.AddCategoryDialog
import org.insbaixcamp.llistadelacompra.ui.components.AddProductDialog
import org.insbaixcamp.llistadelacompra.ui.components.ProductItemCard
import org.insbaixcamp.llistadelacompra.ui.components.ShareListDialog
import org.insbaixcamp.llistadelacompra.ui.viewmodel.ShoppingListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListDetailScreen(
    viewModel: ShoppingListViewModel,
    activeList: ShoppingList,
    onBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var isCompletedSectionExpanded by remember { mutableStateOf(true) }

    // Collect all unique tags across products
    val availableTags = remember(products) {
        products.flatMap { it.tags }.toSet().sorted()
    }

    // Filter products by search query and tag
    val filteredProducts = remember(products, searchQuery, selectedTag) {
        products.filter { prod ->
            val matchesSearch = searchQuery.isBlank() || prod.name.contains(searchQuery, ignoreCase = true) || prod.notes.contains(searchQuery, ignoreCase = true)
            val matchesTag = selectedTag == null || prod.tags.contains(selectedTag)
            matchesSearch && matchesTag
        }
    }

    val pendingProducts = filteredProducts.filter { !it.isCompleted }
    val completedProducts = filteredProducts.filter { it.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(activeList.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Tornar")
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                        Icon(Icons.Default.Search, contentDescription = "Cercar")
                    }
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Category, contentDescription = "Afegir Categoria")
                    }
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProductDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nou Producte", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Animated Search & Tag Filter Bar
            AnimatedVisibility(visible = isSearchVisible || searchQuery.isNotBlank() || selectedTag != null) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        label = { Text("Cercar producte o nota...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (availableTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Filtrar per etiqueta:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = selectedTag == null,
                                onClick = { viewModel.setSelectedTag(null) },
                                label = { Text("Totes") }
                            )
                            availableTags.forEach { tag ->
                                FilterChip(
                                    selected = selectedTag == tag,
                                    onClick = {
                                        viewModel.setSelectedTag(if (selectedTag == tag) null else tag)
                                    },
                                    label = { Text(tag) }
                                )
                            }
                        }
                    }
                }
            }

            // Empty state check
            if (filteredProducts.isEmpty() && categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aquesta llista està buida. Prem 'Nou Producte' per començar!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Render Products Grouped by Categories
                    categories.forEach { category ->
                        val categoryProducts = pendingProducts.filter { it.categoryId == category.id }
                        item(key = "cat_header_${category.id}") {
                            CategoryHeader(
                                category = category,
                                count = categoryProducts.size,
                                onDeleteCategory = { viewModel.deleteCategory(category.id) }
                            )
                        }

                        if (categoryProducts.isEmpty()) {
                            item(key = "cat_empty_${category.id}") {
                                Text(
                                    text = "Sense productes pendents en aquesta categoria",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 8.dp)
                                )
                            }
                        } else {
                            items(categoryProducts, key = { it.id }) { product ->
                                ProductItemCard(
                                    product = product,
                                    onToggleCompleted = { viewModel.toggleProductCompleted(product) },
                                    onDelete = { viewModel.deleteProduct(product.id) }
                                )
                            }
                        }
                    }

                    // Products without category (fallback)
                    val uncategorizedProducts = pendingProducts.filter { prod ->
                        categories.none { cat -> cat.id == prod.categoryId }
                    }
                    if (uncategorizedProducts.isNotEmpty()) {
                        item(key = "cat_header_uncategorized") {
                            Text(
                                text = "Sense categoria",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(uncategorizedProducts, key = { it.id }) { product ->
                            ProductItemCard(
                                product = product,
                                onToggleCompleted = { viewModel.toggleProductCompleted(product) },
                                onDelete = { viewModel.deleteProduct(product.id) }
                            )
                        }
                    }

                    // Completed Products Expandable Section
                    if (completedProducts.isNotEmpty()) {
                        item(key = "completed_header") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isCompletedSectionExpanded = !isCompletedSectionExpanded },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Completats (${completedProducts.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isCompletedSectionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null
                                    )
                                }
                            }
                        }

                        if (isCompletedSectionExpanded) {
                            items(completedProducts, key = { "completed_${it.id}" }) { product ->
                                ProductItemCard(
                                    product = product,
                                    onToggleCompleted = { viewModel.toggleProductCompleted(product) },
                                    onDelete = { viewModel.deleteProduct(product.id) }
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Dialogs
    if (showAddProductDialog) {
        AddProductDialog(
            categories = categories,
            onDismiss = { showAddProductDialog = false },
            onConfirm = { name, categoryId, quantity, tags, notes ->
                viewModel.addProduct(name, categoryId, quantity, tags, notes)
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, colorHex ->
                viewModel.addCategory(name, colorHex)
            }
        )
    }

    if (showShareDialog) {
        ShareListDialog(
            listName = activeList.name,
            onDismiss = { showShareDialog = false },
            onShare = { email ->
                viewModel.shareListWithEmail(activeList.id, email)
            }
        )
    }
}

@Composable
fun CategoryHeader(
    category: Category,
    count: Int,
    onDeleteCategory: () -> Unit
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(categoryColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onDeleteCategory,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar Categoria",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
