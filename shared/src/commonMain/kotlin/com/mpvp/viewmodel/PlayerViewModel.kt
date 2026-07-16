package com.mpvp.viewmodel

import com.mpvp.model.PlaybackSpeed
import com.mpvp.model.PlayerConfig
import com.mpvp.model.PlayerState
import com.mpvp.model.PlayStateEnum
import com.mpvp.model.VideoItem
import com.mpvp.repository.VideoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 播放器ViewModel
 *
 * 管理视频播放器的所有状态和操作，包括：
 * - 播放控制（播放/暂停/停止）
 * - 进度管理（跳转/快进/快退）
 * - 倍速控制
 * - 音量控制
 * - 全屏切换
 * - 播放进度缓存
 * - 错误处理
 *
 * @property repository 视频仓库
 */
class PlayerViewModel(
    private val repository: VideoRepository
) : BaseViewModel() {

    /** 播放器状态 */
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    /** 当前播放的视频 */
    private val _currentVideo = MutableStateFlow<VideoItem?>(null)
    val currentVideo: StateFlow<VideoItem?> = _currentVideo.asStateFlow()

    /** 播放器配置 */
    private val _config = MutableStateFlow(PlayerConfig())
    val config: StateFlow<PlayerConfig> = _config.asStateFlow()

    /** 是否显示控制器 */
    private val _showController = MutableStateFlow(true)
    val showController: StateFlow<Boolean> = _showController.asStateFlow()

    /** 进度更新协程作业 */
    private var progressUpdateJob: Job? = null

    /** 控制器自动隐藏协程作业 */
    private var controllerHideJob: Job? = null

    init {
        loadConfig()
    }

    /**
     * 加载播放器配置
     */
    private fun loadConfig() {
        launch {
            // 这里应该从DataStore加载配置
            // 暂时使用默认配置
            _config.value = PlayerConfig()
        }
    }

    /**
     * 加载视频并准备播放
     *
     * @param video 视频对象
     */
    fun loadVideo(video: VideoItem) {
        _currentVideo.value = video
        _playerState.value = PlayerState(
            playState = PlayStateEnum.PREPARING,
            isLoading = true,
            playbackSpeed = _config.value.defaultPlaybackSpeed
        )

        // 如果记住播放位置，加载上次的播放进度
        if (_config.value.rememberPlayPosition) {
            launch {
                val savedPosition = repository.getPlayProgress(video.id)
                if (savedPosition > 0) {
                    _playerState.value = _playerState.value.copy(
                        currentPosition = savedPosition
                    )
                }
            }
        }

        // 自动播放
        if (_config.value.autoPlay) {
            play()
        }
    }

    /**
     * 播放视频
     */
    fun play() {
        _playerState.value = _playerState.value.copy(
            isPlaying = true,
            playState = PlayStateEnum.PLAYING,
            isLoading = false,
            isError = false
        )
        startProgressUpdate()
        showControllerAutoHide()
    }

    /**
     * 暂停播放
     */
    fun pause() {
        _playerState.value = _playerState.value.copy(
            isPlaying = false,
            playState = PlayStateEnum.PAUSED
        )
        stopProgressUpdate()
        saveCurrentProgress()
    }

    /**
     * 切换播放/暂停
     */
    fun togglePlayPause() {
        if (_playerState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    /**
     * 停止播放
     */
    fun stop() {
        _playerState.value = _playerState.value.copy(
            isPlaying = false,
            playState = PlayStateEnum.STOPPED,
            currentPosition = 0L
        )
        stopProgressUpdate()
        saveCurrentProgress()
    }

    /**
     * 跳转到指定位置
     *
     * @param position 目标位置（毫秒）
     */
    fun seekTo(position: Long) {
        _playerState.value = _playerState.value.copy(
            currentPosition = position.coerceIn(0L, _playerState.value.duration)
        )
    }

    /**
     * 快进
     *
     * @param milliseconds 快进毫秒数（默认5000ms）
     */
    fun seekForward(milliseconds: Long = 5000L) {
        val newPosition = _playerState.value.currentPosition + milliseconds
        seekTo(newPosition)
    }

    /**
     * 快退
     *
     * @param milliseconds 快退毫秒数（默认5000ms）
     */
    fun seekBackward(milliseconds: Long = 5000L) {
        val newPosition = _playerState.value.currentPosition - milliseconds
        seekTo(newPosition)
    }

    /**
     * 设置播放速度
     *
     * @param speed 播放速度
     */
    fun setPlaybackSpeed(speed: Float) {
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }

    /**
     * 设置倍速（使用枚举）
     *
     * @param speed 倍速枚举
     */
    fun setPlaybackSpeed(speed: PlaybackSpeed) {
        setPlaybackSpeed(speed.value)
    }

    /**
     * 长按快进（3倍速）
     */
    fun startLongPressSpeed() {
        setPlaybackSpeed(PlaybackSpeed.LONG_PRESS_SPEED)
    }

    /**
     * 结束长按快进，恢复原速度
     */
    fun endLongPressSpeed() {
        setPlaybackSpeed(_config.value.defaultPlaybackSpeed)
    }

    /**
     * 设置音量
     *
     * @param volume 音量（0.0 ~ 1.0）
     */
    fun setVolume(volume: Float) {
        _playerState.value = _playerState.value.copy(
            volume = volume.coerceIn(0f, 1f),
            isMuted = volume <= 0f
        )
    }

    /**
     * 增加音量
     *
     * @param delta 音量增量
     */
    fun increaseVolume(delta: Float = 0.1f) {
        setVolume(_playerState.value.volume + delta)
    }

    /**
     * 减少音量
     *
     * @param delta 音量减量
     */
    fun decreaseVolume(delta: Float = 0.1f) {
        setVolume(_playerState.value.volume - delta)
    }

    /**
     * 切换静音
     */
    fun toggleMute() {
        _playerState.value = _playerState.value.copy(
            isMuted = !_playerState.value.isMuted
        )
    }

    /**
     * 设置亮度
     *
     * @param brightness 亮度（0.0 ~ 1.0）
     */
    fun setBrightness(brightness: Float) {
        _playerState.value = _playerState.value.copy(
            brightness = brightness.coerceIn(0f, 1f)
        )
    }

    /**
     * 切换全屏
     */
    fun toggleFullscreen() {
        _playerState.value = _playerState.value.copy(
            isFullscreen = !_playerState.value.isFullscreen
        )
    }

    /**
     * 显示控制器
     */
    fun showController() {
        _showController.value = true
        showControllerAutoHide()
    }

    /**
     * 隐藏控制器
     */
    fun hideController() {
        _showController.value = false
        controllerHideJob?.cancel()
    }

    /**
     * 控制器自动隐藏
     *
     * @param delayMillis 延迟时间（毫秒）
     */
    private fun showControllerAutoHide(delayMillis: Long = 5000L) {
        controllerHideJob?.cancel()
        controllerHideJob = launch {
            delay(delayMillis)
            if (_playerState.value.isPlaying) {
                _showController.value = false
            }
        }
    }

    /**
     * 开始进度更新
     */
    private fun startProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = launch {
            while (isActive && _playerState.value.isPlaying) {
                delay(1000) // 每秒更新一次
                val currentState = _playerState.value
                val newPosition = currentState.currentPosition + (1000L * currentState.playbackSpeed).toLong()

                if (newPosition >= currentState.duration && currentState.duration > 0) {
                    // 播放完成
                    _playerState.value = currentState.copy(
                        currentPosition = currentState.duration,
                        isPlaying = false,
                        playState = PlayStateEnum.COMPLETED,
                        isComplete = true
                    )
                    onPlayComplete()
                    break
                } else {
                    _playerState.value = currentState.copy(
                        currentPosition = newPosition
                    )
                }
            }
        }
    }

    /**
     * 停止进度更新
     */
    private fun stopProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    /**
     * 播放完成回调
     */
    private fun onPlayComplete() {
        stopProgressUpdate()
        saveCurrentProgress()
        // 可以在这里添加自动播放下一集的逻辑
    }

    /**
     * 保存当前播放进度
     */
    private fun saveCurrentProgress() {
        val video = _currentVideo.value ?: return
        val state = _playerState.value
        if (state.duration > 0) {
            launch {
                repository.savePlayProgress(
                    video.id,
                    state.currentPosition,
                    state.duration
                )
                repository.recordPlayHistory(video, state.currentPosition)
            }
        }
    }

    /**
     * 更新视频时长
     *
     * @param duration 视频时长（毫秒）
     */
    fun updateDuration(duration: Long) {
        _playerState.value = _playerState.value.copy(
            duration = duration,
            isLoading = false,
            playState = PlayStateEnum.READY
        )
    }

    /**
     * 更新缓冲位置
     *
     * @param bufferedPosition 缓冲位置（毫秒）
     */
    fun updateBufferedPosition(bufferedPosition: Long) {
        _playerState.value = _playerState.value.copy(
            bufferedPosition = bufferedPosition
        )
    }

    /**
     * 设置缓冲状态
     *
     * @param isBuffering 是否正在缓冲
     */
    fun setBuffering(isBuffering: Boolean) {
        _playerState.value = _playerState.value.copy(
            isBuffering = isBuffering,
            playState = if (isBuffering) PlayStateEnum.BUFFERING else PlayStateEnum.PLAYING
        )
    }

    /**
     * 设置错误状态
     *
     * @param errorMessage 错误信息
     */
    fun setError(errorMessage: String) {
        _playerState.value = _playerState.value.copy(
            isError = true,
            errorMessage = errorMessage,
            playState = PlayStateEnum.ERROR,
            isPlaying = false,
            isLoading = false
        )
        stopProgressUpdate()
    }

    /**
     * 重试播放
     */
    fun retry() {
        val video = _currentVideo.value ?: return
        _playerState.value = PlayerState(
            playState = PlayStateEnum.PREPARING,
            isLoading = true
        )
        loadVideo(video)
    }

    /**
     * 播放下一集
     */
    fun playNextEpisode() {
        val video = _currentVideo.value ?: return
        if (video.hasMultipleEpisodes()) {
            val nextEpisodeIndex = video.currentEpisode + 1
            if (nextEpisodeIndex < video.episodeList.size) {
                val nextVideo = video.copy(currentEpisode = nextEpisodeIndex)
                loadVideo(nextVideo)
            }
        }
    }

    /**
     * 播放上一集
     */
    fun playPreviousEpisode() {
        val video = _currentVideo.value ?: return
        if (video.hasMultipleEpisodes() && video.currentEpisode > 0) {
            val prevVideo = video.copy(currentEpisode = video.currentEpisode - 1)
            loadVideo(prevVideo)
        }
    }

    /**
     * 释放资源
     */
    override fun onCleared() {
        super.onCleared()
        stopProgressUpdate()
        saveCurrentProgress()
    }
}
