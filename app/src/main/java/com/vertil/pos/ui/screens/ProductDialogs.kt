package com.vertil.pos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vertil.pos.data.entity.ProductEntity
import com.vertil.pos.ui.theme.VpPrimary

/**
 * Diálogo para crear un producto a partir de un código de barras escaneado
 * que no existe en la DB.
 *
 * El barcode llega pre-llenado y NO editable (es el código detectado).
 * El usuario completa nombre, precio, stock y unidad.
 */
@Composable
fun CreateProductFromBarcodeDialog(
    barcode: String,
    onDismiss: () -> Unit,
    onCreate: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("unidad") }
    var sku by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Crear producto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Código escaneado: $barcode",
                    style = MaterialTheme.typography.labelMedium,
                    color = VpPrimary
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { },
                    label = { Text("Código de barras") },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del producto *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("SKU (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { c -> c.isDigit() } },
                    label = { Text("Precio de venta (centavos) *") },
                    placeholder = { Text("ej: 12550 = RD$ 125.50") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Stock inicial") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unidad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "* Campos obligatorios",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && price.toLongOrNull() != null) {
                        val product = ProductEntity(
                            id = 0,
                            barcode = barcode,
                            sku = sku.ifBlank { null },
                            name = name.trim(),
                            description = null,
                            categoryId = null,
                            brand = null,
                            purchasePriceCents = 0L,
                            salePriceCents = price.toLong(),
                            stock = stock.toDoubleOrNull() ?: 0.0,
                            minimumStock = 5.0,
                            maximumStock = 100.0,
                            unit = unit.ifBlank { "unidad" },
                            imageUri = null,
                            supplierId = null,
                            active = true,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        onCreate(product)
                    }
                },
                enabled = name.isNotBlank() && price.toLongOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = VpPrimary, contentColor = androidx.compose.ui.graphics.Color.Black)
            ) { Text("Crear y agregar al carrito") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Diálogo para editar el precio de un producto en la DB (persistente).
 * Solo accesible si el usuario tiene permiso PRODUCTS_EDIT.
 */
@Composable
fun EditPriceInDbDialog(
    productName: String,
    currentPriceCents: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val currentDisplay = "%.2f".format(currentPriceCents / 100.0)
    var priceInput by remember { mutableStateOf(currentDisplay) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Editar precio del producto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(productName, style = MaterialTheme.typography.bodyMedium)
            }
        },
        text = {
            Column {
                Text(
                    "Precio actual: RD$ $currentDisplay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Nuevo precio (RD$)") },
                    placeholder = { Text("ej: 150.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                val parsed = priceInput.toDoubleOrNull()
                val cents = parsed?.let { (it * 100).toLong() }
                if (cents != null) {
                    Text(
                        "Se guardará: RD$ ${"%.2f".format(cents / 100.0)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VpPrimary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Este cambio es PERSISTENTE en la base de datos. Las próximas ventas usarán este precio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            val cents = priceInput.toDoubleOrNull()?.let { (it * 100).toLong() }
            Button(
                onClick = { if (cents != null) onConfirm(cents) },
                enabled = cents != null && cents >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = VpPrimary, contentColor = androidx.compose.ui.graphics.Color.Black)
            ) { Text("Guardar precio") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/**
 * Diálogo para editar el precio de un item SÓLO en el carrito (no persiste).
 */
@Composable
fun EditCartItemPriceDialog(
    productName: String,
    currentPriceCents: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val currentDisplay = "%.2f".format(currentPriceCents / 100.0)
    var priceInput by remember { mutableStateOf(currentDisplay) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Precio para esta venta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(productName, style = MaterialTheme.typography.bodyMedium)
            }
        },
        text = {
            Column {
                Text(
                    "Precio en DB: RD$ $currentDisplay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("Precio solo para esta venta (RD$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Este cambio NO se guarda en la DB. Solo afecta a esta venta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            val cents = priceInput.toDoubleOrNull()?.let { (it * 100).toLong() }
            Button(
                onClick = { if (cents != null) onConfirm(cents) },
                enabled = cents != null && cents >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = VpPrimary, contentColor = androidx.compose.ui.graphics.Color.Black)
            ) { Text("Aplicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
