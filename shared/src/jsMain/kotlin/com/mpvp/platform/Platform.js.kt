package com.mpvp.platform

/**
 * Web平台实现
 */
class WebPlatform : Platform {
    override val name: String = "Web"
    override val isAndroid: Boolean = false
    override val isDesktop: Boolean = false
    override val isIOS: Boolean = false
    override val isWeb: Boolean = true
}

/**
 * 获取平台实例 - Web实现
 */
actual fun getPlatform(): Platform = WebPlatform()

/**
 * Web文件扫描器
 *
 * Web平台受浏览器安全限制，无法直接扫描文件系统
 * 通过File API让用户手动选择文件
 */
class WebFileScanner : PlatformFileScanner {

    override suspend fun scanVideoFiles(directory: String?): List<String> {
        // Web平台无法直接扫描文件系统
        // 用户通过文件选择器手动选择视频文件
        return emptyList()
    }

    override suspend fun getFileInfo(filePath: String): FileInfo? {
        // Web平台无法通过路径获取文件信息
        return null
    }

    override suspend fun generateThumbnail(videoPath: String, timeMs: Long): String? {
        // Web平台可通过Canvas + Video元素生成缩略图，暂不实现
        return null
    }

    override suspend fun getVideoDuration(videoPath: String): Long {
        // Web平台需要通过HTML5 Video元素获取时长
        return 0L
    }
}

/**
 * Web文件选择器
 *
 * 通过HTML5 input[type=file]选择文件
 */
class WebFilePicker : PlatformFilePicker {

    override suspend fun pickVideoFile(): String? {
        // Web平台通过input[type=file]选择文件
        // 实际实现通过JS interop调用
        return null
    }

    override suspend fun pickMultipleVideoFiles(): List<String> {
        return emptyList()
    }

    override suspend fun pickDirectory(): String? {
        return null
    }
}