package com.mpvp.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * 下载状态枚举
 */
@Serializable
enum class DownloadStatus(val displayName: String) {
    /** 等待中 */
    WAITING("等待中"),
    /** 下载中 */
    DOWNLOADING("下载中"),
    /** 已暂停 */
    PAUSED("已暂停"),
    /** 已完成 */
    COMPLETED("已完成"),
    /** 失败 */
    FAILED("失败")
}

/**
 * 下载任务数据模型
 *
 * 支持视频/音乐/电台等媒体内容的下载管理，包含进度跟踪、
 * 速度计算、断点续传等信息。
 *
 * @property id 下载任务唯一标识
 * @property title 媒体标题
 * @property sourceUrl 下载源地址
 * @property localPath 本地存储路径
 * @property mediaType 媒体类型
 * @property mediaId 关联的媒体项ID
 * @property coverUrl 封面地址
 * @property fileSize 文件总大小（字节）
 * @property downloadedSize 已下载大小（字节）
 * @property status 下载状态
 * @property progress 下载进度（0-100）
 * @property speed 下载速度（字节/秒）
 * @property errorCode 错误码
 * @property errorMessage 错误信息
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 * @property completedAt 完成时间
 */
@Serializable
data class DownloadTask(
    val id: String,
    val title: String,
    val sourceUrl: String,
    val localPath: String,
    val mediaType: MediaType,
    val mediaId: String,
    val coverUrl: String? = null,
    val fileSize: Long = 0,
    val downloadedSize: Long = 0,
    val status: DownloadStatus = DownloadStatus.WAITING,
    val progress: Int = 0,
    val speed: Long = 0,
    val errorCode: Int = 0,
    val errorMessage: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val completedAt: Long? = null
) {
    /** 计算进度百分比 */
    fun calculateProgress(): Int {
        if (fileSize <= 0) return 0
        return ((downloadedSize.toDouble() / fileSize) * 100).toInt().coerceIn(0, 100)
    }

    /** 是否可暂停 */
    fun canPause(): Boolean = status == DownloadStatus.DOWNLOADING

    /** 是否可继续 */
    fun canResume(): Boolean = status == DownloadStatus.PAUSED

    /** 是否可重试 */
    fun canRetry(): Boolean = status == DownloadStatus.FAILED

    /** 是否可删除 */
    fun canDelete(): Boolean = status != DownloadStatus.DOWNLOADING

    /** 格式化下载速度 */
    fun formatSpeed(): String {
        return when {
            speed <= 0 -> ""
            speed < 1024 -> "${speed}B/s"
            speed < 1024 * 1024 -> "${speed / 1024}KB/s"
            else -> String.format("%.1fMB/s", speed / (1024.0 * 1024))
        }
    }

    /** 格式化文件大小 */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes <= 0 -> "未知"
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${bytes / 1024}KB"
            bytes < 1024 * 1024 * 1024 -> String.format("%.1fMB", bytes / (1024.0 * 1024))
            else -> String.format("%.1fGB", bytes / (1024.0 * 1024 * 1024))
        }
    }
}