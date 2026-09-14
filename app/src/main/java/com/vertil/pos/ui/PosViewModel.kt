package com.vertil.pos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vertil.pos.core.engine.CartItem
import com.vertil.pos.core.engine.SaleCalculation
import com.vertil.pos.core.engine.SaleService
import com.vertil.pos.core.engine.SalesEngine
import com.vertil.pos.core.money.Money
import com.vertil.pos.data.entity.ProductEntity
import com.vertil.pos.di.ServiceLocator
import com.vertil.pos.security.LoginResult
import com.vertil.pos.security.Permission
import com.vertil.pos.security.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Resultado estructurado de un escaneo individual.
 * Sirve para mostrar feedback en vivo en la pantalla del escáner.
 */
data class ScanResult(
    val id: String = UUID.randomUUID().toString(),
    val code: String,
    val format: String = "",
    val productName: String?,
    val priceFormatted: String?,
    val addedToCart: Boolean,
    val error: String? = null
)

data class PosUiState(
    val cart: List<CartItem> = emptyList(),
    val calculation: SaleCalculation = SaleCalculation(Money.ZERO, Money.ZERO, Money.ZERO, Money.ZERO),
    val received: Money = Money.ZERO,
    val paymentMethod: SaleService.PaymentMethod = SaleService.PaymentMethod.CASH,
    val taxPercent: Int = 0,
    val isProcessing: Boolean = false,
    val lastError: String? = null,
    val lastSaleNumber: String? = null,
    val snackbar: String? = null
)

data class HomeUiState(
    val todaySalesCents: Long = 0L,
    val monthSalesCents: Long = 0L,
    val todayTransactions: Int = 0,
    val cashOpen: Boolean = false,
    val cashBalanceCents: Long = 0L,
    val lowStockCount: Int = 0,
    val topProductName: String? = null
)

class PosViewModel : ViewModel() {

    private val db = ServiceLocator.db
    private val auth = ServiceLocator.auth
    private val saleService = ServiceLocator.saleService
    private val cashService = ServiceLocator.cashService

    private val _pos = MutableStateFlow(PosUiState())
    val pos: StateFlow<PosUiState> = _pos.asStateFlow()

    private val _home = MutableStateFlow(HomeUiState())
    val home: StateFlow<HomeUiState> = _home.asStateFlow()

    private val _products = MutableStateFlow<List<ProductEntity>>(emptyList())
    val products: StateFlow<List<ProductEntity>> = _products.asStateFlow()

    private val _lowStock = MutableStateFlow<List<ProductEntity>>(emptyList())
    val lowStock: StateFlow<List<ProductEntity>> = _lowStock.asStateFlow()

    private val _loginState = MutableStateFlow<Boolean>(false)
    val loginState: StateFlow<Boolean> = _loginState.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    init {
        observeProducts()
        observeLowStock()
        refreshHome()
    }

    private fun observeProducts() {
        viewModelScope.launch {
            db.productDao().observeAll().collectLatest {
                _products.value = it
            }
        }
    }

    private fun observeLowStock() {
        viewModelScope.launch {
            db.productDao().observeLowStock().collectLatest {
                _lowStock.value = it
            }
        }
    }

