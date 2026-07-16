package com.mpvp.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

/**
 * Web平台图片解码实现
 *
 * 使用Skia库解码图片
 *
 * @param bytes 图片字节数组
 * @return ImageBitmap对象
 */
actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap {
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}
