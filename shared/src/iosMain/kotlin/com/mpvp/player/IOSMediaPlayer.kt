package com.mpvp.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.mpvp.model.PlayerState
import com.mpvp.model.PlayStateEnum
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.addKeyValueObserver
import platform.AVFoundation.currentItem
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.rate
import platform.AVFoundation.removeKeyValueObserver
import platform.AVFoundation.seekToTime
import platform.AVFoundation.setVolume
import platform.AVFoundation.timeControlStatus
import platform.AVFoundation.AVPlayerTimeControlStatusPaused
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSTimer
import platform.Foundation.NSURL
import platform.darwin.NSObject
import platform.darwin.NSKeyValueObservingOptionNew
import kotlin.math.roundToInt

/**
 * iOS 平台播放器实现
 *
 * 使用 AVPlayer 实现视频播放
 * 支持本地视频文件和网络视频流
 *
 * @property listener 播放器事件监听器
 */
class IOSMediaPlayer(
    private val listener: PlayerEventListener
) : MediaPlayer {

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    /** AVPlayer实例 */
    private var avPlayer: AVPlayer? = null

    /** 进度更新定时器 */
    private var progressTimer: NSTimer? = null

    /** 是否已释放 */
    private var isReleased = false

    /** 状态观察者 */
    private val statusObserver = StatusObserver()

    /** 播放状态观察者 */
    private val timeControlObserver = TimeControlObserver()

    /**
     * 获取AVPlayer实例（用于绑定AVPlayerLayer）
     */
    fun getAVPlayer(): AVPlayer? = avPlayer

    override fun initialize(url: String) {
        release()

        _state.value = PlayerState(
            playState = PlayStateEnum.PREPARING,
            isLoading = true
        )

        val nsUrl = NSURL.URLWithString(url) ?: run {
            _state.value = _state.value.copy(
                isError = true,
                errorMessage = "无效的视频地址",
                playState = PlayStateEnum.ERROR,
                isLoading = false
            )
            listener.onError("无效的视频地址")
            return
        }

        val asset = AVURLAsset.URLAssetWithURL(nsUrl, null)
        val playerItem = AVPlayerItem(playerItemWithAsset = asset)
        val player = AVPlayer(playerWithPlayerItem = playerItem)

        avPlayer = player

        // 观察播放器项状态
        playerItem.addKeyValueObserver("status", options = NSKeyValueObservingOptionNew) { _, _ ->
            when (playerItem.status) {
                AVPlayerItemStatusReadyToPlay -> {
                    val durationMs = (playerItem.duration.value * 1000 / playerItem.duration.timescale).toLong()
                    _state.value = _state.value.copy(
                        duration = durationMs,
                        isLoading = false,
                        playState = PlayStateEnum.READY
                    )
                    listener.onPrepared(durationMs)
                    startProgressUpdate()
                }
                AVPlayerItemStatusFailed -> {
                    val errorMsg = playerItem.error?.localizedDescription ?: "视频加载失败"
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = errorMsg,
                        playState = PlayStateEnum.ERROR,
                        isLoading = false
                    )
                    listener.onError(errorMsg)
                }
                else -> {}
            }
        }

        // 观察播放控制状态
        player.addKeyValueObserver("timeControlStatus", options = NSKeyValueObservingOptionNew) { _, _ ->
            when (player.timeControlStatus) {
                AVPlayerTimeControlStatusPlaying -> {
                    _state.value = _state.value.copy(
                        isPlaying = true,
                        isBuffering = false,
                        isLoading = false,
                        playState = PlayStateEnum.PLAYING
                    )
                    listener.onPlayingStateChanged(true)
                }
                AVPlayerTimeControlStatusPaused -> {
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        playState = PlayStateEnum.PAUSED
                    )
                    listener.onPlayingStateChanged(false)
                }
                AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate -> {
                    _state.value = _state.value.copy(
                        isBuffering = true,
                        isLoading = true
                    )
                    listener.onBufferingStateChanged(true)
                }
                else -> {}
            }
        }

        // 监听播放完成
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = playerItem,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            if (!isReleased) {
                _state.value = _state.value.copy(
                    isPlaying = false,
                    playState = PlayStateEnum.COMPLETED,
                    isComplete = true
                )
                listener.onPlayCompleted()
                stopProgressUpdate()
            }
        }
    }

    /**
     * 开始进度更新
     */
    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressTimer = NSTimer.scheduledTimerWithTimeInterval(0.5, true) {
            if (isReleased) return@scheduledTimerWithTimeInterval
            val player = avPlayer ?: return@scheduledTimerWithTimeInterval
            val currentTime = player.currentTime()
            val positionMs = (currentTime.value * 1000 / currentTime.timescale).toLong()
            val duration = _state.value.duration

            _state.value = _state.value.copy(currentPosition = positionMs)
            listener.onProgressChanged(positionMs, duration)
        }
    }

    /**
     * 停止进度更新
     */
    private fun stopProgressUpdate() {
        progressTimer?.invalidate()
        progressTimer = null
    }

    override fun play() {
        if (isReleased) return
        avPlayer?.play()
    }

    override fun pause() {
        if (isReleased) return
        avPlayer?.pause()
    }

    override fun stop() {
        if (isReleased) return
        avPlayer?.pause()
        avPlayer?.seekToTime(platform.AVFoundation.CMTimeMakeWithSeconds(0.0, 1000))
        stopProgressUpdate()
        _state.value = _state.value.copy(
            isPlaying = false,
            currentPosition = 0L,
            playState = PlayStateEnum.STOPPED
        )
    }

    override fun seekTo(positionMs: Long) {
        if (isReleased) return
        val time = platform.AVFoundation.CMTimeMakeWithSeconds(positionMs.toDouble() / 1000.0, 1000)
        avPlayer?.seekToTime(time)
        _state.value = _state.value.copy(currentPosition = positionMs)
        listener.onProgressChanged(positionMs, _state.value.duration)
    }

    override fun setPlaybackSpeed(speed: Float) {
        if (isReleased) return
        avPlayer?.rate = speed
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    override fun setVolume(volume: Float) {
        if (isReleased) return
        avPlayer?.setVolume(volume.coerceIn(0f, 1f))
        _state.value = _state.value.copy(volume = volume.coerceIn(0f, 1f), isMuted = volume <= 0f)
    }

    override fun setMuted(muted: Boolean) {
        if (isReleased) return
        avPlayer?.muted = muted
        _state.value = _state.value.copy(isMuted = muted)
    }

    override fun getCurrentPosition(): Long {
        val player = avPlayer ?: return _state.value.currentPosition
        val currentTime = player.currentTime()
        return (currentTime.value * 1000 / currentTime.timescale).toLong()
    }

    override fun getDuration(): Long {
        val player = avPlayer ?: return _state.value.duration
        val item = player.currentItem ?: return _state.value.duration
        return (item.duration.value * 1000 / item.duration.timescale).toLong()
    }

    override fun getBufferedPosition(): Long {
        val player = avPlayer ?: return _state.value.bufferedPosition
        val item = player.currentItem ?: return _state.value.bufferedPosition
        val loadedRanges = item.loadedTimeRanges
        if (loadedRanges.isEmpty()) return 0L
        val range = loadedRanges.first() as? platform.AVFoundation.NSValue ?: return 0L
        val timeRange = range.CMTimeRangeValue()
        val endMs = (timeRange.end.value * 1000 / timeRange.end.timescale).toLong()
        return endMs
    }

    override fun isPlaying(): Boolean {
        return avPlayer?.timeControlStatus == AVPlayerTimeControlStatusPlaying
    }

    override fun isBuffering(): Boolean {
        return avPlayer?.timeControlStatus == AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
    }

    override fun release() {
        isReleased = true
        stopProgressUpdate()
        avPlayer?.pause()
        avPlayer?.removeKeyValueObserver(timeControlObserver, "timeControlStatus")
        avPlayer?.currentItem?.removeKeyValueObserver(statusObserver, "status")
        NSNotificationCenter.defaultCenter.removeObserver(this)
        avPlayer = null
        _state.value = PlayerState(playState = PlayStateEnum.RELEASED)
    }

    /**
     * 状态观察者
     */
    private inner class StatusObserver : NSObject()

    /**
     * 播放控制观察者
     */
    private inner class TimeControlObserver : NSObject()
}

/**
 * iOS 平台播放器工厂实现
 */
actual class MediaPlayerFactory actual constructor() {
    actual fun create(listener: PlayerEventListener): MediaPlayer {
        return IOSMediaPlayer(listener)
    }
}
