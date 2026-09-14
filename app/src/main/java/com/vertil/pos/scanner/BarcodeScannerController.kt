package com.vertil.pos.scanner

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * BarcodeScannerController — usa CameraX + ML Kit Barcode Scanning.
 *
 * V1.0.1: mejora el comportamiento del escaneo:
 *  - Escaneo continuo: la cámara permanece activa hasta que el usuario pulsa "Terminar".
 *  - Debounce por código: el MISMO código no se reporta 2 veces en menos de 1500ms,
 *    pero códigos DIFERENTES se reportan inmediatamente.
 *  - Cada lectura dispara el callback con el código y un timestamp.
 */
class BarcodeScannerController(
    private val context: Context,
    private val onBarcode: (ScanEvent) -> Unit,
    private val onError: (String) -> Unit
) {

    /** Evento de escaneo entregado a la UI. */
    data class ScanEvent(
        val code: String,
        val format: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val executor = Executors.newSingleThreadExecutor()
    private val debouncePerCodeMs = 1500L  // mismo código: mínimo 1.5s entre reportes
    private val lastReportByCode = mutableMapOf<String, Long>()

    @Volatile private var scanningEnabled = true

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_ITF,
                Barcode.FORMAT_CODABAR,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_DATA_MATRIX
            )
            .build()
    )

    @SuppressLint("UnsafeOptInUsageError")
    fun startCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor, ::analyzeImage) }

                val selector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analyzer)
                scanningEnabled = true
            } catch (t: Throwable) {
                onError("No se pudo iniciar cámara: ${t.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Pausa el reporte de códigos (manteniendo la cámara activa).
     * Útil mientras el usuario está creando un producto nuevo.
     */
    fun pauseScanning() { scanningEnabled = false }

    /** Reanuda el reporte de códigos. */
    fun resumeScanning() {
        lastReportByCode.clear()
        scanningEnabled = true
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun analyzeImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (!scanningEnabled) {
                    return@addOnSuccessListener
                }
                barcodes.firstOrNull { it.rawValue != null }?.let { barcode ->
                    val value = barcode.rawValue ?: return@let
                    val format = formatLabel(barcode.format)
                    val now = System.currentTimeMillis()
                    val lastTime = lastReportByCode[value] ?: 0L
                    if (now - lastTime >= debouncePerCodeMs) {
                        lastReportByCode[value] = now
                        onBarcode(ScanEvent(code = value, format = format, timestamp = now))
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun formatLabel(format: Int): String = when (format) {
        Barcode.FORMAT_EAN_13 -> "EAN-13"
        Barcode.FORMAT_EAN_8 -> "EAN-8"
        Barcode.FORMAT_UPC_A -> "UPC-A"
        Barcode.FORMAT_UPC_E -> "UPC-E"
        Barcode.FORMAT_CODE_128 -> "CODE-128"
        Barcode.FORMAT_CODE_39 -> "CODE-39"
        Barcode.FORMAT_CODE_93 -> "CODE-93"
        Barcode.FORMAT_ITF -> "ITF"
        Barcode.FORMAT_CODABAR -> "CODABAR"
        Barcode.FORMAT_QR_CODE -> "QR"
        Barcode.FORMAT_DATA_MATRIX -> "DATA-MATRIX"
        else -> "UNKNOWN"
    }

    fun stop() {
        scanningEnabled = false
        try { scanner.close() } catch (_: Throwable) {}
        executor.shutdown()
    }
}
