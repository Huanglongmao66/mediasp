package com.mpvp.platform

/**
 * 平台接口
 *
 * 定义跨平台需要实现的平台特定功能
 * 各平台（Android/Desktop/iOS/Web）需要提供具体实现
 */
interface Platform {

    /** 平台名称 */
    val name: String

    /** 当前平台是否为Android */
    val isAndroid: Boolean

    /** 当前平台是否为Desktop */
    val isDesktop: Boolean

    /** 当前平台是否为iOS */
    val isIOS: Boolean

    /** 当前平台是否为Web */
    val isWeb: Boolean
}

/**
 * 平台文件扫描器接口
 *
 * 用于扫描本地视频文件，各平台需提供具体实现
 */
interface PlatformFileScanner {

    /**
     * 扫描指定目录下的视频文件
     *
     * @param directory 目录路径，为空则扫描默认目录
     * @return 视频文件路径列表
     */
    suspend fun scanVideoFiles(directory: String? = null): List<String>

    /**
     * 获取文件信息
     *
     * @param filePath 文件路径
     * @return 文件信息对象
     */
    suspend fun getFileInfo(filePath: String): FileInfo?

    /**
     * 生成视频缩略图
     *
     * @param videoPath 视频路径
     * @param timeMs 缩略图时间点（毫秒）
     * @return 缩略图路径
     */
    suspend fun generateThumbnail(videoPath: String, timeMs: Long = 0): String?

    /**
     * 获取视频时长
     *
     * @param videoPath 视频路径
     * @return 视频时长（毫秒）
     */
    suspend fun getVideoDuration(videoPath: String): Long
}

/**
 * 文件信息数据类
 *
 * @property path 文件路径
 * @property name 文件名
 * @property size 文件大小（字节）
 * @property lastModified 最后修改时间
 * @property isDirectory 是否为目录
 */
data class FileInfo(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean = false
)

/**
 * 文件选择器接口
 *
 * 用于在不同平台选择文件或目录
 */
interface PlatformFilePicker {

    /**
     * 选择单个视频文件
     *
     * @return 选中的文件路径，取消选择返回null
     */
    suspend fun pickVideoFile(): String?

    /**
     * 选择多个视频文件
     *
     * @return 选中的文件路径列表
     */
    suspend fun pickMultipleVideoFiles(): List<String>

    /**
     * 选择目录
     *
     * @return 选中的目录路径
     */
    suspend fun pickDirectory(): String?
}

/**
 * 获取当前平台实例
 *
 * 各平台需要提供此函数的实现
 */
expect fun getPlatform(): Platform
