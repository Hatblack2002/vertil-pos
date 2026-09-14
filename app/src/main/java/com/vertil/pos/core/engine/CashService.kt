package com.vertil.pos.core.engine

import androidx.room.withTransaction
import com.vertil.pos.core.money.Money
import com.vertil.pos.data.db.PosDatabase
import com.vertil.pos.data.entity.CashMovementEntity
import com.vertil.pos.data.entity.CashSessionEntity
import com.vertil.pos.security.AuthenticationManager
import com.vertil.pos.security.Permission

/**
 * CashService — apertura/cierre de caja + movimientos.
 */
class CashService(
    private val db: PosDatabase,
    private val auth: AuthenticationManager,
    private val audit: AuditLogger
) {
    suspend fun openCash(opening: Money): Long {
        auth.requirePermission(Permission.CASH_OPEN)
        val session = auth.current.value ?: throw IllegalStateException("No session")
        val existing = db.cashDao().getOpenSession()
        if (existing != null) throw IllegalStateException("Ya hay una caja abierta")
        val sessionId = db.cashDao().insert(CashSessionEntity(
            userId = session.userId,
            openingBalanceCents = opening.cents
        ))
        db.cashDao().insertMovement(CashMovementEntity(
            cashSessionId = sessionId,
            type = "OPEN",
            amountCents = opening.cents,
            reason = "Fondo inicial",
            userId = session.userId
        ))
        audit.log("CASH_OPENED", "CashSession", sessionId.toString(), mapOf("opening" to opening.format()))
        return sessionId
    }

    suspend fun closeCash(counted: Money): CloseResult {
        auth.requirePermission(Permission.CASH_CLOSE)
        val session = auth.current.value ?: throw IllegalStateException("No session")
        val open = db.cashDao().getOpenSession() ?: return CloseResult.Failure("No hay caja abierta")
        val movements = db.cashDao().sumMovements(open.id)
        val expected = CashEngine.computeExpected(Money.ofCents(open.openingBalanceCents), Money.ofCents(movements))
        val difference = CashEngine.computeDifference(expected, counted)

        db.withTransaction {
            db.cashDao().update(open.copy(
                closingBalanceCents = counted.cents,
                expectedBalanceCents = expected.cents,
                differenceCents = difference.cents,
                closeTime = System.currentTimeMillis(),
                status = "CLOSED"
            ))
            db.cashDao().insertMovement(CashMovementEntity(
                cashSessionId = open.id,
                type = "CLOSE",
                amountCents = -counted.cents,
                reason = "Cierre de caja",
                userId = session.userId
            ))
        }
        audit.log("CASH_CLOSED", "CashSession", open.id.toString(), mapOf(
            "expected" to expected.format(),
            "counted" to counted.format(),
            "difference" to difference.format()
        ))
        return CloseResult.Success(expected, counted, difference)
    }

    suspend fun addCashMovement(type: String, amount: Money, reason: String?) {
        val open = db.cashDao().getOpenSession() ?: throw IllegalStateException("No hay caja abierta")
        val session = auth.current.value ?: throw IllegalStateException("No session")
        db.cashDao().insertMovement(CashMovementEntity(
            cashSessionId = open.id,
            type = type,
            amountCents = amount.cents,
            reason = reason,
            userId = session.userId
        ))
    }

    suspend fun getOpenSession(): CashSessionEntity? = db.cashDao().getOpenSession()
}

sealed class CloseResult {
    data class Success(val expected: Money, val counted: Money, val difference: Money) : CloseResult()
    data class Failure(val message: String) : CloseResult()
}
