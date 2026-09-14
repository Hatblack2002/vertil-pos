package com.vertil.pos.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vertil.pos.data.dao.*
import com.vertil.pos.data.entity.*

@Database(
    entities = [
        CategoryEntity::class,
        ProductEntity::class,
        SupplierEntity::class,
        CustomerEntity::class,
        UserEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        InventoryMovementEntity::class,
        CashSessionEntity::class,
        CashMovementEntity::class,
        AuditLogEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun supplierDao(): SupplierDao
    abstract fun customerDao(): CustomerDao
    abstract fun userDao(): UserDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun cashDao(): CashDao
    abstract fun auditDao(): AuditDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile private var instance: PosDatabase? = null

        fun get(context: Context): PosDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PosDatabase::class.java,
                    "vertil-pos.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
