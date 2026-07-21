package com.mpvp.model

import kotlinx.serialization.Serializable

/**
 * 图片项数据模型
 *
 * @property id 唯一标识
 * @property title 图片标题
 * @property width 宽度（像素）
 * @property height 高度（像素）
 * @property thumbnailUrl 缩略图URL
 * @property coverUrl 原图URL
 * @property sourceUrl 原图地址
 * @property isFavorite 是否收藏
 */
@Serializable
data class ImageItem(
    override val id: String,
    override val title: String,
    val width: Int = 0,
    val height: Int = 0,
    val thumbnailUrl: String? = null,
    override val coverUrl: String? = null,
    override val sourceUrl: String,
    override val isFavorite: Boolean = false
) : MediaItem {
    override val type: MediaType = MediaType.IMAGE

    /** 宽高比，用于瀑布流布局计算 */
    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 1f
}
