package com.dam2.flashdownloader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dam2.flashdownloader.domain.model.Category
import com.dam2.flashdownloader.domain.model.DownloadItem
import com.dam2.flashdownloader.domain.model.DownloadStatus
import com.dam2.flashdownloader.domain.model.Priority
import com.dam2.flashdownloader.domain.repository.DownloadRepository
import com.dam2.flashdownloader.platform.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel principal para gestionar las descargas
 * Integra notificaciones del sistema para feedback del usuario
 */
class DownloadViewModel(
    private val repository: DownloadRepository,
    private val notificationManager: NotificationManager
) : ViewModel() {

    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadDownloads()
        observeDownloadChanges()
    }

    /**
     * Carga todas las descargas desde el repositorio
     */
    private fun loadDownloads() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllDownloads().onSuccess { downloads ->
                _downloads.value = downloads
            }
            _isLoading.value = false
        }
    }

    /**
     * Observa cambios en las descargas y actualiza notificaciones
     */
    private fun observeDownloadChanges() {
        viewModelScope.launch {
            _downloads.collect { downloads ->
                downloads.forEach { download ->
                    updateNotificationForDownload(download)
                }
            }
        }
    }

    /**
     * Actualiza la notificación según el estado de la descarga
     */
    private fun updateNotificationForDownload(download: DownloadItem) {
        when (val status = download.status) {
            is DownloadStatus.Downloading -> {
                val progress = if (status.totalBytes > 0) {
                    status.bytesDownloaded.toFloat() / status.totalBytes.toFloat()
                } else {
                    0f
                }
                notificationManager.showDownloadProgress(
                    downloadId = download.id,
                    fileName = download.fileName,
                    progress = progress,
                    speed = status.speed,
                    isPaused = false
                )
            }

            is DownloadStatus.Paused -> {
                val progress = if (status.totalBytes > 0) {
                    status.bytesDownloaded.toFloat() / status.totalBytes.toFloat()
                } else {
                    0f
                }
                notificationManager.showDownloadProgress(
                    downloadId = download.id,
                    fileName = download.fileName,
                    progress = progress,
                    speed = 0,
                    isPaused = true
                )
            }

            is DownloadStatus.Completed -> {
                notificationManager.showDownloadCompleted(
                    downloadId = download.id,
                    fileName = download.fileName,
                    filePath = status.filePath
                )
            }

            is DownloadStatus.Failed -> {
                notificationManager.showDownloadFailed(
                    downloadId = download.id,
                    fileName = download.fileName,
                    error = status.error
                )
            }

            is DownloadStatus.Cancelled -> {
                notificationManager.cancelNotification(download.id)
            }

            else -> {
                // Queued - no mostrar notificación aún
            }
        }
    }

    /**
     * Agrega una nueva descarga
     */
    fun addDownload(
        url: String,
        fileName: String,
        category: Category = Category.GENERAL,
        priority: Priority = Priority.NORMAL
    ) {
        viewModelScope.launch {
            val download = DownloadItem(
                id = generateDownloadId(),
                url = url,
                fileName = fileName,
                category = category,
                priority = priority,
                status = DownloadStatus.Queued(),
                createdAt = System.currentTimeMillis()
            )

            repository.saveDownload(download)
            loadDownloads()
        }
    }

    /**
     * Pausa una descarga
     */
    fun pauseDownload(id: String) {
        viewModelScope.launch {
            repository.getDownload(id).onSuccess { download ->
                download?.let {
                    if (it.status is DownloadStatus.Downloading) {
                        val status = it.status as DownloadStatus.Downloading
                        val pausedStatus = DownloadStatus.Paused(
                            bytesDownloaded = status.bytesDownloaded,
                            totalBytes = status.totalBytes,
                            elapsedSeconds = status.elapsedSeconds
                        )
                        repository.updateDownload(it.copy(status = pausedStatus))
                        loadDownloads()
                    }
                }
            }
        }
    }

    /**
     * Reanuda una descarga pausada
     */
    fun resumeDownload(id: String) {
        viewModelScope.launch {
            repository.getDownload(id).onSuccess { download ->
                download?.let {
                    if (it.status is DownloadStatus.Paused) {
                        val status = it.status as DownloadStatus.Paused
                        val resumedStatus = DownloadStatus.Downloading(
                            bytesDownloaded = status.bytesDownloaded,
                            totalBytes = status.totalBytes,
                            speed = 0L,
                            elapsedSeconds = status.elapsedSeconds,
                            sessionStartTime = System.currentTimeMillis()
                        )
                        repository.updateDownload(it.copy(status = resumedStatus))
                        loadDownloads()
                    }
                }
            }
        }
    }

    /**
     * Cancela una descarga
     */
    fun cancelDownload(id: String) {
        viewModelScope.launch {
            repository.getDownload(id).onSuccess { download ->
                download?.let {
                    repository.updateDownload(it.copy(status = DownloadStatus.Cancelled))
                    notificationManager.cancelNotification(id)
                    loadDownloads()
                }
            }
        }
    }

    /**
     * Elimina una descarga
     */
    fun removeDownload(id: String) {
        viewModelScope.launch {
            repository.deleteDownload(id)
            notificationManager.cancelNotification(id)
            loadDownloads()
        }
    }

    /**
     * Limpia todas las notificaciones
     */
    fun clearAllNotifications() {
        notificationManager.cancelAllNotifications()
    }

    /**
     * Genera un ID único para una descarga
     */
    private fun generateDownloadId(): String {
        return "download_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    override fun onCleared() {
        super.onCleared()
        // Opcional: limpiar notificaciones al cerrar la app
        // notificationManager.cancelAllNotifications()
    }
}