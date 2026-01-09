package com.dam2.flashdownloader.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Tests unitarios para el enum Category
 * Verifica nombres de display, iconos y completitud
 */
class CategoryTest {

    @Test
    fun `category should have correct icon names`() {
        assertEquals("📦", Category.GENERAL.iconName)
        assertEquals("📄", Category.DOCUMENTS.iconName)
        assertEquals("🖼️", Category.IMAGES.iconName)
        assertEquals("🎵", Category.AUDIO.iconName)
        assertEquals("🎬", Category.VIDEO.iconName)
        assertEquals("📚", Category.COMPRESSED.iconName)
        assertEquals("⚙️", Category.SOFTWARE.iconName)
    }

    @Test
    fun `category should have correct display names`() {
        assertEquals("General", Category.GENERAL.displayName)
        assertEquals("Documentos", Category.DOCUMENTS.displayName)
        assertEquals("Imágenes", Category.IMAGES.displayName)
        assertEquals("Audio", Category.AUDIO.displayName)
        assertEquals("Vídeo", Category.VIDEO.displayName)
        assertEquals("Comprimidos", Category.COMPRESSED.displayName)
        assertEquals("Software", Category.SOFTWARE.displayName)
    }

    @Test
    fun `category entries should contain all values`() {
        val entries = Category.entries
        assertEquals(7, entries.size)
        assertTrue(entries.contains(Category.GENERAL))
        assertTrue(entries.contains(Category.DOCUMENTS))
        assertTrue(entries.contains(Category.IMAGES))
        assertTrue(entries.contains(Category.AUDIO))
        assertTrue(entries.contains(Category.VIDEO))
        assertTrue(entries.contains(Category.COMPRESSED))
        assertTrue(entries.contains(Category.SOFTWARE))
    }

    @Test
    fun `category display names should be non-empty`() {
        Category.entries.forEach { category ->
            assertTrue(category.displayName.isNotEmpty(), "Display name no debe estar vacío para ${category.name}")
        }
    }

    @Test
    fun `category icon names should be non-empty`() {
        Category.entries.forEach { category ->
            assertTrue(category.iconName.isNotEmpty(), "Icon name no debe estar vacío para ${category.name}")
        }
    }

    @Test
    fun `category valueOf should work correctly`() {
        assertEquals(Category.GENERAL, Category.valueOf("GENERAL"))
        assertEquals(Category.DOCUMENTS, Category.valueOf("DOCUMENTS"))
        assertEquals(Category.IMAGES, Category.valueOf("IMAGES"))
        assertEquals(Category.AUDIO, Category.valueOf("AUDIO"))
        assertEquals(Category.VIDEO, Category.valueOf("VIDEO"))
        assertEquals(Category.COMPRESSED, Category.valueOf("COMPRESSED"))
        assertEquals(Category.SOFTWARE, Category.valueOf("SOFTWARE"))
    }

    @Test
    fun `category display names should be unique`() {
        val displayNames = Category.entries.map { it.displayName }
        val uniqueNames = displayNames.toSet()
        assertEquals(displayNames.size, uniqueNames.size, "Los nombres de display deben ser únicos")
    }

    @Test
    fun `all categories should be accessible`() {
        // Verificar que todas las categorías son accesibles sin excepciones
        Category.entries.forEach { category ->
            assertNotNull(category.name)
            assertNotNull(category.displayName)
            assertNotNull(category.iconName)
        }
    }
}
