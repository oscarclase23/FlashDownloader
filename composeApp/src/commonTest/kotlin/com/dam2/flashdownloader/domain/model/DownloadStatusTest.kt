package com.dam2.flashdownloader.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests unitarios para DownloadStatus y sus estados
 * Verifica el comportamiento de cada estado y sus propiedades calculadas
 */
class DownloadStatusTest {

    // ========== Tests para Queued ==========

    @Test
    fun `Queued should calculate progress correctly`() {
        val status = DownloadStatus.Queued(
            bytesDownloaded = 50L,
            totalBytes = 100L
        )
        assertEquals(0.5f, status.progress)
    }

    @Test
    fun `Queued should handle zero total bytes`() {
        val status = DownloadStatus.Queued(
            bytesDownloaded = 0L,
            totalBytes = 0L
        )
        assertEquals(0f, status.progress)
    }

    @Test
    fun `Queued should handle unknown total size`() {
        val status = DownloadStatus.Queued(
            bytesDownloaded = 1000L,
            totalBytes = -1L
        )
        assertEquals(0f, status.progress)
    }

    // ========== Tests para Downloading ==========

    @Test
    fun `Downloading should calculate progress correctly`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 25L * 1024 * 1024, // 25 MB
            totalBytes = 100L * 1024 * 1024,     // 100 MB
            speed = 1024 * 1024                   // 1 MB/s
        )
        assertEquals(0.25f, status.progress)
    }

    @Test
    fun `Downloading should calculate progress percentage`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 50L,
            totalBytes = 100L,
            speed = 10L
        )
        assertEquals(50, status.progressPercentage)
    }

    @Test
    fun `Downloading should calculate estimated time remaining`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 50L * 1024 * 1024, // 50 MB descargados
            totalBytes = 100L * 1024 * 1024,     // 100 MB totales
            speed = 1024 * 1024                   // 1 MB/s
        )
        // Remaining: 50 MB / 1 MB/s = 50 segundos
        assertEquals(50L * 1024 * 1024 / (1024 * 1024), status.estimatedTimeRemaining)
    }

    @Test
    fun `Downloading should return -1 for estimated time when speed is zero`() {
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 50L,
            totalBytes = 100L,
            speed = 0L
        )
        assertEquals(-1L, status.estimatedTimeRemaining)
    }

    @Test
    fun `Downloading should track elapsed time`() {
        val sessionStart = System.currentTimeMillis()
        val status = DownloadStatus.Downloading(
            bytesDownloaded = 50L,
            totalBytes = 100L,
            speed = 10L,
            elapsedSeconds = 30L,
            sessionStartTime = sessionStart
        )

        // El tiempo total debe incluir los 30 segundos previos más el tiempo de sesión
        assertTrue(status.totalElapsedSeconds >= 30L)
    }

    // ========== Tests para Paused ==========

    @Test
    fun `Paused should store state correctly`() {
        val status = DownloadStatus.Paused(
            bytesDownloaded = 500L,
            totalBytes = 1000L,
            elapsedSeconds = 60L
        )

        assertEquals(500L, status.bytesDownloaded)
        assertEquals(1000L, status.totalBytes)
        assertEquals(60L, status.elapsedSeconds)
    }

    @Test
    fun `Paused should calculate progress correctly`() {
        val status = DownloadStatus.Paused(
            bytesDownloaded = 75L,
            totalBytes = 100L,
            elapsedSeconds = 30L
        )
        assertEquals(0.75f, status.progress)
        assertEquals(75, status.progressPercentage)
    }

    // ========== Tests para Completed ==========

    @Test
    fun `Completed should store file path and size`() {
        val status = DownloadStatus.Completed(
            filePath = "/downloads/file.zip",
            totalBytes = 1024L * 1024 * 100 // 100 MB
        )

        assertEquals("/downloads/file.zip", status.filePath)
        assertEquals(1024L * 1024 * 100, status.totalBytes)
    }

    @Test
    fun `Completed should optionally store hash`() {
        val hash = "a1b2c3d4e5f6"
        val status = DownloadStatus.Completed(
            filePath = "/downloads/file.zip",
            totalBytes = 1024L,
            calculatedHash = hash
        )

        assertEquals(hash, status.calculatedHash)
    }

    // ========== Tests para Failed ==========

    @Test
    fun `Failed should store error message`() {
        val errorMessage = "Network timeout"
        val status = DownloadStatus.Failed(
            error = errorMessage,
            bytesDownloaded = 500L
        )

        assertEquals(errorMessage, status.error)
        assertEquals(500L, status.bytesDownloaded)
    }

    @Test
    fun `Failed should handle zero bytes downloaded`() {
        val status = DownloadStatus.Failed(
            error = "Connection refused"
        )

        assertEquals(0L, status.bytesDownloaded)
    }

    // ========== Tests para Cancelled ==========

    @Test
    fun `Cancelled should be a data object`() {
        val status1 = DownloadStatus.Cancelled
        val status2 = DownloadStatus.Cancelled

        assertEquals(status1, status2)
        assertTrue(status1 === status2) // Same instance
    }

    // ========== Tests de propiedades computadas ==========

    @Test
    fun `isActive should return true for Queued and Downloading`() {
        assertTrue(DownloadStatus.Queued().isActive)
        assertTrue(DownloadStatus.Downloading(0L, 100L).isActive)

        assertFalse(DownloadStatus.Paused(0L, 100L, 0L).isActive)
        assertFalse(DownloadStatus.Completed("", 100L).isActive)
        assertFalse(DownloadStatus.Failed("").isActive)
        assertFalse(DownloadStatus.Cancelled.isActive)
    }

    @Test
    fun `canResume should return true for Paused and Failed`() {
        assertTrue(DownloadStatus.Paused(50L, 100L, 30L).canResume)
        assertTrue(DownloadStatus.Failed("Error").canResume)

        assertFalse(DownloadStatus.Queued().canResume)
        assertFalse(DownloadStatus.Downloading(0L, 100L).canResume)
        assertFalse(DownloadStatus.Completed("", 100L).canResume)
        assertFalse(DownloadStatus.Cancelled.canResume)
    }

    @Test
    fun `isTerminal should return true for Completed and Cancelled`() {
        assertTrue(DownloadStatus.Completed("/path", 100L).isTerminal)
        assertTrue(DownloadStatus.Cancelled.isTerminal)

        assertFalse(DownloadStatus.Queued().isTerminal)
        assertFalse(DownloadStatus.Downloading(0L, 100L).isTerminal)
        assertFalse(DownloadStatus.Paused(0L, 100L, 0L).isTerminal)
        assertFalse(DownloadStatus.Failed("Error").isTerminal)
    }

    // ========== Tests de escenarios reales ==========

    @Test
    fun `should handle typical download lifecycle`() {
        // 1. Queued
        var status: DownloadStatus = DownloadStatus.Queued(totalBytes = 1000L)
        assertTrue(status.isActive)
        assertFalse(status.canResume)

        // 2. Downloading
        status = DownloadStatus.Downloading(
            bytesDownloaded = 250L,
            totalBytes = 1000L,
            speed = 50L
        )
        assertTrue(status.isActive)
        assertEquals(25, (status as DownloadStatus.Downloading).progressPercentage)

        // 3. Paused
        status = DownloadStatus.Paused(
            bytesDownloaded = 250L,
            totalBytes = 1000L,
            elapsedSeconds = 5L
        )
        assertFalse(status.isActive)
        assertTrue(status.canResume)

        // 4. Resumed (Downloading again)
        status = DownloadStatus.Downloading(
            bytesDownloaded = 250L,
            totalBytes = 1000L,
            speed = 50L,
            elapsedSeconds = 5L
        )
        assertTrue(status.isActive)

        // 5. Completed
        status = DownloadStatus.Completed(
            filePath = "/downloads/file.dat",
            totalBytes = 1000L
        )
        assertTrue(status.isTerminal)
        assertFalse(status.canResume)
    }
}
