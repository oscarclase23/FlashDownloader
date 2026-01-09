package com.dam2.flashdownloader.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests unitarios para el enum Priority
 * Verifica el comportamiento de niveles, ordenamiento y nombres de display
 */
class PriorityTest {

    @Test
    fun `priority levels should be ordered correctly`() {
        // Los niveles deben estar en orden ascendente
        assertTrue(Priority.CRITICAL.level > Priority.HIGH.level)
        assertTrue(Priority.HIGH.level > Priority.NORMAL.level)
        assertTrue(Priority.NORMAL.level > Priority.LOW.level)
    }

    @Test
    fun `priority display names should match expected values`() {
        assertEquals("🔴 Crítica", Priority.CRITICAL.displayName)
        assertEquals("🟠 Alta", Priority.HIGH.displayName)
        assertEquals("🟡 Normal", Priority.NORMAL.displayName)
        assertEquals("🟢 Baja", Priority.LOW.displayName)
    }

    @Test
    fun `priority entries should contain all enum values`() {
        val entries = Priority.entries
        assertEquals(4, entries.size)
        assertTrue(entries.contains(Priority.CRITICAL))
        assertTrue(entries.contains(Priority.HIGH))
        assertTrue(entries.contains(Priority.NORMAL))
        assertTrue(entries.contains(Priority.LOW))
    }

    @Test
    fun `priority levels should have unique values`() {
        val levels = Priority.entries.map { it.level }
        val uniqueLevels = levels.toSet()
        assertEquals(levels.size, uniqueLevels.size, "Los niveles deben ser únicos")
    }

    @Test
    fun `fromLevel should return correct priority`() {
        assertEquals(Priority.LOW, Priority.fromLevel(1))
        assertEquals(Priority.NORMAL, Priority.fromLevel(2))
        assertEquals(Priority.HIGH, Priority.fromLevel(3))
        assertEquals(Priority.CRITICAL, Priority.fromLevel(4))
    }

    @Test
    fun `fromLevel should return NORMAL for invalid level`() {
        assertEquals(Priority.NORMAL, Priority.fromLevel(0))
        assertEquals(Priority.NORMAL, Priority.fromLevel(999))
        assertEquals(Priority.NORMAL, Priority.fromLevel(-1))
    }

    @Test
    fun `priority should be comparable by level`() {
        val priorities = listOf(Priority.LOW, Priority.CRITICAL, Priority.NORMAL, Priority.HIGH)
        val sorted = priorities.sortedByDescending { it.level }

        assertEquals(Priority.CRITICAL, sorted[0])
        assertEquals(Priority.HIGH, sorted[1])
        assertEquals(Priority.NORMAL, sorted[2])
        assertEquals(Priority.LOW, sorted[3])
    }
}
