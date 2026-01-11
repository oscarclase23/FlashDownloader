package com.dam2.flashdownloader.data.repository

import com.dam2.flashdownloader.domain.model.*
import com.dam2.flashdownloader.domain.repository.DownloadRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Tests unitarios para DownloadRepository
 * Verifica operaciones CRUD, filtrado de historial y manejo de errores
 */
class DownloadRepositoryTest {

    private lateinit var repository: DownloadRepository
    private lateinit var mockStorage: MockPersistentStorage

    @BeforeTest
    fun setup() {
        mockStorage = MockPersistentStorage()
        repository = DownloadRepositoryImpl(mockStorage)
    }

    // ========== Tests de operaciones básicas CRUD ==========

    @Test
    fun `saveDownload should store download correctly`() = runTest {
        val download = createTestDownload()
        val result = repository.saveDownload(download)

        assertTrue(result.isSuccess, "Save debería ser exitoso")

        val retrieved = repository.getDownload(download.id)
        assertTrue(retrieved.isSuccess)
        assertNotNull(retrieved.getOrNull())
        assertEquals(download.id, retrieved.getOrNull()?.id)
        assertEquals(download.fileName, retrieved.getOrNull()?.fileName)
    }

    @Test
    fun `getAllDownloads should return all stored downloads`() = runTest {
        val download1 = createTestDownload(id = "1", fileName = "file1.zip")
        val download2 = createTestDownload(id = "2", fileName = "file2.pdf")
        val download3 = createTestDownload(id = "3", fileName = "file3.mp4")

        repository.saveDownload(download1)
        repository.saveDownload(download2)
        repository.saveDownload(download3)

        val result = repository.getAllDownloads()
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
    }

