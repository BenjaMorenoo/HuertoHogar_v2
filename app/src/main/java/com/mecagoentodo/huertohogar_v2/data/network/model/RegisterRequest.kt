package com.mecagoentodo.huertohogar_v2.data.network.model

// Esta clase representa el cuerpo de la petición para registrar un nuevo usuario.
// Incluye los campos requeridos por PocketBase como passwordConfirm.
data class RegisterRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val name: String,
    val address: String,
    val phone: String
)