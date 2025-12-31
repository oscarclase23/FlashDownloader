package com.dam2.flashdownloader.platform

import com.sun.tools.javac.util.Context
import kotlinx.coroutines.flow.StateFlow

/**
 * Gestor de portapapeles multiplataforma con detección automática de URLs
 * Monitoriza el portapapeles en busca de URLs válidas para descargar
 */
expect class ClipboardManager() {
    /**
     * Flow que emite la URL actual del portapapeles
     * Emite null si no hay una URL válida en el portapapeles
     */
    val clipboardUrl: StateFlow<String?>

    /**
     * Inicia la monitorización del portapapeles
     * Debe llamarse cuando la UI está lista para recibir URLs
     */
    fun startMonitoring()

    /**
     * Detiene la monitorización del portapapeles
     * Debe llamarse cuando la UI ya no necesita detectar URLs
     */
    fun stopMonitoring()

    /**
     * Verifica si una cadena es una URL válida para descargar
     * @param text Texto a verificar
     * @return true si es una URL HTTP/HTTPS válida
     */
    fun isValidUrl(text: String): Boolean
}
