package com.dam2.flashdownloader.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests completos para DownloadItem
 */
class DownloadItemTest {

    private fun createTestDownloadItem(
        id: String = "test-id",
        url: String = "https://example.com/file.zip",
        fileName: String = "file.zip",
        category: Category = Category.COMPRESSED,
        priority: Priority = Priority.MEDIUM,
        status: DownloadStatus = DownloadStatus.Queued(),
        createdAt: Long = System.currentTimeMillis(),
        speedLimit: Long? = null,
        localPath: String? = null,
        hash: String? = null,
        metadata: DownloadMetadata = DownloadMetadata()
    ) = DownloadItem(
        id = id,
        url = url,
        fileName = fileName,
        category = category,
        priority = priority,
        status = status,
        createdAt = createdAt,
        speedLimit = speedLimit,
        localPath = localPath,
        hash = hash,
        metadata = metadata
    )

    @Test
    fun `DownloadItem should be created with all properties`() {
        val item = createTestDownloadItem(
            id = "123",
            url = "https://example.com/test.pdf",
            fileName = "test.pdf",
            category = Category.DOCUMENTS,
            priority = Priority.HIGH,
            speedLimit = 1024000L,
            hash = "abc123"
        )

        assertEquals("123", item.id)
        assertEquals("https://example.com/test.pdf", item.url)
        assertEquals("test.pdf", item.fileName)
        assertEquals(Category.DOCUMENTS, item.category)
        assertEquals(Priority.HIGH, item.priority)
        assertEquals(1024000L, item.speedLimit)
        assertEquals("abc123", item.hash)
    }

