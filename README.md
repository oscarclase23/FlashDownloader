# ⚡ FlashDownloader – Gestor de Descargas Kotlin Multiplatform

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-1.6.11-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Windows](https://img.shields.io/badge/Platform-Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)
![Status](https://img.shields.io/badge/Build-Passing-success?style=for-the-badge)

**FlashDownloader** es un gestor de descargas moderno, robusto y multiplataforma desarrollado con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform**. Permite gestionar, pausar, reanudar y organizar descargas de archivos tanto en dispositivos **Android** como en escritorio (**Windows/Linux/macOS**), compartiendo más del 90% del código de negocio y UI.

---

## 🚀 Funcionalidades Principales

### 📥 Motor de Descargas Avanzado

* **Soporte Multi-protocolo**: Descarga de archivos vía HTTP/HTTPS utilizando **Ktor Client**.
* **Gestión de Estado**: Capacidad para **Pausar, Reanudar y Cancelar** descargas en tiempo real.
* **Control de Ancho de Banda**: Limitador de velocidad integrado (`BandwidthLimiter`) para no saturar la red.
* **Persistencia**: Recuperación automática del progreso tras cerrar la aplicación.

### 📊 Estadísticas en Tiempo Real

* Monitorización visual de:
* Velocidad de descarga (MB/s).
* Progreso (Barra y porcentaje).
* Tamaño total y descargado.
* Estimación de tiempo restante (ETA).



### 🛠️ Herramientas y Utilidades

* **Validación de Integridad**: Cálculo automático de Hashes (**MD5, SHA-1, SHA-256**) para verificar los archivos descargados.
* **Smart Clipboard**: Detección automática de enlaces en el portapapeles al añadir una nueva descarga.
* **Drag & Drop**: Arrastrar enlaces directamente a la ventana (en Desktop) para iniciar descargas.
* **Categorización**: Organización automática por tipos (Música, Vídeo, Documentos, Imágenes, Comprimidos).

### 🎨 Interfaz Adaptativa (Material 3)

* Diseño **Responsive** que se adapta a pantallas táctiles (Android) y ratón/teclado (Desktop).
* **Tema Oscuro/Claro** consistente en todas las plataformas.
* **Notificaciones Nativas**:
* Android: Servicio en primer plano con notificación de progreso.
* Desktop: Notificaciones de sistema y bandeja.



---

## 🧩 Arquitectura

El proyecto sigue una arquitectura **Clean Architecture + MVVM** estricta, maximizando la compartición de código en `commonMain`.

* **Presentation Layer**:
* **UI**: Jetpack Compose (Android) / Compose Desktop (JVM).
* **ViewModel**: `DownloadViewModel` compartido. Gestiona el estado de la UI y los eventos asíncronos.


* **Domain Layer**:
* **Modelos**: `DownloadItem`, `DownloadStatus`, `Category`.
* **Interfaces**: `DownloadRepository`, `DownloadManager`.


* **Data Layer**:
* **Network**: `Ktor` para peticiones HTTP.
* **Storage**: Implementaciones específicas para sistema de archivos:
* Android: `AndroidFileWriter` (ContentResolver/MediaStore).
* JVM: `JvmFileWriter` (Java NIO).


* **DI**: Inyección de dependencias con **Koin**.



---

## 📗 Manual de Usuario

### ▶ Opción 1 — Ejecutar desde el IDE

```bash
git clone https://github.com/oscarclase23/FlashDownloader.git

```

1. Abrir en **IntelliJ IDEA** o **Android Studio**.
2. Sincronizar Gradle.
3. Ejecutar comando según la plataforma:

* **Escritorio (JVM):**
```shell
# Windows
.\gradlew.bat :composeApp:run

# Mac/Linux
./gradlew :composeApp:run

```


* **Android:**
```shell
# Windows
.\gradlew.bat :composeApp:assembleDebug

# Mac/Linux
./gradlew :composeApp:assembleDebug

```



### 💾 Opción 2 — Instaladores (Releases)

Gracias a **GitHub Actions**, cada versión genera automáticamente instaladores optimizados. Descarga la última versión desde:

👉 **[Releases de FlashDownloader](https://github.com/oscarclase23/FlashDownloader/releases/tag/v0.0.6)**

| Plataforma | Archivo | Descripción |
| --- | --- | --- |
| **Windows** | `FlashDownloader-1.0.0.exe` | Instalador ejecutable estándar. |
| **Windows** | `FlashDownloader-1.0.0.msi` | Instalador MSI para despliegues. |
| **Android** | `FlashDownloader-Android.apk` | Paquete de instalación APK. |

---

## 🧪 Pruebas Realizadas

El proyecto incluye una suite de tests automatizados utilizando **Kotlin Test**, **MockK** y **Turbine**.

### Pruebas Unitarias (`commonTest`)

| Componente | Descripción de la Prueba |
| --- | --- |
| **BandwidthLimiter** | Verificación del algoritmo de limitación de velocidad (Token Bucket). |
| **DownloadItem** | Cálculo correcto de progreso, velocidad media y ETA. |
| **Extensions** | Tests de utilidades (formato de bytes a MB/GB, formateo de tiempo). |
| **Category** | Clasificación correcta de extensiones de archivo y filtrado. |
| **Priorities** | Ordenación correcta de la cola de descargas. |

### Pruebas Manuales

* **Android**: Ciclo de vida (rotación, minimizado), permisos de almacenamiento, notificaciones en segundo plano.
* **Desktop**: Redimensionado de ventana, integración con bandeja del sistema, arrastrar y soltar archivos.
* **Red**: Comportamiento ante pérdida de conexión y reanudación exitosa.

---

## 🏁 Conclusiones y Retos

### 🌟 Logros

* Desarrollo de una aplicación completa de gestión de archivos con **código compartido > 90%**.
* Implementación exitosa de un sistema de integración continua (CI/CD) que genera **EXE, MSI y APK** automáticamente.
* Gestión eficiente de concurrencia y flujos de datos (`Flow`) para actualizar la UI a 60fps sin bloqueos.

### ❗ Desafíos Técnicos

1. **Sistema de Archivos Multiplataforma**:
* *Reto:* Android 10+ requiere Scoped Storage (Uri/ContentResolver), mientras que Desktop usa rutas tradicionales (`java.io.File`).
* *Solución:* Patrón `FileWriterFactory` con implementaciones específicas (`AndroidFileWriter` vs `JvmFileWriter`).


2. **Cálculo de Hashes en Archivos Grandes**:
* *Reto:* Calcular MD5/SHA de archivos de >2GB bloqueaba la UI o daba `OutOfMemoryError`.
* *Solución:* Procesamiento por chunks (bloques) dentro de corrutinas en `Dispatchers.IO`.


3. **Empaquetado Nativo**:
* *Reto:* Generar instaladores de Windows correctos desde GitHub Actions.
* *Solución:* Configuración de WiX Toolset y scripts de Gradle personalizados para `packageMsi` y `packageExe`.



---

## 🔗 Repositorio

[https://github.com/oscarclase23/FlashDownloader.git](https://github.com/oscarclase23/FlashDownloader.git)

---

*Desarrollado como proyecto de demostración de capacidades de Kotlin Multiplatform.*
