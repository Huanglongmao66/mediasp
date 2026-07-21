package com.mpvp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mpvp.model.PlayerState
import com.mpvp.player.MediaPlayer
import com.mpvp.utils.TimeFormatter

/**
 * 视频播放器组件
 *
 * 完整的视频播放器界面，包含视频渲染区域和控制器
 * 支持手势控制：亮度调节、音量调节、进度调节、长按倍速
 *
 * @param state 播放器状态
 * @param mediaPlayer 播放器实例（用于视频渲染视图绑定）
 * @param showController 是否显示控制器
 * @param modifier 修饰符
 * @param onTogglePlayPause 切换播放暂停
 * @param onSeekTo 跳转到位置
 * @param onSeekForward 快进
 * @param onSeekBackward 快退
 * @param onPreviousEpisode 上一集
 * @param onNextEpisode 下一集
 * @param onToggleFullscreen 切换全屏
 * @param onToggleMute 切换静音
 * @param onSpeedClick 倍速点击
 * @param onRetry 重试
 * @param onControllerToggle 控制器显示切换
 * @param onVolumeChanged 音量变化
 * @param onBrightnessChanged 亮度变化
 * @param onLongPressStart 长按开始
 * @param onLongPressEnd 长按结束
 */
@Composable
fun VideoPlayer(
    state: PlayerState,
    mediaPlayer: MediaPlayer?,
    showController: Boolean,
    modifier: Modifier = Modifier,
    gestureFeedback: GestureFeedback? = null,
    onTogglePlayPause: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    onSeekForward: () -> Unit = {},
    onSeekBackward: () -> Unit = {},
    onPreviousEpisode: () -> Unit = {},
    onNextEpisode: () -> Unit = {},
    onToggleFullscreen: () -> Unit = {},
    onToggleMute: () -> Unit = {},
    onSpeedClick: () -> Unit = {},
    onRetry: () -> Unit = {},
    onControllerToggle: () -> Unit = {},
    onVolumeChanged: (Float) -> Unit = {},
    onBrightnessChanged: (Float) -> Unit = {},
    onLongPressStart: () -> Unit = {},
    onLongPressEnd: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .playerGestures(
                state = state,
                onTogglePlayPause = onTogglePlayPause,
                onSeekTo = onSeekTo,
                onVolumeChanged = onVolumeChanged,
                onBrightnessChanged = onBrightnessChanged,
                onControllerToggle = onControllerToggle,
                onLongPressStart = onLongPressStart,
                onLongPressEnd = onLongPressEnd
            )
    ) {
        // 视频渲染Surface
        VideoSurface(
            mediaPlayer = mediaPlayer,
            modifier = Modifier.fillMaxSize()
        )

        // 加载中动画
        if (state.isLoading || state.isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation()
            }
        }

        // 错误页面
        if (state.isError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ErrorRetryPage(
                    errorMessage = state.errorMessage ?: "播放失败",
                    onRetry = onRetry
                )
            }
        }

        // 手势反馈UI
        GestureFeedbackOverlay(
            feedback = gestureFeedback,
            modifier = Modifier.fillMaxSize()
        )

        // 中央播放按钮（暂停时显示）
        if (!state.isPlaying && !state.isLoading && !state.isError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "播放",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
            }
        }

        // 控制器
        if (showController && !state.isError) {
            PlayerController(
                state = state,
                modifier = Modifier.align(Alignment.BottomCenter),
                onTogglePlayPause = onTogglePlayPause,
                onSeekTo = onSeekTo,
                onSeekForward = onSeekForward,
                onSeekBackward = onSeekBackward,
                onPreviousEpisode = onPreviousEpisode,
                onNextEpisode = onNextEpisode,
                onToggleFullscreen = onToggleFullscreen,
                onToggleMute = onToggleMute,
                onSpeedClick = onSpeedClick
            )
        }
    }
}

/**
 * 播放器控制器组件
 *
 * 包含进度条、控制按钮等
 */
@Composable
private fun PlayerController(
    state: PlayerState,
    modifier: Modifier = Modifier,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onPreviousEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onToggleMute: () -> Unit,
    onSpeedClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 进度条
        PlayerProgressBar(
            state = state,
            onSeekTo = onSeekTo
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 控制按钮行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：上一集、快退、播放/暂停、快进、下一集
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousEpisode) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "上一集",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onSeekBackward) {
                    Icon(
                        imageVector = Icons.Filled.FastRewind,
                        contentDescription = "快退10秒",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onTogglePlayPause) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (state.isPlaying) "暂停" else "播放",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = onSeekForward) {
                    Icon(
                        imageVector = Icons.Filled.FastForward,
                        contentDescription = "快进10秒",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onNextEpisode) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "下一集",
                        tint = Color.White
                    )
                }
            }

            // 右侧：音量、倍速、全屏
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 倍速按钮
                IconButton(onClick = onSpeedClick) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Speed,
                            contentDescription = "倍速",
                            tint = Color.White
                        )
                        Text(
                            text = TimeFormatter.formatSpeed(state.playbackSpeed),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                // 音量按钮
                IconButton(onClick = onToggleMute) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "音量",
                        tint = Color.White
                    )
                }

                // 全屏按钮
                IconButton(onClick = onToggleFullscreen) {
                    Icon(
                        imageVector = if (state.isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                        contentDescription = "全屏",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 播放进度条组件
 */
@Composable
private fun PlayerProgressBar(
    state: PlayerState,
    onSeekTo: (Long) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 时间显示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isDragging) {
                    TimeFormatter.formatDuration((dragPosition * state.duration).toLong())
                } else {
                    state.getFormattedCurrentPosition()
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
            Text(
                text = state.getFormattedDuration(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 进度条
        Slider(
            value = if (isDragging) dragPosition else state.getProgressPercent(),
            onValueChange = { newValue ->
                isDragging = true
                dragPosition = newValue
            },
            onValueChangeFinished = {
                isDragging = false
                onSeekTo((dragPosition * state.duration).toLong())
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * 加载动画组件
 */
@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "加载中...",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

/**
 * 错误重试页面组件
 */
@Composable
fun ErrorRetryPage(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "播放失败",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onRetry)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "重试",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}
