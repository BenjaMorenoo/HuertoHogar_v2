package com.mecagoentodo.huertohogar_v2.data.network.model

// Esta clase representa el cuerpo de la petición para autenticarse
// con el email y la contraseña.
data class LoginRequest(
    val identity: String, // email o username
    val password: String
)