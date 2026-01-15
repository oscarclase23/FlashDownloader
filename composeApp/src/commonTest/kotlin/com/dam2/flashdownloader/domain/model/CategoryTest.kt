package com.dam2.flashdownloader.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests completos para el enum Category
 */
class CategoryTest {

    @Test
    fun `test all category values exist`() {
        val categories = Category.values()
        assertEquals(5, categories.size)
        assertTrue(categories.contains(Category.DOCUMENTS))
        assertTrue(categories.contains(Category.MULTIMEDIA))
        assertTrue(categories.contains(Category.SOFTWARE))
        assertTrue(categories.contains(Category.COMPRESSED))
        assertTrue(categories.contains(Category.OTHERS))
    }

    @Test
    fun `test category display names`() {
        assertEquals("Documentos", Category.DOCUMENTS.displayName)
        assertEquals("Multimedia", Category.MULTIMEDIA.displayName)
        assertEquals("Software", Category.SOFTWARE.displayName)
        assertEquals("Comprimidos", Category.COMPRESSED.displayName)
        assertEquals("Otros", Category.OTHERS.displayName)
    }

    @Test
    fun `test category icons`() {
        assertEquals("📄", Category.DOCUMENTS.iconName)
        assertEquals("🎬", Category.MULTIMEDIA.iconName)
        assertEquals("💿", Category.SOFTWARE.iconName)
        assertEquals("📦", Category.COMPRESSED.iconName)
        assertEquals("📁", Category.OTHERS.iconName)
    }

    // Tests de fromFileName - DOCUMENTS
    @Test
    fun `fromFileName should return DOCUMENTS for pdf files`() {
        assertEquals(Category.DOCUMENTS, Category.fromFileName("document.pdf"))
        assertEquals(Category.DOCUMENTS, Category.fromFileName("REPORT.PDF"))
        assertEquals(Category.DOCUMENTS, Category.fromFileName("file.Pdf"))
    }

    @Test
    fun `fromFileName should return DOCUMENTS for doc files`() {
        assertEquals(Category.DOCUMENTS, Category.fromFileName("document.doc"))
        assertEquals(Category.DOCUMENTS, Category.fromFileName("report.docx"))
        assertEquals(Category.DOCUMENTS, Category.fromFileName("FILE.DOC"))
    }

    @Test
    fun `fromFileName should return DOCUMENTS for spreadsheet files`() {
        assertEquals(Category.DOCUMENTS, Category.fromFileName("data.xls"))
        assertEquals(Category.DOCUMENTS, Category.fromFileName("report.xlsx"))
    }

    @Test
    fun `fromFileName should return DOCUMENTS for presentation files`() {
        assertEquals(Category.DOCUMENTS, Category.fromFileName("slides.ppt"))
        assertEquals(Category.DOCUMENTS, Category.fromFileName("presentation.pptx"))
    }

    @Test
    fun `fromFileName should return DOCUMENTS for text files`() {
        assertEquals(Category.DOCUMENTS, Category.fromFileName("readme.txt"))
        assertEquals(Category.DOCUMENTS, Category.fromFileName("notes.TXT"))
    }

