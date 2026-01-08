package com.dam2.flashdownloader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dam2.flashdownloader.domain.model.DownloadItem
import com.dam2.flashdownloader.presentation.DownloadViewModel
import com.dam2.flashdownloader.ui.components.DetailsDialog
import com.dam2.flashdownloader.ui.components.DownloadListItem

/**
 * Pantalla principal de descargas con soporte para ver detalles
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDownloadScreen(
    viewModel: DownloadViewModel,
    modifier: Modifier = Modifier
) {
    val downloads by viewModel.downloads.collectAsState()
    var selectedDownload by remember { mutableStateOf<DownloadItem?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("FlashDownloader") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (downloads.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No hay descargas",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Lista de descargas
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = downloads,
                        key = { it.id }
                    ) { download ->
                        DownloadListItem(
                            download = download,
                            onPause = { viewModel.pauseDownload(download.id) },
                            onResume = { viewModel.resumeDownload(download.id) },
                            onCancel = { viewModel.cancelDownload(download.id) },
                            onRemove = { viewModel.removeDownload(download.id) },
                            onShowDetails = { selectedDownload = download }
                        )
                    }
                }
            }

            // Diálogo de detalles
            selectedDownload?.let { download ->
                DetailsDialog(
                    download = download,
                    onDismiss = { selectedDownload = null }
                )
            }
        }
    }
}
