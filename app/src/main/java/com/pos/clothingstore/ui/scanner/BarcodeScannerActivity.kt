package com.pos.clothingstore.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.pos.clothingstore.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * شاشة مسح الباركود
 */
class BarcodeScannerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BarcodeScanner"
        const val EXTRA_SCANNED_BARCODE = "SCANNED_BARCODE"
        private const val CAMERA_REQUEST = 1001
    }

    private lateinit var cameraExecutor: ExecutorService
    private var scanCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (hasCameraPermission()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "إذن الكاميرا مطلوب", Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(
                        findViewById<androidx.camera.view.PreviewView>(R.id.preview_view)?.surfaceProvider
                    )
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImage(imageProxy)
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e(TAG, "فشل تشغيل الكاميرا: ${e.message}")
                Toast.makeText(this, "فشل تشغيل الكاميرا", Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        if (scanCompleted) { imageProxy.close(); return }

        val mediaImage = imageProxy.image
        if (mediaImage == null) { imageProxy.close(); return }

        try {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty() && !scanCompleted) {
                        val value = barcodes[0].rawValue
                        if (!value.isNullOrEmpty()) {
                            scanCompleted = true
                            onBarcodeScanned(value)
                        }
                    }
                }
                .addOnFailureListener { Log.d(TAG, "لم يتم العثور على باركود") }
                .addOnCompleteListener { imageProxy.close() }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ تحليل: ${e.message}")
            imageProxy.close()
        }
    }

    private fun onBarcodeScanned(barcode: String) {
        vibrate()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val resultIntent = intent.putExtra(EXTRA_SCANNED_BARCODE, barcode)
            setResult(RESULT_OK, resultIntent)
            finish()
        }, 300)
    }

    private fun vibrate() {
        val vibrator = getSystemService(Vibrator::class.java)
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
    }

    override fun onPause() { super.onPause(); scanCompleted = false }

    override fun onDestroy() { super.onDestroy(); cameraExecutor.shutdown() }
}
