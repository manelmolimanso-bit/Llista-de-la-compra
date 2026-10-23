package org.insbaixcamp.llistadelacompra.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.insbaixcamp.llistadelacompra.data.model.Category
import org.insbaixcamp.llistadelacompra.data.model.Product
import org.insbaixcamp.llistadelacompra.data.model.ShoppingList

class ShoppingListRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val listsCollection = firestore.collection("shopping_lists")

    /**
     * Retrieves all lists where user is owner or listed in sharedWithEmails using a real-time listener.
     */
    fun getListsForUser(userUid: String, userEmail: String): Flow<List<ShoppingList>> = callbackFlow {
        // Real-time snapshot listener on shopping_lists collection
        val listener = listsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val lists = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ShoppingList::class.java)?.copy(id = doc.id)
                }.filter { list ->
                    list.ownerId == userUid || (userEmail.isNotEmpty() && list.isSharedWith(userEmail))
                }.sortedByDescending { it.createdAt }

                trySend(lists)
            }
        }
        awaitClose { listener.remove() }
    }

    /**
     * Creates a new shopping list and prepopulates default categories.
     */
    suspend fun createList(name: String, user: FirebaseUser): Result<String> {
        return try {
            val email = user.email ?: ""
            val newList = ShoppingList(
                name = name,
                ownerId = user.uid,
                ownerEmail = email,
                sharedWithEmails = emptyList(),
                createdAt = System.currentTimeMillis()
            )
            val docRef = listsCollection.add(newList).await()
            val listId = docRef.id

            // Prepopulate default categories in sub-collection
            val categoriesSubCollection = listsCollection.document(listId).collection("categories")
            for (category in AutoCategorizer.DEFAULT_CATEGORIES) {
                categoriesSubCollection.document(category.id).set(category).await()
            }

            Result.success(listId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateListName(listId: String, newName: String): Result<Unit> {
        return try {
            listsCollection.document(listId).update("name", newName).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun shareListWithEmail(listId: String, emailToShare: String): Result<Unit> {
        return try {
            val cleanEmail = emailToShare.trim().lowercase()
            listsCollection.document(listId)
                .update("sharedWithEmails", FieldValue.arrayUnion(cleanEmail))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteList(listId: String): Result<Unit> {
        return try {
            listsCollection.document(listId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Categories ---

    fun getCategories(listId: String): Flow<List<Category>> = callbackFlow {
        if (listId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val categoriesSubCollection = listsCollection.document(listId).collection("categories")
        val listener = categoriesSubCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val categories = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                }
                trySend(categories)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun addCategory(listId: String, name: String, colorHex: String, iconName: String): Result<String> {
        return try {
            val newCategory = Category(name = name, colorHex = colorHex, iconName = iconName)
            val docRef = listsCollection.document(listId).collection("categories").add(newCategory).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategory(listId: String, category: Category): Result<Unit> {
        return try {
            listsCollection.document(listId).collection("categories")
                .document(category.id)
                .set(category)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(listId: String, categoryId: String): Result<Unit> {
        return try {
            listsCollection.document(listId).collection("categories").document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Products ---

    fun getProducts(listId: String): Flow<List<Product>> = callbackFlow {
        if (listId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val productsSubCollection = listsCollection.document(listId).collection("products")
        val listener = productsSubCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val products = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                }.sortedByDescending { it.createdAt }
                trySend(products)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun addProduct(listId: String, product: Product): Result<String> {
        return try {
            val newProd = product.copy(createdAt = System.currentTimeMillis())
            val docRef = listsCollection.document(listId).collection("products").add(newProd).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(listId: String, product: Product): Result<Unit> {
        return try {
            listsCollection.document(listId).collection("products")
                .document(product.id)
                .set(product)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleProductCompleted(listId: String, productId: String, currentCompleted: Boolean): Result<Unit> {
        return try {
            listsCollection.document(listId).collection("products")
                .document(productId)
                .update("completed", !currentCompleted)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Fallback try setting isCompleted if completed field name differs
            try {
                listsCollection.document(listId).collection("products")
                    .document(productId)
                    .update("isCompleted", !currentCompleted)
                    .await()
                Result.success(Unit)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    suspend fun deleteProduct(listId: String, productId: String): Result<Unit> {
        return try {
            listsCollection.document(listId).collection("products").document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
