package com.mecagoentodo.huertohogar_v2.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val date: Date,
    val total: Double,
    val shippingAddress: String
)