    // Tests de totalSize
    @Test
    fun `totalSize should return totalBytes from Downloading status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Downloading(
                bytesDownloaded = 500L,
                totalBytes = 1000L
            )
        )
        assertEquals(1000L, item.totalSize)
    }

    @Test
    fun `totalSize should return totalBytes from Paused status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Paused(
                bytesDownloaded = 500L,
                totalBytes = 1000L,
                elapsedSeconds = 10L
            )
        )
        assertEquals(1000L, item.totalSize)
    }

    @Test
    fun `totalSize should return totalBytes from Completed status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Completed(
                filePath = "/path",
                totalBytes = 1000L
            )
        )
        assertEquals(1000L, item.totalSize)
    }

    @Test
    fun `totalSize should return totalBytes from Queued status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Queued(
                bytesDownloaded = 0L,
                totalBytes = 1000L
            )
        )
        assertEquals(1000L, item.totalSize)
    }

    @Test
    fun `totalSize should return metadata totalBytes for other statuses`() {
        val metadata = DownloadMetadata(totalBytes = 2000L)
        val item = createTestDownloadItem(
            status = DownloadStatus.Cancelled,
            metadata = metadata
        )
        assertEquals(2000L, item.totalSize)
    }

    @Test
    fun `totalSize should return metadata totalBytes for Failed status`() {
        val metadata = DownloadMetadata(totalBytes = 3000L)
        val item = createTestDownloadItem(
            status = DownloadStatus.Failed(error = "Error"),
            metadata = metadata
        )
        assertEquals(3000L, item.totalSize)
    }

    // Tests de downloadedBytes
    @Test
    fun `downloadedBytes should return bytesDownloaded from Downloading status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Downloading(
                bytesDownloaded = 750L,
                totalBytes = 1000L
            )
        )
        assertEquals(750L, item.downloadedBytes)
    }

    @Test
    fun `downloadedBytes should return bytesDownloaded from Paused status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Paused(
                bytesDownloaded = 600L,
                totalBytes = 1000L,
                elapsedSeconds = 10L
            )
        )
        assertEquals(600L, item.downloadedBytes)
    }

    @Test
    fun `downloadedBytes should return bytesDownloaded from Failed status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Failed(
                error = "Network error",
                bytesDownloaded = 300L
            )
        )
        assertEquals(300L, item.downloadedBytes)
    }

    @Test
    fun `downloadedBytes should return totalBytes for Completed status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Completed(
                filePath = "/path",
                totalBytes = 1000L
            )
        )
        assertEquals(1000L, item.downloadedBytes)
    }

    @Test
    fun `downloadedBytes should return bytesDownloaded from Queued status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Queued(
                bytesDownloaded = 200L,
                totalBytes = 1000L
            )
        )
        assertEquals(200L, item.downloadedBytes)
    }

    @Test
    fun `downloadedBytes should return 0 for Cancelled status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Cancelled
        )
        assertEquals(0L, item.downloadedBytes)
    }

    // Tests de currentSpeed
    @Test
    fun `currentSpeed should return speed from Downloading status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Downloading(
                bytesDownloaded = 500L,
                totalBytes = 1000L,
                speed = 102400L
            )
        )
        assertEquals(102400L, item.currentSpeed)
    }

    @Test
    fun `currentSpeed should return 0 for Paused status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Paused(
                bytesDownloaded = 500L,
                totalBytes = 1000L,
                elapsedSeconds = 10L
            )
        )
        assertEquals(0L, item.currentSpeed)
    }

    @Test
    fun `currentSpeed should return 0 for Queued status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Queued()
        )
        assertEquals(0L, item.currentSpeed)
    }

    @Test
    fun `currentSpeed should return 0 for Completed status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Completed(
                filePath = "/path",
                totalBytes = 1000L
            )
        )
        assertEquals(0L, item.currentSpeed)
    }

    @Test
    fun `currentSpeed should return 0 for Failed status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Failed(error = "Error")
        )
        assertEquals(0L, item.currentSpeed)
    }

    @Test
    fun `currentSpeed should return 0 for Cancelled status`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Cancelled
        )
        assertEquals(0L, item.currentSpeed)
    }

    // Tests de copy
    @Test
    fun `copy should create new instance with modified properties`() {
        val original = createTestDownloadItem(
            id = "original",
            priority = Priority.LOW
        )
        val copied = original.copy(
            id = "copied",
            priority = Priority.HIGH
        )

        assertEquals("copied", copied.id)
        assertEquals(Priority.HIGH, copied.priority)
        assertEquals(original.url, copied.url)
        assertEquals(original.fileName, copied.fileName)
    }

    @Test
    fun `copy should preserve unmodified properties`() {
        val original = createTestDownloadItem(
            url = "https://example.com/file.zip",
            fileName = "file.zip",
            category = Category.COMPRESSED
        )
        val copied = original.copy(priority = Priority.HIGH)

        assertEquals(original.url, copied.url)
        assertEquals(original.fileName, copied.fileName)
        assertEquals(original.category, copied.category)
        assertEquals(Priority.HIGH, copied.priority)
    }

    // Tests con diferentes combinaciones
    @Test
    fun `DownloadItem with no speed limit should have null speedLimit`() {
        val item = createTestDownloadItem(speedLimit = null)
        assertNull(item.speedLimit)
    }

    @Test
    fun `DownloadItem with speed limit should preserve it`() {
        val item = createTestDownloadItem(speedLimit = 512000L)
        assertEquals(512000L, item.speedLimit)
    }

    @Test
    fun `DownloadItem with no hash should have null hash`() {
        val item = createTestDownloadItem(hash = null)
        assertNull(item.hash)
    }

    @Test
    fun `DownloadItem with hash should preserve it`() {
        val item = createTestDownloadItem(hash = "sha256hash")
        assertEquals("sha256hash", item.hash)
    }

    @Test
    fun `DownloadItem should preserve metadata`() {
        val metadata = DownloadMetadata(
            totalBytes = 5000L,
            mimeType = "application/zip",
            supportsRangeRequests = true,
            serverFileName = "server-file.zip"
        )
        val item = createTestDownloadItem(metadata = metadata)
        
        assertEquals(metadata, item.metadata)
        assertEquals(5000L, item.metadata.totalBytes)
        assertEquals("application/zip", item.metadata.mimeType)
        assertEquals(true, item.metadata.supportsRangeRequests)
        assertEquals("server-file.zip", item.metadata.serverFileName)
    }

    @Test
    fun `DownloadItem should handle unknown totalSize`() {
        val item = createTestDownloadItem(
            status = DownloadStatus.Downloading(
                bytesDownloaded = 1000L,
                totalBytes = -1L
            ),
            metadata = DownloadMetadata(totalBytes = -1L)
        )
        assertEquals(-1L, item.totalSize)
    }

    @Test
    fun `DownloadItem with localPath should preserve it`() {
        val item = createTestDownloadItem(localPath = "/downloads/file.zip")
        assertEquals("/downloads/file.zip", item.localPath)
    }

    @Test
    fun `DownloadItem should preserve createdAt timestamp`() {
        val timestamp = 1234567890L
        val item = createTestDownloadItem(createdAt = timestamp)
        assertEquals(timestamp, item.createdAt)
    }
}