    @Test
    fun `getDownload should return null for non-existent id`() = runTest {
        val result = repository.getDownload("non-existent-id")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun `updateDownload should modify existing download`() = runTest {
        val download = createTestDownload()
        repository.saveDownload(download)

        val updated = download.copy(fileName = "updated-file.zip")
        val updateResult = repository.updateDownload(updated)

        assertTrue(updateResult.isSuccess)

        val retrieved = repository.getDownload(download.id)
        assertEquals("updated-file.zip", retrieved.getOrNull()?.fileName)
    }

    @Test
    fun `updateDownload should fail for non-existent download`() = runTest {
        val download = createTestDownload(id = "non-existent")
        val result = repository.updateDownload(download)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not found") == true)
    }

    @Test
    fun `updateAll should replace entire download list`() = runTest {
        // Guardar descargas iniciales
        repository.saveDownload(createTestDownload(id = "1"))
        repository.saveDownload(createTestDownload(id = "2"))

        // Actualizar con nueva lista
        val newList = listOf(
            createTestDownload(id = "3"),
            createTestDownload(id = "4"),
            createTestDownload(id = "5")
        )

        val result = repository.updateAll(newList)
        assertTrue(result.isSuccess)

        val all = repository.getAllDownloads().getOrNull()
        assertEquals(3, all?.size)
        assertTrue(all?.any { it.id == "3" } == true)
        assertFalse(all?.any { it.id == "1" } == true)
    }

    @Test
    fun `deleteDownload should remove download`() = runTest {
        val download = createTestDownload()
        repository.saveDownload(download)

        val deleteResult = repository.deleteDownload(download.id)
        assertTrue(deleteResult.isSuccess)

        val retrieved = repository.getDownload(download.id)
        assertNull(retrieved.getOrNull())
    }

    @Test
    fun `deleteDownload should succeed even for non-existent id`() = runTest {
        val result = repository.deleteDownload("non-existent-id")
        assertTrue(result.isSuccess)
    }

    // ========== Tests de clearCompleted ==========

    @Test
    fun `clearCompleted should remove only completed downloads`() = runTest {
        val completed = createTestDownload(
            id = "1",
            status = DownloadStatus.Completed("/path/file1.zip", 1024L)
        )
        val downloading = createTestDownload(
            id = "2",
            status = DownloadStatus.Downloading(500L, 1000L)
        )
        val queued = createTestDownload(
            id = "3",
            status = DownloadStatus.Queued()
        )

        repository.saveDownload(completed)
        repository.saveDownload(downloading)
        repository.saveDownload(queued)

        val result = repository.clearCompleted()
        assertTrue(result.isSuccess)

        val remaining = repository.getAllDownloads().getOrNull()
        assertEquals(2, remaining?.size)
        assertFalse(remaining?.any { it.id == "1" } == true)
        assertTrue(remaining?.any { it.id == "2" } == true)
        assertTrue(remaining?.any { it.id == "3" } == true)
    }

    // ========== Tests de datos parciales ==========

    @Test
    fun `savePartialData and getPartialData should work correctly`() = runTest {
        val downloadId = "test-download"
        val bytesDownloaded = 5000L

        val saveResult = repository.savePartialData(downloadId, bytesDownloaded)
        assertTrue(saveResult.isSuccess)

        val getResult = repository.getPartialData(downloadId)
        assertTrue(getResult.isSuccess)
        assertEquals(bytesDownloaded, getResult.getOrNull())
    }

    @Test
    fun `getPartialData should return 0 for non-existent id`() = runTest {
        val result = repository.getPartialData("non-existent")
        assertTrue(result.isSuccess)
        assertEquals(0L, result.getOrNull())
    }

    // ========== Tests de historial ==========

    @Test
    fun `getHistory should return only completed downloads`() = runTest {
        val completed1 = createTestDownload(
            id = "1",
            fileName = "completed1.zip",
            status = DownloadStatus.Completed("/path/completed1.zip", 1024L)
        )
        val completed2 = createTestDownload(
            id = "2",
            fileName = "completed2.zip",
            status = DownloadStatus.Completed("/path/completed2.zip", 2048L)
        )
        val downloading = createTestDownload(
            id = "3",
            status = DownloadStatus.Downloading(500L, 1000L)
        )

        repository.saveDownload(completed1)
        repository.saveDownload(completed2)
        repository.saveDownload(downloading)

        val result = repository.getHistory()
        assertTrue(result.isSuccess)

        val history = result.getOrNull()
        assertEquals(2, history?.size)
        assertTrue(history?.all { it.fileName.contains("completed") } == true)
    }

    @Test
    fun `getHistory should filter by category`() = runTest {
        val videoDownload = createTestDownload(
            id = "1",
            fileName = "video.mp4",
            category = Category.VIDEO,
            status = DownloadStatus.Completed("/path/video.mp4", 1024L)
        )
        val audioDownload = createTestDownload(
            id = "2",
            fileName = "audio.mp3",
            category = Category.AUDIO,
            status = DownloadStatus.Completed("/path/audio.mp3", 512L)
        )

        repository.saveDownload(videoDownload)
        repository.saveDownload(audioDownload)

        val filter = DownloadHistoryFilter(category = Category.VIDEO)
        val result = repository.getHistory(filter)

        assertTrue(result.isSuccess)
        val history = result.getOrNull()
        assertEquals(1, history?.size)
        assertEquals("video.mp4", history?.first()?.fileName)
    }

    @Test
    fun `getHistory should filter by search query`() = runTest {
        val download1 = createTestDownload(
            id = "1",
            fileName = "important-document.pdf",
            url = "https://example.com/doc.pdf",
            status = DownloadStatus.Completed("/path/doc.pdf", 1024L)
        )
        val download2 = createTestDownload(
            id = "2",
            fileName = "random-file.zip",
            url = "https://example.com/random.zip",
            status = DownloadStatus.Completed("/path/random.zip", 2048L)
        )

        repository.saveDownload(download1)
        repository.saveDownload(download2)

        val filter = DownloadHistoryFilter(searchQuery = "important")
        val result = repository.getHistory(filter)

        assertTrue(result.isSuccess)
        val history = result.getOrNull()
        assertEquals(1, history?.size)
        assertEquals("important-document.pdf", history?.first()?.fileName)
    }

    @Test
    fun `getHistory should filter by date range`() = runTest {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - (24 * 60 * 60 * 1000)
        val twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000)

        val recentDownload = createTestDownload(
            id = "1",
            createdAt = now,
            status = DownloadStatus.Completed("/path/recent.zip", 1024L)
        )
        val oldDownload = createTestDownload(
            id = "2",
            createdAt = twoDaysAgo,
            status = DownloadStatus.Completed("/path/old.zip", 1024L)
        )

        repository.saveDownload(recentDownload)
        repository.saveDownload(oldDownload)

        val filter = DownloadHistoryFilter(
            dateRange = DateRange(start = oneDayAgo, end = now + 1000)
        )
        val result = repository.getHistory(filter)

        assertTrue(result.isSuccess)
        val history = result.getOrNull()
        assertEquals(1, history?.size)
        assertEquals("1", history?.first()?.id)
    }

