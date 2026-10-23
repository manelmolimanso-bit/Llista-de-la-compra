package org.insbaixcamp.llistadelacompra.data.model

import com.google.firebase.firestore.DocumentId

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = ""
)

data class ShoppingList(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val ownerEmail: String = "",
    val sharedWithEmails: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isOwner(uid: String): Boolean = ownerId == uid
    fun isSharedWith(email: String): Boolean = sharedWithEmails.any { it.equals(email, ignoreCase = true) }
}

data class Category(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val colorHex: String = "#4CAF50",
    val iconName: String = "category"
)

data class Product(
    @DocumentId
    val id: String = "",
    val categoryId: String = "",
    val name: String = "",
    val isCompleted: Boolean = false,
    val quantity: String = "1",
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class PredefinedProduct(
    val name: String,
    val categoryName: String,
    val defaultTag: String? = null
)
