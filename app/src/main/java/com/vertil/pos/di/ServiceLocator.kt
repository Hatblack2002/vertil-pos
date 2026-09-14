package com.vertil.pos.di

import android.content.Context
import com.vertil.pos.backup.BackupService
import com.vertil.pos.core.engine.AuditLogger
import com.vertil.pos.core.engine.CashService
import com.vertil.pos.core.engine.SaleService
import com.vertil.pos.data.db.PosDatabase
import com.vertil.pos.security.AuthenticationManager

/**
 * ServiceLocator manual.
 */
object ServiceLocator {

    lateinit var db: PosDatabase
        private set
    lateinit var auth: AuthenticationManager
        private set
    lateinit var audit: AuditLogger
        private set
    lateinit var saleService: SaleService
        private set
    lateinit var cashService: CashService
        private set
    lateinit var backupService: BackupService
        private set

    @Volatile private var initialized = false
    fun isInitialized(): Boolean = initialized

    @Synchronized
    fun init(context: Context) {
        if (initialized) return
        val appCtx = context.applicationContext
        db = PosDatabase.get(appCtx)
        auth = AuthenticationManager(db.userDao())
        audit = AuditLogger(db.auditDao(), auth)
        saleService = SaleService(db, auth, audit)
        cashService = CashService(db, auth, audit)
        backupService = BackupService(db)
        initialized = true
    }

    /**
     * Seed inicial: crea admin/admin si no hay usuarios.
     */
    suspend fun seedIfEmpty() {
        if (db.userDao().count() == 0) {
            auth.createUser("admin", "admin123", "Administrador", com.vertil.pos.security.Role.ADMIN)
        }
        if (db.categoryDao().getAll().isEmpty()) {
            val cats = listOf("Bebidas", "Alimentos", "Limpieza", "Higiene", "Snacks", "Electrónica", "Hogar", "Ropa", "Otros")
            cats.forEach { db.categoryDao().insert(com.vertil.pos.data.entity.CategoryEntity(name = it)) }
        }
    }
}
