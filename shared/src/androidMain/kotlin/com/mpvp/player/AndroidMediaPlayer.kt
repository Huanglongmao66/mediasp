package com.mpvp.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.mpvp.model.PlayerState
import com.mpvp.model.PlayStateEnum

/**
 * Android ExoPlayer 播放器实现
 *
 * 使用 androidx.media3 ExoPlayer 实现视频播放
 * 支持 mp4、m3u8、直播流等多种格式
 *
 * @property context Android上下文
 * @property listener 播放器事件监听器
 */
class AndroidMediaPlayer(
    private val context: Context,
    private val listener: PlayerEventListener
) : MediaPlayer {

    /** ExoPlayer 实例 */
    private var exoPlayer: ExoPlayer? = null

    /** 播放器状态 */
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    /** 视频地址 */
    private var videoUrl: String = ""

    override fun initialize(url: String) {
        this.videoUrl = url
        release()

        // 创建 ExoPlayer 实例
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            // 设置监听器
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            _state.value = _state.value.copy(
                                isBuffering = true,
                                playState = PlayStateEnum.BUFFERING,
                                isLoading = true
                            )
                            listener.onBufferingStateChanged(true)
                        }
                        Player.STATE_READY -> {
                            val duration = duration.coerceAtLeast(0L)
                            _state.value = _state.value.copy(
                                isBuffering = false,
                                isLoading = false,
                                duration = duration,
                                playState = if (isPlaying) PlayStateEnum.PLAYING else PlayStateEnum.READY
                            )
                            listener.onBufferingStateChanged(false)
                            listener.onPrepared(duration)
                        }
                        Player.STATE_ENDED -> {
                            _state.value = _state.value.copy(
                                isPlaying = false,
                                playState = PlayStateEnum.COMPLETED,
                                isComplete = true
                            )
                            listener.onPlayCompleted()
                        }
                        Player.STATE_IDLE -> {
                            _state.value = _state.value.copy(
                                playState = PlayStateEnum.IDLE
                            )
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.value = _state.value.copy(
                        isPlaying = isPlaying,
                        playState = if (isPlaying) PlayStateEnum.PLAYING else PlayStateEnum.PAUSED
                    )
                    listener.onPlayingStateChanged(isPlaying)
                }

                override fun onPlayerError(error: PlaybackException) {
                    val errorMessage = when (error.errorCode) {
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                            "网络连接失败，请检查网络"
                        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                            "视频文件不存在"
                        PlaybackException.ERROR_CODE_PARSING_UNSUPPORTED ->
                            "不支持的视频格式"
                        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ->
                            "解码器初始化失败"
                        else -> "播放错误: ${error.message}"
                    }
                    _state.value = _state.value.copy(
                        isError = true,
                        errorMessage = errorMessage,
                        playState = PlayStateEnum.ERROR,
                        isPlaying = false,
                        isLoading = false
                    )
                    listener.onError(errorMessage, error.errorCode)
                }

                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    listener.onVideoSizeChanged(videoSize.width, videoSize.height)
                }
            })

            // 设置媒体源
            val mediaItem = MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    override fun play() {
        exoPlayer?.play()
    }

    override fun pause() {
        exoPlayer?.pause()
    }

    override fun stop() {
        exoPlayer?.stop()
        _state.value = _state.value.copy(
            currentPosition = 0L,
            playState = PlayStateEnum.STOPPED
        )
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _state.value = _state.value.copy(currentPosition = positionMs)
    }

    override fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    override fun setVolume(volume: Float) {
        exoPlayer?.volume = volume.coerceIn(0f, 1f)
        _state.value = _state.value.copy(volume = volume, isMuted = volume <= 0f)
    }

    override fun setMuted(muted: Boolean) {
        exoPlayer?.volume = if (muted) 0f else _state.value.volume
        _state.value = _state.value.copy(isMuted = muted)
    }

    override fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }

    override fun getDuration(): Long {
        return exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
    }

    override fun getBufferedPosition(): Long {
        return exoPlayer?.bufferedPosition ?: 0L
    }

    override fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
    }

    override fun isBuffering(): Boolean {
        return _state.value.isBuffering
    }

    override fun release() {
        exoPlayer?.release()
        exoPlayer = null
        _state.value = PlayerState(playState = PlayStateEnum.RELEASED)
    }

    /**
     * 获取 ExoPlayer 实例（用于Surface绑定）
     */
    fun getExoPlayer(): ExoPlayer? = exoPlayer
}

/**
 * Android 平台播放器工厂实现
 */
actual class MediaPlayerFactory actual constructor() {

    /**
     * 创建 Android ExoPlayer 播放器实例
     *
     * 注意：需要通过 [setContext] 设置 Android Context
     *
     * @param listener 事件监听器
     * @return 播放器实例
     */
    actual fun create(listener: PlayerEventListener): MediaPlayer {
        val context = MediaPlayerFactoryContext.context
            ?: throw IllegalStateException("必须先调用 MediaPlayerFactoryContext.initialize() 设置 Context")
        return AndroidMediaPlayer(context, listener)
    }
}

/**
 * 播放器工厂上下文持有器
 *
 * 用于在非Composable环境中传递Android Context给播放器工厂
 */
object MediaPlayerFactoryContext {
    /** Android Context */
    var context: Context? = null

    /**
     * 初始化工厂上下文
     *
     * @param context Android Context
     */
    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
}
