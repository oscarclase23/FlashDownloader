package com.dam2.flashdownloader.domain.model

/**
 * Estados posibles de una descarga durante su ciclo de vida
 */
sealed class DownloadStatus {
    data class Queued(
        val bytesDownloaded: Long = 0L,
        val totalBytes: Long = -1L,
        val elapsedSeconds: Long? = null
    ) : DownloadStatus()

    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val speed: Long = 0L,
        val elapsedSeconds: Long = 0L,
        val sessionStartTime: Long = System.currentTimeMillis()
    ) : DownloadStatus()

    data class Paused(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val elapsedSeconds: Long
    ) : DownloadStatus()

    data class Completed(
        val filePath: String,
        val totalBytes: Long,
        val calculatedHash: String? = null
    ) : DownloadStatus()

    data class Failed(
        val error: String,
        val bytesDownloaded: Long = 0L
    ) : DownloadStatus()

    data object Cancelled : DownloadStatus()
}
