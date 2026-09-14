package com.vertil.pos.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.vertil.pos.scanner.BarcodeScannerController
import com.vertil.pos.ui.theme.VpPrimary
import com.vertil.pos.ui.theme.VpSecondary

/**
 * Pantalla de escaneo V1.0.1.
 *
 * Comportamiento:
 *  - La cámara permanece activa todo el tiempo.
 *  - Cada código detectado se busca en la DB y se agrega al carrito en vivo.
 *  - La lista de "escaneados recientemente" se muestra en el panel inferior.
 *  - Si el código no existe, se ofrece crear el producto.
 *  - El usuario pulsa "Terminar escaneo" para volver al POS con el carrito actualizado.
 */
@Composable
fun ScannerScreen(
    onBarcodeDetected: (String) -> com.vertil.pos.ui.ScanResult,
    onCreateProductForBarcode: (String) -> Unit,
    onClose: () -> Unit
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var error by remember { mutableStateOf<String?>(null) }
    val scannedItems = remember { mutableStateListOf<com.vertil.pos.ui.ScanResult>() }
    val listState = rememberLazyListState()

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permLauncher.launch(Manifest.permission.CAMERA)
    }

    val scanner = remember {
        BarcodeScannerController(
            context = ctx,
            onBarcode = { event ->
                val result = onBarcodeDetected(event.code)
                scannedItems.add(0, result)
                if (scannedItems.size > 20) {
                    scannedItems.removeAt(scannedItems.size - 1)
                }
                if (result.productName == null) {
                    // Producto no encontrado — abrir diálogo de creación
                    onCreateProductForBarcode(event.code)
                }
            },
            onError = { msg -> error = msg }
        )
    }

    // Auto-scroll al top cuando se añade un item
    LaunchedEffect(scannedItems.size) {
        if (scannedItems.isNotEmpty()) listState.animateScrollToItem(0)
    }

    DisposableEffect(Unit) {
        onDispose { scanner.stop() }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Escanear productos", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("La cámara sigue activa — escanea varios productos", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
            }
            // Indicador "en vivo"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(VpPrimary.copy(alpha = 0.25f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("EN VIVO", color = VpPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        // Cámara
        Box(modifier = Modifier.weight(1.4f).fillMaxWidth()) {
            if (hasPermission) {
                AndroidView(
                    factory = { context ->
                        PreviewView(context).also { previewView ->
                            scanner.startCamera(previewView, lifecycleOwner)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                // Marco de escaneo centrado
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .align(Alignment.Center)
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Transparent)
                )
                // Bordes del marco
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .align(Alignment.Center)
                        .height(180.dp)
                ) {
                    // Bordes (4 esquinas)
                    Box(modifier = Modifier.fillMaxSize().padding(0.dp)) {
                        // top-left
                        Box(Modifier.align(Alignment.TopStart).size(28.dp, 4.dp).background(VpPrimary))
                        Box(Modifier.align(Alignment.TopStart).size(4.dp, 28.dp).background(VpPrimary))
                        // top-right
                        Box(Modifier.align(Alignment.TopEnd).size(28.dp, 4.dp).background(VpPrimary))
                        Box(Modifier.align(Alignment.TopEnd).size(4.dp, 28.dp).background(VpPrimary))
                        // bottom-left
                        Box(Modifier.align(Alignment.BottomStart).size(28.dp, 4.dp).background(VpPrimary))
                        Box(Modifier.align(Alignment.BottomStart).size(4.dp, 28.dp).background(VpPrimary))
                        // bottom-right
                        Box(Modifier.align(Alignment.BottomEnd).size(28.dp, 4.dp).background(VpPrimary))
                        Box(Modifier.align(Alignment.BottomEnd).size(4.dp, 28.dp).background(VpPrimary))
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Permiso de cámara requerido", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("VERTIL POS necesita la cámara para escanear códigos de barras.", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { permLauncher.launch(Manifest.permission.CAMERA) }, colors = ButtonDefaults.buttonColors(containerColor = VpPrimary, contentColor = Color.Black)) {
                        Text("Conceder permiso")
                    }
                }
            }
        }

        // Panel inferior con lista de escaneados
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = VpPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Escaneados", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    if (scannedItems.isNotEmpty()) {
                        Text("${scannedItems.size}", color = VpPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(Modifier.height(8.dp))
                }

                if (scannedItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Apunta la cámara al código de barras.\n\nFormatos soportados:\nEAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, ITF, Codabar, QR, DataMatrix",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp),
                        state = listState
                    ) {
                        items(scannedItems, key = { it.id }) { item ->
                            ScannedItemRow(item)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        }
                    }
                }

                // Botón Terminar
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VpPrimary, contentColor = Color.Black)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Terminar escaneo y ver carrito", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ScannedItemRow(item: com.vertil.pos.ui.ScanResult) {
    val accent = if (item.productName != null) VpPrimary else VpSecondary
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador de estado
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(50))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (item.productName != null) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.productName ?: "Producto no encontrado",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                "Código: ${item.code} · ${item.format}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
            if (item.priceFormatted != null) {
                Text(
                    item.priceFormatted,
                    color = VpPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (item.addedToCart) {
            Text(
                "✓ agregado",
                color = VpPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
