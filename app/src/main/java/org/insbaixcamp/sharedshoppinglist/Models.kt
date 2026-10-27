package org.insbaixcamp.sharedshoppinglist

import com.google.firebase.firestore.DocumentId

// Data classes per al model de dades de Firestore

data class ShoppingList(
    @DocumentId val id: String = "",
    val title: String = "",
    val ownerId: String = "",
    val members: List<String> = emptyList()
)

data class Category(
    @DocumentId val id: String = "",
    val name: String = ""
)

data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val categoryId: String = "",
    val isCompleted: Boolean = false,
    val tags: List<String> = emptyList()
)
