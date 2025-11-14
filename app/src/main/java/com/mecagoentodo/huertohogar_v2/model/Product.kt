package com.mecagoentodo.huertohogar_v2.model

data class Product(
    val id: Int,
    val code: String,
    val name: String,
    val category: String,
    val price: Double,
    val unit: String,
    val stock: Double,
    val description: String,
    val imageUrl: String
)