package com.mecagoentodo.huertohogar_v2.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mecagoentodo.huertohogar_v2.model.Product
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Database(entities = [CartItem::class, Purchase::class, PurchaseItem::class, Product::class], version = 21, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.productDao())
                }
            }
        }

        suspend fun populateDatabase(productDao: ProductDao) {
            productDao.deleteAll()
            
            productDao.insert(Product("prod_1", "", "FR001", "Manzanas Fuji", "Frutas Frescas", 1200.0, "kilo", 150.0, "Manzanas Fuji crujientes y dulces, cultivadas en el Valle del Maule. Perfectas para meriendas saludables o como ingrediente en postres.", "https://laveguitadengo.cl/wp-content/uploads/2022/04/manzanafuji.jpg"))
            productDao.insert(Product("prod_2", "", "VE001", "Tomates Orgánicos", "Verduras Orgánicas", 2500.0, "kilo", 100.0, "Tomates orgánicos frescos y jugosos, cultivados sin pesticidas. Ideales para ensaladas, salsas o para disfrutar solos.", "https://placehold.co/600x400/E9967A/FFFFFF/png?text=Tomate"))
            productDao.insert(Product("prod_3", "", "VE002", "Espinacas Orgánicas", "Verduras Orgánicas", 1500.0, "atado", 80.0, "Espinacas orgánicas de hojas frescas y saludables, ricas en hierro y vitaminas. Perfectas para ensaladas, batidos o como guarnición.", "https://placehold.co/600x400/8FBC8F/FFFFFF/png?text=Espinaca"))
            productDao.insert(Product("prod_4", "", "LA001", "Yogurt Natural", "Productos Lácteos", 1800.0, "unidad", 50.0, "Yogurt natural sin endulzar, cremoso y de sabor suave. Producido con leche de vacas de libre pastoreo.", "https://placehold.co/600x400/F5F5DC/000000/png?text=Yogurt"))
            productDao.insert(Product("prod_5", "", "PO001", "Miel de Abeja Orgánica", "Productos Orgánicos", 4500.0, "frasco", 60.0, "Miel de abeja 100% pura y orgánica, recolectada de forma sostenible en los bosques nativos del sur de Chile.", "https://placehold.co/600x400/FFD700/000000/png?text=Miel"))
        }
    }
}