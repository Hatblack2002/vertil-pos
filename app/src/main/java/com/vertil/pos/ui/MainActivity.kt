package com.vertil.pos.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vertil.pos.di.ServiceLocator
import com.vertil.pos.security.Permission
import com.vertil.pos.ui.screens.*
import com.vertil.pos.ui.theme.VertilPosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (!ServiceLocator.isInitialized()) ServiceLocator.init(applicationContext)
        enableEdgeToEdge()
        setContent { VertilPosTheme { RootScreen() } }
    }
}

private enum class Tab(val label: String, val icon: ImageVector, val perm: Permission? = null) {
    HOME("Inicio", Icons.Filled.Dashboard),
    POS("POS", Icons.Filled.PointOfSale, Permission.POS_ACCESS),
    PRODUCTS("Productos", Icons.Filled.Inventory2, Permission.PRODUCTS_VIEW),
    INVENTORY("Stock", Icons.Filled.Warning, Permission.INVENTORY_VIEW),
    SALES("Ventas", Icons.Filled.Receipt, Permission.SALES_VIEW),
    CASH("Caja", Icons.Filled.AccountBalanceWallet, Permission.CASH_VIEW),
    SETTINGS("Ajustes", Icons.Filled.Settings)
}

@Composable
private fun RootScreen(vm: PosViewModel = viewModel()) {
    val loggedIn by vm.loginState.collectAsState()
    var tab by remember { mutableStateOf(Tab.HOME) }
    var showScanner by remember { mutableStateOf(false) }
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(vm.pos.collectAsState().value.snackbar) {
        vm.pos.value.snackbar?.let { snackbarHost.showSnackbar(it); vm.consumeSnack() }
    }

    if (!loggedIn) {
        val loginErr by vm.loginError.collectAsState()
        LoginScreen(
            onLogin = { u, p -> vm.login(u, p); return@LoginScreen false },
            loginError = loginErr
        )
        return
    }

    // Scanner overlay
    if (showScanner) {
        ScannerScreen(
            onBarcodeDetected = { code ->
                // Buscar producto por barcode y agregar al carrito
                vm.addToCartByBarcode(code)
            },
            onBack = { showScanner = false }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val visibleTabs = Tab.values().filter { it.perm == null || vm.hasPermission(it.perm) }
                visibleTabs.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            AnimatedContent(targetState = tab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab") { t ->
                when (t) {
                    Tab.HOME -> DashboardScreen(vm, onOpenPos = { tab = Tab.POS }, contentPadding = padding)
                    Tab.POS -> PosScreen(vm, onScan = { showScanner = true }, contentPadding = padding)
                    Tab.PRODUCTS -> ProductsScreen(vm, contentPadding = padding)
                    Tab.INVENTORY -> InventoryScreen(vm, contentPadding = padding)
                    Tab.SALES -> SalesScreen(vm, contentPadding = padding)
                    Tab.CASH -> CashScreen(vm, contentPadding = padding)
                    Tab.SETTINGS -> SettingsScreen(vm, contentPadding = padding)
                }
            }
        }
    }
}
