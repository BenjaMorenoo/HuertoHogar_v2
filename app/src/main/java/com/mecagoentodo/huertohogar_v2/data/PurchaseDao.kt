package com.mecagoentodo.huertohogar_v2.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class PurchaseWithItems(
    @Embedded val purchase: Purchase,
    @Relation(
        parentColumn = "id",
        entityColumn = "purchaseId"
    )
    val items: List<PurchaseItem>
)

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItem>)

    @Transaction
    @Query("SELECT * FROM purchases WHERE userId = :userId ORDER BY date DESC")
    fun getPurchasesForUser(userId: String): Flow<List<PurchaseWithItems>>
}