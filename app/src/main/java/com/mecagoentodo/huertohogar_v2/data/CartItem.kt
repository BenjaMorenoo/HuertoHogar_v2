package com.mecagoentodo.huertohogar_v2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val collectionId: String, // Added for image URL
    val imageUrl: String      // Added for image URL
) {
    /**
     * Constructs the full URL for the product image hosted on PocketBase.
     */
    val fullImageUrl: String
        get() {
            if (productId.isBlank() || collectionId.isBlank() || imageUrl.isBlank()) {
                return ""
            }
            return "https://api-huertohogar.ironhost.cl/api/files/${collectionId}/${productId}/${imageUrl}"
        }
}