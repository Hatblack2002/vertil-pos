package com.vertil.pos.core

import com.vertil.pos.core.engine.SalesEngine
import com.vertil.pos.core.engine.CartItem
import com.vertil.pos.core.money.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {
    @Test
    fun `formatting cents to display`() {
        assertEquals("RD$ 0.00", Money.ZERO.format())
        assertEquals("RD$ 1.50", Money.ofCents(150).format())
        assertEquals("RD$ 125.50", Money.ofCents(12550).format())
        assertEquals("RD$ 1000.00", Money.ofCents(100000).format())
    }

    @Test
    fun `arithmetic is exact`() {
        val a = Money.ofCents(12550)
        val b = Money.ofCents(450)
        assertEquals(13000L, (a + b).cents)
        assertEquals(12100L, (a - b).cents)
        assertEquals(25100L, (a * 2).cents)
    }

    @Test
    fun `percent calculation rounds half up`() {
        val total = Money.ofCents(1000) // 10.00
        val ten = total.percent(10) // 1.00
        assertEquals(100L, ten.cents)
        // 18% of 125.50 = 22.59 (22.59 exactly, 22.59 → 2259 cents)
        val m = Money.ofCents(12550)
        val eighteen = m.percent(18)
        assertEquals(2259L, eighteen.cents)
    }

    @Test
    fun `parse handles dot and comma`() {
        assertEquals(12550L, Money.parse("125.50")?.cents)
        assertEquals(12550L, Money.parse("125,50")?.cents)
        assertEquals(100000L, Money.parse("1000")?.cents)
        assertEquals(0L, Money.parse("0")?.cents)
    }

    @Test
    fun `negative amounts`() {
        val neg = Money.ofCents(-500)
        assertTrue(neg.isNegative())
        assertEquals("-RD$ 5.00", neg.format())
    }
}

class SalesEngineTest {

    private fun item(id: Long, priceCents: Long, qty: Double = 1.0, stock: Double = 100.0) =
        CartItem(
            productId = id, name = "P$id", barcode = null,
            unitPrice = Money.ofCents(priceCents), quantity = qty,
            stockAvailable = stock
        )

    @Test
    fun `subtotal sums all items`() {
        val items = listOf(item(1, 1000, 2.0), item(2, 500, 1.0))
        val subtotal = SalesEngine.calculateSubtotal(items)
        assertEquals(2500L, subtotal.cents) // 2*1000 + 1*500 = 2500
    }

    @Test
    fun `tax is percent of subtotal minus discount`() {
        val items = listOf(item(1, 1000, 2.0)) // subtotal 2000
        val calc = SalesEngine.calculateTotal(items, taxPercent = 18)
        // discount = 0, tax = 2000 * 0.18 = 360
        assertEquals(360L, calc.tax.cents)
        assertEquals(2360L, calc.total.cents)
    }

    @Test
    fun `change is null when insufficient payment`() {
        val total = Money.ofCents(5000)
        val received = Money.ofCents(3000)
        assertNull(SalesEngine.calculateChange(total, received))
    }

    @Test
    fun `change is correct when sufficient`() {
        val total = Money.ofCents(3500)
        val received = Money.ofCents(5000)
        val change = SalesEngine.calculateChange(total, received)
        assertEquals(1500L, change?.cents)
    }

    @Test
    fun `cart validation fails on insufficient stock`() {
        val items = listOf(item(1, 1000, qty = 5.0, stock = 3.0))
        val v = SalesEngine.validateCart(items)
        assertTrue(v.errors.any { it.contains("Stock insuficiente") })
    }

    @Test
    fun `cart validation fails on zero quantity`() {
        val items = listOf(item(1, 1000, qty = 0.0, stock = 100.0))
        val v = SalesEngine.validateCart(items)
        assertTrue(v.errors.any { it.contains("Cantidad inválida") })
    }

    @Test
    fun `next sale number formats correctly`() {
        assertEquals("V-000001", SalesEngine.nextSaleNumber(0))
        assertEquals("V-000002", SalesEngine.nextSaleNumber(1))
        assertEquals("V-001234", SalesEngine.nextSaleNumber(1233))
    }
}
