package com.vertil.pos.security

/**
 * Roles de usuario.
 */
enum class Role(val label: String) {
    ADMIN("Administrador"),
    MANAGER("Gerente"),
    CASHIER("Cajero")
}

/**
 * Permisos granulares. La verificación se hace en el Core, no en UI.
 */
enum class Permission {
    POS_ACCESS,
    PRODUCTS_VIEW, PRODUCTS_CREATE, PRODUCTS_EDIT, PRODUCTS_DELETE,
    INVENTORY_VIEW, INVENTORY_ADJUST,
    SALES_VIEW, SALES_REFUND, SALES_CANCEL,
    CASH_OPEN, CASH_CLOSE, CASH_VIEW,
    CUSTOMERS_VIEW, CUSTOMERS_EDIT,
    SUPPLIERS_VIEW, SUPPLIERS_EDIT,
    REPORTS_VIEW,
    USERS_MANAGE,
    SETTINGS_EDIT,
    BACKUP_MANAGE,
    AUDIT_VIEW
}

/**
 * Matriz de permisos por rol.
 * La UI puede ocultar botones, pero el Core debe verificar SIEMPRE.
 */
object RolePermissions {

    private val matrix: Map<Role, Set<Permission>> = mapOf(
        Role.ADMIN to Permission.values().toSet(),
        Role.MANAGER to setOf(
            Permission.POS_ACCESS,
            Permission.PRODUCTS_VIEW, Permission.PRODUCTS_CREATE, Permission.PRODUCTS_EDIT,
            Permission.INVENTORY_VIEW, Permission.INVENTORY_ADJUST,
            Permission.SALES_VIEW, Permission.SALES_REFUND,
            Permission.CASH_OPEN, Permission.CASH_CLOSE, Permission.CASH_VIEW,
            Permission.CUSTOMERS_VIEW, Permission.CUSTOMERS_EDIT,
            Permission.SUPPLIERS_VIEW, Permission.SUPPLIERS_EDIT,
            Permission.REPORTS_VIEW,
            Permission.AUDIT_VIEW
        ),
        Role.CASHIER to setOf(
            Permission.POS_ACCESS,
            Permission.PRODUCTS_VIEW,
            Permission.SALES_VIEW,
            Permission.CASH_OPEN, Permission.CASH_VIEW,
            Permission.CUSTOMERS_VIEW
        )
    )

    fun can(role: Role, permission: Permission): Boolean =
        matrix[role]?.contains(permission) ?: false

    fun permissionsFor(role: Role): Set<Permission> = matrix[role] ?: emptySet()
}
