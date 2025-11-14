package com.mecagoentodo.huertohogar_v2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mecagoentodo.huertohogar_v2.data.AppDatabase
import com.mecagoentodo.huertohogar_v2.data.CartItem
import com.mecagoentodo.huertohogar_v2.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val cartDao = AppDatabase.getDatabase(application).cartDao()

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
                    price = product.price
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

    fun checkout() {
        viewModelScope.launch {
            cartDao.clearCart()
            _checkoutState.value = CheckoutState.Success
        }
    }
    
    fun resetCheckoutState(){
        _checkoutState.value = CheckoutState.Idle
    }
}

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Success : CheckoutState()
}