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
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.mpvp.player.DesktopMediaPlayer
import com.mpvp.player.MediaPlayer
import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.media.MediaView

/**
 * Desktop 平台视频渲染Surface实现
 *
 * 使用 JavaFX MediaView 绑定 MediaPlayer 实现真实视频渲染
 */
@Composable
actual fun VideoSurface(
    mediaPlayer: MediaPlayer?,
    modifier: Modifier = Modifier
) {
    val viewController = LocalUIViewController.current
    var mediaView by remember { mutableStateOf<MediaView?>(null) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .background(Color.Black)
            .onSizeChanged { size ->
                containerSize = size
                mediaView?.apply {
                    Platform.runLater {
                        fitWidth = size.width.toDouble()
                        fitHeight = size.height.toDouble()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (mediaPlayer is DesktopMediaPlayer) {
            val javafxPlayer = mediaPlayer.getJavaFXPlayer()
            if (javafxPlayer != null) {
                if (mediaView == null) {
                    Platform.runLater {
                        val mv = MediaView(javafxPlayer).apply {
                            isPreserveRatio = true
                            fitWidth = containerSize.width.toDouble()
                            fitHeight = containerSize.height.toDouble()
                        }
                        mediaView = mv

                        val stackPane = StackPane(mv)
                        stackPane.styleClass.add("video-container")

                        viewController.window.content = stackPane
                    }
                }
            } else {
                Text(
                    text = "准备播放...",
                    color = Color.White
                )
            }
        } else {
            Text(
                text = "视频渲染区域 (Desktop)",
                color = Color.White
            )
        }
    }
}
