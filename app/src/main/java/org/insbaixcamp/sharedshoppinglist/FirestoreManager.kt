package org.insbaixcamp.sharedshoppinglist

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreManager(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val listsRef = db.collection("lists")

    // Retorna totes les llistes on l'usuari actual apareix al camp `members`
    fun getListsForUser(userUid: String): Flow<List<ShoppingList>> = callbackFlow {
        if (userUid.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = listsRef.whereArrayContains("members", userUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lists = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ShoppingList::class.java)?.copy(id = doc.id)
                    }
                    trySend(lists)
                }
            }
        awaitClose { listener.remove() }
    }

    // Crear nova llista amb l'usuari com a propietari i primer membre
    suspend fun createList(title: String, ownerId: String): Result<String> {
        if (title.isBlank()) return Result.failure(Exception("El títol no pot estar buit"))
        return try {
            val newList = hashMapOf(
                "title" to title.trim(),
                "ownerId" to ownerId,
                "members" to listOf(ownerId)
            )
            val docRef = listsRef.add(newList).await()
            val listId = docRef.id

            // Afegir categoria inicial "General"
            docRef.collection("categories").add(hashMapOf("name" to "General")).await()

            Result.success(listId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Compartir llista afegint el UID/Email d'un altre usuari a `members`
    suspend fun shareList(listId: String, memberUidOrEmail: String): Result<Unit> {
        if (memberUidOrEmail.isBlank()) return Result.failure(Exception("El membre no pot estar buit"))
        return try {
            listsRef.document(listId)
                .update("members", FieldValue.arrayUnion(memberUidOrEmail.trim()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar llista
    suspend fun deleteList(listId: String): Result<Unit> {
        return try {
            listsRef.document(listId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Subcol·lecció Categories ---

    fun getCategories(listId: String): Flow<List<Category>> = callbackFlow {
        if (listId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val categoriesRef = listsRef.document(listId).collection("categories")
        val listener = categoriesRef.addSnapshotListener { snapshot, error ->
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

    suspend fun addCategory(listId: String, name: String): Result<String> {
        if (name.isBlank()) return Result.failure(Exception("El nom de la categoria és obligatori"))
        return try {
            val docRef = listsRef.document(listId).collection("categories")
                .add(hashMapOf("name" to name.trim())).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(listId: String, categoryId: String): Result<Unit> {
        return try {
            listsRef.document(listId).collection("categories").document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Subcol·lecció Productes ---

    fun getProducts(listId: String): Flow<List<Product>> = callbackFlow {
        if (listId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val productsRef = listsRef.document(listId).collection("products")
        val listener = productsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val products = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                }
                trySend(products)
            }
        }
        awaitClose { listener.remove() }
    }

    // Afegir producte amb auto-classificació de categoria si cal
    suspend fun addProduct(
        listId: String,
        name: String,
        categoryId: String,
        tags: List<String>,
        existingCategories: List<Category>
    ): Result<String> {
        if (name.isBlank()) return Result.failure(Exception("El nom del producte és obligatori"))

        return try {
            // Auto-classificació bàsica segons el nom del producte
            var targetCategoryId = categoryId
            if (targetCategoryId.isBlank()) {
                val autoCatName = detectAutoCategory(name)
                val matched = existingCategories.firstOrNull { it.name.equals(autoCatName, ignoreCase = true) }
                if (matched != null) {
                    targetCategoryId = matched.id
                } else {
                    // Si no existeix la categoria trobada, la crea automàticament
                    val newCatRes = addCategory(listId, autoCatName)
                    targetCategoryId = newCatRes.getOrDefault("")
                }
            }

            val productData = hashMapOf(
                "name" to name.trim(),
                "categoryId" to targetCategoryId,
                "isCompleted" to false,
                "tags" to tags.map { it.trim() }.filter { it.isNotEmpty() }
            )

            val docRef = listsRef.document(listId).collection("products").add(productData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Canviar l'estat isCompleted sense esborrar el producte
    suspend fun toggleProductCompleted(listId: String, productId: String, currentStatus: Boolean): Result<Unit> {
        return try {
            listsRef.document(listId).collection("products")
                .document(productId)
                .update("isCompleted", !currentStatus)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(listId: String, productId: String): Result<Unit> {
        return try {
            listsRef.document(listId).collection("products").document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Auto-classificació bàsica per a la característica extra
    private fun detectAutoCategory(productName: String): String {
        val lower = productName.lowercase()
        return when {
            lower.contains("llet") || lower.contains("formatge") || lower.contains("iogurt") || lower.contains("leche") -> "Làctics"
            lower.contains("poma") || lower.contains("plàtan") || lower.contains("taronja") || lower.contains("patata") -> "Fruiteria"
            lower.contains("pa") || lower.contains("croissant") || lower.contains("galetes") -> "Panaderia"
            lower.contains("pollastre") || lower.contains("vedella") || lower.contains("hamburguesa") -> "Carnisseria"
            lower.contains("aigua") || lower.contains("suc") || lower.contains("cervesa") -> "Begudes"
            lower.contains("sabó") || lower.contains("detergent") || lower.contains("paper") -> "Neteja"
            else -> "General"
        }
    }
}
