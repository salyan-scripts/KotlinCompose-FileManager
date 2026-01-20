package com.salyan.filemanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FileManagerScreen()
                }
            }
        }
    }
}

@Composable
fun FileManagerScreen() {
    val context = LocalContext.current
    val filesState = remember { mutableStateOf(listOf<File>()) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                context.findActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        } else {
            loadFiles(filesState)
        }
    }

    LazyColumn {
        items(filesState.value) { file ->
            Text(
                text = "${file.name} - ${if (file.isDirectory) "Pasta" else "Arquivo"} - ${dateFormat.format(Date(file.lastModified()))}"
            )
        }
    }
}

private fun loadFiles(filesState: androidx.compose.runtime.MutableState<List<File>>) {
    val root = Environment.getExternalStorageDirectory()
    root.listFiles()?.let { files ->
        filesState.value = files.toList().sortedBy { it.name }
    }
}

// Extensão para encontrar Activity
import android.app.Activity
fun androidx.compose.ui.platform.LocalContext.findActivity(): Activity? {
    var current = current
    while (current is android.content.ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
