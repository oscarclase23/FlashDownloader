package com.dam2.flashdownloader.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dam2.flashdownloader.platform.ClipboardManager

/**
 * Componente de FABs flotantes con detección de portapapeles
 * Muestra un FAB secundario cuando detecta una URL en el portapapeles
 */
@Composable
fun ClipboardFloatingActionButtons(
    clipboardManager: ClipboardManager,
    onAddDownload: () -> Unit,
    onPasteUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Observar URL del portapapeles
    val clipboardUrl by clipboardManager.clipboardUrl.collectAsState()

    // Iniciar/detener monitoreo con el ciclo de vida
    LaunchedEffect(Unit) {
        clipboardManager.startMonitoring()
    }

    DisposableEffect(Unit) {
        onDispose {
            clipboardManager.stopMonitoring()
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // FAB de portapapeles (solo visible si hay URL detectada)
        AnimatedVisibility(
            visible = clipboardUrl != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            SmallFloatingActionButton(
                onClick = {
                    clipboardUrl?.let { url ->
                        onPasteUrl(url)
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Pegar URL del portapapeles"
                )
            }
        }

        // FAB principal (siempre visible)
        FloatingActionButton(
            onClick = onAddDownload
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Nueva descarga"
            )
        }
    }
}