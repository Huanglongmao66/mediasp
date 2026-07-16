package com.mpvp.repository

import com.mpvp.model.MusicItem

/**
 * 音乐仓库
 *
 * 框架阶段使用内置示例数据，后续可替换为网络音乐源实现。
 */
class MusicRepository : InMemoryMediaRepository<MusicItem>(
    initialItems = listOf(
        MusicItem(
            id = "music_1",
            title = "示例歌曲一",
            artist = "示例艺术家",
            album = "示例专辑",
            duration = 234_000L,
            coverUrl = null,
            sourceUrl = "https://example.com/music/song1.mp3"
        ),
        MusicItem(
            id = "music_2",
            title = "示例歌曲二",
            artist = "示例艺术家",
            album = "示例专辑",
            duration = 198_000L,
            sourceUrl = "https://example.com/music/song2.mp3"
        ),
        MusicItem(
            id = "music_3",
            title = "示例歌曲三",
            artist = "另一位艺术家",
            duration = 256_000L,
            sourceUrl = "https://example.com/music/song3.mp3"
        )
    )
) {
    override fun updateFavorite(item: MusicItem, isFavorite: Boolean): MusicItem =
        item.copy(isFavorite = isFavorite)
}
