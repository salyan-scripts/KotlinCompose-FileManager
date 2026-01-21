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
import androidx.compose.material.icons.filled.*
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
    var showHidden by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("name") } // "name", "date", "size"

    // Solicitar permissão
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
            updateFiles(currentPath, showHidden, sortBy) { files = it }
        } else {
            ActivityCompat.requestPermissions(context as MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    // Atualizar arquivos quando path, showHidden ou sortBy mudar
    LaunchedEffect(currentPath, showHidden, sortBy) {
        if (permissionGranted) {
            updateFiles(currentPath, showHidden, sortBy) { files = it }
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
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ordenar por nome") },
                            onClick = { sortBy = "name"; showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Ordenar por data") },
                            onClick = { sortBy = "date"; showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Ordenar por tamanho") },
                            onClick = { sortBy = "size"; showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(if (showHidden) "Ocultar arquivos ocultos" else "Exibir arquivos ocultos") },
                            onClick = { showHidden = !showHidden; showMenu = false }
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (!permissionGranted) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aguardando permissão de armazenamento...")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(files) { file ->
                    ListItem(
                        headlineContent = { Text(file.name) },
                        supportingContent = {
                            Text(
                                if (file.isDirectory) "Pasta • ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(file.lastModified()))}"
                                else "${file.length() / 1024} KB • ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(file.lastModified()))}"
                            )
                        },
                        leadingContent = {
                            if (file.isDirectory) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFFA500))
                            } else {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        modifier = Modifier.clickable {
                            if (file.isDirectory) {
                                currentPath = file
                            }
                        }
                    )
                }
            }
        }
    }
}

fun updateFiles(path: File, showHidden: Boolean, sortBy: String, setFiles: (List<File>) -> Unit) {
    val list = path.listFiles()?.toList() ?: emptyList()
    val filtered = if (showHidden) list else list.filter { !it.name.startsWith(".") }
    val sorted = when (sortBy) {
        "date" -> filtered.sortedByDescending { it.lastModified() }
        "size" -> filtered.sortedByDescending { it.length() }
        else -> filtered.sortedBy { it.name.lowercase() }
    }
    setFiles(sorted)
}
