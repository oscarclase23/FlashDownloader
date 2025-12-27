package com.dam2.flashdownloader.domain.model

/**
 * Prioridad de una descarga (afecta el orden de procesamiento)
 */
enum class Priority(val level: Int, val displayName: String) {
    LOW(1, "🟢 Baja"),
    NORMAL(2, "🟡 Normal"),
    HIGH(3, "🟠 Alta"),
    CRITICAL(4, "🔴 Crítica");

    companion object {
        fun fromLevel(level: Int): Priority {
            return entries.find { it.level == level } ?: NORMAL
        }
    }
}
