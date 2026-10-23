package org.insbaixcamp.llistadelacompra.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.insbaixcamp.llistadelacompra.data.model.Category
import org.insbaixcamp.llistadelacompra.data.model.Product
import org.insbaixcamp.llistadelacompra.data.model.ShoppingList
import org.insbaixcamp.llistadelacompra.data.repository.AutoCategorizer
import org.insbaixcamp.llistadelacompra.data.repository.ShoppingListRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListViewModel(
    private val repository: ShoppingListRepository = ShoppingListRepository()
) : ViewModel() {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)

    // User's available shopping lists (owned + shared)
    val userLists: StateFlow<List<ShoppingList>> = _currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(emptyList())
        } else {
            repository.getListsForUser(user.uid, user.email ?: "")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Currently selected list
    private val _activeList = MutableStateFlow<ShoppingList?>(null)
    val activeList: StateFlow<ShoppingList?> = _activeList.asStateFlow()

    // Categories in current list
    val categories: StateFlow<List<Category>> = _activeList.flatMapLatest { list ->
        if (list == null) flowOf(emptyList()) else repository.getCategories(list.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Products in current list
    val products: StateFlow<List<Product>> = _activeList.flatMapLatest { list ->
        if (list == null) flowOf(emptyList()) else repository.getProducts(list.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI state for search and tags
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setUser(user: FirebaseUser?) {
        _currentUser.value = user
    }

    fun selectList(list: ShoppingList?) {
        _activeList.value = list
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun clearMessages() {
        _infoMessage.value = null
        _errorMessage.value = null
    }

    // --- List Operations ---

    fun createList(name: String) {
        val user = _currentUser.value ?: run {
            _errorMessage.value = "Has d'estar connectat per crear una llista"
            return
        }
        if (name.isBlank()) {
            _errorMessage.value = "El nom de la llista no pot estar buit"
            return
        }
        viewModelScope.launch {
            repository.createList(name.trim(), user)
                .onSuccess { listId ->
                    _infoMessage.value = "Llista '$name' creada correctament"
                }
                .onFailure { e ->
                    _errorMessage.value = "Error al crear llista: ${e.localizedMessage}"
                }
        }
    }

    fun shareListWithEmail(listId: String, email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "L'email no pot estar buit"
            return
        }
        viewModelScope.launch {
            repository.shareListWithEmail(listId, email.trim())
                .onSuccess {
                    _infoMessage.value = "Llista compartida amb $email!"
                }
                .onFailure { e ->
                    _errorMessage.value = "Error al compartir: ${e.localizedMessage}"
                }
        }
    }

    fun deleteList(listId: String) {
        viewModelScope.launch {
            repository.deleteList(listId)
                .onSuccess {
                    if (_activeList.value?.id == listId) {
                        _activeList.value = null
                    }
                    _infoMessage.value = "Llista eliminada"
                }
                .onFailure { e ->
                    _errorMessage.value = "Error al eliminar llista: ${e.localizedMessage}"
                }
        }
    }

    // --- Category Operations ---

    fun addCategory(name: String, colorHex: String = "#4CAF50", iconName: String = "category") {
        val currentList = _activeList.value ?: run {
            _errorMessage.value = "Cap llista seleccionada"
            return
        }
        if (name.isBlank()) {
            _errorMessage.value = "El nom de la categoria no pot estar buit"
            return
        }
        viewModelScope.launch {
            repository.addCategory(currentList.id, name.trim(), colorHex, iconName)
                .onSuccess {
                    _infoMessage.value = "Categoria afegida!"
                }
                .onFailure { e ->
                    _errorMessage.value = "Error en afegir categoria: ${e.localizedMessage}"
                }
        }
    }

    fun deleteCategory(categoryId: String) {
        val currentList = _activeList.value ?: return
        viewModelScope.launch {
            repository.deleteCategory(currentList.id, categoryId)
                .onSuccess {
                    _infoMessage.value = "Categoria eliminada"
                }
                .onFailure { e ->
                    _errorMessage.value = "Error en eliminar categoria: ${e.localizedMessage}"
                }
        }
    }

    // --- Product Operations & Auto Categorization ---

    fun suggestCategoryForName(productName: String): Category? {
        return AutoCategorizer.findSuggestedCategory(productName, categories.value)
    }

    fun addProduct(
        name: String,
        categoryId: String,
        quantity: String = "1",
        tags: List<String> = emptyList(),
        notes: String = ""
    ) {
        val currentList = _activeList.value ?: run {
            _errorMessage.value = "Cap llista seleccionada"
            return
        }
        if (name.isBlank()) {
            _errorMessage.value = "El nom del producte no pot estar buit"
            return
        }

        // Default to "Altres" or first available category if categoryId is empty
        val finalCategoryId = categoryId.ifEmpty {
            categories.value.firstOrNull()?.id ?: ""
        }

        val newProduct = Product(
            name = name.trim(),
            categoryId = finalCategoryId,
            quantity = quantity.ifBlank { "1" },
            tags = tags.map { it.trim() }.filter { it.isNotEmpty() },
            notes = notes.trim()
        )

        viewModelScope.launch {
            repository.addProduct(currentList.id, newProduct)
                .onSuccess {
                    _infoMessage.value = "Producte afegit!"
                }
                .onFailure { e ->
                    _errorMessage.value = "Error en afegir producte: ${e.localizedMessage}"
                }
        }
    }

    fun toggleProductCompleted(product: Product) {
        val currentList = _activeList.value ?: return
        viewModelScope.launch {
            repository.toggleProductCompleted(currentList.id, product.id, product.isCompleted)
        }
    }

    fun deleteProduct(productId: String) {
        val currentList = _activeList.value ?: return
        viewModelScope.launch {
            repository.deleteProduct(currentList.id, productId)
                .onSuccess {
                    _infoMessage.value = "Producte eliminat"
                }
        }
    }
}
