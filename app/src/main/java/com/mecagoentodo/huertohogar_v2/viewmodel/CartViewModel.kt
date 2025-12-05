package com.mecagoentodo.huertohogar_v2.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mecagoentodo.huertohogar_v2.data.*
import com.mecagoentodo.huertohogar_v2.data.repository.ProductRepository
import com.mecagoentodo.huertohogar_v2.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class CartViewModel( 
    application: Application,
    private val productRepository: ProductRepository
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val cartDao = db.cartDao()
    private val purchaseDao = db.purchaseDao()

    val cartItems: StateFlow<List<CartItem>> = cartDao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState = _checkoutState.asStateFlow()

    fun addToCart(product: Product) {
        viewModelScope.launch {
            val existingItem = cartDao.getItemByProductId(product.id)
            if (existingItem != null) {
                increaseQuantity(existingItem)
            } else {
                val newItem = CartItem(
                    productId = product.id,
                    productName = product.name,
                    quantity = 1,
                    price = product.price,
                    collectionId = product.collectionId, // Added
                    imageUrl = product.imageUrl       // Added
                )
                cartDao.insert(newItem)
            }
        }
    }

    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch {
            val updatedItem = item.copy(quantity = item.quantity + 1)
            cartDao.update(updatedItem)
        }
    }

    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch {
            if (item.quantity > 1) {
                val updatedItem = item.copy(quantity = item.quantity - 1)
                cartDao.update(updatedItem)
            } else {
                cartDao.delete(item)
            }
        }
    }

    fun removeFromCart(item: CartItem) {
        viewModelScope.launch {
            cartDao.delete(item)
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            cartDao.clearCart()
        }
    }

    fun checkout(userId: String, shippingAddress: String) {
        viewModelScope.launch {
            try {
                val items = cartItems.first()
                if (items.isEmpty()) return@launch

                // 1. Leer y validar el stock de todos los productos primero
                val stockUpdates = items.map { cartItem ->
                    val product = productRepository.getProductById(cartItem.productId)
                    val newStock = product.stock - cartItem.quantity
                    if (newStock < 0) {
                        throw IllegalStateException("No hay stock suficiente para ${product.name}.")
                    }
                    Pair(cartItem.productId, newStock)
                }

                // 2. Si la validación fue exitosa, ejecutar todas las actualizaciones de stock
                val context = getApplication<Application>().applicationContext
                stockUpdates.forEach { (productId, newStock) ->
                    productRepository.updateProduct(context, productId, mapOf("stock" to newStock), null)
                }

                // 3. Guardar la compra en la base de datos local
                val total = items.sumOf { it.price * it.quantity }
                val purchase = Purchase(
                    userId = userId,
                    date = Date(),
                    total = total,
                    shippingAddress = shippingAddress
                )
                val purchaseId = purchaseDao.insertPurchase(purchase)

                val purchaseItems = items.map { 
                    PurchaseItem(
                        purchaseId = purchaseId,
                        productId = it.productId,
                        productName = it.productName,
                        quantity = it.quantity,
                        price = it.price
                    )
                }
                purchaseDao.insertPurchaseItems(purchaseItems)

                // 4. Limpiar el carrito y actualizar el estado
                cartDao.clearCart()
                _checkoutState.value = CheckoutState.Success
            } catch (t: Throwable) {
                Log.e("CartViewModel", "Error durante el checkout", t)
                _checkoutState.value = CheckoutState.Error("Error en el checkout: ${t.message}")
            }
        }
    }
    
    fun resetCheckoutState(){
        _checkoutState.value = CheckoutState.Idle
    }
}

class CartViewModelFactory(private val application: Application, private val productRepository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(application, productRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Success : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}