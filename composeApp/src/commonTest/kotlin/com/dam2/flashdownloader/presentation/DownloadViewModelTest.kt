package com.dam2.flashdownloader.presentation

import com.dam2.flashdownloader.data.repository.MockPersistentStorage
import com.dam2.flashdownloader.data.repository.DownloadRepositoryImpl
import com.dam2.flashdownloader.domain.model.*
import com.dam2.flashdownloader.domain.repository.DownloadRepository
import com.dam2.flashdownloader.platform.NotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Tests unitarios para DownloadViewModel
 * Verifica la lógica de presentación, estados y operaciones
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DownloadViewModelTest {

    private lateinit var viewModel: DownloadViewModel
    private lateinit var repository: DownloadRepository
    private lateinit var notificationManager: MockNotificationManager
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = DownloadRepositoryImpl(MockPersistentStorage())
        notificationManager = MockNotificationManager()
        viewModel = DownloadViewModel(repository, notificationManager)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== Tests de inicialización ==========

    @Test
    fun `viewModel should initialize with empty downloads list`() = runTest {
        advanceUntilIdle()

        val downloads = viewModel.downloads.value
        assertEquals(0, downloads.size)
    }

    @Test
    fun `viewModel should load existing downloads on init`() = runTest {
        // Pre-poblar repositorio
        val download = createTestDownload()
        repository.saveDownload(download)

        // Crear nuevo ViewModel
        val newViewModel = DownloadViewModel(repository, notificationManager)
        advanceUntilIdle()

        val downloads = newViewModel.downloads.value
        assertEquals(1, downloads.size)
        assertEquals(download.id, downloads.first().id)
    }

    // ========== Tests de addDownload ==========

    @Test
    fun `addDownload should create new download with correct properties`() = runTest {
        val url = "https://example.com/file.zip"
        val fileName = "test-file.zip"
        val category = Category.COMPRESSED
        val priority = Priority.HIGH

        viewModel.addDownload(
            url = url,
            fileName = fileName,
            category = category,
            priority = priority
        )
        advanceUntilIdle()

        val downloads = viewModel.downloads.value
        assertEquals(1, downloads.size)

        val download = downloads.first()
        assertEquals(fileName, download.fileName)
        assertEquals(url, download.url)
        assertEquals(category, download.category)
        assertEquals(priority, download.priority)
        assertTrue(download.status is DownloadStatus.Queued)
    }

    @Test
    fun `addDownload should use default category and priority if not specified`() = runTest {
        viewModel.addDownload(
            url = "https://example.com/file.zip",
            fileName = "file.zip"
        )
        advanceUntilIdle()

        val download = viewModel.downloads.value.first()
        assertEquals(Category.GENERAL, download.category)
        assertEquals(Priority.NORMAL, download.priority)
    }

    @Test
    fun `addDownload should generate unique IDs`() = runTest {
        viewModel.addDownload("https://example.com/file1.zip", "file1.zip")
        viewModel.addDownload("https://example.com/file2.zip", "file2.zip")
        advanceUntilIdle()

        val downloads = viewModel.downloads.value
        assertEquals(2, downloads.size)
        assertNotEquals(downloads[0].id, downloads[1].id)
    }

    // ========== Tests de pauseDownload ==========

    @Test
    fun `pauseDownload should change status to Paused`() = runTest {
        // Crear descarga en estado Downloading
        val download = createTestDownload(
            status = DownloadStatus.Downloading(
                bytesDownloaded = 500L,
                totalBytes = 1000L,
                speed = 100L,
                elapsedSeconds = 5L
            )
        )
        repository.saveDownload(download)

        // Recrear ViewModel para cargar la descarga
        viewModel = DownloadViewModel(repository, notificationManager)
        advanceUntilIdle()

        viewModel.pauseDownload(download.id)
        advanceUntilIdle()

        val updated = viewModel.downloads.value.find { it.id == download.id }
        assertNotNull(updated)
        assertTrue(updated.status is DownloadStatus.Paused)

        val pausedStatus = updated.status as DownloadStatus.Paused
        assertEquals(500L, pausedStatus.bytesDownloaded)
        assertEquals(1000L, pausedStatus.totalBytes)
        assertEquals(5L, pausedStatus.elapsedSeconds)
    }

    @Test
    fun `pauseDownload should do nothing if download is not Downloading`() = runTest {
        val download = createTestDownload(status = DownloadStatus.Queued())
        repository.saveDownload(download)

        viewModel = DownloadViewModel(repository, notificationManager)
        advanceUntilIdle()

        viewModel.pauseDownload(download.id)
        advanceUntilIdle()

        val updated = viewModel.downloads.value.find { it.id == download.id }
        assertTrue(updated?.status is DownloadStatus.Queued)
    }

    // ========== Tests de resumeDownload ==========

    @Test
    fun `resumeDownload should change status from Paused to Downloading`() = runTest {
        val download = createTestDownload(
            status = DownloadStatus.Paused(
                bytesDownloaded = 500L,
                totalBytes = 1000L,
                elapsedSeconds = 10L
            )
        )
        repository.saveDownload(download)

        viewModel = DownloadViewModel(repository, notificationManager)
        advanceUntilIdle()

        viewModel.resumeDownload(download.id)
        advanceUntilIdle()

        val updated = viewModel.downloads.value.find { it.id == download.id }
        assertNotNull(updated)
        assertTrue(updated.status is DownloadStatus.Downloading)

        val downloadingStatus = updated.status as DownloadStatus.Downloading
        assertEquals(500L, downloadingStatus.bytesDownloaded)
        assertEquals(1000L, downloadingStatus.totalBytes)
        assertEquals(10L, downloadingStatus.elapsedSeconds)
    }

    @Test
    fun `resumeDownload should do nothing if download is not Paused`() = runTest {
        val download = createTestDownload(status = DownloadStatus.Completed("/path", 1000L))
        repository.saveDownload(download)

        viewModel = DownloadViewModel(repository, notificationManager)
        advanceUntilIdle()

        viewModel.resumeDownload(download.id)
        advanceUntilIdle()

        val updated = viewModel.downloads.value.find { it.id == download.id }
        assertTrue(updated?.status is DownloadStatus.Completed)
    }

    // ========== Tests de cancelDownload ==========

    @Test
    fun `cancelDownload should change status to Cancelled`() = runTest {
        val download = createTestDownload(
            status = DownloadStatus.Downloading(100L, 1000L)
        )
        repository.saveDownload(download)

        viewModel = DownloadViewModel(repository, notificationManager)
        advanceUntilIdle()

        viewModel.cancelDownload(download.id)
        advanceUntilIdle()

        val updated = viewModel.downloads.value.find { it.id == download.id }
        assertTrue(updated?.status is DownloadStatus.Cancelled)
    }

    @Test
    fun `cancelDownload should cancel notification`() = runTest {
        val download = createTestDownload()
        repository.saveDownload(download)

        viewModel = DownloadViewModel(repository, notificationManager)
        advanceUntilIdle()

        viewModel.cancelDownload(download.id)
        advanceUntilIdle()

        assertTrue(notificationManager.cancelledNotifications.contains(download.id))
    }

    // ========== Tests de removeDownload ==========

    @Test
    fun `removeDownload should delete download from list`() = runTest {
        val download = createTestDownload()
        repository.saveDownload(download)

        viewModel = DownloadViewModel(repository, notificationManager)
        advanceUntilIdle()

        assertEquals(1, viewModel.downloads.value.size)

        viewModel.removeDownload(download.id)
        advanceUntilIdle()

        assertEquals(0, viewModel.downloads.value.size)
    }

    @Test
    fun `removeDownload should cancel notification`() = runTest {
        val download = createTestDownload()
        repository.saveDownload(download)

        viewModel = DownloadViewModel(repository, notificationManager)
        advanceUntilIdle()

        viewModel.removeDownload(download.id)
        advanceUntilIdle()

        assertTrue(notificationManager.cancelledNotifications.contains(download.id))
    }

    // ========== Tests de notificaciones ==========

    @Test
    fun `clearAllNotifications should cancel all notifications`() = runTest {
        viewModel.clearAllNotifications()

        assertTrue(notificationManager.allNotificationsCancelled)
    }

    // ========== Tests de isLoading ==========

    @Test
    fun `isLoading should be false after initialization completes`() = runTest {
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
    }

    // ========== Helpers ==========

    private fun createTestDownload(
        id: String = "test-${System.currentTimeMillis()}",
        fileName: String = "test.zip",
        status: DownloadStatus = DownloadStatus.Queued()
    ) = DownloadItem(
        id = id,
        fileName = fileName,
        url = "https://example.com/$fileName",
        filePath = "/downloads/$fileName",
        category = Category.GENERAL,
        priority = Priority.NORMAL,
        status = status,
        createdAt = System.currentTimeMillis(),
        metadata = DownloadMetadata()
    )
}

/**
 * Mock de NotificationManager para testing
 */
class MockNotificationManager : NotificationManager() {
    val progressNotifications = mutableListOf<String>()
    val completedNotifications = mutableListOf<String>()
    val failedNotifications = mutableListOf<String>()
    val cancelledNotifications = mutableListOf<String>()
    var allNotificationsCancelled = false

    override fun showDownloadProgress(
        downloadId: String,
        fileName: String,
        progress: Float,
        speed: Long,
        isPaused: Boolean
    ) {
        progressNotifications.add(downloadId)
    }

    override fun showDownloadCompleted(downloadId: String, fileName: String, filePath: String) {
        completedNotifications.add(downloadId)
    }

    override fun showDownloadFailed(downloadId: String, fileName: String, error: String) {
        failedNotifications.add(downloadId)
    }

    override fun cancelNotification(downloadId: String) {
        cancelledNotifications.add(downloadId)
    }

    override fun cancelAllNotifications() {
        allNotificationsCancelled = true
    }
}
