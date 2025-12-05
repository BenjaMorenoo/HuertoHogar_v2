package com.mecagoentodo.huertohogar_v2.data.network.model

import com.mecagoentodo.huertohogar_v2.data.User

// Esta clase representa la respuesta que da PocketBase al autenticarse.
// Contiene el token y los datos del usuario (el "record").
data class AuthResponse(
    val token: String,
    val record: User
)