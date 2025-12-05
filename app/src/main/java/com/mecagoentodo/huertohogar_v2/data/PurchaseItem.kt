package com.mecagoentodo.huertohogar_v2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_items")
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val purchaseId: Long,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double
)