package com.mpvp.model

import kotlinx.serialization.Serializable

/**
 * 播放器配置类
 *
 * 存储播放器的全局配置信息，持久化到DataStore
 *
 * @property autoPlay 是否自动播放
 * @property rememberPlayPosition 是否记住播放位置
 * @property defaultPlaybackSpeed 默认播放速度
 * @property hardwareDecode 是否使用硬件解码
 * @property backgroundPlay 是否允许后台播放
 * @property themeMode 主题模式
 * @property gridColumns 网格列数（0表示自适应）
 * @property showDuration 是否显示时长
 * @property cacheSizeMB 缓存大小（MB）
 * @property danmakuEnabled 弹幕是否开启
 * @property danmakuOpacity 弹幕透明度（0.0 ~ 1.0）
 * @property danmakuSpeed 弹幕速度
 * @property subtitleEnabled 字幕是否开启
 * @property subtitleFontSize 字幕字体大小
 */
@Serializable
data class PlayerConfig(
    val autoPlay: Boolean = true,
    val rememberPlayPosition: Boolean = true,
    val defaultPlaybackSpeed: Float = 1.0f,
    val hardwareDecode: Boolean = true,
    val backgroundPlay: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val gridColumns: Int = 0,
    val showDuration: Boolean = true,
    val cacheSizeMB: Int = 500,
    val danmakuEnabled: Boolean = false,
    val danmakuOpacity: Float = 0.8f,
    val danmakuSpeed: Float = 1.0f,
    val subtitleEnabled: Boolean = true,
    val subtitleFontSize: Int = 16
)

/**
 * 主题模式枚举
 */
@Serializable
enum class ThemeMode {
    /** 亮色主题 */
    LIGHT,

    /** 暗色主题 */
    DARK,

    /** 跟随系统 */
    SYSTEM
}

/**
 * UI状态密封类
 *
 * 用于表示UI的不同状态，支持加载中、成功、错误、空状态
 */
sealed interface UiState<out T> {

    /**
     * 加载中状态
     */
    object Loading : UiState<Nothing>

    /**
     * 成功状态
     */
    data class Success<T>(val data: T) : UiState<T>

    /**
     * 错误状态
     */
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>

    /**
     * 空状态
     */
    object Empty : UiState<Nothing>
}

/**
 * 播放历史记录实体
 *
 * @property id 记录ID
 * @property videoId 视频ID
 * @property videoTitle 视频标题
 * @property videoUrl 视频地址
 * @property coverUrl 封面地址
 * @property duration 视频总时长
 * @property playPosition 播放位置
 * @property playProgress 播放进度百分比
 * @property playTime 播放时间戳
 * @property sourceType 来源类型
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
    val playTime: Long = System.currentTimeMillis(),
    val sourceType: VideoSourceType = VideoSourceType.NETWORK
) {

    /**
     * 获取格式化的播放时间
     */
    fun getFormattedPlayTime(): String {
        return TimeFormatter.formatTimeAgo(playTime)
    }

    /**
     * 获取格式化的播放进度
     */
    fun getFormattedProgress(): String {
        return "${(playProgress * 100).toInt()}%"
    }
}

/**
 * 收藏分组实体
 *
 * @property id 分组ID
 * @property name 分组名称
 * @property coverUrls 分组内视频封面列表（用于展示）
 * @property videoCount 视频数量
 * @property createTime 创建时间
 */
@Serializable
data class FavoriteGroup(
    val id: String,
    val name: String,
    val coverUrls: List<String> = emptyList(),
    val videoCount: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)
