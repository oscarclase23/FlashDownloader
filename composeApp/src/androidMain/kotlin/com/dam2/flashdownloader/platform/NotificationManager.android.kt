package com.dam2.flashdownloader.platform

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dam2.flashdownloader.ui.utils.formatSpeed

/**
 * Implementación Android del NotificationManager
 * Gestiona notificaciones persistentes para el progreso de descargas
 */
actual class NotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

    companion object {
        private const val CHANNEL_ID = "download_channel"
        private const val CHANNEL_NAME = "Descargas"
        private const val CHANNEL_DESC = "Notificaciones de progreso de descargas"
    }

    init {
        createNotificationChannel()
    }

    /**
     * Crea el canal de notificaciones para Android O+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                AndroidNotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESC
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    actual fun showDownloadProgress(
        downloadId: String,
        fileName: String,
        progress: Float,
        speed: Long,
        isPaused: Boolean
    ) {
        val progressInt = (progress * 100).toInt()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(fileName)
            .setContentText(
                if (isPaused) {
                    "Pausado - $progressInt%"
                } else {
                    "$progressInt% - ${speed.formatSpeed()}"
                }
            )
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progressInt, progress <= 0f)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()

        notificationManager.notify(downloadId.hashCode(), notification)
    }

    actual fun showDownloadCompleted(
        downloadId: String,
        fileName: String,
        filePath: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Descarga completada")
            .setContentText(fileName)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()

        notificationManager.notify(downloadId.hashCode(), notification)
    }

    actual fun showDownloadFailed(
        downloadId: String,
        fileName: String,
        error: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Error en descarga")
            .setContentText("$fileName - $error")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .build()

        notificationManager.notify(downloadId.hashCode(), notification)
    }

    actual fun cancelNotification(downloadId: String) {
        notificationManager.cancel(downloadId.hashCode())
    }

    actual fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}