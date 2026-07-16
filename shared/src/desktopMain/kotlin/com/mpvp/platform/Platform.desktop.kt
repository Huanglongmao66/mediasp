package com.mpvp.platform

import java.io.File

/**
 * Desktop平台实现
 */
class DesktopPlatform : Platform {
    override val name: String = "Desktop (${System.getProperty("os.name")})"
    override val isAndroid: Boolean = false
    override val isDesktop: Boolean = true
    override val isIOS: Boolean = false
    override val isWeb: Boolean = false
}

/**
 * 获取平台实例 - Desktop实现
 */
actual fun getPlatform(): Platform = DesktopPlatform()

/**
 * Desktop文件扫描器
 *
 * 扫描指定目录下的视频文件
 */
class DesktopFileScanner : PlatformFileScanner {

    /** 支持的视频扩展名 */
    private val videoExtensions = setOf(
        "mp4", "mkv", "avi", "mov", "flv", "webm", "m4v", "3gp", "ts", "m3u8"
    )

    override suspend fun scanVideoFiles(directory: String?): List<String> {
        val scanDir = directory ?: getDefaultVideoDirectory()
        val videoFiles = mutableListOf<String>()

        try {
            val dir = File(scanDir)
            if (dir.exists() && dir.isDirectory) {
                scanDirectoryRecursively(dir, videoFiles)
            }
        } catch (e: Exception) {
            // 扫描失败
        }

        return videoFiles
    }

    /**
     * 递归扫描目录
     */
    private fun scanDirectoryRecursively(directory: File, videoFiles: MutableList<String>) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                scanDirectoryRecursively(file, videoFiles)
            } else {
                val extension = file.extension.lowercase()
                if (videoExtensions.contains(extension)) {
                    videoFiles.add(file.absolutePath)
                }
            }
        }
    }

    /**
     * 获取默认视频目录
     */
    private fun getDefaultVideoDirectory(): String {
        val userHome = System.getProperty("user.home")
        return "$userHome/Videos"
    }

    override suspend fun getFileInfo(filePath: String): FileInfo? {
        return try {
            val file = File(filePath)
            FileInfo(
                path = file.absolutePath,
                name = file.name,
                size = file.length(),
                lastModified = file.lastModified(),
                isDirectory = file.isDirectory
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun generateThumbnail(videoPath: String, timeMs: Long): String? {
        // Desktop平台缩略图生成需要额外的库支持
        return null
    }

    override suspend fun getVideoDuration(videoPath: String): Long {
        // Desktop平台需要使用JavaFX Media或其他库获取视频时长
        return 0L
    }
}

/**
 * Desktop文件选择器
 */
class DesktopFilePicker : PlatformFilePicker {

    override suspend fun pickVideoFile(): String? {
        // 使用java.awt.FileDialog实现文件选择
        val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, "选择视频文件", java.awt.FileDialog.LOAD)
        fileDialog.filenameFilter = java.io.FilenameFilter { _, name ->
            val ext = name.substringAfterLast(".", "").lowercase()
            setOf("mp4", "mkv", "avi", "mov", "flv", "webm", "m4v", "3gp", "ts", "m3u8").contains(ext)
        }
        fileDialog.isVisible = true

        return if (fileDialog.file != null) {
            "${fileDialog.directory}${fileDialog.file}"
        } else {
            null
        }
    }

    override suspend fun pickMultipleVideoFiles(): List<String> {
        // AWT FileDialog不支持多选，返回单个文件
        val file = pickVideoFile()
        return if (file != null) listOf(file) else emptyList()
    }

    override suspend fun pickDirectory(): String? {
        val jFileChooser = javax.swing.JFileChooser()
        jFileChooser.fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY

        val result = jFileChooser.showOpenDialog(null)
        return if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            jFileChooser.selectedFile.absolutePath
        } else {
            null
        }
    }
}
