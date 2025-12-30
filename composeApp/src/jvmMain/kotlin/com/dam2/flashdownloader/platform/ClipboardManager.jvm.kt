package com.dam2.flashdownloader.platform

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.util.regex.Pattern

/**
 * Implementación Desktop (JVM) del ClipboardManager
 * Usa AWT Toolkit para acceder al portapapeles del sistema
 */
actual class ClipboardManager {

    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _clipboardUrl = MutableStateFlow<String?>(null)
    actual val clipboardUrl: StateFlow<String?> = _clipboardUrl.asStateFlow()

    private var monitoringJob: Job? = null
    private var lastClipboardContent: String? = null

    /**
     * Inicia el monitoreo del portapapeles con polling cada medio segundo
     * Desktop tiene mejor rendimiento, por lo que podemos verificar más frecuentemente
     */
    actual fun startMonitoring() {
        if (monitoringJob?.isActive == true) return

        monitoringJob = scope.launch {
            while (isActive) {
                checkClipboard()
                delay(500) // Verificar cada medio segundo en Desktop
            }
        }
    }

    /**
     * Detiene el monitoreo del portapapeles
     */
    actual fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _clipboardUrl.value = null
        lastClipboardContent = null
    }

    /**
     * Verifica el contenido actual del portapapeles usando AWT
     */
    private fun checkClipboard() {
        try {
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                val text = clipboard.getData(DataFlavor.stringFlavor) as? String

                // Solo procesar si el contenido cambió
                if (text != null && text != lastClipboardContent) {
                    lastClipboardContent = text

                    if (isValidUrl(text)) {
                        _clipboardUrl.value = text
                    } else {
                        _clipboardUrl.value = null
                    }
                }
            }
        } catch (e: UnsupportedFlavorException) {
            // El portapapeles no contiene texto
        } catch (e: Exception) {
            // Ignorar otros errores de acceso
        }
    }

    /**
     * Valida si una cadena es una URL HTTP/HTTPS válida
     */
    actual fun isValidUrl(text: String): Boolean {
        val urlPattern = Pattern.compile(
            "^(https?://)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
            Pattern.CASE_INSENSITIVE
        )
        return urlPattern.matcher(text).matches()
    }
}
