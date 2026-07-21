package com.mpvp.ui.components

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Android平台图片解码实现
 *
 * 使用Android的BitmapFactory解码图片
 *
 * @param bytes 图片字节数组
 * @return ImageBitmap对象
 */
actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap {
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
}