    // Tests de fromFileName - MULTIMEDIA
    @Test
    fun `fromFileName should return MULTIMEDIA for video files`() {
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("movie.mp4"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("video.avi"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("film.mkv"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("VIDEO.MP4"))
    }

    @Test
    fun `fromFileName should return MULTIMEDIA for audio files`() {
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("song.mp3"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("audio.wav"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("music.flac"))
    }

    @Test
    fun `fromFileName should return MULTIMEDIA for image files`() {
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("photo.jpg"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("image.jpeg"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("picture.png"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("animation.gif"))
    }

    // Tests de fromFileName - SOFTWARE
    @Test
    fun `fromFileName should return SOFTWARE for executable files`() {
        assertEquals(Category.SOFTWARE, Category.fromFileName("installer.exe"))
        assertEquals(Category.SOFTWARE, Category.fromFileName("setup.msi"))
        assertEquals(Category.SOFTWARE, Category.fromFileName("PROGRAM.EXE"))
    }

    @Test
    fun `fromFileName should return SOFTWARE for platform-specific installers`() {
        assertEquals(Category.SOFTWARE, Category.fromFileName("app.dmg"))
        assertEquals(Category.SOFTWARE, Category.fromFileName("package.deb"))
        assertEquals(Category.SOFTWARE, Category.fromFileName("software.rpm"))
        assertEquals(Category.SOFTWARE, Category.fromFileName("program.apk"))
        assertEquals(Category.SOFTWARE, Category.fromFileName("application.app"))
    }

    // Tests de fromFileName - COMPRESSED
    @Test
    fun `fromFileName should return COMPRESSED for zip files`() {
        assertEquals(Category.COMPRESSED, Category.fromFileName("archive.zip"))
        assertEquals(Category.COMPRESSED, Category.fromFileName("files.ZIP"))
    }

    @Test
    fun `fromFileName should return COMPRESSED for rar files`() {
        assertEquals(Category.COMPRESSED, Category.fromFileName("archive.rar"))
        assertEquals(Category.COMPRESSED, Category.fromFileName("backup.RAR"))
    }

    @Test
    fun `fromFileName should return COMPRESSED for various compression formats`() {
        assertEquals(Category.COMPRESSED, Category.fromFileName("archive.7z"))
        assertEquals(Category.COMPRESSED, Category.fromFileName("backup.tar"))
        assertEquals(Category.COMPRESSED, Category.fromFileName("files.gz"))
        assertEquals(Category.COMPRESSED, Category.fromFileName("data.bz2"))
    }

    // Tests de fromFileName - OTHERS
    @Test
    fun `fromFileName should return OTHERS for unknown extensions`() {
        assertEquals(Category.OTHERS, Category.fromFileName("file.xyz"))
        assertEquals(Category.OTHERS, Category.fromFileName("unknown.abc"))
        assertEquals(Category.OTHERS, Category.fromFileName("data.custom"))
    }

    @Test
    fun `fromFileName should return OTHERS for files without extension`() {
        assertEquals(Category.OTHERS, Category.fromFileName("README"))
        assertEquals(Category.OTHERS, Category.fromFileName("Makefile"))
        assertEquals(Category.OTHERS, Category.fromFileName("config"))
    }

    @Test
    fun `fromFileName should return OTHERS for empty filename`() {
        assertEquals(Category.OTHERS, Category.fromFileName(""))
    }

    // Edge cases
    @Test
    fun `fromFileName should handle files with multiple dots`() {
        assertEquals(Category.DOCUMENTS, Category.fromFileName("my.document.pdf"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("my.video.file.mp4"))
        assertEquals(Category.COMPRESSED, Category.fromFileName("backup.2024.01.15.zip"))
    }

    @Test
    fun `fromFileName should handle files with path separators`() {
        assertEquals(Category.DOCUMENTS, Category.fromFileName("/path/to/document.pdf"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("C:\\Users\\video.mp4"))
    }

    @Test
    fun `fromFileName should be case insensitive`() {
        assertEquals(Category.DOCUMENTS, Category.fromFileName("file.PDF"))
        assertEquals(Category.DOCUMENTS, Category.fromFileName("file.PdF"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("video.MP4"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("video.Mp4"))
        assertEquals(Category.SOFTWARE, Category.fromFileName("app.EXE"))
        assertEquals(Category.COMPRESSED, Category.fromFileName("archive.ZIP"))
    }

    @Test
    fun `fromFileName should handle special characters in filename`() {
        assertEquals(Category.DOCUMENTS, Category.fromFileName("my-document_v2.pdf"))
        assertEquals(Category.MULTIMEDIA, Category.fromFileName("video (1080p).mp4"))
        assertEquals(Category.COMPRESSED, Category.fromFileName("backup [2024].zip"))
    }
}
