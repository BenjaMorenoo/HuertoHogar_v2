package com.mecagoentodo.huertohogar_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mecagoentodo.huertohogar_v2.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel : ViewModel() {

    private val _allProducts = getSampleProducts()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(_allProducts)
    val products: StateFlow<List<Product>> = _searchText
        .combine(_products) { text, products ->
            if (text.isBlank()) {
                products
            } else {
                products.filter { it.name.contains(text, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _allProducts
        )

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    private fun getSampleProducts(): List<Product> {
        return listOf(
            Product(
                id = 1,
                code = "FR001",
                name = "Manzanas Fuji",
                category = "Frutas Frescas",
                price = 1200.0,
                unit = "kilo",
                stock = 150.0,
                description = "Manzanas Fuji crujientes y dulces, cultivadas en el Valle del Maule. Perfectas para meriendas saludables o como ingrediente en postres. Estas manzanas son conocidas por su textura firme y su sabor equilibrado entre dulce y ácido.",
                imageUrl = ""
            ),
            Product(
                id = 2,
                code = "VE001",
                name = "Tomates Orgánicos",
                category = "Verduras Orgánicas",
                price = 2500.0,
                unit = "kilo",
                stock = 100.0,
                description = "Tomates orgánicos frescos y jugosos, cultivados sin pesticidas. Ideales para ensaladas, salsas o para disfrutar solos.",
                imageUrl = ""
            ),
            Product(
                id = 3,
                code = "VE002",
                name = "Espinacas Orgánicas",
                category = "Verduras Orgánicas",
                price = 1500.0,
                unit = "atado",
                stock = 80.0,
                description = "Espinacas orgánicas de hojas frescas y saludables, ricas en hierro y vitaminas. Perfectas para ensaladas, batidos o como guarnición.",
                imageUrl = ""
            ),
            Product(
                id = 4,
                code = "LA001",
                name = "Yogurt Natural",
                category = "Productos Lácteos",
                price = 1800.0,
                unit = "unidad",
                stock = 50.0,
                description = "Yogurt natural sin endulzar, cremoso y de sabor suave. Producido con leche de vacas de libre pastoreo.",
                imageUrl = ""
            ),
            Product(
                id = 5,
                code = "PO001",
                name = "Miel de Abeja Orgánica",
                category = "Productos Orgánicos",
                price = 4500.0,
                unit = "frasco",
                stock = 60.0,
                description = "Miel de abeja 100% pura y orgánica, recolectada de forma sostenible en los bosques nativos del sur de Chile.",
                imageUrl = ""
            )
        )
    }
}