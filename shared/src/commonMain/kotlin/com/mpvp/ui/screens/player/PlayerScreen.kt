package com.mpvp.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mpvp.model.VideoItem
import com.mpvp.ui.components.PlaylistPanel
import com.mpvp.ui.components.VideoPlayer
import com.mpvp.viewmodel.PlayerViewModel

/**
 * 视频播放详情页
 *
 * 全屏视频播放界面，包含播放器和控制器
 *
 * @param viewModel 播放器ViewModel
 * @param video 视频对象
 * @param onBackClick 返回回调
 * @param onToggleFavorite 收藏切换回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    video: VideoItem,
    onBackClick: () -> Unit,
    onToggleFavorite: (VideoItem) -> Unit
) {
    val playerState by viewModel.playerState.collectAsState()
    val showController by viewModel.showController.collectAsState()
    val currentVideo by viewModel.currentVideo.collectAsState()
    val playlist by viewModel.playlist.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showSpeedPanel by remember { mutableStateOf(false) }
    var showPlaylist by remember { mutableStateOf(false) }

    // 加载视频
    androidx.compose.runtime.LaunchedEffect(video.id) {
        if (currentVideo?.id != video.id) {
            viewModel.loadVideo(video)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 顶部控制栏
        if (showController) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }

                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // 收藏按钮
                IconButton(onClick = { onToggleFavorite(video) }) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "收藏",
                        tint = if (video.isFavorite) MaterialTheme.colorScheme.secondary else Color.White
                    )
                }

                // 播放列表按钮
                IconButton(onClick = { showPlaylist = true }) {
                    Icon(
                        imageVector = Icons.Filled.PlaylistPlay,
                        contentDescription = "播放列表",
                        tint = Color.White
                    )
                }

                // 更多按钮
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "更多",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("播放设置") },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("分享") },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("详细信息") },
                            onClick = { showMenu = false }
                        )
                    }
                }
            }
        }

        // 播放器区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            VideoPlayer(
                state = playerState,
                mediaPlayer = viewModel.getMediaPlayer(),
                showController = showController,
                modifier = Modifier.fillMaxSize(),
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeekTo = { position -> viewModel.seekTo(position) },
                onSeekForward = { viewModel.seekForward() },
                onSeekBackward = { viewModel.seekBackward() },
                onPreviousEpisode = { viewModel.playPreviousEpisode() },
                onNextEpisode = { viewModel.playNextEpisode() },
                onToggleFullscreen = { viewModel.toggleFullscreen() },
                onToggleMute = { viewModel.toggleMute() },
                onSpeedClick = { showSpeedPanel = true },
                onRetry = { viewModel.retry() },
                onControllerToggle = {
                    if (showController) {
                        viewModel.hideController()
                    } else {
                        viewModel.showController()
                    }
                },
                onVolumeChanged = { volume -> viewModel.setVolume(volume) },
                onBrightnessChanged = { brightness -> viewModel.setBrightness(brightness) },
                onLongPressStart = { viewModel.startLongPressSpeed() },
                onLongPressEnd = { viewModel.endLongPressSpeed() }
            )

            // 倍速选择面板
            if (showSpeedPanel) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    com.mpvp.ui.components.SpeedSelectionPanel(
                        currentSpeed = playerState.playbackSpeed,
                        onSpeedSelected = { speed -> viewModel.setPlaybackSpeed(speed) },
                        onDismiss = { showSpeedPanel = false }
                    )
                }
            }

            // 播放列表面板
            if (showPlaylist) {
                PlaylistPanel(
                    playlist = playlist,
                    onDismiss = { showPlaylist = false },
                    onVideoSelected = { index ->
                        viewModel.skipToPlaylistIndex(index)
                        showPlaylist = false
                    },
                    onTogglePlayMode = { viewModel.togglePlayMode() }
                )
            }
        }
    }
}
