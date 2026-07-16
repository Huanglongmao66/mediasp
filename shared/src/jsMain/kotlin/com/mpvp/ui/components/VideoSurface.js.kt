package com.mpvp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mpvp.player.MediaPlayer

/**
 * Web 平台视频渲染Surface实现
 *
 * Web端占位实现，实际项目可集成 HTML5 Video Element
 */
@Composable
actual fun VideoSurface(
    mediaPlayer: MediaPlayer?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "视频渲染区域 (Web)",
            color = Color.White
        )
    }
}
