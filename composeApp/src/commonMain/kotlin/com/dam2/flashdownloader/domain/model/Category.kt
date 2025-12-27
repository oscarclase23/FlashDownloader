package com.dam2.flashdownloader.domain.model

/**
 * Categorías de descargas basadas en el tipo de archivo
 */
enum class Category(val displayName: String, val iconName: String) {
    GENERAL("General", "📦"),
    DOCUMENTS("Documentos", "📄"),
    IMAGES("Imágenes", "🖼️"),
    AUDIO("Audio", "🎵"),
    VIDEO("Vídeo", "🎬"),
    COMPRESSED("Comprimidos", "📚"),
    SOFTWARE("Software", "⚙️");
}
