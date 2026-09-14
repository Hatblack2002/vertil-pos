package com.vertil.pos.core.engine

/**
 * Tipos de movimiento de inventario.
 */
enum class InventoryType(val sign: Int, val label: String) {
    SALE(-1, "Venta"),
    PURCHASE(1, "Compra"),
    ADJUSTMENT(0, "Ajuste"),
    RETURN(1, "Devolución"),
    LOSS(-1, "Pérdida"),
    MANUAL_ENTRY(1, "Entrada manual"),
    MANUAL_EXIT(-1, "Salida manual");

    /** Cantidad efectiva con signo. */
    fun effectiveQty(quantity: Double): Double = quantity * sign
}

/**
 * InventoryEngine — validaciones de stock (sin Android).
 */
object InventoryEngine {

    fun computeNewStock(current: Double, type: InventoryType, quantity: Double): Double {
        val effective = type.effectiveQty(quantity)
        return current + effective
    }

    fun isLowStock(stock: Double, minimumStock: Double): Boolean = stock <= minimumStock

    fun wouldGoNegative(current: Double, type: InventoryType, quantity: Double): Boolean {
        val newStock = computeNewStock(current, type, quantity)
        return newStock < 0
    }

    fun validateMovement(current: Double, type: InventoryType, quantity: Double, productName: String): SalesEngine.ValidationResult {
        val errors = mutableListOf<String>()
        if (quantity <= 0) errors.add("La cantidad debe ser positiva para $productName")
        if (quantity > 0 && wouldGoNegative(current, type, quantity)) {
            errors.add("Stock insuficiente para $productName (actual: $current, intenta salir: ${type.effectiveQty(quantity)})")
        }
        return SalesEngine.ValidationResult(errors)
    }
}
