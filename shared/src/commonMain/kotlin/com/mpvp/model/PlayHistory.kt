package com.mpvp.model

import com.mpvp.utils.TimeFormatter
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * 播放历史记录类
 *
 * 记录用户的播放历史，包括播放位置、播放时间等信息
 *
 * @property id 历史记录唯一标识符
 * @property videoId 视频ID
 * @property videoTitle 视频标题
 * @property videoUrl 视频播放地址
 * @property coverUrl 视频封面地址
 * @property duration 视频总时长（毫秒）
 * @property playPosition 上次播放位置（毫秒）
 * @property playProgress 播放进度百分比（0-1）
 * @property playTime 播放时间戳
 * @property sourceType 视频来源类型
 */
@Serializable
data class PlayHistory(
    val id: String,
    val videoId: String,
    val videoTitle: String,
    val videoUrl: String,
    val coverUrl: String? = null,
    val duration: Long = 0L,
    val playPosition: Long = 0L,
    val playProgress: Float = 0f,
    val playTime: Long = Clock.System.now().toEpochMilliseconds(),
    val sourceType: VideoSourceType = VideoSourceType.NETWORK
) {

    /**
     * 获取格式化时长字符串
     */
    fun getFormattedDuration(): String {
        return if (duration > 0) {
            TimeFormatter.formatDuration(duration)
        } else {
            "未知"
        }
    }

    /**
     * 获取格式化播放位置字符串
     */
    fun getFormattedPosition(): String {
        return TimeFormatter.formatDuration(playPosition)
    }

    /**
     * 获取格式化播放时间字符串
     */
    fun getFormattedPlayTime(): String {
        return TimeFormatter.formatDate(playTime)
    }

    /**
     * 转换为VideoItem
     */
    fun toVideoItem(): VideoItem {
        return VideoItem(
            id = videoId,
            title = videoTitle,
            videoUrl = videoUrl,
            coverUrl = coverUrl,
            duration = duration,
            sourceType = sourceType,
            lastPlayPosition = playPosition
        )
    }
}
