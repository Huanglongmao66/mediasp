package com.mpvp.player

import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
 * 使用 JavaFX Media API 实现视频播放
 * 支持本地视频文件和网络视频流
 *
 * @property listener 播放器事件监听器
 */
class DesktopMediaPlayer(
    private val listener: PlayerEventListener
) : MediaPlayer.EventListener, MediaPlayer.StatusListener {

    /** 播放器状态 */
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    /** JavaFX MediaPlayer实例 */
    private var javafxPlayer: javafx.scene.media.MediaPlayer? = null

    /** 视频地址 */
    private var videoUrl: String = ""

    /** 是否已释放 */
    private var isReleased = false

    /**
     * 获取JavaFX MediaPlayer实例（用于绑定MediaView）
     */
    fun getJavaFXPlayer(): javafx.scene.media.MediaPlayer? = javafxPlayer

    override fun initialize(url: String) {
        this.videoUrl = url
        release()

        _state.value = PlayerState(
            playState = PlayStateEnum.PREPARING,
            isLoading = true
        )

        Platform.runLater {
            try {
                val media = Media(url)
                val player = javafx.scene.media.MediaPlayer(media).apply {
                    onEndOfMedia = Runnable {
                        if (!isReleased) {
                            _state.value = _state.value.copy(
                                isPlaying = false,
                                playState = PlayStateEnum.COMPLETED,
                                isComplete = true
                            )
                            listener.onPlayCompleted()
                        }
                    }

                    onError = Runnable {
                        if (!isReleased) {
                            val errorMessage = error?.message ?: "播放错误"
                            _state.value = _state.value.copy(
                                isError = true,
                                errorMessage = errorMessage,
                                playState = PlayStateEnum.ERROR,
                                isLoading = false
                            )
                            listener.onError(errorMessage)
                        }
                    }

                    statusProperty().addListener { _, _, newStatus ->
                        when (newStatus) {
                            javafx.scene.media.MediaPlayer.Status.READY -> {
                                val duration = (media.duration.toMillis()).toLong()
                                _state.value = _state.value.copy(
                                    duration = duration,
                                    isLoading = false,
                                    playState = PlayStateEnum.READY
                                )
                                listener.onPrepared(duration)
                                startProgressUpdate()
                            }
                            javafx.scene.media.MediaPlayer.Status.PLAYING -> {
                                _state.value = _state.value.copy(
                                    isPlaying = true,
                                    playState = PlayStateEnum.PLAYING
                                )
                                listener.onPlayingStateChanged(true)
                            }
                            javafx.scene.media.MediaPlayer.Status.PAUSED -> {
                                _state.value = _state.value.copy(
                                    isPlaying = false,
                                    playState = PlayStateEnum.PAUSED
                                )
                                listener.onPlayingStateChanged(false)
                            }
                            javafx.scene.media.MediaPlayer.Status.STOPPED -> {
                                _state.value = _state.value.copy(
                                    isPlaying = false,
                                    playState = PlayStateEnum.STOPPED
                                )
                            }
                            javafx.scene.media.MediaPlayer.Status.BUFFERING -> {
                                _state.value = _state.value.copy(
                                    isBuffering = true,
                                    isLoading = true
                                )
                                listener.onBufferingStateChanged(true)
                            }
                            else -> {}
                        }
                    }
                }

                javafxPlayer = player
                player.prepare()

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
        }
    }

    /**
     * 开始进度更新
     */
    private fun startProgressUpdate() {
        GlobalScope.launch(Dispatchers.Main) {
            while (!isReleased && _state.value.isPlaying) {
                delay(500)
                javafxPlayer?.let { player ->
                    val position = player.currentTime.toMillis().toLong()
                    val duration = player.totalDuration.toMillis().toLong()
                    val buffered = player.bufferProgressTime.toMillis().toLong()

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
        Platform.runLater {
            javafxPlayer?.play()
        }
    }

    override fun pause() {
        if (isReleased) return
        Platform.runLater {
            javafxPlayer?.pause()
        }
    }

    override fun stop() {
        if (isReleased) return
        Platform.runLater {
            javafxPlayer?.stop()
        }
        _state.value = _state.value.copy(
            isPlaying = false,
            currentPosition = 0L,
            playState = PlayStateEnum.STOPPED
        )
    }

    override fun seekTo(positionMs: Long) {
        if (isReleased) return
        Platform.runLater {
            javafxPlayer?.seek(javafx.util.Duration.millis(positionMs.toDouble()))
        }
        _state.value = _state.value.copy(currentPosition = positionMs)
        listener.onProgressChanged(positionMs, _state.value.duration)
    }

    override fun setPlaybackSpeed(speed: Float) {
        Platform.runLater {
            javafxPlayer?.rate = speed.toDouble()
        }
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    override fun setVolume(volume: Float) {
        Platform.runLater {
            javafxPlayer?.volume = volume.toDouble()
        }
        _state.value = _state.value.copy(volume = volume.coerceIn(0f, 1f), isMuted = volume <= 0f)
    }

    override fun setMuted(muted: Boolean) {
        Platform.runLater {
            javafxPlayer?.isMute = muted
        }
        _state.value = _state.value.copy(isMuted = muted)
    }

    override fun getCurrentPosition(): Long {
        return javafxPlayer?.currentTime?.toMillis()?.toLong() ?: _state.value.currentPosition
    }

    override fun getDuration(): Long {
        return javafxPlayer?.totalDuration?.toMillis()?.toLong() ?: _state.value.duration
    }

    override fun getBufferedPosition(): Long {
        return javafxPlayer?.bufferProgressTime?.toMillis()?.toLong() ?: _state.value.bufferedPosition
    }

    override fun isPlaying(): Boolean {
        return javafxPlayer?.status == javafx.scene.media.MediaPlayer.Status.PLAYING
    }

    override fun isBuffering(): Boolean {
        return javafxPlayer?.status == javafx.scene.media.MediaPlayer.Status.BUFFERING
    }

    override fun release() {
        isReleased = true
        Platform.runLater {
            javafxPlayer?.dispose()
            javafxPlayer = null
        }
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
