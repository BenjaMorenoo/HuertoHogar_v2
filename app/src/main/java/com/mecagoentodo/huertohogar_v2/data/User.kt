package com.mecagoentodo.huertohogar_v2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val address: String,
    val phone: String,
    val password: String
)