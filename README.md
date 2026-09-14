# VERTIL POS

Sistema profesional de punto de venta, inventario y gestión comercial para Android.

## Versión

`1.0.0` (versionCode `1`)

## Características

### POS
- Pantalla optimizada para uso rápido en teléfono
- Botón de escaneo accesible
- Carrito con cantidades, descuentos y subtotales
- Cobro con efectivo/tarjeta/transferencia/otro
- Cálculo de cambio automático
- Venta transaccional atómica (Room withTransaction)

### Escáner de códigos de barras
- **REAL** con CameraX + ML Kit Barcode Scanning
- Formatos: EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, ITF, Codabar
- Autofocus, prevención de lecturas duplicadas (debounce 800ms)
- Escaneo continuo sin reabrir cámara
- Búsqueda automática en DB

### Inventario
- Stock actualizado por ventas reales (no hardcodeado)
- Movimientos: SALE, PURCHASE, ADJUSTMENT, RETURN, LOSS, MANUAL_ENTRY, MANUAL_EXIT
- Alertas de stock bajo
- Historial de movimientos por producto

### Caja
- Apertura con fondo inicial
- Cierre con efectivo contado + diferencia
- Movimientos: SALE, REFUND, CASH_IN, CASH_OUT, OPEN, CLOSE
- Balance en tiempo real

### Productos
- CRUD completo
- Búsqueda por barcode, SKU, nombre, marca
- Índices en Room para queries rápidas
- Soft-delete (active=false)

### Seguridad
- Autenticación local con bcrypt
- Roles: ADMIN, MANAGER, CASHIER
- Permisos verificados en Core (no solo UI)
- Auditoría append-only

### Auditoría
- Registro de operaciones sensibles: LOGIN, SALE_CREATED, CASH_OPENED, etc.
- Usuario, fecha, entidad, ID, detalles

### Backup
- Export/import JSON versionado (vertil-pos-backup-v1)
- Validación de formato
- Confirmación requerida para sobrescribir

### Dinero exacto
- Clase `Money` basada en `Long` (centavos)
- Sin Float/Double para cálculos financieros
- Operaciones deterministas con BigDecimal para impuestos

## Arquitectura

```
VERTIL POS
├── Core (engines)
│   ├── SalesEngine (cálculos puros)
│   ├── InventoryEngine
│   ├── CashEngine
│   ├── SaleService (transacción atómica)
│   ├── CashService
│   └── AuditLogger
├── Database (Room)
│   ├── 12 entidades normalizadas
│   ├── DAOs con índices
│   └── Transactions
├── Scanner (CameraX + ML Kit)
├── Security (bcrypt + roles + permisos)
├── Backup (JSON)
├── UI (Jetpack Compose)
└── DI (ServiceLocator)
```

## Stack técnico

- Kotlin 1.9.24, AGP 8.5.2, Gradle 8.9
- Jetpack Compose (BOM 2024.08.00), Material 3
- Room 2.6.1
- CameraX 1.3.4
- ML Kit Barcode Scanning 17.3.0
- jbcrypt 0.4
- minSdk 26 / targetSdk 34 / compileSdk 34

## Compilar

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Tests

| Suite | Tests |
|---|---|
| SalesEngineTest (Money + SalesEngine) | 12 |
| MoreEnginesTest (Inventory + Cash + Roles) | 18 |
| **Total** | **30** (todos PASS) |

Cobertura: cálculo de subtotal/descuento/impuesto/total/cambio, validación de stock, movimientos de inventario, cálculo de caja, matriz de permisos por rol.

## Login demo

- Usuario: `admin`
- Contraseña: `admin123`

Se crea automáticamente al primer inicio si no hay usuarios.

## Limitaciones conocidas

1. **No probado en dispositivo físico**: el escáner con cámara real y el flujo end-to-end requieren dispositivo Android.
2. **INTERNET permiso merged**: el AAR de ML Kit añade `INTERNET` al manifest. La app NO hace peticiones de red en runtime; el permiso está presente porque ML Kit lo declara. V1.1 lo eliminará usando ML Kit offline-only.
3. **Sin impresora Bluetooth**: el ReceiptGenerator está preparado pero la integración con hardware Bluetooth/USB se pospone.
4. **Sin sincronización cloud**: V1 es offline-first. La arquitectura está preparada para SyncEngine en V2.

## Roadmap

- V1.1: Receivers Bluetooth, eliminación de permisos innecesarios
- V2: SyncEngine para sincronización entre dispositivos
- V3: Multi-dispositivo
- V4: Cloud opcional
- V5: Analytics/AI

## Licencia

Uso interno — VERTIL POS.
