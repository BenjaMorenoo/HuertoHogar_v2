package com.mecagoentodo.huertohogar_v2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mecagoentodo.huertohogar_v2.data.User
import com.mecagoentodo.huertohogar_v2.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountViewModel(private val userRepository: UserRepository, private val userViewModel: UserViewModel) : ViewModel() {

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun registerUser(name: String, email: String, address: String, phone: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _registrationState.value = RegistrationState.Error("Las contraseñas no coinciden.")
            return
        }

        viewModelScope.launch {
            try {
                val registeredUser = userRepository.register(name, email, address, phone, password, confirmPassword)
                userViewModel.setLoggedInUser(registeredUser)
                _registrationState.value = RegistrationState.Success
            } catch (e: Exception) {
                val errorMessage = if (e.message?.contains("validation_not_unique") == true) {
                    "El correo electrónico ya está en uso."
                } else {
                    "Error en el registro: ${e.message}"
                }
                _registrationState.value = RegistrationState.Error(errorMessage)
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val authResponse = userRepository.login(email, password)
                userViewModel.setLoggedInUser(authResponse.record)
                _loginState.value = LoginState.Success(authResponse.record)
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

class AccountViewModelFactory(private val userRepository: UserRepository, private val userViewModel: UserViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountViewModel(userRepository, userViewModel) as T
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