package com.vertil.pos.core.engine

import com.vertil.pos.core.money.Money
import com.vertil.pos.data.entity.SaleEntity
import com.vertil.pos.data.entity.SaleItemEntity

/**
 * Item del carrito (en memoria, antes de persistir).
 */
data class CartItem(
    val productId: Long,
    val name: String,
    val barcode: String?,
    val unitPrice: Money,
    val quantity: Double,
    val stockAvailable: Double,
    val discountCents: Long = 0L
) {
    val subtotal: Money get() = unitPrice.times(quantity.toLong().coerceAtLeast(1)).minus(Money.ofCents(discountCents))
    val hasStock: Boolean get() = stockAvailable >= quantity
}

/**
 * Resultado del cálculo de una venta.
 */
data class SaleCalculation(
    val subtotal: Money,
    val discount: Money,
    val tax: Money,
    val total: Money
)

/**
 * SalesEngine — cálculos puros (sin Android, testeable en JVM).
 *
 * NO usa Float/Double para dinero. Toda operación es con Money (Long cents).
 */
object SalesEngine {

    fun calculateSubtotal(items: List<CartItem>): Money =
        items.fold(Money.ZERO) { acc, item -> acc + item.subtotal }

    fun calculateDiscount(items: List<CartItem>): Money =
        items.fold(Money.ZERO) { acc, item -> acc + Money.ofCents(item.discountCents) }

    /** Impuesto = (subtotal - descuento) * (taxPercent / 100). */
    fun calculateTax(subtotal: Money, discount: Money, taxPercent: Int): Money {
        val base = subtotal - discount
        if (base.isNegative() || base.isZero()) return Money.ZERO
        return base.percent(taxPercent)
    }

    fun calculateTotal(items: List<CartItem>, taxPercent: Int): SaleCalculation {
        val subtotal = calculateSubtotal(items)
        val discount = calculateDiscount(items)
        val tax = calculateTax(subtotal, discount, taxPercent)
        val total = subtotal - discount + tax
        return SaleCalculation(subtotal, discount, tax, total)
    }

    /** Cambio a devolver. Si recibido < total, devuelve null. */
    fun calculateChange(total: Money, received: Money): Money? =
        if (received < total) null else received - total

    /** Valida que todas las cantidades sean positivas y haya stock. */
    fun validateCart(items: List<CartItem>): ValidationResult {
        val errors = mutableListOf<String>()
        items.forEach { item ->
            if (item.quantity <= 0) errors.add("Cantidad inválida para ${item.name}")
            if (!item.hasStock) errors.add("Stock insuficiente para ${item.name} (disponible: ${item.stockAvailable})")
        }
        return ValidationResult(errors)
    }

    data class ValidationResult(val errors: List<String>) {
        val isValid: Boolean get() = errors.isEmpty()
    }

    /**
     * Genera el siguiente número de venta: "V-000001", "V-000002", ...
     * Seguro ante cierres: usa el máximo actual almacenado en DB.
     */
    fun nextSaleNumber(currentMax: Int): String =
        "V-%06d".format(currentMax + 1)
}
