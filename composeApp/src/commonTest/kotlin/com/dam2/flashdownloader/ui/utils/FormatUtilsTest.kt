package com.dam2.flashdownloader.ui.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests unitarios para las funciones de formateo
 * Verifica formatBytes, formatSpeed y formatTime
 */
class FormatUtilsTest {

    // ========== Tests para formatBytes ==========

    @Test
    fun `formatBytes should format zero correctly`() {
        assertEquals("0 B", 0L.formatBytes())
    }

    @Test
    fun `formatBytes should format bytes correctly`() {
        assertEquals("500 B", 500L.formatBytes())
        assertEquals("1023 B", 1023L.formatBytes())
    }

    @Test
    fun `formatBytes should format kilobytes correctly`() {
        assertEquals("1.00 KB", 1024L.formatBytes())
        assertEquals("1.50 KB", 1536L.formatBytes())
        assertEquals("10.00 KB", (10 * 1024).toLong().formatBytes())
        assertEquals("500.00 KB", (500 * 1024).toLong().formatBytes())
    }

    @Test
    fun `formatBytes should format megabytes correctly`() {
        assertEquals("1.00 MB", (1024 * 1024).toLong().formatBytes())
        assertEquals("5.50 MB", (5.5 * 1024 * 1024).toLong().formatBytes())
        assertEquals("100.00 MB", (100L * 1024 * 1024).formatBytes())
        assertEquals("999.99 MB", (999.99 * 1024 * 1024).toLong().formatBytes())
    }

    @Test
    fun `formatBytes should format gigabytes correctly`() {
        assertEquals("1.00 GB", (1024L * 1024 * 1024).formatBytes())
        assertEquals("1.50 GB", (1.5 * 1024 * 1024 * 1024).toLong().formatBytes())
        assertEquals("50.00 GB", (50L * 1024 * 1024 * 1024).formatBytes())
    }

    @Test
    fun `formatBytes should format terabytes correctly`() {
        assertEquals("1.00 TB", (1024L * 1024 * 1024 * 1024).formatBytes())
        assertEquals("2.00 TB", (2L * 1024 * 1024 * 1024 * 1024).formatBytes())
    }

    @Test
    fun `formatBytes should handle negative values gracefully`() {
        assertEquals("0 B", (-100L).formatBytes())
        assertEquals("0 B", (-1L).formatBytes())
    }

    @Test
    fun `formatBytes should have correct decimal places`() {
        val result = (1536L).formatBytes() // 1.5 KB
        assertTrue(result.contains("1.50"), "Debe tener 2 decimales")
    }

    // ========== Tests para formatSpeed ==========

    @Test
    fun `formatSpeed should add per second suffix`() {
        assertEquals("0 B/s", 0L.formatSpeed())
        assertEquals("500 B/s", 500L.formatSpeed())
        assertEquals("1.00 KB/s", 1024L.formatSpeed())
        assertEquals("5.00 MB/s", (5 * 1024 * 1024).toLong().formatSpeed())
    }

    @Test
    fun `formatSpeed should use same formatting as formatBytes`() {
        val bytes = 1024L * 1024 * 5 // 5 MB
        val expectedBytes = bytes.formatBytes()
        val expectedSpeed = "$expectedBytes/s"
        assertEquals(expectedSpeed, bytes.formatSpeed())
    }

    @Test
    fun `formatSpeed should handle negative values`() {
        assertEquals("0 B/s", (-100L).formatSpeed())
    }

    // ========== Tests para formatTime ==========

    @Test
    fun `formatTime should format zero seconds`() {
        assertEquals("0s", 0L.formatTime())
    }

    @Test
    fun `formatTime should format seconds only`() {
        assertEquals("1s", 1L.formatTime())
        assertEquals("30s", 30L.formatTime())
        assertEquals("59s", 59L.formatTime())
    }

    @Test
    fun `formatTime should format minutes and seconds`() {
        assertEquals("1m 0s", 60L.formatTime())
        assertEquals("1m 30s", 90L.formatTime())
        assertEquals("2m 0s", 120L.formatTime())
        assertEquals("5m 45s", 345L.formatTime())
        assertEquals("59m 59s", 3599L.formatTime())
    }

    @Test
    fun `formatTime should format hours and minutes`() {
        assertEquals("1h 0m", 3600L.formatTime())
        assertEquals("1h 30m", 5400L.formatTime())
        assertEquals("2h 15m", 8100L.formatTime())
        assertEquals("10h 0m", 36000L.formatTime())
        assertEquals("23h 59m", 86340L.formatTime())
    }

    @Test
    fun `formatTime should handle edge cases`() {
        assertEquals("0s", (-10L).formatTime()) // Negative values
        assertEquals("1h 0m", 3601L.formatTime()) // Just over an hour
        assertEquals("0s", (-1L).formatTime())
    }

    @Test
    fun `formatTime should not show seconds when hours are present`() {
        val result = 3661L.formatTime() // 1h 1m 1s
        assertTrue(result.contains("h"), "Debe contener horas")
        assertTrue(result.contains("m"), "Debe contener minutos")
        assertTrue(!result.contains("s") || result == "0s", "No debe mostrar segundos cuando hay horas")
    }

    @Test
    fun `formatTime should handle large values`() {
        val oneDayInSeconds = 86400L
        val result = oneDayInSeconds.formatTime()
        assertTrue(result.contains("h"), "Debe manejar días como horas")
    }

    // ========== Tests de integración ==========

    @Test
    fun `formatting functions should handle typical download scenarios`() {
        // Descarga de 100 MB
        val fileSize = 100L * 1024 * 1024
        assertEquals("100.00 MB", fileSize.formatBytes())

        // Velocidad de 5 MB/s
        val speed = 5L * 1024 * 1024
        assertEquals("5.00 MB/s", speed.formatSpeed())

        // Tiempo estimado: 20 segundos
        val time = 20L
        assertEquals("20s", time.formatTime())
    }

    @Test
    fun `formatting should be consistent across different scales`() {
        val sizes = listOf(
            0L,
            1024L,
            1024L * 1024,
            1024L * 1024 * 1024,
            1024L * 1024 * 1024 * 1024
        )

        sizes.forEach { size ->
            val formatted = size.formatBytes()
            assertTrue(formatted.isNotEmpty(), "Formato no debe estar vacío")
            assertTrue(formatted.matches(Regex("\\d+(\\.\\d{2})? [BKMGT]B")),
                "Formato debe coincidir con el patrón esperado: $formatted")
        }
    }
}
