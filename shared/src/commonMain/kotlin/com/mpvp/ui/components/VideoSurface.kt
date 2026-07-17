package com.mpvp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mpvp.player.MediaPlayer

/**
 * 视频渲染Surface组件
 *
 * 跨平台视频渲染视图，各平台提供具体实现：
 * - Android: SurfaceView / PlayerView
 * - Desktop: JavaFX MediaView
 * - iOS: AVPlayerLayer
 * - Web: HTML5 Video Element
 */
@Composable
expect fun VideoSurface(
    mediaPlayer: MediaPlayer?,
    modifier: Modifier = Modifier
)
