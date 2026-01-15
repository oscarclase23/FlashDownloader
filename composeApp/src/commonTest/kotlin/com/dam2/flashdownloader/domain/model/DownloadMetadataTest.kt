package com.dam2.flashdownloader.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests completos para DownloadMetadata
 */
class DownloadMetadataTest {

    @Test
    fun `DownloadMetadata should have default values`() {
        val metadata = DownloadMetadata()
        
        assertEquals(-1L, metadata.totalBytes)
        assertNull(metadata.mimeType)
        assertFalse(metadata.supportsRangeRequests)
        assertNull(metadata.serverFileName)
        assertNull(metadata.lastModified)
    }

    @Test
    fun `DownloadMetadata should store all properties`() {
        val metadata = DownloadMetadata(
            totalBytes = 1024000L,
            mimeType = "application/pdf",
            supportsRangeRequests = true,
            serverFileName = "document.pdf",
            lastModified = "Wed, 15 Jan 2026 12:00:00 GMT"
        )

        assertEquals(1024000L, metadata.totalBytes)
        assertEquals("application/pdf", metadata.mimeType)
        assertTrue(metadata.supportsRangeRequests)
        assertEquals("document.pdf", metadata.serverFileName)
        assertEquals("Wed, 15 Jan 2026 12:00:00 GMT", metadata.lastModified)
    }

    @Test
    fun `DownloadMetadata with unknown size should have -1 totalBytes`() {
        val metadata = DownloadMetadata(totalBytes = -1L)
        assertEquals(-1L, metadata.totalBytes)
    }

    @Test
    fun `DownloadMetadata without range support should be false`() {
        val metadata = DownloadMetadata(supportsRangeRequests = false)
        assertFalse(metadata.supportsRangeRequests)
    }

    @Test
    fun `DownloadMetadata with range support should be true`() {
        val metadata = DownloadMetadata(supportsRangeRequests = true)
        assertTrue(metadata.supportsRangeRequests)
    }

    @Test
    fun `DownloadMetadata should handle various mime types`() {
        val types = listOf(
            "application/pdf",
            "video/mp4",
            "application/zip",
            "image/jpeg",
            "text/plain"
        )

        types.forEach { mimeType ->
            val metadata = DownloadMetadata(mimeType = mimeType)
            assertEquals(mimeType, metadata.mimeType)
        }
    }

    @Test
    fun `DownloadMetadata should handle large file sizes`() {
        val largeSize = 10L * 1024 * 1024 * 1024 // 10 GB
        val metadata = DownloadMetadata(totalBytes = largeSize)
        assertEquals(largeSize, metadata.totalBytes)
    }

    @Test
    fun `DownloadMetadata copy should work correctly`() {
        val original = DownloadMetadata(
            totalBytes = 1000L,
            mimeType = "text/plain"
        )
        val copied = original.copy(totalBytes = 2000L)

        assertEquals(2000L, copied.totalBytes)
        assertEquals("text/plain", copied.mimeType)
    }

    @Test
    fun `DownloadMetadata should handle serverFileName different from client fileName`() {
        val metadata = DownloadMetadata(
            serverFileName = "server-generated-name.pdf"
        )
        assertEquals("server-generated-name.pdf", metadata.serverFileName)
    }

    @Test
    fun `DownloadMetadata should preserve lastModified header`() {
        val lastModified = "Mon, 01 Jan 2024 00:00:00 GMT"
        val metadata = DownloadMetadata(lastModified = lastModified)
        assertEquals(lastModified, metadata.lastModified)
    }
}

/**
 * Tests completos para DownloadStatistics
 */
class DownloadStatisticsTest {

    @Test
    fun `DownloadStatistics should have default values of zero`() {
        val stats = DownloadStatistics()

        assertEquals(0, stats.totalDownloads)
        assertEquals(0, stats.activeDownloads)
        assertEquals(0, stats.queuedDownloads)
        assertEquals(0, stats.pausedDownloads)
        assertEquals(0, stats.completedDownloads)
        assertEquals(0, stats.failedDownloads)
        assertEquals(0L, stats.totalBytesDownloaded)
        assertEquals(0L, stats.currentGlobalSpeed)
        assertEquals(0L, stats.averageSpeed)
    }

