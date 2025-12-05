package com.mecagoentodo.huertohogar_v2.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mecagoentodo.huertohogar_v2.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class AdminUiState {
    object Idle : AdminUiState()
    object Loading : AdminUiState()
    data class Success(val message: String) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}

class AdminViewModel(application: Application, private val productRepository: ProductRepository) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun createProduct(productData: Map<String, Any>, imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val context = getApplication<Application>().applicationContext
                productRepository.createProduct(context, productData, imageUri)
                _uiState.value = AdminUiState.Success("¡Producto creado con éxito!")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: "Sin detalles."
                _uiState.value = AdminUiState.Error("Error HTTP ${e.code()}: $errorBody")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error("Error al crear el producto: ${e.message}")
            }
        }
    }

    fun updateProduct(productId: String, productData: Map<String, Any>, imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                val context = getApplication<Application>().applicationContext
                productRepository.updateProduct(context, productId, productData, imageUri)
                _uiState.value = AdminUiState.Success("¡Producto actualizado con éxito!")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: "Sin detalles."
                _uiState.value = AdminUiState.Error("Error HTTP ${e.code()}: $errorBody")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error("Error al actualizar el producto: ${e.message}")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                productRepository.deleteProduct(productId)
                _uiState.value = AdminUiState.Success("¡Producto eliminado con éxito!")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: "Sin detalles."
                _uiState.value = AdminUiState.Error("Error HTTP ${e.code()}: $errorBody")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error("Error al eliminar el producto: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = AdminUiState.Idle
    }

    // --- Factory ---
    class Factory(private val application: Application, private val productRepository: ProductRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AdminViewModel(application, productRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}