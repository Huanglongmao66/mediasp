package com.mpvp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mpvp.model.PlaybackSpeed

/**
 * 倍速选择面板组件
 *
 * 弹出式面板，提供0.5x ~ 3.0x的倍速选择
 *
 * @param currentSpeed 当前倍速
 * @param onSpeedSelected 倍速选择回调
 * @param onDismiss 关闭回调
 * @param modifier 修饰符
 */
@Composable
fun SpeedSelectionPanel(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp)
    ) {
        // 标题行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Speed,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "播放速度",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "关闭",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 倍速选项列表
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
        ) {
            items(PlaybackSpeed.values()) { speed ->
                SpeedItem(
                    speed = speed,
                    isSelected = speed.value == currentSpeed,
                    onClick = {
                        onSpeedSelected(speed.value)
                        onDismiss()
                    }
                )
            }
        }
    }
}

/**
 * 单个倍速选项
 */
@Composable
private fun SpeedItem(
    speed: PlaybackSpeed,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color.White.copy(alpha = 0.2f)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = speed.displayName.substringBefore(" ").ifEmpty { speed.displayName },
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
        )
    }
}

/**
 * 音量控制面板组件
 *
 * 竖向音量条，支持滑动调节
 *
 * @param currentVolume 当前音量（0.0 ~ 1.0）
 * @param isMuted 是否静音
 * @param onVolumeChanged 音量变化回调
 * @param modifier 修饰符
 */
@Composable
fun VolumeControlPanel(
    currentVolume: Float,
    isMuted: Boolean,
    onVolumeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayVolume = if (isMuted) 0f else currentVolume

    Box(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.6f),
                RoundedCornerShape(24.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // 音量百分比显示
            Text(
                text = "${(displayVolume * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 竖向音量条容器
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                // 音量进度
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp * displayVolume)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White)
                )
            }
        }
    }
}