    @Test
    fun `DownloadStatistics should store all counters`() {
        val stats = DownloadStatistics(
            totalDownloads = 10,
            activeDownloads = 3,
            queuedDownloads = 2,
            pausedDownloads = 1,
            completedDownloads = 3,
            failedDownloads = 1,
            totalBytesDownloaded = 1024000L,
            currentGlobalSpeed = 102400L,
            averageSpeed = 51200L
        )

        assertEquals(10, stats.totalDownloads)
        assertEquals(3, stats.activeDownloads)
        assertEquals(2, stats.queuedDownloads)
        assertEquals(1, stats.pausedDownloads)
        assertEquals(3, stats.completedDownloads)
        assertEquals(1, stats.failedDownloads)
        assertEquals(1024000L, stats.totalBytesDownloaded)
        assertEquals(102400L, stats.currentGlobalSpeed)
        assertEquals(51200L, stats.averageSpeed)
    }

    @Test
    fun `DownloadStatistics should handle large byte counts`() {
        val largeBytes = 100L * 1024 * 1024 * 1024 // 100 GB
        val stats = DownloadStatistics(totalBytesDownloaded = largeBytes)
        assertEquals(largeBytes, stats.totalBytesDownloaded)
    }

    @Test
    fun `DownloadStatistics should handle high speeds`() {
        val highSpeed = 100L * 1024 * 1024 // 100 MB/s
        val stats = DownloadStatistics(
            currentGlobalSpeed = highSpeed,
            averageSpeed = highSpeed / 2
        )
        assertEquals(highSpeed, stats.currentGlobalSpeed)
        assertEquals(highSpeed / 2, stats.averageSpeed)
    }

    @Test
    fun `DownloadStatistics copy should work correctly`() {
        val original = DownloadStatistics(
            totalDownloads = 5,
            activeDownloads = 2
        )
        val copied = original.copy(activeDownloads = 3)

        assertEquals(5, copied.totalDownloads)
        assertEquals(3, copied.activeDownloads)
    }

    @Test
    fun `DownloadStatistics should handle all downloads in different states`() {
        val stats = DownloadStatistics(
            totalDownloads = 20,
            activeDownloads = 5,
            queuedDownloads = 3,
            pausedDownloads = 4,
            completedDownloads = 6,
            failedDownloads = 2
        )

        // Total should match sum of all states
        val sumOfStates = stats.activeDownloads + stats.queuedDownloads + 
                         stats.pausedDownloads + stats.completedDownloads + 
                         stats.failedDownloads
        assertEquals(20, sumOfStates)
    }

    @Test
    fun `DownloadStatistics should handle zero active downloads`() {
        val stats = DownloadStatistics(
            totalDownloads = 10,
            activeDownloads = 0,
            completedDownloads = 10
        )
        assertEquals(0, stats.activeDownloads)
        assertEquals(0L, stats.currentGlobalSpeed)
    }

    @Test
    fun `DownloadStatistics should handle scenario with only queued downloads`() {
        val stats = DownloadStatistics(
            totalDownloads = 5,
            queuedDownloads = 5
        )
        assertEquals(5, stats.queuedDownloads)
        assertEquals(0, stats.activeDownloads)
        assertEquals(0L, stats.currentGlobalSpeed)
    }

    @Test
    fun `DownloadStatistics should calculate realistic average speed`() {
        val stats = DownloadStatistics(
            totalBytesDownloaded = 10240000L, // 10 MB
            currentGlobalSpeed = 1024000L, // 1 MB/s
            averageSpeed = 512000L // 500 KB/s average
        )
        
        assertTrue(stats.averageSpeed <= stats.currentGlobalSpeed)
        assertTrue(stats.averageSpeed > 0)
    }
}
