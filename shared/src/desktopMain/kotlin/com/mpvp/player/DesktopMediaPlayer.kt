package com.mpvp.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.mpvp.model.PlayerState
import com.mpvp.model.PlayStateEnum

/**
 * Desktop 平台播放器实现
 *
 * 当前为跨平台可编译的基础实现，使用模拟进度推进。
 * 后续可替换为 JavaFX Media / VLCJ 等真实后端，
 * 只需保证实现 [MediaPlayer] 接口即可。
 *
 * @property listener 播放器事件监听器
 */
class DesktopMediaPlayer(
    private val listener: PlayerEventListener
) : MediaPlayer {

    /** 协程作用域 */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** 播放器状态 */
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    /** 视频地址 */
    private var videoUrl: String = ""

    /** 是否已释放 */
    private var isReleased = false

    /** 进度更新任务 */
    private var progressJob: Job? = null

    override fun initialize(url: String) {
        this.videoUrl = url
        releaseInternal()

        _state.value = PlayerState(
            playState = PlayStateEnum.PREPARING,
            isLoading = true
        )

        // 模拟异步准备过程
        scope.launch {
            delay(300)
            if (isReleased) return@launch
            // 假设视频时长 5 分钟（真实场景由后端提供）
            val duration = 300_000L
            _state.value = _state.value.copy(
                duration = duration,
                isLoading = false,
                playState = PlayStateEnum.READY
            )
            listener.onPrepared(duration)
        }
    }

    /**
     * 开始进度更新
     */
    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (!isReleased && _state.value.isPlaying) {
                delay(500)
                val current = (_state.value.currentPosition + 500).coerceAtMost(_state.value.duration)
                _state.value = _state.value.copy(currentPosition = current)
                listener.onProgressChanged(current, _state.value.duration)
            }
        }
    }

    override fun play() {
        if (isReleased) return
        _state.value = _state.value.copy(
            isPlaying = true,
            playState = PlayStateEnum.PLAYING
        )
        listener.onPlayingStateChanged(true)
        startProgressUpdate()
    }

    override fun pause() {
        if (isReleased) return
        progressJob?.cancel()
        _state.value = _state.value.copy(
            isPlaying = false,
            playState = PlayStateEnum.PAUSED
        )
        listener.onPlayingStateChanged(false)
    }

    override fun stop() {
        if (isReleased) return
        progressJob?.cancel()
        _state.value = _state.value.copy(
            isPlaying = false,
            currentPosition = 0L,
            playState = PlayStateEnum.STOPPED
        )
    }

    override fun seekTo(positionMs: Long) {
        if (isReleased) return
        _state.value = _state.value.copy(currentPosition = positionMs)
        listener.onProgressChanged(positionMs, _state.value.duration)
    }

    override fun setPlaybackSpeed(speed: Float) {
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    override fun setVolume(volume: Float) {
        _state.value = _state.value.copy(volume = volume.coerceIn(0f, 1f), isMuted = volume <= 0f)
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
        releaseInternal()
        _state.value = PlayerState(playState = PlayStateEnum.RELEASED)
    }

    private fun releaseInternal() {
        isReleased = false
        progressJob?.cancel()
        progressJob = null
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
