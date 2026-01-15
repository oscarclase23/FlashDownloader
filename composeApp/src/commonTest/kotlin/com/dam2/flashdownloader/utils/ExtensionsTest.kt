package com.dam2.flashdownloader.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests para funciones de extensión
 */
class ExtensionsTest {

    // Tests de isValidUrl (String extension)
    @Test
    fun `isValidUrl should return true for valid HTTP URLs`() {
        assertTrue("http://example.com".isValidUrl())
        assertTrue("http://example.com/file.zip".isValidUrl())
        assertTrue("http://example.com:8080/download".isValidUrl())
    }

    @Test
    fun `isValidUrl should return true for valid HTTPS URLs`() {
        assertTrue("https://example.com".isValidUrl())
        assertTrue("https://example.com/file.pdf".isValidUrl())
        assertTrue("https://subdomain.example.com/path/to/file".isValidUrl())
    }

    @Test
    fun `isValidUrl should return true for URLs with query parameters`() {
        assertTrue("https://example.com/file?id=123".isValidUrl())
        assertTrue("https://example.com/download?file=test.zip&token=abc".isValidUrl())
    }

    @Test
    fun `isValidUrl should be case insensitive`() {
        assertTrue("HTTP://example.com".isValidUrl())
        assertTrue("HTTPS://example.com".isValidUrl())
        assertTrue("HtTp://example.com".isValidUrl())
    }

    @Test
    fun `isValidUrl should return false for invalid URLs`() {
        assertFalse("".isValidUrl())
        assertFalse("not a url".isValidUrl())
        assertFalse("ftp://example.com".isValidUrl())
        assertFalse("file:///path/to/file".isValidUrl())
        assertFalse("example.com".isValidUrl())
        assertFalse("www.example.com".isValidUrl())
    }

    // Tests de extractUrls (String extension)
    @Test
    fun `extractUrls should extract single URL from text`() {
        val text = "Check out this file: https://example.com/file.zip"
        val urls = text.extractUrls()
        assertEquals(1, urls.size)
        assertEquals("https://example.com/file.zip", urls[0])
    }

    @Test
    fun `extractUrls should extract multiple URLs from text`() {
        val text = "Download from https://example.com/file1.zip or http://example.org/file2.pdf"
        val urls = text.extractUrls()
        assertEquals(2, urls.size)
        assertTrue(urls.contains("https://example.com/file1.zip"))
        assertTrue(urls.contains("http://example.org/file2.pdf"))
    }

    @Test
    fun `extractUrls should return empty list when no URLs found`() {
        val text = "This text has no URLs"
        val urls = text.extractUrls()
        assertEquals(0, urls.size)
    }

    @Test
    fun `extractUrls should handle URLs with query parameters`() {
        val text = "Download: https://example.com/file?id=123&token=abc"
        val urls = text.extractUrls()
        assertEquals(1, urls.size)
        assertTrue(urls[0].contains("?id=123"))
    }

    @Test
    fun `extractUrls should handle empty string`() {
        val urls = "".extractUrls()
        assertEquals(0, urls.size)
    }

    @Test
    fun `extractUrls should extract URLs from multiline text`() {
        val text = """
            First URL: https://example.com/file1.zip
            Second URL: http://example.org/file2.pdf
            Third URL: https://test.com/file3.tar.gz
        """.trimIndent()
        val urls = text.extractUrls()
        assertEquals(3, urls.size)
    }

    // Tests de getFileExtension (String extension)
    @Test
    fun `getFileExtension should extract extension correctly`() {
        assertEquals("zip", "file.zip".getFileExtension())
        assertEquals("pdf", "document.pdf".getFileExtension())
        assertEquals("mp4", "video.mp4".getFileExtension())
    }

    @Test
    fun `getFileExtension should handle files without extension`() {
        assertEquals("", "README".getFileExtension())
        assertEquals("", "Makefile".getFileExtension())
    }

    @Test
    fun `getFileExtension should handle files with multiple dots`() {
        assertEquals("gz", "file.tar.gz".getFileExtension())
        assertEquals("pdf", "my.document.v2.pdf".getFileExtension())
    }

    @Test
    fun `getFileExtension should handle empty string`() {
        assertEquals("", "".getFileExtension())
    }

    // Integration tests
    @Test
    fun `should extract and validate URLs from clipboard text`() {
        val clipboardText = """
            Download these files:
            https://example.com/file1.zip
            http://test.org/file2.pdf
            Not a URL: example.com
        """.trimIndent()
        
        val urls = clipboardText.extractUrls()
        assertEquals(2, urls.size)
        urls.forEach { url ->
            assertTrue(url.isValidUrl())
        }
    }

    @Test
    fun `should handle file information extraction`() {
        val fileName = "my.important.document.pdf"
        val extension = fileName.getFileExtension()
        
        assertEquals("pdf", extension)
    }
}
