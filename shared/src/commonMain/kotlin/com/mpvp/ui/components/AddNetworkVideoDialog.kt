package com.mpvp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/**
 * 添加网络视频对话框
 *
 * 提供输入网络视频URL和标题的表单
 *
 * @param onConfirm 确认添加回调，参数为URL和标题
 * @param onDismiss 取消回调
 */
@Composable
fun AddNetworkVideoDialog(
    onConfirm: (url: String, title: String, coverUrl: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }
    var urlError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加网络视频") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // URL输入框
                InputField(
                    icon = Icons.Filled.Link,
                    label = "视频地址 *",
                    placeholder = "输入m3u8或mp4地址",
                    value = url,
                    onValueChange = {
                        url = it
                        urlError = null
                    },
                    errorMessage = urlError
                )

                // 标题输入框
                InputField(
                    icon = Icons.Filled.Title,
                    label = "视频标题",
                    placeholder = "输入视频标题（可选）",
                    value = title,
                    onValueChange = { title = it }
                )

                // 封面URL输入框
                InputField(
                    icon = Icons.Filled.Link,
                    label = "封面地址",
                    placeholder = "输入封面图片地址（可选）",
                    value = coverUrl,
                    onValueChange = { coverUrl = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // 验证URL
                    if (url.isBlank()) {
                        urlError = "请输入视频地址"
                        return@TextButton
                    }
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        urlError = "请输入有效的http/https地址"
                        return@TextButton
                    }

                    // 如果标题为空，使用URL中的文件名
                    val finalTitle = if (title.isBlank()) {
                        url.substringAfterLast("/").substringBefore("?").ifBlank { "网络视频" }
                    } else {
                        title
                    }

                    onConfirm(url, finalTitle, coverUrl.ifBlank { null })
                }
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 带图标的输入框
 */
@Composable
private fun InputField(
    icon: ImageVector,
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        isError = errorMessage != null,
        supportingText = if (errorMessage != null) {
            { Text(errorMessage) }
        } else null,
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
