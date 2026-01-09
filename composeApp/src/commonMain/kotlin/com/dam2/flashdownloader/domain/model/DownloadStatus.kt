package com.dam2.flashdownloader.domain.model

/**
 * Representa el estado actual de una descarga durante su ciclo de vida.
 *
 * Esta clase sellada define todos los posibles estados que puede tener
 * una descarga, desde que se agrega hasta que se completa o falla.
 *
 * ## Ciclo de vida típico:
 * 1. [Queued] - La descarga está en cola esperando iniciar
 * 2. [Downloading] - La descarga está en progreso
 * 3. [Paused] - El usuario pausó la descarga (opcional)
 * 4. [Downloading] - Reanudación después de pausa (opcional)
 * 5. [Completed] - La descarga finalizó exitosamente
 *
 * ## Estados alternativos:
 * - [Failed] - La descarga falló por un error
 * - [Cancelled] - El usuario canceló la descarga
 *
 * ## Propiedades calculadas:
 * - [isActive] - Indica si la descarga está activa (Queued o Downloading)
 * - [canResume] - Indica si la descarga puede ser reanudada (Paused o Failed)
 * - [isTerminal] - Indica si la descarga está en estado final (Completed o Cancelled)
 *
 * @see DownloadItem
 */
sealed class DownloadStatus {
    /**
     * La descarga está en cola esperando su turno.
     *
     * Este estado se usa cuando hay múltiples descargas y se aplica un límite
     * de descargas concurrentes. También se usa cuando se agrega una nueva descarga.
     *
     * @property bytesDownloaded Bytes descargados hasta el momento (para resumir descargas previas)
     * @property totalBytes Total de bytes a descargar (puede ser -1 si es desconocido)
     * @property elapsedSeconds Tiempo acumulado de sesiones previas (para reanudaciones)
     * @property progress Progreso calculado entre 0.0 y 1.0
     */
    data class Queued(
        val bytesDownloaded: Long = 0L,
        val totalBytes: Long = -1L,
        val elapsedSeconds: Long? = null
    ) : DownloadStatus() {
        val progress: Float
            get() = if (totalBytes > 0) (bytesDownloaded.toFloat() / totalBytes.toFloat()) else 0f
    }

    /**
     * La descarga está actualmente en progreso.
     *
     * Este es el estado principal durante una descarga activa. Incluye información
     * sobre progreso, velocidad y tiempo estimado.
     *
     * @property bytesDownloaded Bytes descargados hasta el momento
     * @property totalBytes Total de bytes a descargar (puede ser -1 si es desconocido)
     * @property speed Velocidad actual en bytes por segundo
     * @property elapsedSeconds Tiempo acumulado de descarga activa de sesiones previas (segundos)
     * @property sessionStartTime Timestamp cuando comenzó la sesión actual de descarga
     * @property progress Progreso calculado entre 0.0 y 1.0
     * @property progressPercentage Progreso en porcentaje (0-100)
     * @property estimatedTimeRemaining Tiempo estimado restante en segundos (-1 si no se puede calcular)
     * @property totalElapsedSeconds Tiempo total transcurrido incluyendo la sesión actual
     */
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val speed: Long = 0L,
        val elapsedSeconds: Long = 0L,
        val sessionStartTime: Long = System.currentTimeMillis()
    ) : DownloadStatus() {
        val progress: Float
            get() = if (totalBytes > 0) (bytesDownloaded.toFloat() / totalBytes.toFloat()) else 0f

        val progressPercentage: Int
            get() = (progress * 100).toInt()

        /**
         * Calcula el tiempo restante estimado en segundos.
         * @return Tiempo en segundos, o -1 si no se puede calcular
         */
        val estimatedTimeRemaining: Long
            get() {
                if (speed <= 0 || totalBytes <= 0) return -1L
                val remainingBytes = totalBytes - bytesDownloaded
                return remainingBytes / speed
            }

        /**
         * Calcula el tiempo total transcurrido incluyendo la sesión actual.
         * @return Tiempo total en segundos
         */
        val totalElapsedSeconds: Long
            get() = elapsedSeconds + ((System.currentTimeMillis() - sessionStartTime) / 1000)
    }

    /**
     * La descarga ha sido pausada manualmente por el usuario.
     *
     * En este estado, se preserva el progreso para poder reanudar más tarde.
     *
     * @property bytesDownloaded Bytes descargados antes de pausar
     * @property totalBytes Total de bytes a descargar
     * @property elapsedSeconds Tiempo acumulado de descarga activa hasta la pausa
     * @property progress Progreso calculado entre 0.0 y 1.0
     * @property progressPercentage Progreso en porcentaje (0-100)
     */
    data class Paused(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val elapsedSeconds: Long
    ) : DownloadStatus() {
        val progress: Float
            get() = if (totalBytes > 0) (bytesDownloaded.toFloat() / totalBytes.toFloat()) else 0f

        val progressPercentage: Int
            get() = (progress * 100).toInt()
    }

    /**
     * La descarga ha sido completada exitosamente.
     *
     * Estado final que indica que el archivo se descargó completamente y está
     * disponible en el sistema de archivos.
     *
     * @property filePath Ruta absoluta donde se guardó el archivo
     * @property totalBytes Tamaño total del archivo descargado
     * @property calculatedHash Hash SHA-256 del archivo (opcional, para verificación de integridad)
     */
    data class Completed(
        val filePath: String,
        val totalBytes: Long,
        val calculatedHash: String? = null
    ) : DownloadStatus()

    /**
     * La descarga ha fallado debido a un error.
     *
     * Este estado indica que ocurrió un problema durante la descarga.
     * La descarga puede ser reintentada si el error es recuperable.
     *
     * @property error Mensaje de error descriptivo
     * @property bytesDownloaded Bytes descargados antes del error (para posible reanudación)
     */
    data class Failed(
        val error: String,
        val bytesDownloaded: Long = 0L
    ) : DownloadStatus()

    /**
     * La descarga ha sido cancelada por el usuario.
     *
     * Estado final que indica que el usuario canceló explícitamente la descarga.
     * No se puede reanudar desde este estado.
     */
    data object Cancelled : DownloadStatus()

    /**
     * Verifica si la descarga está en un estado activo.
     * @return true si está en cola o descargando
     */
    val isActive: Boolean
        get() = this is Queued || this is Downloading

    /**
     * Verifica si la descarga puede ser reanudada.
     * @return true si está pausada o falló
     */
    val canResume: Boolean
        get() = this is Paused || this is Failed

    /**
     * Verifica si la descarga está en un estado terminal.
     * @return true si está completada o cancelada
     */
    val isTerminal: Boolean
        get() = this is Completed || this is Cancelled
}