    fun refreshHome() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val startToday = startOfDay(now)
            val endToday = endOfDay(now)
            val startMonth = startOfMonth(now)
            val todaySales = db.saleDao().sumTotalCentsBetween(startToday, endToday)
            val monthSales = db.saleDao().sumTotalCentsBetween(startMonth, now)
            val todayTx = db.saleDao().getByDateRange(startToday, endToday).size
            val cashOpen = db.cashDao().getOpenSession() != null
            val cashBalance = db.cashDao().getOpenSession()?.let {
                db.cashDao().sumMovements(it.id) + it.openingBalanceCents
            } ?: 0L
            _home.value = HomeUiState(
                todaySalesCents = todaySales,
                monthSalesCents = monthSales,
                todayTransactions = todayTx,
                cashOpen = cashOpen,
                cashBalanceCents = cashBalance,
                lowStockCount = _lowStock.value.size
            )
        }
    }

    // === Login ===
    fun login(user: String, pass: String) {
        viewModelScope.launch {
            val result = auth.login(user, pass)
            when (result) {
                is LoginResult.Success -> { _loginState.value = true; _loginError.value = null }
                is LoginResult.Failure -> { _loginError.value = result.message }
            }
        }
    }

    fun logout() { auth.logout(); _loginState.value = false }

    fun hasPermission(p: Permission): Boolean = auth.hasPermission(p)

    // === POS / Cart ===
    fun addToCart(product: ProductEntity, qty: Double = 1.0) {
        val current = _pos.value.cart.toMutableList()
        val existing = current.firstOrNull { it.productId == product.id }
        if (existing != null) {
            val newQty = existing.quantity + qty
            current.remove(existing)
            current.add(existing.copy(quantity = newQty))
        } else {
            current.add(CartItem(
                productId = product.id,
                name = product.name,
                barcode = product.barcode,
                unitPrice = Money.ofCents(product.salePriceCents),
                quantity = qty,
                stockAvailable = product.stock
            ))
        }
        recalculate(current)
    }

    fun updateQuantity(productId: Long, delta: Double) {
        val current = _pos.value.cart.toMutableList()
        val item = current.firstOrNull { it.productId == productId } ?: return
        val newQty = item.quantity + delta
        if (newQty <= 0) {
            current.remove(item)
        } else {
            current.remove(item)
            current.add(item.copy(quantity = newQty))
        }
        recalculate(current)
    }

    fun removeFromCart(productId: Long) {
        val current = _pos.value.cart.filterNot { it.productId == productId }
        recalculate(current)
    }

    fun clearCart() { recalculate(emptyList()) }

    fun setReceived(m: Money) { _pos.value = _pos.value.copy(received = m) }
    fun setPaymentMethod(m: SaleService.PaymentMethod) { _pos.value = _pos.value.copy(paymentMethod = m) }
    fun setTaxPercent(p: Int) {
        _pos.value = _pos.value.copy(taxPercent = p)
        recalculate(_pos.value.cart)
    }

    private fun recalculate(cart: List<CartItem>) {
        val calc = SalesEngine.calculateTotal(cart, _pos.value.taxPercent)
        _pos.value = _pos.value.copy(cart = cart, calculation = calc)
    }

    fun checkout() {
        if (_pos.value.isProcessing) return
        val state = _pos.value
        if (state.cart.isEmpty()) {
            _pos.value = state.copy(lastError = "Carrito vacío"); return
        }
        _pos.value = state.copy(isProcessing = true, lastError = null)
        viewModelScope.launch {
            val result = saleService.processSale(
                SaleService.SaleRequest(
                    items = state.cart,
                    customerId = null,
                    paymentMethod = state.paymentMethod,
                    received = state.received,
                    taxPercent = state.taxPercent,
                    notes = null
                )
            )
            when (result) {
                is com.vertil.pos.core.engine.SaleResult.Success -> {
                    val sale = db.saleDao().getById(result.saleId)
                    _pos.value = PosUiState(
                        lastSaleNumber = sale?.number,
                        snackbar = "Venta ${sale?.number} completada: ${result.calculation.total.format()}"
                    )
                    refreshHome()
                }
                is com.vertil.pos.core.engine.SaleResult.Failure -> {
                    _pos.value = _pos.value.copy(isProcessing = false, lastError = result.message)
                }
            }
        }
    }

    fun consumeSnack() { _pos.value = _pos.value.copy(snackbar = null) }

    /**
     * Busca producto por código de barras (vía escáner) y lo agrega al carrito.
     * Devuelve un [ScanResult] estructurado para que la UI del escáner muestre
     * feedback en vivo (producto encontrado / no encontrado / precio).
     *
     * Si el producto no existe, el caller puede decidir abrir el diálogo de creación.
     */
    fun addToCartByBarcode(code: String, format: String = ""): ScanResult {
        // Esta función es sincrónica porque se llama desde el callback del escáner
        // y necesitamos el resultado inmediato. La búsqueda DB se hace en runBlocking
        // para evitar que el callback vuelva antes de tener el producto.
        return kotlinx.coroutines.runBlocking {
            try {
                val p = db.productDao().getByBarcode(code)
                if (p != null) {
                    addToCart(p)
                    _pos.value = _pos.value.copy(snackbar = "Agregado: ${p.name}")
                    ScanResult(
                        code = code,
                        format = format,
                        productName = p.name,
                        priceFormatted = Money.ofCents(p.salePriceCents).format(),
                        addedToCart = true
                    )
                } else {
                    ScanResult(
                        code = code,
                        format = format,
                        productName = null,
                        priceFormatted = null,
                        addedToCart = false,
                        error = "Producto no encontrado"
                    )
                }
            } catch (t: Throwable) {
                ScanResult(
                    code = code,
                    format = format,
                    productName = null,
                    priceFormatted = null,
                    addedToCart = false,
                    error = t.message
                )
            }
        }
    }

    /**
     * Actualiza el precio unitario de un item en el carrito.
     * No persiste en DB — solo afecta esta venta.
     */
    fun updateCartItemPrice(productId: Long, newPriceCents: Long) {
        val current = _pos.value.cart.toMutableList()
        val item = current.firstOrNull { it.productId == productId } ?: return
        val newPrice = Money.ofCents(newPriceCents)
        current.remove(item)
        current.add(item.copy(unitPrice = newPrice))
        recalculate(current)
        _pos.value = _pos.value.copy(snackbar = "Precio actualizado: ${item.name} → ${newPrice.format()}")
    }

    /**
     * Persiste un nuevo precio de venta para un producto en la DB.
     * Solo ADMIN y MANAGER pueden hacerlo.
     * A partir de la próxima vez que se escanee el producto, el precio será el nuevo.
     */
    fun updateProductPriceInDb(productId: Long, newSalePriceCents: Long) {
        if (!hasPermission(Permission.PRODUCTS_EDIT)) {
            _pos.value = _pos.value.copy(snackbar = "Sin permiso para editar productos")
            return
        }
        viewModelScope.launch {
            val p = db.productDao().getById(productId) ?: return@launch
            db.productDao().update(p.copy(
                salePriceCents = newSalePriceCents,
                updatedAt = System.currentTimeMillis()
            ))
            ServiceLocator.audit.log("PRODUCT_PRICE_UPDATED", "Product", productId.toString(), mapOf(
                "oldPrice" to Money.ofCents(p.salePriceCents).format(),
                "newPrice" to Money.ofCents(newSalePriceCents).format()
            ))
            // También actualizar el item en el carrito si está presente
            val cartItem = _pos.value.cart.firstOrNull { it.productId == productId }
            if (cartItem != null) {
                updateCartItemPrice(productId, newSalePriceCents)
            } else {
                _pos.value = _pos.value.copy(snackbar = "Precio del producto actualizado: ${Money.ofCents(newSalePriceCents).format()}")
            }
        }
    }

    // === Cash ===
    fun openCash(opening: Money) {
        viewModelScope.launch {
            try {
                cashService.openCash(opening)
                refreshHome()
            } catch (t: Throwable) { _pos.value = _pos.value.copy(lastError = t.message) }
        }
    }

    fun closeCash(counted: Money) {
        viewModelScope.launch {
            val r = cashService.closeCash(counted)
            refreshHome()
        }
    }

    // === Product lookup by barcode (for scanner) ===
    suspend fun findByBarcode(code: String): ProductEntity? = db.productDao().getByBarcode(code)
    suspend fun searchProducts(q: String): List<ProductEntity> = db.productDao().search(q)

    // === Product CRUD ===
    fun createProduct(p: ProductEntity, callback: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = db.productDao().insert(p)
            ServiceLocator.audit.log("PRODUCT_CREATED", "Product", id.toString(), mapOf("name" to p.name))
            callback(id)
        }
    }

    fun updateProduct(p: ProductEntity) {
        viewModelScope.launch {
            db.productDao().update(p.copy(updatedAt = System.currentTimeMillis()))
            ServiceLocator.audit.log("PRODUCT_UPDATED", "Product", p.id.toString(), mapOf("name" to p.name))
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            db.productDao().softDelete(id, System.currentTimeMillis())
            ServiceLocator.audit.log("PRODUCT_DELETED", "Product", id.toString())
        }
    }

    // === Helpers de tiempo ===
    private fun startOfDay(ts: Long): Long {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = ts; set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }
        return cal.timeInMillis
    }
    private fun endOfDay(ts: Long): Long = startOfDay(ts) + 24 * 3600 * 1000 - 1
    private fun startOfMonth(ts: Long): Long {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = ts; set(java.util.Calendar.DAY_OF_MONTH, 1); set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }
        return cal.timeInMillis
    }
}
