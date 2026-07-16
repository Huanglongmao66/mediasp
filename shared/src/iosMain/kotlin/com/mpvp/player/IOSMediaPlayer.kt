package com.mpvp.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.mpvp.model.PlayerState
import com.mpvp.model.PlayStateEnum

/**
 * iOS 平台播放器实现
 *
 * 实际项目中应使用 AVPlayer 实现
 * 当前为框架实现，提供状态管理基础
 *
 * @property listener 播放器事件监听器
 */
class IOSMediaPlayer(
    private val listener: PlayerEventListener
) : MediaPlayer {

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var videoUrl: String = ""

    override fun initialize(url: String) {
        this.videoUrl = url
        _state.value = PlayerState(
            playState = PlayStateEnum.PREPARING,
            isLoading = true
        )
        // TODO: 使用 AVPlayer 实现实际播放
        listener.onPrepared(0L)
    }

    override fun play() {
        _state.value = _state.value.copy(isPlaying = true, playState = PlayStateEnum.PLAYING)
    }

    override fun pause() {
        _state.value = _state.value.copy(isPlaying = false, playState = PlayStateEnum.PAUSED)
    }

    override fun stop() {
        _state.value = _state.value.copy(isPlaying = false, currentPosition = 0L, playState = PlayStateEnum.STOPPED)
    }

    override fun seekTo(positionMs: Long) {
        _state.value = _state.value.copy(currentPosition = positionMs)
    }

    override fun setPlaybackSpeed(speed: Float) {
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    override fun setVolume(volume: Float) {
        _state.value = _state.value.copy(volume = volume)
    }

    override fun setMuted(muted: Boolean) {
        _state.value = _state.value.copy(isMuted = muted)
    }

    override fun getCurrentPosition(): Long = _state.value.currentPosition
    override fun getDuration(): Long = _state.value.duration
    override fun getBufferedPosition(): Long = _state.value.bufferedPosition
    override fun isPlaying(): Boolean = _state.value.isPlaying
    override fun isBuffering(): Boolean = _state.value.isBuffering

    override fun release() {
        _state.value = PlayerState(playState = PlayStateEnum.RELEASED)
    }
}

/**
 * iOS 平台播放器工厂实现
 */
actual class MediaPlayerFactory actual constructor() {
    actual fun create(listener: PlayerEventListener): MediaPlayer {
        return IOSMediaPlayer(listener)
    }
}
