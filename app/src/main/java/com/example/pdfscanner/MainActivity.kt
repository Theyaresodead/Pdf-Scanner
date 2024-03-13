package com.example.pdfscanner

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pdfscanner.ui.theme.PdfScannerTheme
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Creating options for the application to enable all the capabilities of the application
        val options= GmsDocumentScannerOptions.Builder().setScannerMode(SCANNER_MODE_FULL).setGalleryImportAllowed(true)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF).build()
        val scanner= GmsDocumentScanning.getClient(options)
        setContent {
            PdfScannerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var imageUris by remember{
                        mutableStateOf<List<Uri>>(emptyList())
                    }
                       val scannerLauncher= rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult()
                          , onResult = {
                                   if(it.resultCode== RESULT_OK){
                                       val result=GmsDocumentScanningResult.fromActivityResultIntent(it.data)
                                       imageUris= result?.pages?.map { it.imageUri }?: emptyList()
                                       result?.pdf?.let { pdf ->
                                           val fos=FileOutputStream(File(filesDir,"scan.pdf"))
                                           contentResolver.openInputStream(pdf.uri)?.use {
                                               it.copyTo(fos)
                                           }
                                       }
                                   }
                           } )
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        imageUris.forEach { uris ->
                            AsyncImage(model = uris, contentDescription = null, contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth())
                        }
                        Button(onClick = {scanner.getStartScanIntent(this@MainActivity).addOnFailureListener{
                              Toast.makeText(applicationContext,it.message,Toast.LENGTH_SHORT).show()
                        }.addOnSuccessListener {
                            scannerLauncher.launch(
                                IntentSenderRequest.Builder(it).build()
                            )
                        } }) {
                            Text(text = "Scan Pdf")
                            
                        }
                    }
                }
            }
        }
    }
}
