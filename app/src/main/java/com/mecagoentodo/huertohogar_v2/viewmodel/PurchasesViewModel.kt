package com.mecagoentodo.huertohogar_v2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mecagoentodo.huertohogar_v2.data.AppDatabase
import com.mecagoentodo.huertohogar_v2.data.PurchaseWithItems
import kotlinx.coroutines.flow.Flow

class PurchasesViewModel(application: Application) : AndroidViewModel(application) {

    private val purchaseDao = AppDatabase.getDatabase(application).purchaseDao()

    fun getPurchasesForUser(userId: String): Flow<List<PurchaseWithItems>> {
        return purchaseDao.getPurchasesForUser(userId)
    }
}