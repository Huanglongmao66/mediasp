package com.mpvp.utils

import com.mpvp.model.VideoFormat
import com.mpvp.model.VideoItem
import com.mpvp.model.VideoSourceType

/**
 * 文件解析工具类
 *
 * 提供视频文件解析、路径处理、文件信息获取等功能
 */
object FileUtils {

    /** 支持的视频文件扩展名 */
    val SUPPORTED_VIDEO_EXTENSIONS = listOf(
        "mp4", "mkv", "avi", "mov", "flv", "webm", "m4v", "3gp", "ts", "m3u8"
    )

    /**
     * 从文件路径中提取文件名
     *
     * @param path 文件路径
     * @param withExtension 是否包含扩展名
     * @return 文件名
     */
    fun getFileName(path: String, withExtension: Boolean = false): String {
        val normalizedPath = path.replace("\\", "/")
        val fullName = normalizedPath.substringAfterLast("/")
        return if (withExtension) {
            fullName
        } else {
            fullName.substringBeforeLast(".")
        }
    }

    /**
     * 从文件路径中获取文件扩展名
     *
     * @param path 文件路径
     * @return 扩展名（不含点，小写）
     */
    fun getFileExtension(path: String): String {
        val normalizedPath = path.replace("\\", "/")
        val fileName = normalizedPath.substringAfterLast("/")
        return fileName.substringAfterLast(".", "").lowercase()
    }

    /**
     * 判断文件是否为视频文件
     *
     * @param path 文件路径
     * @return 是否为视频文件
     */
    fun isVideoFile(path: String): Boolean {
        val ext = getFileExtension(path)
        return SUPPORTED_VIDEO_EXTENSIONS.contains(ext)
    }

    /**
     * 根据文件路径创建VideoItem
     *
     * @param path 文件路径
     * @param fileSize 文件大小（字节）
     * @param duration 视频时长（毫秒）
     * @return VideoItem对象
     */
    fun createVideoItemFromPath(
        path: String,
        fileSize: Long = 0L,
        duration: Long = 0L
    ): VideoItem {
        val fileName = getFileName(path)
        val extension = getFileExtension(path)
        val videoFormat = VideoFormat.fromExtension(extension)
        val sourceType = if (path.startsWith("http://") || path.startsWith("https://")) {
            VideoSourceType.NETWORK
        } else {
            VideoSourceType.LOCAL
        }

        return VideoItem(
            id = generateVideoId(path),
            title = fileName,
            videoUrl = path,
            localPath = if (sourceType == VideoSourceType.LOCAL) path else null,
            sourceType = sourceType,
            videoFormat = videoFormat,
            fileSize = fileSize,
            duration = duration
        )
    }

    /**
     * 根据URL创建网络视频VideoItem
     *
     * @param url 视频URL
     * @param title 视频标题
     * @param coverUrl 封面URL
     * @return VideoItem对象
     */
    fun createNetworkVideoItem(
        url: String,
        title: String,
        coverUrl: String? = null
    ): VideoItem {
        val videoFormat = VideoFormat.fromUrl(url)
        val sourceType = when (videoFormat) {
            VideoFormat.LIVE -> VideoSourceType.LIVE
            else -> VideoSourceType.NETWORK
        }

        return VideoItem(
            id = generateVideoId(url),
            title = title,
            videoUrl = url,
            coverUrl = coverUrl,
            sourceType = sourceType,
            videoFormat = videoFormat
        )
    }

    /**
     * 生成视频ID
     *
     * 使用路径的哈希值作为ID
     *
     * @param path 文件路径或URL
     * @return 视频ID
     */
    fun generateVideoId(path: String): String {
        return "video_${path.hashCode().toUInt()}"
    }

    /**
     * 获取文件所在目录
     *
     * @param path 文件路径
     * @return 目录路径
     */
    fun getFileDirectory(path: String): String {
        val normalizedPath = path.replace("\\", "/")
        return normalizedPath.substringBeforeLast("/", "")
    }

    /**
     * 判断是否为网络URL
     *
     * @param url URL字符串
     * @return 是否为网络URL
     */
    fun isNetworkUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://") ||
               url.startsWith("rtmp://") || url.startsWith("rtsp://")
    }

    /**
     * 判断是否为直播源
     *
     * @param url URL字符串
     * @return 是否为直播源
     */
    fun isLiveStream(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return lowerUrl.startsWith("rtmp://") || lowerUrl.startsWith("rtsp://") ||
               (lowerUrl.contains(".m3u8") && !lowerUrl.contains("#EXT-X-ENDLIST"))
    }

    /**
     * 格式化文件路径
     *
     * 统一路径分隔符为正斜杠
     *
     * @param path 原始路径
     * @return 格式化后的路径
     */
    fun normalizePath(path: String): String {
        return path.replace("\\", "/")
    }

    /**
     * 从URL中提取文件名
     *
     * @param url URL
     * @param withExtension 是否包含扩展名
     * @return 文件名
     */
    fun getFileNameFromUrl(url: String, withExtension: Boolean = false): String {
        val cleanUrl = url.substringBefore("?").substringBefore("#")
        return getFileName(cleanUrl, withExtension)
    }
}
