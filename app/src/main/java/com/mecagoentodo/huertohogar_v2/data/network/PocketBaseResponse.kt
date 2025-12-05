package com.mecagoentodo.huertohogar_v2.data.network

// Esta clase genérica nos ayuda a parsear la respuesta de PocketBase,
// que siempre devuelve los resultados dentro de una lista "items".
data class PocketBaseResponse<T>(
    val items: List<T>
)