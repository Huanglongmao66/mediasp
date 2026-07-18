package com.mpvp.ui.components

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.mpvp.player.AndroidMediaPlayer
import com.mpvp.player.MediaPlayer

/**
 * Android 平台视频渲染Surface实现
 *
 * 使用 PlayerView 包装 ExoPlayer，提供视频渲染和内置控制
 * 这里我们只使用 PlayerView 的视频渲染部分，控制器由 Compose 自定义实现
 */
@Composable
actual fun VideoSurface(
    mediaPlayer: MediaPlayer?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val playerView = remember {
        PlayerView(context).apply {
            useController = false
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    DisposableEffect(mediaPlayer) {
        val androidPlayer = mediaPlayer as? AndroidMediaPlayer
        val exoPlayer = androidPlayer?.getExoPlayer()

        exoPlayer?.let {
            playerView.player = it
        }

        onDispose {
            playerView.player = null
        }
    }

    AndroidView(
        factory = { playerView },
        modifier = modifier
    )
}
