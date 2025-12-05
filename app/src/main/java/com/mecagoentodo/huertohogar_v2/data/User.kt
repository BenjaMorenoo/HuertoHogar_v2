package com.mecagoentodo.huertohogar_v2.data

// Este es nuestro modelo de dominio. Representa a un usuario tal como
// lo devuelve la API de PocketBase (sin campos sensibles como la contraseña).
data class User(
    val id: String,
    val name: String,
    val email: String,
    val address: String,
    val phone: String,
    val isAdmin: Boolean = false
)