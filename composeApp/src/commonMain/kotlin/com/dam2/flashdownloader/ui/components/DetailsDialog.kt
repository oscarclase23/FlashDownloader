package com.dam2.flashdownloader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dam2.flashdownloader.domain.model.DownloadItem
import com.dam2.flashdownloader.domain.model.DownloadStatus
import com.dam2.flashdownloader.ui.utils.formatBytes
import com.dam2.flashdownloader.ui.utils.formatSpeed
import com.dam2.flashdownloader.ui.utils.formatTime
import java.text.SimpleDateFormat
import java.util.*

/**
 * Diálogo que muestra información detallada de una descarga
 * Incluye metadatos, estado actual, estadísticas y errores si los hay
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsDialog(
    download: DownloadItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier.width(500.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Barra superior
                TopAppBar(
                    title = { Text("Detalles de Descarga") },
                    actions = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Cerrar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Contenido
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Información básica
                    SectionTitle("Información Básica")
                    DetailRow("Nombre", download.fileName)
                    DetailRow("URL", download.url, isUrl = true)
                    DetailRow("Categoría", "${download.category.iconName} ${download.category.displayName}")
                    DetailRow("Prioridad", download.priority.displayName)

                    HorizontalDivider()

                    // Estadísticas de descarga
                    SectionTitle("Estadísticas")
                    DetailRow("Tamaño total", download.totalSize.formatBytes())
                    DetailRow("Descargado", download.downloadedBytes.formatBytes())

                    val progress = if (download.totalSize > 0) {
                        (download.downloadedBytes.toFloat() / download.totalSize.toFloat() * 100).toInt()
                    } else {
                        0
                    }
                    DetailRow("Progreso", "$progress%")

                    // Información específica según el estado
                    when (val status = download.status) {
                        is DownloadStatus.Downloading -> {
                            DetailRow("Velocidad actual", status.speed.formatSpeed())
                            DetailRow("Tiempo estimado", status.estimatedTimeRemaining.formatTime())
                            DetailRow("Tiempo transcurrido", status.totalElapsedSeconds.formatTime())
                        }

                        is DownloadStatus.Paused -> {
                            DetailRow("Estado", "⏸️ Pausado")
                            DetailRow("Tiempo transcurrido", status.elapsedSeconds.formatTime())
                        }

                        is DownloadStatus.Completed -> {
                            DetailRow("Estado", "✅ Completado")
                            DetailRow("Fecha de completado", formatTimestamp(download.createdAt))
                            status.calculatedHash?.let {
                                DetailRow("Hash SHA-256", it.take(16) + "...")
                            }
                        }

                        is DownloadStatus.Failed -> {
                            DetailRow("Estado", "❌ Fallido", isError = true)
                            DetailRow("Error", status.error, isError = true)
                        }

                        is DownloadStatus.Queued -> {
                            DetailRow("Estado", "⏳ En cola")
                            if (status.bytesDownloaded > 0) {
                                DetailRow("Descarga parcial", status.bytesDownloaded.formatBytes())
                            }
                        }

                        is DownloadStatus.Cancelled -> {
                            DetailRow("Estado", "🚫 Cancelado")
                        }
                    }

                    HorizontalDivider()

                    // Información técnica
                    SectionTitle("Información Técnica")
                    download.localPath?.let {
                        DetailRow("Ruta local", it)
                    }

                    if (download.speedLimit != null) {
                        DetailRow("Límite de velocidad", download.speedLimit.formatSpeed())
                    } else {
                        DetailRow("Límite de velocidad", "Sin límite")
                    }

                    // Metadata adicional
                    download.metadata.mimeType?.let {
                        DetailRow("Tipo MIME", it)
                    }

                    download.metadata.serverFileName?.let {
                        if (it != download.fileName) {
                            DetailRow("Nombre en servidor", it)
                        }
                    }

                    DetailRow("Soporta reanudación",
                        if (download.metadata.supportsRangeRequests) "✅ Sí" else "❌ No"
                    )

                    HorizontalDivider()

                    // ID (útil para debugging)
                    DetailRow("ID", download.id, isSmall = true)
                    DetailRow("Fecha de creación", formatTimestamp(download.createdAt), isSmall = true)
                }
            }
        }
    }
}

/**
 * Título de sección
 */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

/**
 * Fila de detalle con label y valor
 */
@Composable
private fun DetailRow(
    label: String,
    value: String,
    isUrl: Boolean = false,
    isError: Boolean = false,
    isSmall: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = if (isSmall) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = if (isSmall) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = when {
                isError -> MaterialTheme.colorScheme.error
                isUrl -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
            maxLines = if (isUrl) 2 else Int.MAX_VALUE
        )
    }
}

/**
 * Formatea un timestamp a formato legible
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
