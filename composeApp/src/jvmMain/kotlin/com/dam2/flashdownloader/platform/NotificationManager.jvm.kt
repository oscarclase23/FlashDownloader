package com.dam2.flashdownloader.platform

/**
 * Implementación stub del NotificationManager para Desktop (JVM)
 * Por ahora imprime en consola, en el futuro se puede integrar con notificaciones nativas del sistema
 */
actual class NotificationManager {

    actual fun showDownloadProgress(
        downloadId: String,
        fileName: String,
        progress: Float,
        speed: Long,
        isPaused: Boolean
    ) {
        val progressInt = (progress * 100).toInt()
        val status = if (isPaused) "PAUSADO" else "DESCARGANDO"
        println("[Desktop Notification] $status: $fileName - $progressInt% (${speed / 1024} KB/s)")
    }

    actual fun showDownloadCompleted(
        downloadId: String,
        fileName: String,
        filePath: String
    ) {
        println("[Desktop Notification] COMPLETADO: $fileName")
        println("  Ubicación: $filePath")
    }

    actual fun showDownloadFailed(
        downloadId: String,
        fileName: String,
        error: String
    ) {
        println("[Desktop Notification] ERROR: $fileName")
        println("  Motivo: $error")
    }

    actual fun cancelNotification(downloadId: String) {
        // No-op en Desktop
    }

    actual fun cancelAllNotifications() {
        // No-op en Desktop
    }
}
