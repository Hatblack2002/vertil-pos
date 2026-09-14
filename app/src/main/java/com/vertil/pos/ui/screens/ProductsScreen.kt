package com.vertil.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vertil.pos.core.money.Money
import com.vertil.pos.data.entity.ProductEntity
import com.vertil.pos.ui.PosViewModel
import com.vertil.pos.ui.theme.VpPrimary

@Composable
fun ProductsScreen(vm: PosViewModel, contentPadding: PaddingValues = PaddingValues()) {
    val products by vm.products.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ProductEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(contentPadding)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Productos", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("${products.size} productos", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            FloatingActionButton(onClick = { showAdd = true }, containerColor = VpPrimary, contentColor = androidx.compose.ui.graphics.Color.Black) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo")
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            items(products) { p ->
                ProductRow(p, onEdit = { editing = p; showAdd = true }, onDelete = { vm.deleteProduct(p.id) })
            }
        }
    }

    if (showAdd) {
        ProductEditDialog(
            initial = editing,
            onDismiss = { showAdd = false; editing = null },
            onSave = { product ->
                if (editing == null) vm.createProduct(product) else vm.updateProduct(product)
                showAdd = false; editing = null
            }
        )
    }
}

@Composable
private fun ProductRow(p: ProductEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(p.name, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                Text("SKU: ${p.sku ?: "—"} · Barcode: ${p.barcode ?: "—"}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text("Precio: ${Money.ofCents(p.salePriceCents).format()} · Stock: ${p.stock} ${p.unit}", color = VpPrimary, style = MaterialTheme.typography.labelMedium)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun ProductEditDialog(initial: ProductEntity?, onDismiss: () -> Unit, onSave: (ProductEntity) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var barcode by remember { mutableStateOf(initial?.barcode ?: "") }
    var sku by remember { mutableStateOf(initial?.sku ?: "") }
    var price by remember { mutableStateOf((initial?.salePriceCents ?: 0L).toString()) }
    var stock by remember { mutableStateOf((initial?.stock ?: 0.0).toString()) }
    var unit by remember { mutableStateOf(initial?.unit ?: "unidad") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Código de barras") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { price = it.filter { c -> c.isDigit() } }, label = { Text("Precio (centavos)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unidad") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            ProductEntity(
                                id = initial?.id ?: 0,
                                barcode = barcode.ifBlank { null },
                                sku = sku.ifBlank { null },
                                name = name,
                                description = initial?.description,
                                categoryId = initial?.categoryId,
                                brand = initial?.brand,
                                purchasePriceCents = initial?.purchasePriceCents ?: 0L,
                                salePriceCents = price.toLongOrNull() ?: 0L,
                                stock = stock.toDoubleOrNull() ?: 0.0,
                                minimumStock = initial?.minimumStock ?: 5.0,
                                maximumStock = initial?.maximumStock ?: 100.0,
                                unit = unit,
                                imageUri = null,
                                supplierId = initial?.supplierId,
                                active = true,
                                createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VpPrimary, contentColor = androidx.compose.ui.graphics.Color.Black)
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
