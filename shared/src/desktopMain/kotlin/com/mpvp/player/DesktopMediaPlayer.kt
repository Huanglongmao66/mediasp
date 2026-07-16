package com.mpvp.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.mpvp.model.PlayerState
import com.mpvp.model.PlayStateEnum

/**
 * Desktop 平台播放器实现
 *
 * 桌面端使用模拟播放器实现，实际项目中可集成：
 * - VLCJ (VLC媒体播放器封装)
 * - JavaFX Media
 * - GStreamer
 *
 * 当前为框架实现，提供状态管理基础
 *
 * @property listener 播放器事件监听器
 */
class DesktopMediaPlayer(
    private val listener: PlayerEventListener
) : MediaPlayer {

    /** 播放器状态 */
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    /** 视频地址 */
    private var videoUrl: String = ""

    /** 模拟播放线程 */
    private var playThread: Thread? = null

    /** 是否已释放 */
    private var isReleased = false

    override fun initialize(url: String) {
        this.videoUrl = url
        _state.value = PlayerState(
            playState = PlayStateEnum.PREPARING,
            isLoading = true
        )

        // 模拟加载过程
        Thread {
            try {
                Thread.sleep(500) // 模拟加载延迟

                if (isReleased) return@Thread

                // 模拟获取视频时长（实际应从媒体文件解析）
                val duration = 60000L // 模拟1分钟视频

                _state.value = _state.value.copy(
                    duration = duration,
                    isLoading = false,
                    playState = PlayStateEnum.READY
                )
                listener.onPrepared(duration)

            } catch (e: InterruptedException) {
                // 线程被中断，正常退出
            } catch (e: Exception) {
                if (!isReleased) {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = "视频加载失败: ${e.message}",
                        playState = PlayStateEnum.ERROR,
                        isLoading = false
                    )
                    listener.onError("视频加载失败: ${e.message}")
                }
            }
        }.also { playThread = it }.start()
    }

    override fun play() {
        if (isReleased) return

        _state.value = _state.value.copy(
            isPlaying = true,
            playState = PlayStateEnum.PLAYING
        )
        listener.onPlayingStateChanged(true)

        // 启动进度更新线程
        playThread?.interrupt()
        playThread = Thread {
            try {
                while (!isReleased && _state.value.isPlaying) {
                    Thread.sleep(1000)
                    val currentPosition = _state.value.currentPosition + (1000L * _state.value.playbackSpeed).toLong()

                    if (currentPosition >= _state.value.duration && _state.value.duration > 0) {
                        _state.value = _state.value.copy(
                            currentPosition = _state.value.duration,
                            isPlaying = false,
                            playState = PlayStateEnum.COMPLETED,
                            isComplete = true
                        )
                        listener.onPlayCompleted()
                        break
                    }

                    _state.value = _state.value.copy(currentPosition = currentPosition)
                    listener.onProgressChanged(currentPosition, _state.value.duration)
                }
            } catch (e: InterruptedException) {
                // 线程被中断，正常退出
            }
        }.also { playThread = it }
        playThread?.start()
    }

    override fun pause() {
        if (isReleased) return

        playThread?.interrupt()
        _state.value = _state.value.copy(
            isPlaying = false,
            playState = PlayStateEnum.PAUSED
        )
        listener.onPlayingStateChanged(false)
    }

    override fun stop() {
        if (isReleased) return

        playThread?.interrupt()
        _state.value = _state.value.copy(
            isPlaying = false,
            currentPosition = 0L,
            playState = PlayStateEnum.STOPPED
        )
    }

    override fun seekTo(positionMs: Long) {
        if (isReleased) return

        val targetPosition = positionMs.coerceIn(0L, _state.value.duration)
        _state.value = _state.value.copy(currentPosition = targetPosition)
        listener.onProgressChanged(targetPosition, _state.value.duration)
    }

    override fun setPlaybackSpeed(speed: Float) {
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    override fun setVolume(volume: Float) {
        _state.value = _state.value.copy(volume = volume.coerceIn(0f, 1f))
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
        isReleased = true
        playThread?.interrupt()
        playThread = null
        _state.value = PlayerState(playState = PlayStateEnum.RELEASED)
    }
}

/**
 * Desktop 平台播放器工厂实现
 */
actual class MediaPlayerFactory actual constructor() {

    actual fun create(listener: PlayerEventListener): MediaPlayer {
        return DesktopMediaPlayer(listener)
    }
}
