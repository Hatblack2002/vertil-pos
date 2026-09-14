package com.vertil.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vertil.pos.core.money.Money
import com.vertil.pos.ui.PosViewModel
import com.vertil.pos.ui.theme.VpBg
import com.vertil.pos.ui.theme.VpBgGradient
import com.vertil.pos.ui.theme.VpPrimary
import com.vertil.pos.ui.theme.VpSecondary

@Composable
fun DashboardScreen(vm: PosViewModel, onOpenPos: () -> Unit, contentPadding: PaddingValues = PaddingValues()) {
    val home by vm.home.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(VpBgGradient, VpBg)))
            .padding(contentPadding)
            .padding(20.dp)
    ) {
        Text("VERTIL POS", color = VpPrimary, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        Text("Buenos días", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        // KPIs
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("VENTAS HOY", Money.ofCents(home.todaySalesCents).format(), Modifier.weight(1f), VpPrimary)
            KpiCard("TRANSACCIONES", home.todayTransactions.toString(), Modifier.weight(1f), VpSecondary)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("CAJA", Money.ofCents(home.cashBalanceCents).format(), Modifier.weight(1f), if (home.cashOpen) VpPrimary else MaterialTheme.colorScheme.error)
            KpiCard("STOCK BAJO", "${home.lowStockCount} prod.", Modifier.weight(1f), if (home.lowStockCount > 0) VpSecondary else VpPrimary)
        }
        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onOpenPos,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VpPrimary, contentColor = Color.Black)
        ) { Text("ABRIR POS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(20.dp))
        Text("Ventas del mes: ${Money.ofCents(home.monthSalesCents).format()}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun KpiCard(label: String, value: String, modifier: Modifier = Modifier, accent: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(value, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}
