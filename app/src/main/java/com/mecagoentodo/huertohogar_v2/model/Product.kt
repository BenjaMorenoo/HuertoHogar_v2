package com.mecagoentodo.huertohogar_v2.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val collectionId: String = "", // PocketBase specific field
    val code: String,
    val name: String,
    val category: String,
    val price: Double,
    val unit: String,
    val stock: Double,
    val description: String,
    val imageUrl: String // This is just the filename from PocketBase
) {
    /**
     * Constructs the full URL for the product image hosted on PocketBase.
     */
    val fullImageUrl: String
        get() {
            if (id.isBlank() || collectionId.isBlank() || imageUrl.isBlank()) {
                return ""
            }
            return "https://api-huertohogar.ironhost.cl/api/files/${collectionId}/${id}/${imageUrl}"
        }
}