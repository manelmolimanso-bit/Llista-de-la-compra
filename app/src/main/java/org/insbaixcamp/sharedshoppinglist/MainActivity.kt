package org.insbaixcamp.sharedshoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authManager by lazy { AuthManager() }
    private val firestoreManager by lazy { FirestoreManager() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SharedShoppingListApp(
                authManager = authManager,
                firestoreManager = firestoreManager
            )
        }
    }
}

@Composable
fun SharedShoppingListApp(
    authManager: AuthManager,
    firestoreManager: FirestoreManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf<FirebaseUser?>(authManager.currentUser) }
    var activeList by remember { mutableStateOf<ShoppingList?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Recompte de llistes en temps real per a l'usuari actual
    val userListsFlow = remember(currentUser) {
        if (currentUser != null) {
            firestoreManager.getListsForUser(currentUser!!.uid)
        } else {
            flowOf(emptyList())
        }
    }
    val userLists by userListsFlow.collectAsState(initial = emptyList())

    // Recompte de categories i productes en temps real per a la llista activa
    val categoriesFlow = remember(activeList) {
        if (activeList != null) {
            firestoreManager.getCategories(activeList!!.id)
        } else {
            flowOf(emptyList())
        }
    }
    val categories by categoriesFlow.collectAsState(initial = emptyList())

    val productsFlow = remember(activeList) {
        if (activeList != null) {
            firestoreManager.getProducts(activeList!!.id)
        } else {
            flowOf(emptyList())
        }
    }
    val products by productsFlow.collectAsState(initial = emptyList())

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val user = currentUser
            if (user == null) {
                LoginScreen(
                    authManager = authManager,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onLoginSuccess = {
                        currentUser = authManager.currentUser
                    }
                )
            } else {
                val list = activeList
                if (list != null) {
                    BackHandler {
                        activeList = null
                    }
                    DetailScreen(
                        list = list,
                        categories = categories,
                        products = products,
                        onBack = { activeList = null },
                        onAddCategory = { name ->
                            scope.launch {
                                firestoreManager.addCategory(list.id, name)
                                    .onFailure { snackbarHostState.showSnackbar("Error: ${it.message}") }
                            }
                        },
                        onDeleteCategory = { categoryId ->
                            scope.launch {
                                firestoreManager.deleteCategory(list.id, categoryId)
                            }
                        },
                        onAddProduct = { name, categoryId, tags ->
                            scope.launch {
                                firestoreManager.addProduct(list.id, name, categoryId, tags, categories)
                                    .onFailure { snackbarHostState.showSnackbar("Error: ${it.message}") }
                            }
                        },
                        onToggleProduct = { productId, currentStatus ->
                            scope.launch {
                                firestoreManager.toggleProductCompleted(list.id, productId, currentStatus)
                            }
                        },
                        onDeleteProduct = { productId ->
                            scope.launch {
                                firestoreManager.deleteProduct(list.id, productId)
                            }
                        }
                    )
                } else {
                    ListsScreen(
                        lists = userLists,
                        currentUserId = user.uid,
                        onCreateList = { title ->
                            scope.launch {
                                firestoreManager.createList(title, user.uid)
                                    .onFailure { snackbarHostState.showSnackbar("Error: ${it.message}") }
                            }
                        },
                        onShareList = { listId, memberUid ->
                            scope.launch {
                                firestoreManager.shareList(listId, memberUid)
                                    .onSuccess { snackbarHostState.showSnackbar("Llista compartida amb èxit!") }
                                    .onFailure { snackbarHostState.showSnackbar("Error al compartir: ${it.message}") }
                            }
                        },
                        onDeleteList = { listId ->
                            scope.launch {
                                firestoreManager.deleteList(listId)
                            }
                        },
                        onSelectList = { selected ->
                            activeList = selected
                        },
                        onSignOut = {
                            scope.launch {
                                authManager.signOut(context)
                                currentUser = null
                                activeList = null
                            }
                        }
                    )
                }
            }
        }
    }
}
