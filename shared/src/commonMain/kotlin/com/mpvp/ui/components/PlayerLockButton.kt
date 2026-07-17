package com.mpvp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 播放器锁屏按钮组件
 *
 * 在播放器左上角显示锁屏/解锁按钮
 * 锁屏后禁用所有手势操作，防止误触
 *
 * @param isLocked 是否已锁定
 * @param visible 按钮是否可见（跟随控制器显示状态）
 * @param onToggleLock 切换锁定状态回调
 */
@Composable
fun PlayerLockButton(
    isLocked: Boolean,
    visible: Boolean,
    onToggleLock: () -> Unit
) {
    // 锁屏状态下始终显示解锁按钮
    // 非锁屏状态下跟随控制器显示
    val showButton = isLocked || visible

    AnimatedVisibility(
        visible = showButton,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            IconButton(
                onClick = onToggleLock,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = if (isLocked) "解锁" else "锁定",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
