package com.mpvp.platform

import platform.Foundation.NSFileManager
import platform.Foundation.NSFileType
import platform.Foundation.NSFileTypeDirectory
import platform.Foundation.NSFileSize
import platform.Foundation.attributesOfItemAtPath

/**
 * iOS平台实现
 */
class IOSPlatform : Platform {
    override val name: String = "iOS"
    override val isAndroid: Boolean = false
    override val isDesktop: Boolean = false
    override val isIOS: Boolean = true
    override val isWeb: Boolean = false
}

/**
 * 获取平台实例 - iOS实现
 */
actual fun getPlatform(): Platform = IOSPlatform()

/**
 * iOS文件扫描器
 *
 * 扫描iOS应用沙盒中的视频文件
 */
class IOSFileScanner : PlatformFileScanner {

    private val videoExtensions = setOf(
        "mp4", "mkv", "avi", "mov", "flv", "webm", "m4v", "3gp", "ts", "m3u8"
    )

    override suspend fun scanVideoFiles(directory: String?): List<String> {
        val scanDir = directory ?: getDefaultVideoDirectory()
        val videoFiles = mutableListOf<String>()

        try {
            val fileManager = NSFileManager.defaultManager
            scanDirectoryRecursively(scanDir, videoFiles, fileManager)
        } catch (e: Exception) {
            // 扫描失败
        }

        return videoFiles
    }

    private fun scanDirectoryRecursively(
        directoryPath: String,
        videoFiles: MutableList<String>,
        fileManager: NSFileManager
    ) {
        val contents = fileManager.contentsOfDirectoryAtPath(directoryPath, null)
            ?: return

        contents.forEach { item ->
            val itemName = item.toString()
            val fullPath = "$directoryPath/$itemName"

            val isDir = try {
                val attrs = fileManager.attributesOfItemAtPath(fullPath)
                (attrs[NSFileType] as? String) == NSFileTypeDirectory
            } catch (e: Exception) {
                false
            }

            if (isDir) {
                scanDirectoryRecursively(fullPath, videoFiles, fileManager)
            } else {
                val extension = itemName.substringAfterLast(".", "").lowercase()
                if (videoExtensions.contains(extension)) {
                    videoFiles.add(fullPath)
                }
            }
        }
    }

    private fun getDefaultVideoDirectory(): String {
        val documentsDir = NSFileManager.defaultManager.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask
        ).firstOrNull()?.path ?: ""
        return documentsDir
    }

    override suspend fun getFileInfo(filePath: String): FileInfo? {
        return try {
            val fileManager = NSFileManager.defaultManager
            val attrs = fileManager.attributesOfItemAtPath(filePath)
            val name = filePath.substringAfterLast("/")
            val size = (attrs[NSFileSize] as? Number)?.toLong() ?: 0L
            val isDir = (attrs[NSFileType] as? String) == NSFileTypeDirectory

            FileInfo(
                path = filePath,
                name = name,
                size = size,
                lastModified = 0L,
                isDirectory = isDir
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun generateThumbnail(videoPath: String, timeMs: Long): String? {
        return null
    }

    override suspend fun getVideoDuration(videoPath: String): Long {
        return 0L
    }
}