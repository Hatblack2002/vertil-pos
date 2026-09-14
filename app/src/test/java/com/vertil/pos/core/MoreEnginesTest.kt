package com.vertil.pos.core

import com.vertil.pos.core.engine.InventoryEngine
import com.vertil.pos.core.engine.InventoryType
import com.vertil.pos.core.engine.CashEngine
import com.vertil.pos.core.money.Money
import com.vertil.pos.security.Permission
import com.vertil.pos.security.Role
import com.vertil.pos.security.RolePermissions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryEngineTest {

    @Test
    fun `SALE reduces stock`() {
        val new = InventoryEngine.computeNewStock(10.0, InventoryType.SALE, 3.0)
        assertEquals(7.0, new, 0.001)
    }

    @Test
    fun `PURCHASE increases stock`() {
        val new = InventoryEngine.computeNewStock(10.0, InventoryType.PURCHASE, 5.0)
        assertEquals(15.0, new, 0.001)
    }

    @Test
    fun `RETURN increases stock`() {
        val new = InventoryEngine.computeNewStock(10.0, InventoryType.RETURN, 2.0)
        assertEquals(12.0, new, 0.001)
    }

    @Test
    fun `isLowStock when stock equals minimum`() {
        assertTrue(InventoryEngine.isLowStock(5.0, 5.0))
    }

    @Test
    fun `isLowStock false when above minimum`() {
        assertFalse(InventoryEngine.isLowStock(10.0, 5.0))
    }

    @Test
    fun `validateMovement fails when stock would go negative`() {
        val v = InventoryEngine.validateMovement(2.0, InventoryType.SALE, 5.0, "TestProd")
        assertTrue(v.errors.any { it.contains("insuficiente") })
    }

    @Test
    fun `validateMovement passes when enough stock`() {
        val v = InventoryEngine.validateMovement(10.0, InventoryType.SALE, 5.0, "TestProd")
        assertTrue(v.isValid)
    }

    @Test
    fun `validateMovement fails on zero quantity`() {
        val v = InventoryEngine.validateMovement(10.0, InventoryType.SALE, 0.0, "TestProd")
        assertFalse(v.isValid)
    }
}

class CashEngineTest {

    @Test
    fun `expected balance is opening plus movements`() {
        val opening = Money.ofCents(5000)
        val movements = Money.ofCents(3000)
        val expected = CashEngine.computeExpected(opening, movements)
        assertEquals(8000L, expected.cents)
    }

    @Test
    fun `difference is counted minus expected`() {
        val expected = Money.ofCents(8000)
        val counted = Money.ofCents(7500)
        val diff = CashEngine.computeDifference(expected, counted)
        assertEquals(-500L, diff.cents)
    }

    @Test
    fun `negative opening is invalid`() {
        val v = CashEngine.validateOpen(Money.ofCents(-100))
        assertFalse(v.isValid)
    }

    @Test
    fun `negative counted is invalid`() {
        val v = CashEngine.validateClose(Money.ofCents(1000), Money.ofCents(-50))
        assertFalse(v.isValid)
    }
}

class RolePermissionsTest {

    @Test
    fun `ADMIN has all permissions`() {
        Permission.values().forEach { p ->
            assertTrue("ADMIN should have $p", RolePermissions.can(Role.ADMIN, p))
        }
    }

    @Test
    fun `CASHIER cannot manage users`() {
        assertFalse(RolePermissions.can(Role.CASHIER, Permission.USERS_MANAGE))
    }

    @Test
    fun `CASHIER can access POS`() {
        assertTrue(RolePermissions.can(Role.CASHIER, Permission.POS_ACCESS))
    }

    @Test
    fun `MANAGER cannot manage users`() {
        assertFalse(RolePermissions.can(Role.MANAGER, Permission.USERS_MANAGE))
    }

    @Test
    fun `MANAGER can adjust inventory`() {
        assertTrue(RolePermissions.can(Role.MANAGER, Permission.INVENTORY_ADJUST))
    }

    @Test
    fun `CASHIER cannot adjust inventory`() {
        assertFalse(RolePermissions.can(Role.CASHIER, Permission.INVENTORY_ADJUST))
    }
}
