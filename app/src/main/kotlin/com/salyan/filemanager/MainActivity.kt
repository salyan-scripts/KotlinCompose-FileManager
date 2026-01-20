# ... (mantenha o código anterior e substitua a parte do Scaffold e FileItem por isso)

# No topo, adicione esses imports extras se necessário
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

# Dentro de FileManagerApp():
var showHidden by remember { mutableStateOf(false) }
var sortBy by remember { mutableStateOf("name") } // "name", "date", "size"

# Filtrar e ordenar arquivos
val displayedFiles = files
    .filter { showHidden || !it.name.startsWith(".") }
    .sortedBy {
        when (sortBy) {
            "date" -> it.lastModified()
            "size" -> if (it.isFile) it.length() else 0L
            else -> it.name.lowercase()
        }
    }

// No Scaffold, actions:
actions = {
    IconButton(onClick = { sortBy = if (sortBy == "name") "date" else if (sortBy == "date") "size" else "name" }) {
        Icon(Icons.Default.Sort, contentDescription = "Ordenar")
    }
    IconButton(onClick = { showHidden = !showHidden }) {
        Icon(if (showHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = "Ocultos")
    }
    IconButton(onClick = { /* TODO: Popup completo com Nova pasta, etc */ }) {
        Icon(Icons.Default.MoreVert, contentDescription = "Mais")
    }
}

# No FileItem, adicionar subtitle com detalhes
ListItem(
    headlineContent = { Text(file.name) },
    supportingContent = {
        Text(
            if (file.isDirectory) "${file.listFiles()?.size ?: 0} itens • ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(file.lastModified()))}"
            else "${file.length() / 1024} KB • ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(file.lastModified()))}"
        )
    },
    ...
)
