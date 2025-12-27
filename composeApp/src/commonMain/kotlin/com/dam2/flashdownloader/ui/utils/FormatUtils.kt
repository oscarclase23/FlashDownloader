package com.dam2.flashdownloader.ui.utils

/**
 * Formatea bytes a formato legible (KB, MB, GB, TB)
 */
fun Long.formatBytes(): String {
    if (this < 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = this.toDouble()
    var unitIndex = 0

    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }

    return if (unitIndex == 0) {
        "${this} ${units[0]}"
    } else {
        "%.2f ${units[unitIndex]}".format(size)
    }
}

/**
 * Formatea velocidad en bytes/segundo a formato legible
 */
fun Long.formatSpeed(): String = "${this.formatBytes()}/s"

/**
 * Formatea tiempo en segundos a formato legible
 */
fun Long.formatTime(): String {
    if (this < 0) return "0s"

    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}


