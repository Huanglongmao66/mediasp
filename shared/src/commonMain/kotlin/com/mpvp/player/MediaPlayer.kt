package com.mpvp.player

import com.mpvp.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

/**
 * 跨平台媒体播放器接口
 *
 * 定义播放器的通用功能，各平台需要提供具体实现：
 * - Android: ExoPlayer (androidx.media3)
 * - Desktop: JavaFX Media 或 VLCJ
 * - iOS: AVPlayer
 * - Web: HTML5 Video
 */
interface MediaPlayer {

    /** 播放器状态流 */
    val state: StateFlow<PlayerState>

    /**
     * 初始化播放器
     *
     * @param url 视频地址（本地路径或网络URL）
     */
    fun initialize(url: String)

    /**
     * 播放
     */
    fun play()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 停止播放
     */
    fun stop()

    /**
     * 跳转到指定位置
     *
     * @param positionMs 位置（毫秒）
     */
    fun seekTo(positionMs: Long)

    /**
     * 设置播放速度
     *
     * @param speed 播放速度（0.5 ~ 3.0）
     */
    fun setPlaybackSpeed(speed: Float)

    /**
     * 设置音量
     *
     * @param volume 音量（0.0 ~ 1.0）
     */
    fun setVolume(volume: Float)

    /**
     * 设置静音
     *
     * @param muted 是否静音
     */
    fun setMuted(muted: Boolean)

    /**
     * 获取当前播放位置
     *
     * @return 当前位置（毫秒）
     */
    fun getCurrentPosition(): Long

    /**
     * 获取视频总时长
     *
     * @return 总时长（毫秒）
     */
    fun getDuration(): Long

    /**
     * 获取已缓冲位置
     *
     * @return 缓冲位置（毫秒）
     */
    fun getBufferedPosition(): Long

    /**
     * 是否正在播放
     */
    fun isPlaying(): Boolean

    /**
     * 是否正在缓冲
     */
    fun isBuffering(): Boolean

    /**
     * 释放播放器资源
     */
    fun release()
}

/**
 * 播放器事件回调接口
 */
interface PlayerEventListener {

    /**
     * 播放状态变化
     */
    fun onPlayingStateChanged(isPlaying: Boolean) {}

    /**
     * 缓冲状态变化
     */
    fun onBufferingStateChanged(isBuffering: Boolean) {}

    /**
     * 播放进度变化
     *
     * @param position 当前位置
     * @param duration 总时长
     */
    fun onProgressChanged(position: Long, duration: Long) {}

    /**
     * 缓冲进度变化
     *
     * @param bufferedPosition 缓冲位置
     */
    fun onBufferedPositionChanged(bufferedPosition: Long) {}

    /**
     * 播放完成
     */
    fun onPlayCompleted() {}

    /**
     * 发生错误
     *
     * @param errorMessage 错误信息
     * @param errorCode 错误码
     */
    fun onError(errorMessage: String, errorCode: Int = -1) {}

    /**
     * 视频加载完成，获取到时长
     *
     * @param duration 视频时长（毫秒）
     */
    fun onVideoSizeChanged(width: Int, height: Int) {}

    /**
     * 视频准备完成
     */
    fun onPrepared(duration: Long) {}
}

/**
 * 播放器工厂接口
 *
 * 各平台提供具体的播放器创建实现
 */
expect class MediaPlayerFactory() {
    /**
     * 创建播放器实例
     *
     * @param listener 事件监听器
     * @return 播放器实例
     */
    fun create(listener: PlayerEventListener): MediaPlayer
}
