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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mpvp.model.PlayMode
import com.mpvp.model.Playlist
import com.mpvp.model.VideoItem

/**
 * 播放列表面板组件
 *
 * 显示播放列表，支持点击切换视频、切换播放模式
 *
 * @param playlist 播放列表
 * @param onDismiss 关闭面板
 * @param onVideoSelected 选择视频
 * @param onTogglePlayMode 切换播放模式
 */
@Composable
fun PlaylistPanel(
    playlist: Playlist,
    onDismiss: () -> Unit,
    onVideoSelected: (Int) -> Unit,
    onTogglePlayMode: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(playlist.currentIndex) {
        if (playlist.items.isNotEmpty()) {
            listState.animateScrollToItem(playlist.currentIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "播放列表 (${playlist.size()})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 播放模式按钮
                    IconButton(onClick = onTogglePlayMode) {
                        Icon(
                            imageVector = when (playlist.playMode) {
                                PlayMode.LIST_ORDER -> Icons.Filled.Repeat
                                PlayMode.SINGLE_REPEAT -> Icons.Filled.RepeatOne
                                PlayMode.LIST_REPEAT -> Icons.Filled.Repeat
                                PlayMode.RANDOM -> Icons.Filled.Shuffle
                            },
                            contentDescription = playlist.playMode.displayName,
                            tint = Color.White
                        )
                    }
                    Text(
                        text = playlist.playMode.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 关闭按钮
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "关闭",
                            tint = Color.White
                        )
                    }
                }
            }

            Divider(color = Color.White.copy(alpha = 0.1f))

            // 播放列表
            if (playlist.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "播放列表为空",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(playlist.items) { index, item ->
                        PlaylistItem(
                            video = item,
                            isPlaying = index == playlist.currentIndex,
                            onClick = { onVideoSelected(index) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 播放列表项组件
 */
@Composable
private fun PlaylistItem(
    video: VideoItem,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isPlaying) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 播放状态图标
        if (isPlaying) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "正在播放",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = "${video.id.takeLast(3)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.width(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 视频标题
        Text(
            text = video.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // 时长
        if (video.duration > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = com.mpvp.utils.TimeFormatter.formatDuration(video.duration),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
