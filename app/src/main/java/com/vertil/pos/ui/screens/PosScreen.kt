package com.vertil.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vertil.pos.core.engine.SaleService
import com.vertil.pos.core.money.Money
import com.vertil.pos.security.Permission
import com.vertil.pos.ui.PosViewModel
import com.vertil.pos.ui.theme.VpPrimary
import com.vertil.pos.ui.theme.VpSecondary

@Composable
fun PosScreen(vm: PosViewModel, onScan: () -> Unit, contentPadding: PaddingValues = PaddingValues()) {
    val state by vm.pos.collectAsState()
    val home by vm.home.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showCheckout by remember { mutableStateOf(false) }
    var receivedInput by remember { mutableStateOf("") }
    var editingCartItem by remember { mutableStateOf<com.vertil.pos.core.engine.CartItem?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(contentPadding)
    ) {
        // Top bar
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("POS", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (vm.hasPermission(Permission.CASH_VIEW)) {
                Text(if (home.cashOpen) "CAJA ABIERTA" else "CAJA CERRADA",
                    color = if (home.cashOpen) VpPrimary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium)
            }
        }

        // Scan + search row — botón de escaneo GRANDE y accesible
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onScan,
                modifier = Modifier.weight(0.45f).height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VpPrimary, contentColor = Color.Black)
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Escanear", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(0.55f),
                placeholder = { Text("Buscar producto…") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Search results (cuando hay query)
        if (searchQuery.isNotBlank()) {
            val results = remember(searchQuery) {
                vm.products.value.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    (it.barcode?.contains(searchQuery) ?: false) ||
                    (it.sku?.contains(searchQuery, ignoreCase = true) ?: false)
                }
            }
            LazyColumn(modifier = Modifier.weight(1f).padding(8.dp)) {
                items(results) { product ->
                    ProductSearchRow(product) { vm.addToCart(product); searchQuery = "" }
                }
            }
        } else {
            // Cart list
            if (state.cart.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Carrito vacío", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("Escanea un producto o búscalo arriba", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).padding(8.dp)) {
                    items(state.cart) { item ->
                        CartItemRow(
                            item = item,
                            canEditPrice = vm.hasPermission(Permission.PRODUCTS_EDIT),
                            onAdd = { vm.updateQuantity(item.productId, 1.0) },
                            onSub = { vm.updateQuantity(item.productId, -1.0) },
                            onRemove = { vm.removeFromCart(item.productId) },
                            onEditPrice = { editingCartItem = item }
                        )
                    }
                }
            }
        }

        // Totales
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Text(state.calculation.subtotal.format(), color = MaterialTheme.colorScheme.onBackground)
                }
                if (state.calculation.discount.cents > 0) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Descuento", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text("- ${state.calculation.discount.format()}", color = MaterialTheme.colorScheme.error)
                    }
                }
                if (state.calculation.tax.cents > 0) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Impuesto", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(state.calculation.tax.format(), color = MaterialTheme.colorScheme.onBackground)
                    }
                }
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("TOTAL", color = VpPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(state.calculation.total.format(), color = VpPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        Button(
            onClick = { showCheckout = true },
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
            enabled = state.cart.isNotEmpty() && !state.isProcessing,
            colors = ButtonDefaults.buttonColors(containerColor = VpSecondary, contentColor = Color.Black)
        ) { Text("COBRAR ${state.calculation.total.format()}", fontWeight = FontWeight.Bold) }
    }

    // === Checkout dialog ===
    if (showCheckout) {
        val total = state.calculation.total
        val received = Money.parse(receivedInput) ?: Money.ZERO
        val change = if (received >= total) received - total else Money.ZERO
        val canComplete = state.paymentMethod != SaleService.PaymentMethod.CASH || received >= total

        AlertDialog(
            onDismissRequest = { showCheckout = false },
            title = { Text("Cobrar ${total.format()}") },
            text = {
                Column {
                    Row {
                        SaleService.PaymentMethod.values().forEach { m ->
                            FilterChip(selected = state.paymentMethod == m, onClick = { vm.setPaymentMethod(m) }, label = { Text(m.label) }, modifier = Modifier.padding(end = 4.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (state.paymentMethod == SaleService.PaymentMethod.CASH) {
                        OutlinedTextField(
                            value = receivedInput,
                            onValueChange = { receivedInput = it },
                            label = { Text("Efectivo recibido") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Cambio: ${change.format()}", color = VpPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (!canComplete) Text("Pago insuficiente", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Método: ${state.paymentMethod.label}", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.setReceived(received)
                        vm.checkout()
                        showCheckout = false
                        receivedInput = ""
                    },
                    enabled = canComplete && !state.isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = VpPrimary, contentColor = Color.Black)
                ) { Text("Confirmar venta") }
            },
            dismissButton = { TextButton(onClick = { showCheckout = false }) { Text("Cancelar") } }
        )
    }

    // === Editar precio del item en carrito ===
    editingCartItem?.let { item ->
        EditCartItemPriceDialog(
            productName = item.name,
            currentPriceCents = item.unitPrice.cents,
            onDismiss = { editingCartItem = null },
            onConfirm = { newCents ->
                vm.updateCartItemPrice(item.productId, newCents)
                editingCartItem = null
            }
        )
    }
}

@Composable
private fun CartItemRow(
    item: com.vertil.pos.core.engine.CartItem,
    canEditPrice: Boolean,
    onAdd: () -> Unit,
    onSub: () -> Unit,
    onRemove: () -> Unit,
    onEditPrice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text(
                        "${item.unitPrice.format()} c/u · stock: ${item.stockAvailable}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = onSub) { Icon(Icons.Filled.Remove, contentDescription = "Restar", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text("${item.quantity.toInt()}", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onAdd) { Icon(Icons.Filled.Add, contentDescription = "Sumar", tint = VpPrimary) }
                Text(item.subtotal.format(), color = VpPrimary, modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                IconButton(onClick = onRemove) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
            }
            // Fila de acciones: editar precio (carrito y DB)
            if (canEditPrice) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEditPrice) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Editar precio (esta venta)", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductSearchRow(product: com.vertil.pos.data.entity.ProductEntity, onAdd: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                Text("${Money.ofCents(product.salePriceCents).format()} · stock: ${product.stock}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = VpPrimary, contentColor = Color.Black)) { Text("Agregar") }
        }
    }
}
