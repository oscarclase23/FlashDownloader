package com.dam2.flashdownloader.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests completos para la sealed class DownloadStatus
 */
class DownloadStatusTest {

    // Tests de Queued
    @Test
    fun `Queued should have default values`() {
        val status = DownloadStatus.Queued()
        assertEquals(0L, status.bytesDownloaded)
        assertEquals(-1L, status.totalBytes)
        assertEquals(null, status.elapsedSeconds)
    }

    @Test
    fun `Queued should calculate progress correctly when totalBytes is known`() {
        val status = DownloadStatus.Queued(
            bytesDownloaded = 50L,
            totalBytes = 100L
        )
        assertEquals(0.5f, status.progress)
    }

    @Test
    fun `Queued should return zero progress when totalBytes is unknown`() {
        val status = DownloadStatus.Queued(
            bytesDownloaded = 50L,
            totalBytes = -1L
        )
        assertEquals(0f, status.progress)
    }

    @Test
    fun `Queued should return zero progress when totalBytes is zero`() {
        val status = DownloadStatus.Queued(
            bytesDownloaded = 0L,
            totalBytes = 0L
        )
        assertEquals(0f, status.progress)
    }

    @Test
    fun `Queued should preserve elapsedSeconds for resume`() {
        val status = DownloadStatus.Queued(
            bytesDownloaded = 1000L,
            totalBytes = 10000L,
            elapsedSeconds = 120L
        )
        assertEquals(120L, status.elapsedSeconds)
    }

