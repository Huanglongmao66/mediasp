package com.mpvp.player

import com.mpvp.model.PlayStateEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.mpvp.model.PlayerState

/**
 * 播放器管理器
 *
 * 桥接MediaPlayer和ViewModel，统一管理播放器状态
 * 负责播放器的生命周期管理和状态同步
 */
class PlayerManager {

    /** 播放器实例 */
    private var mediaPlayer: MediaPlayer? = null

    /** 播放器状态 */
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    /** 进度更新作业 */
    private var progressJob: Job? = null

    /** 协程作用域 */
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * 初始化播放器
     *
     * @param url 视频地址
     */
    fun initialize(url: String) {
        release()

        // 创建播放器事件监听器
        val listener = object : PlayerEventListener {
            override fun onPlayingStateChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(
                    isPlaying = isPlaying,
                    playState = if (isPlaying) PlayStateEnum.PLAYING else PlayStateEnum.PAUSED
                )
            }

            override fun onBufferingStateChanged(isBuffering: Boolean) {
                _state.value = _state.value.copy(
                    isBuffering = isBuffering,
                    isLoading = isBuffering
                )
            }

            override fun onProgressChanged(position: Long, duration: Long) {
                _state.value = _state.value.copy(
                    currentPosition = position,
                    duration = duration
                )
            }

            override fun onBufferedPositionChanged(bufferedPosition: Long) {
                _state.value = _state.value.copy(bufferedPosition = bufferedPosition)
            }

            override fun onPlayCompleted() {
                _state.value = _state.value.copy(
                    isPlaying = false,
                    playState = PlayStateEnum.COMPLETED,
                    isComplete = true
                )
                stopProgressUpdate()
            }

            override fun onError(errorMessage: String, errorCode: Int) {
                _state.value = _state.value.copy(
                    isError = true,
                    errorMessage = errorMessage,
                    playState = PlayStateEnum.ERROR,
                    isPlaying = false,
                    isLoading = false
                )
                stopProgressUpdate()
            }

            override fun onPrepared(duration: Long) {
                _state.value = _state.value.copy(
                    duration = duration,
                    isLoading = false,
                    playState = PlayStateEnum.READY
                )
                startProgressUpdate()
            }
        }

        // 创建播放器实例
        mediaPlayer = MediaPlayerFactory().create(listener)
        mediaPlayer?.initialize(url)

        _state.value = PlayerState(
            playState = PlayStateEnum.PREPARING,
            isLoading = true
        )
    }

    /**
     * 播放
     */
    fun play() {
        mediaPlayer?.play()
        startProgressUpdate()
    }

    /**
     * 暂停
     */
    fun pause() {
        mediaPlayer?.pause()
        stopProgressUpdate()
    }

    /**
     * 停止
     */
    fun stop() {
        mediaPlayer?.stop()
        stopProgressUpdate()
        _state.value = _state.value.copy(
            currentPosition = 0L,
            playState = PlayStateEnum.STOPPED
        )
    }

    /**
     * 跳转
     */
    fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position)
        _state.value = _state.value.copy(currentPosition = position)
    }

    /**
     * 设置播放速度
     */
    fun setPlaybackSpeed(speed: Float) {
        mediaPlayer?.setPlaybackSpeed(speed)
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    /**
     * 设置音量
     */
    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume)
        _state.value = _state.value.copy(volume = volume, isMuted = volume <= 0f)
    }

    /**
     * 设置静音
     */
    fun setMuted(muted: Boolean) {
        mediaPlayer?.setMuted(muted)
        _state.value = _state.value.copy(isMuted = muted)
    }

    /**
     * 释放播放器
     */
    fun release() {
        stopProgressUpdate()
        mediaPlayer?.release()
        mediaPlayer = null
        _state.value = PlayerState(playState = PlayStateEnum.RELEASED)
    }

    /**
     * 开始进度更新
     */
    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                delay(500) // 每500ms更新一次
                val player = mediaPlayer ?: return@launch
                val position = player.getCurrentPosition()
                val buffered = player.getBufferedPosition()
                _state.value = _state.value.copy(
                    currentPosition = position,
                    bufferedPosition = buffered
                )
            }
        }
    }

    /**
     * 停止进度更新
     */
    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }
}
