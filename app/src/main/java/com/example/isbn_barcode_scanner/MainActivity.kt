package com.example.isbn_barcode_scanner

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.isbn_barcode_scanner.ui.theme.ISBNBarcodeScannerTheme

import org.opencv.android.OpenCVLoader;

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISBNBarcodeScannerTheme {
                if (OpenCVLoader.initLocal()) {
                    Log.i("LOADED", "OpenCV loaded successfully");
                    (Toast.makeText(this, "OpenCV initialization successful!", Toast.LENGTH_LONG)).show();
                } else {
                    Log.e("LOADED", "OpenCV initialization failed!");
                    (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
                }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android Test")
                }
            }
        }


    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ISBNBarcodeScannerTheme {
        Greeting("Android Test")
    }
}