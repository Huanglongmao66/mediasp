package com.mpvp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mpvp.player.MediaPlayer

/**
 * Desktop 平台视频渲染Surface实现
 *
 * 当前为占位实现，仅渲染黑色背景与提示文字。
 * 后续可通过 SwingInterop 嵌入 JavaFX MediaView / VLCJ 播放面板，
 * 只需在此 Composable 中挂载原生组件即可。
 *
 * @param mediaPlayer 播放器实例
 * @param modifier 修饰符
 */
@Composable
actual fun VideoSurface(
    mediaPlayer: MediaPlayer?,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val hint = when {
            mediaPlayer == null -> "视频渲染区域 (Desktop)"
            else -> "准备播放..."
        }
        Text(
            text = hint,
            color = Color.White
        )
    }
}
