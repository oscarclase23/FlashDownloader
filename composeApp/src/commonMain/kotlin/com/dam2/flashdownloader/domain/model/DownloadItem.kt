package com.dam2.flashdownloader.domain.model

/**
 * Modelo de dominio que representa una descarga individual
 */
data class DownloadItem(
    val id: String,
    val url: String,
    val fileName: String,
    val category: Category,
    val priority: Priority,
    val status: DownloadStatus,
    val createdAt: Long,
    val speedLimit: Long? = null,
    val localPath: String? = null,
    val hash: String? = null,
    val metadata: DownloadMetadata = DownloadMetadata()
) {
    val totalSize: Long
        get() = when (status) {
            is DownloadStatus.Downloading -> status.totalBytes
            is DownloadStatus.Paused -> status.totalBytes
            is DownloadStatus.Completed -> status.totalBytes
            is DownloadStatus.Queued -> status.totalBytes
            else -> metadata.totalBytes
        }

    val downloadedBytes: Long
        get() = when (status) {
            is DownloadStatus.Downloading -> status.bytesDownloaded
            is DownloadStatus.Paused -> status.bytesDownloaded
            is DownloadStatus.Failed -> status.bytesDownloaded
            is DownloadStatus.Completed -> status.totalBytes
            is DownloadStatus.Queued -> status.bytesDownloaded
            else -> 0L
        }
}

data class DownloadMetadata(
    val totalBytes: Long = -1L,
    val mimeType: String? = null,
    val supportsRangeRequests: Boolean = false,
    val serverFileName: String? = null,
    val lastModified: String? = null
)


