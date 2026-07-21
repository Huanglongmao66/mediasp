package com.mpvp.model

import kotlinx.serialization.Serializable

/**
 * 音乐项数据模型
 *
 * @property id 唯一标识
 * @property title 歌曲名称
 * @property artist 艺术家
 * @property album 专辑
 * @property duration 时长（毫秒）
 * @property coverUrl 封面URL
 * @property sourceUrl 音频地址
 * @property isFavorite 是否收藏
 */
@Serializable
data class MusicItem(
    override val id: String,
    override val title: String,
    val artist: String = "未知艺术家",
    val album: String = "",
    val duration: Long = 0L,
    override val coverUrl: String? = null,
    override val sourceUrl: String,
    override val isFavorite: Boolean = false
) : MediaItem {
    override val type: MediaType = MediaType.MUSIC
}
