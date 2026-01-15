package com.dam2.flashdownloader.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests completos para el enum Priority
 */
class PriorityTest {

    @Test
    fun `test all priority values exist`() {
        val priorities = Priority.values()
        assertEquals(3, priorities.size)
        assertTrue(priorities.contains(Priority.HIGH))
        assertTrue(priorities.contains(Priority.MEDIUM))
        assertTrue(priorities.contains(Priority.LOW))
    }

    @Test
    fun `test priority levels are correct`() {
        assertEquals(3, Priority.HIGH.level)
        assertEquals(2, Priority.MEDIUM.level)
        assertEquals(1, Priority.LOW.level)
    }

    @Test
    fun `test priority display names`() {
        assertEquals("Alta", Priority.HIGH.displayName)
        assertEquals("Media", Priority.MEDIUM.displayName)
        assertEquals("Baja", Priority.LOW.displayName)
    }

    @Test
    fun `test priority levels are in descending order`() {
        assertTrue(Priority.HIGH.level > Priority.MEDIUM.level)
        assertTrue(Priority.MEDIUM.level > Priority.LOW.level)
    }

    @Test
    fun `fromLevel should return correct priority for valid levels`() {
        assertEquals(Priority.HIGH, Priority.fromLevel(3))
        assertEquals(Priority.MEDIUM, Priority.fromLevel(2))
        assertEquals(Priority.LOW, Priority.fromLevel(1))
    }

    @Test
    fun `fromLevel should return MEDIUM for invalid levels`() {
        assertEquals(Priority.MEDIUM, Priority.fromLevel(0))
        assertEquals(Priority.MEDIUM, Priority.fromLevel(-1))
        assertEquals(Priority.MEDIUM, Priority.fromLevel(4))
        assertEquals(Priority.MEDIUM, Priority.fromLevel(100))
    }

    @Test
    fun `test priority comparison by level`() {
        val priorities = listOf(Priority.LOW, Priority.HIGH, Priority.MEDIUM)
        val sorted = priorities.sortedByDescending { it.level }
        
        assertEquals(Priority.HIGH, sorted[0])
        assertEquals(Priority.MEDIUM, sorted[1])
        assertEquals(Priority.LOW, sorted[2])
    }

    @Test
    fun `test priority enum order`() {
        val values = Priority.values()
        assertEquals(Priority.HIGH, values[0])
        assertEquals(Priority.MEDIUM, values[1])
        assertEquals(Priority.LOW, values[2])
    }

    @Test
    fun `test priority can be used in when expressions`() {
        val priority = Priority.HIGH
        val result = when (priority) {
            Priority.HIGH -> "high"
            Priority.MEDIUM -> "medium"
            Priority.LOW -> "low"
        }
        assertEquals("high", result)
    }

    @Test
    fun `test all priorities have unique levels`() {
        val levels = Priority.values().map { it.level }
        assertEquals(levels.size, levels.distinct().size)
    }

    @Test
    fun `test priority levels are positive`() {
        Priority.values().forEach { priority ->
            assertTrue(priority.level > 0, "Priority ${priority.name} should have positive level")
        }
    }
}
