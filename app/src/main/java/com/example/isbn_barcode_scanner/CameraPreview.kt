package com.example.isbn_barcode_scanner

import android.graphics.Point
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
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
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executors

private class Rect (points: Array<Point>) {
    val corners = ArrayList<Offset>()

    init {
        points.forEach { point ->
            corners.add(Offset(point.x.toFloat(), point.y.toFloat()))
        }
        corners.add(Offset(points[0].x.toFloat(), points[0].y.toFloat()))
    }
}



@Composable
fun CameraPreviewScreen() {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = Executors.newSingleThreadExecutor()
    val context = LocalContext.current
    // val preview = Preview.Builder().build()
    val previewView = remember {
        PreviewView(context)
    }

    val decodedIsbn = remember {
        mutableStateOf("")
    }

    val barcodeCorners = remember {
        mutableStateListOf<Offset>()
    }

    val cameraController = LifecycleCameraController(context)

    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_EAN_13)
        .build()
    val barcodeScanner = BarcodeScanning.getClient(options)

    cameraController.setImageAnalysisAnalyzer(
        cameraExecutor,
        MlKitAnalyzer(
            listOf(barcodeScanner),
            COORDINATE_SYSTEM_VIEW_REFERENCED,
            cameraExecutor
        ) { result: MlKitAnalyzer.Result? ->
            val barcodeResults = result?.getValue(barcodeScanner)
            if ((barcodeResults == null) ||
                (barcodeResults.size == 0) ||
                (barcodeResults.first() == null)
            ) {
                if (decodedIsbn.value.isNotEmpty()) decodedIsbn.value = ""
                if(barcodeCorners.isNotEmpty()) barcodeCorners.clear()
            }else {
                decodedIsbn.value = barcodeResults[0].rawValue.toString()

                val codeBounds = Rect(barcodeResults[0].cornerPoints!!)

                val prevCodeBounds = barcodeCorners.toList()

                val avgBounds = ArrayList<Offset>()

                if(prevCodeBounds.isNotEmpty()){
                    for (i in 0..4){
                        avgBounds.add(Offset(codeBounds.corners[i].x + (prevCodeBounds[i].x - codeBounds.corners[i].x) * 0.33f, codeBounds.corners[i].y + (prevCodeBounds[i].y - codeBounds.corners[i].y) * 0.33f))

                    }
                }else{
                    avgBounds.addAll(codeBounds.corners)
                }
                barcodeCorners.clear()
                barcodeCorners.addAll(avgBounds)
            }
        }
    )

    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController

    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

    Canvas(modifier = Modifier) {
        drawPoints(barcodeCorners, pointMode = PointMode.Polygon, color= Color.Cyan, strokeWidth = 10f)
    }

    Column(
        Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally) {


        Surface(modifier = Modifier, color = Color.Transparent) {
            OutlinedText(
                text = if (decodedIsbn.value.isNotEmpty()) "ISBN: ${decodedIsbn.value}" else "",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 15.dp)
            )
        }


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
                    drawStyle = Stroke(width = outlineWidth, join = StrokeJoin.Round)
                )
            )
        )
    }
}