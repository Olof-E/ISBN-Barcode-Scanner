package com.example.isbn_barcode_scanner

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

typealias scanListener = (code: String) -> Unit

@Composable
fun CameraPreviewScreen() {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = Executors.newSingleThreadExecutor()
    val context = LocalContext.current
    val preview = Preview.Builder().build()
    val previewView = remember {
        PreviewView(context)
    }

    var decodedIsbn by remember {
        mutableStateOf("")
    }

    val imageAnalyzer = ImageAnalysis.Builder()
        .build()
        .also {
            it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { code ->
                decodedIsbn = code
            })
        }

    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageAnalyzer)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

    Column(
        Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally) {


        OutlinedText(
            text = if (decodedIsbn.isNotEmpty())"ISBN: $decodedIsbn" else "",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 15.dp)
        )

        Button(onClick = { }, modifier = Modifier
            .padding(bottom = 20.dp)
            .border(1.5.dp, Color.Black, shape = CircleShape)) {
            Text(text = "Scan", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
        }
    }
}

@Composable
fun OutlinedText(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = 20.sp, fontWeight: FontWeight = FontWeight.Normal, textColor: Color = Color.White, outlineColor: Color = Color.Black, outlineWidth: Float = 3f) {
    Box(modifier = modifier){
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = fontWeight,
            style = LocalTextStyle.current.merge(
                TextStyle(color = textColor,
                    lineHeight = fontSize,
                    fontSize = fontSize,
                    drawStyle = Fill
                )
            )
        )
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = fontWeight,
            style = LocalTextStyle.current.merge(
                TextStyle(color = outlineColor,
                    lineHeight = fontSize,
                    fontSize = fontSize,
                    drawStyle = Stroke(width = outlineWidth, join = StrokeJoin.Bevel)
                )
            )
        )
    }
}


private class BarcodeAnalyzer(private val listener: scanListener) : ImageAnalysis.Analyzer {
    private val options = BarcodeScannerOptions.Builder()
        .enableAllPotentialBarcodes() // Optional
        .build()

    val scanner = BarcodeScanning.getClient(options)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)


            val result = scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val bounds = barcode.boundingBox
                        val corners = barcode.cornerPoints

                        val rawValue = barcode.rawValue

                        val valueType = barcode.valueType
                        // See API reference for complete list of supported types
                        when (valueType) {
                            Barcode.TYPE_ISBN -> {
                                listener(rawValue.toString())
                            }
                        }
                    }
                    if(barcodes.isEmpty()){
                        listener("")
                    }
                    imageProxy.close()
                }
                .addOnFailureListener {}

        }

    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                @Suppress("BlockingMethodInNonBlockingContext")
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }