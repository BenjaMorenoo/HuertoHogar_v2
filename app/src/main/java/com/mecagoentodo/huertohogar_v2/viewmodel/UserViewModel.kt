package com.mecagoentodo.huertohogar_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mecagoentodo.huertohogar_v2.data.User
import com.mecagoentodo.huertohogar_v2.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> = _loggedInUser

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    fun setLoggedInUser(user: User?) {
        _loggedInUser.value = user
    }

    fun logout() {
        _loggedInUser.value = null
    }

    fun updateUser(address: String, phone: String, password: String, confirmPassword: String) {
        val currentUser = _loggedInUser.value
        if (currentUser == null) {
            _updateState.value = UpdateState.Error("No hay un usuario con sesión iniciada.")
            return
        }

        val dataToUpdate = mutableMapOf<String, Any>()
        dataToUpdate["address"] = address
        dataToUpdate["phone"] = "+569$phone"

        if (password.isNotBlank()) {
            if (password != confirmPassword) {
                _updateState.value = UpdateState.Error("Las contraseñas no coinciden.")
                return
            }
            dataToUpdate["password"] = password
            dataToUpdate["passwordConfirm"] = confirmPassword
        }

        viewModelScope.launch {
            try {
                val updatedUser = userRepository.updateUser(currentUser.id, dataToUpdate)
                setLoggedInUser(updatedUser)
                _updateState.value = UpdateState.Success
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Error al actualizar: ${e.message}")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }
    
    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}