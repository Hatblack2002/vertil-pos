package com.vertil.pos.core.engine

import com.vertil.pos.core.money.Money

/**
 * CashEngine — cálculos de caja.
 */
object CashEngine {

    /**
     * Efectivo esperado al cierre = fondo inicial + sum(movimientos)
     * movimientos positivos: ventas efectivo, cash-in
     * movimientos negativos: reembolsos, cash-out
     */
    fun computeExpected(opening: Money, movementsSum: Money): Money = opening + movementsSum

    fun computeDifference(expected: Money, counted: Money): Money = counted - expected

    fun validateOpen(opening: Money): SalesEngine.ValidationResult {
        val errors = mutableListOf<String>()
        if (opening.isNegative()) errors.add("El fondo inicial no puede ser negativo")
        return SalesEngine.ValidationResult(errors)
    }

    fun validateClose(expected: Money, counted: Money): SalesEngine.ValidationResult {
        val errors = mutableListOf<String>()
        if (counted.isNegative()) errors.add("El efectivo contado no puede ser negativo")
        return SalesEngine.ValidationResult(errors)
    }
}