    @Test
    fun `getHistory should sort by completion date descending`() = runTest {
        val now = System.currentTimeMillis()

        val download1 = createTestDownload(
            id = "1",
            createdAt = now - 3000,
            status = DownloadStatus.Completed("/path/file1.zip", 1024L)
        )
        val download2 = createTestDownload(
            id = "2",
            createdAt = now - 1000,
            status = DownloadStatus.Completed("/path/file2.zip", 1024L)
        )
        val download3 = createTestDownload(
            id = "3",
            createdAt = now - 2000,
            status = DownloadStatus.Completed("/path/file3.zip", 1024L)
        )

        repository.saveDownload(download1)
        repository.saveDownload(download2)
        repository.saveDownload(download3)

        val result = repository.getHistory()
        assertTrue(result.isSuccess)

        val history = result.getOrNull()
        assertEquals(3, history?.size)
        // Verificar orden descendente por fecha
        assertEquals("2", history?.get(0)?.id) // Más reciente
        assertEquals("3", history?.get(1)?.id)
        assertEquals("1", history?.get(2)?.id) // Más antiguo
    }

    @Test
    fun `clearHistory should remove only completed downloads from storage`() = runTest {
        val completed = createTestDownload(
            id = "1",
            status = DownloadStatus.Completed("/path/file.zip", 1024L)
        )
        val downloading = createTestDownload(
            id = "2",
            status = DownloadStatus.Downloading(500L, 1000L)
        )

        repository.saveDownload(completed)
        repository.saveDownload(downloading)

        val result = repository.clearHistory()
        assertTrue(result.isSuccess)

        val history = repository.getHistory().getOrNull()
        assertEquals(0, history?.size)

        val allDownloads = repository.getAllDownloads().getOrNull()
        assertEquals(1, allDownloads?.size)
        assertEquals("2", allDownloads?.first()?.id)
    }

    @Test
    fun `removeHistoryEntry should remove only completed download`() = runTest {
        val completed = createTestDownload(
            id = "completed-1",
            status = DownloadStatus.Completed("/path/file.zip", 1024L)
        )

        repository.saveDownload(completed)

        val result = repository.removeHistoryEntry("completed-1")
        assertTrue(result.isSuccess)

        val retrieved = repository.getDownload("completed-1")
        assertNull(retrieved.getOrNull())
    }

    @Test
    fun `removeHistoryEntry should fail for non-completed download`() = runTest {
        val downloading = createTestDownload(
            id = "downloading-1",
            status = DownloadStatus.Downloading(500L, 1000L)
        )

        repository.saveDownload(downloading)

        val result = repository.removeHistoryEntry("downloading-1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not completed") == true)
    }

    @Test
    fun `removeHistoryEntry should fail for non-existent id`() = runTest {
        val result = repository.removeHistoryEntry("non-existent")
        assertTrue(result.isFailure)
    }

    // ========== Tests de thread-safety ==========

    @Test
    fun `repository should handle concurrent operations safely`() = runTest {
        // Este test simula operaciones concurrentes
        val download = createTestDownload()
        repository.saveDownload(download)

        // Múltiples operaciones de lectura deberían ser seguras
        val results = List(10) {
            repository.getDownload(download.id)
        }

        assertTrue(results.all { it.isSuccess })
        assertTrue(results.all { it.getOrNull()?.id == download.id })
    }

    // ========== Helpers ==========

    private fun createTestDownload(
        id: String = "test-id-${System.currentTimeMillis()}",
        fileName: String = "test-file.zip",
        url: String = "https://example.com/$fileName",
        category: Category = Category.GENERAL,
        status: DownloadStatus = DownloadStatus.Queued(),
        createdAt: Long = System.currentTimeMillis()
    ) = DownloadItem(
        id = id,
        fileName = fileName,
        url = url,
        filePath = "/downloads/$fileName",
        category = category,
        priority = Priority.NORMAL,
        status = status,
        createdAt = createdAt,
        speedLimit = null,
        localPath = null,
        hash = null,
        metadata = DownloadMetadata()
    )
}

/**
 * Mock de PersistentStorage para testing
 */
class MockPersistentStorage : PersistentStorage {
    private val downloadsStorage = mutableMapOf<String, String>()
    private val partialDataStorage = mutableMapOf<String, String>()

    override fun saveDownloads(data: String) {
        downloadsStorage["downloads"] = data
    }

    override fun loadDownloads(): String? {
        return downloadsStorage["downloads"]
    }

    override fun savePartialData(id: String, data: String) {
        partialDataStorage[id] = data
    }

    override fun getPartialData(id: String): String? {
        return partialDataStorage[id]
    }
}
