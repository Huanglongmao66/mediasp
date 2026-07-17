package com.mpvp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * 弹幕发送栏组件
 *
 * 提供弹幕输入和发送功能
 *
 * @param visible 是否可见
 * @param onSend 发送弹幕回调
 * @param onDismiss 关闭回调
 */
@Composable
fun DanmakuInputBar(
    visible: Boolean,
    onSend: (String, Long, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFFFFFFL) }
    var selectedType by remember { mutableStateOf(0) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val danmakuColors = listOf(
        0xFFFFFFL to "白色",
        0xFF0000L to "红色",
        0x00FF00L to "绿色",
        0x0000FFL to "蓝色",
        0xFFFF00L to "黄色",
        0xFF00FFL to "粉色"
    )

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 关闭按钮
                IconButton(
                    onClick = {
                        content = ""
                        onDismiss()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "关闭",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 输入框
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("发送弹幕...", color = Color.White.copy(alpha = 0.5f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (content.isNotBlank()) {
                                onSend(content.trim(), selectedColor, selectedType)
                                content = ""
                                keyboardController?.hide()
                            }
                        }
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 发送按钮
                IconButton(
                    onClick = {
                        if (content.isNotBlank()) {
                            onSend(content.trim(), selectedColor, selectedType)
                            content = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "发送",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 颜色选择行（可扩展）
            if (visible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 52.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    danmakuColors.forEach { (color, _) ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(color.toInt()))
                                .padding(2.dp)
                                .clickable { selectedColor = color }
                        ) {
                            if (selectedColor == color) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
