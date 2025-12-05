package com.mecagoentodo.huertohogar_v2.data.repository

import com.mecagoentodo.huertohogar_v2.data.User
import com.mecagoentodo.huertohogar_v2.data.network.ApiService
import com.mecagoentodo.huertohogar_v2.data.network.model.AuthResponse
import com.mecagoentodo.huertohogar_v2.data.network.model.LoginRequest
import com.mecagoentodo.huertohogar_v2.data.network.model.RegisterRequest

class UserRepository(private val apiService: ApiService) {

    suspend fun login(email: String, password: String): AuthResponse {
        val request = LoginRequest(identity = email, password = password)
        return apiService.login(request)
    }

    suspend fun register(name: String, email: String, address: String, phone: String, password: String, passwordConfirm: String): User {
        val request = RegisterRequest(
            name = name,
            email = email,
            address = address,
            phone = phone,
            password = password,
            passwordConfirm = passwordConfirm
        )
        return apiService.register(request)
    }

    suspend fun updateUser(userId: String, dataToUpdate: Map<String, Any>): User {
        return apiService.updateUser(userId, dataToUpdate)
    }
}