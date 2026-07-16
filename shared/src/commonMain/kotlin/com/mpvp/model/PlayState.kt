package com.mpvp.model

import kotlinx.serialization.Serializable

/**
 * 播放器状态数据类
 *
 * 封装视频播放器的所有状态信息，用于UI层响应式更新
 * 包含播放状态、缓冲状态、进度、倍速、音量等
 *
 * @property isPlaying 是否正在播放
 * @property isBuffering 是否正在缓冲
 * @property isFullscreen 是否全屏
 * @property currentPosition 当前播放位置（毫秒）
 * @property duration 视频总时长（毫秒）
 * @property bufferedPosition 已缓冲位置（毫秒）
 * @property playbackSpeed 播放倍速（0.5x ~ 3.0x）
 * @property volume 音量（0.0 ~ 1.0）
 * @property isMuted 是否静音
 * @property brightness 亮度（0.0 ~ 1.0）
 * @property errorMessage 错误信息
 * @property playState 播放状态枚举
 * @property isLoading 是否正在加载
 * @property isError 是否发生错误
 * @property isComplete 是否播放完成
 */
@Serializable
data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isFullscreen: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val brightness: Float = 0.5f,
    val errorMessage: String? = null,
    val playState: PlayStateEnum = PlayStateEnum.IDLE,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isComplete: Boolean = false
) {

    /**
     * 获取播放进度百分比（0 ~ 1）
     */
    fun getProgressPercent(): Float {
        return if (duration > 0) {
            (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    /**
     * 获取缓冲进度百分比（0 ~ 1）
     */
    fun getBufferedPercent(): Float {
        return if (duration > 0) {
            (bufferedPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    /**
     * 获取格式化的当前时间
     */
    fun getFormattedCurrentPosition(): String {
        return TimeFormatter.formatDuration(currentPosition)
    }

    /**
     * 获取格式化的总时长
     */
    fun getFormattedDuration(): String {
        return TimeFormatter.formatDuration(duration)
    }

    /**
     * 剩余时间（毫秒）
     */
    fun getRemainingTime(): Long {
        return (duration - currentPosition).coerceAtLeast(0L)
    }

    /**
     * 获取格式化的剩余时间
     */
    fun getFormattedRemainingTime(): String {
        return TimeFormatter.formatDuration(getRemainingTime())
    }

    /**
     * 是否可以快进
     */
    fun canSeekForward(): Boolean = currentPosition < duration

    /**
     * 是否可以快退
     */
    fun canSeekBackward(): Boolean = currentPosition > 0
}

/**
 * 播放状态枚举
 *
 * 定义播放器的不同播放状态
 */
@Serializable
enum class PlayStateEnum {
    /** 空闲状态 - 初始状态 */
    IDLE,

    /** 准备中 - 正在加载视频 */
    PREPARING,

    /** 缓冲中 - 正在缓冲数据 */
    BUFFERING,

    /** 准备就绪 - 可以开始播放 */
    READY,

    /** 播放中 - 正在播放视频 */
    PLAYING,

    /** 已暂停 - 播放已暂停 */
    PAUSED,

    /** 已停止 - 播放已停止 */
    STOPPED,

    /** 播放完成 - 视频播放结束 */
    COMPLETED,

    /** 错误状态 - 发生错误 */
    ERROR,

    /** 释放状态 - 播放器已释放 */
    RELEASED
}

/**
 * 播放速度枚举
 *
 * 预定义的播放速度选项
 */
@Serializable
enum class PlaybackSpeed(val value: Float, val displayName: String) {
    SPEED_0_5(0.5f, "0.5x"),
    SPEED_0_75(0.75f, "0.75x"),
    SPEED_1_0(1.0f, "1.0x (正常)"),
    SPEED_1_25(1.25f, "1.25x"),
    SPEED_1_5(1.5f, "1.5x"),
    SPEED_1_75(1.75f, "1.75x"),
    SPEED_2_0(2.0f, "2.0x"),
    SPEED_3_0(3.0f, "3.0x");

    companion object {
        /**
         * 默认播放速度
         */
        val DEFAULT = SPEED_1_0

        /**
         * 长按快进速度
         */
        val LONG_PRESS_SPEED = SPEED_3_0

        /**
         * 根据速度值获取枚举
         */
        fun fromValue(value: Float): PlaybackSpeed {
            return values().find { it.value == value } ?: DEFAULT
        }

        /**
         * 获取所有速度值列表
         */
        fun getAllSpeeds(): List<Float> {
            return values().map { it.value }
        }
    }
}
