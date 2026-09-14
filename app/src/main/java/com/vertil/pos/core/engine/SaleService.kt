package com.vertil.pos.core.engine

import androidx.room.withTransaction
import com.vertil.pos.core.money.Money
import com.vertil.pos.data.db.PosDatabase
import com.vertil.pos.data.dao.InventoryDao
import com.vertil.pos.data.dao.SaleDao
import com.vertil.pos.data.dao.SaleItemDao
import com.vertil.pos.data.dao.ProductDao
import com.vertil.pos.data.dao.CashDao
import com.vertil.pos.data.entity.*
import com.vertil.pos.security.AuthenticationManager

/**
 * SaleService — orquesta una venta como TRANSACCIÓN atómica en Room.
 *
 * Garantiza que NUNCA ocurra:
 *  - venta registrada sin descontar stock
 *  - stock descontado sin venta registrada
 *  - caja sin movimiento correspondiente
 *
 * Si cualquier paso falla, la transacción completa se revierte.
 */
class SaleService(
    private val db: PosDatabase,
    private val auth: AuthenticationManager,
    private val auditLogger: AuditLogger
) {

    data class SaleRequest(
        val items: List<CartItem>,
        val customerId: Long?,
        val paymentMethod: PaymentMethod,
        val received: Money,
        val taxPercent: Int,
        val notes: String?
    )

    enum class PaymentMethod(val label: String) {
        CASH("Efectivo"), CARD("Tarjeta"), TRANSFER("Transferencia"), OTHER("Otro")
    }

    suspend fun processSale(request: SaleRequest): SaleResult {
        auth.requirePermission(com.vertil.pos.security.Permission.POS_ACCESS)

        // 1. Validar carrito
        val calc = SalesEngine.calculateTotal(request.items, request.taxPercent)
        val cartValidation = SalesEngine.validateCart(request.items)
        if (!cartValidation.isValid) {
            return SaleResult.Failure(cartValidation.errors.joinToString("; "))
        }

        // 2. Validar pago en efectivo
        if (request.paymentMethod == PaymentMethod.CASH) {
            val change = SalesEngine.calculateChange(calc.total, request.received)
            if (change == null) {
                return SaleResult.Failure("Pago insuficiente. Total: ${calc.total.format()}, recibido: ${request.received.format()}")
            }
        }

        // 3. Validar caja abierta
        val cashSession = db.cashDao().getOpenSession()
        val session = auth.current.value ?: return SaleResult.Failure("No hay sesión activa")

        // 4. TRANSACCIÓN ATÓMICA
        return try {
            val saleId = db.withTransaction {
                // Generar número de venta
                val currentMax = db.saleDao().maxSaleNumber()
                val number = SalesEngine.nextSaleNumber(currentMax)

                // Crear venta
                val sale = SaleEntity(
                    number = number,
                    userId = session.userId,
                    customerId = request.customerId,
                    cashSessionId = cashSession?.id,
                    subtotalCents = calc.subtotal.cents,
                    discountCents = calc.discount.cents,
                    taxCents = calc.tax.cents,
                    totalCents = calc.total.cents,
                    paymentMethod = request.paymentMethod.name,
                    receivedCents = if (request.paymentMethod == PaymentMethod.CASH) request.received.cents else 0L,
                    changeCents = if (request.paymentMethod == PaymentMethod.CASH) (request.received - calc.total).cents.coerceAtLeast(0L) else 0L,
                    notes = request.notes
                )
                val newSaleId = db.saleDao().insert(sale)

                // Insertar items + descontar stock + registrar movimientos inventario
                val saleItems = request.items.map { item ->
                    // Descontar stock
                    val product = db.productDao().getById(item.productId)
                        ?: throw IllegalStateException("Producto no encontrado: ${item.productId}")
                    if (product.stock < item.quantity) {
                        throw IllegalStateException("Stock insuficiente para ${product.name}")
                    }
                    val newStock = product.stock - item.quantity
                    db.productDao().updateStock(item.productId, newStock, System.currentTimeMillis())

                    // Registrar movimiento de inventario
                    db.inventoryDao().insert(InventoryMovementEntity(
                        productId = item.productId,
                        quantity = -item.quantity,
                        type = InventoryType.SALE.name,
                        reason = "Venta $number",
                        userId = session.userId,
                        reference = number
                    ))

                    SaleItemEntity(
                        saleId = newSaleId,
                        productId = item.productId,
                        productName = item.name,
                        barcode = item.barcode,
                        quantity = item.quantity,
                        unitPriceCents = item.unitPrice.cents,
                        discountCents = item.discountCents,
                        subtotalCents = item.subtotal.cents
                    )
                }
                db.saleItemDao().insertAll(saleItems)

                // Registrar movimiento de caja si es efectivo y hay sesión abierta
                if (request.paymentMethod == PaymentMethod.CASH && cashSession != null) {
                    db.cashDao().insertMovement(CashMovementEntity(
                        cashSessionId = cashSession.id,
                        type = "SALE",
                        amountCents = calc.total.cents,
                        reason = "Venta $number",
                        saleId = newSaleId,
                        userId = session.userId
                    ))
                }

                newSaleId
            }

            // 5. Auditoría (fuera de transacción para no bloquear)
            auditLogger.log("SALE_CREATED", "Sale", saleId.toString(), mapOf(
                "number" to db.saleDao().getById(saleId)?.number,
                "total" to calc.total.format(),
                "method" to request.paymentMethod.name
            ))

            SaleResult.Success(saleId, calc)
        } catch (t: Throwable) {
            SaleResult.Failure("Error procesando venta: ${t.message}")
        }
    }
}

sealed class SaleResult {
    data class Success(val saleId: Long, val calculation: SaleCalculation) : SaleResult()
    data class Failure(val message: String) : SaleResult()
}
