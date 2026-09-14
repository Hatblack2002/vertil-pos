package com.vertil.pos.data.dao

import androidx.room.*
import androidx.room.Dao
import com.vertil.pos.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name")
    fun observeAll(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories ORDER BY name")
    suspend fun getAll(): List<CategoryEntity>
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(c: CategoryEntity): Long
    @Update suspend fun update(c: CategoryEntity)
    @Delete suspend fun delete(c: CategoryEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE active = 1 ORDER BY name")
    fun observeAll(): Flow<List<ProductEntity>>
    @Query("SELECT * FROM products ORDER BY name")
    fun observeAllIncludingInactive(): Flow<List<ProductEntity>>
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): ProductEntity?
    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getBySku(sku: String): ProductEntity?
    @Query("""SELECT * FROM products WHERE active = 1 AND (
        name LIKE '%' || :q || '%' OR
        barcode LIKE '%' || :q || '%' OR
        sku LIKE '%' || :q || '%' OR
        brand LIKE '%' || :q || '%') ORDER BY name""")
    suspend fun search(q: String): List<ProductEntity>
    @Query("SELECT * FROM products WHERE active = 1 AND stock <= minimumStock ORDER BY (minimumStock - stock) DESC")
    fun observeLowStock(): Flow<List<ProductEntity>>
    @Query("SELECT COUNT(*) FROM products WHERE active = 1")
    suspend fun count(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(p: ProductEntity): Long
    @Update suspend fun update(p: ProductEntity)
    @Query("UPDATE products SET stock = :newStock, updatedAt = :ts WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Double, ts: Long)
    @Query("UPDATE products SET active = 0, updatedAt = :ts WHERE id = :id")
    suspend fun softDelete(id: Long, ts: Long)
    @Query("SELECT COALESCE(SUM(stock * salePriceCents), 0) FROM products WHERE active = 1")
    suspend fun totalInventoryValueCents(): Long
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name")
    fun observeAll(): Flow<List<SupplierEntity>>
    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getById(id: Long): SupplierEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(s: SupplierEntity): Long
    @Update suspend fun update(s: SupplierEntity)
    @Delete suspend fun delete(s: SupplierEntity)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name")
    fun observeAll(): Flow<List<CustomerEntity>>
    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): CustomerEntity?
    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): CustomerEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(c: CustomerEntity): Long
    @Update suspend fun update(c: CustomerEntity)
    @Delete suspend fun delete(c: CustomerEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE active = 1 ORDER BY username")
    fun observeAll(): Flow<List<UserEntity>>
    @Query("SELECT * FROM users WHERE username = :username AND active = 1 LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): UserEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(u: UserEntity): Long
    @Update suspend fun update(u: UserEntity)
    @Query("UPDATE users SET active = 0 WHERE id = :id") suspend fun softDelete(id: Long)
    @Query("SELECT COUNT(*) FROM users") suspend fun count(): Int
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY createdAt DESC LIMIT 200")
    fun observeRecent(): Flow<List<SaleEntity>>
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getById(id: Long): SaleEntity?
    @Query("SELECT * FROM sales WHERE number = :number LIMIT 1")
    suspend fun getByNumber(number: String): SaleEntity?
    @Query("SELECT * FROM sales WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    suspend fun getByDateRange(start: Long, end: Long): List<SaleEntity>
    @Query("SELECT COUNT(*) FROM sales WHERE status = 'COMPLETED'")
    suspend fun countCompleted(): Int
    @Query("SELECT COALESCE(SUM(totalCents), 0) FROM sales WHERE status = 'COMPLETED' AND createdAt BETWEEN :start AND :end")
    suspend fun sumTotalCentsBetween(start: Long, end: Long): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(s: SaleEntity): Long
    @Update suspend fun update(s: SaleEntity)
    @Query("SELECT COALESCE(MAX(CAST(SUBSTR(number, 3) AS INTEGER)), 0) FROM sales")
    suspend fun maxSaleNumber(): Int
}

@Dao
interface SaleItemDao {
    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getBySale(saleId: Long): List<SaleItemEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(items: List<SaleItemEntity>)
    @Query("SELECT productId, SUM(quantity) as qty FROM sale_items WHERE saleId IN (SELECT id FROM sales WHERE status='COMPLETED' AND createdAt BETWEEN :start AND :end) GROUP BY productId ORDER BY qty DESC LIMIT :limit")
    suspend fun topProductsByQty(start: Long, end: Long, limit: Int): List<ProductQty>
}

data class ProductQty(val productId: Long, val qty: Double)

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_movements ORDER BY createdAt DESC LIMIT 200")
    fun observeRecent(): Flow<List<InventoryMovementEntity>>
    @Query("SELECT * FROM inventory_movements WHERE productId = :productId ORDER BY createdAt DESC")
    fun observeByProduct(productId: Long): Flow<List<InventoryMovementEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(m: InventoryMovementEntity): Long
    @Query("SELECT * FROM inventory_movements WHERE reference = :ref")
    suspend fun getByReference(ref: String): List<InventoryMovementEntity>
}

@Dao
interface CashDao {
    @Query("SELECT * FROM cash_sessions ORDER BY openTime DESC LIMIT 50")
    fun observeRecentSessions(): Flow<List<CashSessionEntity>>
    @Query("SELECT * FROM cash_sessions WHERE status = 'OPEN' LIMIT 1")
    suspend fun getOpenSession(): CashSessionEntity?
    @Query("SELECT * FROM cash_sessions WHERE id = :id")
    suspend fun getById(id: Long): CashSessionEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(s: CashSessionEntity): Long
    @Update suspend fun update(s: CashSessionEntity)
    @Query("SELECT * FROM cash_movements WHERE cashSessionId = :sessionId ORDER BY createdAt DESC")
    fun observeMovements(sessionId: Long): Flow<List<CashMovementEntity>>
    @Query("SELECT * FROM cash_movements WHERE cashSessionId = :sessionId ORDER BY createdAt DESC")
    suspend fun getMovements(sessionId: Long): List<CashMovementEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertMovement(m: CashMovementEntity): Long
    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM cash_movements WHERE cashSessionId = :sessionId")
    suspend fun sumMovements(sessionId: Long): Long
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_log ORDER BY createdAt DESC LIMIT 200")
    fun observeRecent(): Flow<List<AuditLogEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(a: AuditLogEntity): Long
    @Query("SELECT COUNT(*) FROM audit_log") suspend fun count(): Int
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings")
    suspend fun getAll(): List<AppSettingsEntity>
    @Query("SELECT value FROM app_settings WHERE key = :key")
    suspend fun get(key: String): String?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun set(s: AppSettingsEntity)
}
