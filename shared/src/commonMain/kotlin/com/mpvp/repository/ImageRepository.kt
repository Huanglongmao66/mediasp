package com.mpvp.repository

import com.mpvp.model.ImageItem

/**
 * 图片仓库
 *
 * 框架阶段使用内置示例数据，后续可替换为本地相册/网络图源实现。
 */
class ImageRepository : InMemoryMediaRepository<ImageItem>(
    initialItems = listOf(
        ImageItem(
            id = "image_1",
            title = "风景图一",
            width = 1920,
            height = 1080,
            sourceUrl = "https://example.com/image/landscape1.jpg"
        ),
        ImageItem(
            id = "image_2",
            title = "风景图二",
            width = 1080,
            height = 1920,
            sourceUrl = "https://example.com/image/portrait1.jpg"
        ),
        ImageItem(
            id = "image_3",
            title = "风景图三",
            width = 2048,
            height = 1365,
            sourceUrl = "https://example.com/image/landscape2.jpg"
        ),
        ImageItem(
            id = "image_4",
            title = "风景图四",
            width = 1200,
            height = 1600,
            sourceUrl = "https://example.com/image/portrait2.jpg"
        )
    )
) {
    override fun updateFavorite(item: ImageItem, isFavorite: Boolean): ImageItem =
        item.copy(isFavorite = isFavorite)
}
