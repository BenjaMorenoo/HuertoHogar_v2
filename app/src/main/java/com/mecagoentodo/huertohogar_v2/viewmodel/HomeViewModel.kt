package com.mecagoentodo.huertohogar_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mecagoentodo.huertohogar_v2.data.repository.ProductRepository
import com.mecagoentodo.huertohogar_v2.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val products: List<Product>, val categories: List<String>, val selectedCategory: String) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

class HomeViewModel(private val productRepository: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private var allProducts: List<Product> = emptyList()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                allProducts = productRepository.getProducts()
                val categories = listOf("Todas") + allProducts.map { it.category }.distinct().sorted()
                _uiState.value = ProductUiState.Success(allProducts, categories, "Todas")
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error("Error al cargar los productos: ${e.message}")
            }
        }
    }

    fun selectCategory(category: String) {
        val currentState = _uiState.value
        if (currentState is ProductUiState.Success) {
            val filteredProducts = if (category == "Todas") {
                allProducts
            } else {
                allProducts.filter { it.category == category }
            }
            _uiState.update {
                (it as ProductUiState.Success).copy(
                    products = filteredProducts,
                    selectedCategory = category
                )
            }
        }
    }

    fun refreshProducts() {
        loadProducts()
    }

    // --- Factory para el ViewModel ---
    class Factory(private val productRepository: ProductRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(productRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}