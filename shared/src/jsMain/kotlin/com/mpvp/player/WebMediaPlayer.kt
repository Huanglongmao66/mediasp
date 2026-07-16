package com.mpvp.player

import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.events.Event
import com.mpvp.model.PlayerState
import com.mpvp.model.PlayStateEnum

/**
 * Web 平台播放器实现
 *
 * 使用 HTML5 Video API 实现视频播放
 * 支持本地视频文件和网络视频流
 *
 * @property listener 播放器事件监听器
 */
class WebMediaPlayer(
    private val listener: PlayerEventListener
) : MediaPlayer {

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var videoUrl: String = ""

    private var videoElement: HTMLVideoElement? = null

    private var isReleased = false

    /**
     * 获取HTML Video元素（用于绑定到DOM）
     */
    fun getVideoElement(): HTMLVideoElement? = videoElement

    override fun initialize(url: String) {
        this.videoUrl = url
        release()

        _state.value = PlayerState(
            playState = PlayStateEnum.PREPARING,
            isLoading = true
        )

        val video = document.createElement("video") as HTMLVideoElement
        video.controls = false
        video.crossOrigin = "anonymous"
        video.preload = "auto"

        video.onloadedmetadata = { _: Event ->
            if (!isReleased) {
                val duration = video.duration.toLong() * 1000
                _state.value = _state.value.copy(
                    duration = duration,
                    isLoading = false,
                    playState = PlayStateEnum.READY
                )
                listener.onPrepared(duration)
                startProgressUpdate()
            }
        }

        video.onplaying = { _: Event ->
            if (!isReleased) {
                _state.value = _state.value.copy(
                    isPlaying = true,
                    playState = PlayStateEnum.PLAYING
                )
                listener.onPlayingStateChanged(true)
            }
        }

        video.onpause = { _: Event ->
            if (!isReleased) {
                _state.value = _state.value.copy(
                    isPlaying = false,
                    playState = PlayStateEnum.PAUSED
                )
                listener.onPlayingStateChanged(false)
            }
        }

        video.onended = { _: Event ->
            if (!isReleased) {
                _state.value = _state.value.copy(
                    isPlaying = false,
                    playState = PlayStateEnum.COMPLETED,
                    isComplete = true
                )
                listener.onPlayCompleted()
            }
        }

        video.onwaiting = { _: Event ->
            if (!isReleased) {
                _state.value = _state.value.copy(
                    isBuffering = true,
                    isLoading = true
                )
                listener.onBufferingStateChanged(true)
            }
        }

        video.oncanplay = { _: Event ->
            if (!isReleased) {
                _state.value = _state.value.copy(
                    isBuffering = false,
                    isLoading = false
                )
                listener.onBufferingStateChanged(false)
            }
        }

        video.onerror = { _: Event ->
            if (!isReleased) {
                val errorMessage = video.error?.message ?: "播放错误"
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = errorMessage,
                    playState = PlayStateEnum.ERROR,
                    isLoading = false
                )
                listener.onError(errorMessage)
            }
        }

        video.src = url
        videoElement = video
    }

    private fun startProgressUpdate() {
        GlobalScope.launch {
            while (!isReleased && _state.value.isPlaying) {
                delay(500)
                videoElement?.let { video ->
                    val position = (video.currentTime * 1000).toLong()
                    val duration = (video.duration * 1000).toLong()
                    val buffered = if (video.buffered.length > 0) {
                        (video.buffered.end(video.buffered.length - 1) * 1000).toLong()
                    } else {
                        0L
                    }

                    _state.value = _state.value.copy(
                        currentPosition = position,
                        duration = duration,
                        bufferedPosition = buffered
                    )
                    listener.onProgressChanged(position, duration)
                    listener.onBufferedPositionChanged(buffered)
                }
            }
        }
    }

    override fun play() {
        if (isReleased) return
        videoElement?.play()
    }

    override fun pause() {
        if (isReleased) return
        videoElement?.pause()
    }

    override fun stop() {
        if (isReleased) return
        videoElement?.pause()
        videoElement?.currentTime = 0.0
        _state.value = _state.value.copy(
            isPlaying = false,
            currentPosition = 0L,
            playState = PlayStateEnum.STOPPED
        )
    }

    override fun seekTo(positionMs: Long) {
        if (isReleased) return
        videoElement?.currentTime = positionMs.toDouble() / 1000.0
        _state.value = _state.value.copy(currentPosition = positionMs)
        listener.onProgressChanged(positionMs, _state.value.duration)
    }

    override fun setPlaybackSpeed(speed: Float) {
        videoElement?.playbackRate = speed.toDouble()
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    override fun setVolume(volume: Float) {
        videoElement?.volume = volume.toDouble()
        _state.value = _state.value.copy(volume = volume.coerceIn(0f, 1f), isMuted = volume <= 0f)
    }

    override fun setMuted(muted: Boolean) {
        videoElement?.muted = muted
        _state.value = _state.value.copy(isMuted = muted)
    }

    override fun getCurrentPosition(): Long {
        return videoElement?.let { (it.currentTime * 1000).toLong() } ?: _state.value.currentPosition
    }

    override fun getDuration(): Long {
        return videoElement?.let { (it.duration * 1000).toLong() } ?: _state.value.duration
    }

    override fun getBufferedPosition(): Long {
        return videoElement?.let {
            if (it.buffered.length > 0) {
                (it.buffered.end(it.buffered.length - 1) * 1000).toLong()
            } else {
                0L
            }
        } ?: _state.value.bufferedPosition
    }

    override fun isPlaying(): Boolean {
        return videoElement?.paused == false
    }

    override fun isBuffering(): Boolean {
        return videoElement?.readyState == HTMLVideoElement.HAVE_CURRENT_DATA &&
               videoElement?.paused == false
    }

    override fun release() {
        isReleased = true
        videoElement?.pause()
        videoElement?.src = ""
        videoElement?.load()
        videoElement = null
        _state.value = PlayerState(playState = PlayStateEnum.RELEASED)
    }
}

/**
 * Web 平台播放器工厂实现
 */
actual class MediaPlayerFactory actual constructor() {
    actual fun create(listener: PlayerEventListener): MediaPlayer {
        return WebMediaPlayer(listener)
    }
}
