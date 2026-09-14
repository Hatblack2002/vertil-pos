package com.vertil.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vertil.pos.core.money.Money
import com.vertil.pos.ui.PosViewModel
import com.vertil.pos.ui.theme.VpPrimary
import com.vertil.pos.ui.theme.VpSecondary

@Composable
fun InventoryScreen(vm: PosViewModel, contentPadding: PaddingValues = PaddingValues()) {
    val lowStock by vm.lowStock.collectAsState()
    val products by vm.products.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(contentPadding)) {
        Text("Inventario", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
        // Alertas
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ALERTAS DE STOCK BAJO", color = VpSecondary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("${lowStock.size} productos con stock bajo", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            items(products) { p ->
                val isLow = p.stock <= p.minimumStock
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.name, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                            Text("Stock: ${p.stock} ${p.unit} · Min: ${p.minimumStock}", color = if (isLow) VpSecondary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Text("Valor: ${Money.ofCents((p.salePriceCents * p.stock.toLong())).format()}", color = VpPrimary, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SalesScreen(vm: PosViewModel, contentPadding: PaddingValues = PaddingValues()) {
    var sales by remember { mutableStateOf<List<com.vertil.pos.data.entity.SaleEntity>>(emptyList()) }
    LaunchedEffect(Unit) {
        sales = com.vertil.pos.di.ServiceLocator.db.saleDao().getByDateRange(0, System.currentTimeMillis()).reversed()
    }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(contentPadding)) {
        Text("Ventas", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
        Text("${sales.size} ventas registradas", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            items(sales) { sale ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sale.number, color = VpPrimary, fontWeight = FontWeight.Bold)
                            Text("${sale.paymentMethod} · ${sale.status}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Text(java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(sale.createdAt)), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                        Text(Money.ofCents(sale.totalCents).format(), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CashScreen(vm: PosViewModel, contentPadding: PaddingValues = PaddingValues()) {
    val home by vm.home.collectAsState()
    var showOpen by remember { mutableStateOf(false) }
    var showClose by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(contentPadding).padding(16.dp)) {
        Text("Caja", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("ESTADO ACTUAL", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Text(if (home.cashOpen) "CAJA ABIERTA" else "CAJA CERRADA", color = if (home.cashOpen) VpPrimary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Balance: ${Money.ofCents(home.cashBalanceCents).format()}", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.height(20.dp))
        if (!home.cashOpen) {
            Button(onClick = { showOpen = true }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = VpPrimary, contentColor = androidx.compose.ui.graphics.Color.Black)) {
                Text("ABRIR CAJA", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(onClick = { showClose = true }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = VpSecondary, contentColor = androidx.compose.ui.graphics.Color.Black)) {
                Text("CERRAR CAJA", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showOpen) {
        AlertDialog(
            onDismissRequest = { showOpen = false },
            title = { Text("Abrir caja") },
            text = { OutlinedTextField(value = amountInput, onValueChange = { amountInput = it.filter { c -> c.isDigit() } }, label = { Text("Fondo inicial (centavos)") }, singleLine = true) },
            confirmButton = { Button(onClick = {
                val m = Money.ofCents(amountInput.toLongOrNull() ?: 0L)
                vm.openCash(m); showOpen = false; amountInput = ""
            }) { Text("Abrir") } },
            dismissButton = { TextButton(onClick = { showOpen = false }) { Text("Cancelar") } }
        )
    }
    if (showClose) {
        AlertDialog(
            onDismissRequest = { showClose = false },
            title = { Text("Cerrar caja") },
            text = { OutlinedTextField(value = amountInput, onValueChange = { amountInput = it.filter { c -> c.isDigit() } }, label = { Text("Efectivo contado (centavos)") }, singleLine = true) },
            confirmButton = { Button(onClick = {
                val m = Money.ofCents(amountInput.toLongOrNull() ?: 0L)
                vm.closeCash(m); showClose = false; amountInput = ""
            }) { Text("Cerrar") } },
            dismissButton = { TextButton(onClick = { showClose = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun SettingsScreen(vm: PosViewModel, contentPadding: PaddingValues = PaddingValues()) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(contentPadding).padding(16.dp)) {
        Text("Configuración", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ACERCA DE", color = VpPrimary, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Text("VERTIL POS", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Versión 1.0.0", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text("Modo offline-first", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("Moneda: RD$ (DOP)", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text("Motor de base de datos: Room (SQLite)", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text("Escáner: ML Kit Barcode", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = androidx.compose.ui.graphics.Color.White)) {
            Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
        }
    }
}
