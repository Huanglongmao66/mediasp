package com.mpvp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mpvp.player.IOSMediaPlayer
import com.mpvp.player.MediaPlayer

/**
 * iOS 平台视频渲染Surface实现
 *
 * 使用 AVPlayerLayer 绑定 AVPlayer 实现真实视频渲染
 */
@Composable
actual fun VideoSurface(
    mediaPlayer: MediaPlayer?,
    modifier: Modifier
) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (mediaPlayer is IOSMediaPlayer) {
            val avPlayer = mediaPlayer.getAVPlayer()
            if (avPlayer != null) {
                IosVideoLayer(avPlayer)
            } else {
                Text(
                    text = "准备播放...",
                    color = Color.White
                )
            }
        } else {
            Text(
                text = "视频渲染区域 (iOS)",
                color = Color.White
            )
        }
    }
}
