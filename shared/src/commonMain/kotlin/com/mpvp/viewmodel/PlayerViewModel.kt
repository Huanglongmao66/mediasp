package com.mpvp.viewmodel

import com.mpvp.model.PlaybackSpeed
import com.mpvp.model.PlayerConfig
import com.mpvp.model.PlayerState
import com.mpvp.model.PlayStateEnum
import com.mpvp.model.Playlist
import com.mpvp.model.VideoItem
import com.mpvp.player.DanmakuManager
import com.mpvp.player.MediaPlayer
import com.mpvp.player.PlayerManager
import com.mpvp.player.PlaylistManager
import com.mpvp.repository.VideoRepository
import com.mpvp.ui.components.GestureFeedback
import com.mpvp.ui.components.GestureFeedbackType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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

    /** 播放器管理器 */
    private val playerManager = PlayerManager()

    /** 播放列表管理器 */
    private val playlistManager = PlaylistManager()

    /** 弹幕管理器 */
    private val danmakuManager = DanmakuManager()

    /** 播放器状态（从PlayerManager同步） */
    val playerState: StateFlow<PlayerState> = playerManager.state

    /** 播放列表状态 */
    val playlist: StateFlow<Playlist> = playlistManager.playlist

    /** 当前播放的视频 */
    private val _currentVideo = MutableStateFlow<VideoItem?>(null)
    val currentVideo: StateFlow<VideoItem?> = _currentVideo.asStateFlow()

    /** 播放器配置 */
    private val _config = MutableStateFlow(PlayerConfig())
    val config: StateFlow<PlayerConfig> = _config.asStateFlow()

    /** 是否显示控制器 */
    private val _showController = MutableStateFlow(true)
    val showController: StateFlow<Boolean> = _showController.asStateFlow()

    /** 手势反馈状态 */
    private val _gestureFeedback = MutableStateFlow<GestureFeedback?>(null)
    val gestureFeedback: StateFlow<GestureFeedback?> = _gestureFeedback.asStateFlow()

    /** 是否锁定屏幕（防误触） */
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    /** 是否显示弹幕发送栏 */
    private val _showDanmakuInput = MutableStateFlow(false)
    val showDanmakuInput: StateFlow<Boolean> = _showDanmakuInput.asStateFlow()

    /** 控制器自动隐藏协程作业 */
    private var controllerHideJob: Job? = null

    /** 保存的播放位置（用于准备完成后跳转） */
    private var pendingSeekPosition: Long = 0L

    init {
        loadConfig()
        observePlayerState()
    }

    /**
     * 观察播放器状态变化
     */
    private fun observePlayerState() {
        launch {
            playerManager.state.collectLatest { state ->
                if (state.isPlaying) {
                    showControllerAutoHide()
                }
                if (state.playState == PlayStateEnum.READY && pendingSeekPosition > 0) {
                    seekTo(pendingSeekPosition)
                    pendingSeekPosition = 0L
                }
                if (state.isComplete) {
                    onPlayComplete()
                }
            }
        }
    }

    /**
     * 获取当前播放器实例（用于视频渲染视图绑定）
     */
    fun getMediaPlayer(): MediaPlayer? = playerManager.getMediaPlayer()

    /**
     * 获取弹幕管理器实例
     */
    fun getDanmakuManager(): DanmakuManager = danmakuManager

    /**
     * 加载播放器配置
     */
    private fun loadConfig() {
        launch {
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
        pendingSeekPosition = 0L

        val videoUrl = video.getPlayUrl()

        launch {
            if (_config.value.rememberPlayPosition) {
                val savedPosition = repository.getPlayProgress(video.id)
                if (savedPosition > 0) {
                    pendingSeekPosition = savedPosition
                }
            }

            playerManager.initialize(videoUrl)

            if (_config.value.autoPlay) {
                playerManager.play()
            }
        }
    }

    /**
     * 播放视频
     */
    fun play() {
        playerManager.play()
        showControllerAutoHide()
    }

    /**
     * 暂停播放
     */
    fun pause() {
        playerManager.pause()
        saveCurrentProgress()
    }

    /**
     * 切换播放/暂停
     */
    fun togglePlayPause() {
        if (playerState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    /**
     * 停止播放
     */
    fun stop() {
        playerManager.stop()
        saveCurrentProgress()
    }

    /**
     * 跳转到指定位置
     *
     * @param position 目标位置（毫秒）
     */
    fun seekTo(position: Long) {
        val duration = playerState.value.duration
        val targetPosition = if (duration > 0) position.coerceIn(0L, duration) else position
        playerManager.seekTo(targetPosition)
    }

    /**
     * 快进
     *
     * @param milliseconds 快进毫秒数（默认5000ms）
     */
    fun seekForward(milliseconds: Long = 5000L) {
        val newPosition = playerState.value.currentPosition + milliseconds
        seekTo(newPosition)
    }

    /**
     * 快退
     *
     * @param milliseconds 快退毫秒数（默认5000ms）
     */
    fun seekBackward(milliseconds: Long = 5000L) {
        val newPosition = playerState.value.currentPosition - milliseconds
        seekTo(newPosition)
    }

    /**
     * 设置播放速度
     *
     * @param speed 播放速度
     */
    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
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
        playerManager.setVolume(volume)
    }

    /**
     * 增加音量
     *
     * @param delta 音量增量
     */
    fun increaseVolume(delta: Float = 0.1f) {
        setVolume(playerState.value.volume + delta)
    }

    /**
     * 减少音量
     *
     * @param delta 音量减量
     */
    fun decreaseVolume(delta: Float = 0.1f) {
        setVolume(playerState.value.volume - delta)
    }

    /**
     * 切换静音
     */
    fun toggleMute() {
        playerManager.setMuted(!playerState.value.isMuted)
    }

    /**
     * 设置亮度
     *
     * @param brightness 亮度（0.0 ~ 1.0）
     */
    fun setBrightness(brightness: Float) {
        playerManager.setBrightness(brightness)
    }

    /**
     * 切换全屏
     */
    fun toggleFullscreen() {
        playerManager.toggleFullscreen()
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
            if (playerState.value.isPlaying) {
                _showController.value = false
            }
        }
    }

    /**
     * 播放完成回调
     */
    private fun onPlayComplete() {
        saveCurrentProgress()
        if (_config.value.autoPlayNext) {
            playNext()
        }
    }

    /**
     * 设置播放列表
     *
     * @param items 视频列表
     * @param startIndex 起始播放索引
     */
    fun setPlaylist(items: List<VideoItem>, startIndex: Int = 0) {
        playlistManager.setPlaylist(items, startIndex)
        playlistManager.getCurrentVideo()?.let { video ->
            loadVideo(video)
        }
    }

    /**
     * 播放下一个视频
     */
    fun playNext() {
        playlistManager.playNext()?.let { video ->
            loadVideo(video)
        }
    }

    /**
     * 播放上一个视频
     */
    fun playPrevious() {
        playlistManager.playPrevious()?.let { video ->
            loadVideo(video)
        }
    }

    /**
     * 跳转到播放列表指定位置
     */
    fun skipToPlaylistIndex(index: Int) {
        playlistManager.skipTo(index)?.let { video ->
            loadVideo(video)
        }
    }

    /**
     * 切换播放模式
     */
    fun togglePlayMode() {
        playlistManager.togglePlayMode()
    }

    /**
     * 添加视频到播放列表
     */
    fun addToPlaylist(video: VideoItem) {
        playlistManager.addVideo(video)
    }

    /**
     * 从播放列表移除视频
     */
    fun removeFromPlaylist(index: Int) {
        playlistManager.removeVideo(index)
    }

    /**
     * 清空播放列表
     */
    fun clearPlaylist() {
        playlistManager.clear()
        stop()
    }

    /**
     * 保存当前播放进度
     */
    private fun saveCurrentProgress() {
        val video = _currentVideo.value ?: return
        val state = playerState.value
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
     * 重试播放
     */
    fun retry() {
        val video = _currentVideo.value ?: return
        loadVideo(video)
    }

    /**
     * 更新手势反馈
     *
     * @param type 反馈类型
     * @param value 当前值
     * @param maxValue 最大值
     */
    fun updateGestureFeedback(
        type: GestureFeedbackType,
        value: Float,
        maxValue: Float = 1f
    ) {
        _gestureFeedback.value = GestureFeedback(type, value, maxValue)
    }

    /**
     * 清除手势反馈
     */
    fun clearGestureFeedback() {
        _gestureFeedback.value = null
    }

    /**
     * 切换锁屏状态
     */
    fun toggleLock() {
        _isLocked.value = !_isLocked.value
        if (_isLocked.value) {
            hideController()
        }
    }

    /**
     * 显示弹幕输入栏
     */
    fun showDanmakuInput() {
        _showDanmakuInput.value = true
        pause()
    }

    /**
     * 隐藏弹幕输入栏
     */
    fun hideDanmakuInput() {
        _showDanmakuInput.value = false
    }

    /**
     * 发送弹幕
     *
     * @param content 弹幕内容
     * @param color 弹幕颜色
     * @param type 弹幕类型
     */
    fun sendDanmaku(
        content: String,
        color: Long = 0xFFFFFFL,
        type: Int = 0
    ) {
        val danmakuType = when (type) {
            1 -> com.mpvp.model.DanmakuType.TOP
            2 -> com.mpvp.model.DanmakuType.BOTTOM
            else -> com.mpvp.model.DanmakuType.SCROLL
        }
        danmakuManager.sendDanmaku(content, color, danmakuType)
        _showDanmakuInput.value = false
        play()
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
        saveCurrentProgress()
        playerManager.release()
    }
}
