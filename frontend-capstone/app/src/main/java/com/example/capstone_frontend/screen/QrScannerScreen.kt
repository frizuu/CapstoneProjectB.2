package com.example.capstone_frontend.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.capstone_frontend.component.AppColor
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun QrScannerScreen(
    onBack: () -> Unit,
    onQrDetected: (String) -> Unit
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onQrDetected = onQrDetected
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .align(Alignment.TopCenter)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = onBack
                ) {
                    Text(
                        text = "← Kembali",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Scan QRIS Merchant",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Arahkan kamera ke QR Code untuk membaca data transaksi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE0E0E0)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(36.dp)
                    .fillMaxWidth()
                    .height(260.dp)
                    .border(
                        width = 3.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(28.dp)
                    )
            )

            Text(
                text = "Scanning otomatis...",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 54.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColor.Background)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Izin Kamera Dibutuhkan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColor.TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Aplikasi membutuhkan kamera untuk memindai QRIS.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColor.TextGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColor.Primary
                )
            ) {
                Text("Berikan Izin Kamera")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onBack) {
                Text("Kembali")
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onQrDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val scannedOnce = remember { AtomicBoolean(false) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also { previewUseCase ->
                            previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val scannerOptions = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()

                    val scanner = BarcodeScanning.getClient(scannerOptions)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image

                        if (mediaImage != null && !scannedOnce.get()) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    val rawValue = barcodes
                                        .firstOrNull()
                                        ?.rawValue

                                    if (!rawValue.isNullOrBlank() &&
                                        scannedOnce.compareAndSet(false, true)
                                    ) {
                                        mainHandler.post {
                                            onQrDetected(rawValue)
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (_: Exception) {
                    }
                },
                ContextCompat.getMainExecutor(context)
            )

            previewView
        }
    )
}