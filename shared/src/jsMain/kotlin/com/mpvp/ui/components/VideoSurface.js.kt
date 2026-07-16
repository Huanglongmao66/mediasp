package com.mpvp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.mpvp.player.MediaPlayer
import com.mpvp.player.WebMediaPlayer
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement

/**
 * Web 平台视频渲染Surface实现
 *
 * 使用 HTML5 Video Element 绑定到 DOM 实现真实视频渲染
 */
@Composable
actual fun VideoSurface(
    mediaPlayer: MediaPlayer?,
    modifier: Modifier = Modifier
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .background(Color.Black)
            .onSizeChanged { size ->
                containerSize = size
            },
        contentAlignment = Alignment.Center
    ) {
        if (mediaPlayer is WebMediaPlayer) {
            val videoElement = mediaPlayer.getVideoElement()
            if (videoElement != null) {
                VideoElement(videoElement, containerSize)
            } else {
                Text(
                    text = "准备播放...",
                    color = Color.White
                )
            }
        } else {
            Text(
                text = "视频渲染区域 (Web)",
                color = Color.White
            )
        }
    }
}

/**
 * 渲染 HTML5 Video 元素到 DOM
 */
@Composable
private fun VideoElement(videoElement: HTMLVideoElement, size: IntSize) {
    videoElement.style.width = "${size.width}px"
    videoElement.style.height = "${size.height}px"
    videoElement.style.objectFit = "contain"
    videoElement.style.display = "block"
    videoElement.style.background = "#000000"
}