    // Tests de Downloading
    @Test
    fun `Downloading should calculate progress correctly`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 2500L,
            totalBytes = 10000L,
            speed = 100L
        )
        assertEquals(0.25f, status.progress)
        assertEquals(25, status.progressPercentage)
    }

    @Test
    fun `Downloading should return zero progress when totalBytes is unknown`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 1000L,
            totalBytes = -1L,
            speed = 100L
        )
        assertEquals(0f, status.progress)
        assertEquals(0, status.progressPercentage)
    }

    @Test
    fun `Downloading should calculate estimated time remaining correctly`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 2000L,
            totalBytes = 10000L,
            speed = 1000L // 1000 bytes/second
        )
        // Remaining: 8000 bytes / 1000 bytes/s = 8 seconds
        assertEquals(8L, status.estimatedTimeRemaining)
    }

    @Test
    fun `Downloading should return -1 for estimated time when speed is zero`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 2000L,
            totalBytes = 10000L,
            speed = 0L
        )
        assertEquals(-1L, status.estimatedTimeRemaining)
    }

    @Test
    fun `Downloading should return -1 for estimated time when totalBytes is unknown`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 2000L,
            totalBytes = -1L,
            speed = 1000L
        )
        assertEquals(-1L, status.estimatedTimeRemaining)
    }

    @Test
    fun `Downloading should calculate totalElapsedSeconds correctly`() {
        val startTime = System.currentTimeMillis() - 5000 // 5 seconds ago
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 1000L,
            totalBytes = 10000L,
            speed = 100L,
            elapsedSeconds = 10L, // 10 seconds from previous sessions
            sessionStartTime = startTime
        )
        // Should be approximately 10 + 5 = 15 seconds
        val elapsed = status.totalElapsedSeconds
        assertTrue(elapsed >= 14L && elapsed <= 16L, "Expected ~15 seconds, got $elapsed")
    }

    @Test
    fun `Downloading should handle 100 percent progress`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 10000L,
            totalBytes = 10000L,
            speed = 1000L
        )
        assertEquals(1.0f, status.progress)
        assertEquals(100, status.progressPercentage)
    }

    // Tests de Paused
    @Test
    fun `Paused should calculate progress correctly`() {
        val status = DownloadStatus.Paused(
            bytesDownloaded = 3000L,
            totalBytes = 10000L,
            elapsedSeconds = 30L
        )
        assertEquals(0.3f, status.progress)
        assertEquals(30, status.progressPercentage)
    }

    @Test
    fun `Paused should preserve elapsedSeconds`() {
        val status = DownloadStatus.Paused(
            bytesDownloaded = 5000L,
            totalBytes = 10000L,
            elapsedSeconds = 120L
        )
        assertEquals(120L, status.elapsedSeconds)
    }

    @Test
    fun `Paused should handle zero totalBytes`() {
        val status = DownloadStatus.Paused(
            bytesDownloaded = 0L,
            totalBytes = 0L,
            elapsedSeconds = 0L
        )
        assertEquals(0f, status.progress)
    }

    // Tests de Completed
    @Test
    fun `Completed should store file path and size`() {
        val status = DownloadStatus.Completed(
            filePath = "/downloads/file.zip",
            totalBytes = 1024000L
        )
        assertEquals("/downloads/file.zip", status.filePath)
        assertEquals(1024000L, status.totalBytes)
        assertEquals(null, status.calculatedHash)
    }

    @Test
    fun `Completed should store calculated hash when provided`() {
        val hash = "abc123def456"
        val status = DownloadStatus.Completed(
            filePath = "/downloads/file.zip",
            totalBytes = 1024000L,
            calculatedHash = hash
        )
        assertEquals(hash, status.calculatedHash)
    }

    // Tests de Failed
    @Test
    fun `Failed should store error message`() {
        val status = DownloadStatus.Failed(
            error = "Network timeout",
            bytesDownloaded = 5000L
        )
        assertEquals("Network timeout", status.error)
        assertEquals(5000L, status.bytesDownloaded)
    }

    @Test
    fun `Failed should have default bytesDownloaded of zero`() {
        val status = DownloadStatus.Failed(error = "Connection refused")
        assertEquals(0L, status.bytesDownloaded)
    }

    // Tests de Cancelled
    @Test
    fun `Cancelled should be a singleton object`() {
        val status1 = DownloadStatus.Cancelled
        val status2 = DownloadStatus.Cancelled
        assertTrue(status1 === status2)
    }

    // Tests de propiedades computadas - isActive
    @Test
    fun `isActive should be true for Queued`() {
        val status = DownloadStatus.Queued()
        assertTrue(status.isActive)
    }

    @Test
    fun `isActive should be true for Downloading`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 100L,
            totalBytes = 1000L
        )
        assertTrue(status.isActive)
    }

    @Test
    fun `isActive should be false for Paused`() {
        val status = DownloadStatus.Paused(
            bytesDownloaded = 100L,
            totalBytes = 1000L,
            elapsedSeconds = 10L
        )
        assertFalse(status.isActive)
    }

    @Test
    fun `isActive should be false for Completed`() {
        val status = DownloadStatus.Completed(
            filePath = "/path",
            totalBytes = 1000L
        )
        assertFalse(status.isActive)
    }

    @Test
    fun `isActive should be false for Failed`() {
        val status = DownloadStatus.Failed(error = "Error")
        assertFalse(status.isActive)
    }

    @Test
    fun `isActive should be false for Cancelled`() {
        assertFalse(DownloadStatus.Cancelled.isActive)
    }

    // Tests de propiedades computadas - canResume
    @Test
    fun `canResume should be true for Paused`() {
        val status = DownloadStatus.Paused(
            bytesDownloaded = 100L,
            totalBytes = 1000L,
            elapsedSeconds = 10L
        )
        assertTrue(status.canResume)
    }

    @Test
    fun `canResume should be true for Failed`() {
        val status = DownloadStatus.Failed(error = "Network error")
        assertTrue(status.canResume)
    }

    @Test
    fun `canResume should be false for Queued`() {
        assertFalse(DownloadStatus.Queued().canResume)
    }

    @Test
    fun `canResume should be false for Downloading`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 100L,
            totalBytes = 1000L
        )
        assertFalse(status.canResume)
    }

    @Test
    fun `canResume should be false for Completed`() {
        val status = DownloadStatus.Completed(
            filePath = "/path",
            totalBytes = 1000L
        )
        assertFalse(status.canResume)
    }

    @Test
    fun `canResume should be false for Cancelled`() {
        assertFalse(DownloadStatus.Cancelled.canResume)
    }

    // Tests de propiedades computadas - isTerminal
    @Test
    fun `isTerminal should be true for Completed`() {
        val status = DownloadStatus.Completed(
            filePath = "/path",
            totalBytes = 1000L
        )
        assertTrue(status.isTerminal)
    }

    @Test
    fun `isTerminal should be true for Cancelled`() {
        assertTrue(DownloadStatus.Cancelled.isTerminal)
    }

    @Test
    fun `isTerminal should be false for Queued`() {
        assertFalse(DownloadStatus.Queued().isTerminal)
    }

    @Test
    fun `isTerminal should be false for Downloading`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 100L,
            totalBytes = 1000L
        )
        assertFalse(status.isTerminal)
    }

    @Test
    fun `isTerminal should be false for Paused`() {
        val status = DownloadStatus.Paused(
            bytesDownloaded = 100L,
            totalBytes = 1000L,
            elapsedSeconds = 10L
        )
        assertFalse(status.isTerminal)
    }

    @Test
    fun `isTerminal should be false for Failed`() {
        val status = DownloadStatus.Failed(error = "Error")
        assertFalse(status.isTerminal)
    }

    // Edge cases
    @Test
    fun `Downloading with very large numbers should not overflow`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = Long.MAX_VALUE - 1000L,
            totalBytes = Long.MAX_VALUE,
            speed = 1000000L
        )
        assertTrue(status.progress > 0.99f)
        assertTrue(status.estimatedTimeRemaining >= 0)
    }

    @Test
    fun `Progress should never exceed 1_0`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 11000L,
            totalBytes = 10000L, // Downloaded more than total (edge case)
            speed = 1000L
        )
        assertTrue(status.progress >= 1.0f)
    }
}
