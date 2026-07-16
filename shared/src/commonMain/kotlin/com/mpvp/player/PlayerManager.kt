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

    /** 状态更新防抖作业 */
    private var stateUpdateJob: Job? = null

    /** 协程作用域 */
    private val scope = CoroutineScope(Dispatchers.Main)

    /** 进度更新间隔（毫秒） */
    private const val PROGRESS_UPDATE_INTERVAL = 500L

    /** 状态更新防抖延迟（毫秒） */
    private const val STATE_UPDATE_DEBOUNCE = 50L

    /**
     * 初始化播放器
     *
     * @param url 视频地址
     */
    fun initialize(url: String) {
        release()

        val listener = object : PlayerEventListener {
            override fun onPlayingStateChanged(isPlaying: Boolean) {
                debounceUpdate {
                    _state.value = _state.value.copy(
                        isPlaying = isPlaying,
                        playState = if (isPlaying) PlayStateEnum.PLAYING else PlayStateEnum.PAUSED
                    )
                }
            }

            override fun onBufferingStateChanged(isBuffering: Boolean) {
                debounceUpdate {
                    _state.value = _state.value.copy(
                        isBuffering = isBuffering,
                        isLoading = isBuffering
                    )
                }
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
                debounceUpdate {
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        playState = PlayStateEnum.COMPLETED,
                        isComplete = true
                    )
                }
                stopProgressUpdate()
            }

            override fun onError(errorMessage: String, errorCode: Int) {
                debounceUpdate {
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = errorMessage,
                        playState = PlayStateEnum.ERROR,
                        isPlaying = false,
                        isLoading = false
                    )
                }
                stopProgressUpdate()
            }

            override fun onPrepared(duration: Long) {
                debounceUpdate {
                    _state.value = _state.value.copy(
                        duration = duration,
                        isLoading = false,
                        playState = PlayStateEnum.READY
                    )
                }
            }
        }

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
    }

    /**
     * 暂停
     */
    fun pause() {
        mediaPlayer?.pause()
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
     * 获取播放器实例
     */
    fun getMediaPlayer(): MediaPlayer? = mediaPlayer

    /**
     * 释放播放器
     */
    fun release() {
        stopProgressUpdate()
        cancelStateUpdateJob()
        mediaPlayer?.release()
        mediaPlayer = null
        _state.value = PlayerState(playState = PlayStateEnum.RELEASED)
    }

    /**
     * 切换全屏状态
     */
    fun toggleFullscreen() {
        debounceUpdate {
            _state.value = _state.value.copy(
                isFullscreen = !_state.value.isFullscreen
            )
        }
    }

    /**
     * 设置亮度
     */
    fun setBrightness(brightness: Float) {
        _state.value = _state.value.copy(
            brightness = brightness.coerceIn(0f, 1f)
        )
    }

    /**
     * 防抖状态更新
     */
    private fun debounceUpdate(block: () -> Unit) {
        cancelStateUpdateJob()
        stateUpdateJob = scope.launch {
            delay(STATE_UPDATE_DEBOUNCE)
            block()
        }
    }

    /**
     * 取消状态更新作业
     */
    private fun cancelStateUpdateJob() {
        stateUpdateJob?.cancel()
        stateUpdateJob = null
    }

    /**
     * 停止进度更新
     */
    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }
}
