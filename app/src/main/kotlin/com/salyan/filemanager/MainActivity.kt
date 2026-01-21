package com.salyan.filemanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FileManagerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerApp() {
    val context = LocalContext.current
    var currentPath by remember { mutableStateOf(Environment.getExternalStorageDirectory()) }
    var files by remember { mutableStateOf(emptyList<File>()) }
    var permissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
            files = currentPath.listFiles()?.toList()?.sortedBy { it.name.lowercase() } ?: emptyList()
        } else {
            ActivityCompat.requestPermissions(context as MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    LaunchedEffect(currentPath) {
        if (permissionGranted) {
            files = currentPath.listFiles()?.toList()?.sortedBy { it.name.lowercase() } ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        currentPath.absolutePath.removePrefix(Environment.getExternalStorageDirectory().absolutePath).ifEmpty { "Memória interna" }
                    )
                },
                navigationIcon = {
                    if (currentPath != Environment.getExternalStorageDirectory()) {
                        IconButton(onClick = { currentPath = currentPath.parentFile ?: currentPath }) {
                            Icon(Icons.Default.ArrowBack, "Voltar")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menu futuro */ }) {
                        Icon(Icons.Default.MoreVert, "Mais")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!permissionGranted) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Aguardando permissão...")
            }
        } else {
            LazyColumn(Modifier.padding(innerPadding)) {
                items(files) { file ->
                    ListItem(
                        headlineContent = { Text(file.name) },
                        supportingContent = {
                            Text(
                                if (file.isDirectory) "Pasta • ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(file.lastModified()))}"
                                else "${(file.length() / 1024)} KB • ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(file.lastModified()))}"
                            )
                        },
                        leadingContent = {
                            if (file.isDirectory) {
                                Icon(Icons.Default.Folder, null, tint = Color(0xFFFFA500))
                            } else {
                                Icon(Icons.Default.InsertDriveFile, null, tint = Color.Gray)
                            }
                        },
                        modifier = Modifier.clickable {
                            if (file.isDirectory) currentPath = file
                            // TODO: abrir arquivo
                        }
                    )
                }
            }
        }
    }
}
