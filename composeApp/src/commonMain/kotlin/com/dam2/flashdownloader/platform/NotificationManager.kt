package com.dam2.flashdownloader.platform

/**
 * Gestor de notificaciones multiplataforma
 * Utiliza expect/actual para proporcionar implementaciones específicas de plataforma
 */
expect class NotificationManager() {
    /**
     * Muestra una notificación de progreso de descarga
     * @param downloadId ID único de la descarga
     * @param fileName Nombre del archivo que se está descargando
     * @param progress Progreso de 0.0 a 1.0
     * @param speed Velocidad actual en bytes/segundo
     * @param isPaused Indica si la descarga está pausada
     */
    fun showDownloadProgress(
        downloadId: String,
        fileName: String,
        progress: Float,
        speed: Long,
        isPaused: Boolean
    )

    /**
     * Muestra notificación de descarga completada
     * @param downloadId ID único de la descarga
     * @param fileName Nombre del archivo descargado
     * @param filePath Ruta completa del archivo descargado
     */
    fun showDownloadCompleted(
        downloadId: String,
        fileName: String,
        filePath: String
    )

    /**
     * Muestra notificación de error en la descarga
     * @param downloadId ID único de la descarga
     * @param fileName Nombre del archivo que falló
     * @param error Mensaje de error descriptivo
     */
    fun showDownloadFailed(
        downloadId: String,
        fileName: String,
        error: String
    )

    /**
     * Cancela una notificación específica
     * @param downloadId ID de la descarga cuya notificación se cancelará
     */
    fun cancelNotification(downloadId: String)

    /**
     * Cancela todas las notificaciones de descargas
     */
    fun cancelAllNotifications()
}
