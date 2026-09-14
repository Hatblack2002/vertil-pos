package com.vertil.pos.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"], unique = true),
        Index(value = ["sku"], unique = false),
        Index(value = ["name"]),
        Index(value = ["categoryId"]),
        Index(value = ["active"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String?,
    val sku: String?,
    val name: String,
    val description: String?,
    val categoryId: Long?,
    val brand: String?,
    val purchasePriceCents: Long,   // precio de compra (centavos)
    val salePriceCents: Long,        // precio de venta (centavos)
    val stock: Double,               // double para soportar fracción (kg)
    val minimumStock: Double,
    val maximumStock: Double,
    val unit: String,                // "unidad", "kg", "lt", etc.
    val imageUri: String?,
    val supplierId: Long?,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers", indices = [Index(value = ["name"])])
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val notes: String?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers", indices = [Index(value = ["name"]), Index(value = ["phone"])])
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String?,
    val email: String?,
    val address: String?,
    val notes: String?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users", indices = [Index(value = ["username"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: String,  // ADMIN / MANAGER / CASHIER
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sales", indices = [Index(value = ["number"], unique = true), Index(value = ["createdAt"]), Index(value = ["userId"]), Index(value = ["cashSessionId"])])
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,              // "V-000001"
    val userId: Long,
    val customerId: Long?,
    val cashSessionId: Long?,
    val subtotalCents: Long,
    val discountCents: Long,
    val taxCents: Long,
    val totalCents: Long,
    val paymentMethod: String,       // CASH / CARD / TRANSFER / OTHER
    val receivedCents: Long,         // efectivo recibido (0 si no aplica)
    val changeCents: Long,           // cambio
    val status: String = "COMPLETED",// COMPLETED / CANCELLED / REFUNDED
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sale_items", indices = [Index(value = ["saleId"]), Index(value = ["productId"])])
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,         // snapshot
    val barcode: String?,
    val quantity: Double,
    val unitPriceCents: Long,        // precio unitario al momento de venta
    val discountCents: Long,
    val subtotalCents: Long
)

@Entity(tableName = "inventory_movements", indices = [Index(value = ["productId"]), Index(value = ["createdAt"]), Index(value = ["type"])])
data class InventoryMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val quantity: Double,            // positivo entrada, negativo salida
    val type: String,                // SALE / PURCHASE / ADJUSTMENT / RETURN / LOSS / MANUAL_ENTRY / MANUAL_EXIT
    val reason: String?,
    val userId: Long,
    val reference: String?,          // ej: número de venta
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cash_sessions", indices = [Index(value = ["openTime"])])
data class CashSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val openingBalanceCents: Long,
    val closingBalanceCents: Long? = null,
    val expectedBalanceCents: Long? = null,
    val differenceCents: Long? = null,
    val openTime: Long = System.currentTimeMillis(),
    val closeTime: Long? = null,
    val status: String = "OPEN"      // OPEN / CLOSED
)

@Entity(tableName = "cash_movements", indices = [Index(value = ["cashSessionId"]), Index(value = ["createdAt"])])
data class CashMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cashSessionId: Long,
    val type: String,                // SALE / REFUND / CASH_IN / CASH_OUT / OPEN / CLOSE
    val amountCents: Long,           // positivo entrada, negativo salida
    val reason: String?,
    val saleId: Long? = null,
    val userId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_log", indices = [Index(value = ["createdAt"]), Index(value = ["userId"]), Index(value = ["action"])])
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long?,
    val username: String?,
    val action: String,              // LOGIN / SALE_CREATED / etc.
    val entity: String?,
    val entityId: String?,
    val details: String?,            // JSON serializado
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)
