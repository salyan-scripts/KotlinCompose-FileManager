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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.platform.LocalUriHandler
import android.os.storage.StorageManager
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
colorScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
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
    var files by remember { mutableStateOf(listOf<File>()) }
    var permissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
            files = currentPath.listFiles()?.toList() ?: emptyList()
        } else {
            ActivityCompat.requestPermissions(context as MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    LaunchedEffect(currentPath) {
        if (permissionGranted) {
            files = currentPath.listFiles()?.toList() ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentPath.name.ifEmpty { "Memória interna" }) },
var searchText by remember { mutableStateOf(TextFieldValue("")) }
TextField(value = searchText, onValueChange = { searchText = it ; updateFiles(...) }, placeholder = { Text("Buscar") })
                navigationIcon = {
                    if (currentPath != Environment.getExternalStorageDirectory()) {
                        IconButton(onClick = { currentPath = currentPath.parentFile ?: currentPath }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                actions = {
IconButton(onClick = { /* Lista volumes */ getStorageVolumes(context) }) { Icon(Icons.Default.SdCard, "Armazenamentos") }
                    IconButton(onClick = { /* Menu: ordenar, nova pasta */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mais")
                    }
                }
            )
        }
    ) { padding ->
        if (!permissionGranted) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aguardando permissão...")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
AnimatedVisibility(visible = true, enter = fadeIn()) {
                items(files) { file ->
if (files.isEmpty()) { item { Text("Pasta vazia") } }
                    FileItem(file) {
                        if (file.isDirectory) {
                            currentPath = file
                        } else {
                            // TODO: Abrir arquivo
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileItem(file: File, onClick: () -> Unit) {
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
        modifier = Modifier.clickable(onClick = onClick)
    )
}
@Composable
fun FileItem(file: File, onClick: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
        modifier = Modifier
            .clickable(onClick = onClick)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { showMenu = true })
            }
    )

    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text(file.name) },
var searchText by remember { mutableStateOf(TextFieldValue("")) }
TextField(value = searchText, onValueChange = { searchText = it ; updateFiles(...) }, placeholder = { Text("Buscar") })
            confirmButton = {},
            text = {
                Column {
                    TextButton(onClick = { showMenu = false; if (file.isFile) context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(file), "*/*")) }) { Text("Abrir") }
                    TextButton(onClick = { showMenu = false; /* TODO: Dialog para destino */ copyFile(file, File(file.parent, "copia_${file.name}")) }) { Text("Copiar") }
                    TextButton(onClick = { showMenu = false; /* TODO: Dialog para destino */ moveFile(file, File(file.parent, "movido_${file.name}")) }) { Text("Mover") }
                    TextButton(onClick = { file.delete(); showMenu = false }) { Text("Excluir") }
                    TextButton(onClick = { showMenu = false; /* TODO: Dialog para novo nome */ file.renameTo(File(file.parent, "novo_nome")) }) { Text("Renomear") }
                    TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_SEND).setType("*/*").putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))) ; showMenu = false }) { Text("Compartilhar") }
                    TextButton(onClick = { scope.launch { zipFile(file) } ; showMenu = false }) { Text("Compactar") }
                }
            }
        )
    }
}

fun copyFile(source: File, dest: File) {
    source.inputStream().use { input ->
        dest.outputStream().use { output ->
            input.copyTo(output)
        }
    }
}

fun moveFile(source: File, dest: File) {
    copyFile(source, dest)
    source.delete()
}

suspend fun zipFile(file: File) {
    val zipFile = File(file.parent, "${file.name}.zip")
    ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
        FileInputStream(file).use { input ->
            zip.putNextEntry(ZipEntry(file.name))
            input.copyTo(zip)
        }
    }
}
fun getStorageVolumes(context: Context): List<File> {
    val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    return storageManager.storageVolumes.mapNotNull { it.getPathFile() }
}
