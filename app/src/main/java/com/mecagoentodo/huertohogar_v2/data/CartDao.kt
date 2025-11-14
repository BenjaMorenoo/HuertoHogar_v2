package com.mecagoentodo.huertohogar_v2.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItem)

    @Update
    suspend fun update(item: CartItem)

    @Delete
    suspend fun delete(item: CartItem)

    @Query("SELECT * FROM cart_items")
    fun getAllItems(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getItemByProductId(productId: Int): CartItem?

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}