package com.mpvp.model

import com.mpvp.utils.TimeFormatter
import kotlinx.serialization.Serializable

/**
 * 视频实体类
 *
 * 表示一个视频的完整信息，包括基本信息、播放地址、本地路径等
 * 支持本地视频和网络视频两种模式
 *
 * @property id 视频唯一标识符
 * @property title 视频标题
 * @property videoUrl 视频播放地址（网络URL或本地路径）
 * @property coverUrl 视频封面图片地址
 * @property duration 视频总时长（毫秒）
 * @property localPath 本地文件路径（仅本地视频有值）
 * @property sourceType 视频来源类型（本地/网络）
 * @property videoFormat 视频格式（mp4/m3u8/mkv等）
 * @property fileSize 文件大小（字节，仅本地视频有值）
 * @property lastPlayPosition 上次播放位置（毫秒）
 * @property isFavorite 是否已收藏
 * @property addTime 添加时间戳
 * @property lastPlayTime 上次播放时间戳
 * @property episodeList 剧集列表（电视剧等多集视频）
 * @property currentEpisode 当前播放剧集索引
 */
@Serializable
data class VideoItem(
    val id: String,
    val title: String,
    val videoUrl: String,
    val coverUrl: String? = null,
    val duration: Long = 0L,
    val localPath: String? = null,
    val sourceType: VideoSourceType = VideoSourceType.NETWORK,
    val videoFormat: VideoFormat = VideoFormat.MP4,
    val fileSize: Long = 0L,
    val lastPlayPosition: Long = 0L,
    val isFavorite: Boolean = false,
    val addTime: Long = System.currentTimeMillis(),
    val lastPlayTime: Long = 0L,
    val episodeList: List<VideoEpisode> = emptyList(),
    val currentEpisode: Int = 0
) {

    /**
     * 获取当前播放的剧集
     */
    fun getCurrentEpisode(): VideoEpisode? {
        return episodeList.getOrNull(currentEpisode)
    }

    /**
     * 是否为本地视频
     */
    fun isLocalVideo(): Boolean = sourceType == VideoSourceType.LOCAL

    /**
     * 是否为直播源
     */
    fun isLiveStream(): Boolean = videoFormat == VideoFormat.LIVE

    /**
     * 是否有多集
     */
    fun hasMultipleEpisodes(): Boolean = episodeList.size > 1

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
     * 获取播放地址
     *
     * 如果有多集，返回当前集的播放地址
     * 否则返回videoUrl
     */
    fun getPlayUrl(): String {
        return if (hasMultipleEpisodes()) {
            getCurrentEpisode()?.videoUrl ?: videoUrl
        } else {
            videoUrl
        }
    }

    /**
     * 获取播放进度百分比
     */
    fun getPlayProgressPercent(): Float {
        return if (duration > 0) {
            (lastPlayPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
}

/**
 * 视频来源类型枚举
 */
@Serializable
enum class VideoSourceType {
    /** 本地视频文件 */
    LOCAL,

    /** 网络视频流 */
    NETWORK,

    /** 直播源 */
    LIVE,

    /** CMS解析源 */
    CMS
}

/**
 * 视频格式枚举
 */
@Serializable
enum class VideoFormat(val extension: String, val mimeType: String) {
    MP4("mp4", "video/mp4"),
    MKV("mkv", "video/x-matroska"),
    AVI("avi", "video/x-msvideo"),
    MOV("mov", "video/quicktime"),
    FLV("flv", "video/x-flv"),
    WEBM("webm", "video/webm"),
    M3U8("m3u8", "application/vnd.apple.mpegurl"),
    TS("ts", "video/mp2t"),
    LIVE("live", "application/octet-stream"),
    UNKNOWN("", "application/octet-stream");

    companion object {
        /**
         * 根据文件扩展名获取视频格式
         */
        fun fromExtension(ext: String): VideoFormat {
            val lowerExt = ext.lowercase().removePrefix(".")
            return values().find { it.extension == lowerExt } ?: UNKNOWN
        }

        /**
         * 根据URL判断视频格式
         */
        fun fromUrl(url: String): VideoFormat {
            val lowerUrl = url.lowercase()
            return when {
                lowerUrl.contains(".m3u8") -> M3U8
                lowerUrl.contains(".mp4") -> MP4
                lowerUrl.contains(".mkv") -> MKV
                lowerUrl.contains(".avi") -> AVI
                lowerUrl.contains(".mov") -> MOV
                lowerUrl.contains(".flv") -> FLV
                lowerUrl.contains(".webm") -> WEBM
                lowerUrl.contains(".ts") -> TS
                lowerUrl.contains("rtmp://") || lowerUrl.contains("rtsp://") -> LIVE
                else -> UNKNOWN
            }
        }

        /**
         * 获取所有支持的视频扩展名
         */
        fun supportedExtensions(): List<String> {
            return values().filter { it != UNKNOWN && it != LIVE }.map { it.extension }
        }
    }
}

/**
 * 视频剧集信息
 *
 * 用于电视剧、动漫等多集视频的剧集信息
 *
 * @property episodeIndex 剧集索引
 * @property title 剧集标题
 * @property videoUrl 剧集视频地址
 * @property duration 剧集时长（毫秒）
 */
@Serializable
data class VideoEpisode(
    val episodeIndex: Int,
    val title: String,
    val videoUrl: String,
    val duration: Long = 0L
)
