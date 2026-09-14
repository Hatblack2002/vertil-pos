# Changelog

## [1.0.0] - 2026-09-14

### Added
- VERTIL POS — POS + Inventario + Caja
- Money class (Long cents) para cálculos financieros exactos
- Room: 12 entidades normalizadas (products, sales, sale_items, inventory, cash, users, audit, etc.)
- SalesEngine: cálculo de subtotal/descuento/impuesto/total/cambio
- SaleService: venta transaccional atómica (Room withTransaction)
- InventoryEngine: tipos SALE/PURCHASE/ADJUSTMENT/RETURN/LOSS/MANUAL
- CashService: apertura/cierre con diferencia
- Scanner REAL: CameraX + ML Kit Barcode
- AuthenticationManager: bcrypt + roles ADMIN/MANAGER/CASHIER
- RolePermissions: matriz verificada en Core
- AuditLogger: append-only con usuario/acción/entidad/detalles
- BackupService: export/import JSON versionado
- 7 pantallas Compose: Login, Dashboard, POS, Scanner, Products, Inventory, Sales, Cash, Settings
- 30 tests unitarios PASS

### Security
- Contraseñas hasheadas con bcrypt (cost 12)
- Permisos verificados en Core (no solo UI ocultando botones)
- Venta transaccional: nunca stock descontado sin venta registrada

### Known Limitations
- No probado en dispositivo físico
- ML Kit merged INTERNET permiso (no se usa en runtime)
- Sin impresora Bluetooth (capa preparada)
