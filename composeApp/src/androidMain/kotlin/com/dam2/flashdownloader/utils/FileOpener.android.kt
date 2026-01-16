package com.dam2.flashdownloader.utils

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.lang.ref.WeakReference

actual object FileOpener {

    // Variable para guardar el contexto de la App de forma estática
    private var contextRef: WeakReference<Context>? = null

    // ✅ IMPORTANTE: Llamaremos a esto desde la MainActivity
    fun initialize(context: Context) {
        contextRef = WeakReference(context.applicationContext)
    }

    // Función auxiliar para obtener contexto de forma segura
    private fun getContext(): Context? {
        return contextRef?.get()
    }

    // Abre el archivo específico (requiere configuración de FileProvider en Manifest)
    actual fun openFile(filePath: String): Boolean {
        val context = getContext() ?: return false

        return try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "El archivo no existe", Toast.LENGTH_SHORT).show()
                return false
            }

            // Intenta obtener la URI segura.
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla (ej: falta FileProvider), mostramos mensaje pero no crasheamos
            Toast.makeText(context, "No se pudo abrir el archivo.", Toast.LENGTH_SHORT).show()
            false
        }
    }

    // Abre la carpeta de descargas del sistema (La opción sencilla que pediste)
    actual fun openFileLocation(filePath: String): Boolean {
        val context = getContext() ?: return false

        return try {
            // Abrimos la App de Descargas oficial de Android
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Si falla la app de descargas, intentamos abrir un explorador genérico
            try {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setDataAndType(Uri.parse(filePath), "*/*")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }
}