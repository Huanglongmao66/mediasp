package com.mpvp.repository

import com.mpvp.model.RadioItem

/**
 * 电台仓库
 *
 * 框架阶段使用内置示例数据，后续可替换为网络电台源实现。
 */
class RadioRepository : InMemoryMediaRepository<RadioItem>(
    initialItems = listOf(
        RadioItem(
            id = "radio_1",
            title = "示例新闻广播",
            frequency = "FM 98.5",
            category = "新闻",
            sourceUrl = "https://example.com/radio/news.m3u8"
        ),
        RadioItem(
            id = "radio_2",
            title = "示例音乐电台",
            frequency = "FM 101.2",
            category = "音乐",
            sourceUrl = "https://example.com/radio/music.m3u8"
        ),
        RadioItem(
            id = "radio_3",
            title = "示例交通广播",
            frequency = "FM 103.9",
            category = "交通",
            sourceUrl = "https://example.com/radio/traffic.m3u8"
        )
    )
) {
    override fun updateFavorite(item: RadioItem, isFavorite: Boolean): RadioItem =
        item.copy(isFavorite = isFavorite)
}
