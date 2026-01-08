package com.dam2.flashdownloader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.dam2.flashdownloader.platform.ClipboardManager
import com.dam2.flashdownloader.ui.components.ClipboardFloatingActionButtons

/**
 * Pantalla principal de descargas con detección de portapapeles
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    clipboardManager: ClipboardManager,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var urlFromClipboard by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("FlashDownloader") }
            )
        },
        floatingActionButton = {
            ClipboardFloatingActionButtons(
                clipboardManager = clipboardManager,
                onAddDownload = {
                    urlFromClipboard = null
                    showAddDialog = true
                },
                onPasteUrl = { url ->
                    urlFromClipboard = url
                    showAddDialog = true
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Aquí va tu lista de descargas

            // Mostrar diálogo de agregar descarga
            if (showAddDialog) {
                AddDownloadDialog(
                    initialUrl = urlFromClipboard,
                    onDismiss = {
                        showAddDialog = false
                        urlFromClipboard = null
                    },
                    onConfirm = { url, fileName ->
                        // Agregar descarga
                        showAddDialog = false
                        urlFromClipboard = null
                    }
                )
            }
        }
    }
}

@Composable
private fun AddDownloadDialog(
    initialUrl: String?,
    onDismiss: () -> Unit,
    onConfirm: (url: String, fileName: String) -> Unit
) {
    var url by remember { mutableStateOf(initialUrl ?: "") }
    var fileName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva descarga") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Nombre del archivo") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(url, fileName) },
                enabled = url.isNotBlank() && fileName.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
