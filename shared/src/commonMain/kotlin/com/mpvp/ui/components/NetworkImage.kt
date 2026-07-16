package com.mpvp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.mpvp.utils.NetworkUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * 跨平台网络图片加载组件
 *
 * 提供网络图片加载功能，支持：
 * - 异步加载网络图片
 * - 加载中动画
 * - 加载失败占位图
 * - 图片缓存
 *
 * @property url 图片地址
 * @property contentDescription 内容描述
 * @property modifier 修饰符
 * @property contentScale 内容缩放方式
 * @property httpClient HTTP客户端
 */
@Composable
fun NetworkImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    httpClient: HttpClient? = null
) {
    var imageBitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(url) { mutableStateOf(true) }
    var loadError by remember(url) { mutableStateOf(false) }

    // 加载图片
    LaunchedEffect(url) {
        if (url.isNullOrBlank()) {
            isLoading = false
            loadError = true
            return@LaunchedEffect
        }

        isLoading = true
        loadError = false

        try {
            val client = httpClient ?: NetworkUtils.createHttpClient()
            val imageBytes = withContext(Dispatchers.IO) {
                val response = client.get(url)
                response.bodyAsChannel().toByteArray()
            }

            // 将字节数组转换为ImageBitmap
            // 注意：这里使用平台特定的解码方式
            imageBitmap = decodeImageBitmap(imageBytes)
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            loadError = true
        }
    }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
            loadError || imageBitmap == null -> {
                Icon(
                    imageVector = Icons.Filled.BrokenImage,
                    contentDescription = "图片加载失败",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            else -> {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
        }
    }
}

/**
 * 图片加载状态
 */
sealed class ImageLoadState {
    object Loading : ImageLoadState()
    data class Success(val bitmap: ImageBitmap) : ImageLoadState()
    data class Error(val throwable: Throwable? = null) : ImageLoadState()
}

/**
 * 平台特定的图片解码函数
 *
 * 将字节数组解码为ImageBitmap
 *
 * @param bytes 图片字节数组
 * @return ImageBitmap对象
 */
expect fun decodeImageBitmap(bytes: ByteArray): ImageBitmap
