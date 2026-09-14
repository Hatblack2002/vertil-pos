package com.vertil.pos.core.money

/**
 * Money — representación EXACTA de dinero usando Long (centavos).
 *
 * NO usa Float ni Double para cálculos financieros.
 * 1 DOP = 100 centavos → RD$ 125.50 se almacena como 12550L.
 *
 * Todas las operaciones aritméticas son deterministas.
 */
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {

    operator fun plus(other: Money): Money = Money(cents + other.cents)
    operator fun minus(other: Money): Money = Money(cents - other.cents)
    operator fun times(factor: Int): Money = Money(cents * factor)
    operator fun times(factor: Long): Money = Money(cents * factor)
    operator fun div(divisor: Int): Money = Money(cents / divisor)
    operator fun compareTo(other: Long): Int = cents.compareTo(other)

    fun isNegative(): Boolean = cents < 0
    fun isZero(): Boolean = cents == 0L
    fun isPositive(): Boolean = cents > 0

    /** Multiplica por un ratio (ej: impuesto 18% → times(0.18)). Usa BigDecimal internamente para exactitud. */
    fun timesRatio(ratio: Double): Money {
        val bd = java.math.BigDecimal(cents).multiply(java.math.BigDecimal(ratio.toString()))
        return Money(bd.setScale(0, java.math.RoundingMode.HALF_UP).toLong())
    }

    /** Porcentaje (0-100). Ej: discount 10% → percent(10). */
    fun percent(percentage: Int): Money {
        val bd = java.math.BigDecimal(cents)
            .multiply(java.math.BigDecimal(percentage))
            .divide(java.math.BigDecimal(100), 0, java.math.RoundingMode.HALF_UP)
        return Money(bd.toLong())
    }

    /** Formatea a string legible: 12550 → "RD$ 125.50". */
    fun format(symbol: String = "RD$ "): String {
        val abs = if (cents < 0) -cents else cents
        val part = abs / 100
        val frac = abs % 100
        val sign = if (cents < 0) "-" else ""
        return "$sign$symbol$part.${frac.toString().padStart(2, '0')}"
    }

    /** Formato sin símbolo: 12550 → "125.50". */
    fun formatBare(): String {
        val abs = if (cents < 0) -cents else cents
        val part = abs / 100
        val frac = abs % 100
        val sign = if (cents < 0) "-" else ""
        return "$sign$part.${frac.toString().padStart(2, '0')}"
    }

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    companion object {
        val ZERO = Money(0L)
        fun ofCents(cents: Long): Money = Money(cents)
        fun ofUnits(units: Long): Money = Money(units * 100)
        /** Parse "125.50" → 12550 cents. Acepta coma decimal. */
        fun parse(text: String): Money? {
            val cleaned = text.trim().replace(",", ".").replace(Regex("[^\\d.-]"), "")
            val dot = cleaned.indexOf('.')
            return try {
                if (dot < 0) {
                    Money(cleaned.toLong() * 100)
                } else {
                    val intPart = if (dot == 0) 0L else cleaned.substring(0, dot).toLong()
                    val fracRaw = cleaned.substring(dot + 1)
                    val frac = fracRaw.padEnd(2, '0').take(2)
                    val sign = if (cleaned.startsWith("-")) -1 else 1
                    Money(sign * (intPart * 100 + frac.toLong()))
                }
            } catch (_: Throwable) { null }
        }
    }
}
