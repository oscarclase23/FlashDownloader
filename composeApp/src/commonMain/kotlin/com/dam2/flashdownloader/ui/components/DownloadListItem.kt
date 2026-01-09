package com.dam2.flashdownloader.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dam2.flashdownloader.domain.model.DownloadItem
import com.dam2.flashdownloader.domain.model.DownloadStatus
import com.dam2.flashdownloader.ui.utils.formatBytes
import com.dam2.flashdownloader.ui.utils.formatSpeed

/**
 * Componente de ítem de descarga con soporte para doble clic
 * Muestra información básica y permite acciones rápidas
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadListItem(
    download: DownloadItem,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRemove: () -> Unit,
    onShowDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* Single click - no action */ },
                onDoubleClick = onShowDetails,
                onLongClick = { showMenu = true }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabecera con nombre y menú
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = download.category.iconName,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = download.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = download.priority.displayName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Botón de menú
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Más opciones")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ver detalles") },
                            onClick = {
                                onShowDetails()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Info, "Detalles")
                            }
                        )

                        HorizontalDivider()

                        when (download.status) {
                            is DownloadStatus.Downloading -> {
                                DropdownMenuItem(
                                    text = { Text("Pausar") },
                                    onClick = {
                                        onPause()
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Pause, "Pausar")
                                    }
                                )
                            }

                            is DownloadStatus.Paused -> {
                                DropdownMenuItem(
                                    text = { Text("Reanudar") },
                                    onClick = {
                                        onResume()
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.PlayArrow, "Reanudar")
                                    }
                                )
                            }

                            else -> {}
                        }

                        if (download.status !is DownloadStatus.Completed) {
                            DropdownMenuItem(
                                text = { Text("Cancelar") },
                                onClick = {
                                    onCancel()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Cancel, "Cancelar")
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                onRemove()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, "Eliminar")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso
            val progress = if (download.totalSize > 0) {
                download.downloadedBytes.toFloat() / download.totalSize.toFloat()
            } else {
                0f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Información de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = getStatusText(download.status),
                    style = MaterialTheme.typography.bodySmall,
                    color = getStatusColor(download.status)
                )

                Text(
                    text = "${download.downloadedBytes.formatBytes()} / ${download.totalSize.formatBytes()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Velocidad (si está descargando)
            if (download.status is DownloadStatus.Downloading) {
                val status = download.status as DownloadStatus.Downloading
                Text(
                    text = "⚡ ${status.speed.formatSpeed()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Obtiene el texto del estado actual
 */
@Composable
private fun getStatusText(status: DownloadStatus): String {
    return when (status) {
        is DownloadStatus.Queued -> "⏳ En cola"
        is DownloadStatus.Downloading -> {
            val progress = if (status.totalBytes > 0) {
                ((status.bytesDownloaded.toFloat() / status.totalBytes.toFloat()) * 100).toInt()
            } else {
                0
            }
            "⬇️ Descargando ($progress%)"
        }
        is DownloadStatus.Paused -> "⏸️ Pausado"
        is DownloadStatus.Completed -> "✅ Completado"
        is DownloadStatus.Failed -> "❌ Error: ${status.error}"
        is DownloadStatus.Cancelled -> "🚫 Cancelado"
    }
}

/**
 * Obtiene el color según el estado
 */
@Composable
private fun getStatusColor(status: DownloadStatus): androidx.compose.ui.graphics.Color {
    return when (status) {
        is DownloadStatus.Downloading -> MaterialTheme.colorScheme.primary
        is DownloadStatus.Completed -> MaterialTheme.colorScheme.tertiary
        is DownloadStatus.Failed -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
