package com.mecagoentodo.huertohogar_v2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mecagoentodo.huertohogar_v2.data.AppDatabase
import com.mecagoentodo.huertohogar_v2.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountViewModel(application: Application, private val userViewModel: UserViewModel) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDatabase(application).userDao()

    // Registration State
    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    // Login State
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun registerUser(name: String, email: String, address: String, phone: String, password: String, confirmPassword: String) {
        // --- Validaciones ---
        if (name.isBlank() || email.isBlank() || address.isBlank() || phone.isBlank() || password.isBlank()) {
            _registrationState.value = RegistrationState.Error("Todos los campos son obligatorios.")
            return
        }
        if (phone.length != 8 || !phone.all { it.isDigit() }) {
            _registrationState.value = RegistrationState.Error("El teléfono debe tener 8 dígitos.")
            return
        }
        if (password.length !in 6..8) {
            _registrationState.value = RegistrationState.Error("La contraseña debe tener entre 6 y 8 caracteres.")
            return
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            _registrationState.value = RegistrationState.Error("La contraseña debe contener al menos un símbolo.")
            return
        }
        if (password != confirmPassword) {
            _registrationState.value = RegistrationState.Error("Las contraseñas no coinciden.")
            return
        }

        viewModelScope.launch {
            try {
                if (userDao.getUserByEmail(email) != null) {
                    _registrationState.value = RegistrationState.Error("Ya existe un usuario con este correo.")
                    return@launch
                }

                // --- Formateo y Creación del Usuario ---
                val formattedPhone = "+569$phone"
                val user = User(name = name, email = email, address = address, phone = formattedPhone, password = password)
                userDao.insert(user)
                _registrationState.value = RegistrationState.Success
            } catch (e: Exception) {
                _registrationState.value = RegistrationState.Error("Error en el registro: ${e.message}")
            }
        }
    }

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("El correo y la contraseña no pueden estar vacíos.")
            return
        }

        viewModelScope.launch {
            try {
                val user = userDao.getUserByEmail(email)
                if (user == null) {
                    _loginState.value = LoginState.Error("Usuario no encontrado.")
                } else if (user.password != password) {
                    _loginState.value = LoginState.Error("Contraseña incorrecta.")
                } else {
                    userViewModel.setLoggedInUser(user)
                    _loginState.value = LoginState.Success(user)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error en el inicio de sesión: ${e.message}")
            }
        }
    }

    fun resetRegistrationState() {
        _registrationState.value = RegistrationState.Idle
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
}

class AccountViewModelFactory(private val application: Application, private val userViewModel: UserViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountViewModel(application, userViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Success : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

sealed class LoginState {
    object Idle : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}