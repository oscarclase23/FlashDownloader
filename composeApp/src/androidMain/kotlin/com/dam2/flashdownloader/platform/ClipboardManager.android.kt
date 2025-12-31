package com.dam2.flashdownloader.platform

import android.content.ClipboardManager as AndroidClipboardManager
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.regex.Pattern

/**
 * Implementación Android del ClipboardManager
 * Monitoriza el portapapeles del sistema cada segundo
 */
actual class ClipboardManager(private val context: Context) {

    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _clipboardUrl = MutableStateFlow<String?>(null)
    actual val clipboardUrl: StateFlow<String?> = _clipboardUrl.asStateFlow()

    private var monitoringJob: Job? = null
    private var lastClipboardContent: String? = null

    /**
     * Inicia el monitoreo del portapapeles con polling cada segundo
     */
    actual fun startMonitoring() {
        if (monitoringJob?.isActive == true) return

        monitoringJob = scope.launch {
            while (isActive) {
                checkClipboard()
                delay(1000) // Verificar cada segundo
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
     * Verifica el contenido actual del portapapeles
     */
    private fun checkClipboard() {
        try {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString()

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
        } catch (e: Exception) {
            // Ignorar errores de acceso al portapapeles
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
