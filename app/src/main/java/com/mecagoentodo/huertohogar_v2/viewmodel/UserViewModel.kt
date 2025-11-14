package com.mecagoentodo.huertohogar_v2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mecagoentodo.huertohogar_v2.data.AppDatabase
import com.mecagoentodo.huertohogar_v2.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> = _loggedInUser

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState = _updateState.asStateFlow()

    fun setLoggedInUser(user: User) {
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

        // --- Validaciones ---
        if (address.isBlank() || phone.isBlank()) {
            _updateState.value = UpdateState.Error("La dirección y el teléfono no pueden estar vacíos.")
            return
        }
        if (phone.length != 8 || !phone.all { it.isDigit() }) {
            _updateState.value = UpdateState.Error("El teléfono debe tener 8 dígitos.")
            return
        }
        
        var updatedUser = currentUser.copy(address = address, phone = "+569$phone")

        if (password.isNotBlank()) {
             if (password.length !in 6..8) {
                _updateState.value = UpdateState.Error("La contraseña debe tener entre 6 y 8 caracteres.")
                return
            }
            if (!password.any { !it.isLetterOrDigit() }) {
                _updateState.value = UpdateState.Error("La contraseña debe contener al menos un símbolo.")
                return
            }
            if (password != confirmPassword) {
                _updateState.value = UpdateState.Error("Las contraseñas no coinciden.")
                return
            }
            updatedUser = updatedUser.copy(password = password)
        }

        viewModelScope.launch {
            try {
                userDao.update(updatedUser)
                setLoggedInUser(updatedUser) // Actualizamos el usuario en la sesión
                _updateState.value = UpdateState.Success
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Error al actualizar: ${e.message}")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}